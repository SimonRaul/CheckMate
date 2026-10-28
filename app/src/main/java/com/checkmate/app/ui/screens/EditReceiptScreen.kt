package com.checkmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checkmate.app.ui.CURRENCY_SYMBOL
import com.checkmate.app.ui.components.CheckMateTopBar
import com.checkmate.app.ui.components.StickyBottomButton
import com.checkmate.app.ui.components.dashedBorder
import com.checkmate.app.ui.formatMoney
import com.checkmate.app.ui.theme.Blue600
import com.checkmate.app.ui.theme.Danger
import com.checkmate.app.ui.theme.InkMuted
import com.checkmate.app.ui.theme.Outline
import com.checkmate.app.ui.theme.Success
import com.checkmate.app.ui.theme.SuccessContainer
import com.checkmate.app.ui.viewmodel.DraftItem
import com.checkmate.app.ui.viewmodel.SplitViewModel

@Composable
fun EditReceiptScreen(viewModel: SplitViewModel, onBack: () -> Unit, onConfirm: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CheckMateTopBar(title = "Edit receipt", onBack = onBack) },
        bottomBar = {
            StickyBottomButton(
                text = "Confirm Items",
                onClick = onConfirm,
                enabled = state.canConfirmItems,
                supportingText = "${state.items.size} items · subtotal ${formatMoney(state.subtotal)}",
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
            item { SuccessBanner(state.scannedCount) }
            item {
                OutlinedTextField(
                    value = state.merchantName,
                    onValueChange = viewModel::updateMerchant,
                    label = { Text("Store") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Column(Modifier.padding(top = 4.dp)) {
                    Text("Items", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Fix anything we misread before splitting.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                    )
                }
            }
            items(state.items, key = { it.key }) { item ->
                ItemRow(
                    item = item,
                    onNameChange = { viewModel.updateItemName(item.key, it) },
                    onPriceChange = { viewModel.updateItemPrice(item.key, it) },
                    onDelete = { viewModel.removeItem(item.key) },
                )
            }
            item { AddItemButton(onClick = viewModel::addItem) }
        }
    }
}

@Composable
private fun SuccessBanner(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SuccessContainer)
            .padding(16.dp),
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success)
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Receipt scanned! We found $count items.",
            style = MaterialTheme.typography.titleMedium,
            color = Success,
        )
    }
}

@Composable
private fun ItemRow(
    item: DraftItem,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
        ) {
            OutlinedTextField(
                value = item.name,
                onValueChange = onNameChange,
                placeholder = { Text("Item name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = item.priceText,
                onValueChange = onPriceChange,
                placeholder = { Text("0.00") },
                prefix = { Text(CURRENCY_SYMBOL) },
                singleLine = true,
                isError = item.priceText.isNotEmpty() && (item.price ?: 0.0) <= 0.0,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.width(108.dp),
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete ${item.name}", tint = Danger)
            }
        }
    }
}

@Composable
private fun AddItemButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .dashedBorder(Blue600.copy(alpha = 0.6f), cornerRadius = 16.dp)
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Blue600)
            Spacer(Modifier.width(6.dp))
            Text("Add missing item", color = Blue600, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Blue600,
    unfocusedBorderColor = Outline,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
)
