package myapp.ui.player

import android.animation.Animator
import android.annotation.TargetApi
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.ViewStubCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import myapp.ui.player.vlc.AndroidDevices
import myapp.ui.player.vlc.view.PlayerProgress
import org.videolan.libvlc.util.AndroidUtil

class VideoPlayerOverlayDelegate(private val playerActivity: LivePlayerActivity) {

    private lateinit var playerOverlayBrightness: ConstraintLayout
    private lateinit var brightnessValueText: TextView
    private lateinit var playerBrightnessProgress: PlayerProgress
    private lateinit var playerOverlayVolume: ConstraintLayout
    private lateinit var volumeValueText: TextView
    private lateinit var playerVolumeProgress: PlayerProgress
    var info: TextView? = null
    var overlayInfo: View? = null
    lateinit var playerUiContainer: RelativeLayout
    private var overlayTimeout = 0
    private var wasPlaying = true
    private lateinit var abRepeatAddMarker: Button
    private var seekButtons: Boolean = false


    private var orientationLockedBeforeLock: Boolean = false
    lateinit var playlistSearchText: TextInputLayout

    fun showInfo(@StringRes textId: Int, duration: Int) {
        showInfo(playerActivity.getString(textId), duration)
    }

    private fun initOverlay() {
    }

    /**
     * Show text in the info view for "duration" milliseconds
     * @param text
     * @param duration
     */
    fun showInfo(text: String, duration: Int) {
        if (playerActivity.isInPictureInPictureMode) return
        overlayInfo?.isVisible = true
        info?.isVisible = true
        info?.text = text
        playerActivity.handler.removeMessages(LivePlayerActivity.FADE_OUT_INFO)
        playerActivity.handler.sendEmptyMessageDelayed(LivePlayerActivity.FADE_OUT_INFO, duration.toLong())
    }

    fun fadeOutBrightness() {
        this.fadeOutInfo(playerActivity.mBind.root.findViewById(R.id.player_overlay_brightness))
    }

    fun fadeOutVolume() {
        this.fadeOutInfo(playerActivity.mBind.root.findViewById(R.id.player_overlay_volume))
    }


    fun fadeOutInfo(view: View?) {
        if (view?.visibility == View.VISIBLE) {
            view.startAnimation(AnimationUtils.loadAnimation(playerActivity, android.R.anim.fade_out))
            view.isInvisible = true
        }
    }


    /**
     * Show the brightness value with  bar
     * @param brightness the brightness value
     */
    fun showBrightnessBar(brightness: Int) {
        playerActivity.handler.sendEmptyMessage(LivePlayerActivity.FADE_OUT_VOLUME_INFO)
        playerActivity.findViewById<ViewStubCompat>(R.id.player_brightness_stub)?.isVisible = true
        playerOverlayBrightness = playerActivity.findViewById(R.id.player_overlay_brightness)
        brightnessValueText = playerActivity.findViewById(R.id.brightness_value_text)
        playerBrightnessProgress = playerActivity.findViewById(R.id.playerBrightnessProgress)
        playerOverlayBrightness.isVisible = true
        brightnessValueText.text = "$brightness%"
        playerBrightnessProgress.setValue(brightness)
        playerOverlayBrightness.isVisible = true
        playerActivity.handler.removeMessages(LivePlayerActivity.FADE_OUT_BRIGHTNESS_INFO)
        playerActivity.handler.sendEmptyMessageDelayed(LivePlayerActivity.FADE_OUT_BRIGHTNESS_INFO, 1000L)
        dimStatusBar(true)
    }

    /**
     * Show the volume value with  bar
     * @param volume the volume value
     */
    fun showVolumeBar(volume: Int, fromTouch: Boolean) {
        playerActivity.handler.sendEmptyMessage(LivePlayerActivity.FADE_OUT_BRIGHTNESS_INFO)
        playerActivity.findViewById<ViewStubCompat>(R.id.player_volume_stub)?.isVisible = true
        playerOverlayVolume = playerActivity.findViewById(R.id.player_overlay_volume)
        volumeValueText = playerActivity.findViewById(R.id.volume_value_text)
        playerVolumeProgress = playerActivity.findViewById(R.id.playerVolumeProgress)
        volumeValueText.text = "$volume%"
        playerVolumeProgress.isDouble = playerActivity.isAudioBoostEnabled
        playerVolumeProgress.setValue(volume)
        playerOverlayVolume.isVisible = true
        playerActivity.handler.removeMessages(LivePlayerActivity.FADE_OUT_VOLUME_INFO)
        playerActivity.handler.sendEmptyMessageDelayed(LivePlayerActivity.FADE_OUT_VOLUME_INFO, 1000L)
        dimStatusBar(true)
    }

