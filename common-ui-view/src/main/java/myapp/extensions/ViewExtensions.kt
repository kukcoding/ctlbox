package myapp.extensions

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.ldralighieri.corbind.view.clicks
import java.lang.Runnable
import kotlin.coroutines.resume

fun ViewGroup.beginDelayedTransition(duration: Long = 200) {
    TransitionManager.beginDelayedTransition(this, AutoTransition().apply { setDuration(duration) })
}

fun View.getBounds(rect: Rect) {
    rect.set(left, top, right, bottom)
}

/**
 * Call [View.requestApplyInsets] in a safe away. If we're attached it calls it straight-away.
 * If not it sets an [View.OnAttachStateChangeListener] and waits to be attached before calling
 * [View.requestApplyInsets].
 */
fun View.requestApplyInsetsWhenAttached() = doOnAttach {
    it.requestApplyInsets()
}

fun View.doOnAttach(f: (View) -> Unit) {
    if (isAttachedToWindow) {
        f(this)
    } else {
        addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    f(v)
                    removeOnAttachStateChangeListener(this)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    removeOnAttachStateChangeListener(this)
                }
            }
        )
    }
}

/**
 * Allows easy listening to layout passing. Return [true] if you need the listener to keep being
 * attached.
 */
inline fun <V : View> V.doOnLayouts(crossinline action: (view: V) -> Boolean) {
    addOnLayoutChangeListener(
        object : View.OnLayoutChangeListener {
            @Suppress("UNCHECKED_CAST")
            override fun onLayoutChange(
                view: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (!action(view as V)) {
                    view.removeOnLayoutChangeListener(this)
                }
            }
        }
    )
}

/**
 * Allows easy listening to layout passing. Return [true] if you need the listener to keep being
 * attached.
 */
inline fun View.doOnSizeChange(crossinline action: (view: View) -> Boolean) {
    addOnLayoutChangeListener(
        object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                view: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if ((bottom - top) != (oldBottom - oldTop) || (right - left) != (oldRight - oldLeft)) {
                    if (!action(view)) {
                        view.removeOnLayoutChangeListener(this)
                    }
                }
            }
        }
    )
}

suspend fun View.awaitNextLayout() = suspendCancellableCoroutine<Unit> { cont ->
    val listener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            // Remove the listener
            view.removeOnLayoutChangeListener(this)
            // And resume the continuation
            cont.resume(Unit)
        }
    }
    // If the coroutine is cancelled, remove the listener
    cont.invokeOnCancellation { removeOnLayoutChangeListener(listener) }
    // And finally add the listener to view
    addOnLayoutChangeListener(listener)
}

suspend fun View.awaitLayout() {
    if (!isLaidOut) {
        awaitNextLayout()
    }
}

suspend fun View.awaitPreDraw() = suspendCancellableCoroutine<Unit> { cont ->
    val listener = OneShotPreDrawListener.add(this) {
        cont.resume(Unit)
    }
    // If the coroutine is cancelled, remove the listener
    cont.invokeOnCancellation {
        listener.removeListener()
    }
}

suspend fun View.awaitAnimationFrame() = suspendCancellableCoroutine<Unit> { cont ->
    val runnable = Runnable {
        cont.resume(Unit)
    }
    // If the coroutine is cancelled, remove the callback
    cont.invokeOnCancellation { removeCallbacks(runnable) }
    // And finally post the runnable
    postOnAnimation(runnable)
}

fun View.resetAnimation(visibility: Int? = null) {
    this.alpha = 1f
    this.translationX = 0f
    this.translationY = 0f
    this.translationZ = 0f
    this.scaleX = 1f
    this.scaleY = 1f
    this.rotationX = 0f
    this.rotationY = 0f
    this.rotation = 0f
    if (visibility != null) {
        this.visibility = visibility
    }
}

fun View.throttleClickLive(windowDuration: Long = 400): LiveData<Unit> {
    return this.clicks().throttleFirst(windowDuration).asLiveData()
}

/**
 * 왜 flow throttleFirst가 없나?
 */
//fun <T> Flow<T>.throttleFirst2(periodMillis: Long): Flow<T> {
//    // require(periodMillis > 0) { "period should be positive" }
//    return flow {
//        var lastTime = 0L
//        collect { value ->
//            val currentTime = System.currentTimeMillis()
//            if (currentTime - lastTime >= periodMillis) {
//                lastTime = currentTime
//                emit(value)
//            }
//        }
//    }
//}

fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> {
    var job: Job = Job().apply { complete() }

    return onCompletion { job.cancel() }.run {
        flow {
            coroutineScope {
                collect { value ->
                    if (!job.isActive) {
                        emit(value)
                        job = launch { delay(windowDuration) }
                    }
                }
            }
        }
    }
}
