package myapp.ui.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.support.v4.media.session.PlaybackStateCompat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.Surface
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import myapp.extensions.dp
import myapp.extensions.extraNotNull
import myapp.ui.common.databinding.contentView
import myapp.ui.player.databinding.ActivityLivePlayerBinding
import myapp.ui.player.vlc.*
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.AndroidUtil
import splitties.init.appCtx
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class LivePlayerActivity : AppCompatActivity(), MediaPlayerEventListener {

    lateinit var settings: SharedPreferences

    val playerController by lazy { PlayerController(appCtx) }
    private val mArgVideoUri by extraNotNull<Uri>(EXTRA_URI)


    @Inject
    internal lateinit var vmFactory: LivePlayerViewModel.Factory

    private val mViewModel: LivePlayerViewModel by viewModels {
        LivePlayerViewModel.provideFactory(vmFactory, mArgVideoUri)
    }

    val mBind: ActivityLivePlayerBinding by contentView(R.layout.activity_live_player)

    var isLocked = false
    var lockBackButton = false
    lateinit var orientationMode: PlayerOrientationMode
    private var warningToast: Toast? = null
    var isShowing: Boolean = false
    private var wasPaused = false

    //Volume
    internal lateinit var audiomanager: AudioManager
        private set
    internal var audioMax: Int = 0
        private set
    internal var isAudioBoostEnabled: Boolean = false
        private set
    private var isPlaying = false
    var isLoading = false
    private var isDragging: Boolean = false

    private var isMute = false
    private var volSave: Int = 0
    internal var volume: Float = 0.toFloat()
    internal var originalVol: Float = 0.toFloat()

    internal var fov: Float = 0.toFloat()
    lateinit var touchDelegate: VideoTouchDelegate
    val overlayDelegate by lazy { VideoPlayerOverlayDelegate(this@LivePlayerActivity) }

    val currentScaleType: MediaPlayer.ScaleType
        get() = playerController.mediaplayer.videoScale

    /**
     * Flag to indicate whether the media should be paused once loaded
     * (e.g. lock screen, or to restore the pause state)
     */
    private var playbackStarted = false

    private val screenRotation: Int
        get() {
            val wm = applicationContext.getSystemService<WindowManager>()!!
            return wm.defaultDisplay?.rotation ?: Surface.ROTATION_0
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind.vm = mViewModel
        settings = Settings.getInstance(this)

        /* Services and miscellaneous */
        audiomanager = applicationContext.getSystemService<AudioManager>()!!
        audioMax = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        isAudioBoostEnabled = settings.getBoolean("audio_boost", true)
        val screenOrientationSetting =
            Integer.valueOf(settings.getString(SCREEN_ORIENTATION, "99" /*SCREEN ORIENTATION SENSOR*/)!!)
        val sensor = settings.getBoolean(LOCK_USE_SENSOR, true)
        orientationMode = when (screenOrientationSetting) {
            99 -> PlayerOrientationMode(false)
            101 -> PlayerOrientationMode(
                true,
                if (sensor) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            )
            102 -> PlayerOrientationMode(
                true,
                if (sensor) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
            103 -> PlayerOrientationMode(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
            98 -> PlayerOrientationMode(
                true,
                settings.getInt(LAST_LOCK_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            )
            else -> PlayerOrientationMode(true, getOrientationForLock())
        }

        // create touch delegate
        val touch = if (true) {
            val audioTouch = (!AndroidUtil.isLolliPopOrLater || !audiomanager.isVolumeFixed)
                && settings.getBoolean(ENABLE_VOLUME_GESTURE, true)

            val brightnessTouch = settings.getBoolean(ENABLE_BRIGHTNESS_GESTURE, true)
            val v1 = if (audioTouch) TOUCH_FLAG_AUDIO_VOLUME else 0
            val v2 = if (brightnessTouch) TOUCH_FLAG_BRIGHTNESS else 0
            val v3 = if (settings.getBoolean(ENABLE_DOUBLE_TAP_SEEK, true)) TOUCH_FLAG_SEEK else 0
            v1 + v2 + v3
        } else 0


        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val yRange = dm.widthPixels.coerceAtMost(dm.heightPixels)
        val xRange = dm.widthPixels.coerceAtLeast(dm.heightPixels)
        val sc = ScreenConfig(dm, xRange, yRange, resources.configuration.orientation)
        touchDelegate = VideoTouchDelegate(this, touch, sc)
        overlayDelegate.playerUiContainer = findViewById(R.id.player_ui_container)
        customInit()
        setupEvents()
        handler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY.toLong())
    }

    private fun customInit() {
        initPlayerView()
    }

    private fun setupEvents() {
        playerController.playbackState.asLiveData().observe(this, Observer { state ->
            if (state == PlaybackStateCompat.STATE_PLAYING) {

            }
            Timber.d("PLAYBACK_STATE = ${state}")
        })
    }

    private fun initPlayerView() {
        val libVLC = VLCInstance.getInstance(appCtx)
        val videoUri = mViewModel.videoUri

        Timber.d("XXX LIBVLC=${libVLC}")
//        val mediaPlayer = MediaPlayer(libVLC)
//        mediaPlayer.attachViews(mBind.videoLayout, null, false, false)
        playerController.mediaplayer.attachViews(mBind.videoLayout, null, false, true)

        lifecycleScope.launchWhenCreated {
            val media = Media(libVLC, videoUri).apply {
                setHWDecoderEnabled(true, false);
//                addOption(":network-caching=0");
//                addOption(":clock-jitter=0");
//                addOption(":clock-synchro=0");
                addOption(":rtsp-tcp");
            }

            playerController.startPlayback(media, thisActivity, 0L)
        }
    }

    override suspend fun onEvent(event: MediaPlayer.Event) {
        when (event.type) {
            MediaPlayer.Event.Playing -> {
                Timber.d("XXXY onEvent Playback = MediaPlayer.Event.Playing")
                onPlaying()
            }
            MediaPlayer.Event.Paused -> {
                Timber.d("XXXY onEvent Playback = MediaPlayer.Event.Paused")
                // overlayDelegate.updateOverlayPausePlay()
            }
            MediaPlayer.Event.EncounteredError -> {
                Timber.d("XXXY onEvent = MediaPlayer.Event.EncounteredError")
            }
            MediaPlayer.Event.PausableChanged -> {
                Timber.d("XXXY onEvent = MediaPlayer.Event.PausableChanged = ${event.pausable}")
            }
            MediaPlayer.Event.SeekableChanged -> {
                Timber.d("XXXY onEvent = MediaPlayer.Event.SeekableChanged = ${event.seekable}")
            }
            MediaPlayer.Event.LengthChanged -> {
                // updateProgress(newLength = event.lengthChanged)
                Timber.d("XXXY onEvent = MediaPlayer.Event.LengthChanged = ${event.lengthChanged}")
            }
            MediaPlayer.Event.TimeChanged -> {
                // ?????? ??????
                // Timber.d("XXXY onEvent = MediaPlayer.Event.TimeChanged = ${event.timeChanged}")
//                val time = event.timeChanged
//                if (abs(time - lastTime) > 950L) {
//                    updateProgress(newTime = time)
//                    lastTime = time
//                }
            }
            MediaPlayer.Event.PositionChanged -> {
//                lastPosition = event.positionChanged
                // Timber.d("XXXY onEvent = MediaPlayer.Event.PositionChanged = ${event.positionChanged}")
            }
            MediaPlayer.Event.Buffering -> {
                if (playerController.isPlaying()) {
                    if (event.buffering == 100f) {
                        stopLoading()
                    } else if (!handler.hasMessages(LOADING_ANIMATION) && !isLoading && (!::touchDelegate.isInitialized || !touchDelegate.isSeeking()) && !isDragging) {
                        handler.sendEmptyMessageDelayed(LOADING_ANIMATION, LOADING_ANIMATION_DELAY.toLong())
                    }
                }
            }
        }
    }

    private suspend fun dd() {

    }

    override fun onPause() {
        super.onPause()
        this.playerController.stop()
    }

    private val thisActivity get() = this

    /**
     * Handle resize of the surface and the overlay
     */
    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                FADE_OUT -> overlayDelegate.hideOverlay(false)
                FADE_OUT_INFO -> overlayDelegate.fadeOutInfo(overlayDelegate.overlayInfo)
                FADE_OUT_BRIGHTNESS_INFO -> overlayDelegate.fadeOutBrightness()
                FADE_OUT_VOLUME_INFO -> overlayDelegate.fadeOutVolume()
                START_PLAYBACK -> startPlayback()
                AUDIO_SERVICE_CONNECTION_FAILED -> exit(RESULT_CONNECTION_FAILED)
                RESET_BACK_LOCK -> lockBackButton = true
                CHECK_VIDEO_TRACKS -> {
//                    if (videoTracksCount < 1 && audioTracksCount > 0) {
//                        Timber.i("No video track, open in audio mode")
//                        switchToAudioMode(true)
//                    }
                }
                LOADING_ANIMATION -> startLoading()
                HIDE_INFO -> overlayDelegate.hideOverlay(true)
                SHOW_INFO -> overlayDelegate.showOverlay()
                // HIDE_SEEK -> touchDelegate.hideSeekOverlay()
//                HIDE_SETTINGS -> delayDelegate.endPlaybackSetting()
                else -> {
                }
            }
        }
    }

    private fun getOrientationForLock(): Int {
        val wm = applicationContext.getSystemService<WindowManager>()!!
        val display = wm.defaultDisplay
        val rot = screenRotation
        /*
         * Since getRotation() returns the screen's "natural" orientation,
         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
         * landscape.
         */
        var defaultWide = display.width > display.height
        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
            defaultWide = !defaultWide
        return if (defaultWide) {
            when (rot) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                else -> 0
            }
        } else {
            when (rot) {
                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else -> 0
            }
        }
    }

    private fun startPlayback() {
        /* start playback only when audio service and both surfaces are ready */
        if (playbackStarted) return
        playbackStarted = true

        val vlcVout = playerController.getVout()
        if (vlcVout.areViewsAttached()) {
            vlcVout.detachViews()
        }
//            val mediaPlayer = mediaplayer
//            if (!displayManager.isOnRenderer) videoLayout?.let {
//                mediaPlayer.attachViews(it, displayManager, true, false)
//                val size = if (isBenchmark) MediaPlayer.ScaleType.SURFACE_FILL else MediaPlayer.ScaleType.values()[settings.getInt(VIDEO_RATIO, MediaPlayer.ScaleType.SURFACE_BEST_FIT.ordinal)]
//                mediaPlayer.videoScale = size
//            }
//        loadMedia()
    }


