package com.example

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// Bug state for interactive ladybugs & butterflies
data class Insect(
    val id: Int,
    val type: String, // "butterfly" or "ladybug"
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    var color: Color,
    var scale: Float = 1f,
    var angle: Float = 0f
)

@Composable
fun SummerBackground(
    mode: BrowserMode,
    modifier: Modifier = Modifier,
    lowBatteryMode: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    // Infinite transition for continuous smooth physical animations
    val infiniteTransition = rememberInfiniteTransition(label = "SummerLoop")

    // Animated angle for slower/faster rotation (120s full circle for sun as requested, but let's do 60s for visible feedback!)
    val sunAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 120000 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRotation"
    )

    // Drifting clouds offsets: Parallax 3 layers
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 40000 else 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift1"
    )

    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 70000 else 45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift2"
    )

    val cloudOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 110000 else 75000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift3"
    )

    // Poppies swaying swing factor
    val poppySwing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PoppySway"
    )

    // Holographic shifting gradient factor
    val holoBlink by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HoloBlink"
    )

    // Flag wave dynamic angle
    val flagWaveAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 4500 else 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FlagWaveAngle"
    )

    // Butterflies and ladybugs state
    val insects = remember {
        mutableStateListOf(
            Insect(1, "butterfly", 200f, 400f, 200f, 400f, Color(0xFFFFA500), angle = -15f),
            Insect(2, "butterfly", 600f, 600f, 600f, 600f, Color(0xFFFFB6C1), angle = 45f),
            Insect(3, "ladybug", 400f, 1500f, 400f, 1500f, Color(0xFFFF3333)),
            Insect(4, "ladybug", 800f, 1600f, 800f, 1600f, Color(0xFFFF1111))
        )
    }

    // Bug movement loop
    LaunchedEffect(key1 = lowBatteryMode) {
        while (true) {
            val tickDelay = if (lowBatteryMode) 5000L else 2500L
            kotlinx.coroutines.delay(tickDelay)
            // Gently glide the insects to new destinations
            for (bug in insects) {
                if (Random.nextFloat() > 0.4f) {
                    if (bug.type == "butterfly") {
                        // Butterflies hover around middle height
                        bug.targetX = Random.nextInt(100, 1000).toFloat()
                        bug.targetY = Random.nextInt(300, 1000).toFloat()
                    } else {
                        // Ladybugs walk on grass in bottom height
                        bug.targetX = Random.nextInt(100, 1000).toFloat()
                        bug.targetY = Random.nextInt(1300, 1700).toFloat()
                    }
                }
            }
        }
    }

    // Interactively move insects towards target
    val animatedInsects = insects.map { bug ->
        val x by animateFloatAsState(targetValue = bug.targetX, animationSpec = spring(dampingRatio = 0.85f, stiffness = 40f), label = "bugX")
        val y by animateFloatAsState(targetValue = bug.targetY, animationSpec = spring(dampingRatio = 0.85f, stiffness = 40f), label = "bugY")
        val scale by animateFloatAsState(targetValue = bug.scale, animationSpec = tween(300), label = "bugScale")
        bug.copy(x = x, y = y, scale = scale)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Check if clicked near any bug to startle it
                    for (i in insects.indices) {
                        val bug = insects[i]
                        val dist = kotlin.math.hypot(offset.x - bug.x, offset.y - bug.y)
                        if (dist < 150f) { // Within 150 pixels of tap
                            insects[i] = bug.copy(
                                scale = 1.6f,
                                targetX = Random.nextInt(50, 1000).toFloat(),
                                targetY = if (bug.type == "butterfly") Random.nextInt(200, 1000).toFloat() else Random.nextInt(1300, 1700).toFloat()
                            )
                            // Reset scale back in a short moment
                            val currentBugId = bug.id
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(600)
                                val index = insects.indexOfFirst { it.id == currentBugId }
                                if (index != -1) {
                                    insects[index] = insects[index].copy(scale = 1.0f)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            when (mode) {
                BrowserMode.STEALTH -> {
                    // System Stealth: green Matrix neon lines
                    drawRect(color = Color.Black)
                    val columns = (width / 50).toInt() + 1
                    for (col in 0 until columns) {
                        val x = col * 50f
                        val pathCount = 10
                        for (row in 0 until pathCount) {
                            val wave = sin((sunAngle / 10f) + col + row)
                            val y = (height / pathCount) * row + (wave * 120f)
                            drawCircle(
                                color = Color(0xFF00FF66).copy(alpha = (0.05f + 0.1f * kotlin.math.abs(sin(wave)))),
                                radius = 4f + 3f * wave,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
                BrowserMode.INCOGNITO -> {
                    // Incognito Mode: Dark beach, moonlight beach
                    val skyNight = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0A0F24), Color(0xFF1E295D)),
                        startY = 0f,
                        endY = height * 0.7f
                    )
                    drawRect(brush = skyNight)

                    // Sea water gradient at bottom
                    val seaNight = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D1E36), Color(0xFF040A12)),
                        startY = height * 0.7f,
                        endY = height
                    )
                    drawRect(
                        brush = seaNight,
                        topLeft = Offset(0f, height * 0.7f),
                        size = Size(width, height * 0.3f)
                    )

                    // Draw Moon instead of Sun
                    val moonX = width * 0.82f
                    val moonY = height * 0.18f
                    val moonRadius = width * 0.08f
                    drawCircle(
                        color = Color(0xFFFFFEE0),
                        radius = moonRadius,
                        center = Offset(moonX, moonY)
                    )
                    // Moon shadow overlay for sliver glow
                    drawCircle(
                        color = Color(0xFF0A0F24),
                        radius = moonRadius,
                        center = Offset(moonX - (moonRadius * 0.4f), moonY - (moonRadius * 0.1f))
                    )

                    // Moon shadow pathway (Lunar Path on water)
                    val pathWidth = width * 0.12f
                    val pathStart = height * 0.7f
                    val points = 30
                    for (i in 0 until points) {
                        val stepY = pathStart + (height * 0.3f / points) * i
                        val stepW = pathWidth * (1f + 0.12f * i)
                        val drift = sin((sunAngle / 8f) + i) * 15f
                        drawOval(
                            color = Color(0xFFFFFEE0).copy(alpha = (0.25f - (i * 0.005f))),
                            topLeft = Offset(moonX - stepW/2f + drift, stepY),
                            size = Size(stepW, 6f)
                        )
                    }

                    // Night realistic tree silhouettes along shoreline
                    drawRealisticTree(width * 0.15f, pathStart, 1.3f, true, flagWaveAngle)
                    drawRealisticTree(width * 0.32f, pathStart, 1.0f, true, flagWaveAngle)
                    drawRealisticTree(width * 0.68f, pathStart, 1.2f, true, flagWaveAngle)
                    drawRealisticTree(width * 0.85f, pathStart, 1.4f, true, flagWaveAngle)
                }
                BrowserMode.KIDS -> {
                    // Watercolor Playful Dolphins Background
                    val skyKids = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8F5E9), Color(0xFFE0F7FA)),
                        startY = 0f,
                        endY = height
                    )
                    drawRect(brush = skyKids)

                    // Draw beautiful stylized watercolor dolphins
                    drawKidsDolphins(width, height, sunAngle)
                }
                else -> {
                    // REGULAR / GUEST Modes: Summer Landscape + Russian Flag natural integration
                    // White (Drifting clouds) / Blue (Sky) / Red (Poppy field)
                    val skyDay = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4A90D9), Color(0xFF87CEEB)),
                        startY = 0f,
                        endY = height * 0.7f
                    )
                    drawRect(brush = skyDay)

                    // Rotating Sun with soft aura
                    val sunX = width * 0.85f
                    val sunY = height * 0.12f
                    val sunRadius = width * 0.07f
                    // Draw outer aura
                    drawCircle(
                        color = Color(0xFFFFFFB2).copy(alpha = 0.15f),
                        radius = sunRadius * 1.8f,
                        center = Offset(sunX, sunY)
                    )
                    // Draw spinning sun blades
                    rotate(sunAngle, pivot = Offset(sunX, sunY)) {
                        for (i in 0 until 8) {
                            rotate(45f * i, pivot = Offset(sunX, sunY)) {
                                drawLine(
                                    color = Color(0xFFFFF59D),
                                    start = Offset(sunX, sunY - sunRadius),
                                    end = Offset(sunX, sunY - (sunRadius * 2f)),
                                    strokeWidth = 6f
                                )
                            }
                        }
                    }
                    // Inner disk
                    drawCircle(
                        color = Color(0xFFFFEE58),
                        radius = sunRadius,
                        center = Offset(sunX, sunY)
                    )

                    // Drifting clouds (3 layers for parallax)
                    drawParallaxClouds(width, height, cloudOffset1, cloudOffset2, cloudOffset3)

                    // Tricolor organic flag holographic overlay (10% translucent) in the middle
                    drawHolographicFlag(width, height, holoBlink, flagWaveAngle)

                    // Poppies Field at the bottom (RED color of the flag paired with green meadow)
                    val grassTop = height * 0.73f
                    // Grass Base Gradient
                    val grassBase = Brush.verticalGradient(
                        colors = listOf(Color(0xFF81C784), Color(0xFF388E3C)),
                        startY = grassTop,
                        endY = height
                    )
                    drawRect(
                        brush = grassBase,
                        topLeft = Offset(0f, grassTop),
                        size = Size(width, height - grassTop)
                    )

                    // Draw a row of realistic forest trees on the grass horizon behind poppies
                    drawRealisticTree(width * 0.12f, grassTop, 1.4f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.28f, grassTop, 1.1f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.45f, grassTop, 1.5f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.72f, grassTop, 1.2f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.88f, grassTop, 1.6f, false, flagWaveAngle)

                    // Poppies drawing
                    val seed = 42
                    val random = Random(seed)
                    val poppiesCount = 45
                    for (i in 0 until poppiesCount) {
                        val px = random.nextFloat() * width
                        val py = grassTop + random.nextFloat() * (height - grassTop - 40f)
                        val pSize = 14f + random.nextFloat() * 12f

                        // Sway offset based on y pos and sway angle
                        val swayFactor = sin(poppySwing + (py / 100f)) * (10f + (height - py) * 0.02f)

                        // Stem
                        drawLine(
                            color = Color(0xFF2E7D32),
                            start = Offset(px + swayFactor, py),
                            end = Offset(px, py + pSize * 2f),
                            strokeWidth = 3f
                        )

                        // Red flower petals (poppies!)
                        drawCircle(
                            color = Color(0xFFE53935),
                            radius = pSize,
                            center = Offset(px + swayFactor, py)
                        )
                        // Dark flower core
                        drawCircle(
                            color = Color(0xFF212121),
                            radius = pSize * 0.35f,
                            center = Offset(px + swayFactor, py)
                        )
                        // Tiny highlight speck
                        drawCircle(
                            color = Color.White,
                            radius = pSize * 0.1f,
                            center = Offset(px + swayFactor + pSize * 0.1f, py - pSize * 0.1f)
                        )
                    }
                }
            }

            // Draw bugs interactively
            if (mode != BrowserMode.STEALTH && mode != BrowserMode.INCOGNITO) {
                drawInsects(animatedInsects)
            }
        }
    }
}

