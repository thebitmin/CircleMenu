package tech.bitmin.circlemunu

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Created by Bitmin on 2018/3/20.
 * Email: thebititmin@outlook.com
 * Blog: Bitmin.tech
 */
class CircleMenuModel: CircleMenuContract.Model {

    private var mAttrs: TypedArray? = null
    private var mDensity: Float? = null
    private var mItemRadius: Float? = null
    private var mIsHalfCircle = true

    private var mData: CircleMenuContract.Model.ViewData = CircleMenuContract.Model.ViewData()
    private var mCenterData = CircleMenuContract.Model.CenterData()
    private var mItemDataList: ArrayList<CircleMenuContract.Model.ItemData> = ArrayList()
    private var mCircleDataCache: HashMap<String, CircleMenuContract.Model.CircleData> = HashMap()

    private var mInvalidate: (() -> Unit)? = null
    private var mBackgroundCircleColor = Color.parseColor("#F4F4F4")
    private var mItemCircleColor = Color.parseColor("#AED4FE")

    private var mBitmapPaint: Paint? = null
    private var mItemCirclePaint: Paint? = null
    private var mBackgroundCirclePaint: Paint? = null
    private var mSubTitlePaint: Paint? = null
    private var mTitlePaint: Paint? = null

    private var mTextMargin: Float? = null
    private var mTextData: CircleMenuContract.Model.TextData = CircleMenuContract.Model.TextData()
    private var mTextLeading: Float? = null

    private var mTouchItemPosition = -1
    private var mDragAngle = 0f
    private var mItemScale = 1f
    private var mTranslateY = 0

    private var mBackgroundAnimator: ObjectAnimator? = null
    private var mResetItemPointAnimator: ObjectAnimator? = null
    private var mZoomInItemAnimator: ObjectAnimator? = null
    private var mZoomOutItemAnimator: ObjectAnimator? = null

    override fun invalidate(invalidate: () -> Unit) {
        mInvalidate = invalidate
    }

    private fun invalidate() {
        if (mInvalidate == null) {
            throw CircleMenuContract.Model.NotSetInvalidateException()
        }
        mInvalidate?.invoke()
    }

    private fun initTextMargin() {
        mTextMargin = dp2px(10f)
    }

    override fun getTouchItemPosition(): Int {
        return mTouchItemPosition
    }

    override fun getTextData(isTitle: Boolean, itemData: CircleMenuContract.Model.ItemData): CircleMenuContract.Model.TextData? {
        if (isTitle && itemData.title == null) {
            return null
        }
        if (!isTitle && itemData.subTitle == null) {
            return null
        }
        if (itemData.point == null) {
            return null
        }
        if (mItemRadius == null) {
            initItemRadius()
        }
        if (mTextMargin == null) {
            initTextMargin()
        }
        if (mTextLeading == null) {
            initTextLeading()
        }
        val scale = if (isTouchItem(itemData)) mItemScale else 1f
        mTextData.x = itemData.point!!.x + mItemRadius!! * scale + mTextMargin!!
        mTextData.y = if (isTitle) itemData.point!!.y - mTextLeading!! / 2f else itemData.point!!.y + mTextLeading!! / 2f + getSubTitlePaint().fontSpacing
        mTextData.text = if (isTitle) itemData.title else itemData.subTitle
        return mTextData
    }

    //todo 得到的 文字间距并不好用
    private fun initTextLeading() {
        //行间距
        val fontSpacing = getSubTitlePaint().fontSpacing
        val bounds = Rect()
        //获得文字显示范围
        getSubTitlePaint().getTextBounds("bj", 0, 1, bounds)
        //获得文字间距
        mTextLeading = fontSpacing - bounds.bottom
    }

    private fun isTouchItem(itemData: CircleMenuContract.Model.ItemData): Boolean {
        if (mTouchItemPosition == -1) return false
        return itemData == mItemDataList[mTouchItemPosition]
    }

    private fun initSubTitlePaint(): Paint {
        mSubTitlePaint = Paint()
        mSubTitlePaint!!.textSize = dp2px(10f)
        mSubTitlePaint!!.color = Color.parseColor("#FF000000")
        mSubTitlePaint!!.style = Paint.Style.FILL
        return mSubTitlePaint!!
    }

    override fun getSubTitlePaint(): Paint {
        return mSubTitlePaint ?: initSubTitlePaint()
    }

    private fun initTitlePaint(): Paint {
        mTitlePaint = Paint()
        mTitlePaint!!.textSize = dp2px(16f)
        mTitlePaint!!.color = Color.parseColor("#FF000000")
        mTitlePaint!!.style = Paint.Style.FILL
        return mTitlePaint!!
    }

