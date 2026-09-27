package com.example.model

data class ContactEntry(
    val name: String,
    val phoneNumbers: List<String>,
    val category: String = "Friend", // "Family", "Friend", "Work"
    val isFavorite: Boolean = false
) {
    val initials: String
        get() = name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { name.take(1).uppercase() }
}

data class ContactNumberRow(
    val rowNumber: Int,
    val contactName: String,
    val phoneNumber: String,
    val label: String = "Mobile"
)
