package site.arksurvey.ocr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream


object AssetUtils {
    /**
     * 获取assets目录下所有文件
     * @param context  上下文
     * @param path  文件夹地址
     * @return files 文件列表
     */
    fun getFileNames(context: Context, path: String): Array<String>? {
        val assetManager = context.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return files
    }

    /**
     * 获取assets目录下的图片
     * @param context 上下文
     * @param path  文件路径
     * @return  Bitmap图片
     */
    fun getImage(context: Context, path: String): Bitmap? {
        var bitmap: Bitmap? = null
        val assetManager = context.assets
        try {
            val inputStream: InputStream = assetManager.open(path)
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }
}