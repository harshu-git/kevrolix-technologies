package com.antiphonesnatcher.app.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.antiphonesnatcher.app.data.PanicRecoveryMethod
import com.antiphonesnatcher.app.data.PrivacyMode
import com.antiphonesnatcher.app.data.TapSequenceHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Apple-Grade Stealth Shield & Panic Overlay View.
 *
 * Designed for emergency privacy:
 * - OLED Pure Blackout (0-power, looks completely powered off)
 * - Decoy System Update
 * - Decoy Minimal Clock
 *
 * Total Lockdown:
 * - A single tap does NOTHING in PIN mode (screen stays pitch-black).
 * - In Secret Tap Sequence mode, tapping the user's custom 4-zone sequence on the dark screen unlocks.
 * - Volume Up + Down combo or Two-Finger tap directly wakes the unlock pad.
 */
class PrivacyOverlayView(
    context: Context,
    private val onDismissRequest: () -> Unit = {}
) : View(context) {

    private var currentMode: PrivacyMode = PrivacyMode.BLACKOUT
    private var currentStrength: Float = 1.0f
    private var recoveryMethod: PanicRecoveryMethod = PanicRecoveryMethod.SECRET_TAPS
    private var targetPin: String = "1234"
    private var targetSequence: List<Int> = listOf(1, 2, 4, 3)
    private val enteredSequence = mutableListOf<Int>()
    private var lastSequenceTapTime = 0L

    // PIN Pad State
    private var isPinPadVisible = false
    private val enteredPin = StringBuilder()
    private var pinErrorShake = false
    private val keyRects = mutableListOf<Pair<String, RectF>>()

    // Secret Tap & Gesture State
    private var twoFingerDownTimestamp = 0L
    private val twoFingerTapTimestamps = mutableListOf<Long>()

    // Volume combo detection state
    private var isVolUpPressed = false
    private var isVolDownPressed = false
    private var lastVolUpTime = 0L
    private var lastVolDownTime = 0L

    // Time formatters for clock decoy
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    // Paints
    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val titleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val subtitleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8E8E93")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    private val progressBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#222228")
    }

    private val progressBarFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#00E599") // Vibrant emerald
    }

    private val progressRect = RectF()
    private val progressBgRect = RectF()

    // PIN UI Paints
    private val pinDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pinKeyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1C1C20")
    }

    private val pinKeyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val hintTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#636366")
        textAlign = Paint.Align.CENTER
        textSize = 34f
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        requestFocus()
        StatusBarLockHelper.enforceImmersiveSticky(this)
        StatusBarLockHelper.collapsePanels(context)

        @Suppress("DEPRECATION")
        setOnSystemUiVisibilityChangeListener { visibility ->
            if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                StatusBarLockHelper.enforceImmersiveSticky(this)
                StatusBarLockHelper.collapsePanels(context)
            }
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            // Notification panel or system dialog attempted to take focus — immediately collapse and regain focus
            StatusBarLockHelper.collapsePanels(context)
            requestFocus()
            postDelayed({ StatusBarLockHelper.collapsePanels(context); requestFocus() }, 25)
            postDelayed({ StatusBarLockHelper.collapsePanels(context); requestFocus() }, 60)
            postDelayed({ StatusBarLockHelper.collapsePanels(context) }, 120)
        } else {
            StatusBarLockHelper.enforceImmersiveSticky(this)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val now = System.currentTimeMillis()

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                isVolUpPressed = true
                lastVolUpTime = now
                checkVolumeTrigger()
            } else if (event.action == KeyEvent.ACTION_UP) {
                isVolUpPressed = false
            }
            return true // Absorb volume key to prevent volume change sounds
        } else if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                isVolDownPressed = true
                lastVolDownTime = now
                checkVolumeTrigger()
            } else if (event.action == KeyEvent.ACTION_UP) {
                isVolDownPressed = false
            }
            return true // Absorb volume key
        }

        // Absorb all other keys (Back button, Home button, Menu)
        return true
    }

    private fun checkVolumeTrigger() {
        // Trigger if both volume buttons pressed together or within 450ms of each other
        if ((isVolUpPressed && isVolDownPressed) || Math.abs(lastVolUpTime - lastVolDownTime) < 450L) {
            revealUnlockPad()
        }
    }

    fun revealUnlockPad() {
        if (!isPinPadVisible) {
            isPinPadVisible = true
            enteredPin.clear()
            invalidate()
            removeCallbacks(autoHidePinRunnable)
            postDelayed(autoHidePinRunnable, 14000L) // Auto-hide after 14s of inactivity
        }
    }

    private val autoHidePinRunnable = Runnable {
        if (isPinPadVisible) {
            isPinPadVisible = false
            enteredPin.clear()
            invalidate()
        }
    }

    fun updateConfiguration(
        mode: PrivacyMode,
        strength: Float,
        method: PanicRecoveryMethod = PanicRecoveryMethod.SECRET_TAPS,
        pin: String = "1234",
        tapSequence: List<Int> = listOf(1, 2, 4, 3)
    ) {
        currentMode = mode
        currentStrength = strength
        recoveryMethod = method
        targetPin = pin
        targetSequence = tapSequence
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        StatusBarLockHelper.applySystemGestureExclusion(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!hasFocus()) {
            requestFocus()
        }

        // Top Edge Interceptor: If a swipe or touch begins in the top status bar area,
        // snap the notification shade closed immediately before SystemUI can expand it.
        val topStatusThreshold = (48 * resources.displayMetrics.density).toInt()
        if (event.y < topStatusThreshold &&
            (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)) {
            StatusBarLockHelper.collapsePanels(context)
        }

        val now = System.currentTimeMillis()

        // 1. If PIN pad is visible, process keypad touches
        if (isPinPadVisible && recoveryMethod == PanicRecoveryMethod.PIN) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Reset auto-hide timer on user interaction
                removeCallbacks(autoHidePinRunnable)
                postDelayed(autoHidePinRunnable, 14000L)

                val x = event.x
                val y = event.y
                for ((digit, rect) in keyRects) {
                    if (rect.contains(x, y)) {
                        handlePinInput(digit)
                        return true
                    }
                }
            }
            return true
        }

        // 2. Pure Blackout: TWO-FINGER gestures directly wake unlock pad (ONLY in PIN mode!)
        // In Secret Tap Sequence mode, two-finger gestures do NOT unlock and do NOT reveal any pad.
        if (event.pointerCount == 2) {
            val x0 = event.getX(0)
            val y0 = event.getY(0)
            val x1 = event.getX(1)
            val y1 = event.getY(1)
            val distance = Math.hypot((x0 - x1).toDouble(), (y0 - y1).toDouble())

            val effectiveWidth = if (width > 0) width.toFloat() else resources.displayMetrics.widthPixels.toFloat()
            // Genuine two distinct fingers: spacing between 60px (~1.5cm) and 90% of screen width
            val isGenuineTwoFingers = distance in 60.0..(effectiveWidth * 0.90)

            if (isGenuineTwoFingers) {
                when (event.actionMasked) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        twoFingerDownTimestamp = now
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 2-Finger Long Press (hold for 800ms): instantly wakes PIN pad with tactile feedback
                        if (twoFingerDownTimestamp > 0 && (now - twoFingerDownTimestamp) >= 800L) {
                            twoFingerDownTimestamp = 0L
                            if (recoveryMethod == PanicRecoveryMethod.PIN) {
                                try {
                                    performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                } catch (_: Exception) {}
                                revealUnlockPad()
                                return true
                            }
                        }
                    }
                    MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                        // 2-Finger Tap (touch & release within 650ms): wakes PIN pad immediately
                        if (twoFingerDownTimestamp > 0 && (now - twoFingerDownTimestamp) < 650L) {
                            twoFingerDownTimestamp = 0L
                            if (recoveryMethod == PanicRecoveryMethod.PIN) {
                                try {
                                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                } catch (_: Exception) {}
                                revealUnlockPad()
                                return true
                            }
                        }
                    }
                }
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_CANCEL || event.actionMasked == MotionEvent.ACTION_UP) {
            if (event.pointerCount <= 1) {
                twoFingerDownTimestamp = 0L
            }
        }

        // 3. Single Finger Taps:
        // In Secret Tap Sequence mode, check the tapped 4-zone sequence on the dark screen!
        if (event.pointerCount == 1 && event.action == MotionEvent.ACTION_DOWN) {
            if (recoveryMethod == PanicRecoveryMethod.SECRET_TAPS) {
                // If more than 4 seconds elapsed between taps, restart sequence cleanly
                if (now - lastSequenceTapTime > 4000L) {
                    enteredSequence.clear()
                }
                lastSequenceTapTime = now

                val w = if (width > 0) width.toFloat() else resources.displayMetrics.widthPixels.toFloat()
                val h = if (height > 0) height.toFloat() else resources.displayMetrics.heightPixels.toFloat()

                if (w > 0 && h > 0) {
                    val zone = TapSequenceHelper.getZoneFromCoordinates(event.x, event.y, w, h)
                    enteredSequence.add(zone)

                    // Subtle tactile feedback on each registered quadrant tap in the dark
                    try {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {}

                    val requiredSeq = if (targetSequence.isNotEmpty()) targetSequence else listOf(1, 2, 4, 3)
                    if (enteredSequence.size == requiredSeq.size) {
                        if (enteredSequence == requiredSeq) {
                            enteredSequence.clear()
                            try {
                                performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } catch (_: Exception) {}
                            onDismissRequest()
                            return true
                        } else {
                            // Incorrect sequence: reset buffer so user can immediately retry cleanly
                            enteredSequence.clear()
                        }
                    }
                }
            }
            // In PIN mode, single touches do NOTHING (screen stays pitch-black).
        }

        return true // Absorb all touches to keep screen locked & pitch-black
    }

    private fun handlePinInput(digit: String) {
        when (digit) {
            "DEL" -> {
                if (enteredPin.isNotEmpty()) {
                    enteredPin.deleteCharAt(enteredPin.length - 1)
                    invalidate()
                }
            }
            "CLOSE" -> {
                removeCallbacks(autoHidePinRunnable)
                isPinPadVisible = false
                enteredPin.clear()
                invalidate()
            }
            else -> {
                if (enteredPin.length < 4) {
                    enteredPin.append(digit)
                    invalidate()

                    if (enteredPin.length == 4) {
                        if (enteredPin.toString() == targetPin) {
                            // Verified! Dismiss Panic Mode
                            removeCallbacks(autoHidePinRunnable)
                            onDismissRequest()
                        } else {
                            // Wrong PIN: Discreetly clear and return to pure blackout
                            postDelayed({
                                enteredPin.clear()
                                isPinPadVisible = false
                                invalidate()
                            }, 450)
                        }
                    }
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // Base background: OLED pitch-black
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)

        if (isPinPadVisible && recoveryMethod == PanicRecoveryMethod.PIN) {
            drawPinPad(canvas, w, h)
            return
        }

        when (currentMode) {
            PrivacyMode.BLACKOUT -> {
                // Pure black, 0 brightness - completely dead screen aesthetic
            }
            PrivacyMode.DECOY_UPDATE -> {
                drawDecoySystemUpdate(canvas, w, h)
            }
            PrivacyMode.DECOY_CLOCK -> {
                drawDecoyClock(canvas, w, h)
            }
        }
    }

    private fun drawDecoySystemUpdate(canvas: Canvas, w: Float, h: Float) {
        val centerX = w / 2f
        val centerY = h / 2f - 60f

        titleTextPaint.textSize = w * 0.055f
        subtitleTextPaint.textSize = w * 0.035f

        canvas.drawText("Restarting...", centerX, centerY - 50f, titleTextPaint)
        canvas.drawText("Do not turn off your device", centerX, centerY, subtitleTextPaint)

        val barWidth = w * 0.55f
        val barHeight = 6f
        val barLeft = (w - barWidth) / 2f
        val barTop = centerY + 40f

        progressBgRect.set(barLeft, barTop, barLeft + barWidth, barTop + barHeight)
        canvas.drawRoundRect(progressBgRect, 3f, 3f, progressBarBgPaint)

        val progress = (System.currentTimeMillis() % 6000L) / 6000f
        progressRect.set(barLeft, barTop, barLeft + (barWidth * progress), barTop + barHeight)
        canvas.drawRoundRect(progressRect, 3f, 3f, progressBarFillPaint)
    }

    private fun drawDecoyClock(canvas: Canvas, w: Float, h: Float) {
        val now = Date()
        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)

        val centerX = w / 2f
        val centerY = h * 0.35f

        titleTextPaint.textSize = w * 0.22f
        titleTextPaint.typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
        canvas.drawText(timeStr, centerX, centerY, titleTextPaint)

        subtitleTextPaint.textSize = w * 0.042f
        subtitleTextPaint.color = Color.parseColor("#AEAEB2")
        canvas.drawText(dateStr, centerX, centerY + (w * 0.12f), subtitleTextPaint)
    }

    private fun drawPinPad(canvas: Canvas, w: Float, h: Float) {
        keyRects.clear()

        val centerX = w / 2f
        val startY = h * 0.24f

        titleTextPaint.textSize = 20f * resources.displayMetrics.scaledDensity
        titleTextPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        canvas.drawText("Emergency Unlock", centerX, startY, titleTextPaint)

        // Draw dots
        val dotRadius = 7f * resources.displayMetrics.density
        val dotSpacing = 28f * resources.displayMetrics.density
        val totalDotsWidth = 3 * dotSpacing
        val dotsStartX = centerX - (totalDotsWidth / 2f)
        val dotsY = startY + 50f

        for (i in 0 until 4) {
            val dotX = dotsStartX + (i * dotSpacing)
            val isFilled = i < enteredPin.length
            pinDotPaint.color = if (isFilled) 0xFF00E599.toInt() else 0xFF333338.toInt()
            canvas.drawCircle(dotX, dotsY, dotRadius, pinDotPaint)
        }

        // Draw Keypad (3 columns x 4 rows)
        val keySize = 68f * resources.displayMetrics.density
        val keyGapX = 26f * resources.displayMetrics.density
        val keyGapY = 18f * resources.displayMetrics.density
        val gridWidth = (3 * keySize) + (2 * keyGapX)
        val gridStartX = (w - gridWidth) / 2f
        val gridStartY = dotsY + 50f

        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("CLOSE", "0", "DEL")
        )

        pinKeyTextPaint.textSize = 22f * resources.displayMetrics.scaledDensity

        for (row in 0..3) {
            for (col in 0..2) {
                val key = keys[row][col]
                val kx = gridStartX + (col * (keySize + keyGapX))
                val ky = gridStartY + (row * (keySize + keyGapY))

                val rect = RectF(kx, ky, kx + keySize, ky + keySize)
                keyRects.add(Pair(key, rect))

                if (key == "CLOSE") {
                    pinKeyBgPaint.color = 0xFF16161A.toInt()
                    canvas.drawRoundRect(rect, keySize / 2f, keySize / 2f, pinKeyBgPaint)
                    pinKeyTextPaint.textSize = 12f * resources.displayMetrics.scaledDensity
                    pinKeyTextPaint.color = 0xFF8E8E93.toInt()
                    val textY = ky + (keySize / 2f) - ((pinKeyTextPaint.descent() + pinKeyTextPaint.ascent()) / 2f)
                    canvas.drawText("CLOSE", kx + (keySize / 2f), textY, pinKeyTextPaint)
                } else if (key == "DEL") {
                    pinKeyBgPaint.color = 0xFF16161A.toInt()
                    canvas.drawRoundRect(rect, keySize / 2f, keySize / 2f, pinKeyBgPaint)
                    pinKeyTextPaint.textSize = 13f * resources.displayMetrics.scaledDensity
                    pinKeyTextPaint.color = 0xFF8E8E93.toInt()
                    val textY = ky + (keySize / 2f) - ((pinKeyTextPaint.descent() + pinKeyTextPaint.ascent()) / 2f)
                    canvas.drawText("⌫", kx + (keySize / 2f), textY, pinKeyTextPaint)
                } else {
                    pinKeyBgPaint.color = 0xFF1E1E24.toInt()
                    canvas.drawRoundRect(rect, keySize / 2f, keySize / 2f, pinKeyBgPaint)
                    pinKeyTextPaint.textSize = 22f * resources.displayMetrics.scaledDensity
                    pinKeyTextPaint.color = Color.WHITE
                    val textY = ky + (keySize / 2f) - ((pinKeyTextPaint.descent() + pinKeyTextPaint.ascent()) / 2f)
                    canvas.drawText(key, kx + (keySize / 2f), textY, pinKeyTextPaint)
                }
            }
        }
    }
}
