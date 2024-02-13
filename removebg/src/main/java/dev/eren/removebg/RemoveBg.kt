package dev.eren.removebg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import dev.eren.removebg.common.ModelTypes
import dev.eren.removebg.utils.FileUtils.assetFilePath
import dev.eren.removebg.utils.NetUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils


/**
 * Created by erenalpaslan on 18.08.2023
 */
class RemoveBg(context: Context) : Remover<Bitmap> {

    private var module: Module = LiteModuleLoader.load(
        assetFilePath(
            context,
            ModelTypes.U2NET.fileName
        )
    )
    private val maskPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val size = 320

    init {
        maskPaint.isAntiAlias = true
        maskPaint.style = Paint.Style.FILL
        maskPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
    }

    override fun clearBackground(image: Bitmap): Flow<Bitmap?> = flow {
        val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)
        emit(removeBackground(mutableImage))
    }.flowOn(Dispatchers.IO)

    override fun getMaskedImage(input: Bitmap, mask: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result)

        mCanvas.drawBitmap(input, 0f, 0f, null)
        mCanvas.drawBitmap(mask, 0f, 0f, maskPaint)

        return result

//        return addGradientToBitmap(
//            bitmap = result,
//            startColor = start,
//            endColor = end,
//        )
//
//        return addBackgroundColorToBitmap(
//            bitmap = result,
//            backgroundColor = Color.BLUE
//        )
    }

    fun addGradientToBitmap(bitmap: Bitmap, startColor: Int, endColor: Int): Bitmap {
        // Create a new bitmap with the same dimensions as the original bitmap
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        // Create a canvas with the resultBitmap
        val canvas = Canvas(resultBitmap)

        // Create a gradient shader
        val gradientShader = LinearGradient(
            0f, 0f, 0f, bitmap.height.toFloat(),
            startColor, endColor, Shader.TileMode.CLAMP
        )

        // Create a paint object with the gradient shader
        val paint = Paint().apply {
            shader = gradientShader
        }

        // Draw the gradient on the canvas
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)

        // Draw the original bitmap on top of the gradient
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        return resultBitmap
    }

    fun addBackgroundColorToBitmap(bitmap: Bitmap, backgroundColor: Int): Bitmap {
        // Create a new bitmap with the same dimensions as the original bitmap
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        // Create a canvas with the resultBitmap
        val canvas = Canvas(resultBitmap)

        // Draw the background color on the canvas
        canvas.drawColor(backgroundColor)

        // Draw the original bitmap on top of the background color
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        return resultBitmap
    }

    private fun removeBackground(input: Bitmap): Bitmap? {
        val width = input.width
        val height = input.height

        val scaledBitmap = Bitmap.createScaledBitmap(input, size, size, true)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            scaledBitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )
        val outputTensor = module.forward(IValue.from(inputTensor)).toTuple()
        val arr = outputTensor[0].toTensor().dataAsFloatArray

        val scaledMask = NetUtils.convertArrayToBitmap(arr, size, size)?.let {
            Bitmap.createScaledBitmap(it, width, height, true)
        }

        return scaledMask?.let { getMaskedImage(input, it) }
    }

}