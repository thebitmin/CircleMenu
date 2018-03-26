package tech.bitmin.circlemunu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by Bitmin on 2018/3/16.
 * Email: thebititmin@outlook.com
 * Blog: Bitmin.tech
 */
class CircleMenuPresenter(private val mView: CircleMenuContract.View) : CircleMenuContract.Presenter {

    //last touch point
    private var mLastTouchPoint = Point()

    private var mOnMenuItemClickListener: ((position: Int) -> Unit)? = null
    private var mOnCenterItemClickListener: (() -> Unit)? = null
    private var mModel: CircleMenuContract.Model = CircleMenuModel()

    init {
        mModel.invalidate { invalidate() }
    }

    override fun disposeAttrs(context: Context, set: AttributeSet) {
        mModel.disposeAttrs(context, set)
    }

    override fun setViewSize(width: Int, height: Int) {
        val x = if (mModel.getData().isHalfCircle) null else width / 2
        mModel.setCenterPoint(x, height / 2)
    }


    override fun drawBackgroundGrayCircle(canvas: Canvas) {
        val a = dp2px(65f)
        val b = dp2px(22f)
        val circleData1 = mModel.getCircleData(-a.toInt(), 0)
        val circleData2 = mModel.getCircleData(b.toInt(), a.toInt())
        val circleData3 = mModel.getCircleData(b.toInt(), -a.toInt())
        circleData1.radius?.let {
            mView.drawCircle(canvas, circleData1.x, circleData1.y, it, mModel.getBackgroundCirclePaint())
        }
        circleData2.radius?.let {
            mView.drawCircle(canvas, circleData2.x, circleData2.y, it, mModel.getBackgroundCirclePaint())
        }
        circleData3.radius?.let {
            mView.drawCircle(canvas, circleData3.x, circleData3.y, it, mModel.getBackgroundCirclePaint())
        }
    }

    override fun drawBackgroundBlueCircle(canvas: Canvas) {
        val data = mModel.getCircleData()
        val radius = data.radius ?: return
        mView.drawCircle(canvas, data.x, data.y, radius, mModel.getItemCirclePaint())
    }

    override fun drawCenterImage(canvas: Canvas) {
        val centerData = mModel.getCenterData()
        if (centerData.bitmap == null) {
            return
        }
        if (centerData.srcRect == null) {
            return
        }
        if (centerData.desRect == null) {
            return
        }
        mView.drawBitmap(canvas, centerData.bitmap!!, centerData.srcRect!!, centerData.desRect!!, mModel.getBitmapPaint())
    }

    override fun drawItem(canvas: Canvas) {
        drawItemImage(canvas)
        drawItemTitle(canvas)
        drawItemSub(canvas)
    }

    private fun drawItemImage(canvas: Canvas) {
        mModel.getItemDataList().forEach {
            val itemData = it
            if (itemData.bitmap == null) {
                return
            }
            if (itemData.srcRect == null) {
                return
            }
            if (itemData.desRect == null) {
                return
            }
            mView.drawBitmap(canvas, itemData.bitmap!!, itemData.srcRect!!, itemData.desRect!!, mModel.getBitmapPaint())
        }
    }

    private fun drawItemTitle(canvas: Canvas) {
        drawText(canvas, true)
    }

    private fun drawItemSub(canvas: Canvas) {
        drawText(canvas, false)
    }

    private fun drawText(canvas: Canvas, isTitle: Boolean) {
        mModel.getItemDataList().forEach {
            val data = mModel.getTextData(isTitle, it) ?: return
            data.text ?: return
            mView.drawText(canvas, data.text!!, data.x, data.y, mModel.getTitlePaint())
        }
    }

    override fun setOnItemClickListener(listener: (position: Int) -> Unit): CircleMenuContract.Presenter {
        mOnMenuItemClickListener = listener
        return this
    }

    override fun setOnCenterItemClickListener(listener: () -> Unit): CircleMenuContract.Presenter {
        mOnCenterItemClickListener = listener
        return this
    }

    override fun subscribeCenterItemClickListener(event: MotionEvent) {
        if (event.action != MotionEvent.ACTION_DOWN) {
            return
        }
        val radius = mModel.getCenterData().radius ?: return
        val point = mModel.getData().centerPoint
        val distance = getDistance(event.x, event.y, point.x.toFloat(), point.y.toFloat())
        if (distance > radius) {
            return
        }
        mOnCenterItemClickListener?.invoke()
    }

    /**
     * 思路: 根据手势移动经过相对中心点的角度, 对 item 进行移动
     * 1. 记录手势移动前的坐标
     * 2. 根据手势移动时的坐标和移动前的坐标计算移动角度
     * 3. 计算 item 位置加上拖动角度修正
     * 4. 释放手势时, item 回归原位
     */
    override fun subscribeItemDragAnimator(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchPoint.set(event.x.toInt(), event.y.toInt())
                stopResetItemPointAnimator()
            }
            MotionEvent.ACTION_MOVE -> {
                val point = mModel.getData().centerPoint
                val centerX = point.x
                val centerY = point.y
                val lastAngle = Math.toDegrees(Math.atan2(
                        mLastTouchPoint.y.toDouble() - centerY,
                        mLastTouchPoint.x.toDouble() - centerX
                ))
                val currentAngle = Math.toDegrees(Math.atan2(
                        event.y.toDouble() - centerY,
                        event.x.toDouble() - centerX
                ))
                val angle = currentAngle - lastAngle
                var dragAngle = mModel.getDragAngle()
                dragAngle += angle.toFloat()
                mModel.setDragAngle(dragAngle)
                mLastTouchPoint.set(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_UP -> {
                startResetItemPointAnimator()
            }
        }
    }

