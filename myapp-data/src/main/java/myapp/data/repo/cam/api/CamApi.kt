package myapp.data.repo.cam.api

import myapp.data.apicommon.TRApiResponse
import myapp.data.entities.network.*
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
    ): Response<TRApiResponse<CamLoginPayload.Login>>

    /**
     * 카메라 Health
     */
    @POST("http://{ip}/health")
    suspend fun health(
        @Path(value = "ip") ip: String
    ): Response<TRApiResponse<CamHealthPayload.Response>>


    /**
     * 시간 업데이트
     */
    @FormUrlEncoded
    @POST("http://{ip}/config/update-time")
    suspend fun updateTime(
        @Path(value = "ip") ip: String,
        @Field("time") timeSeconds: Long
    ): Response<TRApiResponse<Any>>

    /**
     * 카메라 로그아웃
     */
    @POST("http://{ip}/logout")
    suspend fun logout(@Path(value = "ip") ip: String): Response<TRApiResponse<Any>>


    /**
     * 카메라 설정 조회
     */
    @GET("http://{ip}/config")
    suspend fun config(@Path(value = "ip") ip: String): Response<TRApiResponse<CamConfigPayload.Response>>


    /**
     * 녹화파일목록 조회
     */
    @POST("http://{ip}/recording/list")
    suspend fun recordFiles(@Path(value = "ip") ip: String): Response<TRApiResponse<CamRecordFilesPayload.Response>>


    /**
     * 녹화파일 delete
     */
    @POST("http://{ip}/recording/delete")
    @FormUrlEncoded
    suspend fun deleteFile(
        @Path(value = "ip") ip: String,
        @Field("fileName") fileId: String
    ): Response<TRApiResponse<Any>>


    /**
     * 녹화해상도 설정 변경
     */
    @POST("http://{ip}/config/update-recording")
    @FormUrlEncoded
    suspend fun updateRecordingVideoQuality(
        @Path(value = "ip") ip: String,
        @Field("resolution") resolution: String, // 3840x2160
        @Field("fps") fps: Int
    ): Response<TRApiResponse<Any>>


    /**
     * 녹화 OFF
     */
    @POST("http://{ip}/config/update-recording-off")
    suspend fun updateRecordingOff(
        @Path(value = "ip") ip: String,
    ): Response<TRApiResponse<Any>>


    /**
     * 녹화 스케줄
     */
    @POST("http://{ip}/config/update-recording-schedule")
    @FormUrlEncoded
    suspend fun updateRecordingSchedule(
        @Path(value = "ip") ip: String,
        @Field("startAt") startTimeInSeconds: Long, // epochTime in seconds
        @Field("duration") durationInSeconds: Long  // duration in seconds
    ): Response<TRApiResponse<Any>>

    /**
     * 녹화 상태 조회
     */
    @POST("http://{ip}/status/recording")
    suspend fun recordingState(
        @Path(value = "ip") ip: String
    ): Response<TRApiResponse<CamRecordingStatePayload.Response>>

    /**
     * 스트리밍 해상도 설정 변경
     */
    @POST("http://{ip}/config/update-streaming")
    @FormUrlEncoded
    suspend fun updateStreamingVideoQuality(
        @Path(value = "ip") ip: String,
        @Field("resolution") resolution: String, // 3840x2160
        @Field("fps") fps: Int
    ): Response<TRApiResponse<Any>>


    /**
     * 네트워크 설정 변경
     */
    @POST("http://{ip}/config/update-network")
    @FormUrlEncoded
    suspend fun updateNetworkConfig(
        @Path(value = "ip") ip: String,
        @Field("enabled") enabled: String // wifi
    ): Response<TRApiResponse<Any>>


    /**
     * 비밀번호 변경
     */
    @POST("http://{ip}/config/update-pw")
    @FormUrlEncoded
    suspend fun updatePassword(
        @Path(value = "ip") ip: String,
        @Field("pw") pw: String
    ): Response<TRApiResponse<Any>>

    /**
     * 카메라 이름 변경
     */
    @POST("http://{ip}/config/update-camera-name")
    @FormUrlEncoded
    suspend fun updateCameraName(
        @Path(value = "ip") ip: String,
        @Field("cameraName") cameraName: String
    ): Response<TRApiResponse<Any>>


    /**
     * WIFI 설정 변경
     */
    @POST("http://{ip}/config/update-wifi")
    @FormUrlEncoded
    suspend fun updateWifi(
        @Path(value = "ip") ip: String,
        @Field("ssid") ssid: String,
        @Field("pw") pw: String
    ): Response<TRApiResponse<Any>>

    /**
     * execute 재부팅
     */
    @POST("http://{ip}/exec")
    @FormUrlEncoded
    suspend fun exec(
        @Path(value = "ip") ip: String,
        @Field("cmd") cmd: String
    ): Response<TRApiResponse<Any>>
}
