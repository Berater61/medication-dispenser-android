package com.example.medikamenten_spender

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class Scanner : AppCompatActivity() {

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            // Abgebrochen
            finish()
        } else {
            showResultDialog(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Direkt Scan starten
        val options = ScanOptions().apply {
            setPrompt("     ")
            setBeepEnabled(true)
            setOrientationLocked(false)

            // Wichtig: DataMatrix priorisieren
            setDesiredBarcodeFormats(ScanOptions.DATA_MATRIX)

            // Optional: Wenn du auch QR willst:
            // setDesiredBarcodeFormats(ScanOptions.DATA_MATRIX, ScanOptions.QR_CODE)

            // Optional: bessere Robustheit bei schwierigen Codes
            setBarcodeImageEnabled(true)
        }

        scanLauncher.launch(options)
    }

    private fun showResultDialog(raw: String) {
        AlertDialog.Builder(this)
            .setTitle("Scan Ergebnis")
            .setMessage(raw.take(4000))
            .setPositiveButton("Kopieren") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("scan_raw", raw))
                Toast.makeText(this, "In Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Schließen") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
