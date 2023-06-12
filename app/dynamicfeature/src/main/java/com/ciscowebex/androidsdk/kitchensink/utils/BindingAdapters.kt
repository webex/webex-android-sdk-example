package com.ciscowebex.androidsdk.kitchensink.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.*

@BindingAdapter("dateString")
fun setDateString(view: TextView, dateInLong: Long){
    view.text = Date(dateInLong).toString()
}