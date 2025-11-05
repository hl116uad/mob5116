package hidayatlossen.post5_116

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hidayatlossen.post5_116.databinding.FeedCardBinding

class CardActivity : AppCompatActivity() {
    private lateinit var binding: FeedCardBinding
    private lateinit var appExecutors: AppExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FeedCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appExecutors = AppExecutor()
        val feedId = intent.getIntExtra("feed_id", -1)

        if (feedId != -1) {
            appExecutors.diskIO.execute {
                val dao = DatabaseFeed.getDatabase(this).feedDao()
                val selectedFeed = dao.getFeedById(feedId)

                selectedFeed?.let { feed ->
                    runOnUiThread {
                        // Implementasi binding data ke view jika diperlukan
                        // binding.tvNama.text = feed.nama
                        // binding.tvCaption.text = feed.caption
                    }
                }
            }
        }
    }
}