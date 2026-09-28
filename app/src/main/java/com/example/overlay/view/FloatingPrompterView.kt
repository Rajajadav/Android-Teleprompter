package com.example.overlay.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.camera.NativeCameraLauncher
import com.example.data.local.ScriptEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@SuppressLint("ViewConstructor")
class FloatingPrompterView(
    context: Context,
    private val windowManager: WindowManager,
    private val onSavePosition: (Int, Int, Int) -> Unit,
    private val onCloseRequest: () -> Unit
) : FrameLayout(context) {

    // State
    private var script: ScriptEntity? = null
    private var isPlaying = false
    private var isLocked = false
    private var isMinimized = false
    private var isMirrorMode = false
    private var scrollSpeed = 1.2f
    private var fontSizeSp = 34f
    private var backgroundOpacity = 0.85f

    // Window Layout Params
    val windowParams: WindowManager.LayoutParams
    private var screenWidth = 1080
    private var screenHeight = 2400
    private var safeTopInset = 100

    // Views
    private val expandedContainer: LinearLayout
    private val minimizedContainer: FrameLayout
    private val titleText: TextView
    private val dragHandle: View
    private val lockButton: ImageView
    private val mirrorButton: TextView
    private val minimizeButton: ImageView
    private val closeButton: ImageView
    private val scriptScrollView: ScrollView
    private val scriptContentText: TextView
    private val readingGuideLine: View
    private val controlsBar: LinearLayout
    private val playPauseButton: TextView
    private val speedLabel: TextView
    private val textSizeLabel: TextView
    private val openCameraButton: TextView

    // Scrolling Coroutine
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var scrollJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoHideControlsRunnable = Runnable {
        if (isPlaying) {
            controlsBar.visibility = View.GONE
        }
    }

    init {
        // Calculate display metrics
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        // WindowManager LayoutParams
        val defaultWidth = (screenWidth * 0.92f).toInt()
        val defaultHeight = (screenHeight * 0.28f).toInt()

        windowParams = WindowManager.LayoutParams(
            defaultWidth,
            defaultHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenWidth - defaultWidth) / 2
            y = 160 // Near front camera lens
        }

        // Expanded Container Setup
        expandedContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = createCardBackground(backgroundOpacity)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            elevation = dp(12).toFloat()
        }

        // Header Row: Drag Handle, Title, Lock, Mirror, Minimize, Close
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(2), dp(4), dp(6))
        }

        dragHandle = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(5)).apply {
                marginEnd = dp(8)
            }
            background = createRoundedDrawable(Color.parseColor("#4B5563"), dp(3))
        }

        titleText = TextView(context).apply {
            text = "PromptDesk"
            setTextColor(Color.parseColor("#5EEAD4")) // Teal80
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            isSingleLine = true
        }

        mirrorButton = createHeaderPillButton("MIRROR") {
            toggleMirrorMode()
        }

        lockButton = createHeaderIconButton(android.R.drawable.ic_lock_lock) {
            toggleLock()
        }

        minimizeButton = createHeaderIconButton(android.R.drawable.arrow_down_float) {
            setMinimizedState(true)
        }

        closeButton = createHeaderIconButton(android.R.drawable.ic_menu_close_clear_cancel) {
            onCloseRequest()
        }

        headerRow.addView(dragHandle)
        headerRow.addView(titleText)
        headerRow.addView(mirrorButton)
        headerRow.addView(lockButton)
        headerRow.addView(minimizeButton)
        headerRow.addView(closeButton)
        expandedContainer.addView(headerRow)

        // Reading guide line
        readingGuideLine = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2)).apply {
                topMargin = dp(2)
                bottomMargin = dp(2)
            }
            background = createRoundedDrawable(Color.parseColor("#3314B8A6"), dp(1))
        }
        expandedContainer.addView(readingGuideLine)

        // Script Scroll Area
        scriptScrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        scriptContentText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            gravity = Gravity.CENTER_HORIZONTAL
            setLineSpacing(dp(6).toFloat(), 1.25f)
            setPadding(dp(8), dp(40), dp(8), dp(180)) // top and bottom breathing room
        }
        scriptScrollView.addView(scriptContentText)
        expandedContainer.addView(scriptScrollView)

        // Bottom Controls Bar
        controlsBar = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(2))
        }

        // Restart button
        val restartBtn = createControlButton("⏮") {
            restartScroll()
        }

        // Play/Pause button
        playPauseButton = createControlButton("▶ PLAY") {
            togglePlayPause()
        }
        playPauseButton.setTextColor(Color.parseColor("#5EEAD4"))

        // Speed Controls
        val speedDownBtn = createControlButton("-") { adjustSpeed(-0.2f) }
        speedLabel = TextView(context).apply {
            text = "1.2x"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(dp(4), 0, dp(4), 0)
        }
        val speedUpBtn = createControlButton("+") { adjustSpeed(0.2f) }

        // Font Size Controls
        val fontDownBtn = createControlButton("A-") { adjustFontSize(-4f) }
        textSizeLabel = TextView(context).apply {
            text = "34"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(dp(4), 0, dp(4), 0)
        }
        val fontUpBtn = createControlButton("A+") { adjustFontSize(4f) }

        // Open Native Camera Action Button
        openCameraButton = TextView(context).apply {
            text = "📷 CAMERA"
            setTextColor(Color.parseColor("#042F2E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = createRoundedDrawable(Color.parseColor("#5EEAD4"), dp(12))
            setOnClickListener {
                NativeCameraLauncher.launchCamera(context)
            }
        }

        controlsBar.addView(restartBtn)
        controlsBar.addView(playPauseButton)
        controlsBar.addView(speedDownBtn)
        controlsBar.addView(speedLabel)
        controlsBar.addView(speedUpBtn)
        controlsBar.addView(fontDownBtn)
        controlsBar.addView(textSizeLabel)
        controlsBar.addView(fontUpBtn)
        controlsBar.addView(openCameraButton)

        expandedContainer.addView(controlsBar)
        addView(expandedContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // Minimized Floating Bubble Setup
        minimizedContainer = FrameLayout(context).apply {
            visibility = View.GONE
            val bubbleBg = createRoundedDrawable(Color.parseColor("#121822"), dp(24)).apply {
                setStroke(dp(2), Color.parseColor("#5EEAD4"))
            }
            background = bubbleBg
            setPadding(dp(14), dp(10), dp(14), dp(10))
            elevation = dp(12).toFloat()
        }

        val bubbleText = TextView(context).apply {
            text = "🎙 PromptDesk"
            setTextColor(Color.parseColor("#5EEAD4"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
        }
        minimizedContainer.addView(bubbleText)
        addView(minimizedContainer, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        setupTouchAndDragListeners()
    }

    fun setScriptData(data: ScriptEntity) {
        script = data
        titleText.text = data.title.ifBlank { "PromptDesk" }
        scriptContentText.text = data.content
        fontSizeSp = data.fontSize.coerceIn(24f, 90f)
        scriptContentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        textSizeLabel.text = "${fontSizeSp.toInt()}"

        scrollSpeed = data.scrollSpeed.coerceIn(0.1f, 5.0f)
        speedLabel.text = "${String.format("%.1f", scrollSpeed)}x"

        isMirrorMode = data.mirrorMode
        updateMirrorDisplay()

        // Apply saved position if valid
        if (data.overlayX != 0 || data.overlayY != 120) {
            windowParams.x = data.overlayX.coerceIn(0, screenWidth - dp(100))
            windowParams.y = data.overlayY.coerceIn(safeTopInset, screenHeight - dp(150))
            try {
                windowManager.updateViewLayout(this, windowParams)
            } catch (e: Exception) {
                // Ignore if not yet attached
            }
        }

        // Restore last read position
        if (data.lastPosition > 0) {
            scriptScrollView.post {
                scriptScrollView.scrollTo(0, data.lastPosition)
            }
        }
    }

    private fun setupTouchAndDragListeners() {
        var initialX = 0
        var initialY = 0
        var touchStartX = 0f
        var touchStartY = 0f
        var isDragging = false

        // Drag handle touch listener
        dragHandle.setOnTouchListener { _, event ->
            if (isLocked) return@setOnTouchListener false
            handleWindowDrag(event)
        }

        // Minimized bubble touch listener
        minimizedContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams.x
                    initialY = windowParams.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - touchStartX).toInt()
                    val deltaY = (event.rawY - touchStartY).toInt()
                    if (deltaX * deltaX + deltaY * deltaY > 30) {
                        isDragging = true
                        windowParams.x = (initialX + deltaX).coerceIn(0, screenWidth - dp(80))
                        windowParams.y = (initialY + deltaY).coerceIn(safeTopInset, screenHeight - dp(80))
                        windowManager.updateViewLayout(this, windowParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        setMinimizedState(false)
                    }
                    true
                }
                else -> false
            }
        }

        // Tap text area to toggle play/pause or controls
        scriptContentText.setOnClickListener {
            togglePlayPause()
        }

        // Double tap or header tap to toggle controls bar
        titleText.setOnClickListener {
            controlsBar.visibility = if (controlsBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            if (controlsBar.visibility == View.VISIBLE) scheduleControlsAutoHide()
        }
    }

    private var dragInitialX = 0
    private var dragInitialY = 0
    private var dragTouchStartX = 0f
    private var dragTouchStartY = 0f

    private fun handleWindowDrag(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragInitialX = windowParams.x
                dragInitialY = windowParams.y
                dragTouchStartX = event.rawX
                dragTouchStartY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = (event.rawX - dragTouchStartX).toInt()
                val deltaY = (event.rawY - dragTouchStartY).toInt()
                windowParams.x = (dragInitialX + deltaX).coerceIn(0, screenWidth - dp(100))
                windowParams.y = (dragInitialY + deltaY).coerceIn(safeTopInset, screenHeight - dp(120))
                windowManager.updateViewLayout(this, windowParams)
                return true
            }
            MotionEvent.ACTION_UP -> {
                onSavePosition(windowParams.x, windowParams.y, scriptScrollView.scrollY)
                return true
            }
        }
        return false
    }

    fun togglePlayPause() {
        if (isPlaying) {
            pauseScroll()
        } else {
            startScroll()
        }
    }

    fun startScroll() {
        isPlaying = true
        playPauseButton.text = "⏸ PAUSE"
        playPauseButton.setTextColor(Color.parseColor("#FCA5A5"))
        scheduleControlsAutoHide()

        scrollJob?.cancel()
        scrollJob = coroutineScope.launch {
            while (isPlaying) {
                val step = (scrollSpeed * 1.6f).toInt().coerceAtLeast(1)
                scriptScrollView.smoothScrollBy(0, step)
                delay(16L) // ~60fps smooth loop
            }
        }
    }

    fun pauseScroll() {
        isPlaying = false
        playPauseButton.text = "▶ PLAY"
        playPauseButton.setTextColor(Color.parseColor("#5EEAD4"))
        scrollJob?.cancel()
        controlsBar.visibility = View.VISIBLE
        handler.removeCallbacks(autoHideControlsRunnable)
        onSavePosition(windowParams.x, windowParams.y, scriptScrollView.scrollY)
    }

    fun restartScroll() {
        scriptScrollView.scrollTo(0, 0)
        startScroll()
    }

    fun adjustSpeed(delta: Float) {
        scrollSpeed = (scrollSpeed + delta).coerceIn(0.2f, 5.0f)
        speedLabel.text = "${String.format("%.1f", scrollSpeed)}x"
        scheduleControlsAutoHide()
    }

    fun adjustFontSize(delta: Float) {
        fontSizeSp = (fontSizeSp + delta).coerceIn(22f, 96f)
        scriptContentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
        textSizeLabel.text = "${fontSizeSp.toInt()}"
        scheduleControlsAutoHide()
    }

    private fun toggleMirrorMode() {
        isMirrorMode = !isMirrorMode
        updateMirrorDisplay()
    }

    private fun updateMirrorDisplay() {
        scriptContentText.scaleX = if (isMirrorMode) -1f else 1f
        mirrorButton.setTextColor(if (isMirrorMode) Color.parseColor("#7C3AED") else Color.parseColor("#94A3B8"))
    }

    private fun toggleLock() {
        isLocked = !isLocked
        dragHandle.visibility = if (isLocked) View.INVISIBLE else View.VISIBLE
        lockButton.setColorFilter(if (isLocked) Color.parseColor("#F59E0B") else Color.parseColor("#94A3B8"))
    }

    fun setMinimizedState(minimized: Boolean) {
        isMinimized = minimized
        if (minimized) {
            expandedContainer.visibility = View.GONE
            minimizedContainer.visibility = View.VISIBLE
            windowParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            windowParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            expandedContainer.visibility = View.VISIBLE
            minimizedContainer.visibility = View.GONE
            windowParams.width = (screenWidth * 0.92f).toInt()
            windowParams.height = (screenHeight * 0.28f).toInt()
        }
        windowManager.updateViewLayout(this, windowParams)
    }

    private fun scheduleControlsAutoHide() {
        handler.removeCallbacks(autoHideControlsRunnable)
        handler.postDelayed(autoHideControlsRunnable, 3500L)
    }

    private fun createCardBackground(opacity: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(16).toFloat()
            val alpha = (opacity * 255).toInt().coerceIn(0, 255)
            setColor(Color.argb(alpha, 13, 17, 23)) // Dark slate
            setStroke(dp(1), Color.argb(80, 94, 234, 212)) // Subtle teal border
        }
    }

    private fun createRoundedDrawable(color: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(color)
        }
    }

    private fun createHeaderIconButton(drawableRes: Int, onClick: () -> Unit): ImageView {
        return ImageView(context).apply {
            setImageResource(drawableRes)
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                marginStart = dp(6)
            }
            setColorFilter(Color.parseColor("#94A3B8"))
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setOnClickListener { onClick() }
        }
    }

    private fun createHeaderPillButton(label: String, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(6), dp(2), dp(6), dp(2))
            background = createRoundedDrawable(Color.parseColor("#1E293B"), dp(6))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(6)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun createControlButton(text: String, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = createRoundedDrawable(Color.parseColor("#1F2937"), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(4)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    fun onDestroy() {
        scrollJob?.cancel()
        handler.removeCallbacks(autoHideControlsRunnable)
    }
}
