package com.yechaoa.brightnessandvolume

import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_brightness.*
import kotlin.math.roundToInt


class BrightnessActivity : AppCompatActivity() {

    //当前屏幕亮度
    private var mScreenBrightness: Int = 0

    //每次加减的比例
    private var ratio: Int = 25

    private var mRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brightness)

        init()

        registerContentObserver()

        setListener()
    }

    private fun init() {
        mScreenBrightness = getScreenBrightness()
        updateNum(mScreenBrightness)
    }

    /**
     * 注册监听 系统屏幕亮度变化
     */
    private fun registerContentObserver() {
        this.contentResolver?.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            true,
            mBrightnessObserver
        )
    }

    /**
     * 监听系统亮度变化
     */
    private val mBrightnessObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            try {
                this@BrightnessActivity.contentResolver?.let {
                    mScreenBrightness = Settings.System.getInt(it, Settings.System.SCREEN_BRIGHTNESS)
                    updateNum(mScreenBrightness)
                    setWindowBrightness(mScreenBrightness)
                }
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销监听
        this.contentResolver?.unregisterContentObserver(mBrightnessObserver)
    }

    private fun setListener() {
        btn_add.setOnClickListener {
            if (mScreenBrightness < (255 - ratio)) {
                mScreenBrightness += ratio
            } else {
                mScreenBrightness = 255
            }
            setWindowBrightness(mScreenBrightness)
            updateNum(mScreenBrightness)
        }

        btn_reduce.setOnClickListener {
            if (mScreenBrightness > ratio) {
                mScreenBrightness -= ratio
            } else {
                mScreenBrightness = 1
            }
            setWindowBrightness(mScreenBrightness)
            updateNum(mScreenBrightness)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i("onProgressChanged----", "" + progress)
                mScreenBrightness = progress * ratio
                //判断是否有修改系统设置权限
                if (Settings.System.canWrite(this@BrightnessActivity)) {
                    setScreenBrightness(mScreenBrightness)
                    updateNum(mScreenBrightness)
                } else {
                    Toast.makeText(this@BrightnessActivity, "没有修改权限", Toast.LENGTH_SHORT).show()
                    // 打开允许修改系统设置权限的页面
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
                    startActivityForResult(intent, mRequestCode)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

    }

    /**
     * 处理返回结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mRequestCode) {
            if (Settings.System.canWrite(this@BrightnessActivity)) {
                setScreenBrightness(mScreenBrightness)
                updateNum(mScreenBrightness)
            } else {
                Toast.makeText(this@BrightnessActivity, "拒绝了权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 更新页面显示
     */
    private fun updateNum(mScreenBrightness: Int) {
        //转float 取四舍五入
        val i: Int = (mScreenBrightness / (ratio.toFloat())).roundToInt()
        tv_brightness.text = i.toString()
        seekBar.progress = i
    }

    /**
     * 获取系统屏幕亮度(0-255)
     */
    private fun getScreenBrightness(): Int {
        try {
            return Settings.System.getInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 设置系统屏幕亮度，影响所有页面和app
     * 注意：这种方式是需要手动权限的（android.permission.WRITE_SETTINGS）
     */
    private fun setScreenBrightness(brightness: Int) {
        try {
            //先检测调节模式
            setScreenManualMode()
            //再设置
            Settings.System.putInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 设置系统亮度调节模式(SCREEN_BRIGHTNESS_MODE)
     * SCREEN_BRIGHTNESS_MODE_MANUAL 手动调节
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC 自动调节
     */
    private fun setScreenManualMode() {
        try {
            //获取当前系统亮度调节模式
            val mode = Settings.System.getInt(this.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
            //如果是自动，则改为手动
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(
                    this.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            }
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 设置当前应用屏幕亮度，只当前有效
     */
    private fun setWindowBrightness(brightness: Int) {
        val window = window
        val lp = window.attributes
        lp.screenBrightness = brightness / 255.0f
        window.attributes = lp
    }

}