package myapp.ui.player


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.EventLogger
import dagger.hilt.android.AndroidEntryPoint
import myapp.ui.player.databinding.FragmentFilePlayerBinding


@AndroidEntryPoint
class FilePlayerFragment : Fragment() {

    private val mViewModel: FilePlayerViewModel by activityViewModels()
    private lateinit var mBind: FragmentFilePlayerBinding

    private var player: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentFilePlayerBinding.inflate(inflater, container, false)
        mBind.vm = mViewModel
        mBind.lifecycleOwner = this
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {
        playerView = mBind.playerView
    }

    private fun setupEvents() {
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
        playerView?.onResume()
    }

    override fun onResume() {
        super.onResume()
        if (this.player == null) {
            initializePlayer()
            playerView?.onResume()
        }
    }

    override fun onStop() {
        super.onStop()
        playerView?.onPause()
        releasePlayer()
    }

    private fun initializePlayer() {
        val appCtx = requireContext().applicationContext
        val player = SimpleExoPlayer.Builder(appCtx).build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.setMediaSource(mViewModel.mediaSource(appCtx))
        player.prepare()
        // player.play()
        player.playWhenReady = true

        this.playerView!!.player = player
        player.addAnalyticsListener(EventLogger( /* trackSelector= */null))
        this.player = player
    }

    private fun releasePlayer() {
        this.playerView?.player = null
        this.player?.release()
        this.player = null
    }
}
