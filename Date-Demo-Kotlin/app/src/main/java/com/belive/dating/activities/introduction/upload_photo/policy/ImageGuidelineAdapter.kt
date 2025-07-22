package com.belive.dating.activities.introduction.upload_photo.policy

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemImageGuidelineBinding

class ImageGuidelineAdapter() : RecyclerView.Adapter<ImageGuidelineAdapter.ViewHolder>() {

    var guidelineList = arrayListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageGuidelineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.index =   position + 2

        holder.binding.guideline = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(
                guidelineList[position],
                Html.FROM_HTML_MODE_LEGACY,
            )
        } else {
            SpannableString(guidelineList[position])
        }
    }

    override fun getItemCount(): Int {
        return guidelineList.size
    }

    class ViewHolder(val binding: ItemImageGuidelineBinding) : RecyclerView.ViewHolder(binding.root)
}