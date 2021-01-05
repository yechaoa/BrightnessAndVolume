package com.yechaoa.brightnessandvolume

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_brightness.setOnClickListener {
            startActivity(Intent(this, BrightnessActivity::class.java))
        }

        btn_volume.setOnClickListener {
            startActivity(Intent(this, VolumeActivity::class.java))
        }
    }
}