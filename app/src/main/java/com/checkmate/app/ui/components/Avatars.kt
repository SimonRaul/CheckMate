package com.checkmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.checkmate.app.data.db.entities.MateEntity
import com.checkmate.app.ui.theme.InkMuted
import com.checkmate.app.ui.theme.Outline

/** Coloured circle with the person's initial. */
@Composable
fun MateAvatar(name: String, color: Color, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
        )
    }
}

@Composable
fun MateAvatar(mate: MateEntity, modifier: Modifier = Modifier, size: Dp = 40.dp) =
    MateAvatar(name = mate.name, color = Color(mate.color), modifier = modifier, size = size)

/** Overlapping avatars, e.g. on a Dashboard receipt row. Shows "+N" past [maxVisible]. */
@Composable
fun AvatarStack(
    mates: List<MateEntity>,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    maxVisible: Int = 3,
) {
    val visible = mates.take(maxVisible)
    val extra = mates.size - visible.size
    val count = visible.size + if (extra > 0) 1 else 0
    if (count == 0) return
    val step = size * 0.65f
    Box(modifier.width(size + step * (count - 1)).height(size)) {
        visible.forEachIndexed { index, mate ->
            MateAvatar(
                mate = mate,
                size = size,
                modifier = Modifier
                    .offset(x = step * index)
                    .border(2.dp, Color.White, CircleShape),
            )
        }
        if (extra > 0) {
            Box(
                modifier = Modifier
                    .offset(x = step * visible.size)
                    .size(size)
                    .border(2.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .background(Outline),
                contentAlignment = Alignment.Center,
            ) {
                Text("+$extra", color = InkMuted, fontSize = (size.value * 0.36f).sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
