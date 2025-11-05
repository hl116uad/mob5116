package hidayatlossen.post5_116

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import hidayatlossen.post5_116.databinding.FeedCardBinding

class FeedAdapter(
    private var feedList: List<Feed>,
    private val listener: FeedClickListener
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    interface FeedClickListener {
        fun onEditClick(feed: Feed)
        fun onDeleteClick(feed: Feed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = FeedCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val feed = feedList[position]
        holder.bind(feed)
    }

    override fun getItemCount(): Int = feedList.size

    fun updateData(newList: List<Feed>) {
        feedList = newList.reversed()
        notifyDataSetChanged()
    }

    class FeedViewHolder(
        private val binding: FeedCardBinding,
        private val listener: FeedClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentFeed: Feed? = null

        fun bind(feed: Feed) {
            currentFeed = feed

            binding.apply {
                textView2.text = feed.nama
                textView.text = feed.caption

                // Set gambar profile
                val profileResId = getResourceId(feed.gambarProfile, "drawable")
                if (profileResId != 0) {
                    imageView.setImageResource(profileResId)
                }

                // Set gambar feed utama
                val feedResId = getResourceId(feed.gambarFeed, "drawable")
                if (feedResId != 0) {
                    imageView2.setImageResource(feedResId)
                }

                // Setup tombol hapus/edit dengan PopupMenu
                btnHapusEdit.setOnClickListener { view ->
                    showPopupMenu(view, feed)
                }
            }
        }

        private fun getResourceId(resourceName: String, resourceType: String): Int {
            return binding.root.context.resources.getIdentifier(
                resourceName,
                resourceType,
                binding.root.context.packageName
            )
        }

        private fun showPopupMenu(view: View, feed: Feed) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.edit_delete_menu, popup.menu) // Gunakan menu XML

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        listener.onEditClick(feed)
                        true
                    }
                    R.id.menu_delete -> {
                        listener.onDeleteClick(feed)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }
}