package com.itgo.book_cloud.ui.components


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.itgo.book_cloud.R
import kotlin.properties.Delegates


/**
 * 自定义水平\垂直电池控件
 */
@SuppressLint("CustomViewStyleable")
class BatteryView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mPower: Int
    private var orientation: Int
    private var mColor = 0

    private var mWidth by Delegates.notNull<Int>()
    private var mHeight by Delegates.notNull<Int>()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Battery)
        mColor = typedArray.getColor(R.styleable.Battery_batteryColor, -0x1)
        orientation = typedArray.getInt(R.styleable.Battery_batteryOrientation, 0)
        mPower = typedArray.getInt(R.styleable.Battery_batteryPower, 0)
        mWidth = measuredWidth
        mHeight = measuredHeight
        /**
         * recycle() :官方的解释是：回收TypedArray，以便后面重用。在调用这个函数后，你就不能再使用这个TypedArray。
         * 在TypedArray后调用recycle主要是为了缓存。当recycle被调用后，这就说明这个对象从现在可以被重用了。
         * TypedArray 内部持有部分数组，它们缓存在Resources类中的静态字段中，这样就不用每次使用前都需要分配内存。
         */
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //对View上的內容进行测量后得到的View內容占据的宽度
        mWidth = measuredWidth
        //对View上的內容进行测量后得到的View內容占据的高度
        mHeight = measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //判断电池方向    horizontal: 0   vertical: 1
        if (orientation == 0) {
            drawHorizontalBattery(canvas)
        } else {
            drawVerticalBattery(canvas)
        }
    }

    /**
     * 绘制水平电池
     *
     * @param canvas
     */
    private fun drawHorizontalBattery(canvas: Canvas) {
        val paint = Paint()
        paint.color = mColor
        paint.style = Paint.Style.STROKE
        val strokeWidth = mWidth / 20f
        val strokeWidth2 = strokeWidth / 2
        paint.strokeWidth = strokeWidth
        val r1 = RectF(
            strokeWidth2,
            strokeWidth2,
            width - strokeWidth - strokeWidth2,
            mHeight - strokeWidth2
        )
        //设置外边框颜色为黑色
        paint.color = Color.GRAY
        canvas.drawRect(r1, paint)
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
        //画电池内矩形电量
        val offset = (mWidth - strokeWidth * 2) * mPower / 100f
        val r2 = RectF(strokeWidth, strokeWidth, offset, mHeight - strokeWidth)
        //根据电池电量决定电池内矩形电量颜色
        if (mPower < 30) {
            paint.color = Color.RED
        }
        if (mPower in 30..49) {
            paint.color = Color.BLUE
        }
        if (mPower >= 50) {
            paint.color = Color.GREEN
        }
        canvas.drawRect(r2, paint)
        //画电池头
        val r3 = RectF(mWidth - strokeWidth, mHeight * 0.25f, mWidth.toFloat(), mHeight * 0.75f)
        //设置电池头颜色为黑色
        paint.color = Color.DKGRAY
        canvas.drawRect(r3, paint)
    }

    /**
     * 绘制垂直电池
     *
     * @param canvas
     */
    private fun drawVerticalBattery(canvas: Canvas) {
        val paint = Paint()
        paint.color = mColor
        paint.style = Paint.Style.STROKE
        val strokeWidth = mHeight / 20.0f
        val strokeWidth2 = strokeWidth / 2
        paint.strokeWidth = strokeWidth
        val headHeight = (strokeWidth + 0.5f).toInt()
        val rect = RectF(
            strokeWidth2,
            headHeight + strokeWidth2,
            mWidth - strokeWidth2,
            mHeight - strokeWidth2
        )
        canvas.drawRect(rect, paint)
        paint.strokeWidth = 0f
        val topOffset = (mHeight - headHeight - strokeWidth) * (100 - mPower) / 100.0f
        val rect2 = RectF(
            strokeWidth,
            headHeight + strokeWidth + topOffset,
            mWidth - strokeWidth,
            mHeight - strokeWidth
        )
        paint.style = Paint.Style.FILL
        canvas.drawRect(rect2, paint)
        val headRect = RectF(mWidth / 4.0f, 0f, mWidth * 0.75f, headHeight.toFloat())
        canvas.drawRect(headRect, paint)
    }

    /**
     * 设置电池颜色
     *
     * @param color
     */
    fun setColor(color: Int) {
        mColor = color
        invalidate()
    }
    /**
     * 获取电池电量
     *
     * @return
     *///刷新VIEW
    /**
     * 设置电池电量
     *
     * @param power
     */
    var power: Int
        get() = mPower
        set(power) {
            mPower = power
            if (mPower < 0) {
                mPower = 100
            }
            invalidate() //刷新VIEW
        }
}