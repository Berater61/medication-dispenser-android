package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registrierung : ComponentActivity() {

    private lateinit var btnRegistrieren: Button
    private lateinit var vorname: EditText
    private lateinit var nachname: EditText
    private lateinit var geburtsdatum: EditText
    private lateinit var adresse: EditText
    private lateinit var telefonnummer: EditText
    private lateinit var versicherungsnummer: EditText
    private lateinit var username: EditText
    private lateinit var passwort: EditText
    private lateinit var passwort2: EditText

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registrierung)

        auth = FirebaseAuth.getInstance()

        btnRegistrieren = findViewById(R.id.registrierung_button)
        vorname = findViewById(R.id.vorname)
        nachname = findViewById(R.id.nachname)
        geburtsdatum = findViewById(R.id.geburtsdatum)
        adresse = findViewById(R.id.adresse)
        telefonnummer = findViewById(R.id.telefonnummer)
        versicherungsnummer = findViewById(R.id.versicherungsnummer)
        username = findViewById(R.id.username_register)
        passwort = findViewById(R.id.passwort_register)
        passwort2 = findViewById(R.id.passwort_register1)

        btnRegistrieren.setOnClickListener {

            val email = username.text.toString().trim()
            val pw1 = passwort.text.toString()
            val pw2 = passwort2.text.toString()

            val vname = vorname.text.toString().trim()
            val nname = nachname.text.toString().trim()
            val geb = geburtsdatum.text.toString().trim()
            val adr = adresse.text.toString().trim()
            val tel = telefonnummer.text.toString().trim()
            val versNr = versicherungsnummer.text.toString().trim()

            if (email.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) {
                Toast.makeText(
                    this@Registrierung,
                    "Bitte E-Mail und Passwort eingeben.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (pw1 != pw2) {
                Toast.makeText(
                    this@Registrierung,
                    "Passwörter stimmen nicht überein.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pw1)
                .addOnCompleteListener(this@Registrierung) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        if (uid == null) {
                            Toast.makeText(
                                this@Registrierung,
                                "Fehler: UID ist null.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@addOnCompleteListener
                        }

                        val patient = Patient(
                            vorname = vname,
                            nachname = nname,
                            geburtsdatum = geb,
                            adresse = adr,
                            telefonnummer = tel,
                            versicherungsnummer = versNr,
                            email = email
                        )

                        val database = FirebaseDatabase.getInstance()
                        val dbRef = database.getReference("patienten").child(uid)

                        dbRef.setValue(patient)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@Registrierung,
                                    "Registrierung erfolgreich!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@Registrierung, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@Registrierung,
                                    "Daten konnten nicht gespeichert werden: ${e.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        Toast.makeText(
                            this@Registrierung,
                            task.exception?.localizedMessage ?: "Registrierung fehlgeschlagen.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
