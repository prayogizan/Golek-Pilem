package com.prayogizan.golekpilem.ui.common

import com.prayogizan.golekpilem.ui.theme.RatingHigh
import com.prayogizan.golekpilem.ui.theme.RatingLow
import com.prayogizan.golekpilem.ui.theme.RatingMedium
import org.junit.Assert.assertEquals
import org.junit.Test

class RatingIndicatorTest {

    @Test
    fun `getRatingColor returns RatingHigh for scores greater than or equal to 7_0`() {
        assertEquals(RatingHigh, getRatingColor(7.0))
        assertEquals(RatingHigh, getRatingColor(7.1))
        assertEquals(RatingHigh, getRatingColor(8.4))
        assertEquals(RatingHigh, getRatingColor(10.0))
    }

    @Test
    fun `getRatingColor returns RatingMedium for scores between 5_0 and 6_9`() {
        assertEquals(RatingMedium, getRatingColor(5.0))
        assertEquals(RatingMedium, getRatingColor(5.8))
        assertEquals(RatingMedium, getRatingColor(6.9))
    }

    @Test
    fun `getRatingColor returns RatingLow for scores strictly less than 5_0`() {
        assertEquals(RatingLow, getRatingColor(4.9))
        assertEquals(RatingLow, getRatingColor(3.5))
        assertEquals(RatingLow, getRatingColor(0.0))
    }

    @Test
    fun `score percentage calculation accurately produces whole percentage integers`() {
        val testCases = listOf(
            8.4 to 84,
            7.0 to 70,
            6.9 to 69,
            5.0 to 50,
            4.9 to 49,
            0.0 to 0,
            10.0 to 100
        )

        testCases.forEach { (score, expectedPercentage) ->
            val actualPercentage = (score.coerceIn(0.0, 10.0) * 10).toInt()
            assertEquals("Mismatch for score $score", expectedPercentage, actualPercentage)
        }
    }
}
