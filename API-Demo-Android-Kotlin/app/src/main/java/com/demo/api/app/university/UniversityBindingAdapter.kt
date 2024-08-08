package com.demo.api.app.university

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("web_pages")
fun web_pages(textView: TextView, list: List<String>) {
    textView.text = list.joinToString(separator = "\n")
}