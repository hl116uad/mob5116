package hidayatlossen.post5_116

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import hidayatlossen.post5_116.databinding.ActivityMainBinding
import hidayatlossen.post5_116.databinding.CardDialogViewBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbFeed: DatabaseFeed
    private lateinit var feedDao: FeedDao
    private lateinit var appExecutors: AppExecutor
    private lateinit var adapter: FeedAdapter

    // Data dummy untuk nama dan caption
    private val dummyNames = listOf(
        "Alice Johnson",
        "Bob Smith",
        "Carol Davis",
        "David Wilson",
        "Eva Brown",
        "Frank Miller"
    )

    private val dummyCaptions = listOf(
        "Hari yang indah untuk berbagi cerita! ☀️",
        "Exploring new places and making memories 🌍",
        "Coffee time with good friends ☕",
        "Never stop learning and growing 📚",
        "Nature always has the best views 🌿",
        "Creating something amazing today! ✨"
    )

    // Gambar profile (avatar) - dari gambar1.png sampai gambar6.png
    private val dummyProfileImages = listOf(
        "gambar1",
        "gambar2",
        "gambar3",
        "gambar4",
        "gambar5",
        "gambar6"
    )

    // Gambar feed utama - dari player1.jpg sampai player5.jpg
    private val dummyFeedImages = listOf(
        "player1",
        "player2",
        "player3",
        "player4",
        "player5",
        "player1" // karena hanya ada 5, ulang ke player1 untuk yang ke-6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appExecutors = AppExecutor()
        dbFeed = DatabaseFeed.getDatabase(applicationContext)
        feedDao = dbFeed.feedDao()

        setupRecyclerViews()
        setupObservers()
        insertDummyData() // Memasukkan data dummy

        binding.mtrlBtnAdd.setOnClickListener {
            showAddFeedDialog()
        }
    }

    private fun insertDummyData() {
        appExecutors.diskIO.execute {
            // Cek apakah sudah ada data
            val existingFeeds = feedDao.getAllFeed().value
            if (existingFeeds.isNullOrEmpty()) {
                // Insert 6 data dummy
                for (i in 0 until 6) {
                    val newFeed = Feed(
                        nama = dummyNames[i],
                        gambarFeed = dummyFeedImages[i],
                        caption = dummyCaptions[i],
                        gambarProfile = dummyProfileImages[i]
                    )
                    feedDao.insert(newFeed)
                }
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
                deleteFeed(feed)
            }
        })

        binding.rvRoomDbVertikal.adapter = adapter
    }

    private fun setupObservers() {
        val feedList = feedDao.getAllFeed()
        feedList.observe(this, Observer { feeds ->
            adapter.updateData(feeds)

            // Setup horizontal adapter dengan data yang sama
            val horizontalAdapter = FeedHorizontalAdapter(feeds)
            binding.rvRoomDbHorizontal.adapter = horizontalAdapter
        })
    }

    private fun showAddFeedDialog() {
        val dialogBinding = CardDialogViewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // Setup untuk memilih gambar (dummy implementation)
        dialogBinding.llAddImage.setOnClickListener {
            // Implementasi pemilihan gambar
        }

        dialogBinding.buttonSave.setOnClickListener {
            val nama = dialogBinding.tvUsername.text.toString()
            val caption = dialogBinding.tvCaption.text.toString()

            // Pilih gambar secara random dari dummy data
            val randomProfileIndex = (0 until dummyProfileImages.size).random()
            val randomFeedIndex = (0 until dummyFeedImages.size).random()

            val gambarProfile = dummyProfileImages[randomProfileIndex]
            val gambarFeed = dummyFeedImages[randomFeedIndex]

            if (nama.isNotEmpty() && caption.isNotEmpty()) {
                val newFeed = Feed(
                    nama = nama,
                    gambarFeed = gambarFeed,
                    caption = caption,
                    gambarProfile = gambarProfile
                )

                appExecutors.diskIO.execute {
                    feedDao.insert(newFeed)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showEditFeedDialog(feed: Feed) {
        val dialogBinding = CardDialogViewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvUsername.setText(feed.nama)
        dialogBinding.tvCaption.setText(feed.caption)
        dialogBinding.tvAddPost.text = "Edit Post"
        dialogBinding.tvCreatePost.text = "Edit Post"

        dialogBinding.buttonSave.setOnClickListener {
            val nama = dialogBinding.tvUsername.text.toString()
            val caption = dialogBinding.tvCaption.text.toString()

            if (nama.isNotEmpty() && caption.isNotEmpty()) {
                val updatedFeed = feed.copy(
                    nama = nama,
                    caption = caption
                )

                appExecutors.diskIO.execute {
                    feedDao.update(updatedFeed)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun deleteFeed(feed: Feed) {
        appExecutors.diskIO.execute {
            feedDao.delete(feed)
        }
    }
}