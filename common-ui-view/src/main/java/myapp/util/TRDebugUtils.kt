@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package myapp.util


import android.content.Intent
import android.os.Bundle

object TRDebugUtils {
    fun toStringFromBundle(b: Bundle?): String {
        return toStringFromBundle(null, b)
    }

    fun toStringFromBundle(logPrefix: String?, b: Bundle?): String {
        val sb = StringBuilder()
        internalToStringFromBundle(logPrefix, b, sb)
        return sb.toString()
    }

    fun toStringFromIntent(intent: Intent?): String {
        if (intent == null)
            return "intent=null"
        val sb = StringBuilder()
        internalToStringFromIntent(intent, sb)
        return sb.toString()
    }

    private fun internalToStringFromBundle(logPrefix: String?, b: Bundle?, sb: StringBuilder) {
        var logPre = logPrefix
        if (logPre == null)
            logPre = ""
        if (b == null) {
            sb.append(logPre)
            sb.append("<null>")
            return
        }

        for (key in b.keySet()) {
            val value = b.get(key)
            sb.append(logPre)
            sb.append(String.format("key='%s' value=[%s]\n", key, "" + b.get(key)!!))
            if (value is Bundle) {
                val newPrefix = "    [$key]"
                sb.append(
                    toStringFromBundle(
                        newPrefix,
                        value
                    )
                )
            }

            if (sb.length > 4096) {
                sb.append(logPre)
                sb.append(" to many log. skip!\n")
                return
            }
        }
    }


    private fun internalToStringFromIntent(intent: Intent, sb: StringBuilder) {
        sb.append("" + intent)
        sb.append("\n")

        sb.append("action=" + intent.action)
        sb.append("\n")

        sb.append("data=" + intent.data)

        nameValue(sb, "type", intent.type)
        nameValue(sb, "package", intent.getPackage())
        nameValue(sb, "scheme", intent.scheme)
        nameValue(sb, "categories", intent.categories)

        sb.append("\n")
        sb.append("extras=" + intent.extras)
        if (intent.extras != null) {
            sb.append("\n")
            internalToStringFromBundle(
                "...... ",
                intent.extras,
                sb
            )
        }
        sb.append("\n")
    }

    private fun nameValue(sb: StringBuilder, name: String, value: Any?): Boolean {
        sb.append(if (value == null) "," else "\n")
        sb.append("$name=$value")

        return value != null
    }

    fun toStringOnActivityResult(
        requestCode: Int, resultCode: Int,
        resultData: Intent?
    ): String {
        return String.format(
            "onActivityResult(requestCode:%d, resultCode:%d) resultData=%s",
            requestCode,
            resultCode,
            toStringFromIntent(resultData)
        )
    }
}
