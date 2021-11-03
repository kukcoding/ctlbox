package myapp.extensions

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableDeferred
import myapp.util.TRDialogHelper


suspend fun Context.suspendAlert(title: CharSequence?, message: CharSequence?) {
    val result = CompletableDeferred<Unit>()
    TRDialogHelper.alert(this, title, message) { result.complete(Unit) }
    result.await()
}


suspend fun Fragment.suspendAlert(title: CharSequence?, message: CharSequence?) {
    (context ?: return).suspendAlert(title, message)
}

suspend fun Context.suspendAlert(message: CharSequence?) {
    val result = CompletableDeferred<Unit>()
    TRDialogHelper.alert(this, message) { result.complete(Unit) }
    result.await()
}

suspend fun Fragment.suspendAlert(message: CharSequence?) {
    (context ?: return).suspendAlert(message)
}


suspend fun Context.suspendAlert(@StringRes titleId: Int, @StringRes messageId: Int) {
    val result = CompletableDeferred<Unit>()
    TRDialogHelper.alert(this, titleId, messageId) { result.complete(Unit) }
    result.await()
}


suspend fun Fragment.suspendAlert(@StringRes titleId: Int, @StringRes messageId: Int) {
    (context ?: return).suspendAlert(titleId, messageId)
}


suspend fun Context.suspendAlert(@StringRes messageId: Int) {
    val result = CompletableDeferred<Unit>()
    TRDialogHelper.alert(this, messageId) { result.complete(Unit) }
    result.await()
}

suspend fun Fragment.suspendAlert(@StringRes messageId: Int) {
    (context ?: return).suspendAlert(messageId)
}


suspend fun Context.suspendConfirmYes(title: CharSequence?, message: CharSequence?): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, title, message) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}

suspend fun Fragment.suspendConfirmYes(title: CharSequence?, message: CharSequence?): Boolean {
    return (context ?: return false).suspendConfirmYes(title, message)
}

suspend fun Context.suspendConfirmYes(message: CharSequence?): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, message) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}

suspend fun Fragment.suspendConfirmYes(message: CharSequence?): Boolean {
    return (context ?: return false).suspendConfirmYes(message)
}

suspend fun Context.suspendConfirmYes(@StringRes titleId: Int, @StringRes messageId: Int): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, titleId, messageId) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}

suspend fun Fragment.suspendConfirmYes(@StringRes titleId: Int, @StringRes messageId: Int): Boolean {
    return (context ?: return false).suspendConfirmYes(titleId, messageId)
}


suspend fun Context.suspendConfirmYes(@StringRes messageId: Int): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, messageId) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}


suspend fun Fragment.suspendConfirmYes(@StringRes messageId: Int): Boolean {
    return (context ?: return false).suspendConfirmYes(messageId)
}


suspend fun Context.suspendConfirmOk(title: CharSequence?, message: CharSequence?): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, title, message) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}


suspend fun Fragment.suspendConfirmOk(title: CharSequence?, message: CharSequence?): Boolean {
    return (context ?: return false).suspendConfirmOk(title, message)
}

suspend fun Context.suspendConfirmOk(message: CharSequence?): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, message) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}

suspend fun Fragment.suspendConfirmOk(message: CharSequence?): Boolean {
    return (context ?: return false).suspendConfirmOk(message)
}

suspend fun Context.suspendConfirmOk(@StringRes titleId: Int, @StringRes messageId: Int): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, titleId, messageId) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}

suspend fun Fragment.suspendConfirmOk(@StringRes titleId: Int, @StringRes messageId: Int): Boolean {
    return (context ?: return false).suspendConfirmOk(titleId, messageId)
}


suspend fun Context.suspendConfirmOk(@StringRes messageId: Int): Boolean {
    val result = CompletableDeferred<Boolean>()
    TRDialogHelper.confirmOk(this, messageId) { _, isPositive ->
        result.complete(isPositive)
    }
    return result.await()
}


suspend fun Fragment.suspendConfirmOk(@StringRes messageId: Int): Boolean {
    return (context ?: return false).suspendConfirmOk(messageId)
}

