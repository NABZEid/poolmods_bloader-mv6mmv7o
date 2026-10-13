package com.poolmods.bloader

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(48, 48, 48, 48)
        }
        root.addView(ImageView(this).apply { setImageResource(R.drawable.logo_bubble) },
            LinearLayout.LayoutParams(360, 360))
        root.addView(TextView(this).apply {
            text = "Bloader - pendamping Villiard\nBuka game Villiard dulu, lalu tekan Mulai."
            setTextColor(Color.WHITE); gravity = Gravity.CENTER; setPadding(0, 32, 0, 32)
        })
        root.addView(Button(this).apply {
            text = "Mulai Overlay"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                    Toast.makeText(this@MainActivity, "Izinkan 'Tampil di atas aplikasi lain', lalu tekan Mulai lagi", Toast.LENGTH_LONG).show()
                } else {
                    startService(Intent(this@MainActivity, OverlayService::class.java))
                    finish()
                }
            }
        })
        root.addView(Button(this).apply {
            text = "Stop Overlay"
            setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) }
        })
        setContentView(root)
    }
}
