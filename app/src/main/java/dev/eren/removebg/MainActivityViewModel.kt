package dev.eren.removebg

import android.graphics.Bitmap
import android.os.Environment
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivityViewModel :
    BaseViewModel<MainActivityViewState, MainActivityAction, MainActivityViewAction>() {
    override val initialState: MainActivityViewState
        get() = MainActivityViewState()

    // ViewAction is a sealed class that will be used to represent all possible communications to the ViewModel
    // Examples for a ViewAction include tapping on a button or entering text in a field.
    override fun handleViewAction(viewAction: MainActivityViewAction) {
        viewModelScope.launch {
            when (viewAction) {
                MainActivityViewAction.SegmentImage -> _action.emit(MainActivityAction.SegmentImage)
                is MainActivityViewAction.SavePicture -> saveBitmapToFile(bitmap = viewAction.bitmap)
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        // Get the directory for the user's public pictures directory.
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )

        // Make sure the Pictures directory exists.
        path.mkdirs()

        // Create a file to save the image.
        val file = File(path, "image.png")

        try {
            // Get the OutputStream of the file.
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap to PNG format and write it to the OutputStream.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            // Close the OutputStream.
            stream.close()

            // Return the absolute path of the saved file.
            return file.absolutePath
        } catch (e: IOException) {
            // Handle errors
            e.printStackTrace()
        }

        return null
    }


}