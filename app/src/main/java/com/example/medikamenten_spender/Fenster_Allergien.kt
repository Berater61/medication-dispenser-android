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


class Fenster_Allergien : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageView

    private lateinit var etAllergy1: EditText
    private lateinit var etAllergy2: EditText
    private lateinit var etAllergy3: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.allergien)

        btnSave = findViewById(R.id.btn_save_al)
        btnBack = findViewById(R.id.btnBack_al)

        etAllergy1 = findViewById(R.id.etAllergy1)
        etAllergy2 = findViewById(R.id.etAllergy2)
        etAllergy3 = findViewById(R.id.etAllergy3)

        btnBack.setOnClickListener {
            startActivity(Intent(this, Profil::class.java))
            finish()
        }

        btnSave.setOnClickListener {
            speichernAllergien()
        }
    }

    private fun speichernAllergien() {
        // Fehler zurücksetzen
        etAllergy1.error = null
        etAllergy2.error = null
        etAllergy3.error = null

        val a1 = etAllergy1.text.toString().trim()
        val a2 = etAllergy2.text.toString().trim()
        val a3 = etAllergy3.text.toString().trim()

        // Liste bauen: leere raus
        val listRaw = listOf(a1, a2, a3).filter { it.isNotBlank() }

        // Duplikate prüfen (case-insensitive)
        val lower = listRaw.map { it.lowercase() }
        if (lower.toSet().size != lower.size) {
            Toast.makeText(this, "Bitte keine doppelten Allergien eingeben.", Toast.LENGTH_SHORT).show()
            return
        }

        // Einzelne Einträge validieren
        for (a in listRaw) {
            if (!isValidAllergyText(a)) {
                when (a) {
                    a1 -> etAllergy1.error = "Ungültig (zu kurz / falsche Zeichen)"
                    a2 -> etAllergy2.error = "Ungültig (zu kurz / falsche Zeichen)"
                    a3 -> etAllergy3.error = "Ungültig (zu kurz / falsche Zeichen)"
                }
                Toast.makeText(this, "Bitte gültige Allergien eingeben.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        btnSave.isEnabled = false

        // Firebase überschreiben: allergien = Liste
        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("allergien")

        ref.get()
            .addOnSuccessListener { snap ->

                val existing: List<String> =
                    snap.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                // zusammenführen + Duplikate (case-insensitive) entfernen
                val merged = (existing + listRaw)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }

                ref.setValue(merged)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Allergien hinzugefügt!", Toast.LENGTH_SHORT).show()
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

    private fun isValidAllergyText(text: String): Boolean {
        if (text.length < 2) return false
        // Erlaubt Buchstaben/Zahlen/Leerzeichen sowie . , - / ( ) äöüß
        val regex = Regex("^[A-Za-zÄÖÜäöüß0-9 .,/()\\-]{2,}$")
        return regex.matches(text)
    }
}
