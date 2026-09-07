package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Home : ComponentActivity() {

    private lateinit var startButton: Button
    private lateinit var inhaltText: TextView
    private lateinit var urlaub: LinearLayout
    private lateinit var bottomnav: BottomNavigationView
    private lateinit var btnScanPlan: Button

    private lateinit var tvDate: TextView
    private lateinit var tvNextTime: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // UI-Elemente verbinden
        btnScanPlan = findViewById(R.id.btnScanPlan)
        inhaltText = findViewById(R.id.Text_Home)
        startButton = findViewById(R.id.Start)
        urlaub = findViewById(R.id.linear_layout_urlaub)
        bottomnav = findViewById(R.id.bottomNav)

        tvDate = findViewById(R.id.date)
        tvNextTime = findViewById(R.id.tvNextTime)

        bottomnav.selectedItemId = R.id.nav_home

        // Datum und nächste Einnahme berechnen
        updateCurrentDate()
        updateNextMedicationTime()

        // Firebase Auth laden
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        btnScanPlan.setOnClickListener {
            startActivity(Intent(this, Scanner::class.java))
        }

        if (uid != null) {

            val database = FirebaseDatabase.getInstance()

            val ref = database.getReference("patienten").child(uid)

            // Patientendaten auslesen
            ref.get().addOnSuccessListener { snapshot ->
                val vorname = snapshot.child("vorname").value?.toString() ?: ""
                val nachname = snapshot.child("nachname").value?.toString() ?: ""

                inhaltText.text = "Willkommen, $vorname $nachname"
            }.addOnFailureListener {
                inhaltText.text = "Fehler beim Laden der Daten"
            }
        } else {
            inhaltText.text = "Kein Benutzer eingeloggt"
        }

        urlaub.setOnClickListener {
            val intent = Intent(this, Fenster_Urlaubsmodus::class.java)
            startActivity(intent)
        }

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

                R.id.nav_calendar -> {
                    startActivity(Intent(this, Kalender::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN)
        val currentDate = dateFormat.format(Date())
        tvDate.text = "Heute, $currentDate"
    }

    private fun updateNextMedicationTime() {

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val nextTime = when {
            currentHour < 7 -> "07:00"
            currentHour < 15 -> "15:00"
            currentHour < 23 -> "23:00"
            else -> "07:00"
        }

        tvNextTime.text = nextTime
    }
}
