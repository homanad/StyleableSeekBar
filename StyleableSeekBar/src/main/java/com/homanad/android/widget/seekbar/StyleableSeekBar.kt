package com.homanad.android.widget.seekbar

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.RestrictTo
import kotlin.math.min
import kotlin.math.sqrt

class StyleableSeekBar : View {

    companion object {
        //style
        const val CIRCULAR = 0
        const val ROUNDED_RECTANGLE = 1
        const val DIAMOND = 2
        const val RECTANGLE = 3
        const val TRIANGLE = 4
        const val HEXAGON = 5

        //orientation
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        private const val DOT_COUNT = 10
        private const val ACTIVE_COLOR = Color.BLUE
        private const val INACTIVE_COLOR = Color.GRAY
        private const val MINIMUM_VALUE = 0
        private val ROUNDED_RADIUS = 10.dp
        private val DOT_MARGIN = 10.dp
        private const val NO_COLOR = -1
        private const val NO_POSITION = -1
        private val SELECTED_BORDER_WIDTH = 3.dp
        private const val DEFAULT_ORIENTATION = HORIZONTAL
        private const val DEFAULT_STYLE = CIRCULAR
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @IntDef(HORIZONTAL, VERTICAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Orientation

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @IntDef(CIRCULAR, ROUNDED_RECTANGLE, DIAMOND, RECTANGLE, TRIANGLE, HEXAGON)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Style {}

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    //tools
    private var mProgressPaint = Paint()
    private var mRectFs = mutableListOf<RectF>()
    private var mColors = mutableListOf<Int>()
    private var mBorderPaint = Paint()
    private var mPath = Path()

    //callback
    private var mSeekListener: OnSeekListener? = null
    fun setOnSeekListener(listener: OnSeekListener) {
        mSeekListener = listener
    }

    //attributes
    private var mDotCount = DOT_COUNT
    private var mStyle = DEFAULT_STYLE
    private var mActiveColor = ACTIVE_COLOR
    private var mInactiveColor = INACTIVE_COLOR
    private var mMinimumValue = MINIMUM_VALUE
    private var mRoundedRadius = ROUNDED_RADIUS
    private var mDotMargin = DOT_MARGIN

    private var mStartColor = NO_COLOR
    private var mEndColor = NO_COLOR

    private var mSelectedBorderColor = NO_COLOR
    private var mSelectedBorderWidth = SELECTED_BORDER_WIDTH

    @Orientation
    private var mOrientation = DEFAULT_ORIENTATION

    //local vars
    private var mSelectedPos = NO_POSITION
    private var dotWidth = 0f

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        attrs?.let {
            val a =
                context.obtainStyledAttributes(attrs, R.styleable.StyleableSeekBar, defStyleAttr, 0)
            mDotCount = a.getInteger(R.styleable.StyleableSeekBar_sb_dotCount, DOT_COUNT)
            mStyle = a.getInt(R.styleable.StyleableSeekBar_sb_style, DEFAULT_STYLE)
            mActiveColor = a.getColor(R.styleable.StyleableSeekBar_sb_activeColor, ACTIVE_COLOR)
            mInactiveColor =
                a.getColor(R.styleable.StyleableSeekBar_sb_inactiveColor, INACTIVE_COLOR)
            mMinimumValue =
                a.getInteger(R.styleable.StyleableSeekBar_sb_minimumValue, MINIMUM_VALUE)
            mRoundedRadius = a.getDimensionPixelSize(
                R.styleable.StyleableSeekBar_sb_rounded_radius,
                ROUNDED_RADIUS
            )

            mStartColor =
                a.getColor(R.styleable.StyleableSeekBar_sb_progress_startColor, NO_COLOR)
            mEndColor = a.getColor(R.styleable.StyleableSeekBar_sb_progress_endColor, NO_COLOR)

            mDotMargin =
                a.getDimensionPixelSize(R.styleable.StyleableSeekBar_sb_dotMargin, DOT_MARGIN)

            mSelectedBorderColor =
                a.getColor(R.styleable.StyleableSeekBar_sb_selected_borderColor, NO_COLOR)
            mSelectedBorderWidth =
                a.getDimensionPixelSize(
                    R.styleable.StyleableSeekBar_sb_selected_borderWidth,
                    SELECTED_BORDER_WIDTH
                )

            mOrientation =
                a.getInt(R.styleable.StyleableSeekBar_sb_orientation, DEFAULT_ORIENTATION)

            a.recycle()

            //update init pos
            mSelectedPos = mMinimumValue - 1
        }
    }

    @get:IntRange(from = 1)
    @setparam:IntRange(from = 1)
    var dotCount: Int
        get() = mDotCount
        set(value) {
            mDotCount = value
            invalidate()
        }

    @get:Style
    @setparam:Style
    var style: Int
        get() = mStyle
        set(value) {
            require(
                value == CIRCULAR || value == ROUNDED_RECTANGLE
                        || value == DIAMOND || value == RECTANGLE
                        || value == TRIANGLE || value == HEXAGON
            ) { "invalid style: $value" }
            mStyle = value
            invalidate()
        }

    var roundedRadius: Int
        get() = mRoundedRadius
        set(value) {
            mRoundedRadius = value
            invalidate()
        }

    var activeColor: Int
        get() = mActiveColor
        set(@ColorInt value) {
            mActiveColor = value
            invalidate()
        }

    var inactiveColor: Int
        get() = mInactiveColor
        set(@ColorInt value) {
            mInactiveColor = value
            invalidate()
        }

    var minimumValue: Int
        get() = mMinimumValue
        set(value) {
            mMinimumValue = value
            mSelectedPos = mMinimumValue - 1
            invalidate()
        }

    var startColor: Int
        get() = mStartColor
        set(@ColorInt value) {
            mStartColor = value
            invalidate()
        }

    var endColor: Int
        get() = mEndColor
        set(@ColorInt value) {
            mEndColor = value
            invalidate()
        }

    var dotMargin: Int
        get() = mDotMargin
        set(@IntRange(from = 1) value) {
            mDotMargin = value
            invalidate()
        }

    var selectedBorderColor: Int
        get() = mSelectedBorderColor
        set(@ColorInt value) {
            mSelectedBorderColor = value
            invalidate()
        }

    var selectedBorderWidth: Int
        get() = mSelectedBorderWidth
        set(value) {
            mSelectedBorderWidth = value
            invalidate()
        }

    @get:Orientation
    @setparam:Orientation
    var orientation: Int
        get() = mOrientation
        set(value) {
            require(value == HORIZONTAL || value == VERTICAL) { "invalid orientation: $value" }
            mOrientation = value
            invalidate()
        }

    private fun setupParams() {
        if (mMinimumValue > mDotCount) throw Exception("The minimum value > total dots")
        //clear data
        mRectFs.clear()

        when (mOrientation) {
            HORIZONTAL -> {
                dotWidth = min(measuredWidth.toFloat() / mDotCount, measuredHeight.toFloat())

                val margin = (measuredWidth - (dotWidth * mDotCount)) / (mDotCount + 1).toFloat()
                var startX = margin
                var endX = dotWidth + margin
                while (mRectFs.size < mDotCount) {
                    mRectFs.add(RectF(startX, 0f, endX, measuredHeight.toFloat()))
                    startX += dotWidth + margin
                    endX += dotWidth + margin
                }
            }
            VERTICAL -> {
                dotWidth = min(measuredHeight.toFloat() / mDotCount, measuredWidth.toFloat())

                val margin = (measuredHeight - (dotWidth * mDotCount)) / (mDotCount + 1).toFloat()
                var bottom = measuredHeight - margin
                var top = measuredHeight - dotWidth - margin
                while (mRectFs.size < mDotCount) {
                    mRectFs.add(RectF(0f, top, measuredWidth.toFloat(), bottom))
                    bottom -= dotWidth - margin
                    top -= dotWidth - margin
                }
            }
        }

        if (isGradientMode()) {
            val fractionStep = 1f / mRectFs.size
            var fraction = fractionStep
            mRectFs.forEach { _ ->
                mColors.add(ArgbEvaluator().evaluate(fraction, mStartColor, mEndColor) as Int)
                fraction += fractionStep
            }
        }

        mProgressPaint.run {
            isAntiAlias = true
        }
        if (mSelectedBorderColor != NO_COLOR) mBorderPaint.run {
            color = mSelectedBorderColor
            style = Paint.Style.STROKE
            strokeWidth = mSelectedBorderWidth.toFloat()
            isAntiAlias = true
        }
    }

    private fun getSelectedPos(x: Float, y: Float): Int {
        val mX: Float =
            if (mOrientation == HORIZONTAL) x else (measuredWidth / 2).toFloat()
        val mY: Float =
            if (mOrientation == HORIZONTAL) (measuredHeight / 2).toFloat() else y
        mRectFs.forEach {
            if (isInRange(it, mX, mY))
                return if (isRectFActive(it, mX, mY)) mRectFs.indexOf(it)
                else mRectFs.indexOf(it) - 1
        }
        return mSelectedPos
    }

    private fun isGradientMode() = mStartColor != NO_COLOR && mEndColor != NO_COLOR

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setupParams()

        for (i in 0 until mRectFs.size) {
            val rectF = mRectFs[i]
            val pointF = rectF.getCenterPointF()
            if (i > mSelectedPos && i >= mMinimumValue)
                mProgressPaint.color = mInactiveColor
            else mProgressPaint.color = if (isGradientMode()) mColors[i] else mActiveColor

            when (mStyle) {
                CIRCULAR -> {
                    val radius = dotWidth / 2 - mDotMargin / 2
                    canvas.drawCircle(pointF.x, pointF.y, radius, mProgressPaint)
                    if (i == mSelectedPos && mSelectedBorderColor != NO_COLOR)
                        canvas.drawCircle(
                            pointF.x,
                            pointF.y,
                            radius - mSelectedBorderWidth / 2f,
                            mBorderPaint
                        )
                }
                ROUNDED_RECTANGLE -> {
                    val radius = mRoundedRadius.toFloat()
                    val corners = floatArrayOf(
                        radius, radius,   // Top left radius in px
                        radius, radius,   // Top right radius in px
                        radius, radius,     // Bottom right radius in px
                        radius, radius      // Bottom left radius in px
                    )
                    val path = mPath.apply {
                        reset()
                        addRoundRect(
                            if (mOrientation == HORIZONTAL)
                                rectF.addLeftRightMargin(mDotMargin / 2f)
                            else rectF.addTopBottomMargin(mDotMargin / 2f),
                            corners,
                            Path.Direction.CW
                        )
                    }
                    canvas.drawPath(path, mProgressPaint)
                    if (i == mSelectedPos && mSelectedBorderColor != NO_COLOR)
                        canvas.drawPath(mPath.apply {
                            reset()
                            addRoundRect(
                                if (mOrientation == HORIZONTAL)
                                    rectF.addLeftRightMargin(mDotMargin / 2f)
                                        .addMargin(mSelectedBorderWidth / 2f)
                                else rectF.addTopBottomMargin(mDotMargin / 2f)
                                    .addMargin(mSelectedBorderWidth / 2f),
                                corners,
                                Path.Direction.CW
                            )
                        }, mBorderPaint)
                }
                DIAMOND -> {
                    canvas.drawRhombus(
                        mProgressPaint,
                        pointF.x.toInt(),
                        pointF.y.toInt(),
                        (dotWidth - mDotMargin / 2).toInt()
                    )
                    if (i == mSelectedPos && mSelectedBorderColor != NO_COLOR)
                        canvas.drawRhombus(
                            mBorderPaint,
                            pointF.x.toInt(),
                            pointF.y.toInt(),
                            (dotWidth - mDotMargin / 2 - mSelectedBorderWidth / 2).toInt()
                        )
                }
                RECTANGLE -> {
                    canvas.drawRect(
                        if (mOrientation == HORIZONTAL)
                            rectF.addLeftRightMargin(mDotMargin / 2f)
                        else rectF.addTopBottomMargin(mDotMargin / 2f), mProgressPaint
                    )
                    if (i == mSelectedPos && mSelectedBorderColor != NO_COLOR)
                        canvas.drawRect(
                            if (mOrientation == HORIZONTAL)
                                rectF.addLeftRightMargin(mDotMargin / 2f)
                                    .addMargin(mSelectedBorderWidth / 2f)
                            else rectF.addTopBottomMargin(mDotMargin / 2f)
                                .addMargin(mSelectedBorderWidth / 2f),
                            mBorderPaint
                        )
                }
                TRIANGLE -> {
                    canvas.drawTriangle(
                        mProgressPaint,
                        pointF.x.toInt(),
                        pointF.y.toInt(),
                        (dotWidth - mDotMargin / 2).toInt()
                    )
                    if (i == mSelectedPos && mSelectedBorderColor != NO_COLOR)
                        canvas.drawTriangle(
                            mBorderPaint,
                            pointF.x.toInt(),
                            pointF.y.toInt(),
                            (dotWidth - mDotMargin / 2 - mSelectedBorderWidth).toInt()
                        )
                }
                HEXAGON -> {
                    canvas.drawHexagon(
                        mProgressPaint,
                        pointF.x.toInt(),
                        pointF.y.toInt(),
                        (dotWidth - mDotMargin / 2).toInt()
                    )
                    if (i == mSelectedPos && mSelectedBorderColor != NO_COLOR)
                        canvas.drawHexagon(
                            mBorderPaint,
                            pointF.x.toInt(),
                            pointF.y.toInt(),
                            (dotWidth - mDotMargin / 2 - mSelectedBorderWidth).toInt()
                        )
                }
            }
        }
    }

