package myapp.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kr.ohlab.android.permission.TRPermissionResult
import kr.ohlab.android.permission.TRPermissions

enum class PermissionConfig(
    val perm: String,
    @StringRes val permissionNameResId: Int,
    @StringRes val usageResId: Int,
    @StringRes val rationalMsgResId: Int,
    @StringRes val gotoSettingMsgResId: Int
) {

    WRITE_EXTERNAL_STORAGE(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        R.string.perm_storage_name,
        R.string.perm_storage_usage,
        R.string.msg_need_perm_storage,
        R.string.msg_confirm_need_perm_storage_and_do_u_want_setting
    ),

    READ_EXTERNAL_STORAGE(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        R.string.perm_storage_name,
        R.string.perm_storage_usage,
        R.string.msg_need_perm_storage,
        R.string.msg_confirm_need_perm_storage_and_do_u_want_setting
    ),

    /**
     * 카메라
     */
    CAMERA(
        Manifest.permission.CAMERA,
        R.string.perm_camera_name,
        R.string.perm_camera_usage,
        R.string.perm_camera_rational_msg,
        R.string.perm_camera_goto_setting_msg
    );

    val isSettingPageMovable: Boolean
        get() = this.gotoSettingMsgResId != 0

    fun mandatoryPermissions(): List<PermissionConfig> {
        return emptyList()
    }

    fun optionalPermissions(): List<PermissionConfig> {
        return listOf(WRITE_EXTERNAL_STORAGE)
    }

    fun mandatory(): Boolean {
        return mandatoryPermissions().contains(this)
    }

    /**
     * 권한 허용 체크
     */
    fun isGranted(ctx: Context): Boolean =
        ContextCompat.checkSelfPermission(ctx, this.perm) == PackageManager.PERMISSION_GRANTED

    /**
     * 권한 요청
     */
    suspend fun request(activity: AppCompatActivity): TRPermissionResult {
        return TRPermissions.suspendRequestPermission(activity, this.perm)
    }

    suspend fun request(fragment: Fragment): TRPermissionResult {
        return TRPermissions.suspendRequestPermission(fragment, this.perm)
    }


    companion object {
        fun findByPermOrNull(perm: String?): PermissionConfig? {
            if (perm.isNullOrBlank()) return null
            return values().firstOrNull { it.perm == perm }
        }
    }
}
