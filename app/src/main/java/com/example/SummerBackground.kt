package com.example

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Insect representation with flying state
data class Insect(
    val id: Int,
    val type: String, // "butterfly" or "ladybug"
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    var color: Color,
    var scale: Float = 1f,
    var angle: Float = 0f,
    var isFlying: Boolean = false
)

enum class TimeOfDay { MORNING, DAY, EVENING, NIGHT }

@Composable
fun SummerBackground(
    mode: BrowserMode,
    modifier: Modifier = Modifier,
    lowBatteryMode: Boolean = false,
    isAnimEnabled: Boolean = true,
    flagSpeedMultiplier: Float = 1.0f
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "SummerLoop")

    // Retrieve local system hour to determine the real time of day
    val systemHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val timeOfDay = remember(systemHour) {
        when (systemHour) {
            in 5..10 -> TimeOfDay.MORNING
            in 11..17 -> TimeOfDay.DAY
            in 18..21 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }

    // Canvas sizes stored on draw phase
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Gyroscope / Tilt simulation and integration
    var gyroX by remember { mutableStateOf(0f) }
    var gyroY by remember { mutableStateOf(0f) }

    DisposableEffect(isAnimEnabled) {
        if (!isAnimEnabled) {
            onDispose {}
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == android.hardware.Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
                    gyroX = Math.toDegrees(orientation[2].toDouble()).toFloat() // Roll
                    gyroY = Math.toDegrees(orientation[1].toDouble()).toFloat() // Pitch
                } else if (event.sensor.type == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                    gyroX = gyroX * 0.85f + event.values[0] * 0.15f * 15f
                    gyroY = gyroY * 0.85f + event.values[1] * 0.15f * 15f
                }
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // 1. Sun & Moon rotational and interactive speed
    val baseRotationSpec = if (isAnimEnabled) {
        infiniteRepeatable<Float>(
            animation = tween(if (lowBatteryMode) 100000 else 50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    } else {
        infiniteRepeatable<Float>(
            animation = tween(100000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    }
    val sunAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = baseRotationSpec,
        label = "SunRotation"
    )

    // Interactive sun flash state
    var sunTapTrigger by remember { mutableStateOf(false) }
    val sunFlashAnim by animateFloatAsState(
        targetValue = if (sunTapTrigger) 1f else 0f,
        animationSpec = if (sunTapTrigger) tween(600, easing = EaseOutCubic) else snap(),
        label = "SunFlash"
    )
    LaunchedEffect(sunTapTrigger) {
        if (sunTapTrigger) {
            delay(600)
            sunTapTrigger = false
        }
    }

    // 2. Interactive clouds push offset state
    var cloudPushState by remember { mutableStateOf(0f) }
    val cloudPushAnim by animateFloatAsState(
        targetValue = cloudPushState,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "CloudPushAnim"
    )
    LaunchedEffect(cloudPushState) {
        if (cloudPushState > 0f) {
            delay(1500)
            cloudPushState = 0f
        }
    }

    // Clouddrifts infinite continuous loops
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 60000 else 35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift1"
    )
    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 90000 else 55000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift2"
    )
    val cloudOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (lowBatteryMode) 130000 else 85000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift3"
    )

    // 3. Poppies and meadow sway physics loop
    val poppySwayLoop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAnimEnabled) 3600 else 10000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PoppySway"
    )

    // Interactive bent coordinates
    var touchedPoppyX by remember { mutableStateOf(-1f) }
    var touchedPoppyY by remember { mutableStateOf(-1f) }
    val poppyBendAnim by animateFloatAsState(
        targetValue = if (touchedPoppyX >= 0f) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "PoppyBend"
    )
    LaunchedEffect(touchedPoppyX) {
        if (touchedPoppyX >= 0f) {
            delay(1200)
            touchedPoppyX = -1f
        }
    }

    // 4. Interactive frog leap state
    var frogJumpTrigger by remember { mutableStateOf(false) }
    val frogJumpAnim by animateFloatAsState(
        targetValue = if (frogJumpTrigger) 1f else 0f,
        animationSpec = if (frogJumpTrigger) tween(800, easing = EaseOutQuad) else snap(),
        label = "FrogJump"
    )
    LaunchedEffect(frogJumpTrigger) {
        if (frogJumpTrigger) {
            delay(800)
            frogJumpTrigger = false
        }
    }

    // 5. V—formation slowly flying birds crossing the screen
    val birdsOffset by infiniteTransition.animateFloat(
        initialValue = -350f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAnimEnabled) 32000 else 10000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BirdsPosition"
    )

    // Insect entities
    val insects = remember {
        mutableStateListOf(
            Insect(1, "butterfly", 240f, 480f, 240f, 480f, Color(0xFFFFA500), angle = -20f),
            Insect(2, "butterfly", 700f, 650f, 700f, 650f, Color(0xFFFF69B4), angle = 30f),
            Insect(3, "ladybug", 360f, 1420f, 360f, 1420f, Color(0xFFFF3333)),
            Insect(4, "ladybug", 820f, 1550f, 820f, 1550f, Color(0xFFFF1111))
        )
    }

    // Continuous insect wander loop
    LaunchedEffect(key1 = lowBatteryMode, key2 = isAnimEnabled) {
        if (!isAnimEnabled) return@LaunchedEffect
        while (true) {
            val tickDelay = if (lowBatteryMode) 6000L else 3000L
            delay(tickDelay)
            for (bug in insects) {
                if (!bug.isFlying && Random.nextFloat() > 0.35f) {
                    if (bug.type == "butterfly") {
                        bug.targetX = Random.nextInt(120, 950).toFloat()
                        bug.targetY = Random.nextInt(320, 1050).toFloat()
                    } else {
                        // Ladybugs crawl on grass pasture heights
                        bug.targetX = Random.nextInt(150, 900).toFloat()
                        bug.targetY = Random.nextInt(1300, 1650).toFloat()
                    }
                }
            }
        }
    }

    // Smooth movement mapping
    val animatedInsects = insects.map { bug ->
        val x by animateFloatAsState(targetValue = bug.targetX, animationSpec = spring(dampingRatio = 0.8f, stiffness = 42f), label = "insectX")
        val y by animateFloatAsState(targetValue = bug.targetY, animationSpec = spring(dampingRatio = 0.8f, stiffness = 42f), label = "insectY")
        val scale by animateFloatAsState(targetValue = bug.scale, animationSpec = tween(400), label = "insectScale")
        bug.copy(x = x, y = y, scale = scale)
    }

    // Holographic blinking wave factors
    val holoBlink by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAnimEnabled) 4200 else 10000000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HoloGlow"
    )
    val flagWaveAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                if (isAnimEnabled) {
                    val baseSpeed = if (lowBatteryMode) 4500 else 2500
                    (baseSpeed / maxOf(0.1f, flagSpeedMultiplier)).toInt()
                } else 100000000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "FlagWave"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(canvasSize) {
                detectTapGestures { offset ->
                    val w = canvasSize.width
                    val h = canvasSize.height
                    if (w <= 0f || h <= 0f) return@detectTapGestures

                    // 1. Check if Sun / Moon is tapped (around 0.85w, 0.12h)
                    val sunX = w * 0.85f
                    val sunY = h * 0.12f
                    val distToSun = kotlin.math.hypot(offset.x - sunX, offset.y - sunY)
                    if (distToSun < 130f) {
                        sunTapTrigger = true
                    }

                    // 2. Check if clouds are tapped (upper sky region)
                    if (offset.y < h * 0.6f) {
                        cloudPushState = 120f
                    }

                    // 3. Check if we tapped in the pasture fields close to poppies
                    if (offset.y >= h * 0.70f) {
                        touchedPoppyX = offset.x
                        touchedPoppyY = offset.y
                    }

                    // 4. Check if Pond is tapped (bottom lake region)
                    val pondLeft = w * 0.45f
                    val pondTop = h * 0.84f
                    val pondWidth = w * 0.5f
                    val pondHeight = h * 0.13f
                    if (offset.x in pondLeft..(pondLeft + pondWidth) && offset.y in pondTop..(pondTop + pondHeight)) {
                        frogJumpTrigger = true
                    }

                    // 5. Startle insects
                    for (i in insects.indices) {
                        val bug = insects[i]
                        val dist = kotlin.math.hypot(offset.x - bug.x, offset.y - bug.y)
                        if (dist < 140f) {
                            if (bug.type == "ladybug" && !bug.isFlying) {
                                // Ladybug spreads wings and takes flight!
                                insects[i] = bug.copy(
                                    isFlying = true,
                                    scale = 1.7f,
                                    targetX = Random.nextInt(100, 900).toFloat(),
                                    targetY = Random.nextInt(200, 750).toFloat()
                                )
                                coroutineScope.launch {
                                    delay(4000)
                                    val idx = insects.indexOfFirst { it.id == bug.id }
                                    if (idx != -1) {
                                        insects[idx] = insects[idx].copy(
                                            isFlying = false,
                                            scale = 1.0f,
                                            targetX = Random.nextInt(150, 850).toFloat(),
                                            targetY = Random.nextInt(1350, 1650).toFloat()
                                        )
                                    }
                                }
                            } else {
                                // Butterly startled
                                insects[i] = bug.copy(
                                    scale = 1.6f,
                                    targetX = Random.nextInt(50, 950).toFloat(),
                                    targetY = Random.nextInt(300, 1000).toFloat()
                                )
                                coroutineScope.launch {
                                    delay(800)
                                    val idx = insects.indexOfFirst { it.id == bug.id }
                                    if (idx != -1) {
                                        insects[idx] = insects[idx].copy(scale = 1.0f)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            if (canvasSize != size) {
                canvasSize = size
            }

            when (mode) {
                BrowserMode.STEALTH -> {
                    // Futuristic cyber matrix grid
                    drawRect(color = Color.Black)
                    val cols = (width / 45).toInt() + 1
                    for (c in 0 until cols) {
                        val x = c * 45f
                        val rows = 12
                        for (r in 0 until rows) {
                            val pulse = sin((sunAngle / 8f) + c + r)
                            val y = (height / rows) * r + (pulse * 90f)
                            drawCircle(
                                color = Color(0xFF00FF66).copy(alpha = 0.04f + 0.12f * kotlin.math.abs(pulse)),
                                radius = 3.5f + 2.5f * pulse,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                BrowserMode.INCOGNITO -> {
                    // Luna incognito night beach
                    val skyNight = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0E0B1E), Color(0xFF1E264F)),
                        startY = 0f,
                        endY = height * 0.72f
                    )
                    drawRect(brush = skyNight)

                    val seaNight = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C192E), Color(0xFF030713)),
                        startY = height * 0.72f,
                        endY = height
                    )
                    drawRect(
                        brush = seaNight,
                        topLeft = Offset(0f, height * 0.72f),
                        size = Size(width, height * 0.28f)
                    )

                    // Midnight crescent Moon
                    val moonX = width * 0.85f
                    val moonY = height * 0.12f
                    val moonRadius = width * 0.08f
                    drawCircle(
                        color = Color(0xFFFEFCE8),
                        radius = moonRadius,
                        center = Offset(moonX, moonY)
                    )
                    drawCircle(
                        color = Color(0xFF0E0B1E),
                        radius = moonRadius,
                        center = Offset(moonX - (moonRadius * 0.42f), moonY - (moonRadius * 0.08f))
                    )

                    // Moon path reflection in water
                    val reflectorW = width * 0.14f
                    val startY = height * 0.72f
                    val loops = 25
                    for (l in 0 until loops) {
                        val stepY = startY + (height * 0.28f / loops) * l
                        val stepW = reflectorW * (1f + 0.14f * l)
                        val drift = sin((sunAngle / 10f) + l) * 12f
                        drawOval(
                            color = Color(0xFFFEFCE8).copy(alpha = (0.22f - (l * 0.005f))),
                            topLeft = Offset(moonX - stepW / 2f + drift, stepY),
                            size = Size(stepW, 5.5f)
                        )
                    }

                    // Trees rows on shoreline
                    drawRealisticTree(width * 0.14f, startY, 1.3f, true, flagWaveAngle)
                    drawRealisticTree(width * 0.35f, startY, 1.0f, true, flagWaveAngle)
                    drawRealisticTree(width * 0.65f, startY, 1.2f, true, flagWaveAngle)
                    drawRealisticTree(width * 0.86f, startY, 1.35f, true, flagWaveAngle)
                }

                BrowserMode.KIDS -> {
                    // Playful turquoise green kids friendly sky
                    val skyKids = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0F2FE), Color(0xFFCCFBF1)),
                        startY = 0f,
                        endY = height
                    )
                    drawRect(brush = skyKids)
                    drawKidsDolphins(width, height, sunAngle)
                }

                else -> {
                    // REGULAR / GUEST modes with optimized beautiful real-time Clock Gradients & Summer breeze physics
                    val skyGradients = when (timeOfDay) {
                        TimeOfDay.MORNING -> Brush.verticalGradient(
                            colors = listOf(Color(0xFFF43F5E), Color(0xFFFB923C), Color(0xFFFEF08A)), // Sunrise coral peach gold
                            startY = 0f,
                            endY = height * 0.75f
                        )
                        TimeOfDay.DAY -> Brush.verticalGradient(
                            colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFCCFBF1)), // Summer vibrant sky lagoon
                            startY = 0f,
                            endY = height * 0.75f
                        )
                        TimeOfDay.EVENING -> Brush.verticalGradient(
                            colors = listOf(Color(0xFF6B21A8), Color(0xFFBE185D), Color(0xFFF97316)), // Dusk pink sunset
                            startY = 0f,
                            endY = height * 0.75f
                        )
                        TimeOfDay.NIGHT -> Brush.verticalGradient(
                            colors = listOf(Color(0xFF030712), Color(0xFF1E1B4B), Color(0xFF312E81)), // Dark indigo midnight pasture
                            startY = 0f,
                            endY = height * 0.75f
                        )
                    }
                    drawRect(brush = skyGradients)

                    // Sun/Moon celestial rendering with smooth responsive animations
                    val sunX = width * 0.85f
                    val sunY = height * 0.12f
                    val sunRadius = width * 0.07f

                    if (timeOfDay != TimeOfDay.NIGHT) {
                        // Drawing SUN
                        val scaleFactor = 1f + 0.35f * sunFlashAnim
                        val sunAura = sunRadius * (1.9f * scaleFactor)
                        val sunAuraAlpha = 0.25f * (1f - sunFlashAnim * 0.7f)

                        // 1. Soft corona aura
                        drawCircle(
                            color = Color(0xFFFCD34D).copy(alpha = sunAuraAlpha),
                            radius = sunAura,
                            center = Offset(sunX, sunY)
                        )

                        // 2. Beams (rotating and length pulsing)
                        rotate(sunAngle, pivot = Offset(sunX, sunY)) {
                            for (b in 0 until 12) {
                                rotate(30f * b, pivot = Offset(sunX, sunY)) {
                                    val pulseLen = sunRadius * (2.2f + 0.3f * sin(poppySwayLoop * 3f + b))
                                    drawLine(
                                        color = Color(0xFFFDE047).copy(alpha = 0.55f + 0.15f * sin(poppySwayLoop * 2f)),
                                        start = Offset(sunX, sunY - sunRadius),
                                        end = Offset(sunX, sunY - pulseLen),
                                        strokeWidth = 4.5f
                                    )
                                }
                            }
                        }

                        // 3. Central disk
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = sunRadius,
                            center = Offset(sunX, sunY)
                        )

                        // 4. Glint running to corner on click
                        if (sunFlashAnim > 0f) {
                            val flareX = sunX - sunX * sunFlashAnim
                            val flareY = sunY + (height - sunY) * sunFlashAnim
                            drawCircle(
                                color = Color(0xFFFEF08A).copy(alpha = 0.45f * (1f - sunFlashAnim)),
                                radius = 48f + 25f * sunFlashAnim,
                                center = Offset(flareX, flareY)
                            )
                        }
                    } else {
                        // Drawing MOON
                        drawCircle(
                            color = Color(0xFFE2E8F0),
                            radius = sunRadius,
                            center = Offset(sunX, sunY)
                        )
                        drawCircle(
                            color = Color(0xFF1E1B4B),
                            radius = sunRadius,
                            center = Offset(sunX - sunRadius * 0.4f, sunY - sunRadius * 0.1f)
                        )
                    }

                    // Parallax clouds pushing slightly apart upon touch input!
                    // If cloudPushAnim > 0f, we introduce separation
                    drawParallaxClouds(width, height, cloudOffset1, cloudOffset2, cloudOffset3, cloudPushAnim)

                    // Tri-color holographic flag watermark
                    drawHolographicFlag(width, height, holoBlink, flagWaveAngle, gyroX, gyroY)

                    // Rising pollen stellar specks
                    val specRandom = Random(4721)
                    val specksCount = 18
                    for (s in 0 until specksCount) {
                        val baseScaleX = specRandom.nextFloat()
                        val baseScaleY = specRandom.nextFloat()
                        val speckSize = 3.5f + specRandom.nextFloat() * 5f
                        val phaseOffset = sin((sunAngle / 15f) + s) * 35f
                        val waveRiseY = -((sunAngle * 0.35f + s * 160f) % (height + 250f))
                        val finalX = (baseScaleX * width + phaseOffset) % width
                        val finalY = (height - (baseScaleY * height + waveRiseY) % height) % height

                        drawCircle(
                            color = Color(0xFFFEF08A).copy(alpha = 0.14f * (0.3f + 0.7f * sin((sunAngle / 9f) + s))),
                            radius = speckSize * 2.2f,
                            center = Offset(finalX, finalY)
                        )
                        drawCircle(
                            color = Color(0xFFFBBF24).copy(alpha = 0.65f * (0.3f + 0.7f * sin((sunAngle / 9f) + s))),
                            radius = speckSize,
                            center = Offset(finalX, finalY)
                        )
                    }

                    // V-formation of 5-7 birds slowly crossing the sky
                    val leadX = birdsOffset
                    val leadY = height * 0.16f
                    val birdCount = 6
                    for (b in 0 until birdCount) {
                        // Place birds in a V-formation trailing back left
                        val row = b / 2 + 1
                        val dir = if (b % 2 == 0) 1f else -1f
                        val bx = leadX - row * 70f
                        val by = leadY + row * 45f * dir
                        if (bx in -100f..(width + 100f)) {
                            drawBird(bx, by, 1.0f, flapPhase = b * 0.7f, poppySwayLoop)
                        }
                    }

                    // Meadow Base pasture grass
                    val grassHorizonY = height * 0.73f
                    val grassBaseBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4ADE80), Color(0xFF166534)),
                        startY = grassHorizonY,
                        endY = height
                    )
                    drawRect(
                        brush = grassBaseBrush,
                        topLeft = Offset(0f, grassHorizonY),
                        size = Size(width, height - grassHorizonY)
                    )

                    // Draw realistic background trees
                    drawRealisticTree(width * 0.12f, grassHorizonY, 1.4f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.30f, grassHorizonY, 1.1f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.48f, grassHorizonY, 1.5f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.70f, grassHorizonY, 1.2f, false, flagWaveAngle)
                    drawRealisticTree(width * 0.88f, grassHorizonY, 1.6f, false, flagWaveAngle)

                    // Wildflowers drawing with dynamic realistic bending stems upon touch
                    val flowerRandom = Random(1024)
                    val wildflowerCount = 60
                    for (i in 0 until wildflowerCount) {
                        val px = flowerRandom.nextFloat() * width
                        val py = grassHorizonY + fSizeMultiplier() * (height - grassHorizonY - 60f)
                        val flowerSize = 10f + flowerRandom.nextFloat() * 11f

                        val baseWaveSway = sin(poppySwayLoop + (py / 120f)) * (11f + (height - py) * 0.024f)

                        // 1. Calculate realistic bending when touched under finger!
                        var bendX = 0f
                        var bendY = 0f
                        if (touchedPoppyX >= 0f) {
                            val distToTouch = kotlin.math.hypot(px - touchedPoppyX, py - touchedPoppyY)
                            if (distToTouch < 160f) {
                                val intensity = (1f - distToTouch / 160f).coerceIn(0f, 1f) * poppyBendAnim
                                bendX = intensity * 45f * (if (px > touchedPoppyX) 1f else -1f)
                                bendY = intensity * 20f
                            }
                        }

                        val finalFlowerSwayX = baseWaveSway + bendX
                        val finalFlowerSwayY = bendY

                        // Render flower root stem curve
                        val stemPath = Path().apply {
                            moveTo(px, py + flowerSize * 2.2f)
                            quadraticTo(
                                px + finalFlowerSwayX * 0.4f, py + flowerSize * 1.5f + finalFlowerSwayY * 0.5f,
                                px + finalFlowerSwayX, py + finalFlowerSwayY
                            )
                        }
                        drawPath(
                            path = stemPath,
                            color = Color(0xFF15803D),
                            style = Stroke(width = 2.6f)
                        )

                        // Draw flower head
                        val type = i % 3
                        if (type == 0) {
                            // Red poppy flower
                            drawCircle(
                                color = Color(0xFFEF4444),
                                radius = flowerSize,
                                center = Offset(px + finalFlowerSwayX, py + finalFlowerSwayY)
                            )
                            drawCircle(
                                color = Color(0xFF1E293B),
                                radius = flowerSize * 0.38f,
                                center = Offset(px + finalFlowerSwayX, py + finalFlowerSwayY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = flowerSize * 0.12f,
                                center = Offset(px + finalFlowerSwayX + flowerSize * 0.14f, py + finalFlowerSwayY - flowerSize * 0.14f)
                            )
                        } else if (type == 1) {
                            // White daisies (ромашка)
                            val petalSpins = 9
                            for (p in 0 until petalSpins) {
                                val rad = (p * 2 * PI / petalSpins).toFloat()
                                val ptX = px + finalFlowerSwayX + cos(rad) * (flowerSize * 0.65f)
                                val ptY = py + finalFlowerSwayY + sin(rad) * (flowerSize * 0.65f)
                                drawCircle(
                                    color = Color.White,
                                    radius = flowerSize * 0.42f,
                                    center = Offset(ptX, ptY)
                                )
                            }
                            drawCircle(
                                color = Color(0xFFF59E0B),
                                radius = flowerSize * 0.4f,
                                center = Offset(px + finalFlowerSwayX, py + finalFlowerSwayY)
                            )
                        } else {
                            // Blue Cornflower (василёк)
                            val petalSpins = 7
                            for (p in 0 until petalSpins) {
                                val rad = (p * 2 * PI / petalSpins).toFloat()
                                val ptX = px + finalFlowerSwayX + cos(rad) * (flowerSize * 0.6f)
                                val ptY = py + finalFlowerSwayY + sin(rad) * (flowerSize * 0.6f)
                                drawCircle(
                                    color = Color(0xFF3B82F6),
                                    radius = flowerSize * 0.36f,
                                    center = Offset(ptX, ptY)
                                )
                            }
                            drawCircle(
                                color = Color(0xFF1D4ED8),
                                radius = flowerSize * 0.22f,
                                center = Offset(px + finalFlowerSwayX, py + finalFlowerSwayY)
                            )
                        }
                    }

                    // 6. Stylized Pond (Водоём) with animated ripples, water lilies, and jumping frog
                    val pondLeft = width * 0.45f
                    val pondTop = height * 0.84f
                    val pondWidth = width * 0.5f
                    val pondHeight = height * 0.13f

                    // Pond water body gradient
                    val pondWaterBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D9488).copy(alpha = 0.72f), Color(0xFF0F766E).copy(alpha = 0.90f)),
                        startY = pondTop,
                        endY = pondTop + pondHeight
                    )
                    drawOval(
                        brush = pondWaterBrush,
                        topLeft = Offset(pondLeft, pondTop),
                        size = Size(pondWidth, pondHeight)
                    )
                    // Highlights boundary rim / shallow beach edge
                    drawOval(
                        color = Color(0xFF99F6E4).copy(alpha = 0.45f),
                        topLeft = Offset(pondLeft - 2f, pondTop - 1f),
                        size = Size(pondWidth + 4f, pondHeight + 2f),
                        style = Stroke(width = 3f)
                    )

                    // Mirror reflection of drifting clouds inside pond water
                    val cl1_reflect_X = pondLeft + pondWidth * 0.3f + sin(cloudOffset1 * PI.toFloat() * 2f) * (pondWidth * 0.2f)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = 16f,
                        center = Offset(cl1_reflect_X, pondTop + pondHeight * 0.4f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = 22f,
                        center = Offset(cl1_reflect_X + 10f, pondTop + pondHeight * 0.42f)
                    )

                    // Interactive ripples (concentric waves)
                    for (w in 0 until 3) {
                        val phase = (poppySwayLoop * 2.2f + w * 2.0f) % (2f * PI.toFloat())
                        val factor = 0.15f + 0.85f * (phase / (2f * PI.toFloat()))
                        val alpha = 0.42f * (1f - (phase / (2f * PI.toFloat())))
                        drawOval(
                            color = Color.White.copy(alpha = alpha),
                            topLeft = Offset(
                                pondLeft + pondWidth * 0.5f - (pondWidth * 0.45f * factor),
                                pondTop + pondHeight * 0.5f - (pondHeight * 0.45f * factor)
                            ),
                            size = Size(pondWidth * 0.9f * factor, pondHeight * 0.9f * factor),
                            style = Stroke(width = 1.8f)
                        )
                    }

                    // Water lilies pads (green circle with radial slice)
                    val liliesPos = listOf(
                        Offset(pondLeft + pondWidth * 0.22f, pondTop + pondHeight * 0.35f),
                        Offset(pondLeft + pondWidth * 0.78f, pondTop + pondHeight * 0.65f),
                        Offset(pondLeft + pondWidth * 0.45f, pondTop + pondHeight * 0.75f)
                    )
                    liliesPos.forEach { pos ->
                        // Leaf pad
                        drawCircle(
                            color = Color(0xFF047857),
                            radius = 14f,
                            center = pos
                        )
                        // Cut-out wedge wedge shape
                        val padCutPath = Path().apply {
                            moveTo(pos.x, pos.y)
                            lineTo(pos.x + 16f, pos.y - 6f)
                            lineTo(pos.x + 16f, pos.y + 6f)
                            close()
                        }
                        drawPath(padCutPath, Color(0xFF0D9488).copy(alpha = 0.72f)) // Water overwrite

                        // Water lily flower (star white flower)
                        drawCircle(color = Color.White, radius = 5.5f, center = pos)
                        drawCircle(color = Color(0xFFFBBF24), radius = 2.2f, center = pos)
                    }

                    // Leap-jumping green frog!
                    // Parabolic jumping projection
                    val jumpH = 85f * sin(frogJumpAnim * PI.toFloat())
                    val frogCenterX = pondLeft + pondWidth * 0.65f - frogJumpAnim * 120f
                    val frogCenterY = pondTop + pondHeight * 0.45f - jumpH

                    // Frog head/body
                    drawCircle(
                        color = Color(0xFF22C55E),
                        radius = 10f,
                        center = Offset(frogCenterX, frogCenterY)
                    )
                    // Frog big eyes
                    drawCircle(
                        color = Color.White,
                        radius = 3.6f,
                        center = Offset(frogCenterX - 5f, frogCenterY - 8f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 1.6f,
                        center = Offset(frogCenterX - 5f, frogCenterY - 8f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.6f,
                        center = Offset(frogCenterX + 5f, frogCenterY - 8f)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 1.6f,
                        center = Offset(frogCenterX + 5f, frogCenterY - 8f)
                    )
                    // Sitting legs
                    if (frogJumpAnim == 0f) {
                        drawCircle(
                            color = Color(0xFF15803D),
                            radius = 4.5f,
                            center = Offset(frogCenterX - 11f, frogCenterY + 4f)
                        )
                        drawCircle(
                            color = Color(0xFF15803D),
                            radius = 4.5f,
                            center = Offset(frogCenterX + 11f, frogCenterY + 4f)
                        )
                    } else {
                        // Flying backward legs
                        drawLine(
                            color = Color(0xFF15803D),
                            start = Offset(frogCenterX - 5f, frogCenterY + 8f),
                            end = Offset(frogCenterX - 16f, frogCenterY + 20f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(0xFF15803D),
                            start = Offset(frogCenterX + 5f, frogCenterY + 8f),
                            end = Offset(frogCenterX + 16f, frogCenterY + 20f),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            // Draw ladybugs and butterflies
            if (mode != BrowserMode.STEALTH && mode != BrowserMode.INCOGNITO) {
                drawInsects(animatedInsects, poppySwayLoop)
            }
        }
    }
}

// Bends grass size multipliers
fun fSizeMultiplier(): Float = 0.35f + Random(4723).nextFloat() * 0.65f

// Beautiful dynamic clouds layer splitter
fun DrawScope.drawParallaxClouds(
    width: Float,
    height: Float,
    os1: Float,
    os2: Float,
    os3: Float,
    cloudPushAnim: Float
) {
    // Left drifting cloud (pushes left)
    val cx1 = width - (os1 * (width + 300f)) - cloudPushAnim * 0.4f
    drawCloudShape(cx1, height * 0.25f, 0.72f)

    // Secondary medium cloud (pushes right)
    val cx2 = width - (os2 * (width + 420f)) + 150f + cloudPushAnim * 0.6f
    drawCloudShape(cx2, height * 0.35f, 1.15f)

    // Third cloud (pushes left)
    val cx3 = width - (os3 * (width + 650f)) + 280f - cloudPushAnim * 0.8f
    drawCloudShape(cx3, height * 0.18f, 1.65f)
}

fun DrawScope.drawCloudShape(x: Float, y: Float, valScale: Float) {
    val cloudColor = Color.White.copy(alpha = 0.84f)
    drawCircle(color = cloudColor, radius = 32f * valScale, center = Offset(x, y))
    drawCircle(color = cloudColor, radius = 45f * valScale, center = Offset(x + 30f * valScale, y - 10f * valScale))
    drawCircle(color = cloudColor, radius = 38f * valScale, center = Offset(x + 62f * valScale, y))
    drawCircle(color = cloudColor, radius = 26f * valScale, center = Offset(x - 30f * valScale, y + 5f * valScale))
}

// Holographic flag influenced by roll/pitch tilt!
fun DrawScope.drawHolographicFlag(
    width: Float,
    height: Float,
    holoAlpha: Float,
    waveAngle: Float,
    gyroX: Float,
    gyroY: Float
) {
    val blockH = height * 0.10f
    val startY = height * 0.35f + gyroY * 0.3f // Adjust top offset with pitch
    val flagpoleX = 25f + gyroX * 0.4f // Adjust pole offset with roll

    // Flagpole
    drawLine(
        color = Color(0xFF78909C),
        start = Offset(flagpoleX, startY - 20f),
        end = Offset(flagpoleX, startY + blockH * 3.5f),
        strokeWidth = 6.2f
    )
    drawCircle(
        color = Color(0xFFCFD8DC),
        radius = 8.5f,
        center = Offset(flagpoleX, startY - 20f)
    )

    // Procedural grid calculations
    val pCountX = 35
    val pCountY = 12
    val flagW = width - flagpoleX - 40f
    val flagH = blockH * 3f
    val stepX = flagW / (pCountX - 1)
    val stepY = flagH / (pCountY - 1)

    val nodes = Array(pCountX) { i ->
        Array(pCountY) { j ->
            val lx = flagpoleX + i * stepX
            val ly = startY + j * stepY

            val propDelay = i * 0.18f
            val amplitude = 1.0f + (i.toFloat() / pCountX) * 0.6f

            // Added Gyro tilt shifts inside physical waves!
            val gyroWaveX = sin(gyroX * 0.05f) * 12f * (i.toFloat() / pCountX)
            val gyroWaveY = cos(gyroY * 0.05f) * 8f * (i.toFloat() / pCountX)

            val waveX = sin(waveAngle * 2.5f - propDelay) * (6f * amplitude) + gyroWaveX
            val waveY = sin(waveAngle * 1.8f - propDelay) * (21f * amplitude) + gyroWaveY
            val slump = (i * i * 0.012f) * (1f - (j.toFloat() / pCountY) * 0.22f)

            Offset(lx + waveX, ly + waveY + slump)
        }
    }

    val finalAlpha = holoAlpha * 1.6f
    val stripesColors = listOf(
        Pair(Color(0xFFFFFFFF), Color(0xFFECEFF1)),
        Pair(Color(0xFF0D47A1), Color(0xFF1976D2)),
        Pair(Color(0xFFB71C1C), Color(0xFFD32F2F))
    )

    val stripYRows = pCountY / 3
    for (s in 0 until 3) {
        val stRow = s * stripYRows
        val edRow = (s + 1) * stripYRows

        val stripePath = Path()
        stripePath.moveTo(nodes[0][stRow].x, nodes[0][stRow].y)
        for (i in 1 until pCountX) {
            stripePath.lineTo(nodes[i][stRow].x, nodes[i][stRow].y)
        }
        for (j in stRow + 1 until edRow) {
            stripePath.lineTo(nodes[pCountX - 1][j].x, nodes[pCountX - 1][j].y)
        }
        for (i in pCountX - 1 downTo 0) {
            stripePath.lineTo(nodes[i][edRow - 1].x, nodes[i][edRow - 1].y)
        }
        for (j in edRow - 2 downTo stRow) {
            stripePath.lineTo(nodes[0][j].x, nodes[0][j].y)
        }
        stripePath.close()

        val flagBrush = Brush.horizontalGradient(
            colors = listOf(stripesColors[s].first.copy(alpha = finalAlpha), stripesColors[s].second.copy(alpha = finalAlpha)),
            startX = flagpoleX,
            endX = width
        )
        drawPath(stripePath, flagBrush)
    }

    // Crease thread rendering
    for (i in 0 until pCountX - 1) {
        for (j in 0 until pCountY - 1) {
            val pA = nodes[i][j]
            val pB = nodes[i + 1][j]
            val pC = nodes[i][j + 1]

            val dy = (pB.y - pA.y) / stepX
            val shade = (1.0f + dy * 0.45f).coerceIn(0.5f, 1.5f)

            val crease = if (shade > 1.0f) {
                Color.White.copy(alpha = (shade - 1.0f) * 0.15f * finalAlpha)
            } else {
                Color.Black.copy(alpha = (1.0f - shade) * 0.17f * finalAlpha)
            }

            drawLine(color = crease, start = pA, end = pC, strokeWidth = 1.3f)
        }
    }
}

// Draw insects with wings animations
fun DrawScope.drawInsects(bugs: List<Insect>, poppySway: Float) {
    bugs.forEach { bug ->
        val x = bug.x
        val y = bug.y
        val scale = bug.scale

        if (bug.type == "butterfly") {
            rotate(bug.angle, pivot = Offset(x, y)) {
                // Flutter rapid wings shift
                val flutterShift = sin(poppySway * 18f) * 12f * scale
                
                // Body
                drawLine(
                    color = Color(0xFF2B1D0C),
                    start = Offset(x, y - 18f * scale),
                    end = Offset(x, y + 18f * scale),
                    strokeWidth = 3.6f * scale
                )

                // Right wings
                val wingR = Path().apply {
                    moveTo(x, y)
                    cubicTo(x + 36f * scale + flutterShift, y - 36f * scale, x + 48f * scale + flutterShift, y - 10f * scale, x, y + 4f * scale)
                    cubicTo(x + 38f * scale, y + 10f * scale, x + 24f * scale, y + 28f * scale, x, y + 4f * scale)
                }
                drawPath(wingR, bug.color)

                // Left wings
                val wingL = Path().apply {
                    moveTo(x, y)
                    cubicTo(x - 36f * scale - flutterShift, y - 36f * scale, x - 48f * scale - flutterShift, y - 10f * scale, x, y + 4f * scale)
                    cubicTo(x - 38f * scale, y + 10f * scale, x - 24f * scale, y + 28f * scale, x, y + 4f * scale)
                }
                drawPath(wingL, bug.color)

                // Center shiny wing spots
                drawCircle(color = Color.White, radius = 4.2f * scale, center = Offset(x + 18f * scale, y - 14f * scale))
                drawCircle(color = Color.White, radius = 4.2f * scale, center = Offset(x - 18f * scale, y - 14f * scale))
            }
        } else {
            // Ladybug
            rotate(bug.angle, pivot = Offset(x, y)) {
                if (!bug.isFlying) {
                    // Standard closed round shell
                    drawCircle(color = bug.color, radius = 14f * scale, center = Offset(x, y))
                    drawCircle(color = Color.Black, radius = 6.5f * scale, center = Offset(x, y - 11f * scale))
                    // Shell divider
                    drawLine(
                        color = Color.Black,
                        start = Offset(x, y - 14f * scale),
                        end = Offset(x, y + 14f * scale),
                        strokeWidth = 2f * scale
                    )
                } else {
                    // Flying, wings spread open!
                    // Black underbody
                    drawCircle(color = Color.Black, radius = 12f * scale, center = Offset(x, y))
                    drawCircle(color = Color.Black, radius = 5.5f * scale, center = Offset(x, y - 11f * scale))

                    // Red wings spread at dynamic flap angles rotating rapidly
                    val wingSpreadFlap = sin(poppySway * 32f) * 16f
                    // Left red wing
                    rotate(-25f + wingSpreadFlap, pivot = Offset(x, y)) {
                        drawOval(
                            color = bug.color,
                            topLeft = Offset(x - 15f * scale, y - 12f * scale),
                            size = Size(14f * scale, 24f * scale)
                        )
                        // Spot
                        drawCircle(color = Color.Black, radius = 2.5f * scale, center = Offset(x - 8f * scale, y))
                    }
                    // Right red wing
                    rotate(25f - wingSpreadFlap, pivot = Offset(x, y)) {
                        drawOval(
                            color = bug.color,
                            topLeft = Offset(x + 1f * scale, y - 12f * scale),
                            size = Size(14f * scale, 24f * scale)
                        )
                        // Spot
                        drawCircle(color = Color.Black, radius = 2.5f * scale, center = Offset(x + 8f * scale, y))
                    }
                }
                // Black spots on closed body
                if (!bug.isFlying) {
                    drawCircle(color = Color.Black, radius = 2.4f * scale, center = Offset(x - 5.5f * scale, y - 3f * scale))
                    drawCircle(color = Color.Black, radius = 2.4f * scale, center = Offset(x + 5.5f * scale, y - 3f * scale))
                    drawCircle(color = Color.Black, radius = 2.8f * scale, center = Offset(x - 5.5f * scale, y + 4.5f * scale))
                    drawCircle(color = Color.Black, radius = 2.8f * scale, center = Offset(x + 5.5f * scale, y + 4.5f * scale))
                }
            }
        }
    }
}

fun DrawScope.drawBird(bx: Float, by: Float, scale: Float, flapPhase: Float, poppySway: Float) {
    val wingS = 13f * scale
    val flapFactor = sin(poppySway * 5.5f + flapPhase) * wingS
    val bPath = Path().apply {
        // Left wing
        moveTo(bx - wingS, by - flapFactor)
        // Center of body
        quadraticTo(bx - wingS * 0.4f, by + 1.5f, bx, by)
        // Right wing
        quadraticTo(bx + wingS * 0.4f, by + 1.5f, bx + wingS, by - flapFactor)
        lineTo(bx + wingS, by - flapFactor + 2f)
        quadraticTo(bx + wingS * 0.4f, by + 3.5f, bx, by + 2.0f)
        quadraticTo(bx - wingS * 0.4f, by + 3.5f, bx - wingS, by - flapFactor + 2f)
        close()
    }
    drawPath(bPath, Color(0xFF1E293B).copy(alpha = 0.75f))
}

fun DrawScope.drawKidsDolphins(w: Float, h: Float, tick: Float) {
    val dolphinTint = Color(0xFF0288D1).copy(alpha = 0.16f)
    for (i in 0 until 3) {
        val spacerX = i * 360f
        val dx = (w * 0.15f + spacerX + sin(tick / 16f + i) * 55f) % (w + 220f) - 110f
        val dy = h * 0.52f + sin(tick / 11f + i) * 75f

        drawCircle(color = dolphinTint, radius = 55f, center = Offset(dx, dy))
        drawCircle(color = dolphinTint, radius = 38f, center = Offset(dx + 48f, dy - 22f))
        drawCircle(color = dolphinTint, radius = 22f, center = Offset(dx - 48f, dy + 12f))

        val finPath = Path().apply {
            moveTo(dx - 48f, dy + 12f)
            lineTo(dx - 76f, dy - 2f)
            lineTo(dx - 70f, dy + 32f)
            close()
        }
        drawPath(finPath, dolphinTint)
    }
}

fun DrawScope.drawRealisticTree(x: Float, y: Float, scale: Float, isNight: Boolean, swayAngle: Float) {
    val trunkSway = sin(swayAngle) * 2.2f * scale
    val branchSway = sin(swayAngle * 1.3f) * 6.5f * scale
    val foliageSwayX = sin(swayAngle * 1.05f) * 10f * scale
    val foliageSwayY = cos(swayAngle * 0.7f) * 4f * scale

    val trkW = 14f * scale
    val trkH = 60f * scale

    val trunkBrush = if (isNight) {
        Brush.verticalGradient(listOf(Color(0xFF0E1E38), Color(0xFF030712)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF5D4037), Color(0xFF3E2723)))
    }

    val trunkPath = Path().apply {
        moveTo(x - trkW / 2f, y)
        lineTo(x - trkW * 0.3f + trunkSway, y - trkH)
        lineTo(x - trkW * 0.8f + branchSway - 10f * scale, y - trkH - 16f * scale)
        lineTo(x - trkW * 0.4f + branchSway, y - trkH - 18f * scale)
        lineTo(x + trunkSway, y - trkH - 9f * scale)
        lineTo(x + trkW * 0.5f + branchSway + 10f * scale, y - trkH - 20f * scale)
        lineTo(x + trkW * 0.8f + branchSway + 15f * scale, y - trkH - 18f * scale)
        lineTo(x + trkW / 2f, y)
        close()
    }
    drawPath(trunkPath, trunkBrush)

    val foliageColors = if (isNight) {
        listOf(
            Color(0xFF1E293B).copy(alpha = 0.8f),
            Color(0xFF0F172A).copy(alpha = 0.9f)
        )
    } else {
        listOf(
            Color(0xFF15803D).copy(alpha = 0.95f),
            Color(0xFF166534).copy(alpha = 0.97f),
            Color(0xFF22C55E).copy(alpha = 0.92f)
        )
    }

    val crownCenterY = y - trkH - 9f * scale
    val crownCircles = listOf(
        Triple(-18f * scale, -9f * scale, 26f * scale),
        Triple(18f * scale, -9f * scale, 26f * scale),
        Triple(0f * scale, -22f * scale, 32f * scale)
    )

    crownCircles.forEachIndexed { i, (ox, oy, r) ->
        val phase = i * 0.4f
        val swayIndX = foliageSwayX + sin(swayAngle * 1.2f + phase) * 2.5f * scale
        val swayIndY = foliageSwayY + cos(swayAngle * 0.9f + phase) * 1.8f * scale

        val radBrush = Brush.radialGradient(
            colors = listOf(foliageColors[i % foliageColors.size], foliageColors[(i + 1) % foliageColors.size]),
            center = Offset(x + ox + swayIndX - r * 0.2f, crownCenterY + oy + swayIndY - r * 0.2f),
            radius = r * 1.2f
        )
        drawCircle(
            brush = radBrush,
            radius = r + sin(swayAngle * 2.0f + phase) * 1.0f * scale,
            center = Offset(x + ox + swayIndX, crownCenterY + oy + swayIndY)
        )
    }
}
