package com.mektupeviapp.mektupevi

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.mektupeviapp.mektupevi.ui.theme.MektupEviTheme
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val ONESIGNAL_APP_ID = BuildConfig.os_app_id;

class MainActivity : ComponentActivity() {
    private lateinit var myWebView: WebView
    private lateinit var loadingDialog: ALoadingDialog
    private val file_type = "*/*"
    private var cam_file_data: String? = null
    private var file_path: ValueCallback<Array<Uri>>? = null
    private val file_req_code = 1
    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }

        loadingDialog = ALoadingDialog(this)

        myWebView = findViewById(R.id.webview)
        myWebView.webViewClient = MyWebClient(loadingDialog)
        myWebView.loadUrl("https://www.mektupevi.com/")
        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        myWebView.settings.loadWithOverviewMode = true
        myWebView.settings.useWideViewPort = true
        myWebView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        myWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Check and request necessary permissions
        if (!hasRequiredPermissions()) {
            requestRequiredPermissions()
        }

        // Set the WebChromeClient for file upload
        myWebView.webChromeClient = webChromeClient

        // Set the WebViewClient for URL loading
        myWebView.webViewClient = MyWebClient(loadingDialog)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            if (requestCode == file_req_code) {
                if (file_path == null) return
                file_path?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
                file_path = null
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestRequiredPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack()
            myWebView.isVisible = false
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    // This method will be executed once the timer is over
                    myWebView.isVisible = true

                },
                500 // value in milliseconds
            )
        } else {
            super.onBackPressed()
        }
    }



    class MyWebClient(private val loadingDialog: ALoadingDialog) : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            //view?.evaluateJavascript(disableCssSelectorCode, null)
            loadingDialog.show()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return if (url == null || url.startsWith("http://") || url.startsWith("https://")) {
                false
            } else try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view!!.context.startActivity(intent)
                true
            } catch (e: Exception) {
                Log.i(TAG, "shouldOverrideUrlLoading Exception:$e")
                true
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            // Sayfa yüklendiğinde JavaScript kodunu çalıştır
            loadingDialog.dismiss()
        }
    }

    private val webChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (file_path != null) {
                file_path?.onReceiveValue(null)
                file_path = null
            }

            file_path = filePathCallback

            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = file_type

            // Galeri seçeneği ekleyin
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            val intentArray: Array<Intent?> = arrayOf(galleryIntent)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Dosya Seç")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooserIntent, file_req_code)
            return true
        }
    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        cam_file_data = "file:" + image.absolutePath
        return image
    }
}

@Composable
fun WebViewComponent() {
    // WebView ile ilgili Composable bileşeninizi burada oluşturun
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MektupEviTheme {
        WebViewComponent()
    }
}
