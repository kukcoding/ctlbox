@file:Suppress("unused", "NOTHING_TO_INLINE")


package myapp.extensions

import android.app.Activity
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import myapp.util.Action0
import myapp.util.Action1
import myapp.util.TRDialogHelper

fun Activity?.alert(title: CharSequence, message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.alert(it, title, message) { callback?.invoke() }
}

fun Activity?.alert(message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.alert(it, message) { callback?.invoke() }
}

fun Activity?.alert(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.alert(it, titleId, messageId) { callback?.invoke() }
}

fun Activity?.alert(@StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.alert(it, messageId) { callback?.invoke() }
}

fun Activity?.confirmOk(title: CharSequence, message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, title, message) { _, isPositive -> callback?.invoke(isPositive) }
}

fun Activity?.confirmOk(message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, message) { _, isPositive -> callback?.invoke(isPositive) }
}


fun Activity?.confirmOk(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, titleId, messageId) { _, isPositive -> callback?.invoke(isPositive) }
}


fun Activity?.confirmOk(@StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, messageId) { _, isPositive -> callback?.invoke(isPositive) }
}

fun Activity?.confirmOkIfPositive(title: CharSequence, message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, title, message) { _, isPositive -> if (isPositive) callback?.invoke() }
}

fun Activity?.confirmOkIfPositive(message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, message) { _, isPositive -> if (isPositive) callback?.invoke() }
}

fun Activity?.confirmOkIfPositive(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, titleId, messageId) { _, isPositive -> if (isPositive) callback?.invoke() }
}

fun Activity?.confirmOkIfPositive(@StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, messageId) { _, isPositive -> if (isPositive) callback?.invoke() }
}


fun Activity?.confirmYes(title: CharSequence, message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmYes(it, title, message) { _, isPositive -> callback?.invoke(isPositive) }
}

fun Activity?.confirmYes(message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmYes(it, message) { _, isPositive -> callback?.invoke(isPositive) }
}


fun Activity?.confirmYes(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmYes(it, titleId, messageId) { _, isPositive -> callback?.invoke(isPositive) }
}

fun Activity?.confirmYes(@StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmYes(it, messageId) { _, isPositive -> callback?.invoke(isPositive) }
}

fun Activity?.confirmYesIfPositive(title: CharSequence, message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, title, message) { _, isPositive -> if (isPositive) callback?.invoke() }
}

fun Activity?.confirmYesIfPositive(message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, message) { _, isPositive -> if (isPositive) callback?.invoke() }
}

fun Activity?.confirmYesIfPositive(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, titleId, messageId) { _, isPositive -> if (isPositive) callback?.invoke() }
}

fun Activity?.confirmYesIfPositive(@StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    TRDialogHelper.confirmOk(it, messageId) { _, isPositive -> if (isPositive) callback?.invoke() }
}


fun Fragment?.alert(title: CharSequence, message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.alert(title, message, callback)
}

fun Fragment?.alert(message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.alert(message, callback)
}

fun Fragment?.alert(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action0? = null): AlertDialog? =
    this?.let {
        it.activity?.alert(titleId, messageId, callback)
    }

fun Fragment?.alert(@StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.alert(messageId, callback)
}


fun Fragment?.confirmOk(title: CharSequence, message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmOk(title, message, callback)
    }

fun Fragment?.confirmOk(message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    it.activity?.confirmOk(message, callback)
}


fun Fragment?.confirmOk(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmOk(titleId, messageId, callback)
    }

fun Fragment?.confirmOk(@StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    it.activity?.confirmOk(messageId, callback)
}


fun Fragment?.confirmOkIfPositive(title: CharSequence, message: CharSequence, callback: Action0? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmOkIfPositive(title, message, callback)
    }

fun Fragment?.confirmOkIfPositive(message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.confirmOkIfPositive(message, callback)
}

fun Fragment?.confirmOkIfPositive(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action0? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmOkIfPositive(titleId, messageId, callback)
    }

fun Fragment?.confirmOkIfPositive(@StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.confirmOkIfPositive(messageId, callback)
}


fun Fragment?.confirmYes(title: CharSequence, message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmYes(title, message, callback)
    }

fun Fragment?.confirmYes(message: CharSequence, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    it.activity?.confirmYes(message, callback)
}


fun Fragment?.confirmYes(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmYes(titleId, messageId, callback)
    }

fun Fragment?.confirmYes(@StringRes messageId: Int, callback: Action1<Boolean>? = null): AlertDialog? = this?.let {
    it.activity?.confirmYes(messageId, callback)
}


fun Fragment?.confirmYesIfPositive(
    title: CharSequence,
    message: CharSequence,
    callback: Action0? = null
): AlertDialog? = this?.let {
    it.activity?.confirmYesIfPositive(title, message, callback)
}

fun Fragment?.confirmYesIfPositive(message: CharSequence, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.confirmYesIfPositive(message, callback)
}

fun Fragment?.confirmYesIfPositive(@StringRes titleId: Int, @StringRes messageId: Int, callback: Action0? = null): AlertDialog? =
    this?.let {
        it.activity?.confirmYesIfPositive(titleId, messageId, callback)
    }

fun Fragment?.confirmYesIfPositive(@StringRes messageId: Int, callback: Action0? = null): AlertDialog? = this?.let {
    it.activity?.confirmYesIfPositive(messageId, callback)
}


