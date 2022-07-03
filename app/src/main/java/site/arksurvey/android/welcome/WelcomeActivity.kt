package site.arksurvey.android.welcome

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX
import site.arksurvey.android.databinding.ActivityWelcomeBinding
import site.arksurvey.android.overlay.OverlayService
import site.arksurvey.android.safeLazy

class WelcomeActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "WelcomeActivity"
    }

    private val binding by safeLazy { ActivityWelcomeBinding.inflate(layoutInflater) }

    //region for client send message to service
    private var serviceMessenger: Messenger? = null
    private val messenger = Messenger(WelcomeHandler { serviceMessenger = it })
    private val connection = WelcomeServiceConnection(
        connected = { serviceMessenger = it },
        disconnected = { serviceMessenger = null }
    )

    private fun getShowOverlayViewMsg(show: Boolean): Message {
        val bundle = bundleOf(OverlayService.KEY_FOR_OVERLAY_VIEW to show)
        return Message.obtain(null, OverlayService.MSG_WHAT_CLIENT_TO_SERVICE)
            .apply {
                data = bundle
                replyTo = messenger
            }
    }
    //endregion



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        requestSystemAlertWindowPermission(
            afterAllGranted = {
                val intent = Intent(this, OverlayService::class.java)
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
                val msg = "Starting overlay service..."
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                onCreateInner(savedInstanceState)
            },
            afterPermissionDenied = { deniedList, grantedList ->
                val deniedPermissions = deniedList.joinToString(separator = ", ")
                val grantedPermissions = grantedList.joinToString(separator = ", ")
                val msgIfSomePermissionsAreGranted =
                    if (grantedList.isEmpty()) "" else "You have granted these permissions: $grantedPermissions."
                val msg =
                    "$msgIfSomePermissionsAreGranted You need to accept the permission: $deniedPermissions"
                Log.w(TAG, "onCreate: permission denied. $msg")
                finish()
            }
        )
    }

    private fun onCreateInner(savedInstanceState: Bundle?) {
        binding.reset.setOnClickListener(this)
        binding.showAlertWindowGlobal.setOnClickListener(this)
        binding.showAlertWindowGlobalWithAccessibility.setOnClickListener(this)
    }

    private fun requestSystemAlertWindowPermission(
        afterAllGranted: (grantedList: List<String>) -> Unit,
        afterPermissionDenied: (deniedList: List<String>, grantedList: List<String>) -> Unit
    ) {
        val permission = Manifest.permission.SYSTEM_ALERT_WINDOW
        PermissionX.init(this).permissions(permission)
            .onExplainRequestReason { scope, deniedList ->
                val msg = "You need to accept this permission to use app properly"
                scope.showRequestReasonDialog(deniedList, msg, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList ->
                if (!allGranted) {
                    afterPermissionDenied(deniedList, grantedList)
                    return@request
                }
                afterAllGranted(grantedList)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        unbindService(connection)
        serviceMessenger = null
    }

    override fun onClick(v: View?) {
        val vid = v?.id ?: return
        if (vid == binding.reset.id) {
            serviceMessenger?.send(getShowOverlayViewMsg(OverlayService.VALUE_HIDE))
            serviceMessenger = null
            val msg = "关闭悬浮窗"
            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show()
        }
        if (vid == binding.showAlertWindowGlobal.id) {
            serviceMessenger?.send(getShowOverlayViewMsg(OverlayService.VALUE_SHOW))
            serviceMessenger = null
            val msg = "展示全局显示的悬浮窗（但仍然存在问题）"
            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show()
        }
        if (vid == binding.showAlertWindowGlobalWithAccessibility.id) {
            val msg = "展示全局显示的悬浮窗（待实现）"
            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show()
        }
    }

    private class WelcomeServiceConnection constructor(
        private val connected: (Messenger) -> Unit,
        private val disconnected: () -> Unit
    ) : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connected(Messenger(service))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            disconnected()
        }
    }

    private class WelcomeHandler constructor(
        private val updateServiceMessenger: (Messenger?) -> Unit
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what != OverlayService.MSG_WHAT_SERVICE_TO_CLIENT) return
            updateServiceMessenger(msg.replyTo)
        }
    }
}