package com.example.medikamenten_spender

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.example.medikamenten_spender.Medikament
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout


class Profil : AppCompatActivity() {
    // Container Krankheiten
    private lateinit var rowDisease1: View
    private lateinit var rowDisease2: View
    private lateinit var rowDisease3: View

    // Container Allergien
    private lateinit var rowAllergy1: View
    private lateinit var rowAllergy2: View
    private lateinit var rowAllergy3: View

    // Container Medikamente
    private lateinit var cardMed1: CardView
    private lateinit var cardMed2: CardView
    private lateinit var cardMed3: CardView

    private lateinit var bottomnav: BottomNavigationView
    private lateinit var person: ImageView
    private lateinit var Krankheiten: ImageView
    private lateinit var Allergie: ImageView
    private lateinit var Medikament: ImageView

    // TextViews Persönliche Daten
    private lateinit var tvName: TextView
    private lateinit var tvBirth: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPhone: TextView

    // TextViews Krankheiten
    private lateinit var tvDisease1: TextView
    private lateinit var tvDisease2: TextView
    private lateinit var tvDisease3: TextView

    // TextViews Allergien
    private lateinit var tvAllergy1: TextView
    private lateinit var tvAllergy2: TextView
    private lateinit var tvAllergy3: TextView

    // TextViews Medikamente
    private lateinit var tvMed1Name: TextView
    private lateinit var tvMed1Hint: TextView
    private lateinit var tvMed2Name: TextView
    private lateinit var tvMed2Hint: TextView
    private lateinit var tvMed3Name: TextView
    private lateinit var tvMed3Hint: TextView
    private lateinit var Medikamenten_list: ConstraintLayout
    private lateinit var Krankheit_list: CardView
    private lateinit var Allergien_list: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profil)

        Medikamenten_list= findViewById(R.id.CardMedikament)
        Krankheit_list= findViewById(R.id.cardDiseases)
        Allergien_list= findViewById(R.id.cardAllergies)
        // Krankheiten Container
        rowDisease1 = findViewById(R.id.disease1)
        rowDisease2 = findViewById(R.id.disease2)
        rowDisease3 = findViewById(R.id.disease3)

// Allergien Container
        rowAllergy1 = findViewById(R.id.allergy1)
        rowAllergy2 = findViewById(R.id.allergy2)
        rowAllergy3 = findViewById(R.id.allergy3)

