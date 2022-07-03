package site.arksurvey.android.overlay

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.roundToInt

class OverlayViewTouchListener(
    private val windowManager: WindowManager,
    private val lp: WindowManager.LayoutParams
) : View.OnTouchListener {
    private var pointX = -1.0f
    private var pointY = -1.0f

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null) return false
        val action = event?.actionMasked ?: return false
        if (action == MotionEvent.ACTION_DOWN) {
            pointX = event.rawX
            pointY = event.rawY
        }
        if (action == MotionEvent.ACTION_MOVE) {
            val dX = event.rawX - pointX
            val dY = event.rawY - pointY
            pointX = event.rawX
            pointY = event.rawY
            lp.x += dX.roundToInt()
            lp.y += dY.roundToInt()
            windowManager.updateViewLayout(v, lp)
        }
        if (action == MotionEvent.ACTION_UP) {
            pointX = -1.0f
            pointY = -1.0f
        }
        return true
    }
}