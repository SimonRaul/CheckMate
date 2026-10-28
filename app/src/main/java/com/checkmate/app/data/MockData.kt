package com.checkmate.app.data

/** What the (simulated) scanner "reads" off the receipt in the viewfinder. */
object MockData {
    const val SCANNED_MERCHANT = "Bella Italia"

    val scannedItems: List<Pair<String, Double>> = listOf(
        "Margherita Pizza" to 9.50,
        "Spaghetti Carbonara" to 11.90,
        "Garlic Bread" to 4.50,
        "Tiramisu" to 6.00,
        "Coca-Cola 0.5L" to 3.20,
    )
}
