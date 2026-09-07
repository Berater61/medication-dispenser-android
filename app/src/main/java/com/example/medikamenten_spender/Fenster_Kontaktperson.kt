package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Fenster_Kontaktperson : AppCompatActivity() {

    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageView

    private lateinit var etName: EditText
    private lateinit var etRelation: EditText
    private lateinit var etPhone: EditText
    private lateinit var etMail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fenster_kontakt)

        btnSave = findViewById(R.id.btn_save)
        btnBack = findViewById(R.id.btnBackK)

        etName = findViewById(R.id.etContactName)
        etRelation = findViewById(R.id.etContactRelation)
        etPhone = findViewById(R.id.etContactPhone)
        etMail = findViewById(R.id.etContactMail)

        btnBack.setOnClickListener {
            startActivity(Intent(this, Kontaktperson::class.java))
            finish()
        }

        btnSave.setOnClickListener {
            saveKontaktpersonMax2()
        }
    }

    private fun saveKontaktpersonMax2() {
        // Fehler reset
        etName.error = null
        etRelation.error = null
        etPhone.error = null
        etMail.error = null

        val name = etName.text.toString().trim()
        val relation = etRelation.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val mail = etMail.text.toString().trim()

        // Validierung (minimal & robust)
        if (name.length < 2) {
            etName.error = "Bitte Namen eingeben"
            etName.requestFocus()
            return
        }
        if (relation.length < 2) {
            etRelation.error = "Bitte Beziehung eingeben"
            etRelation.requestFocus()
            return
        }
        if (phone.length < 6) {
            etPhone.error = "Bitte gültige Nummer eingeben"
            etPhone.requestFocus()
            return
        }
        if (mail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            etMail.error = "Bitte gültige E-Mail eingeben"
            etMail.requestFocus()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        btnSave.isEnabled = false

        val newKp = Kontaktperson_c(
            name = name,
            beziehung = relation,
            telefon = phone,
            email = mail
        )

        val patientRef = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)

        // Wir prüfen Slot1 & Slot2. Wenn beide belegt -> Fehlermeldung.
        patientRef.child("kontaktperson1").get()
            .addOnSuccessListener { s1 ->
                val kp1 = s1.getValue(Kontaktperson_c::class.java)
                val slot1Free = kp1 == null || (kp1.name.isBlank() && kp1.telefon.isBlank() && kp1.email.isBlank() && kp1.beziehung.isBlank())

                patientRef.child("kontaktperson2").get()
                    .addOnSuccessListener { s2 ->
                        val kp2 = s2.getValue(Kontaktperson_c::class.java)
                        val slot2Free = kp2 == null || (kp2.name.isBlank() && kp2.telefon.isBlank() && kp2.email.isBlank() && kp2.beziehung.isBlank())

                        // Duplikate vermeiden: gleiche Telefon oder gleiche Mail oder gleicher Name (optional)
                        val duplicate =
                            (!slot1Free && equalsKontakt(kp1, newKp)) ||
                                    (!slot2Free && equalsKontakt(kp2, newKp)) ||
                                    (!slot1Free && kp1.telefon.isNotBlank() && kp1.telefon.equals(newKp.telefon, true)) ||
                                    (!slot2Free && kp2.telefon.isNotBlank() && kp2.telefon.equals(newKp.telefon, true)) ||
                                    (!slot1Free && kp1.email.isNotBlank() && kp1.email.equals(newKp.email, true)) ||
                                    (!slot2Free && kp2.email.isNotBlank() && kp2.email.equals(newKp.email, true))

                        if (duplicate) {
                            btnSave.isEnabled = true
                            Toast.makeText(this, "Diese Kontaktperson ist schon vorhanden.", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        when {
                            slot1Free -> {
                                patientRef.child("kontaktperson1").setValue(newKp)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Kontaktperson gespeichert!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, Kontaktperson::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        btnSave.isEnabled = true
                                        Toast.makeText(this, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            slot2Free -> {
                                patientRef.child("kontaktperson2").setValue(newKp)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Kontaktperson gespeichert!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, Kontaktperson::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        btnSave.isEnabled = true
                                        Toast.makeText(this, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            else -> {
                                btnSave.isEnabled = true
                                Toast.makeText(this, "Maximal 2 Kontaktpersonen erlaubt.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        Toast.makeText(this, "Fehler beim Prüfen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, "Fehler beim Prüfen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun equalsKontakt(a: Kontaktperson_c?, b: Kontaktperson_c): Boolean {
        if (a == null) return false
        return a.name.equals(b.name, ignoreCase = true) &&
                a.beziehung.equals(b.beziehung, ignoreCase = true) &&
                a.telefon.equals(b.telefon, ignoreCase = true) &&
                a.email.equals(b.email, ignoreCase = true)
    }
}
