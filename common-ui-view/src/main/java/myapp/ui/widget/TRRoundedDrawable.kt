package myapp.ui.widget


import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView.ScaleType

class TRRoundedDrawable(private val sourceBitmap: Bitmap) : Drawable() {

    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int = sourceBitmap.width
    private val mBitmapHeight: Int = sourceBitmap.height
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()

    private var mBitmapShader: BitmapShader? = null
    private var mTileModeX: Shader.TileMode = Shader.TileMode.CLAMP
    private var mTileModeY: Shader.TileMode = Shader.TileMode.CLAMP
    private var mRebuildShader = true

    //private float          mCornerRadius = 0;
    private var mOval = false
    private var mBorderWidth = 0f
    private var borderColors = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
    private var mScaleType = ScaleType.FIT_CENTER

    private var mTopLeftCornerRadius = 0f
    private var mTopRightCornerRadius = 0f
    private var mBottomLeftCornerRadius = 0f
    private var mBottomRightCornerRadius = 0f

    private val isAllZeroPartialCorners: Boolean
        get() = mTopLeftCornerRadius <= 0 &&
                mTopRightCornerRadius <= 0 &&
                mBottomLeftCornerRadius <= 0 &&
                mBottomRightCornerRadius <= 0

    private val isAllSamePartialCorners: Boolean
        get() {
            if (mTopLeftCornerRadius != mTopRightCornerRadius) {
                return false
            }
            if (mTopLeftCornerRadius != mBottomLeftCornerRadius) {
                return false
            }
            return mTopLeftCornerRadius == mBottomRightCornerRadius
        }

    val borderColor: Int
        get() = borderColors.defaultColor

    init {
        // Log.d(TAG, "XXX TRRoundedDrawable")
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true

        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = borderColors.getColorForState(
            state,
            DEFAULT_BORDER_COLOR
        )
        mBorderPaint.strokeWidth = mBorderWidth
    }

