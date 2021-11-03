package myapp.ui.widget


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View.MeasureSpec.EXACTLY
import androidx.appcompat.widget.AppCompatImageView
import myapp.ui.common.R

class TRRoundedImageView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var widthWeight: Int = 0
    private var heightWeight: Int = 0

    private var topLeftCornerRadius = DEFAULT_RADIUS
    private var topRightCornerRadius = DEFAULT_RADIUS
    private var bottomLeftCornerRadius = DEFAULT_RADIUS
    private var bottomRightCornerRadius = DEFAULT_RADIUS
    //private float           cornerRadius     = DEFAULT_RADIUS;
    private var borderWidth = DEFAULT_BORDER_WIDTH

    private var borderColors: ColorStateList = ColorStateList.valueOf(TRRoundedDrawable.DEFAULT_BORDER_COLOR)
    private var isOval = false
    private var mutateBackground = false
    private var tileModeX: Shader.TileMode =
        DEFAULT_TILE_MODE
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    private var tileModeY: Shader.TileMode =
        DEFAULT_TILE_MODE
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    private var mColorFilter: ColorFilter? = null
    private var mHasColorFilter = false
    private var mColorMod = false

    private var mResource: Int = 0
    private var mDrawable: Drawable? = null
    private var mBackgroundDrawable: Drawable? = null

    private var mScaleType: ScaleType? = null

    private val isAllZeroPartialCorners: Boolean
        get() = topLeftCornerRadius <= 0 &&
            topRightCornerRadius <= 0 &&
            bottomLeftCornerRadius <= 0 &&
            bottomRightCornerRadius <= 0

    private val isAllSamePartialCorners: Boolean
        get() {
            if (topLeftCornerRadius != topRightCornerRadius) {
                return false
            }
            if (topLeftCornerRadius != bottomLeftCornerRadius) {
                return false
            }
            return topLeftCornerRadius == bottomRightCornerRadius
        }

    private var customInitilized = false

    var borderColor: Int
        get() = borderColors.defaultColor
        set(color) = setBorderColor(ColorStateList.valueOf(color))


    init {
        //Log.d(TAG, "XXX TRRoundedImageView")
        customInit(context, attrs, defStyleRes)
    }

    private fun customInit(context: Context, attrs: AttributeSet?, defStyle: Int) {
        //Log.d(TAG, "XXX customInit")
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.TRRoundedImageView,
            defStyle,
            0
        )

        val index = a.getInt(R.styleable.TRRoundedImageView_android_scaleType, -1)
        scaleType = if (index >= 0) {
            SCALE_TYPES[index]
        } else {
            // default scaletype to FIT_CENTER
            ScaleType.FIT_CENTER
        }

        val cornerRadius = a.getDimensionPixelSize(
            R.styleable.TRRoundedImageView_riv_corner_radius,
            -1
        ).toFloat()
        borderWidth =
            a.getDimensionPixelSize(R.styleable.TRRoundedImageView_riv_border_width, -1).toFloat()

        topLeftCornerRadius =
            a.getDimensionPixelSize(R.styleable.TRRoundedImageView_riv_topLeftCorner_radius, -1).toFloat()
        topRightCornerRadius =
            a.getDimensionPixelSize(R.styleable.TRRoundedImageView_riv_topRightCorner_radius, -1).toFloat()
        bottomLeftCornerRadius =
            a.getDimensionPixelSize(R.styleable.TRRoundedImageView_riv_bottomLeftCorner_radius, -1).toFloat()
        bottomRightCornerRadius =
            a.getDimensionPixelSize(R.styleable.TRRoundedImageView_riv_bottomRightCorner_radius, -1).toFloat()

        widthWeight = a.getInteger(R.styleable.TRRoundedImageView_riv_widthWeight, 0)
        heightWeight = a.getInteger(R.styleable.TRRoundedImageView_riv_heightWeight, 0)

        /**
         * cornerRadius를 지정했으면 partialCornerRadius는 무시됨
         */
        if (cornerRadius >= 0) {
            topLeftCornerRadius = cornerRadius
            topRightCornerRadius = cornerRadius
            bottomLeftCornerRadius = cornerRadius
            bottomRightCornerRadius = cornerRadius
        } else {
            topLeftCornerRadius = maxOf(topLeftCornerRadius, 0f)
            topRightCornerRadius = maxOf(topRightCornerRadius, 0f)
            bottomLeftCornerRadius = maxOf(bottomLeftCornerRadius, 0f)
            bottomRightCornerRadius = maxOf(bottomRightCornerRadius, 0f)
        }

        borderWidth = maxOf(borderWidth, 0f)

        a.getColorStateList(R.styleable.TRRoundedImageView_riv_border_color)?.let {
            borderColors = it
        }

        mutateBackground = a.getBoolean(
            R.styleable.TRRoundedImageView_riv_mutate_background,
            false
        )
        isOval = a.getBoolean(R.styleable.TRRoundedImageView_riv_oval, false)

        var tileModeInt = a.getInt(
            R.styleable.TRRoundedImageView_riv_tile_mode,
            TILE_MODE_UNDEFINED
        )
        if (tileModeInt != TILE_MODE_UNDEFINED) {
            this.tileModeX = parseTileMode(
                tileModeInt
            ) ?: DEFAULT_TILE_MODE
            this.tileModeY = parseTileMode(
                tileModeInt
            ) ?: DEFAULT_TILE_MODE
        }

        tileModeInt = a.getInt(
            R.styleable.TRRoundedImageView_riv_tile_mode_x,
            TILE_MODE_UNDEFINED
        )
        if (tileModeInt != TILE_MODE_UNDEFINED)
            this.tileModeX = parseTileMode(
                tileModeInt
            ) ?: DEFAULT_TILE_MODE

        tileModeInt = a.getInt(
            R.styleable.TRRoundedImageView_riv_tile_mode_y,
            TILE_MODE_UNDEFINED
        )
        if (tileModeInt != TILE_MODE_UNDEFINED)
            this.tileModeY = parseTileMode(
                tileModeInt
            ) ?: DEFAULT_TILE_MODE

        //Log.d(TAG, "XXX TILE_MODE_X=" + this.tileModeX)
        //Log.d(TAG, "XXX TILE_MODE_Y=" + this.tileModeY)
        customInitilized = true
        super.getDrawable()?.let {
            //Log.d(TAG, "XXX DRAWABLE을 다시 그린다");
            setImageDrawable(it)
        }
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(true)
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    /**
     * Return the current scale type in use by this ImageView.
     *
     * @attr ref android.R.styleable#ImageView_scaleType
     * @see ScaleType
     */
    override fun getScaleType(): ScaleType? {
        return mScaleType
    }

    /**
     * Controls how the image should be resized or moved to match the size
     * of this ImageView.
     *
     * @param scaleType The desired scaling mode.
     * @attr ref android.R.styleable#ImageView_scaleType
     */
    override fun setScaleType(scaleType: ScaleType?) {
        assert(scaleType != null)

        if (mScaleType != scaleType) {
            mScaleType = scaleType

            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )
                else -> super.setScaleType(scaleType)
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (!customInitilized) {
            super.setImageDrawable(drawable)
            return
        }

        mResource = 0
        mDrawable = TRRoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap) {
        mResource = 0
        mDrawable = TRRoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(drawable)
    }

    private fun resolveResource(): Drawable? {
        val rsrc = resources ?: return null

        var d: Drawable? = null

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource, null)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: $mResource", e)
                // Don't try again.
                mResource = 0
            }

        }
        return TRRoundedDrawable.fromDrawable(d)
    }

    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable)
    }

    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mutateBackground) {
            if (convert) {
                mBackgroundDrawable = TRRoundedDrawable.fromDrawable(mBackgroundDrawable)
            }
            updateAttrs(mBackgroundDrawable)
        }
    }

    override fun setColorFilter(cf: ColorFilter) {
        if (mColorFilter !== cf) {
            mColorFilter = cf
            mHasColorFilter = true
            mColorMod = true
            applyColorMod()
            invalidate()
        }
    }

    private fun applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable!!.mutate()
            if (mHasColorFilter) {
                mDrawable!!.colorFilter = mColorFilter
            }
            // TODO: support, eventually...
            //mDrawable.setXfermode(mXfermode);
            //mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private fun updateAttrs(drawable: Drawable?) {
        if (drawable == null || !customInitilized) {
            return
        }

        if (drawable is TRRoundedDrawable) {
            // Log.d(TAG, "XXX this.tileModeX=" + this.tileModeX)
            drawable
                .setScaleType(mScaleType)
                .setCornerRadius(
                    topLeftCornerRadius, topRightCornerRadius, bottomLeftCornerRadius,
                    bottomRightCornerRadius
                )
                .setBorderWidth(borderWidth)
                .setBorderColor(borderColors)
                .setOval(isOval)
                .setTileModeX(this.tileModeX)
                .setTileModeY(this.tileModeY)
            applyColorMod()
        } else if (drawable is LayerDrawable) {
            var i = 0
            val layers = drawable.numberOfLayers
            while (i < layers) {
                updateAttrs(drawable.getDrawable(i))
                i++
            }
        }
    }

    override fun setBackground(background: Drawable?) {
        mBackgroundDrawable = background
        updateBackgroundDrawableAttrs(true)
        super.setBackground(background)
    }

    @Deprecated("replace  setCornerRadius()")
    fun setCornerRadiusDimen(resId: Int) {
        val radius = resources.getDimension(resId)
        setCornerRadius(radius, radius, radius, radius)
    }

    fun setCornerRadius(radius: Float) {
        setCornerRadius(radius, radius, radius, radius)
    }

    fun setCornerRadius(
        topLeft: Float, topRight: Float, bottomLeft: Float,
        bottomRight: Float
    ) {
        if (topLeftCornerRadius == topLeft
            && topRightCornerRadius == topRight
            && bottomLeftCornerRadius == bottomLeft
            && bottomRightCornerRadius == bottomRight
        ) {
            return
        }

        topLeftCornerRadius = topLeft
        topRightCornerRadius = topRight
        bottomLeftCornerRadius = bottomLeft
        bottomRightCornerRadius = bottomRight

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }


    fun getBorderWidth(): Float {
        return borderWidth
    }

    fun setBorderWidth(resId: Int) {
        setBorderWidth(resources.getDimension(resId))
    }

    fun setBorderWidth(width: Float) {
        if (borderWidth == width) {
            return
        }

        borderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun setBorderColor(colors: ColorStateList?) {
        if (borderColors == colors) {
            return
        }

        borderColors = colors ?: ColorStateList.valueOf(TRRoundedDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (borderWidth > 0) {
            invalidate()
        }
    }

    fun isOval(): Boolean {
        return isOval
    }

    fun setOval(oval: Boolean) {
        isOval = oval
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun mutatesBackground(): Boolean {
        return mutateBackground
    }

    fun mutateBackground(mutate: Boolean) {
        if (mutateBackground == mutate) {
            return
        }

        mutateBackground = mutate
        updateBackgroundDrawableAttrs(true)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec
        if (widthWeight == 0 || heightWeight == 0) {
            super.onMeasure(widthSpec, heightSpec)
            return
        }

        val widthMode = MeasureSpec.getMode(widthSpec)
        var widthSize = MeasureSpec.getSize(widthSpec)
        val heightMode = MeasureSpec.getMode(heightSpec)
        var heightSize = MeasureSpec.getSize(heightSpec)

        if (widthMode == EXACTLY) {
            if (heightMode != EXACTLY) {
                heightSize = (widthSize * 1f / widthWeight * heightWeight).toInt()
            }
        } else if (heightMode == EXACTLY) {
            widthSize = (heightSize * 1f / heightWeight * widthWeight).toInt()
        } else {
            throw IllegalStateException("Either width or height must be EXACTLY.")
        }

        widthSpec = MeasureSpec.makeMeasureSpec(widthSize, EXACTLY)
        heightSpec = MeasureSpec.makeMeasureSpec(heightSize, EXACTLY)

        super.onMeasure(widthSpec, heightSpec)
    }

    companion object {

        // Constants for tile mode attributes
        private const val TILE_MODE_UNDEFINED = -2
        private const val TILE_MODE_CLAMP = 0
        private const val TILE_MODE_REPEAT = 1
        private const val TILE_MODE_MIRROR = 2

        const val TAG = "TRRoundedImageView"
        const val DEFAULT_RADIUS = 0f
        const val DEFAULT_BORDER_WIDTH = 0f
        val DEFAULT_TILE_MODE: Shader.TileMode = Shader.TileMode.CLAMP

        private val SCALE_TYPES = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )

        private fun parseTileMode(tileMode: Int): Shader.TileMode? {
            return when (tileMode) {
                TILE_MODE_CLAMP -> Shader.TileMode.CLAMP
                TILE_MODE_REPEAT -> Shader.TileMode.REPEAT
                TILE_MODE_MIRROR -> Shader.TileMode.MIRROR
                else -> null
            }
        }
    }
}
