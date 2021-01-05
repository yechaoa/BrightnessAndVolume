# BrightnessAndVolume
修改系统亮度和音量

| <img src="/gif/brightness.gif" width="330"/> | <img src="/gif/volume.gif" width="330"/> | 
| :--: | :--: | 
| 亮度 | 音量 | 

# :collision: 亮度

修改系统`屏幕亮度`这种操作还是挺常见的，一般在`多媒体`开发中都多少会涉及到。

> emmm 效果图好像看不出来变化。。不过不是很重要。。

## 操作拆解
上图中可以看到，分别有`加减按钮`和`seekbar`来控制亮度。

后面会涉及到相关的事件。

## 获取系统屏幕亮度

```kotlin
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
```

注意，这里的返回值是`0-255`区间的。

定义两个参数：
- private var mScreenBrightness: Int = 0 //当前屏幕亮度
- private var ratio: Int = 25 //每次加减的比例

因为返回值最大是255，假设亮度调节是10档，每次加减1档大约是25，这个`精度`可以自己控制。

## 设置当前应用屏幕亮度，只当前有效
#### 加减按钮操作

```kotlin
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
```
如果设置亮度的值大于255了，不会报错，但是会回到`初始值`，所以加减操作的时候要判断一下最大值最小值。

接下来看一下核心方法`setWindowBrightness`：

```kotlin
    /**
     * 设置当前应用屏幕亮度，只当前有效
     */
    private fun setWindowBrightness(brightness: Int) {
        val window = window
        val lp = window.attributes
        lp.screenBrightness = brightness / 255.0f
        window.attributes = lp
    }
```

很简单，设置`window`的属性即可。
这个只会对当前页面有效，返回页面或退到后台，屏幕亮度都会`恢复`到初始值状态。

`updateNum`方法是更新页面显示：

```kotlin
    /**
     * 更新页面显示
     */
    private fun updateNum(mScreenBrightness: Int) {
        //转float 取四舍五入
        val i: Int = (mScreenBrightness / (ratio.toFloat())).roundToInt()
        tv_brightness.text = i.toString()
        seekBar.progress = i
    }
```

其实到这里，已经能满足大部分的需求了。

## 设置系统屏幕亮度，影响所有页面和app
前面讲到的其实是单页面的亮度设置，也可以修改系统的屏幕亮度，即影响所有的页面和app，一般不会有这种操作。
这也涉及到一个`高级`隐私权限，是否`允许修改系统设置`，且需要在app设置页面`手动授权`。

且需要先在`manifest`中添加：

```kotlin
    <!-- 修改系统屏幕亮度 -->
    <uses-permission
        android:name="android.permission.WRITE_SETTINGS"
        tools:ignore="ProtectedPermissions" />
```

这里分几个小步骤：
- 判断权限
- 有则修改亮度
- 无则引导授权

#### seekBar操作
```kotlin
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
```

用`Settings.System.canWrite`来判断是否已授权。

#### 已授权
看`setScreenBrightness`方法：

```kotlin
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
```
我们看到在设置之前，还有一步操作是先`检测调节模式`，因为如果当前亮度是自动调节的，需要改为手动才可以。

```kotlin
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
```
亮度调节模式
- SCREEN_BRIGHTNESS_MODE_MANUAL 手动调节
- SCREEN_BRIGHTNESS_MODE_AUTOMATIC 自动调节

#### 未授权
未授权的情况下，要提示并`引导`用户去授权

```kotlin
	Toast.makeText(this@BrightnessActivity, "没有修改权限", Toast.LENGTH_SHORT).show()
	// 打开允许修改系统设置权限的页面
	val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
	startActivityForResult(intent, mRequestCode)
```

同时，检测返回结果并处理即可

```kotlin
    /**
     * 处理返回结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mRequestCode) {
            if (Settings.System.canWrite(this@BrightnessActivity)) {
                setScreenBrightness(mScreenBrightness)
            } else {
                Toast.makeText(this@BrightnessActivity, "拒绝了权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

```


以上可以看到，不管是改模式还是改亮度，都是用的`Settings.System.putInt`方法，也就是修改了系统的设置，从而达到所有页面和app使用同一亮度的需求。

## 监听系统亮度变化
以上两种方式其实都是我们手动去改的，那如果用户自己去改变了亮度呢，我们页面理应也要做出相应的改变，所以，还需要去监听系统的亮度变化。

