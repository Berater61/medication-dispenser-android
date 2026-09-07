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

class Fenster_Arzt : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageView

    private lateinit var etDoctorName: EditText
    private lateinit var etDoctorField: EditText
    private lateinit var etDoctorPhone: EditText
    private lateinit var etDoctorMail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arzt)

        btnSave = findViewById(R.id.btn_saveA)
        btnBack = findViewById(R.id.btnBackA)

        etDoctorName = findViewById(R.id.etDoctorName)
        etDoctorField = findViewById(R.id.etDoctorField)
        etDoctorPhone = findViewById(R.id.etDoctorPhone)
        etDoctorMail = findViewById(R.id.etDoctorMail)

        btnBack.setOnClickListener {
            startActivity(Intent(this, Kontaktperson::class.java))
            finish()
        }

        btnSave.setOnClickListener {
            speichernArztOderPflege()
        }
    }

    private fun speichernArztOderPflege() {
        // Fehler reset
        etDoctorName.error = null
        etDoctorField.error = null
        etDoctorPhone.error = null
        etDoctorMail.error = null

        val name = etDoctorName.text.toString().trim()
        val field = etDoctorField.text.toString().trim() // rolle
        val phone = etDoctorPhone.text.toString().trim()
        val mail = etDoctorMail.text.toString().trim()

        // Validierung: Name + Fachrichtung Pflicht
        if (!isValidText(name, 2)) {
            etDoctorName.error = "Bitte einen gültigen Namen eingeben."
            etDoctorName.requestFocus()
            return
        }
        if (!isValidText(field, 2)) {
            etDoctorField.error = "Bitte eine gültige Fachrichtung/Rolle eingeben."
            etDoctorField.requestFocus()
            return
        }

        // optional: phone/mail nur prüfen wenn nicht leer
        if (phone.isNotBlank() && !isValidPhone(phone)) {
            etDoctorPhone.error = "Ungültige Telefonnummer."
            etDoctorPhone.requestFocus()
            return
        }
        if (mail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            etDoctorMail.error = "Ungültige E-Mail."
            etDoctorMail.requestFocus()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        btnSave.isEnabled = false

        val baseRef = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)

        // Slots 1..3 belegen: medizinKontakt1..3
        baseRef.get()
            .addOnSuccessListener { snapshot ->

                fun isSlotEmpty(slot: MedizinKontakt?): Boolean {
                    if (slot == null) return true
                    return slot.name.isBlank() &&
                            slot.rolle.isBlank() &&
                            slot.telefon.isBlank() &&
                            slot.email.isBlank()
                }

                val mk1 = snapshot.child("medizinKontakt1").getValue(MedizinKontakt::class.java)
                val mk2 = snapshot.child("medizinKontakt2").getValue(MedizinKontakt::class.java)
                val mk3 = snapshot.child("medizinKontakt3").getValue(MedizinKontakt::class.java)

                // Duplikat verhindern (gleicher Name, case-insensitive)
                val exists = listOf(mk1, mk2, mk3).any { it?.name?.equals(name, ignoreCase = true) == true }
                if (exists) {
                    btnSave.isEnabled = true
                    Toast.makeText(this, "Dieser Kontakt ist schon vorhanden.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val newMk = MedizinKontakt(
                    name = name,
                    rolle = field,
                    telefon = phone,
                    email = mail
                )

                // Freien Slot finden
                val targetKey = when {
                    isSlotEmpty(mk1) -> "medizinKontakt1"
                    isSlotEmpty(mk2) -> "medizinKontakt2"
                    isSlotEmpty(mk3) -> "medizinKontakt3"
                    else -> null
                }

                if (targetKey == null) {
                    btnSave.isEnabled = true
                    Toast.makeText(this, "Maximal 3 Ärzte/Pflegekontakte erlaubt.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                baseRef.child(targetKey).setValue(newMk)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Gespeichert!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Kontaktperson::class.java))
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

    private fun isValidText(text: String, minLen: Int): Boolean {
        if (text.length < minLen) return false
        // Buchstaben/Zahlen/Leerzeichen + . , - / ( ) äöüß
        val regex = Regex("^[A-Za-zÄÖÜäöüß0-9 .,/()\\-]{${minLen},}$")
        return regex.matches(text)
    }

    private fun isValidPhone(text: String): Boolean {
        // simple: +, Zahlen, Leerzeichen, /, -, ()
        val regex = Regex("^[0-9+()\\- /]{5,}$")
        return regex.matches(text)
    }
}
