package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Kontaktperson : AppCompatActivity() {

    private lateinit var bottomnav: BottomNavigationView

    // Add Buttons
    private lateinit var btnAddFamily: ImageView
    private lateinit var btnAddMedical: ImageView

    // Cards Familie
    private lateinit var cardFamily1: CardView
    private lateinit var cardFamily2: CardView

    // Cards Arzt/Pflege
    private lateinit var cardMed1: CardView
    private lateinit var cardMed2: CardView
    private lateinit var cardMed3: CardView

    // TextViews Familie 1
    private lateinit var tvNameFamily1: TextView
    private lateinit var tvRoleFamily1: TextView
    private lateinit var tvPhoneFamily1: TextView
    private lateinit var tvMailFamily1: TextView

    // TextViews Familie 2
    private lateinit var tvNameFamily2: TextView
    private lateinit var tvRoleFamily2: TextView
    private lateinit var tvPhoneFamily2: TextView
    private lateinit var tvMailFamily2: TextView

    // TextViews Med 1
    private lateinit var tvNameMed1: TextView
    private lateinit var tvRoleMed1: TextView
    private lateinit var tvPhoneMed1: TextView
    private lateinit var tvMailMed1: TextView

    // TextViews Med 2
    private lateinit var tvNameMed2: TextView
    private lateinit var tvRoleMed2: TextView
    private lateinit var tvPhoneMed2: TextView
    private lateinit var tvMailMed2: TextView

    // TextViews Med 3
    private lateinit var tvNameMed3: TextView
    private lateinit var tvRoleMed3: TextView
    private lateinit var tvPhoneMed3: TextView
    private lateinit var tvMailMed3: TextView
    private lateinit var btnDeleteFamily1: ImageView
    private lateinit var btnDeleteFamily2: ImageView

    private lateinit var btnDeleteMed1: ImageView
    private lateinit var btnDeleteMed2: ImageView
    private lateinit var btnDeleteMed3: ImageView
    // Merkt ob Slots belegt sind (damit Delete nur geht wenn was drin ist)
    private var family1HasData = false
    private var family2HasData = false

    private var med1HasData = false
    private var med2HasData = false
    private var med3HasData = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.kontaktperson)
        btnDeleteFamily1 = findViewById(R.id.btnDeleteFamily1)
        btnDeleteFamily2 = findViewById(R.id.btnDeleteFamily2)

        btnDeleteMed1 = findViewById(R.id.btnDeleteMed1)
        btnDeleteMed2 = findViewById(R.id.btnDeleteMed2)
        btnDeleteMed3 = findViewById(R.id.btnDeleteMed3)

        btnDeleteFamily1.setOnClickListener { deleteFamilySlot(1) }
        btnDeleteFamily2.setOnClickListener { deleteFamilySlot(2) }

        btnDeleteMed1.setOnClickListener { deleteMedicalSlot(1) }
        btnDeleteMed2.setOnClickListener { deleteMedicalSlot(2) }
        btnDeleteMed3.setOnClickListener { deleteMedicalSlot(3) }
        // Bottom Nav
        bottomnav = findViewById(R.id.bottomNav)
        bottomnav.selectedItemId = R.id.nav_contacts
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
                    true // du bist schon hier
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, Kalender::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Add Buttons
        btnAddFamily = findViewById(R.id.btnAddFamily)
        btnAddMedical = findViewById(R.id.btnAddMedical)

        btnAddFamily.setOnClickListener {
            startActivity(Intent(this, Fenster_Kontaktperson::class.java))
        }
        btnAddMedical.setOnClickListener {
            startActivity(Intent(this, Fenster_Arzt::class.java))
        }
        // Wenn du später eine Arzt/Pflege-Activity hast, hier ändern:
        // btnAddMedical.setOnClickListener { startActivity(Intent(this, Fenster_Arzt::class.java)) }

        // Cards Familie
        cardFamily1 = findViewById(R.id.cardFamily1)
        cardFamily2 = findViewById(R.id.cardFamily2)

        // Cards Med
        cardMed1 = findViewById(R.id.cardMed1)
        cardMed2 = findViewById(R.id.cardMed2)
        cardMed3 = findViewById(R.id.cardMed3)

        // Familie 1 TextViews
        tvNameFamily1 = findViewById(R.id.tvNameFamily1)
        tvRoleFamily1 = findViewById(R.id.tvRoleFamily1)
        tvPhoneFamily1 = findViewById(R.id.tvPhoneFamily1)
        tvMailFamily1 = findViewById(R.id.tvMailFamily1)

        // Familie 2 TextViews
        tvNameFamily2 = findViewById(R.id.tvNameFamily2)
        tvRoleFamily2 = findViewById(R.id.tvRoleFamily2)
        tvPhoneFamily2 = findViewById(R.id.tvPhoneFamily2)
        tvMailFamily2 = findViewById(R.id.tvMailFamily2)

        // Med 1 TextViews
        tvNameMed1 = findViewById(R.id.tvNameMed1)
        tvRoleMed1 = findViewById(R.id.tvRoleMed1)
        tvPhoneMed1 = findViewById(R.id.tvPhoneMed1)
        tvMailMed1 = findViewById(R.id.tvMailMed1)

        // Med 2 TextViews
        tvNameMed2 = findViewById(R.id.tvNameMed2)
        tvRoleMed2 = findViewById(R.id.tvRoleMed2)
        tvPhoneMed2 = findViewById(R.id.tvPhoneMed2)
        tvMailMed2 = findViewById(R.id.tvMailMed2)

        // Med 3 TextViews
        tvNameMed3 = findViewById(R.id.tvNameMed3)
        tvRoleMed3 = findViewById(R.id.tvRoleMed3)
        tvPhoneMed3 = findViewById(R.id.tvPhoneMed3)
        tvMailMed3 = findViewById(R.id.tvMailMed3)

        // Erstes Laden
        loadContacts()
    }

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    private fun loadContacts() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            hideAllCards()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)

        ref.get()
            .addOnSuccessListener { snapshot ->

                // ===== Familie / Bezugsperson (Slots) =====
                val kp1 = snapshot.child("kontaktperson1").getValue(Kontaktperson_c::class.java)
                val kp2 = snapshot.child("kontaktperson2").getValue(Kontaktperson_c::class.java)

                // ===== Ärzte / Pflege (Slots) =====
                val mk1 = snapshot.child("medizinKontakt1").getValue(MedizinKontakt::class.java)
                val mk2 = snapshot.child("medizinKontakt2").getValue(MedizinKontakt::class.java)
                val mk3 = snapshot.child("medizinKontakt3").getValue(MedizinKontakt::class.java)

                // ✅ WICHTIG: Slot-Status setzen für Delete-Buttons
                markSlotStates(kp1, kp2, mk1, mk2, mk3)

                bindCard(
                    cardFamily1,
                    tvNameFamily1, tvRoleFamily1, tvPhoneFamily1, tvMailFamily1,
                    kp1?.name.orEmpty(),
                    kp1?.beziehung.orEmpty(),
                    kp1?.telefon.orEmpty(),
                    kp1?.email.orEmpty(),
                    alwaysVisible = true
                )

                bindCard(
                    cardFamily2,
                    tvNameFamily2, tvRoleFamily2, tvPhoneFamily2, tvMailFamily2,
                    kp2?.name.orEmpty(),
                    kp2?.beziehung.orEmpty(),
                    kp2?.telefon.orEmpty(),
                    kp2?.email.orEmpty(),
                    alwaysVisible = false
                )

                bindCard(
                    cardMed1,
                    tvNameMed1, tvRoleMed1, tvPhoneMed1, tvMailMed1,
                    mk1?.name.orEmpty(),
                    mk1?.rolle.orEmpty(),
                    mk1?.telefon.orEmpty(),
                    mk1?.email.orEmpty(),
                    alwaysVisible = true
                )

                bindCard(
                    cardMed2,
                    tvNameMed2, tvRoleMed2, tvPhoneMed2, tvMailMed2,
                    mk2?.name.orEmpty(),
                    mk2?.rolle.orEmpty(),
                    mk2?.telefon.orEmpty(),
                    mk2?.email.orEmpty(),
                    alwaysVisible = false
                )

                bindCard(
                    cardMed3,
                    tvNameMed3, tvRoleMed3, tvPhoneMed3, tvMailMed3,
                    mk3?.name.orEmpty(),
                    mk3?.rolle.orEmpty(),
                    mk3?.telefon.orEmpty(),
                    mk3?.email.orEmpty(),
                    alwaysVisible = false
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                hideAllCards()
            }
    }

    private fun hideAllCards() {
        cardFamily1.visibility = View.GONE
        cardFamily2.visibility = View.GONE
        cardMed1.visibility = View.GONE
        cardMed2.visibility = View.GONE
        cardMed3.visibility = View.GONE
    }

    private fun bindCard(
        card: View,
        tvName: TextView,
        tvRole: TextView,
        tvPhone: TextView,
        tvMail: TextView,
        name: String,
        role: String,
        phone: String,
        mail: String,
        alwaysVisible: Boolean
    ) {
        val n = name.trim()
        val r = role.trim()
        val p = phone.trim()
        val m = mail.trim()

        val isEmpty = n.isBlank() && r.isBlank() && p.isBlank() && m.isBlank()

        if (isEmpty && !alwaysVisible) {
            card.visibility = View.GONE
            return
        }

        card.visibility = View.VISIBLE
        tvName.text = n
        tvRole.text = r
        tvPhone.text = p
        tvMail.text = m
    }

    private fun deleteFamilySlot(slot: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        val key = when (slot) {
            1 -> "kontaktperson1"
            2 -> "kontaktperson2"
            else -> return
        }

        // Optional: wenn du verhindern willst, dass leere Karte gelöscht wird
        val hasData = when (slot) {
            1 -> family1HasData
            2 -> family2HasData
            else -> false
        }
        if (!hasData) {
            Toast.makeText(this, "Kein Eintrag zum Löschen.", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child(key)

        // Slot "leeren" statt removeValue -> Struktur bleibt gleich
        ref.setValue(Kontaktperson_c())
            .addOnSuccessListener {
                Toast.makeText(this, "Kontaktperson gelöscht.", Toast.LENGTH_SHORT).show()
                loadContacts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteMedicalSlot(slot: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        val key = when (slot) {
            1 -> "medizinKontakt1"
            2 -> "medizinKontakt2"
            3 -> "medizinKontakt3"
            else -> return
        }

        val hasData = when (slot) {
            1 -> med1HasData
            2 -> med2HasData
            3 -> med3HasData
            else -> false
        }
        if (!hasData) {
            Toast.makeText(this, "Kein Eintrag zum Löschen.", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child(key)

        ref.setValue(MedizinKontakt())
            .addOnSuccessListener {
                Toast.makeText(this, "Kontakt gelöscht.", Toast.LENGTH_SHORT).show()
                loadContacts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
    private fun markSlotStates(
        kp1: Kontaktperson_c?,
        kp2: Kontaktperson_c?,
        mk1: MedizinKontakt?,
        mk2: MedizinKontakt?,
        mk3: MedizinKontakt?
    ) {
        fun kpHasData(kp: Kontaktperson_c?): Boolean {
            if (kp == null) return false
            return kp.name.isNotBlank() || kp.beziehung.isNotBlank() || kp.telefon.isNotBlank() || kp.email.isNotBlank()
        }

        fun mkHasData(mk: MedizinKontakt?): Boolean {
            if (mk == null) return false
            return mk.name.isNotBlank() || mk.rolle.isNotBlank() || mk.telefon.isNotBlank() || mk.email.isNotBlank()
        }

        family1HasData = kpHasData(kp1)
        family2HasData = kpHasData(kp2)

        med1HasData = mkHasData(mk1)
        med2HasData = mkHasData(mk2)
        med3HasData = mkHasData(mk3)
    }

}
