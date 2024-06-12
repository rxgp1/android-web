package kr.hyperlink.app.sample.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kr.hyperlink.app.sample.R
import java.util.UUID

class WebViewUtil {

    // 설정 내용 모음
    class Config {
        companion object {
            // 메인 도메인
            const val BASE_URL: String  = "https://www.hyper-link.kr:18443/server/login"

            // 메인 스플래시 대기 숫자
            const val MAIN_DELAY_TIME = 1000L

            // 유저 에이전트
            const val USER_AGENT = "hyperlink/APP-hyper-link"

            // 자바스크립트 인터페이스
            const val JAVASCRIPT_INTERFACE_NAME = "android"

            // 권한 처리 수정
            fun getPermissionsBySite(): ArrayList<String> {
                val permissions : ArrayList<String> = ArrayList()
                permissions.add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT  >=  Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                    permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
                    permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                return permissions
            }
        }
    }

    interface AlertDialogListener {
        enum class DialogType {
            POSITIVE,
            NEGATIVE
        }
        fun onClick(dialogType: DialogType)
    }

    data class AlertDialogData(val title: String,
                               val message: String,
                               val negativeMessage: String?,
                               val positiveMessage: String?,
                               val cancellable: Boolean = false)

    companion object {

        @Synchronized fun getUniqueID (context: Context) : String {
            val prevUniqueID = "PREF_UNIQUE_ID"
            val sharedPrefs = context.getSharedPreferences(prevUniqueID, Context.MODE_PRIVATE)
            var uniqueID: String? = sharedPrefs.getString(prevUniqueID, null)
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString()
                sharedPrefs.edit().apply {
                    this.putString(prevUniqueID, uniqueID)
                    this.apply()
                }
            }
            return uniqueID
        }

        fun checkPermissions(permissions: List<String>, context: Context) : Boolean {
            for(permission in permissions) {
                val isPermission = ContextCompat.checkSelfPermission(
                    context,
                    permission
                )
                if(isPermission == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
            return true
        }

        fun showToast(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        fun showToastForLong(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }

        fun showDialog(context: Context,
                       dialogData: AlertDialogData,
                       listener: AlertDialogListener?) : androidx.appcompat.app.AlertDialog {
            val builder =
                androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogStyle)
            builder.setTitle(dialogData.title)
            builder.setMessage(dialogData.message)
            dialogData.negativeMessage?.let {
                builder.setNegativeButton(dialogData.negativeMessage) { _, _ ->
                    listener?.onClick(AlertDialogListener.DialogType.NEGATIVE)
                }
            }
            dialogData.positiveMessage?.let {
                builder.setPositiveButton(dialogData.positiveMessage) { _, _ ->
                    listener?.onClick(AlertDialogListener.DialogType.POSITIVE)
                }
            }
            builder.setCancelable(dialogData.cancellable)
            return builder.create()
        }

        fun getStringFromResource(context: Context, resId: Int) : String {
            return context.resources.getString(resId)
        }
    }
}

object Console {
    private const val TAG = "HYPER-LINK-APP"

    // 로그 출력 여부
    private const val IS_LOG = true

    // 로그 에러 출력 여부
    private const val IS_ERROR = true

    /**
     * 테스트를 위한 로그 메소드
     */
    fun s (key: String, value: String)  {
        val sb = StringBuffer().append("[").append(key).append(": ").append(value).append(" ]").toString()
        println(sb)
    }

    /**
     * 테스트를 위한 로그 메소드
     */
    fun s (value: String?) {
        println(value)
    }

    // Verbose 로그 출력
    fun v (msg: String) {
        if(IS_LOG) {
            Log.v(TAG, msg)
        }
    }

    fun d (msg: String) {
        if(IS_LOG) {
            Log.d(TAG, msg)
        }
    }
}