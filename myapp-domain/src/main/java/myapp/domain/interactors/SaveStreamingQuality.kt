package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.entities.VideoQuality
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import javax.inject.Inject

class SaveStreamingQuality @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager
) : Interactor<SaveStreamingQuality.Params>() {

    data class Params(val ip: String, val resolution: String, val fps: Int)

    suspend fun executeSync(ip: String, resolution: String, fps: Int) = executeSync(
        Params(ip = ip, resolution = resolution, fps = fps)
    )

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        dataSource.updateStreamingVideoQuality(
            resolution = params.resolution,
            fps = params.fps
        ).getOrThrow()

        camManager.updateConfig { old ->
            old.copy(
                streaming = VideoQuality(
                    resolution = params.resolution,
                    fps = params.fps,
                    kbps = old.recording.kbps
                )
            )
        }
    }
}
