package org.rockbox

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import org.rockbox.service.RockboxPlaybackService

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var vibrator: Vibrator? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Plein écran immersif edge-to-edge (expérience iPod tactile intégrale)
        setupImmersiveFullscreen()

        // 2. Initialiser le vibrateur matériel pour le retour haptique de la molette ClickWheel
        initVibrator()

        // 3. Charger le layout XML officiel (res/layout/activity_main.xml)
        setContentView(R.layout.activity_main)

        // 4. Configurer la WebView haute fidélité avec accélération matérielle
        webView = findViewById<WebView>(R.id.rockboxWebView).apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        setupWebSettings()
        setupAssetLoaderAndNavigation()
        setupNativeHapticBridge()
        setupBackNavigation()

        // 5. Démarrer le service audio d'arrière-plan pour lecture continue écran éteint
        startAudioService()
    }

    private fun setupImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebSettings() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false // Permet la lecture audio fluide sans blocage navigateur
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun setupAssetLoaderAndNavigation() {
        // Le WebViewAssetLoader officiel de Google charge les assets locaux de manière sécurisée (CORS-friendly, aucun bug file://)
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                // Fallback de sécurité vers le protocole direct file:///android_asset
                if (request.isForMainFrame) {
                    webView.loadUrl("file:///android_asset/www/index.html")
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return true // Empêche les logs superflus
            }
        }

        // Chargement immédiat de l'application Rockbox compilée
        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")
    }

    private fun setupNativeHapticBridge() {
        // Pont JavaScript <-> Android permettant à ClickWheel de déclencher la vraie vibration physique
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun vibrate(durationMs: Long) {
                runOnUiThread {
                    try {
                        if (vibrator?.hasVibrator() == true) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val effect = if (durationMs <= 10) {
                                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                                } else {
                                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                                }
                                vibrator?.vibrate(effect)
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(durationMs)
                            }
                        }
                    } catch (_: Throwable) {}
                }
            }

            @JavascriptInterface
            fun getAndroidSdk(): Int = Build.VERSION.SDK_INT
        }, "AndroidBridge")
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Tenter de revenir en arrière dans les menus Rockbox ou minimiser l'app sans crash
                webView.evaluateJavascript("if (window.__rockboxNavigateBack) { window.__rockboxNavigateBack(); true; } else { false; }") { handled ->
                    if (handled != "true") {
                        if (webView.canGoBack()) {
                            webView.goBack()
                        } else {
                            moveTaskToBack(true)
                        }
                    }
                }
            }
        })
    }

    private fun startAudioService() {
        try {
            val serviceIntent = Intent(this, RockboxPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (_: Throwable) {}
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveFullscreen()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}