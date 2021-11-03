package myapp.sys

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ctlbox.AppLifecycleObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

class AppLifecycleObserverImpl @Inject constructor(
    @ApplicationContext context: Context
) : DefaultLifecycleObserver, AppLifecycleObserver {
    private val foregroundStateFlow = MutableStateFlow(false)

    override fun observeForeground(): Flow<Boolean> = foregroundStateFlow
    override fun onStart(owner: LifecycleOwner) {
        // super.onStart(owner)
        Timber.d("Returning to foreground…")
        foregroundStateFlow.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        // super.onStop(owner)
        Timber.d("Moving to background…")
        foregroundStateFlow.value = false
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    fun onMoveToForeground() {
//        Timber.d("Returning to foreground…")
//        foregroundStateFlow.value = true
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    fun onMoveToBackground() {
//        Timber.d("Moving to background…")
//        foregroundStateFlow.value = false
//    }
}
