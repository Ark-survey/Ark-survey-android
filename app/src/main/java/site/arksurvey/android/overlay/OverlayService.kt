package site.arksurvey.android.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import site.arksurvey.android.R
import site.arksurvey.android.safeLazy

class OverlayService : Service() {
    companion object {
        private const val TAG = "AlertViewService"

        const val MSG_WHAT_CLIENT_TO_SERVICE = 0x114514
        const val MSG_WHAT_SERVICE_TO_CLIENT = 0x1919810

        const val KEY_FOR_OVERLAY_VIEW = "key_for_overlay_view"
        const val VALUE_SHOW = 0
        const val VALUE_REMOVE = 1
        const val VALUE_HIDE = 2
    }

    private val messenger = Messenger(OverlayServiceHandler())

    private val windowManager by safeLazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var view: ConstraintLayout? = null


    private fun showOverlayView() {
        if (view != null) {
            view?.isVisible = true
            return
        }
        val lp = WindowManager.LayoutParams().apply {
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
        view?.setOnTouchListener(OverlayViewTouchListener(windowManager, lp))
        windowManager.addView(view, lp)
    }

    private fun removeOverlayView() {
        if (view != null) {
            windowManager.removeView(view)
        }
        view = null
    }

    private fun hideOverlayView() {
        view?.isVisible = false
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

    @SuppressLint("HandlerLeak")
    private inner class OverlayServiceHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            try {
                val what = msg.what
                if (what == MSG_WHAT_CLIENT_TO_SERVICE) {
                    val data = msg.data!!
                    if (!data.containsKey(KEY_FOR_OVERLAY_VIEW))
                        throw IllegalArgumentException("Must send for overlay view")
                    val action = data.getInt(KEY_FOR_OVERLAY_VIEW)
                    when (action) {
                        VALUE_SHOW -> showOverlayView()
                        VALUE_REMOVE -> removeOverlayView()
                        VALUE_HIDE -> hideOverlayView()
                    }
                    Log.d(TAG, "handleMessage: message from client. what=$what, action=$action")
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

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    @IntDef(VALUE_SHOW, VALUE_REMOVE, VALUE_HIDE)
    annotation class ForOverlayView
}