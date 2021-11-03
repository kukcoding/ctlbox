package myapp.tube

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import timber.log.Timber


sealed class TubePlayerState {
    object Unknown : TubePlayerState()
    object Unstarted : TubePlayerState()
    object Ended : TubePlayerState()
    object Playing : TubePlayerState()
    object Paused : TubePlayerState()
    object Buffering : TubePlayerState()
    object VideoCued : TubePlayerState()
    data class OnPlayerReady(val player: YouTubePlayer) : TubePlayerState()
    data class Error(val err: PlayerConstants.PlayerError) : TubePlayerState()
}


data class TubePlayInfo(
    val videoId: String,
    val durationSecond: Float? = null,
    val currentSecond: Float? = null
)


open class TubePlayerStateListenerAdapter : AbstractYouTubePlayerListener() {
    interface OnPlayerStateChangeListener {
        fun onPlayerStateChanged(playerState: TubePlayerState)
    }

    interface OnPlayInfoChangeListener {
        fun onPlayInfoChanged(playInfo: TubePlayInfo)
    }

    var lastPlayerState: TubePlayerState? = null
        private set

    var lastPlayInfo: TubePlayInfo? = null
        private set

    private var playInfoChangeListeners: List<OnPlayInfoChangeListener> = emptyList()
    private var stateChangeListeners: List<OnPlayerStateChangeListener> = emptyList()

    fun addStateChangeListener(listener: OnPlayerStateChangeListener) {
        stateChangeListeners = stateChangeListeners.filter { it != listener } + listener
    }

    fun removeStateChangeListener(listener: OnPlayerStateChangeListener) {
        stateChangeListeners = stateChangeListeners - listener
    }

    fun addPlayInfoChangeListener(listener: OnPlayInfoChangeListener) {
        playInfoChangeListeners = playInfoChangeListeners.filter { it != listener } + listener
    }

    fun removePlayInfoChangeListener(listener: OnPlayInfoChangeListener) {
        playInfoChangeListeners = playInfoChangeListeners - listener
    }

    private fun notifyPlayerState(playerState: TubePlayerState) {
        // Timber.d("XXX notifyPlayerState = $playerState")
        lastPlayerState = playerState
        stateChangeListeners.forEach { listener ->
            listener.onPlayerStateChanged(playerState)
        }
    }

    private fun notifyPlayInfo(playInfo: TubePlayInfo) {
        // Timber.d("XXX notifyPlayInfo = $playInfo")
        lastPlayInfo = playInfo
        playInfoChangeListeners.forEach { listener ->
            listener.onPlayInfoChanged(playInfo)
        }
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        state: PlayerConstants.PlayerState
    ) {
        val tubePlayerState = when (state) {
            PlayerConstants.PlayerState.BUFFERING -> TubePlayerState.Buffering
            PlayerConstants.PlayerState.PAUSED -> TubePlayerState.Paused
            PlayerConstants.PlayerState.ENDED -> TubePlayerState.Ended
            PlayerConstants.PlayerState.PLAYING -> TubePlayerState.Playing
            PlayerConstants.PlayerState.UNKNOWN -> TubePlayerState.Unknown
            PlayerConstants.PlayerState.UNSTARTED -> TubePlayerState.Unstarted
            PlayerConstants.PlayerState.VIDEO_CUED -> TubePlayerState.VideoCued
        }
        notifyPlayerState(tubePlayerState)
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        notifyPlayerState(TubePlayerState.OnPlayerReady(youTubePlayer))
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
        Timber.d("XXX PlayerError: ${error.name}")
        notifyPlayerState(TubePlayerState.Error(error))
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        // Timber.d("XXX onVideoDuration: $duration")
        val info = lastPlayInfo
        if (info != null) {
            notifyPlayInfo(info.copy(durationSecond = duration))
        }
    }


    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        // Timber.d("XXX onCurrentSecond: $second")
        val info = lastPlayInfo
        if (info != null) {
            notifyPlayInfo(info.copy(currentSecond = second))
        }
    }

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
        // Timber.d("XXX onVideoLoadedFraction: $loadedFraction")
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        // Timber.d("XXX onVideoId: $videoId")
        notifyPlayInfo(TubePlayInfo(videoId = videoId))
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
        // Timber.d("XXX onApiChange")
    }
}
