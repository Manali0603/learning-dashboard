package com.example.intellipaatassignment.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `valid credentials pass`() {
        assertTrue(LoginValidator.validate("learner@example.com", "password123").isValid)
    }

    @Test
    fun `blank fields report required errors`() {
        val result = LoginValidator.validate("", "")
        assertEquals("Email is required", result.emailError)
        assertEquals("Password is required", result.passwordError)
    }

    @Test
    fun `malformed emails are rejected`() {
        listOf("learner", "learner@", "learner@example", "@example.com", "a b@example.com")
            .forEach { assertNotNull("expected error for '$it'", LoginValidator.validateEmail(it)) }
    }

    @Test
    fun `short password is rejected`() {
        assertNotNull(LoginValidator.validatePassword("12345"))
        assertNull(LoginValidator.validatePassword("123456"))
    }
}
