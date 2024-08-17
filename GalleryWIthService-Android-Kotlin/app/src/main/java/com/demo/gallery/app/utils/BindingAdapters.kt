package com.demo.gallery.app.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("path")
fun loadImage(view: ImageView, path: String?) {
    if (path != null) {
        Glide.with(view).load(path).into(view)
    }
}