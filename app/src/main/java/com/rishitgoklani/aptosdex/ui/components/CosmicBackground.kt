package com.rishitgoklani.aptosdex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import com.rishitgoklani.aptosdex.ui.theme.AptosBlue
import com.rishitgoklani.aptosdex.ui.theme.SoftPurple
import com.rishitgoklani.aptosdex.ui.theme.WarningAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Aptos Galactic Flow - A vast, slowly swirling cosmic vista
 * Featuring multi-layer parallax stars, galactic swirls, and dust lanes
 */
@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "galactic_flow")
    
    // Very slow galactic rotation - 5 minutes per full rotation
    val galaxyRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(300_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "galaxy_rotation"
    )
    
    // Gentle pulsing luminosity - 12 second cycle
    val luminosityPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(12_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "luminosity"
    )
    
    // Parallax star layer drifts (slowest to fastest)
    val farStarDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "far_drift"
    )
    
    val midStarDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(120_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "mid_drift"
    )
    
    val nearStarDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "near_drift"
    )
    
    // Secondary swirl for depth
    val secondarySwirl by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(240_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "secondary_swirl"
    )
    
    val aptosBlue = AptosBlue
    val softPurple = SoftPurple
    val amber = WarningAmber
    val deepVoid = MaterialTheme.colorScheme.background
    
    // Cache star fields based on current canvas size
    var lastSize by remember { mutableStateOf(Size.Zero) }
    var farStars by remember { mutableStateOf<List<Star>>(emptyList()) }
    var midStars by remember { mutableStateOf<List<Star>>(emptyList()) }
    var nearStars by remember { mutableStateOf<List<Star>>(emptyList()) }

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            val maxRadius = width.coerceAtLeast(height) * 1.2f
            
            // === LAYER 1: DEEP SPACE BASE ===
            drawRect(color = deepVoid)
            
            // Rebuild star fields if canvas size changed
            if (lastSize != size) {
                farStars = generateStarField(600, 12345, width, height)
                midStars = generateStarField(380, 54321, width, height)
                nearStars = generateStarField(180, 98765, width, height)
                lastSize = size
            }

            // === LAYER 2: FAR STAR FIELD (Distant, faint, slow) ===
            farStars.forEach { star ->
                val driftedX = ((star.x + width * farStarDrift * 0.05f) % width)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0E0E0).copy(alpha = star.alpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(driftedX, star.y),
                        radius = star.size * 1.5f
                    ),
                    radius = star.size * 0.7f,
                    center = Offset(driftedX, star.y)
                )
            }
            

            

            // === LAYER 4: MID STAR FIELD (Medium distance, moderate brightness) ===
            midStars.forEach { star ->
                val driftedX = ((star.x + width * midStarDrift * 0.12f) % width)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0E0E0).copy(alpha = star.alpha * 0.65f),
                            Color.Transparent
                        ),
                        center = Offset(driftedX, star.y),
                        radius = star.size * 2f
                    ),
                    radius = star.size * 1.1f,
                    center = Offset(driftedX, star.y)
                )
            }
            
            // Remove big spiral arms and secondary glow to keep it minimal and dark
            
            // === LAYER 6: COSMIC DUST LANES (Dark wisps for depth) ===
            rotate(degrees = galaxyRotation * 0.3f, pivot = Offset(width * 0.5f, height * 0.5f)) {
                val dustColor = deepVoid.copy(alpha = 0.4f)
                
                // Dust lane 1
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, dustColor, dustColor, Color.Transparent),
                        start = Offset(0f, height * 0.3f),
                        end = Offset(width, height * 0.3f)
                    ),
                    topLeft = Offset(0f, height * 0.25f),
                    size = androidx.compose.ui.geometry.Size(width, height * 0.12f)
                )
                
                // Dust lane 2
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, dustColor, Color.Transparent),
                        start = Offset(0f, height * 0.65f),
                        end = Offset(width, height * 0.65f)
                    ),
                    topLeft = Offset(0f, height * 0.6f),
                    size = androidx.compose.ui.geometry.Size(width, height * 0.15f)
                )
            }
            
            // (Removed secondary glow swirl)
            
            // === LAYER 8: NEAR STAR FIELD (Closest, brightest, fastest) ===
            nearStars.forEach { star ->
                val driftedX = ((star.x + width * nearStarDrift * 0.25f) % width)
                val shimmer = 0.85f + (sin((nearStarDrift * PI * 2f + star.x).toFloat()) * 0.15f).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = star.alpha * shimmer),
                            Color(0xFFE0E0E0).copy(alpha = star.alpha * 0.6f * shimmer),
                            Color.Transparent
                        ),
                        center = Offset(driftedX, star.y),
                        radius = star.size * 3f
                    ),
                    radius = star.size * 1.4f,
                    center = Offset(driftedX, star.y)
                )
            }
        }
    }
}

private data class Star(val x: Float, val y: Float, val size: Float, val alpha: Float)

private fun generateStarField(count: Int, seed: Int, width: Float, height: Float): List<Star> {
    val random = Random(seed)
    return List(count) {
        Star(
            x = random.nextFloat() * width,
            y = random.nextFloat() * height,
            size = (0.5f + random.nextFloat() * 2f).dp.value,
            alpha = 0.3f + random.nextFloat() * 0.7f
        )
    }
}
