package hidayatlossen.post5_116

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hidayatlossen.post5_116.databinding.AvatarItemBinding

class FeedHorizontalAdapter(
    private val feedList: List<Feed>
) : RecyclerView.Adapter<FeedHorizontalAdapter.FeedHorizontalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHorizontalViewHolder {
        val binding = AvatarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedHorizontalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedHorizontalViewHolder, position: Int) {
        val feed = feedList[position]
        holder.bind(feed)
    }

    override fun getItemCount(): Int = feedList.size

    class FeedHorizontalViewHolder(
        private val binding: AvatarItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feed: Feed) {
            binding.apply {
                tvUsername.text = feed.nama

                // Set gambar profile
                val profileResId = getResourceId(feed.gambarProfile, "drawable")
                if (profileResId != 0) {
                    civProfile.setImageResource(profileResId)
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
    }
}