    /**
     * Dim the status bar and/or navigation icons when needed on Android 3.x.
     * Hide it on Android 4.0 and later
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun dimStatusBar(dim: Boolean) {
        var visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        var navbar = 0
        if (dim || playerActivity.isLocked) {
            playerActivity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            navbar =
                navbar or (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            if (AndroidUtil.isKitKatOrLater) visibility = visibility or View.SYSTEM_UI_FLAG_IMMERSIVE
            visibility = visibility or View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            playerActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            visibility = visibility or View.SYSTEM_UI_FLAG_VISIBLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }

        playerUiContainer.setPadding(0, 0, 0, 0)
        playerUiContainer.fitsSystemWindows = !playerActivity.isLocked

        if (AndroidDevices.hasNavBar)
            visibility = visibility or navbar
        playerActivity.window.decorView.systemUiVisibility = visibility
    }

    /**
     * show overlay
     * @param forceCheck: adjust the timeout in function of playing state
     */
    fun showOverlay(forceCheck: Boolean = false) {
        if (forceCheck) overlayTimeout = 0
        showOverlayTimeout(0)
    }

    /**
     * show overlay
     */
    fun showOverlayTimeout(timeout: Int) {
        initOverlay()
        overlayTimeout = when {
            timeout != 0 -> timeout
            else -> LivePlayerActivity.OVERLAY_INFINITE
        }
        if (!playerActivity.isShowing) {
            playerActivity.isShowing = true
            dimStatusBar(false)
        }
        playerActivity.handler.removeMessages(LivePlayerActivity.FADE_OUT)
        if (overlayTimeout != LivePlayerActivity.OVERLAY_INFINITE) {
            playerActivity.handler.sendMessageDelayed(
                playerActivity.handler.obtainMessage(LivePlayerActivity.FADE_OUT),
                overlayTimeout.toLong()
            )
        }
    }

    fun updateOrientationIcon() {
//        val drawable = if (!player.orientationMode.locked) {
//            R.drawable.ic_player_rotate
//        } else if (player.orientationMode.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || player.orientationMode.orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || player.orientationMode.orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
//            R.drawable.ic_player_lock_landscape
//        } else {
//            R.drawable.ic_player_lock_portrait
//        }
//        hudBinding.orientationToggle.setImageDrawable(ContextCompat.getDrawable(player, drawable))
    }

    private fun enterAnimate(views: Array<View?>, translationStart: Float) = views.forEach { view ->
        view?.isVisible = true
        view?.alpha = 0f
        view?.translationY = translationStart
        view?.animate()?.alpha(1F)?.translationY(0F)?.setDuration(150L)?.setListener(null)
    }

    private fun exitAnimate(views: Array<View?>, translationEnd: Float) = views.forEach { view ->
        view?.animate()?.alpha(0F)?.translationY(translationEnd)?.setDuration(150L)
            ?.setListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    view.isInvisible = true
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
            })
    }


    /**
     * hider overlay
     */
    fun hideOverlay(fromUser: Boolean) {
        if (playerActivity.isShowing) {
            playerActivity.handler.removeMessages(LivePlayerActivity.FADE_OUT)
            playerActivity.isShowing = false
            dimStatusBar(true)
            playlistSearchText.editText?.setText("")
        } else if (!fromUser) {
            /*
             * Try to hide the Nav Bar again.
             * It seems that you can't hide the Nav Bar if you previously
             * showed it in the last 1-2 seconds.
             */
            dimStatusBar(true)
        }
    }


    fun toggleOverlay() {
        if (!playerActivity.isShowing) showOverlay()
        else hideOverlay(true)
    }

    /**
     * Lock player
     */
    fun lockScreen() {
        orientationLockedBeforeLock = playerActivity.orientationMode.locked
        if (!playerActivity.orientationMode.locked) playerActivity.toggleOrientation()
        hideOverlay(true)
        playerActivity.lockBackButton = true
        playerActivity.isLocked = true
    }

    /**
     * Remove player lock
     */
    fun unlockScreen() {
        playerActivity.orientationMode.locked = orientationLockedBeforeLock
        playerActivity.requestedOrientation = playerActivity.getScreenOrientation(playerActivity.orientationMode)
        playerActivity.isShowing = false
        playerActivity.isLocked = false
        showOverlay()
        playerActivity.lockBackButton = false
    }


}
