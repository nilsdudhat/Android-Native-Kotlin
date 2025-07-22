package com.belive.dating.activities.notification

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.R
import com.belive.dating.api.user.models.notification.NotificationModel
import com.belive.dating.databinding.ItemNotificationBinding
import com.belive.dating.extensions.getKoinContext

class NotificationAdapter(private val clickListener: NotificationClickListener) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    interface NotificationClickListener {
        fun onNotificationClicked(notification: NotificationModel)
    }

    private val diffUtil = object : DiffUtil.ItemCallback<NotificationModel>() {
        override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem == newItem
        }
    }

    val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.divider.isVisible = position != (asyncListDiffer.currentList.size - 1)

        val notification = asyncListDiffer.currentList[position]
        holder.binding.notification = notification

        holder.binding.title = if (notification.profileData != null) {
            notification.profileData!!.fullName
        } else {
            notification.title
        }

        holder.binding.body = if (notification.createdAt == null) {
            SpannableString(StringBuilder().append(notification.body))
        } else {
            val body = StringBuilder().append(notification.body).append(" ").append(notification.createdAt)
            val spannableMessage = SpannableString(body)

            // Styling for time
            val boldPart = notification.createdAt
            val boldStart = body.toString().indexOf(boldPart.toString())
            val boldEnd = boldStart + boldPart!!.length
            spannableMessage.setSpan(StyleSpan(Typeface.BOLD), boldStart, boldEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableMessage.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(getKoinContext(), R.color.colorTextHint)),
                boldStart,
                boldEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableMessage
        }

        holder.binding.root.setOnClickListener {
            clickListener.onNotificationClicked(notification)
        }
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
}