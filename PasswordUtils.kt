package com.example.mindmile

object PasswordUtils {
    fun getPasswordErrors(password: String): List<String> {
        val errors = mutableListOf<String>()
        if (password.length < 8) errors.add("• At least 8 characters")
        if (!password.any { it.isUpperCase() }) errors.add("• At least one uppercase letter")
        if (!password.any { it.isLowerCase() }) errors.add("• At least one lowercase letter")
        if (!password.any { it.isDigit() }) errors.add("• At least one digit")
        if (!password.any { !it.isLetterOrDigit() }) errors.add("• At least one special character")
        return errors
    }
}
