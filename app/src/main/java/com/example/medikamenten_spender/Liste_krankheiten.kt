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

class Liste_krankheiten : ComponentActivity() {

    private lateinit var diseaseContainer: LinearLayout
    private val currentDiseases: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liste_krankheiten)

        diseaseContainer = findViewById(R.id.diseaseContainer)

        findViewById<ImageView>(R.id.btnBack_dis_list).setOnClickListener { finish() }

        loadDiseases()
    }

    private fun loadDiseases() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Kein Benutzer eingeloggt.", Toast.LENGTH_LONG).show()
            return
        }

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("krankheiten")

        ref.get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children
                    .mapNotNull { it.getValue(String::class.java)?.trim() }
                    .filter { it.isNotBlank() }

                currentDiseases.clear()
                currentDiseases.addAll(list)

                renderDiseases()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fehler beim Laden: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun renderDiseases() {
        diseaseContainer.removeAllViews()

        if (currentDiseases.isEmpty()) {
            addEmptyHint("Noch keine Krankheiten gespeichert.")
            return
        }

        currentDiseases.forEachIndexed { index, disease ->
            addDiseaseCard(index, disease)
        }
    }

    private fun deleteDiseaseAt(index: Int) {
        if (index !in currentDiseases.indices) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        currentDiseases.removeAt(index)

        val ref = FirebaseDatabase.getInstance()
            .getReference("patienten")
            .child(uid)
            .child("krankheiten")

        ref.setValue(currentDiseases)
            .addOnSuccessListener {
                renderDiseases()
                Toast.makeText(this, "Krankheit gelöscht.", Toast.LENGTH_SHORT).show()
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
        diseaseContainer.addView(tv)
    }

    private fun addDiseaseCard(index: Int, disease: String) {
        val card = CardView(this).apply {
            radius = dp(18).toFloat()
            cardElevation = dp(8).toFloat()
            useCompatPadding = true
            setCardBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(14) }
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

        val tvDisease = TextView(this).apply {
            text = disease
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
            setOnClickListener { deleteDiseaseAt(index) }
        }

        row.addView(tvDisease)
        row.addView(btnDelete)

        card.addView(row)
        diseaseContainer.addView(card)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