// Draw insects (butterflies / ladybugs) manually on current canvas
fun DrawScope.drawInsects(bugs: List<Insect>) {
    bugs.forEach { bug ->
        val x = bug.x
        val y = bug.y
        val scale = bug.scale

        if (bug.type == "butterfly") {
            // Draw butterfly using high-level DrawScope rotate wrapper
            rotate(bug.angle, pivot = Offset(x, y)) {
                // Body
                drawLine(
                    color = Color(0xFF3E2723),
                    start = Offset(x, y - 20f * scale),
                    end = Offset(x, y + 20f * scale),
                    strokeWidth = 4f * scale
                )

                // Right wing
                val wingPathRight = Path().apply {
                    moveTo(x, y)
                    cubicTo(
                        x + 35f * scale, y - 40f * scale,
                        x + 50f * scale, y - 10f * scale,
                        x, y + 5f * scale
                    )
                    cubicTo(
                        x + 40f * scale, y + 10f * scale,
                        x + 25f * scale, y + 30f * scale,
                        x, y + 5f * scale
                    )
                }
                drawPath(path = wingPathRight, color = bug.color)

                // Left wing
                val wingPathLeft = Path().apply {
                    moveTo(x, y)
                    cubicTo(
                        x - 35f * scale, y - 40f * scale,
                        x - 50f * scale, y - 10f * scale,
                        x, y + 5f * scale
                    )
                    cubicTo(
                        x - 40f * scale, y + 10f * scale,
                        x - 25f * scale, y + 30f * scale,
                        x, y + 5f * scale
                    )
                }
                drawPath(path = wingPathLeft, color = bug.color)

                // Shiny central spots
                drawCircle(color = Color.White, radius = 5f * scale, center = Offset(x + 20f * scale, y - 15f * scale))
                drawCircle(color = Color.White, radius = 5f * scale, center = Offset(x - 20f * scale, y - 15f * scale))
            }
        } else {
            // Ladybug using high-level DrawScope rotate wrapper
            rotate(bug.angle, pivot = Offset(x, y)) {
                // Red body
                drawCircle(color = bug.color, radius = 15f * scale, center = Offset(x, y))
                // Black head
                drawCircle(color = Color.Black, radius = 7f * scale, center = Offset(x, y - 12f * scale))

                // Center split line
                drawLine(
                    color = Color.Black,
                    start = Offset(x, y - 15f * scale),
                    end = Offset(x, y + 15f * scale),
                    strokeWidth = 2f * scale
                )

                // Spots
                drawCircle(color = Color.Black, radius = 2.5f * scale, center = Offset(x - 6f * scale, y - 4f * scale))
                drawCircle(color = Color.Black, radius = 2.5f * scale, center = Offset(x + 6f * scale, y - 4f * scale))
                drawCircle(color = Color.Black, radius = 3.5f * scale, center = Offset(x - 6f * scale, y + 5f * scale))
                drawCircle(color = Color.Black, radius = 3.5f * scale, center = Offset(x + 6f * scale, y + 5f * scale))
            }
        }
    }
}

