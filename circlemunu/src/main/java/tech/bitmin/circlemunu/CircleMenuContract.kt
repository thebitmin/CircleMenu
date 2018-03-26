package tech.bitmin.circlemunu

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by Bitmin on 2018/3/16.
 * Email: thebititmin@outlook.com
 * Blog: Bitmin.tech
 */
interface CircleMenuContract {

    interface View {
        fun getPresenter(): Presenter
        fun getContext(): Context
        fun postInvalidate()
        fun drawBitmap(canvas: Canvas, bitmap: Bitmap, srcRect: Rect, desRect: Rect, paint: Paint)
        fun drawCircle(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint)
        fun drawText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint)
    }

    interface Presenter {
        fun setCenterPoint(x: Int?, y: Int?): Presenter
        fun setCenter(centerData: Model.CenterData): Presenter
        fun addItem(itemData: Model.ItemData): Presenter
        fun addItem(imageResId: Int): Presenter
        fun addItems(itemDataList: List<Model.ItemData>): Presenter
        fun setItemRadius(radius: Float): Presenter
        fun setOnCenterItemClickListener(listener: () -> Unit): Presenter
        fun setOnItemClickListener(listener: (position: Int) -> Unit): Presenter
        fun startBackgroundAnimator(): Presenter
        fun stopBackgroundAnimator(): Presenter
        fun setHalfCircle(isHalfCircle: Boolean): Presenter
        fun invalidate()
        fun disposeAttrs(context: Context, set: AttributeSet)
        fun setViewSize(width: Int, height: Int)
        fun drawBackgroundGrayCircle(canvas: Canvas)
        fun drawBackgroundBlueCircle(canvas: Canvas)
        fun drawCenterImage(canvas: Canvas)
        fun drawItem(canvas: Canvas)
        fun subscribeItemClickPositionListener(event: MotionEvent)
        fun subscribeCenterItemClickListener(event: MotionEvent)
        fun subscribeItemDragAnimator(event: MotionEvent)
        fun subscribeItemTouchAnimator(event: MotionEvent)
        fun subscribeItemClickListener(event: MotionEvent)
    }

    interface Model {
        fun setDensity(density: Float)
        fun disposeAttrs(context: Context, set: AttributeSet)
        fun setCenterPoint(x: Int?, y: Int?)
        fun setCenter(centerData: CenterData)
        fun setCenterImage(bitmap: Bitmap)
        fun setCenterImage(context: Context, resId: Int)
        fun addItem(itemData: ItemData)
        fun addItem(context: Context, resId: Int)
        fun addItems(itemDataList: List<ItemData>)
        fun setItemRadius(radius: Float)
        fun setHalfCircle(isHalfCircle: Boolean)
        fun getItemDataList(): List<ItemData>
        fun indexOf(itemData: ItemData): Int
        fun setDragAngle(dragAngle: Float)
        fun getDragAngle(): Float
        fun getCenterData(): CenterData
        fun getData(): ViewData
        fun getItemRadius(): Float
        fun getBackgroundAnimator(): ObjectAnimator
        fun getZoomInAnimator(): ObjectAnimator
        fun getZoomOutAnimator(): ObjectAnimator
        fun getResetPointAnimator(): ObjectAnimator
        fun invalidate(invalidate: () -> Unit)
        fun getItemCircleX(): Int
        fun getItemCircleY(): Int
        fun getBitmapPaint(): Paint
        fun getItemCirclePaint(): Paint
        fun getBackgroundCirclePaint(): Paint
        fun getCircleData(): CircleData
        fun getCircleData(x: Int, y: Int): CircleData
        fun getTouchItemRadius(): Float
        fun resetZoomOutValue()
        fun resetDragAngleValue()
        fun getSubTitlePaint(): Paint
        fun getTitlePaint(): Paint
        fun getTextData(isTitle: Boolean, itemData: ItemData): TextData?
        fun setTouchItemPosition(position: Int): Int
        fun getTouchItemPosition(): Int
        fun resetAllData()

        class ViewData {
            var radius: Float? = null
            var centerPoint: Point = Point()
            var dragAngle: Float = 0f
            var isHalfCircle: Boolean = true
        }

        class TextData {
            var x: Float = 0f
            var y: Float = 0f
            var text: String? = null
        }

        class CircleData {
            var x = 0f
            var y = 0f
            var radius: Float? = null

            override fun toString(): String {
                return String.format("x: %f, y: %f, radius: %f", x, y, radius)
            }
        }

        class CenterData {

            var bitmap: Bitmap? = null
            var srcRect: Rect? = null
            var desRect: Rect? = null
            var radius: Float? = null

            @Suppress("unused")
            fun setBitmap(context: Context, resId: Int) {
                this.bitmap = getBitmap(context, resId)
            }

            @Suppress("DEPRECATION")
            private fun getBitmap(context: Context, resId: Int): Bitmap {
                return if (Build.VERSION.SDK_INT < 21) {
                    (context.resources.getDrawable(resId) as BitmapDrawable).bitmap
                } else {
                    (context.resources.getDrawable(resId, context.theme) as BitmapDrawable).bitmap
                }
            }
        }

        class ItemData {

            var bitmap: Bitmap? = null
            var title: String? = null
            var subTitle: String? = null
            var angle: Double? = null
            var point: Point? = null
            var desRect: Rect? = null
            var srcRect: Rect? = null

            @Suppress("unused")
            fun setImageBitmap(bitmap: Bitmap): ItemData {
                this.bitmap = bitmap
                return this
            }

            fun setImageBitmap(context: Context, resId: Int): ItemData {
                this.bitmap = getBitmap(context, resId)
                return this
            }

            fun setTitleResId(context: Context, titleResId: Int): ItemData {
                this.title = getString(context, titleResId)
                return this
            }

            @Suppress("unused")
            fun setSubTitleResId(context: Context, subTitleResId: Int): ItemData {
                this.subTitle = getString(context, subTitleResId)
                return this
            }

            private fun getBitmap(context: Context, resId: Int): Bitmap {
                return if (Build.VERSION.SDK_INT < 21) {
                    @Suppress("DEPRECATION")
                    (context.resources.getDrawable(resId) as BitmapDrawable).bitmap
                } else {
                    (context.resources.getDrawable(resId, context.theme) as BitmapDrawable).bitmap
                }
            }

            private fun getString(context: Context, resId: Int): String {
                return context.resources.getString(resId)
            }
        }

        class NotSetInvalidateException : KotlinNullPointerException("You should set the invalidate() before start animator!")
    }
}