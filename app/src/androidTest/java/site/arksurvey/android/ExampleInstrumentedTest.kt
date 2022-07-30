package site.arksurvey.android

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import site.arksurvey.ocr.OcrPack
import site.arksurvey.ocr.utils.AssetUtils
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("site.arksurvey.android", appContext.packageName)
    }

    @Test
    fun ocr() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val res = AssetUtils.getImage(testContext, "images/1.jpg")
        assert(res != null)
        println(res)
        val ocr = OcrPack(appContext)
        val result = ocr.predict(res!!)
        println(result)
    }
}