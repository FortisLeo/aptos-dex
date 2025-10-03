package com.rishitgoklani.morero.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Preview
@Composable
fun orbPreview() {
}

// Fibonacci sphere helper
private fun generateFibonacciSphere(count: Int): List<Triple<Float, Float, Float>> {
    val pts = ArrayList<Triple<Float, Float, Float>>(count)
    val golden = (Math.PI * (3 - kotlin.math.sqrt(5.0))).toFloat()
    for (i in 0 until count) {
        val y = 1f - (i.toFloat() / (count - 1)) * 2f
        val r = kotlin.math.sqrt(1f - y * y)
        val theta = i * golden
        pts += Triple(kotlin.math.cos(theta) * r, y, kotlin.math.sin(theta) * r)
    }
    return pts
}

@Composable
fun SphericalParticleOrb(
    modifier: Modifier = Modifier,
    particleCount: Int = 2000,
    sphereRadius: Dp = 120.dp,
    dotColor: Color = Color.White,
    rotationDurationMs: Int = (10000f / 0.6f).toInt(),
    focalLength: Float = 400f,
    onClick: () -> Unit
) {
    val radiusPx = with(LocalDensity.current) { sphereRadius.toPx() }
    var points3D by remember { mutableStateOf(generateFibonacciSphere(particleCount)) }
    val infinite = rememberInfiniteTransition(label = "orb_rotate")
    val rotationDeg by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(
        modifier = modifier
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val ang = Math.toRadians(rotationDeg.toDouble())
            val cosA = cos(ang).toFloat()
            val sinA = sin(ang).toFloat()

            points3D.forEach { (x0, y0, z0) ->
                val x1 = x0 * cosA + z0 * sinA
                val z1 = -x0 * sinA + z0 * cosA
                val y1 = y0
                val sx = x1 * radiusPx
                val sy = y1 * radiusPx
                val sz = z1 * radiusPx
                val scale = focalLength / (focalLength - sz).coerceAtLeast(0.1f)
                val px = cx + sx * scale
                val py = cy - sy * scale
                val ds = 2f * scale
                drawCircle(dotColor, radius = ds, center = Offset(px, py))
            }
        }
    }
}


