package kr.ohlab.android.permission


import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


open class TRPermissionManager : TRBasePermissionManager() {

    private lateinit var completableDeferred: CompletableDeferred<TRPermissionResult>

    override fun onPermissionResult(permissionResult: TRPermissionResult) {
        completableDeferred.complete(permissionResult)
    }

    companion object {

        private const val TAG = "TRPermissionManager"

        /**
         * A static factory method to request permission from activity.
         *
         * @param activity an instance of [AppCompatActivity]
         * @param requestId Request ID for permission request
         * @param permissions Permission(s) to request
         *
         * @return [TRPermissionResult]
         *
         * Suspends the coroutines until result is available.
         */
        suspend fun requestPermissions(
            activity: AppCompatActivity,
            requestId: Int,
            vararg permissions: String
        ): TRPermissionResult {
            return withContext(Dispatchers.Main) {
                return@withContext requestPermissionsInternal(
                    activity,
                    requestId,
                    *permissions
                )
            }
        }

        /**
         * A static factory method to request permission from fragment.
         *
         * @param fragment an instance of [Fragment]
         * @param requestId Request ID for permission request
         * @param permissions Permission(s) to request
         *
         * @return [TRPermissionResult]
         *
         * Suspends the coroutines until result is available.
         */
        suspend fun requestPermissions(
            fragment: Fragment,
            requestId: Int,
            vararg permissions: String
        ): TRPermissionResult {
            return withContext(Dispatchers.Main) {
                return@withContext requestPermissionsInternal(
                    fragment,
                    requestId,
                    *permissions
                )
            }
        }

        private suspend fun requestPermissionsInternal(
            activityOrFragment: Any,
            requestId: Int,
            vararg permissions: String
        ): TRPermissionResult {
            val fragmentManager = if (activityOrFragment is AppCompatActivity) {
                activityOrFragment.supportFragmentManager
            } else {
                (activityOrFragment as Fragment).childFragmentManager
            }
            return if (fragmentManager.findFragmentByTag(TAG) != null) {
                val permissionManager = fragmentManager.findFragmentByTag(TAG) as TRPermissionManager
                permissionManager.completableDeferred = CompletableDeferred()
                permissionManager.requestPermissions(requestId, *permissions)
                permissionManager.completableDeferred.await()
            } else {
                val permissionManager = TRPermissionManager().apply {
                    completableDeferred = CompletableDeferred()
                }
                fragmentManager.beginTransaction().add(permissionManager, TAG).commitNow()
                permissionManager.requestPermissions(requestId, *permissions)
                permissionManager.completableDeferred.await()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (completableDeferred.isActive) {
            completableDeferred.cancel()
        }
    }
}
