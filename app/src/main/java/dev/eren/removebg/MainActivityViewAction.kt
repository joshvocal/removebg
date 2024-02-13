package dev.eren.removebg

import android.graphics.Bitmap

interface ViewAction

sealed class MainActivityViewAction : ViewAction {
    object SegmentImage : MainActivityViewAction()
    data class SavePicture(val bitmap: Bitmap) : MainActivityViewAction()
}
