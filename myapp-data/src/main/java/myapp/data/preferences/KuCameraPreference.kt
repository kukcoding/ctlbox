package myapp.data.preferences

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import myapp.data.entities.KuCamera
import splitties.preferences.PrefDelegate
import splitties.preferences.Preferences

private data class KuCameraListJson(val list: List<KuCamera>)

object KuCameraPreference : Preferences("KuCamera") {

    // camId/camName/lastAccessTime

    /**
     * 카메라 목록을 담고 있는 JSON
     * cameraListJson
     */
    val cameraListJson = StringOrNullPref("cameraListJson")

    private fun fromJson(json: String?): List<KuCamera> {
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                Gson().fromJson(json, KuCameraListJson::class.java).list
            } catch (ignore: Throwable) {
                emptyList()
            }
        }
    }

    private fun toJson(list: List<KuCamera>): String {
        return Gson().toJson(KuCameraListJson(list))
    }

    private fun saveCommit(cameras: List<KuCamera>) {
        this.cameraListJson.value = toJson(cameras)
    }

    fun replace(cameras: List<KuCamera>) {
        saveCommit(cameras)
    }

    fun add(camera: KuCamera) {
        val list = this.cameraList().filter { it.cameraId != camera.cameraId }.toMutableList()
        list.add(camera)
        saveCommit(list)
    }

    fun remove(camId: String) {
        saveCommit(this.cameraList().filter { it.cameraId != camId })
    }

    fun cameraList(): List<KuCamera> {
        return fromJson(this.cameraListJson.value)
    }

    fun observeCameraList(): Flow<List<KuCamera>> {
        return this.cameraListJson.valueFlow().map { fromJson(it) }
    }


    fun reset() {
        listOf<PrefDelegate<*>>(
            cameraListJson
        ).forEach { it.resetDefault() }
    }
}