    /**
     * 获取点击位置
     */
    override fun subscribeItemClickPositionListener(event: MotionEvent) {
        if (event.action != MotionEvent.ACTION_DOWN) {
            return
        }
        touchWitchItem(event)
    }

    /**
     * 1. 判断触摸点在哪个 item 上
     * 2. 放大 item
     * 3. 手势离开后缩小
     */
    override fun subscribeItemTouchAnimator(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mModel.getTouchItemPosition() == -1) {
                    return
                }
                stopZoomOutItemAnimator()
                startZoomInItemAnimator()
            }
            MotionEvent.ACTION_UP -> {
                stopZoomInItemAnimator()
                startZoomOutItemAnimator()
            }
        }
    }

    /**
     * 1. 判断手势抬起时的位置
     * 2. 在点击 item 上, 执行 onItemClick()
     */
    override fun subscribeItemClickListener(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val position = mModel.getTouchItemPosition()
                if (position == -1) {
                    return
                }
                val point = mModel.getItemDataList()[position].point!!
                val distance = getDistance(event.x, event.y, point.x.toFloat(), point.y.toFloat())
                if (distance > mModel.getTouchItemRadius()) {
                    return
                }
                mOnMenuItemClickListener?.invoke(position)
            }
        }
    }

    override fun setCenterPoint(x: Int?, y: Int?): CircleMenuContract.Presenter {
        mModel.setCenterPoint(x, y)
        return this
    }

    override fun setCenter(centerData: CircleMenuContract.Model.CenterData): CircleMenuContract.Presenter {
        mModel.setCenter(centerData)
        return this
    }

    override fun addItems(itemDataList: List<CircleMenuContract.Model.ItemData>): CircleMenuPresenter {
        mModel.addItems(itemDataList)
        return this
    }

    override fun addItem(itemData: CircleMenuContract.Model.ItemData): CircleMenuPresenter {
        mModel.addItem(itemData)
        return this
    }

    override fun addItem(imageResId: Int): CircleMenuContract.Presenter {
        mModel.addItem(mView.getContext(), imageResId)
        return this
    }

    override fun startBackgroundAnimator(): CircleMenuContract.Presenter {
        if (mModel.getBackgroundAnimator().isStarted) {
            return this
        }
        mModel.getBackgroundAnimator().start()
        return this
    }

    /**
     * 获取触摸到的 Item position，return -1 则没有触摸到任何 Item
     */
    private fun touchWitchItem(event: MotionEvent): Int {
        val x = event.x
        val y = event.y
        mModel.getItemDataList().forEach {
            val distance = getDistance(x, y, it.point!!.x.toFloat(), it.point!!.y.toFloat())
            if (distance <= mModel.getItemRadius()) {
                return mModel.setTouchItemPosition(mModel.indexOf(it))
            }
        }
        return -1
    }

    override fun setHalfCircle(isHalfCircle: Boolean): CircleMenuContract.Presenter {
        mModel.setHalfCircle(isHalfCircle)
        return this
    }

    override fun setItemRadius(radius: Float): CircleMenuContract.Presenter {
        mModel.setItemRadius(radius)
        return this
    }

    override fun invalidate() {
        mModel.resetAllData()
        mView.postInvalidate()
    }

    private fun startZoomInItemAnimator() {
        stopZoomOutItemAnimator()
        if (!mModel.getZoomInAnimator().isStarted) {
            mModel.getZoomInAnimator().start()
        }
    }

    private fun stopZoomInItemAnimator() {
        if (mModel.getZoomInAnimator().isStarted) {
            mModel.getZoomInAnimator().end()
        }
    }

    private fun startZoomOutItemAnimator() {
        stopZoomInItemAnimator()
        if (!mModel.getZoomOutAnimator().isStarted) {
            mModel.resetZoomOutValue()
            mModel.getZoomOutAnimator().start()
        }
    }

    private fun stopZoomOutItemAnimator() {
        if (mModel.getZoomOutAnimator().isStarted) {
            mModel.getZoomOutAnimator().end()
        }
    }

    private fun startResetItemPointAnimator() {
        if (!mModel.getResetPointAnimator().isStarted) {
            mModel.resetDragAngleValue()
            mModel.getResetPointAnimator().start()
        }
    }

    private fun stopResetItemPointAnimator() {
        if (mModel.getResetPointAnimator().isStarted) {
            mModel.getResetPointAnimator().end()
        }
    }

    override fun stopBackgroundAnimator(): CircleMenuContract.Presenter {
        if (!mModel.getBackgroundAnimator().isStarted) {
            return this
        }
        mModel.getBackgroundAnimator().end()
        return this
    }

    private fun dp2px(dp: Float): Float {
        return dp * mView.getContext().resources.displayMetrics.density
    }

    private fun getDistance(aX: Float, aY: Float, bX: Float, bY: Float): Double {
        val y = Math.pow((bY - aY).toDouble(), 2.0)
        val x = Math.pow((bX - aX).toDouble(), 2.0)
        return Math.sqrt(y + x)
    }
}