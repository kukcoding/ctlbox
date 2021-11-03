package myapp.ui.player.vlc

interface IPlaybackSettingsController {
    enum class DelayState {
        OFF, AUDIO
    }

    fun showAudioDelaySetting()
    fun endPlaybackSetting()
}
