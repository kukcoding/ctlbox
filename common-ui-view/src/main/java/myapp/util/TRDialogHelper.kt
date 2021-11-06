@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package myapp.util

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object TRDialogHelper {
    fun builder(context: Context, title: CharSequence?, message: CharSequence?): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(false)
        }
    }

    fun builder(context: Context, message: CharSequence?): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context).apply {
            setMessage(message)
            setCancelable(false)
        }
    }

    fun builder(context: Context, @StringRes messageId: Int): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context).apply {
            setMessage(messageId)
            setCancelable(false)
        }
    }

    fun builder(context: Context, @StringRes titleId: Int, @StringRes messageId: Int): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context).apply {
            setTitle(titleId)
            setMessage(messageId)
            setCancelable(false)
        }
    }

    private fun alertButtons(builder: MaterialAlertDialogBuilder, callback: Action1<DialogInterface>) {
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ -> callback(dialogInterface) }
    }

    fun alert(
        context: Context,
        title: CharSequence?,
        message: CharSequence?,
        callback: Action1<DialogInterface>
    ): AlertDialog {
        return builder(context, title, message)
            .also { alertButtons(it, callback) }.show()
    }

    fun alert(
        context: Context,
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        callback: Action1<DialogInterface>
    ): AlertDialog {
        return builder(context, titleId, messageId)
            .also { alertButtons(it, callback) }.show()
    }

    fun alert(context: Context, message: CharSequence?, callback: Action1<DialogInterface>): AlertDialog {
        return builder(context, message)
            .also { alertButtons(it, callback) }.show()
    }

    fun alert(context: Context, @StringRes messageId: Int, callback: Action1<DialogInterface>): AlertDialog {
        return builder(context, messageId)
            .also { alertButtons(it, callback) }.show()
    }

    private fun confirmOkButtons(builder: MaterialAlertDialogBuilder, callback: Action2<DialogInterface, Boolean>) {
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ -> callback(dialogInterface, true) }
        builder.setNeutralButton(android.R.string.cancel) { dialogInterface, _ -> callback(dialogInterface, false) }
    }

    fun confirmOk(
        context: Context,
        @StringRes messageId: Int,
        callback: Action2<DialogInterface, Boolean>
    ): AlertDialog {
        return builder(context, messageId)
            .also { confirmOkButtons(it, callback) }.show()
    }

    fun confirmOk(
        context: Context,
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        callback: Action2<DialogInterface, Boolean>
    ): AlertDialog {
        return builder(context, titleId, messageId)
            .also { confirmOkButtons(it, callback) }.show()
    }

    fun confirmOk(context: Context, message: CharSequence?, callback: Action2<DialogInterface, Boolean>): AlertDialog {
        return builder(context, message)
            .also { confirmOkButtons(it, callback) }.show()
    }

    fun confirmOk(
        context: Context,
        title: CharSequence?,
        message: CharSequence?,
        callback: Action2<DialogInterface, Boolean>
    ): AlertDialog {
        return builder(context, title, message)
            .also { confirmOkButtons(it, callback) }.show()
    }


    private fun confirmYesButtons(builder: MaterialAlertDialogBuilder, callback: Action2<DialogInterface, Boolean>) {
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ -> callback(dialogInterface, true) }
        builder.setNeutralButton(android.R.string.cancel) { dialogInterface, _ -> callback(dialogInterface, false) }
    }

    fun confirmYes(
        context: Context,
        @StringRes messageId: Int,
        callback: Action2<DialogInterface, Boolean>
    ): AlertDialog {
        return builder(context, messageId)
            .also { confirmYesButtons(it, callback) }.show()
    }

    fun confirmYes(
        context: Context,
        @StringRes titleId: Int,
        @StringRes messageId: Int,
        callback: Action2<DialogInterface, Boolean>
    ): AlertDialog {
        return builder(context, titleId, messageId)
            .also { confirmYesButtons(it, callback) }.show()
    }


    fun confirmYes(context: Context, message: CharSequence?, callback: Action2<DialogInterface, Boolean>): AlertDialog {
        return builder(context, message)
            .also { confirmYesButtons(it, callback) }.show()
    }

    fun confirmYes(
        context: Context,
        title: CharSequence?,
        message: CharSequence?,
        callback: Action2<DialogInterface, Boolean>
    ): AlertDialog {
        return builder(context, title, message)
            .also { confirmYesButtons(it, callback) }.show()
    }

}