    override fun getTitlePaint(): Paint {
        return mTitlePaint ?: initTitlePaint()
    }

    override fun getItemCircleX(): Int {
        return mData.centerPoint.x
    }

    override fun getItemCircleY(): Int {
        return mData.centerPoint.y
    }

    private fun initBitmapPaint(): Paint {
        mBitmapPaint = Paint()
        mBitmapPaint!!.flags = Paint.ANTI_ALIAS_FLAG
        mBitmapPaint!!.isFilterBitmap = true
        mBitmapPaint!!.isDither = true
        return mBitmapPaint!!
    }

    override fun getBitmapPaint(): Paint {
        return mBitmapPaint ?: initBitmapPaint()
    }

    private fun initItemCirclePaint(): Paint {
        mItemCirclePaint = Paint()
        initDrawCirclePaint(mItemCirclePaint!!, mItemCircleColor)
        return mItemCirclePaint!!
    }

    override fun getItemCirclePaint(): Paint {
        return mItemCirclePaint ?: initItemCirclePaint()
    }

    private fun initBackgroundCirclePaint(): Paint {
        mBackgroundCirclePaint = Paint()
        initDrawCirclePaint(mBackgroundCirclePaint!!, mBackgroundCircleColor)
        return mBackgroundCirclePaint!!
    }

    override fun getBackgroundCirclePaint(): Paint {
        return mBackgroundCirclePaint ?: initBackgroundCirclePaint()
    }

