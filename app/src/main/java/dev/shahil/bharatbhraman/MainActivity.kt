package dev.shahil.bharatbhraman

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )

        val navHome = findViewById<View>(R.id.navHome)
        val navMiddle = findViewById<View>(R.id.navMiddle)
        val navProfile = findViewById<View>(R.id.navProfile)

        if (savedInstanceState == null) replaceFragment(HomeFragment())

        navHome.setOnClickListener { replaceFragment(HomeFragment()) }
        navMiddle.setOnClickListener { replaceFragment(StatesFragment()) }
        navProfile.setOnClickListener { replaceFragment(ProfileFragment()) }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}