package com.checkmate.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun itemsAreSplitEquallyAndRemainderCentsAreKept() {
        val shares = SplitCalculator.split(
            listOf(
                SplitLine(10.00, setOf(1L, 2L)),
                SplitLine(5.01, setOf(1L, 2L)), // 501 cents -> 251 + 250
                SplitLine(3.00, setOf(3L)),
            ),
            tipPercent = 0,
        ).associateBy { it.mateId }

        assertEquals(751L, shares.getValue(1L).subtotalCents)
        assertEquals(750L, shares.getValue(2L).subtotalCents)
        assertEquals(300L, shares.getValue(3L).subtotalCents)
        assertEquals(1801L, shares.values.sumOf { it.subtotalCents })
    }

    @Test
    fun tipIsProportionalToWhatEachMateOrdered() {
        val shares = SplitCalculator.split(
            listOf(SplitLine(20.00, setOf(1L)), SplitLine(10.00, setOf(2L))),
            tipPercent = 10,
        ).associateBy { it.mateId }

        assertEquals(200L, shares.getValue(1L).tipCents)
        assertEquals(100L, shares.getValue(2L).tipCents)
        assertEquals(2200L, shares.getValue(1L).totalCents)
    }

    @Test
    fun unassignedItemsAreIgnored() {
        val shares = SplitCalculator.split(listOf(SplitLine(9.99, emptySet())), tipPercent = 15)
        assertTrue(shares.isEmpty())
    }
}
