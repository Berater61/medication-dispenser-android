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

class Fenster_PersoenlicheDaten : AppCompatActivity() {

    private lateinit var btnSpeichern: Button
    private lateinit var btnBack: ImageView

    private lateinit var etName: EditText
    private lateinit var etBirth: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.persoenlichedaten)

        btnSpeichern = findViewById(R.id.btn_save)
        btnBack = findViewById(R.id.btnBack)

        etName = findViewById(R.id.etName)
        etBirth = findViewById(R.id.etBirth)
        etAddress = findViewById(R.id.etAddress)
        etPhone = findViewById(R.id.etPhone)

        btnBack.setOnClickListener {
            startActivity(Intent(this, Profil::class.java))
            finish()
        }

        btnSpeichern.setOnClickListener {
            speichern()
        }
    }

    private fun speichern() {
        // Fehler zurücksetzen
        etName.error = null
        etBirth.error = null
        etAddress.error = null
        etPhone.error = null

        val fullName = etName.text.toString().trim()
        val birth = etBirth.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // 1) Validierung über Validate.kt
        if (!Validate.isValidName(fullName)) {
            etName.error = "Bitte Vor- und Nachnamen eingeben (mind. 2 Wörter)."
            etName.requestFocus()
            return
        }

        if (!Validate.isValidGermanDate(birth)) {
            etBirth.error = "Bitte Datum im Format TT.MM.JJJJ eingeben (z.B. 15.03.1948)."
            etBirth.requestFocus()
            return
        }

        if (!Validate.isValidAddress(address)) {
            etAddress.error = "Bitte eine gültige Adresse eingeben."
            etAddress.requestFocus()
            return
        }

        if (!Validate.isValidPhone(phone)) {
            etPhone.error = "Bitte eine gültige Telefonnummer eingeben (z.B. +49..., 0151...)."
            etPhone.requestFocus()
            return
        }

        // Name splitten über Validate.kt
        val (vorname, nachname) = Validate.splitVornameNachname(fullName)

        // 2) UID holen
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        // 3) In Firebase überschreiben (nur diese Felder)
        val updates: Map<String, Any> = mapOf(
            "vorname" to vorname,
            "nachname" to nachname,
            "geburtsdatum" to birth,
            "adresse" to address,
            "telefonnummer" to phone
        )

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)

        btnSpeichern.isEnabled = false

        ref.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Gespeichert!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Profil::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                btnSpeichern.isEnabled = true
                Toast.makeText(this, "Fehler beim Speichern: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
