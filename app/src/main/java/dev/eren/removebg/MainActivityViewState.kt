package dev.eren.removebg

import android.graphics.Bitmap

data class MainActivityViewState(
    val inputImage: Bitmap? = null,
    val maskedImage: Bitmap? = null,
    val outputImage: Bitmap? = null,
    val isLoading: Boolean = false,
    val isImageSegmentationSuccess: Boolean = false,
    val isCustomBackground: Boolean = false,
)