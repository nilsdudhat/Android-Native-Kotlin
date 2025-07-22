package com.belive.dating.activities.dashboard.fragments.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.api.user.models.friend.FriendDetails
import com.belive.dating.databinding.ItemFriendMessageBinding
import com.belive.dating.extensions.getUserPrefs
import com.belive.dating.extensions.logger
import com.belive.dating.helpers.helper_functions.swipe_direct.SwipeRevealLayout

class FriendMessageAdapter(
    val viewModel: MessageViewModel,
    val listener: FriendClickListener,
    val swipeListener: OnSwipeStateListener,
    val blockListener: OnBlockListener,
) : RecyclerView.Adapter<FriendMessageAdapter.ViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<FriendDetails>() {
        override fun areItemsTheSame(oldItem: FriendDetails, newItem: FriendDetails): Boolean {
            return (oldItem.friendId == newItem.friendId) && (oldItem.userData.id == newItem.userData.id) && (oldItem.friendData.id == newItem.friendData.id) && (oldItem.lastMessage?.id == newItem.lastMessage?.id) && (oldItem.lastMessage?.message == newItem.lastMessage?.message) && (oldItem.unReadCount == newItem.unReadCount) && (oldItem.isOnline == newItem.isOnline) && (oldItem.isTyping == newItem.isTyping) && (oldItem.lastMessage?.createdAt == newItem.lastMessage?.createdAt) && (oldItem.userData.fullName == newItem.userData.fullName) && (oldItem.friendData.fullName == newItem.friendData.fullName) && (oldItem.userData.userImage == newItem.userData.userImage) && (oldItem.friendData.userImage == newItem.friendData.userImage)
        }

        override fun areContentsTheSame(oldItem: FriendDetails, newItem: FriendDetails): Boolean {
            return oldItem == newItem
        }
    }

    interface FriendClickListener {
        fun onFriendClicked(friendId: Int, friendName: String, friendImage: String)
    }

    interface OnBlockListener {
        fun onBlockClicked(friendId: Int, fullName: String)
    }

    interface OnSwipeStateListener {
        fun onSwipeOpened(position: Int)
        fun onSwipeClosed(position: Int)
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = asyncListDiffer.currentList[position]
        holder.binding.friend = friend
        holder.binding.isLastItem = position == itemCount - 1
        holder.binding.executePendingBindings()

        // Restore swipe state
        holder.itemView.post {
            logger("--swipe--", "Restore swipe position: ${holder.bindingAdapterPosition}: currentSwipePosition: ${viewModel.currentSwipedPosition.get()}")

            if (viewModel.currentSwipedPosition.get() == holder.bindingAdapterPosition) {
                holder.binding.swipeLayout.open(true)
            } else {
                holder.binding.swipeLayout.close(true)
            }
        }

        // Set swipe listener
        holder.binding.swipeLayout.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
            override fun onClosed(view: SwipeRevealLayout?) {
                swipeListener.onSwipeClosed(holder.bindingAdapterPosition)
            }

            override fun onOpened(view: SwipeRevealLayout?) {
                swipeListener.onSwipeOpened(holder.bindingAdapterPosition)
            }

            override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {

            }
        })

        holder.binding.rlDelete.setOnClickListener {
            logger("--swipe--", "Delete Clicked: ${holder.bindingAdapterPosition}")

            holder.binding.swipeLayout.close(true)

            friend?.let { friendDetails ->
                if (getUserPrefs().userId == friendDetails.friendId) {
                    blockListener.onBlockClicked(friendDetails.userData.id, friendDetails.userData.fullName)
                } else {
                    blockListener.onBlockClicked(friendDetails.friendData.id, friendDetails.friendData.fullName)
                }
            }
        }

        holder.binding.main.setOnClickListener {
            if (holder.binding.swipeLayout.isOpened) {
                holder.binding.swipeLayout.close(true)

                holder.binding.swipeLayout.postDelayed({
                    friend?.let { friendDetails ->
                        if (getUserPrefs().userId == friendDetails.friendId) {
                            listener.onFriendClicked(friendDetails.userData.id, friendDetails.userData.fullName, friendDetails.userData.userImage)
                        } else {
                            listener.onFriendClicked(friendDetails.friendData.id, friendDetails.friendData.fullName, friendDetails.friendData.userImage)
                        }
                    }
                }, 300)
            } else {
                friend?.let { friendDetails ->
                    if (getUserPrefs().userId == friendDetails.friendId) {
                        listener.onFriendClicked(friendDetails.userData.id, friendDetails.userData.fullName, friendDetails.userData.userImage)
                    } else {
                        listener.onFriendClicked(friendDetails.friendData.id, friendDetails.friendData.fullName, friendDetails.friendData.userImage)
                    }
                }
            }
        }
    }

    fun closePosition(position: Int) {
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemFriendMessageBinding) : RecyclerView.ViewHolder(binding.root)
}