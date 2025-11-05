package hidayatlossen.post5_116

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import hidayatlossen.post5_116.databinding.ActivityMainBinding
import hidayatlossen.post5_116.databinding.BottomSheetImagePickerBinding
import hidayatlossen.post5_116.databinding.CardDialogViewBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbFeed: DatabaseFeed
    private lateinit var feedDao: FeedDao
    private lateinit var appExecutors: AppExecutor
    private lateinit var adapter: FeedAdapter
    private lateinit var horizontalAdapter: FeedHorizontalAdapter

    // Variabel untuk menyimpan URI gambar yang dipilih
    private var selectedImageUri: Uri? = null

    // Flag untuk menandai apakah data dummy sudah dimasukkan
    private var isDummyDataInserted = false

    // Activity result launcher untuk memilih gambar dari file manager
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                showToast("Gambar berhasil dipilih")
            }
        }
    }

    // Data dummy untuk nama dan caption (5 data saja)
    private val dummyNames = listOf(
        "Hidayat lossen",
        "Fadila Lossen",
        "Arvin Setiawan",
        "Wayu Kurnia",
        "Bambang Pamungkas"
    )

    private val dummyCaptions = listOf(
        "Hari yang indah untuk berbagi cerita! ☀️",
        "Exploring new places and making memories 🌍",
        "Coffee time dengan teman baik ☕",
        "Terus belajar dan berkembang 📚",
        "Alam selalu memberikan pemandangan terbaik 🌿"
    )

    // Gambar profile (avatar) - dari gambar1.png sampai gambar5.png
    private val dummyProfileImages = listOf(
        "gambar1",
        "gambar2",
        "gambar3",
        "gambar4",
        "gambar5"
    )

    // Gambar feed utama - dari player1.jpg sampai player5.jpg
    private val dummyFeedImages = listOf(
        "player1",
        "player2",
        "player3",
        "player4",
        "player5"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appExecutors = AppExecutor()
        dbFeed = DatabaseFeed.getDatabase(applicationContext)
        feedDao = dbFeed.feedDao()

        // Bersihkan database setiap aplikasi dibuka
        clearDatabaseOnStart()

        setupRecyclerViews()
        setupObservers()
        insertDummyData() // Memasukkan data dummy

        binding.mtrlBtnAdd.setOnClickListener {
            showAddFeedDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optional: Bersihkan database ketika aplikasi ditutup
        // clearDatabaseOnDestroy()
    }

    private fun clearDatabaseOnStart() {
        appExecutors.diskIO.execute {
            feedDao.deleteAllFeeds()
//            runOnUiThread {
//                showToast("Database dibersihkan")
//            }
            isDummyDataInserted = false // Reset flag
        }
    }

    private fun clearDatabaseOnDestroy() {
        appExecutors.diskIO.execute {
            feedDao.deleteAllFeeds()
        }
    }

    private fun insertDummyData() {
        appExecutors.diskIO.execute {
            // Cek apakah sudah ada data dan flag
            val existingFeeds = feedDao.getAllFeed().value
            if (existingFeeds.isNullOrEmpty() && !isDummyDataInserted) {
                // Insert 5 data dummy saja
                for (i in 0 until 5) {
                    val newFeed = Feed(
                        nama = dummyNames[i],
                        gambarFeed = dummyFeedImages[i],
                        caption = dummyCaptions[i],
                        gambarProfile = dummyProfileImages[i]
                    )
                    feedDao.insert(newFeed)
                }

                isDummyDataInserted = true // Set flag menjadi true

                // Tampilkan toast di main thread
//                runOnUiThread {
//                    showToast("Data dummy berhasil ditambahkan")
//                }
            }
        }
    }

    private fun setupRecyclerViews() {
        // Horizontal RecyclerView
        binding.rvRoomDbHorizontal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Vertical RecyclerView
        binding.rvRoomDbVertikal.layoutManager = LinearLayoutManager(this)

        adapter = FeedAdapter(emptyList(), object : FeedAdapter.FeedClickListener {
            override fun onEditClick(feed: Feed) {
                showEditFeedDialog(feed)
            }

            override fun onDeleteClick(feed: Feed) {
                showDeleteConfirmation(feed)
            }
        })

        horizontalAdapter = FeedHorizontalAdapter(emptyList())

        binding.rvRoomDbVertikal.adapter = adapter
        binding.rvRoomDbHorizontal.adapter = horizontalAdapter
    }

    private fun setupObservers() {
        val feedList = feedDao.getAllFeed()
        feedList.observe(this, Observer { feeds ->
            // Untuk vertical RecyclerView: data terbaru di atas (reversed)
            val verticalData = feeds.reversed()
            adapter.updateData(verticalData)

            // Untuk horizontal RecyclerView: tampilkan maksimal 5 data terbaru
            val horizontalData = feeds.takeLast(5) // Ambil 5 data terakhir
            horizontalAdapter.updateData(horizontalData)
        })
    }

    private fun showAddFeedDialog() {
        val dialogBinding = CardDialogViewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Reset selected image setiap kali dialog dibuka
        selectedImageUri = null

        // Setup untuk memilih gambar dari file manager dengan bottom sheet
        dialogBinding.llAddImage.setOnClickListener {
            showImagePickerBottomSheet()
        }

        dialogBinding.buttonSave.setOnClickListener {
            val nama = dialogBinding.tvUsername.text.toString().trim()
            val caption = dialogBinding.tvCaption.text.toString().trim()

            // Validasi input kosong
            if (nama.isEmpty() || caption.isEmpty()) {
                showToast("Data tidak boleh kosong")
                return@setOnClickListener
            }

            // Pilih gambar secara random dari dummy data
            val randomProfileIndex = (0 until dummyProfileImages.size).random()
            val randomFeedIndex = (0 until dummyFeedImages.size).random()

            val gambarProfile = dummyProfileImages[randomProfileIndex]

            // Jika user memilih gambar, gunakan nama file dari URI
            // Jika tidak, gunakan gambar dummy
            val gambarFeed = if (selectedImageUri != null) {
                getFileNameFromUri(selectedImageUri)
            } else {
                dummyFeedImages[randomFeedIndex]
            }

            val newFeed = Feed(
                nama = nama,
                gambarFeed = gambarFeed,
                caption = caption,
                gambarProfile = gambarProfile
            )

            appExecutors.diskIO.execute {
                feedDao.insert(newFeed)

                // Tampilkan toast di main thread
                runOnUiThread {
                    showToast("Data berhasil disimpan")
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showImagePickerBottomSheet() {
        val bottomSheetBinding = BottomSheetImagePickerBinding.inflate(LayoutInflater.from(this))
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Option 1: Pilih dari File Manager (Universal)
        bottomSheetBinding.optionFileManager.setOnClickListener {
            openFilePicker()
            bottomSheetDialog.dismiss()
        }

        // Option 2: Pilih dari Gallery
        bottomSheetBinding.optionGallery.setOnClickListener {
            openGallery()
            bottomSheetDialog.dismiss()
        }

        // Option 3: Batalkan
        bottomSheetBinding.optionCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*" // Hanya file gambar
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        // Buat chooser untuk memilih aplikasi file manager
        val chooser = Intent.createChooser(intent, "Pilih Gambar dari File Manager")

        try {
            pickImageLauncher.launch(chooser)
        } catch (e: Exception) {
            showToast("Tidak ada aplikasi file manager yang tersedia")
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            pickImageLauncher.launch(intent)
        } catch (e: Exception) {
            showToast("Tidak ada aplikasi gallery yang tersedia")
        }
    }

    private fun getFileNameFromUri(uri: Uri?): String {
        // Selalu gunakan nama berdasarkan timestamp untuk menghindari error
        return "image_${System.currentTimeMillis()}.jpg"
    }

    private fun showEditFeedDialog(feed: Feed) {
        val dialogBinding = CardDialogViewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Reset selected image
        selectedImageUri = null

        // Menggunakan binding untuk set text
        dialogBinding.tvUsername.setText(feed.nama)
        dialogBinding.tvCaption.setText(feed.caption)
        dialogBinding.tvAddPost.text = "Edit Post"
        dialogBinding.tvCreatePost.text = "Edit Post"

        // Tambahkan fungsi pick image juga di edit dialog
        dialogBinding.llAddImage.setOnClickListener {
            showImagePickerBottomSheet()
        }

        dialogBinding.buttonSave.setOnClickListener {
            val nama = dialogBinding.tvUsername.text.toString().trim()
            val caption = dialogBinding.tvCaption.text.toString().trim()

            // Validasi input kosong
            if (nama.isEmpty() || caption.isEmpty()) {
                showToast("Data tidak boleh kosong")
                return@setOnClickListener
            }

            // Jika user memilih gambar baru, gunakan yang baru
            // Jika tidak, pertahankan gambar lama
            val gambarFeed = if (selectedImageUri != null) {
                getFileNameFromUri(selectedImageUri)
            } else {
                feed.gambarFeed
            }

            val updatedFeed = feed.copy(
                nama = nama,
                caption = caption,
                gambarFeed = gambarFeed
            )

            appExecutors.diskIO.execute {
                feedDao.update(updatedFeed)

                // Tampilkan toast di main thread
                runOnUiThread {
                    showToast("Data berhasil diupdate")
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(feed: Feed) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Apakah Anda yakin ingin menghapus post ini?")
            .setPositiveButton("Hapus") { dialog, which ->
                deleteFeed(feed)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteFeed(feed: Feed) {
        appExecutors.diskIO.execute {
            feedDao.delete(feed)

            // Tampilkan toast di main thread
            runOnUiThread {
                showToast("Data berhasil dihapus")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}