// Drift 3 layers of clouds clouds
fun DrawScope.drawParallaxClouds(
    width: Float,
    height: Float,
    offset1: Float,
    offset2: Float,
    offset3: Float
) {
    // Cloud layer 1 (Far, small, slow)
    val cx1_a = width - (offset1 * (width + 300f))
    drawCloudShape(cx1_a, height * 0.25f, 0.7f)

    val cx1_b = width + 500f - (offset1 * (width + 300f))
    drawCloudShape(cx1_b, height * 0.4f, 0.6f)

    // Cloud layer 2 (Medium)
    val cx2_a = width - (offset2 * (width + 400f)) + 150f
    drawCloudShape(cx2_a, height * 0.32f, 1.1f)

    // Cloud layer 3 (Near, large, faster)
    val cx3_a = width - (offset3 * (width + 600f)) + 300f
    drawCloudShape(cx3_a, height * 0.18f, 1.6f)
}

fun DrawScope.drawCloudShape(x: Float, y: Float, scale: Float) {
    val bColor = Color.White.copy(alpha = 0.85f)
    drawCircle(color = bColor, radius = 30f * scale, center = Offset(x, y))
    drawCircle(color = bColor, radius = 42f * scale, center = Offset(x + 30f * scale, y - 10f * scale))
    drawCircle(color = bColor, radius = 36f * scale, center = Offset(x + 60f * scale, y))
    drawCircle(color = bColor, radius = 25f * scale, center = Offset(x - 30f * scale, y + 5f * scale))
}

