package com.icechao.klinelib.base;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

/*************************************************************************
 * Description   :
 *
 * @PackageName  : com.icechao.klinelib.utils
 * @FileName     : ScrollAndScaleView.java
 * @Author       : chao
 * @Date         : 2019/1/8
 * @Email        : icechliu@gmail.com
 * @version      : V1
 *************************************************************************/
public abstract class ScrollAndScaleView extends RelativeLayout implements
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {
    protected int scrollX = 0;
    protected GestureDetectorCompat gestureDetector;
    protected ScaleGestureDetector scaleDetector;

    protected boolean isLongPress = false;

    private OverScroller overScroller;

    protected boolean touch = false;

    protected float scaleX = 1;

    protected float scaleXMax = 2f;

    protected float scaleXMin = 0.5f;

    private boolean isMultipleTouch = false;

    private boolean isScrollEnable = true;

    private boolean isScaleEnable = true;

    public ScrollAndScaleView(Context context) {
        super(context);
        init();
    }

    public ScrollAndScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollAndScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        gestureDetector = new GestureDetectorCompat(getContext(), this);
        scaleDetector = new ScaleGestureDetector(getContext(), this);
        overScroller = new OverScroller(getContext());
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!isLongPress && !isMultipleTouch()) {
//            scrollBy(Math.round(distanceX), 0);
            int oldX = scrollX;
            scrollX -= Math.round(distanceX);
            if (scrollX != oldX) {
                onScrollChanged(scrollX, 0, oldX, 0);
                invalidate();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        isLongPress = true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!isTouch() && isScrollEnable()) {
            overScroller.fling(scrollX, 0
                    , Math.round(velocityX / scaleX), 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    0, 0);
        }
        return true;
    }

    @Override
    public void computeScroll() {

        if (overScroller.computeScrollOffset()) {
            if (!isTouch()) {
//                scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
                int oldX = scrollX;
                scrollX = overScroller.getCurrX();
                if (scrollX != oldX) {
                    onScrollChanged(scrollX, 0, oldX, 0);
                    invalidate();
                }
            } else {

                overScroller.forceFinished(true);
            }
        }
    }

//    @Override
//    public void scrollBy(int x, int y) {
//        scrollTo(scrollX - Math.round(x / scaleX), 0);
//    }
//
//    @Override
//    public void scrollTo(int x, int y) {
//        if (!isScrollEnable()) {
//            overScroller.forceFinished(true);
//            return;
//        }
//        int oldX = scrollX;
//        scrollX = x;
//        if (scrollX != oldX) {
//            onScrollChanged(scrollX, 0, oldX, 0);
//            invalidate();
//        }
//
//    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!isScaleEnable()) {
            return false;
        }
        float oldScale = scaleX;
        scaleX *= detector.getScaleFactor();
        if (scaleX < scaleXMin) {
            scaleX = scaleXMin;
        } else if (scaleX > scaleXMax) {
            scaleX = scaleXMax;
        }
        onScaleChanged(scaleX, oldScale);

        return true;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    protected void onScaleChanged(float scale, float oldScale) {
        animValidate();
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private float x;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 按压手指超过1个
        if (event.getPointerCount() > 1) {
            isLongPress = false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                touch = true;
                x = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //长按之后移动
                if (isLongPress) {
                    onLongPress(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                animValidate();
                break;
            case MotionEvent.ACTION_UP:
                if (x == event.getX()) {
                    if (isLongPress) {
                        isLongPress = false;
                    }
                }
                touch = false;
                animValidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                isLongPress = false;
                touch = false;
                animValidate();
                break;
            default:
                break;
        }
        isMultipleTouch = event.getPointerCount() > 1;
        this.gestureDetector.onTouchEvent(event);
        this.scaleDetector.onTouchEvent(event);
        return true;
    }


    /**
     * 滑到了最左边
     */
    abstract public void onLeftSide();

    /**
     * 滑到了最右边
     */
    abstract public void onRightSide();

    /**
     * 是否在触摸中
     *
     * @return
     */
    public boolean isTouch() {
        return touch;
    }

    /**
     * 获取位移的最小值
     *
     * @return
     */
//    public int getMinScrollX(){
//        return 0;
//    }

    /**
     * 获取位移的最大值
     *
     * @return
     */
//    public int getMaxScrollX(){
//        return 0;
//    }

    /**
     * 设置ScrollX
     *
     * @param scrollX
     */
    public void setScrollX(int scrollX) {
        this.scrollX = scrollX;
        scrollTo(scrollX, 0);
    }

    /**
     * 是否是多指触控
     *
     * @return
     */
    public boolean isMultipleTouch() {
        return isMultipleTouch;
    }


    public float getScaleXMax() {
        return scaleXMax;
    }

    public float getScaleXMin() {
        return scaleXMin;
    }

    public boolean isScrollEnable() {
        return isScrollEnable;
    }

    public boolean isScaleEnable() {
        return isScaleEnable;
    }

    /**
     * 设置缩放的最大值
     */
    public void setScaleXMax(float scaleXMax) {
        this.scaleXMax = scaleXMax;
    }

    /**
     * 设置缩放的最小值
     */
    public void setScaleXMin(float scaleXMin) {
        this.scaleXMin = scaleXMin;
    }

    /**
     * 设置是否可以滑动
     */
    public void setScrollEnable(boolean scrollEnable) {
        isScrollEnable = scrollEnable;
    }

    /**
     * 设置是否可以缩放
     */
    public void setScaleEnable(boolean scaleEnable) {
        isScaleEnable = scaleEnable;
    }



    /**
     * 刷新界面
     */
    public void animValidate() {
        invalidate();
    }
}
