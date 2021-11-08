package myapp.ui.player

import android.animation.AnimatorSet
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import androidx.core.view.ScaleGestureDetectorCompat
import myapp.ui.player.vlc.AndroidDevices
import org.videolan.libvlc.MediaPlayer
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToInt

const val TOUCH_FLAG_AUDIO_VOLUME = 1
const val TOUCH_FLAG_BRIGHTNESS = 1 shl 1
const val TOUCH_FLAG_SEEK = 1 shl 2

//Touch Events
private const val TOUCH_NONE = 0
private const val TOUCH_VOLUME = 1
private const val TOUCH_BRIGHTNESS = 2
private const val TOUCH_MOVE = 3
private const val TOUCH_SEEK = 4
private const val TOUCH_IGNORE = 5
private const val MIN_FOV = 20f
private const val MAX_FOV = 150f

//stick event
private const val JOYSTICK_INPUT_DELAY = 300

class VideoTouchDelegate(
    private val playerActivity: LivePlayerActivity,
    private val touchControls: Int,
    var screenConfig: ScreenConfig
) {
    companion object {
        private const val SEEK_TIMEOUT = 750L
    }

    var handler = Handler(Looper.getMainLooper())

    var numberOfTaps = 0
    var lastTapTimeMs: Long = 0
    var touchDownMs: Long = 0
    private var touchAction = TOUCH_NONE
    private var initTouchY = 0f
    private var initTouchX = 0f
    private var touchY = -1f
    private var touchX = -1f
    private var verticalTouchActive = false

    private var lastMove: Long = 0

    //Seek
    private var nbTimesTaped = 0
    private var lastSeekWasForward = true
    private var seekAnimRunning = false
    private var animatorSet: AnimatorSet = AnimatorSet()
    var isShowing: Boolean = false
    var isShowingDialog: Boolean = false

    private val scaleGestureDetector by lazy(LazyThreadSafetyMode.NONE) {
        ScaleGestureDetector(playerActivity, mScaleListener).apply {
            ScaleGestureDetectorCompat.setQuickScaleEnabled(
                this,
                false
            )
        }
    }

    // Brightness
    private var isFirstBrightnessGesture = true

    fun onTouchEvent(event: MotionEvent): Boolean {// Mouse events for the core
        // Seek
// Seek (Right or Left move)
        // No volume/brightness action if coef < 2 or a secondary display is connected
        //TODO : Volume action when a secondary display is connected
// Mouse events for the core
        // Audio
        // Seek
        // Mouse events for the core
        // locked or swipe disabled, only handle show/hide & ignore all actions

        if (!playerActivity.isLocked) {
            scaleGestureDetector.onTouchEvent(event)
            if (scaleGestureDetector.isInProgress) {
                touchAction = TOUCH_IGNORE
                return true
            }
        }
        if (touchControls == 0 || playerActivity.isLocked) {
            // locked or swipe disabled, only handle show/hide & ignore all actions
            if (event.action == MotionEvent.ACTION_UP && touchAction != TOUCH_IGNORE) playerActivity.overlayDelegate.toggleOverlay()
            return false
        }

        val xChanged = if (touchX != -1f && touchY != -1f) event.x - touchX else 0f
        val yChanged = if (touchX != -1f && touchY != -1f) event.y - touchY else 0f

        // coef is the gradient's move to determine a neutral zone
        val coef = abs(yChanged / xChanged)
        val xgesturesize = xChanged / screenConfig.metrics.xdpi * 2.54f
        val deltaY = ((abs(initTouchY - event.y) / screenConfig.metrics.xdpi + 0.5f) * 2f).coerceAtLeast(1f)

        val (xTouch, yTouch) = try {
            Pair(event.x.roundToInt(), event.y.roundToInt())
        } catch (e: IllegalArgumentException) {
            return false
        }

        val now = System.currentTimeMillis()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownMs = now
                verticalTouchActive = false
                // Audio
                initTouchY = event.y
                initTouchX = event.x
                touchY = initTouchY
                playerActivity.initAudioVolume()
                touchAction = TOUCH_NONE
                // Seek
                touchX = event.x
                // Mouse events for the core
                playerActivity.sendMouseEvent(MotionEvent.ACTION_DOWN, xTouch, yTouch)
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchAction == TOUCH_IGNORE) return false
                // Mouse events for the core
                playerActivity.sendMouseEvent(MotionEvent.ACTION_MOVE, xTouch, yTouch)

                if (playerActivity.fov == 0f) {
                    // No volume/brightness action if coef < 2 or a secondary display is connected
                    //TODO : Volume action when a secondary display is connected
//                    if (touchAction != TOUCH_SEEK && coef > 2 && player.isOnPrimaryDisplay) {
                    if (touchAction != TOUCH_SEEK && coef > 2) {
                        if (!verticalTouchActive) {
                            if (abs(yChanged / screenConfig.yRange) >= 0.05) {
                                verticalTouchActive = true
                                touchY = event.y
                                touchX = event.x
                            }
                            return false
                        }
                        touchY = event.y
                        touchX = event.x
                        doVerticalTouchAction(yChanged)
                    } else if (initTouchX < screenConfig.metrics.widthPixels * 0.95) {
                        // Seek (Right or Left move)
                        // doSeekTouch(deltaY.roundToInt(), xgesturesize, false)
                    }
                } else {
                    touchY = event.y
                    touchX = event.x
                    touchAction = TOUCH_MOVE
                    val yaw = playerActivity.fov * -xChanged / screenConfig.xRange.toFloat()
                    val pitch = playerActivity.fov * -yChanged / screenConfig.xRange.toFloat()
                    playerActivity.updateViewpoint(yaw, pitch, 0f)
                }
            }
            MotionEvent.ACTION_UP -> {
                val touchSlop = ViewConfiguration.get(playerActivity).scaledTouchSlop
                if (touchAction == TOUCH_IGNORE) touchAction = TOUCH_NONE
                // Mouse events for the core
                playerActivity.sendMouseEvent(MotionEvent.ACTION_UP, xTouch, yTouch)
                touchX = -1f
                touchY = -1f
                // Seek
                if (touchAction == TOUCH_SEEK) {
                    // doSeekTouch(deltaY.roundToInt(), xgesturesize, true)
                    return true
                }
                // Vertical actions
                if (touchAction == TOUCH_VOLUME || touchAction == TOUCH_BRIGHTNESS) {
                    doVerticalTouchAction(yChanged)
                    return true
                }

                handler.removeCallbacksAndMessages(null)

                if (now - touchDownMs > ViewConfiguration.getDoubleTapTimeout()) {
                    //it was not a tap
                    numberOfTaps = 0
                    lastTapTimeMs = 0
                }

                //verify that the touch coordinate distance did not exceed the touchslop to increment the count tap
                if (abs(event.x - initTouchX) < touchSlop && abs(event.y - initTouchY) < touchSlop) {
                    if (numberOfTaps > 0 && now - lastTapTimeMs < ViewConfiguration.getDoubleTapTimeout()) {
                        numberOfTaps += 1
                    } else {
                        numberOfTaps = 1
                    }
                }

                lastTapTimeMs = now

                //handle multi taps
                if (numberOfTaps > 1 && !playerActivity.isLocked) {
                    if (touchControls and TOUCH_FLAG_SEEK == 0) {
                        playerActivity.doPlayPause()
                    } else {
                        val range =
                            (if (screenConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) screenConfig.xRange else screenConfig.yRange).toFloat()
                        if (BuildConfig.DEBUG) {
                            Timber.d("Landscape: ${screenConfig.orientation == Configuration.ORIENTATION_LANDSCAPE} range: $range eventx: ${event.x}")
                        }
                        when {
                            event.x < range / 4f -> {
                                // seekDelta(-10000)
                            }
                            event.x > range * 0.75 -> {
                                // seekDelta(10000)
                            }
                            else -> playerActivity.doPlayPause()
                        }
                    }
                }

                handler.postDelayed({
                    when (numberOfTaps) {
                        1 -> playerActivity.handler.sendEmptyMessage(if (playerActivity.isShowing) LivePlayerActivity.HIDE_INFO else LivePlayerActivity.SHOW_INFO)
                    }
                }, ViewConfiguration.getDoubleTapTimeout().toLong())
            }
        }
        return touchAction != TOUCH_NONE
    }

    fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (playerActivity.isLoading) return false
        //Check for a joystick event
        if (event.source and InputDevice.SOURCE_JOYSTICK != InputDevice.SOURCE_JOYSTICK || event.action != MotionEvent.ACTION_MOVE)
            return false

        val inputDevice = event.device

        val dpadx = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val dpady = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
        if (inputDevice == null || abs(dpadx) == 1.0f || abs(dpady) == 1.0f) return false

        val x = AndroidDevices.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_X)
        val y = AndroidDevices.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_Y)
        val rz = AndroidDevices.getCenteredAxis(event, inputDevice, MotionEvent.AXIS_RZ)

        if (System.currentTimeMillis() - lastMove > JOYSTICK_INPUT_DELAY) {
            when {
                abs(x) > 0.3 -> {
                    //seekDelta(if (x > 0.0f) 10000 else -10000)
                    //seekDelta(if (x > 0.0f) 10000 else -10000)
                }
                abs(y) > 0.3 -> {
                    if (isFirstBrightnessGesture)
                        initBrightnessTouch()
                    playerActivity.changeBrightness(-y / 10f)
                }
                abs(rz) > 0.3 -> {
                    playerActivity.volume = playerActivity.audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                    val delta = -(rz / 7 * playerActivity.audioMax).toInt()
                    val vol = (playerActivity.volume.toInt() + delta).coerceIn(0, playerActivity.audioMax)
                    playerActivity.setAudioVolume(vol, true)
                }
            }
            lastMove = System.currentTimeMillis()
        }
        return true
    }

    fun isSeeking() = touchAction == TOUCH_SEEK

    fun clearTouchAction() {
        touchAction = TOUCH_NONE
    }

    private fun doVerticalTouchAction(y_changed: Float) {
        val rightAction = touchX.toInt() > 4 * screenConfig.metrics.widthPixels / 7f
        val leftAction = !rightAction && touchX.toInt() < 3 * screenConfig.metrics.widthPixels / 7f
        if (!leftAction && !rightAction) return
        val audio = touchControls and TOUCH_FLAG_AUDIO_VOLUME != 0
        val brightness = touchControls and TOUCH_FLAG_BRIGHTNESS != 0
        if (!audio && !brightness)
            return
        if (rightAction) {
            if (audio) doVolumeTouch(y_changed)
            else doBrightnessTouch(y_changed)
        } else {
            if (brightness) doBrightnessTouch(y_changed)
            else doVolumeTouch(y_changed)
        }
        playerActivity.overlayDelegate.hideOverlay(true)
    }

    private fun doVolumeTouch(y_changed: Float) {
        if (touchAction != TOUCH_NONE && touchAction != TOUCH_VOLUME) return
        val audioMax = playerActivity.audioMax
        val delta = -(y_changed / screenConfig.yRange * audioMax * 1.25f)
        playerActivity.volume += delta
        val vol = playerActivity.volume.toInt().coerceIn(0, audioMax * if (playerActivity.isAudioBoostEnabled) 2 else 1)
        if (delta < 0) playerActivity.originalVol = vol.toFloat()
        if (delta != 0f) {
            if (vol > audioMax) {
                if (playerActivity.isAudioBoostEnabled) {
                    if (playerActivity.originalVol < audioMax) {
                        playerActivity.displayWarningToast()
                        playerActivity.setAudioVolume(audioMax, true)
                    } else {
                        playerActivity.setAudioVolume(vol, true)
                    }
                    touchAction = TOUCH_VOLUME
                }
            } else {
                playerActivity.setAudioVolume(vol, true)
                touchAction = TOUCH_VOLUME
            }
        }
    }

    private fun initBrightnessTouch() {
        val lp = playerActivity.window.attributes

        //Check if we already have a brightness
        val brightnesstemp = if (lp.screenBrightness != -1f)
            lp.screenBrightness
        else {
            //Check if the device is in auto mode
            val contentResolver = playerActivity.applicationContext.contentResolver
            if (Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            ) {
                //cannot retrieve a value -> 0.5
                0.5f
            } else Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128).toFloat() / 255
        }

        lp.screenBrightness = brightnesstemp
        playerActivity.window.attributes = lp
        isFirstBrightnessGesture = false
    }

    private fun doBrightnessTouch(ychanged: Float) {
        if (touchAction != TOUCH_NONE && touchAction != TOUCH_BRIGHTNESS) return
        if (isFirstBrightnessGesture) initBrightnessTouch()
        touchAction = TOUCH_BRIGHTNESS

        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        val delta = -ychanged / screenConfig.yRange * 1.25f

        playerActivity.changeBrightness(delta)
    }

    private val mScaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var savedScale: MediaPlayer.ScaleType? = null
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return screenConfig.xRange != 0 || playerActivity.fov == 0f
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (playerActivity.fov != 0f && !playerActivity.isLocked) {
                val diff = LivePlayerActivity.DEFAULT_FOV * (1 - detector.scaleFactor)
                if (playerActivity.updateViewpoint(0f, 0f, diff)) {
                    playerActivity.fov = (playerActivity.fov + diff).coerceIn(MIN_FOV, MAX_FOV)
                    return true
                }
            }
            return false
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            if (playerActivity.fov == 0f && !playerActivity.isLocked) {
                val grow = detector.scaleFactor > 1.0f
                if (grow && playerActivity.currentScaleType != MediaPlayer.ScaleType.SURFACE_FIT_SCREEN) {
                    savedScale = playerActivity.currentScaleType
                    playerActivity.setVideoScale(MediaPlayer.ScaleType.SURFACE_FIT_SCREEN)
                } else if (!grow && savedScale != null) {
                    playerActivity.setVideoScale(savedScale!!)
                    savedScale = null
                } else if (!grow && playerActivity.currentScaleType == MediaPlayer.ScaleType.SURFACE_FIT_SCREEN) {
                    playerActivity.setVideoScale(MediaPlayer.ScaleType.SURFACE_BEST_FIT)
                }
            }
        }
    }
}

data class ScreenConfig(val metrics: DisplayMetrics, val xRange: Int, val yRange: Int, val orientation: Int)
