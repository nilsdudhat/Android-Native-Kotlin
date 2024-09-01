package com.practical.crawlapps.utils

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("color")
fun loadBackground(view: View?, color: Int?) {
    if ((view != null) && (color != null)) {
        view.setBackgroundResource(color)
    }
}

@BindingAdapter("map")
fun groupID(view: TextView?, map: Map<String, Int>?) {
    if ((view != null) && (map != null)) {
        if (map.containsKey("groupID")) {
            view.text = map["groupID"].toString()
            return
        }
        if (map.containsKey("position")) {
            view.text = map["position"].toString()
        }
    }
}