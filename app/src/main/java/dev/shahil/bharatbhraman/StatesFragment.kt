package dev.shahil.bharatbhraman

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class StatesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_states, container, false)
        val searchEditText = view.findViewById<TextInputEditText>(R.id.searchEditText)
        val cardUp = view.findViewById<CardView>(R.id.cardUp)
        val cardBihar = view.findViewById<CardView>(R.id.cardBihar)
        val cardRajasthan = view.findViewById<CardView>(R.id.cardRajasthan)
        val cardGujarat = view.findViewById<CardView>(R.id.cardGujarat)
        val cardUttarakhand = view.findViewById<CardView>(R.id.cardUttarakhand)
        val cardMaharashtra = view.findViewById<CardView>(R.id.cardMaharashtra)
        // Search Feature Enabled
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                cardUp.visibility = if ("uttar pradesh".contains(query)) View.VISIBLE else View.GONE
                cardBihar.visibility = if ("bihar".contains(query)) View.VISIBLE else View.GONE
                cardRajasthan.visibility = if ("rajasthan".contains(query)) View.VISIBLE else View.GONE
                cardGujarat.visibility = if ("gujarat".contains(query)) View.VISIBLE else View.GONE
                cardUttarakhand.visibility = if ("uttarakhand".contains(query)) View.VISIBLE else View.GONE
                cardMaharashtra.visibility = if ("maharashtra".contains(query)) View.VISIBLE else View.GONE
            }
        })
        cardUp.setOnClickListener {
            startActivity(Intent(requireContext(), Up::class.java))
        }
        cardBihar.setOnClickListener {
            startActivity(Intent(requireContext(), Bihar::class.java))
        }
        cardRajasthan.setOnClickListener {
            startActivity(Intent(requireContext(), Rajasthan::class.java))
        }
        cardGujarat.setOnClickListener {
            startActivity(Intent(requireContext(), Gujarat::class.java))
        }
        cardUttarakhand.setOnClickListener {
            startActivity(Intent(requireContext(), Uttarakhand::class.java))
        }
        cardMaharashtra.setOnClickListener {
            startActivity(Intent(requireContext(), Maharashtra::class.java))
        }
        return view
    }
}