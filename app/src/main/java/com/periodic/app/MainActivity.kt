package com.periodic.app

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.filament.Skybox
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot

class MainActivity : Activity() {
    companion object {
        init { Utils.init() }
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private lateinit var hud: PeriodicHudView

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.render(frameTimeNanos)
            hud.invalidate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val root = FrameLayout(this)
        surfaceView = SurfaceView(this)
        root.addView(surfaceView, FrameLayout.LayoutParams(-1, -1))

        modelViewer = ModelViewer(surfaceView)
        modelViewer.scene.skybox = Skybox.Builder()
            .color(0.004f, 0.008f, 0.025f, 1.0f)
            .build(modelViewer.engine)
        modelViewer.view.isPostProcessingEnabled = true
        modelViewer.camera.setExposure(16.0f, 1.0f / 60.0f, 100.0f)

        val glb = assets.open("periodic_table.glb").use { ByteBuffer.wrap(it.readBytes()) }
        modelViewer.loadModelGlb(glb)
        modelViewer.transformToUnitCube()

        hud = PeriodicHudView(this) { atomicNumber ->
            val intent = Intent(this, ElementDetailActivity::class.java)
            intent.putExtra("atomicNumber", atomicNumber)
            startActivity(intent)
        }
        root.addView(hud, FrameLayout.LayoutParams(-1, -1))

        val title = TextView(this).apply {
            text = "PERIODIC  •  FILAMENT 3D"
            setTextColor(Color.rgb(223, 210, 164))
            textSize = 16f
            setShadowLayer(10f, 0f, 0f, Color.rgb(70, 130, 220))
            gravity = Gravity.CENTER
            setPadding(22, 10, 22, 10)
            setBackgroundColor(Color.argb(120, 4, 8, 22))
        }
        val titleLp = FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        titleLp.topMargin = 20
        root.addView(title, titleLp)

        setContentView(root)
        choreographer = Choreographer.getInstance()
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
        super.onPause()
    }

    override fun onDestroy() {
        choreographer.removeFrameCallback(frameCallback)
        modelViewer.destroyModel()
        modelViewer.scene.skybox?.let { modelViewer.engine.destroySkybox(it) }
        super.onDestroy()
    }

    private inner class PeriodicHudView(
        context: android.content.Context,
        private val onElement: (Int) -> Unit
    ) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var downX = 0f
        private var downY = 0f
        private var moved = false
        private var lastTap = 0L
        private val legend = listOf(
            "ALKALI" to Color.rgb(155,70,52), "TRANSITION" to Color.rgb(86,111,132),
            "NONMETAL" to Color.rgb(71,113,148), "HALOGEN" to Color.rgb(52,126,92),
            "NOBLE" to Color.rgb(73,103,153), "LANTHANIDE" to Color.rgb(111,76,139),
            "ACTINIDE" to Color.rgb(133,66,82)
        )

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // Star cluster overlay adds depth while Filament renders the physical table.
            paint.style = Paint.Style.FILL
            for (i in 0 until 90) {
                val x = ((i * 97L + 31) % width.coerceAtLeast(1)).toFloat()
                val y = ((i * 53L + 17) % height.coerceAtLeast(1)).toFloat()
                val a = 45 + ((i * 29) % 120)
                paint.color = Color.argb(a, 170 + i % 70, 190 + i % 55, 255)
                canvas.drawCircle(x, y, if (i % 11 == 0) 2.3f else 1.1f, paint)
            }

            paint.color = Color.argb(155, 3, 7, 19)
            canvas.drawRoundRect(RectF(18f, height - 67f, width - 18f, height - 15f), 18f, 18f, paint)
            paint.textSize = 12f
            paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
            var x = 35f
            for ((label, color) in legend) {
                paint.color = color
                canvas.drawCircle(x, height - 42f, 7f, paint)
                paint.color = Color.rgb(220, 224, 232)
                canvas.drawText(label, x + 12f, height - 37f, paint)
                x += paint.measureText(label) + 41f
            }

            paint.textSize = 13f
            paint.color = Color.argb(225, 220, 226, 240)
            paint.typeface = android.graphics.Typeface.DEFAULT
            canvas.drawText("Drag to orbit • pinch to zoom • tap a tile from the front view • double-tap to reset", 25f, 38f, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            modelViewer.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x; downY = event.y; moved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (hypot(event.x - downX, event.y - downY) > 18f) moved = true
                }
                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    if (!moved && now - lastTap < 320) {
                        modelViewer.transformToUnitCube()
                        lastTap = 0L
                        return true
                    }
                    lastTap = now
                    if (!moved) hitTestFrontView(event.x, event.y)?.let(onElement)
                }
            }
            return true
        }

        private fun hitTestFrontView(px: Float, py: Float): Int? {
            // The GLB is framed to a unit cube. This mapping targets the default front view.
            val left = width * 0.08f
            val right = width * 0.92f
            val top = height * 0.18f
            val bottom = height * 0.84f
            if (px !in left..right || py !in top..bottom) return null
            val col = floor((px - left) / ((right - left) / 18f)).toInt() + 1
            val row = floor((py - top) / ((bottom - top) / 9f)).toInt() + 1
            return ElementData.ALL.firstOrNull { it.group == col && it.period == row }?.number
        }
    }
}