fun DrawScope.drawHolographicFlag(width: Float, height: Float, holoAlpha: Float, waveAngle: Float) {
    val blockH = height * 0.11f
    val startY = height * 0.35f
    
    // Complex 3D mesh physics approximation for flag tissue under summer wind
    fun getPhysicsWaveY(x: Float, rowOffset: Float): Float {
        // Double periodic waving function simulating cloth tension
        val primaryWave = sin(x * 0.0045f - waveAngle) * 32f
        val secondaryWrinkle = sin(x * 0.02f - waveAngle * 2.3f) * 6f * (x / width) // wrinkles grow further from flagpole
        val verticalSlump = (1f - kotlin.math.cos(x / width * Math.PI.toFloat() * 0.5f)) * 14f // gravity slump
        return startY + primaryWave + secondaryWrinkle + verticalSlump + rowOffset
    }
    
    // Normal calculation for shade mapping (fabric folds shading)
    // Derivative of the wave function gives a gradient used for lighting
    fun getShadingFactor(x: Float): Float {
        val slope = kotlin.math.cos(x * 0.0045f - waveAngle) * 0.15f + kotlin.math.cos(x * 0.02f - waveAngle * 2.3f) * 0.1f
        return (0.85f + 0.3f * slope).coerceIn(0.5f, 1.3f)
    }

    fun drawPhysFlagStrip(startRow: Float, endRow: Float, brushList: List<Color>, alpha: Float) {
        val steps = 40
        val stepW = width / steps
        val path = Path()
        
        // Move to start
        path.moveTo(0f, getPhysicsWaveY(0f, startRow))
        for (i in 1..steps) {
            val x = i * stepW
            path.lineTo(x, getPhysicsWaveY(x, startRow))
        }
        path.lineTo(width, getPhysicsWaveY(width, endRow))
        for (i in steps downTo 0) {
            val x = i * stepW
            path.lineTo(x, getPhysicsWaveY(x, endRow))
        }
        path.close()

        // Create gradient shade with light reflection (fabric wrinkles shadowing)
        val shaderBrush = Brush.horizontalGradient(
            0f to brushList[0].copy(alpha = alpha),
            width * 0.25f to brushList[1].copy(alpha = alpha),
            width * 0.55f to brushList[0].copy(alpha = alpha),
            width * 0.85f to brushList[1].copy(alpha = alpha),
            width to brushList[0].copy(alpha = alpha)
        )
        drawPath(path, shaderBrush)
        
        // Draw fine vertical shades using individual segment paths for authentic physical depth
        for (i in 0 until steps) {
            val xL = i * stepW
            val xR = (i + 1) * stepW
            val shade = getShadingFactor(xL + stepW / 2)
            val shadeColor = if (shade > 1.0f) {
                Color.White.copy(alpha = (shade - 1.0f) * 0.4f * alpha)
            } else {
                Color.Black.copy(alpha = (1.0f - shade) * 0.45f * alpha)
            }
            
            val shadowPath = Path().apply {
                moveTo(xL, getPhysicsWaveY(xL, startRow))
                lineTo(xR, getPhysicsWaveY(xR, startRow))
                lineTo(xR, getPhysicsWaveY(xR, endRow))
                lineTo(xL, getPhysicsWaveY(xL, endRow))
                close()
            }
            drawPath(shadowPath, Brush.verticalGradient(listOf(shadeColor, Color.Transparent, shadeColor)))
        }
    }

    val alphaCoeff = holoAlpha * 1.5f
    
    // Russian Tricolor strips
    drawPhysFlagStrip(0f, blockH, listOf(Color.White, Color(0xFFF1F5F9)), alphaCoeff)
    drawPhysFlagStrip(blockH, blockH * 2, listOf(Color(0xFF0052B4), Color(0xFF1E88E5)), alphaCoeff)
    drawPhysFlagStrip(blockH * 2, blockH * 3, listOf(Color(0xFFD32F2F), Color(0xFFE53935)), alphaCoeff)
}