    /**Select over center*/
//    private fun isRectFActive(rectF: RectF, x: Float, y: Float): Boolean {
//        return if (mOrientation == ORIENTATION_HORIZONTAL) x > (rectF.left + rectF.right) / 2
//        else y < (rectF.top + rectF.bottom) / 2
//    }

    /**Select in range*/
    private fun isRectFActive(rectF: RectF, x: Float, y: Float): Boolean {
        return if (mOrientation == HORIZONTAL) x > rectF.left + 1
        else y < rectF.bottom + 1
    }

    private fun isInRange(rectF: RectF, x: Float, y: Float): Boolean {
        return rectF.contains(x, y)
    }

    private fun changeProgress(x: Float, y: Float) {
        if (mMinimumValue - 1 <= getSelectedPos(x, y)) mSelectedPos = getSelectedPos(x, y)

        mSeekListener?.onProgressChanged(this, mSelectedPos + 1)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.action?.let {
            when (it) {
                MotionEvent.ACTION_DOWN -> {
                    mSeekListener?.onStartTracking(this)
                    changeProgress(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    changeProgress(event.x, event.y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mSeekListener?.onStopTracking(this)
                }
                else -> {
                }
            }
        }
        invalidate()
        return true
    }

    interface OnSeekListener {
        fun onStartTracking(styleableSeekBar: StyleableSeekBar)
        fun onStopTracking(styleableSeekBar: StyleableSeekBar)
        fun onProgressChanged(styleableSeekBar: StyleableSeekBar, progress: Int) //need isFromUser
    }
}

fun Canvas.drawRhombus(paint: Paint, x: Int, y: Int, width: Int) {
    val halfWidth = width / 2
    val path = Path()
    path.moveTo(x.toFloat(), (y + halfWidth).toFloat()) // Top
    path.lineTo((x - halfWidth).toFloat(), y.toFloat()) // Left
    path.lineTo(x.toFloat(), (y - halfWidth).toFloat()) // Bottom
    path.lineTo((x + halfWidth).toFloat(), y.toFloat()) // Right
    path.lineTo(x.toFloat(), (y + halfWidth).toFloat()) // Back to Top
    path.close()
    drawPath(path, paint)
}

fun Canvas.drawTriangle(paint: Paint, x: Int, y: Int, width: Int) {
    val halfWidth = width / 2
    val path = Path()
    path.moveTo(x.toFloat(), (y - halfWidth).toFloat()) // Top
    path.lineTo((x - halfWidth).toFloat(), (y + halfWidth).toFloat()) // Bottom left
    path.lineTo((x + halfWidth).toFloat(), (y + halfWidth).toFloat()) // Bottom right
    path.lineTo(x.toFloat(), (y - halfWidth).toFloat()) // Back to Top
    path.close()
    drawPath(path, paint)
}

fun Canvas.drawHexagon(paint: Paint, x: Int, y: Int, width: Int) {
    val halfWidth = width / sqrt(3.0)
    val path = Path()
    val triangleHeight = (sqrt(3.0) * halfWidth / 2).toFloat()

    path.moveTo(x.toFloat(), (y + halfWidth).toFloat())
    path.lineTo(x - triangleHeight, (y + halfWidth / 2).toFloat())
    path.lineTo(x - triangleHeight, (y - halfWidth / 2).toFloat())
    path.lineTo(x.toFloat(), (y - halfWidth).toFloat())
    path.lineTo(x + triangleHeight, (y - halfWidth / 2).toFloat())
    path.lineTo(x + triangleHeight, (y + halfWidth / 2).toFloat())
    path.close()

    drawPath(path, paint)
}

fun RectF.getCenterPointF(): PointF {
    val x = (left + right) / 2
    val y = (top + bottom) / 2
    return PointF(x, y)
}

fun RectF.addLeftRightMargin(margin: Float) = RectF(left + margin, top, right - margin, bottom)

fun RectF.addLeftMargin(margin: Float) = RectF(left + margin, top, right, bottom)

fun RectF.addRightMargin(margin: Float) = RectF(left, top, right - margin, bottom)

fun RectF.addTopBottomMargin(margin: Float) = RectF(left, top + margin, right, bottom - margin)

fun RectF.addTopMargin(margin: Float) = RectF(left, top + margin, right, bottom)

fun RectF.addBottomMargin(margin: Float) = RectF(left, top, right, bottom - margin)

fun RectF.addMargin(margin: Float) =
    RectF(left + margin, top + margin, right - margin, bottom - margin)