package dev.shahil.bharatbhraman

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import dev.shahil.bharatbhraman.databinding.ActivityMain3Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MainActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityMain3Binding

    private val apiKey = "AIzaSyA6R3h227DiyiIDAY2o_aJDUIpBskRDjLI"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvInfo.text = "Point camera at a monument and tap Scan"

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE)
        }

        binding.btnScan.setOnClickListener {
            captureAndAnalyze()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Log.e("ScannerApp", "Camera error", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureAndAnalyze() {
        val bitmap = binding.viewFinder.bitmap

        if (bitmap != null) {
            binding.tvInfo.text = "Processing..."
            binding.btnScan.isEnabled = false

            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    callGeminiAPI(bitmap)
                }

                binding.tvInfo.text = result
                binding.btnScan.isEnabled = true
            }
        } else {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val maxSize = 800
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val width: Int
        val height: Int

        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }

        return bitmap.scale(width, height)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun callGeminiAPI(bitmap: Bitmap): String {
        return try {
            val base64Image = bitmapToBase64(resizeBitmap(bitmap))

            val json = JSONObject()
            val partsArray = org.json.JSONArray()

            partsArray.put(JSONObject().put("text",
                "Identify the monument and explain its history. If not a monument, say 'Not a monument'."
            ))

            partsArray.put(JSONObject().put("inline_data",
                JSONObject().apply {
                    put("mime_type", "image/jpeg")
                    put("data", base64Image)
                }
            ))

            val content = JSONObject().put("parts", partsArray)
            val contentsArray = org.json.JSONArray().put(content)

            json.put("contents", contentsArray)

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val resString = response.body?.string() ?: return "No response"

            val jsonResponse = JSONObject(resString)

            if (jsonResponse.has("candidates")) {
                jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } else {
                "No result"
            }

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Please allow camera permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}