//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        return touchDelegate.onTouchEvent(event)
//    }

    open fun exit(resultCode: Int) {
        if (isFinishing) return
//        val resultIntent = Intent(ACTION_RESULT)
//        videoUri?.let { uri ->
//            service?.run {
//                if (AndroidUtil.isNougatOrLater)
//                    resultIntent.putExtra(EXTRA_URI, uri.toString())
//                else
//                    resultIntent.data = videoUri
//                resultIntent.putExtra(EXTRA_POSITION, time)
//                resultIntent.putExtra(EXTRA_DURATION, length)
//            }
//            setResult(resultCode, resultIntent)
//            finish()
//        }
        setResult(resultCode)
        finish()
    }

    private fun exitOK() {
        exit(Activity.RESULT_OK)
    }

    private fun saveBrightness() {
        // Save brightness if user wants to
        if (settings.getBoolean(SAVE_BRIGHTNESS, false)) {
            val brightness = window.attributes.screenBrightness
            if (brightness != -1f) settings.putSingle(BRIGHTNESS_VALUE, brightness)
        }
    }

    private fun restoreBrightness() {
        if (settings.getBoolean(SAVE_BRIGHTNESS, false)) {
            val brightness = settings.getFloat(BRIGHTNESS_VALUE, -1f)
            if (brightness != -1f) setWindowBrightness(brightness)
        }
    }

    private fun setWindowBrightness(brightness: Float) {
        val lp = window.attributes
        lp.screenBrightness = brightness
        // Set Brightness
        window.attributes = lp
    }


    private fun onPlaying() {
        isPlaying = true
        stopLoading()
        handler.sendEmptyMessageDelayed(FADE_OUT, OVERLAY_TIMEOUT.toLong())
        // optionsDelegate?.setup()
        settings.edit { remove(VIDEO_PAUSED) }
    }


    fun getScreenOrientation(mode: PlayerOrientationMode): Int {
        return if (!mode.locked) {
            if (AndroidUtil.isJellyBeanMR2OrLater)
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            else
                ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            mode.orientation
        }
    }

    fun toggleOrientation() {
        orientationMode.locked = !orientationMode.locked
        orientationMode.orientation = getOrientationForLock()

        requestedOrientation = getScreenOrientation(orientationMode)
        if (orientationMode.locked) settings.putSingle(LAST_LOCK_ORIENTATION, requestedOrientation)
        overlayDelegate.updateOrientationIcon()
    }

    /**
     * Start the video loading animation.
     */
    private fun startLoading() {
        if (isLoading) return
        isLoading = true
        mBind.playerOverlayLoading.isVisible = true
    }

    /**
     * Stop the video loading animation.
     */
    private fun stopLoading() {
        handler.removeMessages(LOADING_ANIMATION)
        if (!isLoading) return
        isLoading = false
        mBind.playerOverlayLoading.isInvisible = true
    }

    internal fun initAudioVolume() {
        val playerVolume = this.playerController.getVolume()
        if (playerVolume <= 100f) {
            volume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            originalVol = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        } else {
            volume = playerVolume.toFloat() * audioMax / 100
        }
    }

    internal fun updateViewpoint(yaw: Float, pitch: Float, fov: Float): Boolean {
        return playerController.updateViewpoint(yaw, pitch, 0f, fov, false) ?: false
    }

    internal fun changeBrightness(delta: Float) {
        // Estimate and adjust Brightness
        val lp = window.attributes
        var brightness = (lp.screenBrightness + delta).coerceIn(0.01f, 1f)
        setWindowBrightness(brightness)
        brightness = (brightness * 100).roundToInt().toFloat()
        overlayDelegate.showBrightnessBar(brightness.toInt())
    }

    private fun mute(mute: Boolean) {
        isMute = mute
        if (isMute) volSave = playerController.getVolume()
        playerController.setVolume(if (isMute) 0 else volSave)
    }

    internal fun setAudioVolume(volume: Int, fromTouch: Boolean = false) {
        var vol = volume
        if (AndroidUtil.isNougatOrLater && (vol <= 0) xor isMute) {
            mute(!isMute)
            return  //Android N+ throws "SecurityException: Not allowed to change Do Not Disturb state"
        }

        if (vol <= audioMax) {
            playerController.setVolume(100)
            if (vol != audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                try {
                    audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                    // High Volume warning can block volume setting
                    if (audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC) != vol)
                        audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI)
                } catch (ignored: RuntimeException) {
                }
                //Some device won't allow us to change volume
            }
            vol = (vol * 100 / audioMax.toFloat()).roundToInt()
        } else {
            vol = (vol * 100 / audioMax.toFloat()).roundToInt()
            playerController.setVolume(vol.toFloat().roundToInt())
        }
        overlayDelegate.showVolumeBar(vol, fromTouch)
        volSave = playerController.getVolume()
    }


    fun resizeVideo() {
        val next = (playerController.mediaplayer.videoScale.ordinal + 1) % MediaPlayer.SURFACE_SCALES_COUNT
        val scale = MediaPlayer.ScaleType.values()[next]
        setVideoScale(scale)
        handler.sendEmptyMessage(SHOW_INFO)
    }

    internal fun setVideoScale(scale: MediaPlayer.ScaleType) {
        playerController.mediaplayer.videoScale = scale
        when (scale) {
            MediaPlayer.ScaleType.SURFACE_BEST_FIT -> overlayDelegate.showInfo(R.string.surface_best_fit, 1000)
            MediaPlayer.ScaleType.SURFACE_FIT_SCREEN -> overlayDelegate.showInfo(R.string.surface_fit_screen, 1000)
            MediaPlayer.ScaleType.SURFACE_FILL -> overlayDelegate.showInfo(R.string.surface_fill, 1000)
            MediaPlayer.ScaleType.SURFACE_16_9 -> overlayDelegate.showInfo("16:9", 1000)
            MediaPlayer.ScaleType.SURFACE_4_3 -> overlayDelegate.showInfo("4:3", 1000)
            MediaPlayer.ScaleType.SURFACE_ORIGINAL -> overlayDelegate.showInfo(R.string.surface_original, 1000)
        }
        settings.putSingle(VIDEO_RATIO, scale.ordinal)
    }

    fun doPlayPause() {
        if (!playerController.pausable) return
        if (playerController.isPlaying()) {
            overlayDelegate.showOverlayTimeout(OVERLAY_INFINITE)
            // pause()
            playerController.pause()
        } else {
            handler.sendEmptyMessageDelayed(FADE_OUT, 300L)
            // play()
            playerController.play()
        }
    }


    //Toast that appears only once
    fun displayWarningToast() {
        warningToast?.cancel()
        warningToast = Toast.makeText(application, R.string.audio_boost_warning, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.LEFT or Gravity.BOTTOM, 16.dp, 0)
            show()
        }
    }

    internal fun sendMouseEvent(action: Int, x: Int, y: Int) {
        playerController.getVout().sendMouseEvent(action, 0, x, y)
    }

    companion object {
        private const val EXTRA_URI = "EXTRA_URI"
        private const val RESULT_CONNECTION_FAILED = Activity.RESULT_FIRST_USER + 1
        private const val RESULT_PLAYBACK_ERROR = Activity.RESULT_FIRST_USER + 2
        private const val RESULT_VIDEO_TRACK_LOST = Activity.RESULT_FIRST_USER + 3
        private const val LOADING_ANIMATION_DELAY = 1000

        const val OVERLAY_TIMEOUT = 4000
        const val OVERLAY_INFINITE = -1
        const val FADE_OUT = 1
        const val FADE_OUT_INFO = 2
        private const val START_PLAYBACK = 3
        private const val AUDIO_SERVICE_CONNECTION_FAILED = 4
        private const val RESET_BACK_LOCK = 5
        private const val CHECK_VIDEO_TRACKS = 6
        private const val LOADING_ANIMATION = 7
        internal const val SHOW_INFO = 8
        internal const val HIDE_INFO = 9
        internal const val HIDE_SEEK = 10

        //internal const val HIDE_SETTINGS = 11
        const val FADE_OUT_BRIGHTNESS_INFO = 12
        const val FADE_OUT_VOLUME_INFO = 13
        internal const val DEFAULT_FOV = 80f

        fun createIntent(context: Context, uri: Uri) = Intent(context, LivePlayerActivity::class.java).also {
            it.putExtra(EXTRA_URI, uri)
        }

    }


    override fun onResume() {
        super.onResume()
        mViewModel.camManager.disconnectedMessage.flow.asLiveData().observe(this, { disconnected ->
            if (disconnected) {
                mViewModel.camManager.disconnectedMessage.show(this)
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val dm = DisplayMetrics()
        touchDelegate.screenConfig = ScreenConfig(
            dm,
            dm.widthPixels.coerceAtLeast(dm.heightPixels),
            dm.widthPixels.coerceAtMost(dm.heightPixels),
            newConfig.orientation
        )
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        return !isLoading && ::touchDelegate.isInitialized && touchDelegate.dispatchGenericMotionEvent(event)
    }
}

