package com.example

import com.example.model.ChatMessage
import com.example.model.ContactEntry
import com.example.model.MessageRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceNotesAndContactsTest {

    @Test
    fun `contacts filter correctly by name`() {
        val contacts = listOf(
            ContactEntry("Mom", listOf("+1 555-1111"), category = "Family"),
            ContactEntry("Dad", listOf("+1 555-2222"), category = "Family"),
            ContactEntry("Alex Rivera", listOf("+1 555-3333"), category = "Friend"),
            ContactEntry("Emma Watson", listOf("+1 555-4444"), category = "Friend")
        )

        val query = "al"
        val filtered = contacts.filter { it.name.contains(query, ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("Alex Rivera", filtered.first().name)
    }

    @Test
    fun `contacts filter by category`() {
        val contacts = listOf(
            ContactEntry("Mom", listOf("+1 555-1111"), category = "Family"),
            ContactEntry("Dad", listOf("+1 555-2222"), category = "Family"),
            ContactEntry("Alex Rivera", listOf("+1 555-3333"), category = "Friend")
        )

        val family = contacts.filter { it.category.equals("Family", ignoreCase = true) }
        assertEquals(2, family.size)
        assertTrue(family.all { it.category == "Family" })
    }

    @Test
    fun `contact initials are generated properly`() {
        val alex = ContactEntry("Alex Rivera", listOf("+1 555-3333"))
        assertEquals("AR", alex.initials)

        val mom = ContactEntry("Mom", listOf("+1 555-1111"))
        assertEquals("M", mom.initials)
    }

    @Test
    fun `voice note message captures duration and audio path`() {
        val note = ChatMessage(
            role = MessageRole.USER,
            text = "Voice note for Mom (8 s)",
            isVoiceNote = true,
            audioFilePath = "/data/cache/voice_notes/test.m4a",
            durationSeconds = 8
        )

        assertTrue(note.isVoiceNote)
        assertEquals(8, note.durationSeconds)
        assertEquals("/data/cache/voice_notes/test.m4a", note.audioFilePath)
        assertFalse(note.isVoice)
    }
}
