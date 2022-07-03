package site.arksurvey.android.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import site.arksurvey.android.R
import site.arksurvey.android.safeLazy

class OverlayService : Service() {
    companion object {
        private const val TAG = "AlertViewService"

        const val MSG_WHAT_CLIENT_TO_SERVICE = 0x114514
        const val MSG_WHAT_SERVICE_TO_CLIENT = 0x1919810

        const val KEY_FOR_OVERLAY_VIEW = "key_for_overlay_view"
        const val VALUE_SHOW = true
        const val VALUE_HIDE = false
    }

    private val messenger = Messenger(OverlayServiceHandler())

    private val windowManager by safeLazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var view: ConstraintLayout? = null


    private fun showOverlayView() {
        val lp = WindowManager.LayoutParams().apply {
            /**
             * 设置type 这里进行了兼容
             */
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
        }
        view = LayoutInflater.from(this).inflate(R.layout.overlay, null) as ConstraintLayout
        windowManager.addView(view, lp)
    }

    private fun hideOverlayView() {
        if (view != null) {
            windowManager.removeView(view)
        }
        view = null
    }


    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        removeOverlayView()
    }

    private fun removeOverlayView() {
        val viewParent = view?.parent as? ViewGroup
        if (view != null && viewParent != null && viewParent.contains(view!!)) {
            windowManager.removeView(view!!)
        }
        view = null
    }


    @SuppressLint("HandlerLeak")
    private inner class OverlayServiceHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            try {
                val what = msg.what
                if (what == MSG_WHAT_CLIENT_TO_SERVICE) {
                    val data = msg.data!!
                    if (!data.containsKey(KEY_FOR_OVERLAY_VIEW))
                        throw IllegalArgumentException("Must send for overlay view")
                    val show = data.getBoolean(KEY_FOR_OVERLAY_VIEW)
                    if (show) showOverlayView() else hideOverlayView()
                    Log.d(TAG, "handleMessage: message from client. what=$what, show=$show")
                    // update the service messenger in client
                    val replyMsg = Message.obtain(null, MSG_WHAT_SERVICE_TO_CLIENT)
                        .apply { replyTo = messenger }
                    msg.replyTo!!.send(replyMsg)
                }
            } catch (e: Exception) {
                Log.w(TAG, "handleMessage", e)
                super.handleMessage(msg)
            }
        }
    }
}