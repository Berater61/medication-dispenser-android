package com.example.medikamenten_spender

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class Liste_medikament : ComponentActivity() {

    private lateinit var medContainer: LinearLayout

    // Wir halten die aktuell geladenen Medikamente, damit wir löschen + speichern können
    private val currentMeds: MutableList<Medikament> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medikamenten_liste)

        medContainer = findViewById(R.id.medContainer)

        runCatching {
            findViewById<ImageView>(R.id.btnBack_med).setOnClickListener { finish() }
        }

        loadMedikamente()
    }

    private fun loadMedikamente() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("medikamente")

        ref.get()
            .addOnSuccessListener { snapshot ->
                val meds: List<Medikament> =
                    snapshot.getValue(object : GenericTypeIndicator<List<Medikament>>() {})
                        ?: emptyList()

                currentMeds.clear()
                currentMeds.addAll(meds)

                renderMeds()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun renderMeds() {
        medContainer.removeAllViews()

        if (currentMeds.isEmpty()) {
            addEmptyHint("Noch keine Medikamente gespeichert.")
            return
        }

        currentMeds.forEachIndexed { index, med ->
            addMedCard(
                index = index,
                name = med.name.trim(),
                hint = med.dosisHinweis.trim()
            )
        }
    }

    private fun deleteMedicationAt(index: Int) {
        if (index !in currentMeds.indices) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        currentMeds.removeAt(index)

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("medikamente")

        ref.setValue(currentMeds)
            .addOnSuccessListener {
                renderMeds() // neu zeichnen, damit Buttons/Indizes sauber sind
                Toast.makeText(this, "Medikament gelöscht.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Löschen fehlgeschlagen: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addEmptyHint(text: String) {
        val tv = TextView(this).apply {
            this.text = text
            textSize = 14f
            setPadding(dp(8), dp(12), dp(8), dp(12))
            gravity = Gravity.CENTER
            setTextColor(0xFF6B7A8C.toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        medContainer.addView(tv)
    }

    // ✅ Kein "Medikament 1" mehr, dafür Delete Icon rechts
    private fun addMedCard(index: Int, name: String, hint: String) {
        val card = CardView(this).apply {
            radius = dp(18).toFloat()
            cardElevation = dp(8).toFloat()
            useCompatPadding = true
            setCardBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }

        // Horizontales Layout: links Text, rechts Delete Icon
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val leftCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val tvName = TextView(this).apply {
            text = if (name.isBlank()) "—" else name
            textSize = 18f
            setTextColor(0xFF263238.toInt())
        }

        val tvHint = TextView(this).apply {
            text = if (hint.isBlank()) "Kein Dosishinweis" else hint
            textSize = 13f
            setTextColor(0xFF6B7A8C.toInt())
            setPadding(0, dp(6), 0, 0)
        }

        leftCol.addView(tvName)
        leftCol.addView(tvHint)

        val btnDelete = ImageView(this).apply {
            setImageResource(R.drawable.asset_delete) // dein Asset
            contentDescription = "Löschen"
            layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply {
                marginStart = dp(12)
            }
            setPadding(dp(2), dp(2), dp(2), dp(2))
            isClickable = true
            isFocusable = true

            setOnClickListener {
                deleteMedicationAt(index)
            }
        }

        row.addView(leftCol)
        row.addView(btnDelete)

        card.addView(row)
        medContainer.addView(card)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
