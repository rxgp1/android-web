package kr.hyperlink.app.sample.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.provider.MediaStore
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessaging
import kr.hyperlink.app.sample.R
import kr.hyperlink.app.sample.utils.Console
import kr.hyperlink.app.sample.utils.WebViewUtil
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
class WebActivity : AppCompatActivity() {

    private lateinit var mWebView: WebView
    private lateinit var loading: View
    private var pushID: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var finalURL: String = ""
    private var doubleBackToExitPressedOnce = false
    private val url = WebViewUtil.Config.BASE_URL  // DEV
    // private val url = "https://office-new.shinsegaefood.com/view/APPMOB0001"  // PROD
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var imageUri: Uri = Uri.EMPTY


    // 로그인 이후 처리
    private val blockedUrlList = arrayOf("")

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mWebView.canGoBack()) {
                val isGoBack = blockedUrlList.any { item -> finalURL.contains(item) }
                if(!isGoBack) {
                    mWebView.goBack()
                } else {
                    if (doubleBackToExitPressedOnce) {
                        finish()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    WebViewUtil.showToast(this@WebActivity, "뒤로가기를 한번더 클릭하면 종료됩니다.")
                    Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                }
            } else {
                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }
                doubleBackToExitPressedOnce = true
                WebViewUtil.showToast(this@WebActivity, "뒤로가기를 한번더 클릭하면 종료됩니다.")
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (uploadMessage != null) {
                val dataString = result.data?.dataString
                var results: Array<Uri> = arrayOf(imageUri)
                if(dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
                uploadMessage?.let {
                    it.onReceiveValue(results)
                    uploadMessage = null
                }
            }
        } else {
            uploadMessage?.let {
                val results: Array<Uri> = arrayOf()
                it.onReceiveValue(results)
                uploadMessage = null
            }
            WebViewUtil.showToastForLong(this@WebActivity, "파일 업로드가 취소 되었습니다. 권한을 체크하여 주시기 바랍니다.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        mWebView = findViewById(R.id.web_area)
        loading = findViewById(R.id.layout_loading)
        val layoutLoading = loading.findViewById<ImageView>(R.id.loading)
        Glide.with(this).asGif().load(R.drawable.loading).into(layoutLoading)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                pushID = task.result
                Console.d(pushID.toString())
            }
        }

        mWebView = setWebView(mWebView)
        mWebView.webChromeClient = innerChromeClient
        mWebView.webViewClient = innerWebViewClient

        // 구현부
        val link = intent.extras?.getString("link")
        if(link != null) {
            loadURL(mWebView, link.toString())
        } else {
            loadURL(mWebView, url)
        }
        // 백키 정책
        this.onBackPressedDispatcher.addCallback(this@WebActivity, callback)
    }

    private fun setWebView(mWebView: WebView) : WebView {
        mWebView.settings.javaScriptEnabled = true
        mWebView.settings.javaScriptCanOpenWindowsAutomatically = true
        mWebView.settings.setSupportMultipleWindows(true)
        mWebView.settings.loadWithOverviewMode = true
        mWebView.settings.useWideViewPort = true
        mWebView.settings.allowFileAccess = true
        mWebView.settings.domStorageEnabled = true
        mWebView.settings.allowContentAccess = true
        mWebView.settings.userAgentString = WebViewUtil.Config.USER_AGENT
        mWebView.addJavascriptInterface(this@WebActivity, WebViewUtil.Config.JAVASCRIPT_INTERFACE_NAME)
        return mWebView
    }

    private fun getVersionInfo(): String {
        val info: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
        return info.versionName
    }

    private fun getDeviceModelName(): String {
        return Build.MODEL
    }

    private fun loadURL(view: WebView, url: String) {
        view.loadUrl(url)
    }

    @JavascriptInterface
    fun requestRegisterDevice() {
        handler.post {
            val js = StringBuffer()
                .append("javascript:registerDevice('")
                .append(WebViewUtil.getUniqueID(this))
                .append("', '")
                .append(this.pushID)
                .append("', '")
                .append(getVersionInfo())
                .append("', '")
                .append("ANDROID")
                .append("', '")
                .append(getDeviceModelName())
                .append("');").toString()
            this.loadURL(mWebView, js)
        }
    }

    /**
     * 화면 재구성 시 ui 적용
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }


    private var innerWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            request.let {
                it?.url.let { uri->
                    finalURL = uri.toString()
                    if (finalURL.startsWith("tel:")) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(finalURL))
                        startActivity(intent)
                        return true
                    } else if (finalURL.startsWith("mailto:")) {
                        val i = Intent(Intent.ACTION_SENDTO, Uri.parse(finalURL))
                        startActivity(i)
                        return true
                    } else if (finalURL.startsWith("sms:")) {
                        val i = Intent(Intent.ACTION_SENDTO, Uri.parse(finalURL))
                        startActivity(i)
                        return true
                    } else if(finalURL.contains("map.kakao.com")) {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(finalURL))
                        intent.setPackage("com.android.chrome")
                        startActivity(i)
                        return true
                    } else if(finalURL.contains("address")) {
                        return super.shouldOverrideUrlLoading(view, request)
                    } else {
                        loadURL(mWebView, uri.toString())
                    }
                }
            }
            return true
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            finalURL = url
            loadURL(mWebView, url)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loading.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loading.visibility = View.GONE
            url?.let {
                finalURL = it
            }
        }
    }

    private val innerChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }

        // 파일 처리
        override fun onShowFileChooser(webView:WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams:FileChooserParams):Boolean {
            val pkgName = packageName
            val imageStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Camera")
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs()
            }
            val file = File(imageStorageDir.toString() + File.separator + "IMG_" + System.currentTimeMillis().toString() + ".jpg")
            // imageUri = Uri.fromFile(file) // save to the private variable
            imageUri = FileProvider.getUriForFile(this@WebActivity, "${pkgName}.fileprovider", file)


            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

            val i = Intent(Intent.ACTION_PICK).apply {
                this.type = "image/*"
                this.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val chooserIntent = Intent.createChooser(i, "Image Chooser")
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                arrayOf<Parcelable>(captureIntent)
            )
            uploadMessage?.let {
                it.onReceiveValue(null)
                uploadMessage = null
            }
            uploadMessage = filePathCallback
            if(fileChooserParams.isCaptureEnabled) {
                resultLauncher.launch(captureIntent)
            } else {
                resultLauncher.launch(chooserIntent)
            }
            return true
        }


        override fun onConsoleMessage(message: ConsoleMessage): Boolean {
            Console.d("${message.message()} -- From line " +
                    "${message.lineNumber()} of ${message.sourceId()}")
            return true
        }

        override fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
            val dialogData = WebViewUtil.AlertDialogData (
                title = "알림",
                message = message?:"",
                negativeMessage = null,
                positiveMessage = WebViewUtil.getStringFromResource(this@WebActivity, android.R.string.ok),
                cancellable = false
            )
            WebViewUtil.showDialog(this@WebActivity, dialogData, object: WebViewUtil.AlertDialogListener {
                override fun onClick(dialogType: WebViewUtil.AlertDialogListener.DialogType) {
                    if(dialogType == WebViewUtil.AlertDialogListener.DialogType.POSITIVE) {
                        result.confirm()
                    }
                }
            }).show()
            return true
        }

        override fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
            val dialogData = WebViewUtil.AlertDialogData (
                title = "확인",
                message = message?:"",
                negativeMessage = "Yes",
                positiveMessage = "No",
                cancellable = false
            )
            WebViewUtil.showDialog(this@WebActivity, dialogData, object: WebViewUtil.AlertDialogListener {
                override fun onClick(dialogType: WebViewUtil.AlertDialogListener.DialogType) {
                    if(dialogType == WebViewUtil.AlertDialogListener.DialogType.POSITIVE) {
                        result.confirm()
                    } else {
                        result.cancel()
                    }
                }
            }).show()
            return true
        }
    }
}