    override fun isStateful(): Boolean {
        return borderColors.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
            return true
        } else {
            return super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float

        when (mScaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)

                mShaderMatrix.reset()
                mShaderMatrix.setTranslate(
                    ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f).toInt().toFloat(),
                    ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat()
                )
            }

            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)

                mShaderMatrix.reset()

                dx = 0f
                dy = 0f

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }
                mShaderMatrix.setScale(scale, scale)
                // jjfive
                //mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth,
                //                            (int) (dy + 0.5f) + mBorderWidth);
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + mBorderWidth / 2.0f,
                    (dy + 0.5f).toInt() + mBorderWidth / 2.0f
                )
            }

            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()

                if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    scale = 1.0f
                } else {
                    scale = Math.min(
                        mBounds.width() / mBitmapWidth.toFloat(),
                        mBounds.height() / mBitmapHeight.toFloat()
                    )
                }

                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)

                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }

        mDrawableRect.set(mBorderRect)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mBounds.set(bounds)

        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            mBitmapShader = BitmapShader(sourceBitmap, mTileModeX, mTileModeY)
            if (mTileModeX == Shader.TileMode.CLAMP && mTileModeY == Shader.TileMode.CLAMP) {
                mBitmapShader!!.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.shader = mBitmapShader
            mRebuildShader = false
        }

        if (mOval) {
            if (mBorderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (hasPartialCorners()) {
                val path = Path()
                if (mBorderWidth > 0) {

                    // canvas.drawRoundRect(mDrawableRect, Math.max(mCornerRadius, 0), Math.max(mCornerRadius, 0), mBitmapPaint);
                    // canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mBorderPaint);

                    partialCornerPath(mDrawableRect, path)
                    canvas.drawPath(path, mBitmapPaint)

                    partialCornerPath(mBorderRect, path)
                    canvas.drawPath(path, mBorderPaint)
                } else {
                    // canvas.drawRoundRect(mDrawableRect, mCornerRadius, mCornerRadius, mBitmapPaint);
                    partialCornerPath(mDrawableRect, path)
                    canvas.drawPath(path, mBitmapPaint)
                }
            } else {
                val cornerRadius = mTopLeftCornerRadius
                if (mBorderWidth > 0) {
                    canvas.drawRoundRect(
                        mDrawableRect, cornerRadius.coerceAtLeast(0f),
                        cornerRadius.coerceAtLeast(0f), mBitmapPaint
                    )
                    canvas.drawRoundRect(mBorderRect, cornerRadius, cornerRadius, mBorderPaint)
                } else {
                    canvas.drawRoundRect(mDrawableRect, cornerRadius, cornerRadius, mBitmapPaint)
                }
            }

            if (false) {
                Log.i(
                    "YYY",
                    String.format(
                        "LTRB %.1f %.1f %.1f %.1f",
                        mDrawableRect.left,
                        mDrawableRect.top,
                        mDrawableRect.right,
                        mDrawableRect.bottom
                    )
                )
            }
        }
    }

    private fun hasPartialCorners(): Boolean {
        if (isAllZeroPartialCorners) {
            return false
        }
        return !isAllSamePartialCorners
    }

    private fun partialCornerPath(rect: RectF, path: Path) {
        path.reset()
        path.moveTo(rect.left, rect.top + mTopLeftCornerRadius)

        // drawArc left top
        path.rCubicTo(
            0f, 0f,
            0f, -mTopLeftCornerRadius,
            mTopLeftCornerRadius, -mTopLeftCornerRadius
        )

        // drawLine top-horizontal
        path.rLineTo(
            rect.width() - mTopLeftCornerRadius - mTopRightCornerRadius,
            0f
        )

        // drawArc right top
        path.rCubicTo(
            0f, 0f,
            mTopRightCornerRadius, 0f,
            mTopRightCornerRadius, mTopRightCornerRadius
        )

        // drawLine right-vertical
        path.rLineTo(
            0f,
            rect.height() - mTopRightCornerRadius - mBottomRightCornerRadius
        )

        // drawArc right bottom
        path.rCubicTo(
            0f, 0f,
            0f, mBottomRightCornerRadius,
            -mBottomRightCornerRadius, mBottomRightCornerRadius
        )

        // drawLine bottom-horizontal
        path.rLineTo(
            -1 * (rect.width() - mBottomRightCornerRadius - mBottomLeftCornerRadius),
            0f
        )

        // drawArc left bottom
        path.rCubicTo(
            0f, 0f,
            -mBottomLeftCornerRadius, 0f,
            -mBottomLeftCornerRadius, -mBottomLeftCornerRadius
        )

        // drawLine left-vertical
        path.rLineTo(
            0f, -1 * (rect.height() - mBottomLeftCornerRadius - mTopLeftCornerRadius)
        )
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getAlpha(): Int {
        return mBitmapPaint.alpha
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return mBitmapPaint.colorFilter
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    fun setCornerRadius(
        topLeft: Float, topRight: Float, bottomLeft: Float,
        bottomRight: Float
    ): TRRoundedDrawable {
        mTopLeftCornerRadius = topLeft
        mTopRightCornerRadius = topRight
        mBottomLeftCornerRadius = bottomLeft
        mBottomRightCornerRadius = bottomRight
        return this
    }

    fun getBorderWidth(): Float {
        return mBorderWidth
    }

    fun setBorderWidth(width: Float): TRRoundedDrawable {
        mBorderWidth = width
        mBorderPaint.strokeWidth = mBorderWidth
        return this
    }

    fun setBorderColor(color: Int): TRRoundedDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    fun setBorderColor(colors: ColorStateList?): TRRoundedDrawable {
        borderColors = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = borderColors.getColorForState(
            state,
            DEFAULT_BORDER_COLOR
        )
        return this
    }

    fun isOval(): Boolean {
        return mOval
    }

    fun setOval(oval: Boolean): TRRoundedDrawable {
        mOval = oval
        return this
    }

    fun getScaleType(): ScaleType {
        return mScaleType
    }

    fun setScaleType(scaleType: ScaleType?): TRRoundedDrawable {
        val type = scaleType ?: ScaleType.FIT_CENTER
        if (mScaleType != type) {
            mScaleType = type
            updateShaderMatrix()
        }
        return this
    }

    fun getTileModeX(): Shader.TileMode {
        return mTileModeX
    }

    fun setTileModeX(tileModeX: Shader.TileMode): TRRoundedDrawable {
        if (mTileModeX != tileModeX) {
            mTileModeX = tileModeX
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun getTileModeY(): Shader.TileMode {
        return mTileModeY
    }

    fun setTileModeY(tileModeY: Shader.TileMode): TRRoundedDrawable {
        if (mTileModeY != tileModeY) {
            mTileModeY = tileModeY
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {

        const val TAG = "TRRoundedDrawable"
        const val DEFAULT_BORDER_COLOR = Color.BLACK

        fun fromBitmap(bitmap: Bitmap?): TRRoundedDrawable? {
            return if (bitmap != null) {
                TRRoundedDrawable(bitmap)
            } else {
                null
            }
        }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is TRRoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val ld = drawable as LayerDrawable?
                    val num = ld!!.numberOfLayers

                    // loop through layers to and change to RoundedDrawables if possible
                    for (i in 0 until num) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(
                            ld.getId(i),
                            fromDrawable(d)
                        )
                    }
                    return ld
                }

                // try to get a bitmap from the drawable and
                val bm =
                    drawableToBitmap(drawable)
                if (bm != null) {
                    return TRRoundedDrawable(bm)
                }
            }
            return drawable
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            var bitmap: Bitmap?
            val width = Math.max(drawable.intrinsicWidth, 2)
            val height = Math.max(drawable.intrinsicHeight, 2)
            try {
                bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.w(TAG, "Failed to create bitmap from drawable!")
                bitmap = null
            }

            return bitmap
        }
    }
}
