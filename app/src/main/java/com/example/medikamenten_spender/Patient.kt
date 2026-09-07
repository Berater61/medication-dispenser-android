package com.example.medikamenten_spender

import com.google.firebase.database.FirebaseDatabase

// 1) Kontaktperson (Familie/Bezugsperson)
data class Kontaktperson_c(
    val name: String = "",
    val beziehung: String = "",
    val telefon: String = "",
    val email: String = ""
)

// 2) Arzt / Pflegekraft
data class MedizinKontakt(
    val name: String = "",
    val rolle: String = "",
    val telefon: String = "",
    val email: String = ""
)

// 3) Medikament
data class Medikament(
    val name: String = "",
    val dosisHinweis: String = ""
)

// 4) Patient (erweitert)
data class Patient(
    val vorname: String = "",
    val nachname: String = "",
    val geburtsdatum: String = "",
    val adresse: String = "",
    val telefonnummer: String = "",
    val versicherungsnummer: String = "",
    val email: String = "",

    val kontaktperson1: Kontaktperson_c = Kontaktperson_c(),
    val kontaktperson2: Kontaktperson_c = Kontaktperson_c(),
    val kontaktperson3: Kontaktperson_c = Kontaktperson_c(),

    val medizinKontakt1: MedizinKontakt = MedizinKontakt(),
    val medizinKontakt2: MedizinKontakt = MedizinKontakt(),
    val medizinKontakt3: MedizinKontakt = MedizinKontakt(),

    val medikamente: List<Medikament> = emptyList(),
    val allergien: List<String> = emptyList(),
    val krankheiten: List<String> = emptyList()
) {

    // -------------------------
    // Firebase Helper
    // -------------------------
    private fun patientRef(uid: String) =
        FirebaseDatabase.getInstance().getReference("patienten").child(uid)

    // ✅ 1) Komplettes Objekt überschreiben
    fun saveOverwrite(uid: String) =
        patientRef(uid).setValue(this)

    // ✅ 2) Nur Basisdaten überschreiben (ohne Rest anzufassen)
    fun overwriteBasicData(
        uid: String,
        vorname: String,
        nachname: String,
        geburtsdatum: String,
        adresse: String,
        telefonnummer: String,
        versicherungsnummer: String,
        email: String
    ) = patientRef(uid).updateChildren(
        mapOf(
            "vorname" to vorname,
            "nachname" to nachname,
            "geburtsdatum" to geburtsdatum,
            "adresse" to adresse,
            "telefonnummer" to telefonnummer,
            "versicherungsnummer" to versicherungsnummer,
            "email" to email
        )
    )

    // ✅ 3) Eine Kontaktperson (Slot 1-3) überschreiben
    fun overwriteKontaktperson(uid: String, slot: Int, kp: Kontaktperson_c) {
        val key = when (slot) {
            1 -> "kontaktperson1"
            2 -> "kontaktperson2"
            3 -> "kontaktperson3"
            else -> throw IllegalArgumentException("Slot muss 1, 2 oder 3 sein.")
        }
        patientRef(uid).child(key).setValue(kp)
    }

    // ✅ 4) Einen MedizinKontakt (Slot 1-3) überschreiben
    fun overwriteMedizinKontakt(uid: String, slot: Int, mk: MedizinKontakt) {
        val key = when (slot) {
            1 -> "medizinKontakt1"
            2 -> "medizinKontakt2"
            3 -> "medizinKontakt3"
            else -> throw IllegalArgumentException("Slot muss 1, 2 oder 3 sein.")
        }
        patientRef(uid).child(key).setValue(mk)
    }

    // ✅ 5) Listen komplett überschreiben
    fun overwriteMedikamente(uid: String, list: List<Medikament>) =
        patientRef(uid).child("medikamente").setValue(list)

    fun overwriteAllergien(uid: String, list: List<String>) =
        patientRef(uid).child("allergien").setValue(list)

    fun overwriteKrankheiten(uid: String, list: List<String>) =
        patientRef(uid).child("krankheiten").setValue(list)

    // ✅ 6) Convenience: Medikament hinzufügen (überschreibt Liste danach)
    fun addMedikament(uid: String, med: Medikament) {
        val newList = medikamente + med
        overwriteMedikamente(uid, newList)
    }

    // ✅ 7) Convenience: Medikament entfernen nach Name (überschreibt Liste danach)
    fun removeMedikamentByName(uid: String, name: String) {
        val newList = medikamente.filterNot { it.name.equals(name, ignoreCase = true) }
        overwriteMedikamente(uid, newList)
    }

    // ✅ 8) Convenience: Allergie hinzufügen / entfernen
    fun addAllergie(uid: String, allergie: String) {
        val newList = allergien + allergie
        overwriteAllergien(uid, newList)
    }

    fun removeAllergie(uid: String, allergie: String) {
        val newList = allergien.filterNot { it.equals(allergie, ignoreCase = true) }
        overwriteAllergien(uid, newList)
    }
}
