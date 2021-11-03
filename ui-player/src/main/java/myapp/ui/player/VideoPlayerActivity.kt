package myapp.ui.player

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.videolan.libvlc.MediaPlayer


@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity(), MediaPlayerEventListener, TextWatcher {
    override suspend fun onEvent(event: MediaPlayer.Event) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
    }

    //
//    lateinit var settings: SharedPreferences
//
//    val playerController by lazy { PlayerController(appCtx) }
//    private val mArgVideoUri by extraNotNull<Uri>(EXTRA_URI)
//
//    // private val mArgSectionType by extraNotNull<Bible.SectionType>(EXTRA_SECTION_TYPE)
//    val mBind: ActivityVideoPlayerBinding by contentView(R.layout.activity_video_player)
//    private var warningToast: Toast? = null
//
//    @Inject
//    internal lateinit var vmFactory: LivePlayerViewModel.Factory
//    private val mViewModel: LivePlayerViewModel by viewModels {
//        LivePlayerViewModel.provideFactory(vmFactory, mArgVideoUri)
//    }
//    val currentScaleType: MediaPlayer.ScaleType
//        get() = playerController.mediaplayer.videoScale
//
//    lateinit var orientationMode: PlayerOrientationMode
//
//    //Volume
//    internal lateinit var audiomanager: AudioManager
//        private set
//    internal var audioMax: Int = 0
//        private set
//    internal var isAudioBoostEnabled: Boolean = false
//        private set
//
//    private var isMute = false
//    private var volSave: Int = 0
//    internal var volume: Float = 0.toFloat()
//    internal var originalVol: Float = 0.toFloat()
//
//    private var isPlaying = false
//    var isLoading = false
//    var isLocked = false
//    var lockBackButton = false
//    private var wasPaused = false
//    private var savedTime: Long = -1
//
//    var keepScreenOn = false
//    val overlayDelegate by lazy { VideoPlayerOverlayDelegate(this@VideoPlayerActivity) }
////    val delayDelegate: VideoDelayDelegate by lazy(LazyThreadSafetyMode.NONE) { VideoDelayDelegate(this@VideoPlayerActivity) }
//
//    internal var fov: Float = 0.toFloat()
//    lateinit var touchDelegate: VideoTouchDelegate
//
//    /**
//     * Flag to indicate whether the media should be paused once loaded
//     * (e.g. lock screen, or to restore the pause state)
//     */
//    private var playbackStarted = false
//
//    private val screenRotation: Int
//        get() {
//            val wm = applicationContext.getSystemService<WindowManager>()!!
//            return wm.defaultDisplay?.rotation ?: Surface.ROTATION_0
//        }
//    var isShowing: Boolean = false
//
//    /* for getTime and seek */
//    private var forcedTime: Long = -1
//    private var lastTime: Long = -1
//
//    val time: Long
//        get() {
//
//            var time = playerController.getCurrentTime()
//            if (forcedTime != -1L && lastTime != -1L) {
//                if (lastTime > forcedTime) {
//                    if (time in (forcedTime + 1)..lastTime || time > lastTime) {
//                        forcedTime = -1
//                        lastTime = forcedTime
//                    }
//                } else {
//                    if (time > forcedTime) {
//                        forcedTime = -1
//                        lastTime = forcedTime
//                    }
//                }
//            } else if (time == 0L) {
//                time = playerController.mediaplayer.time
//            }
//            return if (forcedTime == -1L) time else forcedTime
//        }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mBind.vm = mViewModel
//        settings = Settings.getInstance(this)
//        /* Services and miscellaneous */
//        audiomanager = applicationContext.getSystemService<AudioManager>()!!
//        audioMax = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//        isAudioBoostEnabled = settings.getBoolean("audio_boost", true)
//        val screenOrientationSetting =
//            Integer.valueOf(settings.getString(SCREEN_ORIENTATION, "99" /*SCREEN ORIENTATION SENSOR*/)!!)
//        val sensor = settings.getBoolean(LOCK_USE_SENSOR, true)
//        orientationMode = when (screenOrientationSetting) {
//            99 -> PlayerOrientationMode(false)
//            101 -> PlayerOrientationMode(
//                true,
//                if (sensor) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//            )
//            102 -> PlayerOrientationMode(
//                true,
//                if (sensor) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//            )
//            103 -> PlayerOrientationMode(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
//            98 -> PlayerOrientationMode(
//                true,
//                settings.getInt(LAST_LOCK_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
//            )
//            else -> PlayerOrientationMode(true, getOrientationForLock())
//        }
//
//        //overlayDelegate.playlist = mBind.root.findViewById(R.id.video_playlist)
//        overlayDelegate.playlistSearchText = mBind.root.findViewById(R.id.playlist_search_text)
//        //overlayDelegate.playlistContainer = mBind.root.findViewById(R.id.video_playlist_container)
//        //overlayDelegate.closeButton = mBind.root.findViewById(R.id.close_button)
//        overlayDelegate.playlistSearchText.editText?.addTextChangedListener(this)
//        overlayDelegate.playerUiContainer = mBind.root.findViewById(R.id.player_ui_container)
//
//        // create touch delegate
//        val touch = if (true) {
//            val audioTouch = (!AndroidUtil.isLolliPopOrLater || !audiomanager.isVolumeFixed) && settings.getBoolean(
//                ENABLE_VOLUME_GESTURE,
//                true
//            )
//            val brightnessTouch = settings.getBoolean(ENABLE_BRIGHTNESS_GESTURE, true)
//            val v1 = if (audioTouch) TOUCH_FLAG_AUDIO_VOLUME else 0
//            val v2 = if (brightnessTouch) TOUCH_FLAG_BRIGHTNESS else 0
//            val v3 = if (settings.getBoolean(ENABLE_DOUBLE_TAP_SEEK, true)) TOUCH_FLAG_SEEK else 0
//            v1 + v2 + v3
//        } else 0
//
//        val dm = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(dm)
//        val yRange = dm.widthPixels.coerceAtMost(dm.heightPixels)
//        val xRange = dm.widthPixels.coerceAtLeast(dm.heightPixels)
//        val sc = ScreenConfig(dm, xRange, yRange, resources.configuration.orientation)
//        touchDelegate = VideoTouchDelegate(this, touch, sc, false)
//        customInit()
//        setupEvents()
//    }
//
//    private fun customInit() {
//        initPlayerView()
//    }
//
//    private fun setupEvents() {}
//
//    private fun initPlayerView() {
//        val libVLC = VLCInstance.getInstance(appCtx)
//        val videoUri = mViewModel.videoUri
//
//        Timber.d("XXX LIBVLC=${libVLC}")
////        val mediaPlayer = MediaPlayer(libVLC)
////        mediaPlayer.attachViews(mBind.videoLayout, null, false, false)
//        playerController.mediaplayer.attachViews(mBind.videoLayout, null, false, true)
//
//        lifecycleScope.launchWhenCreated {
//            val media = Media(libVLC, videoUri).apply {
//                setHWDecoderEnabled(true, false);
////                addOption(":network-caching=0");
////                addOption(":clock-jitter=0");
////                addOption(":clock-synchro=0");
//                addOption(":rtsp-tcp");
//            }
//
//            playerController.startPlayback(media, thisActivity, 0L)
//        }
//    }
//
//    override suspend fun onEvent(event: MediaPlayer.Event) {
//        // Timber.d("XXX onEvent=${event}")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        this.playerController.stop()
//    }
//
//    private val thisActivity get() = this
//
//    /**
//     * Handle resize of the surface and the overlay
//     */
//    val handler: Handler = object : Handler(Looper.getMainLooper()) {
//        override fun handleMessage(msg: Message) {
//            when (msg.what) {
//                FADE_OUT -> overlayDelegate.hideOverlay(false)
//                FADE_OUT_INFO -> overlayDelegate.fadeOutInfo(overlayDelegate.overlayInfo)
//                FADE_OUT_BRIGHTNESS_INFO -> overlayDelegate.fadeOutBrightness()
//                FADE_OUT_VOLUME_INFO -> overlayDelegate.fadeOutVolume()
//                START_PLAYBACK -> startPlayback()
//                AUDIO_SERVICE_CONNECTION_FAILED -> exit(RESULT_CONNECTION_FAILED)
//                RESET_BACK_LOCK -> lockBackButton = true
//                CHECK_VIDEO_TRACKS -> {
////                    if (videoTracksCount < 1 && audioTracksCount > 0) {
////                        Timber.i("No video track, open in audio mode")
////                        switchToAudioMode(true)
////                    }
//                }
//                LOADING_ANIMATION -> startLoading()
//                HIDE_INFO -> overlayDelegate.hideOverlay(true)
//                SHOW_INFO -> overlayDelegate.showOverlay()
//                HIDE_SEEK -> touchDelegate.hideSeekOverlay()
////                HIDE_SETTINGS -> delayDelegate.endPlaybackSetting()
//                else -> {
//                }
//            }
//        }
//    }
//
//    private fun getOrientationForLock(): Int {
//        val wm = applicationContext.getSystemService<WindowManager>()!!
//        val display = wm.defaultDisplay
//        val rot = screenRotation
//        /*
//         * Since getRotation() returns the screen's "natural" orientation,
//         * which is not guaranteed to be SCREEN_ORIENTATION_PORTRAIT,
//         * we have to invert the SCREEN_ORIENTATION value if it is "naturally"
//         * landscape.
//         */
//        var defaultWide = display.width > display.height
//        if (rot == Surface.ROTATION_90 || rot == Surface.ROTATION_270)
//            defaultWide = !defaultWide
//        return if (defaultWide) {
//            when (rot) {
//                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
//                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
//                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
//                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
//                else -> 0
//            }
//        } else {
//            when (rot) {
//                Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
//                Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
//                Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
//                Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
//                else -> 0
//            }
//        }
//    }
//
//    private fun startPlayback() {
//        /* start playback only when audio service and both surfaces are ready */
//        if (playbackStarted) return
//        playbackStarted = true
//
//        val vlcVout = playerController.getVout()
//        if (vlcVout.areViewsAttached()) {
//            vlcVout.detachViews()
//        }
////            val mediaPlayer = mediaplayer
////            if (!displayManager.isOnRenderer) videoLayout?.let {
////                mediaPlayer.attachViews(it, displayManager, true, false)
////                val size = if (isBenchmark) MediaPlayer.ScaleType.SURFACE_FILL else MediaPlayer.ScaleType.values()[settings.getInt(VIDEO_RATIO, MediaPlayer.ScaleType.SURFACE_BEST_FIT.ordinal)]
////                mediaPlayer.videoScale = size
////            }
//
//        initUI()
//
//        loadMedia()
//    }
//
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        return touchDelegate.onTouchEvent(event)
//    }
//
//    open fun exit(resultCode: Int) {
//        if (isFinishing) return
////        val resultIntent = Intent(ACTION_RESULT)
////        videoUri?.let { uri ->
////            service?.run {
////                if (AndroidUtil.isNougatOrLater)
////                    resultIntent.putExtra(EXTRA_URI, uri.toString())
////                else
////                    resultIntent.data = videoUri
////                resultIntent.putExtra(EXTRA_POSITION, time)
////                resultIntent.putExtra(EXTRA_DURATION, length)
////            }
////            setResult(resultCode, resultIntent)
////            finish()
////        }
//        setResult(resultCode)
//        finish()
//    }
//
//    private fun exitOK() {
//        exit(Activity.RESULT_OK)
//    }
//
//    private fun saveBrightness() {
//        // Save brightness if user wants to
//        if (settings.getBoolean(SAVE_BRIGHTNESS, false)) {
//            val brightness = window.attributes.screenBrightness
//            if (brightness != -1f) settings.putSingle(BRIGHTNESS_VALUE, brightness)
//        }
//    }
//
//    private fun restoreBrightness() {
//        if (settings.getBoolean(SAVE_BRIGHTNESS, false)) {
//            val brightness = settings.getFloat(BRIGHTNESS_VALUE, -1f)
//            if (brightness != -1f) setWindowBrightness(brightness)
//        }
//    }
//
//    private fun setWindowBrightness(brightness: Float) {
//        val lp = window.attributes
//        lp.screenBrightness = brightness
//        // Set Brightness
//        window.attributes = lp
//    }
//
//    private fun initUI() {
//        keepScreenOn = true
//    }
//
//    /*
//     * Additionnal method to prevent alert dialog to pop up
//     */
//    private fun loadMedia(fromStart: Boolean) {
//        loadMedia()
//    }
//
//    /**
//     * External extras:
//     * - position (long) - position of the video to start with (in ms)
//     * - subtitles_location (String) - location of a subtitles file to load
//     * - from_start (boolean) - Whether playback should start from start or from resume point
//     * - title (String) - video title, will be guessed from file if not set.
//     */
//    @SuppressLint("SdCardPath")
//    @TargetApi(12)
//    fun loadMedia() {
//        isPlaying = false
//        var title: String? = null
//        var fromStart = settings.getString(KEY_VIDEO_CONFIRM_RESUME, "0") == "1"
//        var itemTitle: String? = null
//        var positionInPlaylist = -1
//        val intent = intent
//        val extras = intent.extras
//        var startTime = 0L
//        val currentMedia = playerController.getMedia()
//        val hasMedia = currentMedia != null
//        val isPlaying = playerController.isPlaying()
//
//        /*
//     * If the activity has been paused by pressing the power button, then
//     * pressing it again will show the lock screen.
//     * But onResume will also be called, even if vlc-android is still in
//     * the background.
//     * To workaround this, pause playback if the lockscreen is displayed.
//     */
//        val km = applicationContext.getSystemService<KeyguardManager>()!!
//        if (km.inKeyguardRestrictedInputMode())
//            wasPaused = true
//        if (wasPaused && BuildConfig.DEBUG)
//            Timber.d("Video was previously paused, resuming in paused mode")
//
//        var videoUri = mArgVideoUri
//        val restorePlayback = hasMedia && currentMedia?.uri == videoUri
//        if (startTime == 0L && savedTime > 0L && restorePlayback) startTime = savedTime
//
//
//        if (wasPaused) {
//            // XXX: Workaround to update the seekbar position
//            forcedTime = startTime
//            forcedTime = -1
//            overlayDelegate.showOverlay(true)
//        }
//    }
//
//    private fun onPlaying() {
//        val mw = playerController.getMedia() as MediaWrapper
//        isPlaying = true
//        stopLoading()
//        if (!mw.hasFlag(MediaWrapper.MEDIA_PAUSED))
//            handler.sendEmptyMessageDelayed(FADE_OUT, OVERLAY_TIMEOUT.toLong())
//        else {
//            mw.removeFlags(MediaWrapper.MEDIA_PAUSED)
//            wasPaused = false
//        }
//        // optionsDelegate?.setup()
//        settings.edit { remove(VIDEO_PAUSED) }
//    }
//
//
//    fun getScreenOrientation(mode: PlayerOrientationMode): Int {
//        return if (!mode.locked) {
//            if (AndroidUtil.isJellyBeanMR2OrLater)
//                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
//            else
//                ActivityInfo.SCREEN_ORIENTATION_SENSOR
//        } else {
//            mode.orientation
//        }
//    }
//
//    fun toggleOrientation() {
//        orientationMode.locked = !orientationMode.locked
//        orientationMode.orientation = getOrientationForLock()
//
//        requestedOrientation = getScreenOrientation(orientationMode)
//        if (orientationMode.locked) settings.putSingle(LAST_LOCK_ORIENTATION, requestedOrientation)
//        overlayDelegate.updateOrientationIcon()
//    }
//
//    /**
//     * Start the video loading animation.
//     */
//    private fun startLoading() {
//        if (isLoading) return
//        isLoading = true
//        mBind.playerOverlayLoading.isVisible = true
//    }
//
//    /**
//     * Stop the video loading animation.
//     */
//    private fun stopLoading() {
//        handler.removeMessages(LOADING_ANIMATION)
//        if (!isLoading) return
//        isLoading = false
//        mBind.playerOverlayLoading.isInvisible = true
//    }
//
//    internal fun initAudioVolume() {
//        val playerVolume = this.playerController.getVolume()
//        if (playerVolume <= 100f) {
//            volume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
//            originalVol = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
//        } else {
//            volume = playerVolume.toFloat() * audioMax / 100
//        }
//    }
//
//    internal fun updateViewpoint(yaw: Float, pitch: Float, fov: Float): Boolean {
//        return playerController.updateViewpoint(yaw, pitch, 0f, fov, false) ?: false
//    }
//
//    internal fun changeBrightness(delta: Float) {
//        // Estimate and adjust Brightness
//        val lp = window.attributes
//        var brightness = (lp.screenBrightness + delta).coerceIn(0.01f, 1f)
//        setWindowBrightness(brightness)
//        brightness = (brightness * 100).roundToInt().toFloat()
//        overlayDelegate.showBrightnessBar(brightness.toInt())
//    }
//
//    private fun mute(mute: Boolean) {
//        isMute = mute
//        if (isMute) volSave = playerController.getVolume()
//        playerController.setVolume(if (isMute) 0 else volSave)
//    }
//
//    internal fun setAudioVolume(volume: Int, fromTouch: Boolean = false) {
//        var vol = volume
//        if (AndroidUtil.isNougatOrLater && (vol <= 0) xor isMute) {
//            mute(!isMute)
//            return  //Android N+ throws "SecurityException: Not allowed to change Do Not Disturb state"
//        }
//
//        if (vol <= audioMax) {
//            playerController.setVolume(100)
//            if (vol != audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC)) {
//                try {
//                    audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
//                    // High Volume warning can block volume setting
//                    if (audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC) != vol)
//                        audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI)
//                } catch (ignored: RuntimeException) {
//                }
//                //Some device won't allow us to change volume
//            }
//            vol = (vol * 100 / audioMax.toFloat()).roundToInt()
//        } else {
//            vol = (vol * 100 / audioMax.toFloat()).roundToInt()
//            playerController.setVolume(vol.toFloat().roundToInt())
//        }
//        overlayDelegate.showVolumeBar(vol, fromTouch)
//        volSave = playerController.getVolume()
//    }
//
//
//    fun seek(position: Long, fromUser: Boolean = false) {
//        if (playerController.getLength() > 0.0) {
//            playerController.setTime(time, false)
//        } else {
//            playerController.setPosition((time.toFloat() / NO_LENGTH_PROGRESS_MAX.toFloat()))
//        }
//    }
//
//    fun seek(time: Long, fromUser: Boolean = false, fast: Boolean = false) {
//        if (playerController.getLength() > 0.0) {
//            playerController.setTime(time, fast)
//        } else {
//            playerController.setPosition((time.toFloat() / NO_LENGTH_PROGRESS_MAX.toFloat()))
//        }
////        if (fromUser) {
////            publishState(time)
////        }
//    }
//
//    internal fun seek(position: Long, length: Long, fromUser: Boolean = false, fast: Boolean = false) {
//        forcedTime = position
//        lastTime = playerController.getCurrentTime()
//        if (playerController.getLength() > 0.0) {
//            playerController.setTime(time, fast)
//        } else {
//            playerController.setPosition((time.toFloat() / NO_LENGTH_PROGRESS_MAX.toFloat()))
//        }
//        playerController.updateProgress(position)
//    }
//
//
//    fun resizeVideo() {
//        val next = (playerController.mediaplayer.videoScale.ordinal + 1) % MediaPlayer.SURFACE_SCALES_COUNT
//        val scale = MediaPlayer.ScaleType.values()[next]
//        setVideoScale(scale)
//        handler.sendEmptyMessage(SHOW_INFO)
//    }
//
//    internal fun setVideoScale(scale: MediaPlayer.ScaleType) {
//        playerController.mediaplayer.videoScale = scale
//        when (scale) {
//            MediaPlayer.ScaleType.SURFACE_BEST_FIT -> overlayDelegate.showInfo(R.string.surface_best_fit, 1000)
//            MediaPlayer.ScaleType.SURFACE_FIT_SCREEN -> overlayDelegate.showInfo(R.string.surface_fit_screen, 1000)
//            MediaPlayer.ScaleType.SURFACE_FILL -> overlayDelegate.showInfo(R.string.surface_fill, 1000)
//            MediaPlayer.ScaleType.SURFACE_16_9 -> overlayDelegate.showInfo("16:9", 1000)
//            MediaPlayer.ScaleType.SURFACE_4_3 -> overlayDelegate.showInfo("4:3", 1000)
//            MediaPlayer.ScaleType.SURFACE_ORIGINAL -> overlayDelegate.showInfo(R.string.surface_original, 1000)
//        }
//        settings.putSingle(VIDEO_RATIO, scale.ordinal)
//    }
//
//    fun doPlayPause() {
//        if (!playerController.pausable) return
//        if (playerController.isPlaying()) {
//            overlayDelegate.showOverlayTimeout(OVERLAY_INFINITE)
//            // pause()
//            playerController.pause()
//        } else {
//            handler.sendEmptyMessageDelayed(FADE_OUT, 300L)
//            // play()
//            playerController.play()
//        }
//    }
//
//
//    override fun afterTextChanged(s: Editable?) {
//    }
//
//    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//    }
//
//    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//
//        if (s == null) return
//        val length = s.length
//        if (length > 0) {
//            //playlistModel?.filter(s)
//        } else {
//            //playlistModel?.filter(null)
//        }
//    }
//
//    //Toast that appears only once
//    fun displayWarningToast() {
//        warningToast?.cancel()
//        warningToast = Toast.makeText(application, R.string.audio_boost_warning, Toast.LENGTH_SHORT).apply {
//            setGravity(Gravity.LEFT or Gravity.BOTTOM, 16.dp, 0)
//            show()
//        }
//    }
//
//    internal fun sendMouseEvent(action: Int, x: Int, y: Int) {
//        playerController.getVout().sendMouseEvent(action, 0, x, y)
//    }
//
//    companion object {
//        private const val EXTRA_URI = "EXTRA_URI"
//        private const val RESULT_CONNECTION_FAILED = Activity.RESULT_FIRST_USER + 1
//        private const val RESULT_PLAYBACK_ERROR = Activity.RESULT_FIRST_USER + 2
//        private const val RESULT_VIDEO_TRACK_LOST = Activity.RESULT_FIRST_USER + 3
//
//        const val OVERLAY_TIMEOUT = 4000
//        const val OVERLAY_INFINITE = -1
//        const val FADE_OUT = 1
//        const val FADE_OUT_INFO = 2
//        private const val START_PLAYBACK = 3
//        private const val AUDIO_SERVICE_CONNECTION_FAILED = 4
//        private const val RESET_BACK_LOCK = 5
//        private const val CHECK_VIDEO_TRACKS = 6
//        private const val LOADING_ANIMATION = 7
//        internal const val SHOW_INFO = 8
//        internal const val HIDE_INFO = 9
//        internal const val HIDE_SEEK = 10
//
//        //internal const val HIDE_SETTINGS = 11
//        const val FADE_OUT_BRIGHTNESS_INFO = 12
//        const val FADE_OUT_VOLUME_INFO = 13
//        internal const val DEFAULT_FOV = 80f
//
//        @Volatile
//        internal var sDisplayRemainingTime: Boolean = false
//
//
//        fun createIntent(context: Context, uri: Uri) = Intent(context, VideoPlayerActivity::class.java).also {
//            it.putExtra(EXTRA_URI, uri)
//        }
//
//    }
}


//@ExperimentalCoroutinesApi
//@ObsoleteCoroutinesApi
//@BindingAdapter("length", "time")
//fun setPlaybackTime(view: TextView, length: Long, time: Long) {
//    view.text = if (VideoPlayerActivity.sDisplayRemainingTime && length > 0)
//        "-" + '\u00A0'.toString() + Tools.millisToString(length - time)
//    else
//        Tools.millisToString(length)
//}
