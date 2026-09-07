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

    class Fnster_Medikamenten : AppCompatActivity() {

        private lateinit var btnSave: Button
        private lateinit var btnBack: ImageView

        private lateinit var etMedName: EditText
        private lateinit var etMedHint: EditText
        private lateinit var etMedDose: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.medikament)

            btnSave = findViewById(R.id.btn_save)
            btnBack = findViewById(R.id.btnBackM)

            etMedName = findViewById(R.id.etMedName)
            etMedHint = findViewById(R.id.etMedHint)
            etMedDose = findViewById(R.id.etMedDose)

            btnBack.setOnClickListener {
                startActivity(Intent(this, Profil::class.java))
                finish()
            }

            btnSave.setOnClickListener {
                speichernMedikament()
            }
        }

        private fun speichernMedikament() {
            // Fehler reset
            etMedName.error = null
            etMedHint.error = null
            etMedDose.error = null

            val name = etMedName.text.toString().trim()
            val hint = etMedHint.text.toString().trim()
            val dose = etMedDose.text.toString().trim()

            // Validierung
            if (!isValidText(name, minLen = 2)) {
                etMedName.error = "Bitte einen gültigen Medikamentnamen eingeben."
                etMedName.requestFocus()
                return
            }

            if (!isValidText(hint, minLen = 2)) {
                etMedHint.error = "Bitte einen gültigen Einnahmehinweis eingeben."
                etMedHint.requestFocus()
                return
            }

            // dose ist optional – nur prüfen, wenn etwas drin steht
            if (dose.isNotBlank() && !isValidText(dose, minLen = 1)) {
                etMedDose.error = "Bitte eine gültige Dosierung eingeben."
                etMedDose.requestFocus()
                return
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
                return
            }

            btnSave.isEnabled = false

            // Du speicherst im Model: Medikament(name, dosisHinweis)
            // Wir bauen dafür einen schönen Text aus hint + dose
            val dosisHinweis = if (dose.isBlank()) hint else "$hint ($dose)"
            val newMed = Medikament(name = name, dosisHinweis = dosisHinweis)

            val medsRef = FirebaseDatabase.getInstance()
                .getReference("patienten")
                .child(uid)
                .child("medikamente")

            // 1) bestehende Liste laden
            medsRef.get()
                .addOnSuccessListener { snapshot ->
                    val currentList = mutableListOf<Medikament>()

                    // Snapshot kann Liste sein -> wir lesen jedes Kind als Medikament
                    for (child in snapshot.children) {
                        val med = child.getValue(Medikament::class.java)
                        if (med != null) currentList.add(med)
                    }

                    // Optional: Duplikate vermeiden (gleicher Name)
                    val exists = currentList.any { it.name.equals(newMed.name, ignoreCase = true) }
                    if (exists) {
                        btnSave.isEnabled = true
                        Toast.makeText(this, "Dieses Medikament ist schon vorhanden.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // 2) hinzufügen
                    currentList.add(newMed)

                    // 3) Liste überschreiben
                    medsRef.setValue(currentList)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Medikament gespeichert!", Toast.LENGTH_SHORT).show()
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

        private fun isValidText(text: String, minLen: Int): Boolean {
            if (text.length < minLen) return false
            // Erlaubt Buchstaben/Zahlen/Leerzeichen sowie . , - / ( ) + äöüß
            val regex = Regex("^[A-Za-zÄÖÜäöüß0-9 .,/()\\-+]{${minLen},}$")
            return regex.matches(text)
        }
    }
