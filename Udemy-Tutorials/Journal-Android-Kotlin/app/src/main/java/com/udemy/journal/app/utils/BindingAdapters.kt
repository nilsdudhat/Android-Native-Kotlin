package com.udemy.journal.app.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.udemy.journal.app.R

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view).load(url).placeholder(R.drawable.ic_add).into(view)
    }
}

@BindingAdapter("timeAdded")
fun displayTimeAgo(textView: TextView, timeAdded: Timestamp) {
    textView.text = getTimeAgo(timeAdded)
}