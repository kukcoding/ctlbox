@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package myapp.util

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

object TRLayouts {
    private const val LOG_TAG = "TRLayouts"

    fun paddingZero(vararg views: View) {
        for (v in views) {
            v.setPadding(0, 0, 0, 0)
        }
    }

    fun padding(left: Int, top: Int, right: Int, bottom: Int, vararg views: View) {
        for (v in views) {
            v.setPadding(left, top, right, bottom)
        }
    }

    fun padding(padding: Int, vararg views: View) {
        for (v in views) {
            v.setPadding(padding, padding, padding, padding)
        }
    }

    fun paddingLeftRight(left: Int, right: Int, vararg views: View) {
        for (v in views) {
            val top = v.paddingTop
            val bottom = v.paddingBottom
            v.setPadding(left, top, right, bottom)
        }
    }

    fun paddingLeftRight(leftRight: Int, vararg views: View) {
        for (v in views) {
            val top = v.paddingTop
            val bottom = v.paddingBottom
            v.setPadding(leftRight, top, leftRight, bottom)
        }
    }

    fun paddingTopBottom(top: Int, bottom: Int, vararg views: View) {
        for (v in views) {
            val left = v.paddingLeft
            val right = v.paddingRight
            v.setPadding(left, top, right, bottom)
        }
    }

    fun paddingTopBottom(topBottom: Int, vararg views: View) {
        for (v in views) {
            val left = v.paddingLeft
            val right = v.paddingRight
            v.setPadding(left, topBottom, right, topBottom)
        }
    }


    fun paddingBottom(bottom: Number, vararg views: View) {
        val bottomPadding = bottom.toInt()
        for (v in views) {
            if (v.paddingBottom != bottomPadding) {
                val left = v.paddingLeft
                val right = v.paddingRight
                val top = v.paddingTop
                v.setPadding(left, top, right, bottomPadding)
            }
        }
    }

    fun paddingLeft(left: Int, vararg views: View) {
        for (v in views) {
            val right = v.paddingRight
            val top = v.paddingTop
            val bottom = v.paddingBottom
            v.setPadding(left, top, right, bottom)
        }
    }

    fun paddingTop(top: Int, vararg views: View) {
        for (v in views) {
            val left = v.paddingLeft
            val right = v.paddingRight
            val bottom = v.paddingBottom
            v.setPadding(left, top, right, bottom)
        }
    }

    fun paddingRight(right: Int, vararg views: View) {
        for (v in views) {
            val left = v.paddingLeft
            val top = v.paddingTop
            val bottom = v.paddingBottom
            v.setPadding(left, top, right, bottom)
        }
    }

    fun margin(size: Int, vararg views: View) {
        margin(
            left = size,
            top = size,
            right = size,
            bottom = size,
            views = *views
        )
    }

    fun margin(left: Int, top: Int, right: Int, bottom: Int, vararg views: View) {

        for (v in views) {

            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.topMargin = top
                layoutParams.leftMargin = left
                layoutParams.rightMargin = right
                layoutParams.bottomMargin = bottom
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }


    fun marginTop(margin: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.topMargin = margin
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }

    fun marginBottom(v: View): Int {
        val layoutParams = v.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            return layoutParams.bottomMargin
        } else {
            Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
        }
        return 0
    }

    fun marginBottom(margin: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.bottomMargin = margin
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }


    fun marginLeft(margin: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.leftMargin = margin
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }

    fun marginRight(margin: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.rightMargin = margin
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }

    fun marginLeftRight(leftRight: Int, vararg views: View) {
        marginLeftRight(left = leftRight, right = leftRight, views = *views)
    }

    fun marginLeftRight(left: Int, right: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.leftMargin = left
                layoutParams.rightMargin = right
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }

    fun marginTopBottom(top: Int, bottom: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.topMargin = top
                layoutParams.bottomMargin = bottom
                v.layoutParams = layoutParams
            } else {
                Log.w(LOG_TAG, "layoutParam is not MarginLayoutParams : $v")
            }
        }
    }

    fun width(width: Number, vararg views: View) {
        val w = width.toInt()

        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams.width != w) {
                layoutParams.width = w
                v.layoutParams = layoutParams
            }
        }
    }

    fun widthMatchParent(vararg views: View) {
        width(ViewGroup.LayoutParams.MATCH_PARENT, *views)
    }

    fun widthWrapContent(vararg views: View) {
        width(ViewGroup.LayoutParams.WRAP_CONTENT, *views)
    }

    fun heightMatchParent(vararg views: View) {
        height(ViewGroup.LayoutParams.MATCH_PARENT, *views)
    }

    fun heightWrapContent(vararg views: View) {
        height(ViewGroup.LayoutParams.WRAP_CONTENT, *views)
    }

    fun height(height: Number, vararg views: View) {
        val h = height.toInt()
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams.height != h) {
                layoutParams.height = h
                v.layoutParams = layoutParams
            }
        }
    }

    fun resize(width: Number, height: Number, vararg views: View) {
        val w = width.toInt()
        val h = height.toInt()

        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams.width != w || layoutParams.height != h) {
                layoutParams.width = w
                layoutParams.height = h
                v.layoutParams = layoutParams
            }
        }
    }

    /**
     * square size
     */
    fun resize(size: Int, vararg views: View) {
        for (v in views) {
            val layoutParams = v.layoutParams
            if (layoutParams.width != size || layoutParams.height != size) {
                layoutParams.width = size
                layoutParams.height = size
                v.layoutParams = layoutParams
            }
        }
    }

    fun aspectHeight(requiredWidth: Number, ratioWidth: Number, ratioHeight: Number): Float {
        val ratio = ratioHeight.toFloat() / ratioWidth.toFloat()
        return requiredWidth.toFloat() * ratio
    }

    fun aspectWidth(requiredHeight: Number, ratioWidth: Number, ratioHeight: Number): Float {
        val ratio = ratioWidth.toFloat() / ratioHeight.toFloat()
        return requiredHeight.toFloat() * ratio
    }

    fun resizeAspectByWidth(
        requiredWidth: Int,
        ratioWidth: Number,
        ratioHeight: Number,
        vararg views: View
    ) {
        resize(
            requiredWidth,
            aspectHeight(requiredWidth, ratioWidth, ratioHeight).toInt(),
            *views
        )
    }

    fun resizeAspectByHeight(
        requiredHeight: Int,
        ratioWidth: Number,
        ratioHeight: Number,
        vararg views: View
    ) {
        resize(
            aspectWidth(
                requiredHeight,
                ratioWidth,
                ratioHeight
            ).toInt(), requiredHeight, *views
        )
    }

    fun createLinearLayout(
        context: Context,
        layoutConfigBlock: (LinearLayout.LayoutParams) -> Unit
    ): LinearLayout {
        return createLinearLayout(
            context,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            layoutConfigBlock
        )
    }

    fun createLinearLayout(
        context: Context,
        width: Int,
        height: Int,
        layoutConfigBlock: (LinearLayout.LayoutParams) -> Unit
    ): LinearLayout {
        val layout = LinearLayout(context)
        layout.layoutParams = LinearLayout.LayoutParams(width, height).also {
            layoutConfigBlock(it)
        }

        return layout
    }
}
