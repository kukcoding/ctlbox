package myapp.data.repo.cam.api

import myapp.data.entities.network.CamConfigPayload
import myapp.data.entities.network.CamHealthPayload
import myapp.data.entities.network.CamLoginPayload
import myapp.data.entities.network.CamRecordFilesPayload
import retrofit2.Response
import retrofit2.http.*

interface CamApi {

    /**
     * 카메라 로그인
     */
    @POST("http://{ip}/login")
    @FormUrlEncoded
    suspend fun login(
        @Path(value = "ip") ip: String,
        @Field("pw") pw: String
    ): Response<CamLoginPayload.Login>

    /**
     * 카메라 Health
     */
    @POST("http://{ip}/health")
    suspend fun health(
        @Path(value = "ip") ip: String
    ): Response<CamHealthPayload.Response>


    /**
     * 카메라 로그아웃
     */
    @POST("http://{ip}/logout")
    suspend fun logout(@Path(value = "ip") ip: String): Response<Unit>


    /**
     * 카메라 설정 조회
     */
    @GET("http://{ip}/config")
    suspend fun config(@Path(value = "ip") ip: String): Response<CamConfigPayload.Response>


    /**
     * 녹화파일목록 조회
     */
    @POST("http://{ip}/recording/list")
    suspend fun recordFiles(@Path(value = "ip") ip: String): Response<CamRecordFilesPayload.Response>


    /**
     * 녹화파일 delete
     */
    @POST("http://{ip}/recording/delete")
    @FormUrlEncoded
    suspend fun deleteFile(
        @Path(value = "ip") ip: String,
        @Field("fileName") fileId: String
    ): Response<Unit>


    /**
     * 녹화해상도 설정 변경
     */
    @POST("http://{ip}/config/update-recording")
    @FormUrlEncoded
    suspend fun updateRecordingResolution(
        @Path(value = "ip") ip: String,
        @Field("resolution") resolution: String // 3840x2160
    ): Response<Unit>


    /**
     * 네트워크 설정 변경
     */
    @POST("http://{ip}/config/update-network")
    @FormUrlEncoded
    suspend fun updateNetworkConfig(
        @Path(value = "ip") ip: String,
        @Field("enabled") enabled: String // wifi
    ): Response<Unit>


    /**
     * 비밀번호 변경
     */
    @POST("http://{ip}/config/update-pw")
    @FormUrlEncoded
    suspend fun updatePassword(
        @Path(value = "ip") ip: String,
        @Field("pw") pw: String
    ): Response<Unit>

    /**
     *
     */
    @POST("http://{ip}/config/update-camera-name")
    @FormUrlEncoded
    suspend fun updateCameraName(
        @Path(value = "ip") ip: String,
        @Field("cameraName") cameraName: String
    ): Response<Unit>
}
