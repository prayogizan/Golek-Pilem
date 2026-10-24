package com.prayogizan.golekpilem.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceTest {

    @Test
    fun `Success holds data and returns data from dataOrNull`() {
        val resource: Resource<String> = Resource.Success("Test Movie")
        assertTrue(resource is Resource.Success)
        assertEquals("Test Movie", (resource as Resource.Success).data)
        assertEquals("Test Movie", resource.dataOrNull())
    }

    @Test
    fun `Error holds message and cause and returns null from dataOrNull`() {
        val exception = RuntimeException("Network Error")
        val resource: Resource<String> = Resource.Error("Something went wrong", exception)
        assertTrue(resource is Resource.Error)
        val error = resource as Resource.Error
        assertEquals("Something went wrong", error.message)
        assertEquals(exception, error.cause)
        assertNull(resource.dataOrNull())
    }

    @Test
    fun `Loading returns null from dataOrNull`() {
        val resource: Resource<String> = Resource.Loading
        assertTrue(resource is Resource.Loading)
        assertNull(resource.dataOrNull())
    }

    @Test
    fun `map transforms Success data`() {
        val resource: Resource<Int> = Resource.Success(42)
        val mapped = resource.map { it.toString() }

        assertTrue(mapped is Resource.Success)
        assertEquals("42", (mapped as Resource.Success).data)
    }

    @Test
    fun `map preserves Error and Loading states`() {
        val errorResource: Resource<Int> = Resource.Error("Failed")
        val mappedError = errorResource.map { it.toString() }
        assertTrue(mappedError is Resource.Error)
        assertEquals("Failed", (mappedError as Resource.Error).message)

        val loadingResource: Resource<Int> = Resource.Loading
        val mappedLoading = loadingResource.map { it.toString() }
        assertTrue(mappedLoading is Resource.Loading)
    }
}
