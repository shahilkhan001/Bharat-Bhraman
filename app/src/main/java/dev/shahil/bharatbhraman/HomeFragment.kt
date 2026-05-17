package dev.shahil.bharatbhraman

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val videoView = view.findViewById<VideoView>(R.id.introVideo)

        val uri = Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.intro_video)
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f) // 🔇 Mute audio
        }

        videoView.start()

        val scanBtn = view.findViewById<MaterialButton>(R.id.scanBtn)
        scanBtn.setOnClickListener {
            startActivity(Intent(requireContext(), _root_ide_package_.dev.shahil.bharatbhraman.MainActivity3::class.java))
        }

        val voiceBtn = view.findViewById<MaterialButton>(R.id.voiceBtn)
        voiceBtn.setOnClickListener {
            startActivity(Intent(requireContext(), _root_ide_package_.dev.shahil.bharatbhraman.MainActivity2::class.java))
        }

        return view
    }
}