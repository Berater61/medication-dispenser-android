package com.example.medikamenten_spender

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Fenster_Urlaubsmodus : AppCompatActivity() {
    private lateinit var btnweiter: Button
    private lateinit var imagezurück: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.urlaub)
        btnweiter= findViewById(R.id.btnWeiter)
        imagezurück= findViewById(R.id.btnBack)


        imagezurück.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
        btnweiter.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

    }
}
