package dev.shahil.bharatbhraman

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface MyMemoryApiService {
    @GET("get")
    suspend fun translateText(
        @Query("q") text: String,
        @Query("langpair") langPair: String
    ): Response<MyMemoryResponse>
}

class MainActivity2 : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var sourceLang: Spinner
    private lateinit var targetLang: Spinner
    private lateinit var recordBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton
    private lateinit var speakBtn: MaterialButton
    private lateinit var swapBtn: MaterialButton
    private lateinit var touristModeBtn: MaterialButton
    private lateinit var guideModeBtn: MaterialButton
    private lateinit var inputTextView: TextView
    private lateinit var outputTextView: TextView
    private lateinit var manualInputEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var toggleInputBtn: ImageButton

    private lateinit var tts: TextToSpeech
    private lateinit var apiService: MyMemoryApiService

    private var inputText: String = ""
    private var translatedText: String = ""
    private var isInputVisible = false

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val languages = arrayOf(
        "English", "Hindi", "Bengali", "Tamil", "Marathi",
        "Spanish", "French", "German", "Japanese", "Chinese", "Korean"
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startVoiceInput()
            else Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }

    private val speechLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val res = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!res.isNullOrEmpty()) {
                    inputText = res[0]
                    inputTextView.text = inputText
                    manualInputEditText.setText(inputText)
                    translateText(inputText)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        initViews()
        setupApi()
        setupSpinners()
        setupButtons()

        sourceLang.setSelection(0)
        targetLang.setSelection(1)
        setTouristModeUI()

        tts = TextToSpeech(this, this)
    }

    private fun initViews() {
        sourceLang = findViewById(R.id.sourceLang)
        targetLang = findViewById(R.id.targetLang)
        recordBtn = findViewById(R.id.recordBtn)
        translateBtn = findViewById(R.id.translateBtn)
        speakBtn = findViewById(R.id.speakBtn)
        swapBtn = findViewById(R.id.swapBtn)
        touristModeBtn = findViewById(R.id.touristModeBtn)
        guideModeBtn = findViewById(R.id.guideModeBtn)
        inputTextView = findViewById(R.id.inputText)
        outputTextView = findViewById(R.id.outputText)
        manualInputEditText = findViewById(R.id.manualInputEditText)
        progressBar = findViewById(R.id.progressBar)
        toggleInputBtn = findViewById(R.id.toggleInputBtn)
    }

    private fun setupApi() {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MyMemoryApiService::class.java)
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        sourceLang.adapter = adapter
        targetLang.adapter = adapter
    }

    private fun setupButtons() {

        touristModeBtn.setOnClickListener {
            sourceLang.setSelection(0)
            targetLang.setSelection(1)
            setTouristModeUI()
        }

        guideModeBtn.setOnClickListener {
            sourceLang.setSelection(1)
            targetLang.setSelection(0)
            setGuideModeUI()
        }

        swapBtn.setOnClickListener {
            val s = sourceLang.selectedItemPosition
            val t = targetLang.selectedItemPosition
            sourceLang.setSelection(t)
            targetLang.setSelection(s)
        }

        toggleInputBtn.setOnClickListener {
            isInputVisible = !isInputVisible
            manualInputEditText.visibility =
                if (isInputVisible) View.VISIBLE else View.GONE
        }

        recordBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startVoiceInput()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        translateBtn.setOnClickListener {
            val typed = manualInputEditText.text.toString().trim()

            if (typed.isNotEmpty()) {
                inputText = typed
                inputTextView.text = inputText
                translateText(inputText)
            } else if (inputText.isNotEmpty()) {
                translateText(inputText)
            } else {
                Toast.makeText(this, "Speak or type first", Toast.LENGTH_SHORT).show()
            }
        }

        speakBtn.setOnClickListener {
            if (translatedText.isNotEmpty()) {
                speak()
            } else {
                Toast.makeText(this, "Nothing to speak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Manual fallback translations
    private fun getManualTouristTranslation(text: String, source: String, target: String): String? {
        val t = text.lowercase(Locale.getDefault()).trim()

        return when {
            source == "en" && target == "hi" && t == "where are you from and what is your name" ->
                "आप कहाँ से हैं और आपका नाम क्या है?"

            source == "en" && target == "hi" && t == "where are you from" ->
                "आप कहाँ से हैं?"

            source == "en" && target == "hi" && t == "what is your name" ->
                "आपका नाम क्या है?"

            source == "en" && target == "hi" && t == "where is the hotel" ->
                "होटल कहाँ है?"

            source == "en" && target == "hi" && t == "how much does this cost" ->
                "यह कितने का है?"

            source == "en" && target == "hi" && t == "help me" ->
                "मेरी मदद कीजिए।"

            else -> null
        }
    }

    // ✅ Safety restriction
    private fun isBadTranslation(text: String): Boolean {
        val lower = text.lowercase(Locale.getDefault())
        val banned = listOf("madarchod", "bhenchod", "chutiya", "gandu")
        return banned.any { lower.contains(it) }
    }

    private fun translateText(text: String) {
        val sourceCode = getTranslateLanguageCode(sourceLang.selectedItem.toString())
        val targetCode = getTranslateLanguageCode(targetLang.selectedItem.toString())

        // Manual fallback first
        val manual = getManualTouristTranslation(text, sourceCode, targetCode)
        if (manual != null) {
            translatedText = manual
            outputTextView.text = translatedText
            speak()
            return
        }

        progressBar.visibility = View.VISIBLE

        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.translateText(text, "$sourceCode|$targetCode")
                }

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val translated = response.body()?.responseData?.translatedText?.trim()

                    if (!translated.isNullOrEmpty()) {
                        if (isBadTranslation(translated)) {
                            translatedText = ""
                            outputTextView.text = "Translation unavailable for this sentence."
                        } else {
                            translatedText = translated
                            outputTextView.text = translatedText
                            speak() // ✅ auto speak after translation
                        }
                    } else {
                        translatedText = ""
                        outputTextView.text = "Translation unavailable"
                    }
                } else {
                    Toast.makeText(this@MainActivity2, "Server Error", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity2, "Translation failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startVoiceInput() {
        val lang = getSpeechLanguageCode(sourceLang.selectedItem.toString())

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        speechLauncher.launch(intent)
    }

    private fun speak() {
        if (!::tts.isInitialized || translatedText.isBlank()) return

        val locale = getTtsLocale(targetLang.selectedItem.toString())
        val result = tts.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.language = Locale.US
        }

        tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "TRANSLATED_SPEECH")
    }

    private fun setTouristModeUI() {
        touristModeBtn.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
        guideModeBtn.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
    }

    private fun setGuideModeUI() {
        guideModeBtn.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
        touristModeBtn.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
    }

    private fun getTranslateLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "Hindi" -> "hi"
            "Bengali" -> "bn"
            "Tamil" -> "ta"
            "Marathi" -> "mr"
            "Spanish" -> "es"
            "French" -> "fr"
            "German" -> "de"
            "Japanese" -> "ja"
            "Chinese" -> "zh-CN"
            "Korean" -> "ko"
            else -> "en"
        }
    }

    private fun getSpeechLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en-US"
            "Hindi" -> "hi-IN"
            "Bengali" -> "bn-IN"
            "Tamil" -> "ta-IN"
            "Marathi" -> "mr-IN"
            "Spanish" -> "es-ES"
            "French" -> "fr-FR"
            "German" -> "de-DE"
            "Japanese" -> "ja-JP"
            "Chinese" -> "zh-CN"
            "Korean" -> "ko-KR"
            else -> "en-US"
        }
    }

    private fun getTtsLocale(language: String): Locale {
        return when (language) {
            "English" -> Locale("en", "US")
            "Hindi" -> Locale("hi", "IN")
            "Bengali" -> Locale("bn", "IN")
            "Tamil" -> Locale("ta", "IN")
            "Marathi" -> Locale("mr", "IN")
            "Spanish" -> Locale("es", "ES")
            "French" -> Locale("fr", "FR")
            "German" -> Locale("de", "DE")
            "Japanese" -> Locale("ja", "JP")
            "Chinese" -> Locale.SIMPLIFIED_CHINESE
            "Korean" -> Locale("ko", "KR")
            else -> Locale.US
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Toast.makeText(this, "Text to Speech init failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}