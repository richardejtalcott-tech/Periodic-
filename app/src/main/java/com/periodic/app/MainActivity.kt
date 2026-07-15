package com.periodic.app

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.Choreographer
import android.view.Gravity
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.filament.Skybox
import com.google.android.filament.LightManager
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

class MainActivity : Activity() {
    companion object { init { Utils.init() } }
    private lateinit var surfaceView: SurfaceView
    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private var keyLight: Int = 0
    private var fillLight: Int = 0
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.render(frameTimeNanos)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val root = FrameLayout(this)
        surfaceView = SurfaceView(this)
        root.addView(surfaceView, FrameLayout.LayoutParams(-1,-1))
        modelViewer = ModelViewer(surfaceView)
        modelViewer.scene.skybox = Skybox.Builder().color(0.006f,0.010f,0.016f,1f).build(modelViewer.engine)
        modelViewer.view.isPostProcessingEnabled = true
        modelViewer.camera.setExposure(12f, 1f/60f, 100f)
        val env = assets.open("scientific_lab.glb").use { ByteBuffer.wrap(it.readBytes()) }
        modelViewer.loadModelGlb(env)
        modelViewer.transformToUnitCube()
        keyLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.78f, 0.90f, 1.0f).intensity(85000f)
            .direction(-0.45f, -0.75f, -0.55f).castShadows(true)
            .build(modelViewer.engine, keyLight)
        modelViewer.scene.addEntity(keyLight)
        fillLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(0.40f, 0.72f, 0.78f).intensity(26000f)
            .direction(0.55f, 0.15f, -0.35f).castShadows(false)
            .build(modelViewer.engine, fillLight)
        modelViewer.scene.addEntity(fillLight)

        val table = PeriodicTableView(this) { number ->
            val i = Intent(this, ElementDetailActivity::class.java)
            i.putExtra("atomicNumber", number)
            startActivity(i)
            overridePendingTransition(R.anim.exhibit_enter, R.anim.exhibit_exit)
        }
        root.addView(table, FrameLayout.LayoutParams(-1,-1))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20,8,20,8)
            setBackgroundColor(Color.argb(90,4,9,15))
        }
        brand.addView(TextView(this).apply {
            text="PERIODIC"; textSize=19f; setTextColor(Color.rgb(228,239,248)); letterSpacing=.12f
            setShadowLayer(12f,0f,0f,Color.rgb(91,176,228))
        })
        brand.addView(TextView(this).apply {
            text="   matter, revealed"; textSize=11f; setTextColor(Color.rgb(155,188,205)); letterSpacing=.08f
        })
        val lp=FrameLayout.LayoutParams(-2,-2,Gravity.TOP or Gravity.CENTER_HORIZONTAL); lp.topMargin=10
        root.addView(brand,lp)
        setContentView(root)
        choreographer=Choreographer.getInstance()
    }
    override fun onResume(){ super.onResume(); choreographer.postFrameCallback(frameCallback) }
    override fun onPause(){ choreographer.removeFrameCallback(frameCallback); super.onPause() }
    override fun onDestroy(){
        choreographer.removeFrameCallback(frameCallback)
        modelViewer.scene.removeEntity(keyLight); modelViewer.scene.removeEntity(fillLight)
        modelViewer.engine.destroyEntity(keyLight); modelViewer.engine.destroyEntity(fillLight)
        EntityManager.get().destroy(keyLight); EntityManager.get().destroy(fillLight)
        modelViewer.destroyModel(); modelViewer.scene.skybox?.let{modelViewer.engine.destroySkybox(it)}
        super.onDestroy()
    }
}
