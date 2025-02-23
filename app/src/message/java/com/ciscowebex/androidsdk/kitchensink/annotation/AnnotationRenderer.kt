package com.ciscowebex.androidsdk.kitchensink.annotation

import android.content.Context
import android.widget.FrameLayout

class AnnotationRenderer(requireContext: Context) {
    interface AnnotationRendererCallback {
        fun onAnnotationRenderingReady()
        fun onAnnotationRenderingStopped()
    }

    fun startRendering(): Boolean {
        return false
    }

    fun stopRendering() {

    }

    fun renderData(data: String) {

    }

    fun getAnnotationLayer(): FrameLayout? {
        return null
    }

    fun setAnnotationRendererCallback(callback: AnnotationRendererCallback) {

    }
}