package com.checkmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checkmate.app.data.db.entities.MateEntity
import com.checkmate.app.ui.components.CheckMateTopBar
import com.checkmate.app.ui.components.MateAvatar
import com.checkmate.app.ui.components.StickyBottomButton
import com.checkmate.app.ui.components.dashedCircle
import com.checkmate.app.ui.formatMoney
import com.checkmate.app.ui.theme.Blue50
import com.checkmate.app.ui.theme.Blue600
import com.checkmate.app.ui.theme.InkMuted
import com.checkmate.app.ui.theme.Navy700
import com.checkmate.app.ui.viewmodel.DraftItem
import com.checkmate.app.ui.viewmodel.SplitViewModel

@Composable
fun AssignMatesScreen(viewModel: SplitViewModel, onBack: () -> Unit, onCalculate: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mates by viewModel.mates.collectAsStateWithLifecycle()
    val selectedMate = mates.firstOrNull { it.id == state.selectedMateId } ?: mates.firstOrNull()
    var showAddMate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CheckMateTopBar(title = "Who had what?", onBack = onBack) },
        bottomBar = {
            val unassigned = state.unassignedCount
            StickyBottomButton(
                text = "Calculate Totals",
                onClick = onCalculate,
                enabled = state.items.isNotEmpty() && unassigned == 0,
                supportingText = if (unassigned == 0) "All items assigned ✓" else "$unassigned item(s) still need someone",
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Your mates",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(mates, key = { it.id }) { mate ->
                        MateChip(
                            mate = mate,
                            selected = mate.id == selectedMate?.id,
                            onClick = { viewModel.selectMate(mate.id) },
                        )
                    }
                    item { AddMateChip(onClick = { showAddMate = true }) }
                }
            }
            item { HintCard(selectedMate) }
            items(state.items, key = { it.key }) { item ->
                AssignItemRow(
                    item = item,
                    mates = mates,
                    selectedMate = selectedMate,
                    onAssignSelected = { selectedMate?.let { viewModel.toggleAssignment(item.key, it.id) } },
                    onRemoveMate = { mateId -> viewModel.toggleAssignment(item.key, mateId) },
                    onSplit = { viewModel.toggleSplit(item.key) },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }

    if (showAddMate) {
        AddMateDialog(
            onDismiss = { showAddMate = false },
            onAdd = { name ->
                viewModel.addMate(name)
                showAddMate = false
            },
        )
    }
}

@Composable
private fun MateChip(mate: MateEntity, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .then(if (selected) Modifier.border(3.dp, Blue600, CircleShape) else Modifier),
        ) {
            MateAvatar(mate, size = 52.dp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = mate.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Blue600 else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AddMateChip(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(62.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .dashedCircle(InkMuted),
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = InkMuted)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Add mate", style = MaterialTheme.typography.bodySmall, color = InkMuted)
    }
}

@Composable
private fun HintCard(selectedMate: MateEntity?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Blue50)
            .padding(12.dp),
    ) {
        Icon(Icons.Filled.TouchApp, contentDescription = null, tint = Blue600)
        Spacer(Modifier.width(10.dp))
        Text(
            text = if (selectedMate != null) {
                "Tap an empty slot to give an item to ${selectedMate.name}. Tap an avatar to remove it."
            } else {
                "Add a mate to start assigning items."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Navy700,
        )
    }
}

@Composable
private fun AssignItemRow(
    item: DraftItem,
    mates: List<MateEntity>,
    selectedMate: MateEntity?,
    onAssignSelected: () -> Unit,
    onRemoveMate: (Long) -> Unit,
    onSplit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val assigned = mates.filter { it.id in item.mateIds }
    val price = item.price ?: 0.0
    val sharedByAll = mates.size > 1 && assigned.size == mates.size
    // Two empty slots on an unassigned item, one while more mates could still join, none when everyone's on it.
    val emptySlots = when {
        assigned.isEmpty() -> 2
        assigned.size < mates.size -> 1
        else -> 0
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatMoney(price), style = MaterialTheme.typography.bodyMedium, color = InkMuted)
                    Spacer(Modifier.width(8.dp))
                    SplitChip(selected = sharedByAll, onClick = onSplit)
                }
                if (assigned.size > 1) {
                    Text(
                        text = "${formatMoney(price / assigned.size)} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = Blue600,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                assigned.take(MAX_SLOT_AVATARS).forEach { mate ->
                    MateAvatar(
                        mate = mate,
                        size = 34.dp,
                        modifier = Modifier.clickable { onRemoveMate(mate.id) },
                    )
                }
                if (assigned.size > MAX_SLOT_AVATARS) {
                    Text("+${assigned.size - MAX_SLOT_AVATARS}", style = MaterialTheme.typography.bodySmall, color = InkMuted)
                }
                repeat(emptySlots) {
                    EmptySlot(enabled = selectedMate != null, onClick = onAssignSelected)
                }
            }
        }
    }
}

private const val MAX_SLOT_AVATARS = 3

@Composable
private fun EmptySlot(enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .dashedCircle(if (enabled) Blue600.copy(alpha = 0.6f) else InkMuted.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Assign", tint = Blue600.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SplitChip(selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Blue600 else Blue50,
        contentColor = if (selected) Color.White else Blue600,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Split", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AddMateDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a mate") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(20) },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onAdd(name) }),
                modifier = Modifier.focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
