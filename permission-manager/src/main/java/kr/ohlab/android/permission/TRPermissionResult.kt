package kr.ohlab.android.permission

sealed class TRPermissionResult(val requestCode: Int) {
    class PermissionGranted(requestCode: Int) : TRPermissionResult(requestCode)

    class PermissionDenied(
        requestCode: Int,
        val deniedPermissions: List<String>
    ) : TRPermissionResult(requestCode)

    class ShowRational(requestCode: Int) : TRPermissionResult(requestCode)

    class PermissionDeniedPermanently(
        requestCode: Int,
        val permanentlyDeniedPermissions: List<String>
    ) : TRPermissionResult(requestCode)
}