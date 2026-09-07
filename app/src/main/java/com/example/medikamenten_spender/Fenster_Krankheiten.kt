package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class Fenster_Krankheiten : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageView

    private lateinit var etDisease1: EditText
    private lateinit var etDisease2: EditText
    private lateinit var etDisease3: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.krankheiten)

        btnSave = findViewById(R.id.btn_sav)
        btnBack = findViewById(R.id.btnBack)

        etDisease1 = findViewById(R.id.etDisease1)
        etDisease2 = findViewById(R.id.etDisease2)
        etDisease3 = findViewById(R.id.etDisease3)

        btnBack.setOnClickListener {
            startActivity(Intent(this, Profil::class.java))
            finish()
        }

        btnSave.setOnClickListener {
            speichernKrankheiten()
        }
    }

    private fun speichernKrankheiten() {
        // Fehler zurücksetzen
        etDisease1.error = null
        etDisease2.error = null
        etDisease3.error = null

        // Eingaben lesen
        val d1 = etDisease1.text.toString().trim()
        val d2 = etDisease2.text.toString().trim()
        val d3 = etDisease3.text.toString().trim()

        // Liste bauen: leere rauswerfen
        val listRaw = listOf(d1, d2, d3).filter { it.isNotBlank() }

        // Validierung: max 3 (ist sowieso), keine Duplikate, Zeichen ok
        if (listRaw.size > 3) {
            Toast.makeText(this, "Maximal 3 Krankheiten.", Toast.LENGTH_SHORT).show()
            return
        }

        // Duplikate prüfen (case-insensitive)
        val lower = listRaw.map { it.lowercase() }
        if (lower.toSet().size != lower.size) {
            Toast.makeText(this, "Bitte keine doppelten Krankheiten eingeben.", Toast.LENGTH_SHORT).show()
            return
        }

        // Einzelne Einträge validieren
        for (d in listRaw) {
            if (!isValidDiseaseText(d)) {
                // Setze Fehler auf das passende Feld
                when (d) {
                    d1 -> etDisease1.error = "Ungültig (zu kurz / falsche Zeichen)"
                    d2 -> etDisease2.error = "Ungültig (zu kurz / falsche Zeichen)"
                    d3 -> etDisease3.error = "Ungültig (zu kurz / falsche Zeichen)"
                }
                Toast.makeText(this, "Bitte gültige Krankheiten eingeben.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // UID holen
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        btnSave.isEnabled = false

        // Firebase überschreiben: krankheiten = Liste
        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("krankheiten")

        ref.get()
            .addOnSuccessListener { snap ->

                val existing: List<String> =
                    snap.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                val merged = (existing + listRaw)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() } // keine Duplikate

                ref.setValue(merged)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Krankheiten hinzugefügt!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Profil::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        Toast.makeText(this, "Fehler beim Speichern: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }

    }

    private fun isValidDiseaseText(text: String): Boolean {
        // Mindestlänge
        if (text.length < 2) return false
        // Erlaubt Buchstaben/Zahlen/Leerzeichen sowie . , - / ( ) äöüß
        val regex = Regex("^[A-Za-zÄÖÜäöüß0-9 .,/()\\-]{2,}$")
        return regex.matches(text)
    }
}
