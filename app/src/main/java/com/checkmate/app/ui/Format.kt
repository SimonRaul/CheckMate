package com.checkmate.app.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CURRENCY = "€"

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

fun formatMoney(amount: Double): String = String.format(Locale.US, "%s%.2f", CURRENCY, amount)

fun formatCents(cents: Long): String = formatMoney(cents / 100.0)

fun formatDate(epochMillis: Long): String = dateFormatter.format(Instant.ofEpochMilli(epochMillis))

const val CURRENCY_SYMBOL = CURRENCY
