package myapp.ui.home


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import myapp.Cam
import myapp.data.cam.CamConnectMonitor
import myapp.ui.home.databinding.FragmentMjpgPlayerBinding
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException


@AndroidEntryPoint
class MjpgPlayerFragment : Fragment() {

    private val mViewModel: HomeViewModel by viewModels({ requireParentFragment() })
    private lateinit var mBind: FragmentMjpgPlayerBinding
    private var stopped: Boolean = false
    private var lastFetchedTime = 0L
    private var failCount = 0
    private var cameraIp: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentMjpgPlayerBinding.inflate(inflater, container, false)
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
    }

    private fun setupEvents() {
        mViewModel.camManager.observeConfig().asLiveData().observe(viewLifecycleOwner, { cfg ->
            if (cfg != null) {
                if (stopped) {
                    startPlay()
                }
            } else {
                if (!stopped) {
                    stopPlay()
                }
            }
        })
        mViewModel.camManager.observeCameraIp().asLiveData().observe(viewLifecycleOwner, { ip ->
            this.cameraIp = ip
        })
    }

    override fun onResume() {
        super.onResume()
        startPlay()
    }

    override fun onPause() {
        super.onPause()
        stopPlay()
    }

    private fun startPlay() {
        stopped = false
        showMjpg()
        lifecycleScope.launchWhenCreated {
            while (lifecycleScope.isActive && !stopped) {
                val ip = cameraIp
                if (ip == null) {
                    mBind.mjpgView.setImageDrawable(null)
                    mBind.mjpgView.isInvisible = true
                    mBind.overlayError.isGone = true
                    delay(400)
                    continue
                }
                val diff = System.currentTimeMillis() - lastFetchedTime
                if (diff < 100) {
                    delay(100 - diff)
                }
                fetch(Cam.url(ip = ip, path = "/mjpg"))
            }
            if (stopped) {
                hideMjpg()
            }
        }
    }

    private fun hideMjpg() {
        mBind.mjpgView.isGone = true
        mBind.overlayError.isGone = true
    }

    private fun showMjpg() {
        mBind.mjpgView.isVisible = true
        mBind.overlayError.isGone = true
    }

    private suspend fun fetch(url: String) {
        val newUrl = url.toHttpUrl().newBuilder().addQueryParameter("_t", System.currentTimeMillis().toString()).build()
        disconnectLast()
        val bitmap = fetchBitmapFromUrl(newUrl)
        if (bitmap != null) {
            failCount = 0
            mBind.overlayError.isVisible = false
            mBind.mjpgView.setImageBitmap(bitmap)
            mBind.mjpgView.isVisible = true
            lastFetchedTime = System.currentTimeMillis()

            // mjpg
            CamConnectMonitor.getInstance(requireContext()).updateConnectivityOkForce()
        } else {
            failCount++
            if (failCount > 10) {
                failCount = 10
                mBind.overlayError.isVisible = true
                mBind.mjpgView.setImageDrawable(null)
            } else {
                mBind.overlayError.isVisible = false
            }
        }
    }

    private fun stopPlay() {
        disconnectLast()
        stopped = true
        hideMjpg()
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient()
    }


    private var lastNetworkCall: Call? = null
    private var lastNetworkJob: CompletableDeferred<Bitmap?>? = null

    private suspend fun fetchBitmapFromUrl(url: HttpUrl): Bitmap? {
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)
        lastNetworkCall = call

        val job = CompletableDeferred<Bitmap?>()
        lastNetworkJob = job
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                lastNetworkCall = null
                job.complete(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val stream = response.body?.byteStream()
                if (stream != null) {
                    job.complete(BitmapFactory.decodeStream(stream))
                } else {
                    job.complete(null)
                }
                lastNetworkCall = null
            }
        })

        return job.await()
    }

    private fun disconnectLast() {
        try {
            lastNetworkCall?.cancel()
            lastNetworkJob?.cancel()
        } catch (ignore: Throwable) {
        } finally {
            lastNetworkCall = null
            lastNetworkJob = null
        }
    }

}
