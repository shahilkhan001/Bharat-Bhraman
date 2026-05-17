package dev.shahil.bharatbhraman

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class KashiActivity : AppCompatActivity() {

    private var is360ViewVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kashi)

        val fab = findViewById<FloatingActionButton>(R.id.fab)

        // 360 View Setup
        val view360 = layoutInflater.inflate(R.layout.activity_360_view, null)
        val webView = view360.findViewById<WebView>(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        val url = "https://www.google.com/maps/embed?pb=!4v1696438000000!6m8!1m7!1sCAoSLEFGMVFpcE5ZbWZ4aXJrRk5rQ0p4d2xQeXJXc0Z4d2RrR1l5bE9qQmR6eE1F!2m2!1d25.3109!2d83.0107!3f0!4f0!5f0.7820865974627469"

        val html = """
            <html>
            <body style="margin:0; padding:0;">
                <iframe 
                    src="$url"
                    width="100%" 
                    height="100%" 
                    style="border:0;" 
                    allowfullscreen
                    loading="lazy">
                </iframe>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)

        // FAB Toggle for 360 View
        fab.setOnClickListener {
            if (!is360ViewVisible) {
                addContentView(
                    view360,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                is360ViewVisible = true
            } else {
                if (view360.parent != null) {
                    (view360.parent as ViewGroup).removeView(view360)
                }
                is360ViewVisible = false
            }
        }
    }
}