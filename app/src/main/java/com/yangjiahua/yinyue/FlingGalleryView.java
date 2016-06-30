package com.yangjiahua.yinyue;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class FlingGalleryView extends ViewGroup {

    private static final int SNAP_VELOCITY = 1000;

    private int mCurrentScreen;
    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private float mLastMotionX;
    private float mLastMotionY;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private int mTouchSlop;

    private int mMaximumVelocity;

    private OnScrollToScreenListener mScrollToScreenListener;

    private OnCustomTouchListener mCustomTouchListener;

    private boolean isEveryScreen=false;

    public FlingGalleryView(Context context) {
        super(context);
        init();
        mCurrentScreen = 0;
    }

    public FlingGalleryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlingGalleryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        ;
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }


    private int count = -1;
    private int defaultScreen = -1;


    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {

            scrollTo(mScroller.getCurrX(), 0);
            if(isEveryScreen)singleScrollToScreen();
            postInvalidate();
        }
    }


    private void singleScrollToScreen() {
        final int screenWidth = getWidth();
        int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
        if (whichScreen > (getChildCount() - 1)) {
            return;
        }
        if (defaultScreen == -1) {
            defaultScreen = whichScreen;
            count = 1;
        } else {
            if (defaultScreen == whichScreen && count == 0) {
                count = 1;
            } else {
                if (defaultScreen != whichScreen) {
                    defaultScreen = whichScreen;
                    count = 0;
                }
            }
        }
        if (count == 0) {
            if (mScrollToScreenListener != null) {
                mScrollToScreenListener.operation(whichScreen, getChildCount());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "Workspace can only be used in EXACTLY mode.");
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "Workspace can only be used in EXACTLY mode.");
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(mCurrentScreen * width, 0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.setOnTouchListener(childTouchListener);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth,
                        child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }


    private OnTouchListener childTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    };


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCustomTouchListener != null) {
            mCustomTouchListener.operation(ev);
        }
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_MOVE:

                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int touchSlop = mTouchSlop;
                if (xDiff > touchSlop) {

                    if (Math.abs(mLastMotionY - y) / Math.abs(mLastMotionX - x) < 1) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        final float x = ev.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {

                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                count = -1;
                defaultScreen = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final float t_width = (getWidth() / 4f);

                    if (getScrollX() > ((getChildCount() - 1) * getWidth() + t_width)) {
                        break;
                    }

                    if (getScrollX() < ((t_width) * -1)) {
                        break;
                    }
                    final int deltaX = (int) (mLastMotionX - x);
                    mLastMotionX = x;
                    scrollBy(deltaX, 0);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();
                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {

                        snapToScreen(mCurrentScreen - 1, false);
                    } else if (velocityX < -SNAP_VELOCITY
                            && mCurrentScreen < getChildCount() - 1) {

                        snapToScreen(mCurrentScreen + 1, false);
                    } else {
                        snapToDestination();
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
        }
        return true;
    }


    private void snapToDestination() {
        final int screenWidth = getWidth();

        final int whichScreen = (getScrollX() + (screenWidth / 2))/ screenWidth;
        snapToScreen(whichScreen, false);
    }


    private void snapToScreen(int whichScreen, boolean isJump) {

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if (getScrollX() != (whichScreen * getWidth())) {
            final int delta = whichScreen * getWidth() - getScrollX();
            count = -1;
            defaultScreen = -1;

            mScroller.startScroll(getScrollX(), 0, delta, 0,
                    Math.abs(delta) * 2);
            final int t_mCurrentScreen = mCurrentScreen;
            mCurrentScreen = whichScreen;

            if (t_mCurrentScreen != whichScreen) {

                if (Math.abs(t_mCurrentScreen - whichScreen) == 1 && !isJump) {
                    doOnScrollToScreen();
                }
            }
            invalidate();
        }
    }

    private void doOnScrollToScreen() {
        if (mScrollToScreenListener != null) {
            mScrollToScreenListener.operation(mCurrentScreen, getChildCount());
        }
    }


    public void setToScreen(int whichScreen, boolean isAnimation) {
        if (isAnimation) {
            snapToScreen(whichScreen, true);
        } else {
            whichScreen = Math.max(0,
                    Math.min(whichScreen, getChildCount() - 1));
            mCurrentScreen = whichScreen;

            scrollTo(whichScreen * getWidth(), 0);
            if (whichScreen != mCurrentScreen) {
                doOnScrollToScreen();
            }
            invalidate();
        }
    }

    public void setDefaultScreen(int defaultScreen) {
        mCurrentScreen = defaultScreen;
    }


    public int getCurrentScreen() {
        return mCurrentScreen;
    }


    public void setOnScrollToScreenListener(
            OnScrollToScreenListener scrollToScreenListener) {
        if (scrollToScreenListener != null) {
            this.mScrollToScreenListener = scrollToScreenListener;
        }
    }


    public void setOnCustomTouchListener(
            OnCustomTouchListener customTouchListener) {
        if (customTouchListener != null) {
            this.mCustomTouchListener = customTouchListener;
        }
    }

    public interface OnScrollToScreenListener {
        public void operation(int currentScreen, int screenCount);
    }


    public interface OnCustomTouchListener {
        public void operation(MotionEvent event);
    }


    public void setEveryScreen(boolean isEveryScreen) {
        this.isEveryScreen = isEveryScreen;
    }
}