fun DrawScope.drawRealisticTree(x: Float, y: Float, scale: Float, isNight: Boolean, swayAngle: Float) {
    // Math sway values representing wind action on trunk vs crown
    val trunkSway = sin(swayAngle) * 2f * scale
    val branchSway = sin(swayAngle * 1.5f) * 6f * scale
    val leavesSwayX = sin(swayAngle * 1.2f) * 12f * scale
    val leavesSwayY = kotlin.math.cos(swayAngle * 0.8f) * 4f * scale

    val trunkWidth = 14f * scale
    val trunkHeight = 65f * scale
    val trunkBrush = if (isNight) {
        Brush.verticalGradient(listOf(Color(0xFF0F1E36), Color(0xFF020617)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF3E2723)))
    }
    
    // Draw trunk with subtle organic branch geometry that sways slightly
    val trunkPath = Path().apply {
        moveTo(x - trunkWidth/2f, y)
        lineTo(x - trunkWidth * 0.3f + trunkSway, y - trunkHeight)
        lineTo(x - trunkWidth * 0.8f + branchSway - 10f * scale, y - trunkHeight - 20f * scale)
        lineTo(x - trunkWidth * 0.4f + branchSway, y - trunkHeight - 22f * scale)
        lineTo(x + trunkSway, y - trunkHeight - 8f * scale)
        lineTo(x + trunkWidth * 0.5f + branchSway + 10f * scale, y - trunkHeight - 25f * scale)
        lineTo(x + trunkWidth * 0.8f + branchSway + 18f * scale, y - trunkHeight - 23f * scale)
        lineTo(x + trunkWidth/2f, y)
        close()
    }
    drawPath(trunkPath, trunkBrush)
    
    // Foliage Group palette
    val foliageColors = if (isNight) {
        listOf(
            Color(0xFF1E293B).copy(alpha = 0.85f),
            Color(0xFF0F172A).copy(alpha = 0.95f),
            Color(0xFF334155).copy(alpha = 0.75f),
            Color(0xFF1E293B).copy(alpha = 0.9f)
        )
    } else {
        listOf(
            Color(0xFF2E7D32).copy(alpha = 0.96f), // Mid-green
            Color(0xFF1B5E20).copy(alpha = 0.98f), // Dark shadow green
            Color(0xFF4CAF50).copy(alpha = 0.95f), // Rich green
            Color(0xFF81C784).copy(alpha = 0.9f),  // Highlight light green
            Color(0xFFA5D6A7).copy(alpha = 0.82f)  // Warm solar top leaves
        )
    }
    
    val cCenterY = y - trunkHeight - 12f * scale

    // Procedural Leafy Clusters: Draw multiple layered circles representing dense foliage.
    // Animating circle positions and radii to mock leaf-movement & wind friction.
    val layers = listOf(
        // (X-offset, Y-offset, BaseRadius, ColorIndex, SwayPhaseShift)
        Triple(-22f * scale, -10f * scale, 30f * scale),
        Triple(22f * scale, -10f * scale, 30f * scale),
        Triple(0f * scale, -26f * scale, 36f * scale),
        Triple(-18f * scale, 0f, 28f * scale),
        Triple(18f * scale, 0f, 28f * scale)
    )

    layers.forEachIndexed { index, (oX, oY, r) ->
        val phase = index * 0.45f
        val indSwayX = leavesSwayX + sin(swayAngle * 1.4f + phase) * 4f * scale
        val indSwayY = leavesSwayY + kotlin.math.cos(swayAngle * 1.1f + phase) * 2f * scale
        
        // Shader-like micro-gradients representing daylight diffusion inside foliage
        val foliageBrush = Brush.radialGradient(
            colors = listOf(foliageColors[index % foliageColors.size], foliageColors[(index + 1) % foliageColors.size]),
            center = Offset(x + oX + indSwayX - r*0.3f, cCenterY + oY + indSwayY - r*0.3f),
            radius = r * 1.2f
        )
        drawCircle(
            brush = foliageBrush,
            radius = r + sin(swayAngle * 2.5f + phase) * 1.5f * scale, // breathing leaves effect
            center = Offset(x + oX + indSwayX, cCenterY + oY + indSwayY)
        )
    }

    // Top highlights/shimmering wind leaves (Shader-foliage overlay layer)
    if (!isNight && foliageColors.size > 4) {
        val microLeaves = listOf(
            Offset(-12f * scale, -32f * scale) to 18f * scale,
            Offset(12f * scale, -22f * scale) to 15f * scale,
            Offset(0f, -42f * scale) to 22f * scale,
            Offset(-28f * scale, -18f * scale) to 14f * scale,
            Offset(28f * scale, -18f * scale) to 14f * scale
        )
        microLeaves.forEachIndexed { i, (pos, r) ->
            val phase = i * 0.73f
            val leafX = x + pos.x + leavesSwayX + sin(swayAngle * 1.8f + phase) * 6f * scale
            val leafY = cCenterY + pos.y + leavesSwayY + kotlin.math.cos(swayAngle * 1.3f + phase) * 3f * scale
            
            // Draw a shiny warm leaf highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(foliageColors[4], foliageColors[3]),
                    center = Offset(leafX - r*0.4f, leafY - r*0.4f),
                    radius = r
                ),
                radius = r,
                center = Offset(leafX, leafY)
            )

            // Draw procedural details inside (shimmering leaf dots mimicking realistic sunlit foliage shader)
            val shimmerCount = 5
            for (j in 0 until shimmerCount) {
                val dx = sin(swayAngle * 3f + i + j) * (r * 0.4f)
                val dy = kotlin.math.cos(swayAngle * 2.1f + i + j) * (r * 0.4f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f + 0.15f * sin(swayAngle * 4f + i + j)),
                    radius = 2f * scale,
                    center = Offset(leafX + dx, leafY + dy)
                )
            }
        }
    }
}

fun DrawScope.drawKidsDolphins(width: Float, height: Float, tick: Float) {
    val dColor = Color(0xFF0288D1).copy(alpha = 0.2f)
    for (i in 0 until 3) {
        val delta = i * 400f
        val dx = (width * 0.2f + delta + sin(tick / 15f + i) * 60f) % (width + 200f) - 100f
        val dy = height * 0.5f + sin(tick / 10f + i) * 80f

        // Draw simple watercolor-like semi-translucent circle representations of leaping dolphin bodies
        drawCircle(color = dColor, radius = 60f, center = Offset(dx, dy))
        drawCircle(color = dColor, radius = 40f, center = Offset(dx + 50f, dy - 20f))
        drawCircle(color = dColor, radius = 25f, center = Offset(dx - 50f, dy + 15f))

        // tail fin
        val tPath = Path().apply {
            moveTo(dx - 50f, dy + 15f)
            lineTo(dx - 80f, dy)
            lineTo(dx - 75f, dy + 35f)
            close()
        }
        drawPath(tPath, dColor)
    }
}
