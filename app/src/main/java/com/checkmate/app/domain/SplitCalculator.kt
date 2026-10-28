package com.checkmate.app.domain

import kotlin.math.roundToLong

/** One receipt line: its price and the mates who shared it. */
data class SplitLine(val price: Double, val mateIds: Set<Long>)

/** What one mate owes, in cents. */
data class MateShare(val mateId: Long, val subtotalCents: Long, val tipCents: Long) {
    val totalCents: Long get() = subtotalCents + tipCents
}

/**
 * Splits a bill. Works in integer cents so shares always add up exactly:
 * - each item is divided equally between the mates who had it; leftover cents go one each to
 *   the mates with the lowest ids,
 * - the tip is proportional: every mate tips `tipPercent` of their own subtotal.
 */
object SplitCalculator {
    fun split(lines: List<SplitLine>, tipPercent: Int): List<MateShare> {
        val subtotals = mutableMapOf<Long, Long>()
        for (line in lines) {
            if (line.mateIds.isEmpty()) continue
            val cents = (line.price * 100).roundToLong()
            val mates = line.mateIds.sorted()
            val base = cents / mates.size
            val remainder = cents % mates.size
            mates.forEachIndexed { index, mateId ->
                val extra = if (index < remainder) 1 else 0
                subtotals[mateId] = (subtotals[mateId] ?: 0L) + base + extra
            }
        }
        return subtotals.entries
            .sortedBy { it.key }
            .map { (mateId, subtotal) ->
                MateShare(mateId, subtotal, (subtotal * tipPercent / 100.0).roundToLong())
            }
    }
}
