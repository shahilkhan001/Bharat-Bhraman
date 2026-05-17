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

class RamActivity : AppCompatActivity() {

    private var is360ViewVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ram)

        val img1 = findViewById<ImageView>(R.id.img1)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        Glide.with(this)
            .load("https://images.pexels.com/photos/18362045/pexels-photo-18362045.jpeg")
            .into(img1)

        val view360 = layoutInflater.inflate(R.layout.activity_360_view, null)
        val webView = view360.findViewById<WebView>(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        // ✅ Use REAL 360 IMAGE (not Google link)
        val panoramaImage = "https://upload.wikimedia.org/wikipedia/commons/5/5f/Ram_Mandir_360_View_Nepal.jpg"

        val html = """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://cdn.jsdelivr.net/npm/pannellum@2.5.6/build/pannellum.js"></script>
                <link href="https://cdn.jsdelivr.net/npm/pannellum@2.5.6/build/pannellum.css" rel="stylesheet"/>
                <style>
                    body { margin: 0; }
                    #panorama { width: 100%; height: 100%; }
                </style>
            </head>
            <body>
            <div id="panorama"></div>

            <script>
            pannellum.viewer('panorama', {
                "type": "equirectangular",
                "panorama": "$panoramaImage",
                "autoLoad": true,
                "hfov": 110
            });
            </script>

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
                (view360.parent as ViewGroup).removeView(view360)
                is360ViewVisible = false
            }
        }
    }
}