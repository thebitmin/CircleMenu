package tech.bitmin.circlemunu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by Bitmin on 2018/3/14.
 * Email: thebititmin@outlook.com
 * Blog: Bitmin.tech
 */
class CircleMenu : View, CircleMenuContract.View {

    var mPresenter: CircleMenuContract.Presenter = CircleMenuPresenter(this)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mPresenter.disposeAttrs(context, attrs)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mPresenter.setViewSize(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPresenter.drawBackgroundGrayCircle(canvas)
        mPresenter.drawBackgroundBlueCircle(canvas)
        mPresenter.drawCenterImage(canvas)
        mPresenter.drawItem(canvas)
        mPresenter.startBackgroundAnimator()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mPresenter.subscribeItemClickPositionListener(event)
        mPresenter.subscribeCenterItemClickListener(event)
        mPresenter.subscribeItemTouchAnimator(event)
        mPresenter.subscribeItemClickListener(event)
        mPresenter.subscribeItemDragAnimator(event)
        return true
    }

    override fun drawBitmap(canvas: Canvas, bitmap: Bitmap, srcRect: Rect, desRect: Rect, paint: Paint) {
        canvas.drawBitmap(bitmap, srcRect, desRect, paint)
    }

    override fun drawCircle(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        canvas.drawCircle(x, y, radius, paint)
    }

    override fun drawText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        canvas.drawText(text, x, y, paint)
    }

    override fun getPresenter(): CircleMenuContract.Presenter {
        return mPresenter
    }
}