这里也分几个小步骤：
- 注册监听
- 处理变化
- 注销监听

#### 注册监听
```kotlin
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
```

#### 处理变化
```kotlin
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
```

#### 注销监听
```kotlin
    override fun onDestroy() {
        super.onDestroy()
        //注销监听
        this.contentResolver?.unregisterContentObserver(mBrightnessObserver)
    }
```

ok，至此关于`修改屏幕亮度`的讲解就全部结束了，如果对你有用，就点个赞吧^ - ^


# :collision: 音量

修改系统`音量`这种操作还是挺常见的，一般在`多媒体`开发中都多少会涉及到。


## 常用方法
#### 获取音频管理器

```kotlin
mAudioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
```

#### 获取媒体音量最大值

```kotlin
mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
```

#### 获取系统当前媒体音量

```kotlin
mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
```
这里涉及到几个常见`音量类型`：
- STREAM_VOICE_CALL 通话
- STREAM_SYSTEM 系统
- STREAM_RING 铃声
- STREAM_MUSIC 媒体音量
- STREAM_ALARM 闹钟
- STREAM_NOTIFICATION 通知

#### 获取系统音量模式

```kotlin
mAudioManager.ringerMode
```
音量模式：
- RINGER_MODE_NORMAL 正常
- RINGER_MODE_SILENT 静音
- RINGER_MODE_VIBRATE 震动

## 设置系统媒体音量
来看一下是如何修改音量的

```kotlin
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
```
> 注意，这里要判断一下是否超出了音量的最大值最小值。

在事件中，除了判断最大值最小值之外，还调用了两个方法

`updateNum`更新页面显示：

```kotlin
    /**
     * 更新页面显示
     */
    private fun updateNum(volume: Int) {
        tv_volume.text = volume.toString()
        seekBar.progress = volume
    }
```

还调用了`setStreamVolume`方法，这里就涉及到`setStreamVolume`和`adjustStreamVolume`的区别：
- setStreamVolume 直接设置音量，指哪打哪
- adjustStreamVolume 步长式设置音量，即10,20,30这样阶梯式

二者都可以设置音量，可以根据自己的业务需求来选择。

#### setStreamVolume
来看一下具体的`setStreamVolume `方法：

```kotlin
    private fun setStreamVolume(volume: Int) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI)
    }
```
- 参数1：音量类型
- 参数2：音量数值
- 参数3：
  - AudioManager.FLAG_SHOW_UI 调整音量时显示系统音量进度条 , 0 则不显示
  - AudioManager.FLAG_ALLOW_RINGER_MODES 是否铃声模式
  - AudioManager.FLAG_VIBRATE 是否震动模式
  - AudioManager.FLAG_SHOW_VIBRATE_HINT 震动提示
  - AudioManager.FLAG_SHOW_SILENT_HINT 静音提示
  - AudioManager.FLAG_PLAY_SOUND 调整音量时播放声音

#### adjustStreamVolume
###### 音量递增

```kotlin
    private fun adjustRaise() {
        mAudioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }
```
- 参数1：音量类型
- 参数2：音量调整方向
  - AudioManager.ADJUST_RAISE 音量逐渐递增
  - AudioManager.ADJUST_LOWER 音量逐渐递减
  - AudioManager.ADJUST_SAME 不变
- 参数3：同setStreamVolume参数3
###### 音量递减

```kotlin
    private fun adjustLower(volume: Int) {
        mAudioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }
```

> Github： [https://github.com/yechaoa/BrightnessAndVolume](https://github.com/yechaoa/BrightnessAndVolume)


## 监听音量控制按键
除了我们手动去改之外，用户也可以通过`物理按键`或是`耳机`来控制音量，这时，我们理应也要做出相应的改变，所以，还需要对音量按键做监听才行。

这里就用到熟悉的老方法了，重写`Activity`的`onKeyDown`方法：

```kotlin
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
```
其实很简单，拦截事件，然后执行我们的逻辑就行了。

## 总结
总的来说，代码量并不多，难度系数也不高，唯一要注意的是各个参数的类型，要根据自己的实际业务来选择即可。

ok，至此关于`修改音量`的讲解就全部结束了，如果对你有用，就点个赞吧^ - ^


<br>

```
   Copyright [2021] [yechaoa]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