// Medikamente Container (CardViews)
        cardMed1 = findViewById(R.id.medItem1)
        cardMed2 = findViewById(R.id.medItem2)
        cardMed3 = findViewById(R.id.medItem3)


        bottomnav = findViewById(R.id.bottomNav)
        bottomnav.selectedItemId = R.id.nav_profile

        person = findViewById(R.id.btnEditPersonal)
        Krankheiten = findViewById(R.id.btnEditDiseases)
        Allergie = findViewById(R.id.btnEditAllergies)
        Medikament = findViewById(R.id.btnEditMeds)

        // Persönliche Daten
        tvName = findViewById(R.id.tvName)
        tvBirth = findViewById(R.id.tvBirth)
        tvAddress = findViewById(R.id.tvAddress)
        tvPhone = findViewById(R.id.tvPhone)

        // Krankheiten
        tvDisease1 = findViewById(R.id.tvDisease1)
        tvDisease2 = findViewById(R.id.tvDisease2)
        tvDisease3 = findViewById(R.id.tvDisease3)

        // Allergien
        tvAllergy1 = findViewById(R.id.tvAllergy1)
        tvAllergy2 = findViewById(R.id.tvAllergy2)
        tvAllergy3 = findViewById(R.id.tvAllergy3)

        // Medikamente
        tvMed1Name = findViewById(R.id.tvMed1Name)
        tvMed1Hint = findViewById(R.id.tvMed1Hint)
        tvMed2Name = findViewById(R.id.tvMed2Name)
        tvMed2Hint = findViewById(R.id.tvMed2Hint)
        tvMed3Name = findViewById(R.id.tvMed3Name)
        tvMed3Hint = findViewById(R.id.tvMed3Hint)

        // Clicks zu den Fenstern
        Medikament.setOnClickListener {
            startActivity(Intent(this, Fnster_Medikamenten::class.java))
        }
        person.setOnClickListener {
            startActivity(Intent(this, Fenster_PersoenlicheDaten::class.java))
        }
        Krankheiten.setOnClickListener {
            startActivity(Intent(this, Fenster_Krankheiten::class.java))
        }
        Allergie.setOnClickListener {
            startActivity(Intent(this, Fenster_Allergien::class.java))
        }
        Medikamenten_list.setOnClickListener {
            startActivity(Intent(this, Liste_medikament::class.java))
        }
        Allergien_list.setOnClickListener {
            startActivity(Intent(this, Liste_allergien::class.java))
        }
        Krankheit_list.setOnClickListener {
            startActivity(Intent(this, Liste_krankheiten::class.java))
        }
        bottomnav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    // Du bist schon hier -> nicht neu starten
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

        // initial laden
        loadPatientData()
    }

    override fun onResume() {
        super.onResume()
        loadPatientData()
    }

    private fun bindRow(container: View, tv: TextView, value: String) {
        val v = value.trim()
        if (v.isBlank()) {
            container.visibility = View.GONE
        } else {
            container.visibility = View.VISIBLE
            tv.text = v
        }
    }

    private fun bindMedCard(container: View, tvName: TextView, tvHint: TextView, name: String, hint: String) {
        val n = name.trim()
        val h = hint.trim()

        // Karte nur anzeigen, wenn mindestens eins gefüllt ist
        if (n.isBlank() && h.isBlank()) {
            container.visibility = View.GONE
        } else {
            container.visibility = View.VISIBLE
            tvName.text = n
            tvHint.text = h
        }
    }

    private fun loadPatientData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)

        ref.get()
            .addOnSuccessListener { snapshot ->
                // --- Persönliche Daten ---
                val vorname = snapshot.child("vorname").value?.toString().orEmpty()
                val nachname = snapshot.child("nachname").value?.toString().orEmpty()
                val geb = snapshot.child("geburtsdatum").value?.toString().orEmpty()
                val adr = snapshot.child("adresse").value?.toString().orEmpty()
                val tel = snapshot.child("telefonnummer").value?.toString().orEmpty()

                tvName.text = listOf(vorname, nachname).filter { it.isNotBlank() }.joinToString(" ")
                tvBirth.text = geb
                tvAddress.text = adr
                tvPhone.text = tel

                // --- Krankheiten (Liste) ---
                val krankList = snapshot.child("krankheiten").children
                    .mapNotNull { it.getValue(String::class.java)?.trim() }
                    .filter { it.isNotBlank() }

                bindRow(rowDisease1, tvDisease1, krankList.getOrNull(0).orEmpty())
                bindRow(rowDisease2, tvDisease2, krankList.getOrNull(1).orEmpty())
                bindRow(rowDisease3, tvDisease3, krankList.getOrNull(2).orEmpty())


                // --- Allergien (Liste) ---
                val allergList = snapshot.child("allergien").children
                    .mapNotNull { it.getValue(String::class.java)?.trim() }
                    .filter { it.isNotBlank() }

                bindRow(rowAllergy1, tvAllergy1, allergList.getOrNull(0).orEmpty())
                bindRow(rowAllergy2, tvAllergy2, allergList.getOrNull(1).orEmpty())
                bindRow(rowAllergy3, tvAllergy3, allergList.getOrNull(2).orEmpty())


                // --- Medikamente (Liste von Objekten) ---
                val meds: List<Medikament> =
                    snapshot.child("medikamente")
                        .getValue(object : GenericTypeIndicator<List<Medikament>>() {})
                        ?: emptyList()

                val m1 = meds.getOrNull(0)
                val m2 = meds.getOrNull(1)
                val m3 = meds.getOrNull(2)

                bindMedCard(cardMed1, tvMed1Name, tvMed1Hint, m1?.name.orEmpty(), m1?.dosisHinweis.orEmpty())
                bindMedCard(cardMed2, tvMed2Name, tvMed2Hint, m2?.name.orEmpty(), m2?.dosisHinweis.orEmpty())
                bindMedCard(cardMed3, tvMed3Name, tvMed3Hint, m3?.name.orEmpty(), m3?.dosisHinweis.orEmpty())


            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
