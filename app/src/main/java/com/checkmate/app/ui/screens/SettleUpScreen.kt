package com.checkmate.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checkmate.app.ui.components.CheckMateTopBar
import com.checkmate.app.ui.components.MateAvatar
import com.checkmate.app.ui.components.StickyBottomButton
import com.checkmate.app.ui.formatCents
import com.checkmate.app.ui.theme.Blue50
import com.checkmate.app.ui.theme.Blue600
import com.checkmate.app.ui.theme.InkMuted
import com.checkmate.app.ui.theme.Navy700
import com.checkmate.app.ui.theme.Navy900
import com.checkmate.app.ui.theme.Success
import com.checkmate.app.ui.theme.SuccessContainer
import com.checkmate.app.ui.theme.WhatsAppGreen
import com.checkmate.app.ui.viewmodel.ShareRow
import com.checkmate.app.ui.viewmodel.SplitViewModel

private val TipOptions = listOf("No tip" to 0, "10%" to 10, "15%" to 15, "20%" to 20)

@Composable
fun SettleUpScreen(viewModel: SplitViewModel, onBack: () -> Unit, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shares by viewModel.shares.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val subtotalCents = shares.sumOf { it.share.subtotalCents }
    val tipCents = shares.sumOf { it.share.tipCents }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CheckMateTopBar(title = "Settle up", onBack = onBack) },
        bottomBar = {
            StickyBottomButton(
                text = "Save & Finish",
                loading = state.isSaving,
                enabled = shares.isNotEmpty(),
                onClick = {
                    viewModel.save {
                        Toast.makeText(context, "Saved to your history", Toast.LENGTH_SHORT).show()
                        onFinished()
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TotalBillCard(
                    merchant = state.merchantName,
                    totalCents = subtotalCents + tipCents,
                    subtotalCents = subtotalCents,
                    tipCents = tipCents,
                    people = shares.size,
                )
            }
            item {
                TipSection(
                    selectedPercent = state.tipPercent,
                    tipCents = tipCents,
                    onSelect = viewModel::setTip,
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Who owes what", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    val settled = shares.count { it.mate.id in state.settledMateIds }
                    Text("$settled/${shares.size} settled", style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                }
            }
            items(shares, key = { it.mate.id }) { row ->
                OweRow(
                    row = row,
                    settled = row.mate.id in state.settledMateIds,
                    onWhatsApp = {
                        remindViaWhatsApp(context, row.mate.name, formatCents(row.share.totalCents), state.merchantName)
                    },
                    onSettle = { viewModel.toggleSettled(row.mate.id) },
                )
            }
        }
    }
}

@Composable
private fun TotalBillCard(merchant: String, totalCents: Long, subtotalCents: Long, tipCents: Long, people: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Navy900, Navy700, Blue600)))
            .padding(22.dp),
    ) {
        Text("Total bill", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        Text(formatCents(totalCents), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text(merchant, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderStat("Subtotal", formatCents(subtotalCents), Modifier.weight(1f))
            HeaderStat("Tip", formatCents(tipCents), Modifier.weight(1f))
            HeaderStat("People", people.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeaderStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipSection(selectedPercent: Int, tipCents: Long, onSelect: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Add a tip", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (tipCents > 0) {
                    Text("+${formatCents(tipCents)}", style = MaterialTheme.typography.titleMedium, color = Blue600)
                }
            }
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TipOptions.forEachIndexed { index, (label, percent) ->
                    SegmentedButton(
                        selected = percent == selectedPercent,
                        onClick = { onSelect(percent) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TipOptions.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Blue600,
                            activeContentColor = Color.White,
                        ),
                        icon = {},
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "The tip is split proportionally: whoever ordered more, tips more.",
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}

@Composable
private fun OweRow(row: ShareRow, settled: Boolean, onWhatsApp: () -> Unit, onSettle: () -> Unit) {
    val share = row.share
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            MateAvatar(row.mate, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(row.mate.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (settled) "Settled ✓" else "${formatCents(share.subtotalCents)} + ${formatCents(share.tipCents)} tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (settled) Success else InkMuted,
                )
            }
            Text(
                text = formatCents(share.totalCents),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textDecoration = if (settled) TextDecoration.LineThrough else null,
                color = if (settled) InkMuted else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(8.dp))
            if (row.mate.isSelf) {
                Spacer(Modifier.size(40.dp))
            } else {
                ActionIcon(Icons.Filled.Sms, "Remind ${row.mate.name} on WhatsApp", WhatsAppGreen, WhatsAppGreen.copy(alpha = 0.14f), onWhatsApp)
            }
            Spacer(Modifier.width(6.dp))
            if (settled) {
                ActionIcon(Icons.Filled.Check, "Mark ${row.mate.name} as not settled", Success, SuccessContainer, onSettle)
            } else {
                ActionIcon(Icons.Filled.AccountBalance, "Mark ${row.mate.name} as settled", Blue600, Blue50, onSettle)
            }
        }
    }
}

@Composable
private fun ActionIcon(icon: ImageVector, description: String, tint: Color, background: Color, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

/** Opens WhatsApp with a pre-filled reminder; falls back to the system share sheet. */
private fun remindViaWhatsApp(context: Context, name: String, amount: String, merchant: String) {
    val message = "Hey $name! 👋 Your share for $merchant is $amount. Split with CheckMate ✅"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    try {
        context.startActivity(Intent(send).setPackage("com.whatsapp"))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent.createChooser(send, "Send reminder"))
    }
}
