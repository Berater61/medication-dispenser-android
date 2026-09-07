package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Kalender : AppCompatActivity() {
    private lateinit var bottomnav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kalender)
        bottomnav=findViewById(R.id.bottomNav)
        bottomnav.selectedItemId = R.id.nav_calendar

        bottomnav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, Profil::class.java))
                    finish()
                    true
                }

                R.id.nav_contacts -> {
                    startActivity(Intent(this, Kontaktperson::class.java))
                    finish()
                    true
                }

                R.id.nav_calendar-> {
                    true
                }

                else -> false
            }
        }

    }
}
