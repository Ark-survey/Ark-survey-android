package site.arksurvey.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect
import android.os.Build
import com.baidu.paddle.lite.MobileConfig
import com.baidu.paddle.lite.PaddlePredictor
import com.baidu.paddle.lite.PowerMode
import com.baidu.paddle.lite.Tensor
import java.nio.ByteBuffer
import java.nio.ByteOrder

class OcrPack(context: Context, model: String="ch-ocrv3") {
    private val config = MobileConfig()

    init {
        val path = "models/$model/inference.pdmodel"
        val buffer = context.assets.open(path).buffered().toString()
        this.config.run {
            modelFromBuffer = buffer
            powerMode = PowerMode.LITE_POWER_HIGH
            threads = 1
        }
    }

    private fun copy2tensor(bitmap: Bitmap, tensor: Tensor) {
        // resize
        val size = LongArray(4).apply {
            this[0] = 1
            this[1] = 1
            this[2] = bitmap.height.toLong()
            this[3] = bitmap.width.toLong()
        }
        tensor.resize(size)
        // copy data
        val data = bitmap.let {
            // bitmap to byte array
            val bytes = it.byteCount
            val buffer = ByteBuffer.allocate(bytes)
            it.copyPixelsToBuffer(buffer)
            buffer.array()
        }.let {
            // byte array to float array
            val floats = ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().array()
            floats.copyOfRange(0, floats.size)
        }
    }

    private fun cropBitmap(bitmap: Bitmap, crop: Rect): Bitmap {
        val src = Rect(0, 0, bitmap.width, bitmap.height)
        return if (crop == src) {
            bitmap
        } else if (bitmap.config != Bitmap.Config.HARDWARE) {
            Bitmap.createBitmap(
                bitmap, crop.left, crop.top, crop.width(), crop.height()
            )
        } else {
            // For hardware bitmaps, use the Picture API to directly create a software bitmap
            val picture = Picture()
            val canvas: Canvas = picture.beginRecording(crop.width(), crop.height())
            canvas.drawBitmap(bitmap, -crop.left.toFloat(), -crop.top.toFloat(), null)
            picture.endRecording()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Bitmap.createBitmap(
                    picture, crop.width(), crop.height(),
                    Bitmap.Config.ARGB_8888
                )
            } else {
                throw IllegalStateException("Not support P")
            }
        }
    }

    fun predict(image: Bitmap, roi: IntArray?=null): String {
        val predictor = PaddlePredictor.createPaddlePredictor(this.config)
        var data = image
        if (roi != null) {
            data = this.cropBitmap(data, Rect(roi[0], roi[1], roi[2], roi[3]))
        }
        val inputTensor = predictor.getInput(0)
        copy2tensor(data, inputTensor)
        predictor.run()
        val outputTensor = predictor.getOutput(0)
        return outputTensor.toString()
    }
}
