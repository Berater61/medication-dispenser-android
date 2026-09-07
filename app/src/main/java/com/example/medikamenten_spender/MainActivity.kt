package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    private lateinit var anmelden: Button
    private lateinit var passwort: EditText
    private lateinit var username: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.anmeldung)

        val registerHint = findViewById<TextView>(R.id.registerHint)
        anmelden = findViewById(R.id.Anmelden)
        passwort = findViewById(R.id.password)
        username = findViewById(R.id.username)
        auth = FirebaseAuth.getInstance()

        anmelden.setOnClickListener {
            val email = username.text.toString().trim()
            val pw = passwort.text.toString()

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Bitte E-Mail und Passwort eingeben.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Anmeldung erfolgreich!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Home::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.localizedMessage ?: "Anmeldung fehlgeschlagen.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        registerHint.setOnClickListener {
            val intent = Intent(this, Registrierung::class.java)
            startActivity(intent)
        }
    }
}
