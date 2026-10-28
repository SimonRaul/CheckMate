package com.checkmate.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.checkmate.app.data.MockData
import com.checkmate.app.ui.theme.ScanGlow
import com.checkmate.app.ui.theme.Success
import com.checkmate.app.ui.theme.ViewfinderBottom
import com.checkmate.app.ui.theme.ViewfinderTop
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Simulated camera: a dark viewfinder showing a mock receipt inside a glowing crop frame.
 * The shutter (or gallery) "captures" it and hands the mock items to the Edit screen.
 */
@Composable
fun ScanScreen(onClose: () -> Unit, onCaptured: () -> Unit) {
    var flashOn by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    val shutterFlash by animateFloatAsState(
        targetValue = if (capturing) 0.85f else 0f,
        animationSpec = tween(durationMillis = 140),
        label = "shutterFlash",
    )

    LaunchedEffect(capturing) {
        if (capturing) {
            delay(420)
            onCaptured()
            capturing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ViewfinderTop, ViewfinderBottom))),
    ) {
        MockReceipt(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-24).dp),
        )
        CropFrame(
            Modifier
                .align(Alignment.Center)
                .offset(y = (-24).dp)
                .size(width = 272.dp, height = 404.dp),
        )
        if (flashOn) {
            Box(Modifier.fillMaxSize().background(Color(0x14FFF4D6)))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            RoundIconButton(Icons.Filled.Close, "Close camera", onClick = onClose)
            Text(
                text = "Scan receipt",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(52.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp),
        ) {
            HintPill("Hold steady – edges detected")
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                RoundIconButton(
                    icon = if (flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    description = if (flashOn) "Turn flash off" else "Turn flash on",
                    onClick = { flashOn = !flashOn },
                )
                ShutterButton(onClick = { capturing = true })
                RoundIconButton(Icons.Filled.PhotoLibrary, "Import from gallery", onClick = { capturing = true })
            }
        }

        if (shutterFlash > 0f) {
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = shutterFlash)))
        }
    }
}

@Composable
private fun MockReceipt(modifier: Modifier = Modifier) {
    val ink = Color(0xFF2B2B2B)
    val mono = FontFamily.Monospace
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .rotate(-1.5f)
            .size(width = 240.dp, height = 372.dp)
            .shadow(18.dp, RoundedCornerShape(6.dp))
            .background(Color(0xFFFDFCF7), RoundedCornerShape(6.dp))
            .padding(18.dp),
    ) {
        Text(MockData.SCANNED_MERCHANT.uppercase(), fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ink)
        Text("Via Roma 12 · Table 4", fontFamily = mono, fontSize = 10.sp, color = Color.Gray)
        DashedDivider()
        MockData.scannedItems.forEach { (name, price) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(
                    text = name,
                    fontFamily = mono,
                    fontSize = 11.sp,
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(String.format(Locale.US, "%.2f", price), fontFamily = mono, fontSize = 11.sp, color = ink)
            }
        }
        DashedDivider()
        Row(Modifier.fillMaxWidth()) {
            Text("TOTAL", fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ink, modifier = Modifier.weight(1f))
            Text(
                String.format(Locale.US, "%.2f", MockData.scannedItems.sumOf { it.second }),
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ink,
            )
        }
        Spacer(Modifier.weight(1f))
        Barcode()
        Text("Grazie! Come again", fontFamily = mono, fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun DashedDivider() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(1.dp),
    ) {
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
        )
    }
}

@Composable
private fun Barcode() {
    val widths = listOf(2, 1, 3, 1, 2, 2, 1, 3, 1, 1, 2, 3, 1, 2, 1, 3, 2, 1, 1, 2, 3, 1, 2, 1)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(30.dp)) {
        widths.forEach { w ->
            Box(Modifier.width(w.dp).fillMaxHeight().background(Color(0xFF2B2B2B)))
        }
    }
}

/** Glowing, pulsing crop rectangle with a moving scan line. */
@Composable
private fun CropFrame(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cropFrame")
    val glow by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "glow",
    )
    val scan by transition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanLine",
    )
    Canvas(modifier) {
        val corner = CornerRadius(20.dp.toPx())
        // Soft glow: wide, faint strokes underneath the main border.
        listOf(22.dp, 14.dp, 8.dp).forEach { width ->
            drawRoundRect(
                color = ScanGlow.copy(alpha = 0.12f * glow),
                cornerRadius = corner,
                style = Stroke(width = width.toPx()),
            )
        }
        drawRoundRect(color = ScanGlow, cornerRadius = corner, style = Stroke(width = 3.dp.toPx()))

        val y = size.height * scan
        drawLine(
            brush = Brush.horizontalGradient(listOf(Color.Transparent, ScanGlow, Color.Transparent)),
            start = Offset(12.dp.toPx(), y),
            end = Offset(size.width - 12.dp.toPx(), y),
            strokeWidth = 3.dp.toPx(),
        )
    }
}

@Composable
private fun HintPill(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Success))
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RoundIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f)),
    ) {
        Icon(icon, contentDescription = description, tint = Color.White)
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .scale(if (pressed) 0.92f else 1f)
            .size(82.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(8.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Take photo" },
    )
}
