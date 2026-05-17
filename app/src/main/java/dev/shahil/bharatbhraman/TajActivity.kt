package dev.shahil.bharatbhraman

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TajActivity : AppCompatActivity() {

    private var is360ViewVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taj)

        val img1 = findViewById<ImageView>(R.id.img1)
        val img2 = findViewById<ImageView>(R.id.img2)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        Glide.with(this)
            .load("https://upload.wikimedia.org/wikipedia/commons/d/da/Taj-Mahal.jpg")
            .into(img1)

        Glide.with(this)
            .load("https://images.pexels.com/photos/28762052/pexels-photo-28762052.jpeg")
            .into(img2)

        val view360 = layoutInflater.inflate(R.layout.activity_360_view, null)
        val webView = view360.findViewById<WebView>(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        val url = "https://www.google.com/maps/embed?pb=!4v1696437728009!6m8!1m7!1sCAoSLEFGMVFpcE5rZk1ka2VrV3p3YVhMVXBxQVl1Tjd3V2pYR0ZtWEpubzMza3hG!2m2!1d27.175015!2d78.042155!3f0!4f0!5f0.7820865974627469"

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