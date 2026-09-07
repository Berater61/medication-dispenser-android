package com.example.medikamenten_spender

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTest {
    @Test
    fun validProfileValuesAreAccepted() {
        assertTrue(Validate.isValidName("Max Mustermann"))
        assertTrue(Validate.isValidGermanDate("29.02.2024"))
        assertTrue(Validate.isValidAddress("Musterweg 12"))
        assertTrue(Validate.isValidPhone("+49 30 123456"))
    }

    @Test
    fun invalidProfileValuesAreRejected() {
        assertFalse(Validate.isValidName("Max"))
        assertFalse(Validate.isValidGermanDate("31.02.2024"))
        assertFalse(Validate.isValidAddress("A 1"))
        assertFalse(Validate.isValidPhone("abc123"))
    }

    @Test
    fun fullNameIsSplitAtTheFirstName() {
        assertEquals("Max" to "Maria Mustermann", Validate.splitVornameNachname("Max Maria Mustermann"))
    }
}