    private fun initDrawCirclePaint(paint: Paint, color: Int) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp2px(2f)
        paint.isAntiAlias = true
    }

    override fun getCircleData(): CircleMenuContract.Model.CircleData {
        if (mCircleDataCache.containsKey("0,0")) return mCircleDataCache["0,0"]!!
        val circleData = CircleMenuContract.Model.CircleData()
        circleData.x = mData.centerPoint.x.toFloat()
        circleData.y = mData.centerPoint.y.toFloat()
        circleData.radius = mData.radius
        mCircleDataCache["0,0"] = circleData
        return circleData
    }

    override fun getCircleData(x: Int, y: Int): CircleMenuContract.Model.CircleData {
        val key = "$x,$y"
        if (x == 0 && y == 0) return getCircleData()
        val circleData = if (mCircleDataCache.containsKey(key)) mCircleDataCache[key]!! else CircleMenuContract.Model.CircleData()
        val a = if (mIsHalfCircle) x else 0
        val b = if (mIsHalfCircle) y else 0
        val c = if (mIsHalfCircle && a != 0) a / Math.abs(a) else 0
        val d = if (mIsHalfCircle && b != 0) b / Math.abs(b) else 0
        circleData.x = if (y != 0) mData.centerPoint.x.toFloat() + a else mData.centerPoint.x.toFloat() + a - c * mTranslateY
        circleData.y = if (y == 0) mData.centerPoint.y.toFloat() else mData.centerPoint.y.toFloat() + b + d * mTranslateY
        circleData.radius = mData.radius
        mCircleDataCache[key] = circleData
        return circleData
    }

    /**
     * 用于 ObjectAnimator 设置属性
     */
    @Suppress("unused")
    private fun setItemScale(itemScale: Float) {
        mItemScale = itemScale
        setItemImageDesRect()
        invalidate()
    }

    /**
     * 用于 ObjectAnimator 设置属性
     */
    @Suppress("unused")
    private fun getItemScale(): Float {
        return mItemScale
    }

    /**
     * 用于 ObjectAnimator 设置属性
     */
    @Suppress("unused")
    private fun setTranslateY(translateY: Int) {
        mTranslateY = translateY
        invalidate()
    }

    /**
     * 用于 ObjectAnimator 设置属性
     */
    @Suppress("unused")
    private fun getTranslateY(): Int {
        return mTranslateY
    }

    private fun initBackgroundAnimator(): ObjectAnimator {
        mBackgroundAnimator = ObjectAnimator.ofInt(this, "translateY", 0, dp2px(130f).toInt())
        mBackgroundAnimator!!.duration = 2000
        mBackgroundAnimator!!.repeatCount = -1
        mBackgroundAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mBackgroundAnimator!!.repeatMode = ObjectAnimator.REVERSE
        return mBackgroundAnimator!!
    }

    private fun initResetItemPointAnimator(): ObjectAnimator {
        mResetItemPointAnimator = ObjectAnimator.ofFloat(this, "dragAngle", mData.dragAngle, 0f)
        mResetItemPointAnimator!!.duration = 300
        mResetItemPointAnimator!!.repeatCount = 0
        mResetItemPointAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mResetItemPointAnimator!!.repeatMode = ObjectAnimator.RESTART
        return mResetItemPointAnimator!!
    }

    private fun initZoomInItemAnimator(): ObjectAnimator {
        mZoomInItemAnimator = ObjectAnimator.ofFloat(this, "itemScale", 1f, 1.5f)
        mZoomInItemAnimator!!.duration = 200
        mZoomInItemAnimator!!.repeatCount = 0
        mZoomInItemAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mZoomInItemAnimator!!.repeatMode = ObjectAnimator.RESTART
        return mZoomInItemAnimator!!
    }

    private fun initZoomOutItemAnimator(): ObjectAnimator {
        mZoomOutItemAnimator = ObjectAnimator.ofFloat(this, "itemScale", mItemScale, 1f)
        mZoomOutItemAnimator!!.duration = 200
        mZoomOutItemAnimator!!.repeatCount = 0
        mZoomOutItemAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mZoomOutItemAnimator!!.repeatMode = ObjectAnimator.RESTART
        return mZoomOutItemAnimator!!
    }

    override fun resetZoomOutValue() {
        mZoomOutItemAnimator?.setFloatValues(mItemScale, 1f)
    }

    override fun resetDragAngleValue() {
        mResetItemPointAnimator?.setFloatValues(mData.dragAngle, 0f)
    }

    override fun getBackgroundAnimator(): ObjectAnimator {
        return mBackgroundAnimator ?: initBackgroundAnimator()
    }

    override fun getResetPointAnimator(): ObjectAnimator {
        return mResetItemPointAnimator ?: initResetItemPointAnimator()
    }

    override fun getZoomInAnimator(): ObjectAnimator {
        return mZoomInItemAnimator ?: initZoomInItemAnimator()
    }

    override fun getZoomOutAnimator(): ObjectAnimator {
        return mZoomOutItemAnimator ?: initZoomOutItemAnimator()
    }

    /**
     * 设置中心点, 重新设置 center 和 item 的位置, 刷新布局
     */
    override fun setCenterPoint(x: Int?, y: Int?) {
        if (x != null) {
            mData.centerPoint.x = x
        }
        if (y != null) {
            mData.centerPoint.y = y
        }
        if (mCenterData.bitmap != null) {
            setCenterSrcRect()
            setCenterDesRect()
        }
        setItemImagePoint()
        setItemImageDesRect()
    }

    override fun setHalfCircle(isHalfCircle: Boolean) {
        mIsHalfCircle = isHalfCircle
        setItemAngle()
        setItemImagePoint()
        setItemImageDesRect()
        if (mCenterData.bitmap != null) {
            setCenterSrcRect()
            setCenterDesRect()
        }
    }

    private fun setItemImageDesRect() {
        mItemDataList.forEach { setItemImageDesRect(it) }
    }

    private fun setItemImageDesRect(itemData: CircleMenuContract.Model.ItemData) {
        if (mItemRadius == null) {
            initItemRadius()
        }
        if (itemData.point == null) {
            throw KotlinNullPointerException("itemData.point == null, please set itemPoint before setItemDesRect")
        }
        val x = itemData.point!!.x
        val y = itemData.point!!.y
        val radius: Int = if (mItemDataList.indexOf(itemData) == mTouchItemPosition)
            (mItemRadius!! * mItemScale).toInt() else mItemRadius!!.toInt()
        itemData.desRect = Rect(x - radius, y - radius, x + radius, y + radius)
    }

    private fun setItemImagePoint() {
        mItemDataList.forEach { setItemImagePoint(it) }
    }

    /**
    * 设置 item 坐标
    */
    private fun setItemImagePoint(itemData: CircleMenuContract.Model.ItemData) {
        if (itemData.angle == null) {
            setItemAngle()
        }
        if (mData.radius == null) {
            initRadius()
        }
        val centerX = mData.centerPoint.x
        val centerY = mData.centerPoint.y
        val itemX = (centerX + mData.radius!! * Math.cos(Math.toRadians(itemData.angle!! + mDragAngle))).toInt()
        val itemY = (centerY + mData.radius!! * Math.sin(Math.toRadians(itemData.angle!! + mDragAngle))).toInt()
        if (itemData.point == null) {
            itemData.point = Point(itemX, itemY)
            return
        }
        itemData.point!!.set(itemX, itemY)
    }

    override fun indexOf(itemData: CircleMenuContract.Model.ItemData): Int {
        return mItemDataList.indexOf(itemData)
    }

    private fun setItemAngle() {
        mItemDataList.forEach {
            setItemAngle(it)
        }
    }

    private fun setItemAngle(itemData: CircleMenuContract.Model.ItemData) {
        val angle = if (mIsHalfCircle) 360 / mItemDataList.size / 2 else 360 / mItemDataList.size
        val startAngle = if (mIsHalfCircle) (-90).toDouble() + angle / 2 else (-90).toDouble()
        val position = mItemDataList.indexOf(itemData)
        itemData.angle = startAngle + angle * position + mData.dragAngle
    }

    override fun setCenter(centerData: CircleMenuContract.Model.CenterData) {
        mCenterData = centerData
        setCenterSrcRect()
        setCenterDesRect()
    }

    override fun setCenterImage(bitmap: Bitmap) {
        mCenterData.bitmap = bitmap
    }

    override fun getCenterData(): CircleMenuContract.Model.CenterData {
        return mCenterData
    }

    override fun getData(): CircleMenuContract.Model.ViewData {
        return mData
    }

    /**
     * 用于 ObjectAnimator 设置属性
     */
    override fun getDragAngle(): Float {
        return mData.dragAngle
    }

    /**
     * 用于 ObjectAnimator 设置属性
     */
    override fun setDragAngle(dragAngle: Float) {
        mData.dragAngle = dragAngle
        setItemAngle()
        setItemImagePoint()
        setItemImageDesRect()
        invalidate()
    }

    override fun addItems(itemDataList: List<CircleMenuContract.Model.ItemData>) {
        itemDataList.forEach { addItem(it) }
    }

    override fun addItem(context: Context, resId: Int) {
        val itemData = CircleMenuContract.Model.ItemData().setImageBitmap(context, resId)
        addItem(itemData)
    }

    /**
     * 添加 item
     * 1. 加入 List
     * 2. 获得 Bitmap
     * 3. 设置 裁剪区域 srcRect
     * 4. 设置 item point
     * 5. 设置 绘图区域 desRect
     */
    override fun addItem(itemData: CircleMenuContract.Model.ItemData) {
        mItemDataList.add(itemData)
    }

    override fun resetAllData() {
        setItemAngle()
        setItemImagePoint()
        setItemImageSrcRect()
        setItemImageDesRect()
        setCenterSrcRect()
        setCenterDesRect()
    }

    private fun setItemImageSrcRect() {
        mItemDataList.forEach { setItemImageSrcRect(it) }
    }

    private fun setItemImageSrcRect(itemData: CircleMenuContract.Model.ItemData) {
        if (itemData.bitmap == null) {
            return
        }
        if (itemData.srcRect == null) {
            itemData.srcRect = Rect()
        }
        itemData.srcRect!!.set(0, 0, itemData.bitmap!!.width, itemData.bitmap!!.height)
    }

    override fun setItemRadius(radius: Float) {
        mItemRadius = radius
    }

    private fun initItemRadius() {
        mItemRadius = dp2px(30f)
    }

    private fun initRadius() {
        mData.radius = dp2px(220f)
    }

    private fun getAttrsRadius(context: Context, set: AttributeSet): Float {
        return getDimension(context, set, R.styleable.CircleMenu_radius, dp2px(220f))
    }

    private fun getAttrsCenterX(context: Context, set: AttributeSet): Int {
        return getDimension(context, set, R.styleable.CircleMenu_centerItemRadius, dp2px(38f)).toInt()
    }

    private fun getAttrsCenterItemRadius(context: Context, set: AttributeSet): Float {
        return getDimension(context, set, R.styleable.CircleMenu_centerItemRadius, dp2px(94f))
    }

    private fun getAttrsItemRadius(context: Context, set: AttributeSet): Float {
        return getDimension(context, set, R.styleable.CircleMenu_itemRadius, dp2px(30f))
    }

    private fun getAttrsTextMargin(context: Context, set: AttributeSet): Float {
        return getDimension(context, set, R.styleable.CircleMenu_itemTextMargin, dp2px(10f))
    }

    private fun getAttrsBackgroundCircleColor(context: Context, set: AttributeSet): Int {
        return getColor(context, set, R.styleable.CircleMenu_backgroundCircleColor, mBackgroundCircleColor)
    }

    private fun getAttrsItemCircleColor(context: Context, set: AttributeSet): Int {
        return getColor(context, set, R.styleable.CircleMenu_itemCircleColor, mItemCircleColor)
    }

    private fun getAttrsCenterDrawable(context: Context, set: AttributeSet): Drawable? {
        return getDrawable(context, set, R.styleable.CircleMenu_centerItemDrawable)
    }

    private fun getAttrsHalfCircle(context: Context, set: AttributeSet): Boolean {
        return getBoolean(context, set, R.styleable.CircleMenu_halfCircle, true)
    }

    private fun getDimension(context: Context, set: AttributeSet, index: Int, defValue: Float): Float {
        return getAttrs(context, set).getDimension(index, defValue)
    }

    private fun getColor(context: Context, set: AttributeSet, index: Int, defValue: Int): Int {
        return getAttrs(context, set).getColor(index, defValue)
    }

    private fun getDrawable(context: Context, set: AttributeSet, index: Int): Drawable? {
        return getAttrs(context, set).getDrawable(index)
    }

    private fun getBoolean(context: Context, set: AttributeSet, index: Int, defValue: Boolean): Boolean {
        return getAttrs(context, set).getBoolean(index, defValue)
    }

    override fun setTouchItemPosition(position: Int): Int {
        mTouchItemPosition = position
        return position
    }

    /**
     * 获取 Attrs 配置信息
     */
    override fun disposeAttrs(context: Context, set: AttributeSet) {
        getAttrs(context, set)
        setDensity(context)
        mData.radius = getAttrsRadius(context, set)
        mData.centerPoint.x = getAttrsCenterX(context, set)
        mCenterData.radius = getAttrsCenterItemRadius(context, set)
        mItemRadius = getAttrsItemRadius(context, set)
        mTextMargin = getAttrsTextMargin(context, set)
        mBackgroundCircleColor = getAttrsBackgroundCircleColor(context, set)
        mItemCircleColor = getAttrsItemCircleColor(context, set)
        val drawable = getAttrsCenterDrawable(context, set)
        if (drawable != null && drawable is BitmapDrawable) {
            setCenterImage(drawable)
            setCenterSrcRect()
            setCenterDesRect()
        }
        mIsHalfCircle = getAttrsHalfCircle(context, set)
        mAttrs?.recycle()
    }

    override fun getItemRadius(): Float {
        if (mItemRadius == null) {
            mItemRadius = dp2px(30f)
        }
        return mItemRadius!!
    }

    override fun getTouchItemRadius(): Float {
        return getItemRadius() * mItemScale
    }

    private fun setCenterSrcRect() {
        if (mCenterData.bitmap == null) {
            return
        }
        if (mCenterData.radius == null) {
            initCenterRadius()
        }
        val radius = mCenterData.radius!!.toInt()
        val left = if (mIsHalfCircle) radius - mData.centerPoint.x else 0
        val top = 0
        val right = mCenterData.bitmap!!.width
        val bottom = mCenterData.bitmap!!.height
        mCenterData.srcRect = Rect(left, top, right, bottom)
    }

    private fun setCenterDesRect() {
        if (mCenterData.radius == null) {
            initCenterRadius()
        }
        val radius = mCenterData.radius!!.toInt()
        val x = mData.centerPoint.x
        val y = mData.centerPoint.y
        val left = if (mIsHalfCircle) 0 else x - radius
        val top = y - radius
        val right = x + radius
        val bottom = y + radius
        mCenterData.desRect = Rect(left, top, right, bottom)
    }

    private fun initCenterRadius() {
        if (mData.centerPoint.y == 0) {
            mCenterData.radius = dp2px(94F)
        } else {
            mCenterData.radius = mData.centerPoint.y * 0.36f
        }
    }

    private fun setCenterImage(drawable: BitmapDrawable) {
        mCenterData.bitmap = drawable.bitmap
    }

    override fun setCenterImage(context: Context, resId: Int) {
        mCenterData.bitmap = getBitmap(resId, context)
    }

    @Suppress("DEPRECATION")
    private fun getBitmap(resId: Int, context: Context): Bitmap {
        return if (Build.VERSION.SDK_INT < 21) {
            (context.resources.getDrawable(resId) as BitmapDrawable).bitmap
        } else {
            (context.resources.getDrawable(resId, context.theme) as BitmapDrawable).bitmap
        }
    }

    private fun getAttrs(context: Context, set: AttributeSet): TypedArray {
        return if (mAttrs == null) context.obtainStyledAttributes(set, R.styleable.CircleMenu) else mAttrs!!
    }

    private fun setDensity(context: Context) {
        mDensity = context.resources.displayMetrics.density
    }

    private fun dp2px(dp: Float): Float {
        return if (mDensity == null) 0f else dp * mDensity!!
    }

    override fun setDensity(density: Float) {
        mDensity = density
    }

    override fun getItemDataList(): List<CircleMenuContract.Model.ItemData> {
        return mItemDataList
    }
}