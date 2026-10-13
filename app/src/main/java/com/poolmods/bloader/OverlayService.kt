package com.poolmods.bloader

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.*
import kotlin.concurrent.thread

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var bubble: ImageView
    private lateinit var panel: LinearLayout
    private lateinit var status: TextView
    private val ui = Handler(Looper.getMainLooper())
    private lateinit var bubbleLp: WindowManager.LayoutParams
    private lateinit var panelLp: WindowManager.LayoutParams

    private fun type() = if (Build.VERSION.SDK_INT >= 26)
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

    private fun lp(x: Int, y: Int) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        type(), WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x; this.y = y }

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        buildBubble(); buildPanel()
        wm.addView(bubble, bubbleLp)
    }

    private fun buildBubble() {
        bubble = ImageView(this).apply { setImageResource(R.drawable.logo_bubble) }
        bubbleLp = lp(20, 300).also { it.width = 140; it.height = 140 }
        var sx = 0; var sy = 0; var tx = 0f; var ty = 0f; var moved = false
        bubble.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = bubbleLp.x; sy = bubbleLp.y; tx = e.rawX; ty = e.rawY; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - tx).toInt(); val dy = (e.rawY - ty).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    bubbleLp.x = sx + dx; bubbleLp.y = sy + dy
                    wm.updateViewLayout(bubble, bubbleLp)
                }
                MotionEvent.ACTION_UP -> if (!moved) togglePanel()
            }
            true
        }
    }

    private var open = false
    private fun togglePanel() {
        open = !open
        if (open) {
            panelLp.x = bubbleLp.x + 150; panelLp.y = bubbleLp.y
            wm.addView(panel, panelLp); refresh()
        } else wm.removeView(panel)
    }

    private val switches = HashMap<String, Switch>()
    private fun buildPanel() {
        panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            background = GradientDrawable().apply { setColor(Color.parseColor("#E6071A33")); cornerRadius = 28f; setStroke(3, Color.parseColor("#1E90FF")) }
        }
        panelLp = lp(0, 0)
        status = TextView(this).apply { setTextColor(Color.WHITE); textSize = 12f }
        panel.addView(status)
        fun toggle(label: String, key: String) {
            val s = Switch(this).apply {
                text = label; setTextColor(Color.WHITE)
                setOnCheckedChangeListener { v, on -> if (v.isPressed) thread { Api.set(key, on); ui.post { refresh() } } }
            }
            switches[key] = s; panel.addView(s)
        }
        toggle("Autoplay", "autoplay")
        toggle("Auto Queue", "autoQueue")
        toggle("Long Line", "longLine")
        toggle("Aimbot (pantulan)", "autoBounce")
        fun btn(label: String, times: Int) = panel.addView(Button(this).apply {
            text = label
            setOnClickListener { thread { val r = Api.inject(times); ui.post { refresh(r) } } }
        })
        btn("Inject Coins 1M", 1)
        btn("Inject Coins 2M", 2)
    }

    private fun refresh(known: org.json.JSONObject? = null) = thread {
        val s = known ?: Api.state()
        ui.post {
            if (s == null) { status.text = "Villiard belum terbuka"; return@post }
            status.text = "Villiard terhubung | Coins: ${s.optInt("coins")}"
            for ((k, sw) in switches) { sw.isPressed = false; sw.isChecked = s.optBoolean(k) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { wm.removeView(bubble) } catch (_: Exception) {}
        if (open) try { wm.removeView(panel) } catch (_: Exception) {}
    }
}
