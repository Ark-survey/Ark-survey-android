package site.arksurvey.android.welcome

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX
import site.arksurvey.android.databinding.ActivityWelcomeBinding
import site.arksurvey.android.overlay.OverlayService
import site.arksurvey.android.safeLazy

class WelcomeActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "WelcomeActivity"
    }

    private val binding by safeLazy { ActivityWelcomeBinding.inflate(layoutInflater) }

    //region for client send message to service
    private var serverMessenger: Messenger? = null
    private val connection = WelcomeServiceConnection(
        connected = { serverMessenger = it },
        disconnected = { serverMessenger = null }
    )

    private fun getShowOverlayViewMsg(show: Boolean): Message {
        val bundle = bundleOf(OverlayService.KEY_FOR_OVERLAY_VIEW to show)
        return Message.obtain(null, OverlayService.MSG_WHAT_CLIENT_TO_SERVER)
            .apply { data = bundle }
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
        binding.reset.setOnClickListener {
            serverMessenger?.send(getShowOverlayViewMsg(OverlayService.VALUE_HIDE))
            val msg = "关闭悬浮窗"
            Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show()
        }
        binding.showAlertWindowGlobal.setOnClickListener {
            serverMessenger?.send(getShowOverlayViewMsg(OverlayService.VALUE_SHOW))
        }
        binding.showAlertWindowGlobalWithAccessibility.setOnClickListener {
            val msg =
                "${binding.showAlertWindowGlobalWithAccessibility.text}_${binding.welcomeToolbar.title}"
            Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show()
        }
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
        serverMessenger = null
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
}