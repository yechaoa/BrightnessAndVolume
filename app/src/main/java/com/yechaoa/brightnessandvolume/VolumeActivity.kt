package com.yechaoa.brightnessandvolume

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_volume.*

class VolumeActivity : AppCompatActivity() {

    //音频管理器
    private lateinit var mAudioManager: AudioManager

    //当前音量
    private var mCurrentVolume: Int = 0

    //最大音量
    private var mMaxVolume: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volume)

        init()
        setListener()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        mAudioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        /**
         * ringerMode 音量模式
         * RINGER_MODE_NORMAL 正常
         * RINGER_MODE_SILENT 静音
         * RINGER_MODE_VIBRATE 震动
         */
        when (mAudioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> tv_mode.text = "当前音量模式：正常"
            AudioManager.RINGER_MODE_SILENT -> tv_mode.text = "当前音量模式：静音"
            AudioManager.RINGER_MODE_VIBRATE -> tv_mode.text = "当前音量模式：震动"
        }

        /**
         * 获取系统媒体音量
         * STREAM_VOICE_CALL 通话
         * STREAM_SYSTEM 系统
         * STREAM_RING 铃声
         * STREAM_MUSIC 媒体音量
         * STREAM_ALARM 闹钟
         * STREAM_NOTIFICATION 通知
         */
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        //获取媒体音量最大值
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        seekBar.max = mMaxVolume
        tv_max.text = "最大音量：$mMaxVolume"

        updateNum(mCurrentVolume)
    }

    private fun setListener() {

        btn_mode.setOnClickListener {
            mAudioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            tv_mode.text = "当前音量模式：正常"
        }

        btn_add.setOnClickListener {
            if (mCurrentVolume < mMaxVolume) {
                mCurrentVolume++
            } else {
                mCurrentVolume = mMaxVolume
            }
            updateNum(mCurrentVolume)
            setStreamVolume(mCurrentVolume)
        }

        btn_reduce.setOnClickListener {
            if (mCurrentVolume > 0) {
                mCurrentVolume--
            } else {
                mCurrentVolume = 0
            }
            updateNum(mCurrentVolume)
            setStreamVolume(mCurrentVolume)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i("onProgressChanged----", "" + progress)
                mCurrentVolume = progress
                updateNum(mCurrentVolume)
                setStreamVolume(mCurrentVolume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    /**
     * 更新页面显示
     */
    private fun updateNum(volume: Int) {
        tv_volume.text = volume.toString()
        seekBar.progress = volume
    }

    /**
     * 设置系统媒体音量
     * setStreamVolume 直接设置音量
     * adjustStreamVolume 步长式设置音量，即10,20,30这样阶梯式
     *
     * 参数1：音量类型
     * 参数2：音量数值
     * 参数3：
     *      AudioManager.FLAG_SHOW_UI 调整音量时显示系统音量进度条 , 0 则不显示
     *      AudioManager.FLAG_ALLOW_RINGER_MODES 是否铃声模式
     *      AudioManager.FLAG_VIBRATE 是否震动模式
     *      AudioManager.FLAG_SHOW_VIBRATE_HINT 震动提示
     *      AudioManager.FLAG_SHOW_SILENT_HINT 静音提示
     *      AudioManager.FLAG_PLAY_SOUND 调整音量时播放声音
     */
    private fun setStreamVolume(volume: Int) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * 音量逐渐递增
     *
     * 参数1：音量类型
     * 参数2：音量调整方向
     *      AudioManager.ADJUST_RAISE 音量逐渐递增，
     *      AudioManager.ADJUST_LOWER 音量逐渐递减
     *      AudioManager.ADJUST_SAME 不变
     * 参数3：AudioManager.FLAG_SHOW_UI 调整音量时显示系统音量进度条，0 则不显示
     */
    private fun adjustRaise() {
        mAudioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    /**
     * 音量逐渐递减
     */
    private fun adjustLower(volume: Int) {
        mAudioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }

    /**
     * 监听并接管系统的音量按键，
     * 注意：最好保持原有逻辑不变
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            //音量+按键
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (mCurrentVolume < mMaxVolume) {
                    mCurrentVolume++
                } else {
                    mCurrentVolume = mMaxVolume
                }
                updateNum(mCurrentVolume)
                setStreamVolume(mCurrentVolume)
                return true
            }
            //音量-按键
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (mCurrentVolume > 0) {
                    mCurrentVolume--
                } else {
                    mCurrentVolume = 0
                }
                updateNum(mCurrentVolume)
                setStreamVolume(mCurrentVolume)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}