package com.prayogizan.golekpilem.ui.navigation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Screen_Main serializes and deserializes correctly`() {
        val serialized = json.encodeToString<Screen>(Screen.Main)
        val deserialized = json.decodeFromString<Screen>(serialized)

        assertTrue(deserialized is Screen.Main)
        assertEquals(Screen.Main, deserialized)
    }

    @Test
    fun `Screen_Detail serializes and deserializes correctly with movie arguments`() {
        val original = Screen.Detail(id = 533535, mediaType = "movie")
        val serialized = json.encodeToString<Screen>(original)
        val deserialized = json.decodeFromString<Screen>(serialized)

        assertTrue(deserialized is Screen.Detail)
        val detail = deserialized as Screen.Detail
        assertEquals(533535, detail.id)
        assertEquals("movie", detail.mediaType)
    }

    @Test
    fun `Screen_Detail serializes and deserializes correctly with tv arguments`() {
        val original = Screen.Detail(id = 1399, mediaType = "tv")
        val serialized = json.encodeToString<Screen>(original)
        val deserialized = json.decodeFromString<Screen>(serialized)

        assertTrue(deserialized is Screen.Detail)
        val detail = deserialized as Screen.Detail
        assertEquals(1399, detail.id)
        assertEquals("tv", detail.mediaType)
    }
}
