package com.example.intellipaatassignment.domain

data class LoginValidationResult(
    val emailError: String? = null,
    val passwordError: String? = null
) {
    val isValid: Boolean get() = emailError == null && passwordError == null
}

object LoginValidator {

    const val MIN_PASSWORD_LENGTH = 6

    // Pragmatic check (something@domain.tld), not full RFC 5322 — the server is the real authority.
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(email: String, password: String): LoginValidationResult =
        LoginValidationResult(
            emailError = validateEmail(email),
            passwordError = validatePassword(password)
        )

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email is required"
            !EMAIL_REGEX.matches(trimmed) -> "Enter a valid email address"
            else -> null
        }
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }
}