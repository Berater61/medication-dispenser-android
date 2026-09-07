package com.example.medikamenten_spender

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Liste_allergien : ComponentActivity() {

    private lateinit var allergyContainer: LinearLayout
    private val currentAllergies: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liste_allergien)

        allergyContainer = findViewById(R.id.allergyContainer)

        findViewById<ImageView>(R.id.btnBack_al_list).setOnClickListener { finish() }

        loadAllergies()
    }

    private fun loadAllergies() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("allergien")

        ref.get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children
                    .mapNotNull { it.getValue(String::class.java)?.trim() }
                    .filter { it.isNotBlank() }

                currentAllergies.clear()
                currentAllergies.addAll(list)

                renderAllergies()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun renderAllergies() {
        allergyContainer.removeAllViews()

        if (currentAllergies.isEmpty()) {
            addEmptyHint("Noch keine Allergien gespeichert.")
            return
        }

        currentAllergies.forEachIndexed { index, allergy ->
            addAllergyCard(index, allergy)
        }
    }

    private fun deleteAllergyAt(index: Int) {
        if (index !in currentAllergies.indices) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        currentAllergies.removeAt(index)

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("allergien")

        // Allergien sind bei dir eine Liste -> wir speichern als Array wieder rein
        ref.setValue(currentAllergies)
            .addOnSuccessListener {
                renderAllergies()
                Toast.makeText(this, "Allergie gelöscht.", Toast.LENGTH_SHORT).show()
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
        allergyContainer.addView(tv)
    }

    private fun addAllergyCard(index: Int, allergy: String) {
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

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val tvAllergy = TextView(this).apply {
            text = allergy
            textSize = 18f
            setTextColor(0xFF263238.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val btnDelete = ImageView(this).apply {
            setImageResource(R.drawable.asset_delete)
            contentDescription = "Löschen"
            layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply {
                marginStart = dp(12)
            }
            setPadding(dp(2), dp(2), dp(2), dp(2))
            isClickable = true
            isFocusable = true
            setOnClickListener { deleteAllergyAt(index) }
        }

        row.addView(tvAllergy)
        row.addView(btnDelete)

        card.addView(row)
        allergyContainer.addView(card)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
