package com.rishitgoklani.aptosdex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

data class OrbitingToken(
    val icon: @Composable () -> Unit,
    val orbitRadius: Float,
    val speed: Float = 1f,
    val startAngle: Float = 0f,
    val iconSize: Float = 48f
)

@Composable
fun OrbitalAnimation(
    centerContent: @Composable () -> Unit,
    orbitingTokens: List<OrbitingToken>,
    modifier: Modifier = Modifier,
    orbitColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbital")

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw orbit rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)

            orbitingTokens.forEach { token ->
                // Two-part fade using sweep gradient: bright arcs with soft fades on opposite sides
                val base = orbitColor
                val transparent = base.copy(alpha = 0f)
                val half = base.copy(alpha = base.alpha * 0.5f)

                val brush = Brush.sweepGradient(
                    colors = listOf(
                        transparent, half, base, half, transparent, // first arc fade-in/out
                        transparent, half, base, half, transparent   // second arc opposite side
                    ),
                    center = center
                )

                drawCircle(
                    brush = brush,
                    radius = token.orbitRadius,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Center logo
        centerContent()

        // Orbiting tokens
        orbitingTokens.forEach { token ->
            val angle by infiniteTransition.animateFloat(
                initialValue = token.startAngle,
                targetValue = token.startAngle + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (10000 / token.speed).toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "orbit_angle"
            )

            OrbitingItem(
                angle = angle,
                radius = token.orbitRadius,
                iconSize = token.iconSize,
                content = token.icon
            )
        }
    }
}

@Composable
private fun OrbitingItem(
    angle: Float,
    radius: Float,
    iconSize: Float,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val centerX = constraints.maxWidth / 2f
        val centerY = constraints.maxHeight / 2f

        val angleRad = Math.toRadians(angle.toDouble())
        // Calculate position on the ring, then offset by half icon size to center it on the ring
        val x = centerX + radius * cos(angleRad).toFloat() - (iconSize / 2f)
        val y = centerY + radius * sin(angleRad).toFloat() - (iconSize / 2f)

        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { (x - centerX).toDp() },
                    y = with(density) { (y - centerY).toDp() }
                )
        ) {
            content()
        }
    }
}
