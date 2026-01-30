package com.ciscowebex.androidsdk.kitchensink.annotation

import android.content.Context
import android.widget.FrameLayout
import com.ciscowebex.androidsdk.annotation.renderer.LiveAnnotationRenderer

class AnnotationRenderer(requireContext: Context) {
    private val renderer = LiveAnnotationRenderer(requireContext)

    interface AnnotationRendererCallback:  LiveAnnotationRenderer.LiveAnnotationRendererCallback{
    }

    fun startRendering(): Boolean {
        return renderer.startRendering()
    }

    fun stopRendering() {
        renderer.stopRendering()
    }

    fun renderData(data: String) {
        renderer.renderData(data)
    }

    fun getAnnotationLayer(): FrameLayout? {
        return renderer.getAnnotationLayer()
    }

    fun setAnnotationRendererCallback(callback: AnnotationRendererCallback) {
        renderer.setAnnotationRendererCallback(callback)
    }
}