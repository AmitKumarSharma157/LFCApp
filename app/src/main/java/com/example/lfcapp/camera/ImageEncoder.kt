package com.example.lfcapp.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max

/**
 * Turns a captured camera frame into the Base64 JPEG string sent to the
 * image-processing API. CPU-heavy: call it off the main thread.
 */
class ImageEncoder @Inject constructor() {

    /** Returns an upright, size-capped copy of a raw camera frame, ready to show and upload. */
    fun prepare(bitmap: Bitmap, rotationDegrees: Int): Bitmap =
        bitmap.rotate(rotationDegrees).scaleDown(MAX_DIMENSION_PX)

    /**
     * Compresses [photo] as hard as needed to fit [MAX_UPLOAD_CHARS] once Base64-encoded:
     * first by lowering JPEG quality (never below [MIN_QUALITY]), then by shrinking the image
     * (never below [MIN_DIMENSION_PX]). If both floors are hit, the smallest readable result is sent.
     */
    fun toBase64Jpeg(photo: Bitmap): String {
        var bitmap = photo
        while (true) {
            val jpeg = bitmap.compressToBudget()
            val longestSide = max(bitmap.width, bitmap.height)
            if (base64Length(jpeg.size) <= MAX_UPLOAD_CHARS || longestSide <= MIN_DIMENSION_PX) {
                if (bitmap !== photo) bitmap.recycle()
                return Base64.encodeToString(jpeg, Base64.NO_WRAP)
            }
            val smaller = bitmap.scaleDown(max(MIN_DIMENSION_PX, (longestSide * SCALE_STEP).toInt()))
            if (bitmap !== photo) bitmap.recycle()
            bitmap = smaller
        }
    }

    // Binary search for the highest quality that fits the budget, so the image is only
    // degraded as much as necessary. Falls back to MIN_QUALITY when nothing fits.
    private fun Bitmap.compressToBudget(): ByteArray {
        var low = MIN_QUALITY
        var high = MAX_QUALITY
        var best: ByteArray? = null
        while (low <= high) {
            val quality = (low + high) / 2
            val jpeg = toJpeg(quality)
            if (base64Length(jpeg.size) <= MAX_UPLOAD_CHARS) {
                best = jpeg
                low = quality + 1
            } else {
                high = quality - 1
            }
        }
        return best ?: toJpeg(MIN_QUALITY)
    }

    private fun Bitmap.toJpeg(quality: Int): ByteArray =
        ByteArrayOutputStream().use { stream ->
            compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }

    // Base64 turns every 3 bytes into 4 characters (with padding).
    private fun base64Length(byteCount: Int): Int = (byteCount + 2) / 3 * 4

    // CameraX delivers frames in sensor orientation; rotating here makes the
    // uploaded image match what the user saw in the preview.
    private fun Bitmap.rotate(degrees: Int): Bitmap {
        if (degrees == 0) return this
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    // Full-resolution photos are several MB once Base64-encoded; capping the
    // longest side keeps uploads fast without losing useful detail.
    private fun Bitmap.scaleDown(maxDimension: Int): Bitmap {
        val longestSide = max(width, height)
        if (longestSide <= maxDimension) return this
        val factor = maxDimension.toFloat() / longestSide
        return scale((width * factor).toInt(), (height * factor).toInt())
    }

    private companion object {
        const val MAX_DIMENSION_PX = 1920

        /** Upload budget: size of the Base64 string actually sent (~100 KB). */
        const val MAX_UPLOAD_CHARS = 100 * 1024
        const val MAX_QUALITY = 80

        // Below these, printed text on labels starts to blur into JPEG artifacts.
        const val MIN_QUALITY = 40
        const val MIN_DIMENSION_PX = 1024
        const val SCALE_STEP = 0.8f
    }
}
