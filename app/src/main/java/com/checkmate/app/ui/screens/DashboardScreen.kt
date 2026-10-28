package com.checkmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checkmate.app.data.db.entities.ReceiptWithDetails
import com.checkmate.app.ui.components.AvatarStack
import com.checkmate.app.ui.components.MateAvatar
import com.checkmate.app.ui.formatDate
import com.checkmate.app.ui.formatMoney
import com.checkmate.app.ui.theme.Blue50
import com.checkmate.app.ui.theme.Blue600
import com.checkmate.app.ui.theme.InkMuted
import com.checkmate.app.ui.theme.Navy700
import com.checkmate.app.ui.viewmodel.DashboardViewModel

private const val USER_NAME = "Alex"

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onScanClick: () -> Unit) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Greeting() }
            item { ScanCard(onClick = onScanClick) }
            item { Stats(receipts.orEmpty()) }
            item {
                Text(
                    text = "Recent activity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            val list = receipts
            when {
                list == null -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                list.isEmpty() -> item {
                    Text(
                        text = "No splits yet. Scan your first receipt!",
                        color = InkMuted,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
                else -> items(list, key = { it.receipt.id }) { ReceiptRow(it) }
            }
        }
    }
}

@Composable
private fun Greeting() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Hi, $USER_NAME! 👋", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Who's paying today?",
                style = MaterialTheme.typography.bodyLarge,
                color = InkMuted,
            )
        }
        MateAvatar(name = USER_NAME, color = Blue600, size = 46.dp)
    }
}

@Composable
private fun ScanCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Blue600),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Blue600, Navy700)))
                .padding(horizontal = 20.dp, vertical = 26.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Scan New Receipt", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    text = "Snap it and split it in seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun Stats(receipts: List<ReceiptWithDetails>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile("Bills split", receipts.size.toString(), Modifier.weight(1f))
        StatTile("Total split", formatMoney(receipts.sumOf { it.receipt.total }), Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = InkMuted)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ReceiptRow(details: ReceiptWithDetails) {
    val receipt = details.receipt
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Blue50),
            ) {
                Icon(merchantIcon(receipt.merchantName), contentDescription = null, tint = Blue600)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = receipt.merchantName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${formatDate(receipt.createdAt)} · ${details.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
                Spacer(Modifier.height(6.dp))
                AvatarStack(details.mates, size = 24.dp)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatMoney(receipt.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${details.mates.size} people", style = MaterialTheme.typography.bodySmall, color = InkMuted)
            }
        }
    }
}

private fun merchantIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        listOf("coffee", "cafe", "café", "starbucks").any { it in lower } -> Icons.Filled.LocalCafe
        listOf("pizza", "italia", "restaurant", "bistro", "grill", "burger").any { it in lower } -> Icons.Filled.Restaurant
        else -> Icons.Filled.Receipt
    }
}
