package com.example.medikamenten_spender

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object Validate {

    fun isValidName(fullName: String): Boolean {
        if (fullName.isBlank()) return false
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.size < 2) return false
        val regex = Regex("^[A-Za-zÄÖÜäöüß'\\- ]{2,}$")
        return regex.matches(fullName)
    }

    fun isValidGermanDate(date: String): Boolean {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        sdf.isLenient = false
        return try {
            sdf.parse(date)
            true
        } catch (_: ParseException) {
            false
        }
    }

    fun isValidAddress(address: String): Boolean {
        return address.trim().length >= 5
    }

    fun isValidPhone(phone: String): Boolean {
        val p = phone.trim()
        if (p.length < 6) return false
        val regex = Regex("^[+0-9][0-9 ()\\-]{5,}$")
        return regex.matches(p)
    }

    /** Hilfsfunktion: "Max Mustermann" -> Pair("Max", "Mustermann") */
    fun splitVornameNachname(fullName: String): Pair<String, String> {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val vorname = parts.firstOrNull().orEmpty()
        val nachname = parts.drop(1).joinToString(" ")
        return Pair(vorname, nachname)
    }
}
