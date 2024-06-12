package kr.hyperlink.app.sample

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kr.hyperlink.app.sample.activities.WebActivity
import kr.hyperlink.app.sample.utils.WebViewUtil

class MainActivity : AppCompatActivity() {
    private val permissions = WebViewUtil.Config.getPermissionsBySite()
    private var handler : Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    companion object {
        private const val REQUEST_PERMISSION = 9999
        private const val REQUEST_PERMISSION_FINAL = 9998
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            runnable?.let {
                handler.removeCallbacks(it)
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (!WebViewUtil.checkPermissions(permissions, this@MainActivity)) {
            ActivityCompat.requestPermissions(this@MainActivity, permissions.toTypedArray(), REQUEST_PERMISSION)
        } else {
            showMain()
        }
        this.onBackPressedDispatcher.addCallback(this@MainActivity, callback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            showMain()
        } else {
            if(requestCode == REQUEST_PERMISSION_FINAL) {
                showMain()
            } else {
                showPermissionWarning()
            }
        }
    }

    private fun showPermissionWarning() {
        val dialogData = WebViewUtil.AlertDialogData (
            title = "경고",
            message = "권한을 획득하여 주시기 바랍니다\n두번 이상 거부하면 권한의 승인이 처리되지 않습니다.",
            negativeMessage = "재 승인",
            positiveMessage = "계속 진행",
            cancellable = false
        )
        WebViewUtil.showDialog(this, dialogData, object: WebViewUtil.AlertDialogListener {
            override fun onClick(dialogType: WebViewUtil.AlertDialogListener.DialogType) {
                if(dialogType == WebViewUtil.AlertDialogListener.DialogType.POSITIVE) {
                    showMain()
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity, permissions.toTypedArray(), REQUEST_PERMISSION_FINAL)
                }
            }
        }).show()
    }


    private fun showMain() {
        runnable = Runnable {
            val initActivity = Intent(this@MainActivity, WebActivity::class.java)
            val extras: Bundle? = intent?.extras
            if(extras != null) {
                val link: String? = extras.getString("link")
                initActivity.putExtra("link", link)
            }
            startActivity(initActivity)
            finish()
        }
        runnable?.let {
            handler.postDelayed(it, WebViewUtil.Config.MAIN_DELAY_TIME)
        }
    }
}
