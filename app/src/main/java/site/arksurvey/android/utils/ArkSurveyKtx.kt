package site.arksurvey.android

import android.content.res.Resources
import android.util.TypedValue

/**
 * Use [Lazy] without synchronize anything to generate object we want
 *
 * Recommend to use it for Ui initialization
 *
 * @param T     Value type
 * @param block Logic to get object we want
 * @return The [Lazy] object to delegate
 */
inline fun <reified T> safeLazy(noinline block: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.PUBLICATION, block)

val Float.dp: Float
    get() {
        val dm = Resources.getSystem().displayMetrics
        val type = TypedValue.COMPLEX_UNIT_DIP
        return TypedValue.applyDimension(type, this, dm)
    }

val Float.sp: Float
    get() {
        val type = TypedValue.COMPLEX_UNIT_SP
        val dm = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(type, this, dm)
    }

val Float.px: Float
    get() {
        val type = TypedValue.COMPLEX_UNIT_PX
        val dm = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(type, this, dm)
    }

val Int.dp: Int
    get() {
        val type = TypedValue.COMPLEX_UNIT_DIP
        val dm = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(type, this.toFloat(), dm).toInt()
    }

val Int.sp: Int
    get() {
        val type = TypedValue.COMPLEX_UNIT_SP
        val dm = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(type, this.toFloat(), dm).toInt()
    }

val Int.px: Int
    get() {
        val type = TypedValue.COMPLEX_UNIT_PX
        val dm = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(type, this.toFloat(), dm).toInt()
    }
