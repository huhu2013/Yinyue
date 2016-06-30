package com.yangjiahua.yinyue;




import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;


import java.util.Formatter;
import java.util.Locale;

public class DiscreteSeekBar extends View {


    public interface OnProgressChangeListener {

        public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser);

        public void onStartTrackingTouch(DiscreteSeekBar seekBar);

        public void onStopTrackingTouch(DiscreteSeekBar seekBar);
    }


    public static abstract class NumericTransformer {

        public abstract int transform(int value);


        public String transformToString(int value) {
            return String.valueOf(value);
        }


        public boolean useStringTransform() {
            return false;
        }
    }


    private static class DefaultNumericTransformer extends NumericTransformer {

        @Override
        public int transform(int value) {
            return value;
        }
    }


    private static final boolean isLollipopOrGreater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private static final String DEFAULT_FORMATTER = "%d";

    private static final int PRESSED_STATE = android.R.attr.state_pressed;
    private static final int FOCUSED_STATE = android.R.attr.state_focused;
    private static final int PROGRESS_ANIMATION_DURATION = 250;
    private static final int INDICATOR_DELAY_FOR_TAPS = 150;
    private static final int DEFAULT_THUMB_COLOR = 0xff009688;
    private ThumbDrawable mThumb;
    private TrackRectDrawable mTrack;
    private TrackRectDrawable mScrubber;
    private Drawable mRipple;

    private int mTrackHeight;
    private int mScrubberHeight;
    private int mAddedTouchBounds;

    private int mMax;
    private int mMin;
    private int mValue;
    private int mKeyProgressIncrement = 1;
    private boolean mMirrorForRtl = false;
    private boolean mAllowTrackClick = true;
    private boolean mIndicatorPopupEnabled = true;

    Formatter mFormatter;
    private String mIndicatorFormatter;
    private NumericTransformer mNumericTransformer;
    private StringBuilder mFormatBuilder;
    private OnProgressChangeListener mPublicChangeListener;
    private boolean mIsDragging;
    private int mDragOffset;

    private Rect mInvalidateRect = new Rect();
    private Rect mTempRect = new Rect();
    private PopupIndicator mIndicator;
    private AnimatorCompat mPositionAnimator;
    private float mAnimationPosition;
    private int mAnimationTarget;
    private float mDownX;
    private float mTouchSlop;

    public DiscreteSeekBar(Context context) {
        this(context, null);
    }

    public DiscreteSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.discreteSeekBarStyle);
    }

    public DiscreteSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setWillNotDraw(false);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        float density = context.getResources().getDisplayMetrics().density;
        mTrackHeight = (int) (1 * density);
        mScrubberHeight = (int) (4 * density);
        int thumbSize = (int) (density * ThumbDrawable.DEFAULT_SIZE_DP);


        int touchBounds = (int) (density * 32);
        mAddedTouchBounds = (touchBounds - thumbSize) / 2;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiscreteSeekBar,
                defStyleAttr, R.style.Widget_DiscreteSeekBar);

        int max = 100;
        int min = 0;
        int value = 0;
        mMirrorForRtl = a.getBoolean(R.styleable.DiscreteSeekBar_dsb_mirrorForRtl, mMirrorForRtl);
        mAllowTrackClick = a.getBoolean(R.styleable.DiscreteSeekBar_dsb_allowTrackClickToDrag, mAllowTrackClick);
        mIndicatorPopupEnabled = a.getBoolean(R.styleable.DiscreteSeekBar_dsb_indicatorPopupEnabled, mIndicatorPopupEnabled);
        int indexMax = R.styleable.DiscreteSeekBar_dsb_max;
        int indexMin = R.styleable.DiscreteSeekBar_dsb_min;
        int indexValue = R.styleable.DiscreteSeekBar_dsb_value;
        final TypedValue out = new TypedValue();

        if (a.getValue(indexMax, out)) {
            if (out.type == TypedValue.TYPE_DIMENSION) {
                max = a.getDimensionPixelSize(indexMax, max);
            } else {
                max = a.getInteger(indexMax, max);
            }
        }
        if (a.getValue(indexMin, out)) {
            if (out.type == TypedValue.TYPE_DIMENSION) {
                min = a.getDimensionPixelSize(indexMin, min);
            } else {
                min = a.getInteger(indexMin, min);
            }
        }
        if (a.getValue(indexValue, out)) {
            if (out.type == TypedValue.TYPE_DIMENSION) {
                value = a.getDimensionPixelSize(indexValue, value);
            } else {
                value = a.getInteger(indexValue, value);
            }
        }

        mMin = min;
        mMax = Math.max(min + 1, max);
        mValue = Math.max(min, Math.min(max, value));
        updateKeyboardRange();

        mIndicatorFormatter = a.getString(R.styleable.DiscreteSeekBar_dsb_indicatorFormatter);

        ColorStateList trackColor = a.getColorStateList(R.styleable.DiscreteSeekBar_dsb_trackColor);
        ColorStateList progressColor = a.getColorStateList(R.styleable.DiscreteSeekBar_dsb_progressColor);
        ColorStateList rippleColor = a.getColorStateList(R.styleable.DiscreteSeekBar_dsb_rippleColor);
        boolean editMode = isInEditMode();
        if (editMode || rippleColor == null) {
            rippleColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.DKGRAY});
        }
        if (editMode || trackColor == null) {
            trackColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.GRAY});
        }
        if (editMode || progressColor == null) {
            progressColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{DEFAULT_THUMB_COLOR});
        }
        mRipple = SeekBarCompat.getRipple(rippleColor);
        if (isLollipopOrGreater) {
            SeekBarCompat.setBackground(this, mRipple);
        } else {
            mRipple.setCallback(this);
        }


        TrackRectDrawable shapeDrawable = new TrackRectDrawable(trackColor);
        mTrack = shapeDrawable;
        mTrack.setCallback(this);

        shapeDrawable = new TrackRectDrawable(progressColor);
        mScrubber = shapeDrawable;
        mScrubber.setCallback(this);

        mThumb = new ThumbDrawable(progressColor, thumbSize);
        mThumb.setCallback(this);
        mThumb.setBounds(0, 0, mThumb.getIntrinsicWidth(), mThumb.getIntrinsicHeight());


        if (!editMode) {
            mIndicator = new PopupIndicator(context, attrs, defStyleAttr, convertValueToMessage(mMax));
            mIndicator.setListener(mFloaterListener);
        }
        a.recycle();

        setNumericTransformer(new DefaultNumericTransformer());

    }


    public void setIndicatorFormatter(@Nullable String formatter) {
        mIndicatorFormatter = formatter;
        updateProgressMessage(mValue);
    }


    public void setNumericTransformer(@Nullable NumericTransformer transformer) {
        mNumericTransformer = transformer != null ? transformer : new DefaultNumericTransformer();

        if (!isInEditMode()) {
            if (mNumericTransformer.useStringTransform()) {
                mIndicator.updateSizes(mNumericTransformer.transformToString(mMax));
            } else {
                mIndicator.updateSizes(convertValueToMessage(mNumericTransformer.transform(mMax)));
            }
        }
        updateProgressMessage(mValue);
    }

    public NumericTransformer getNumericTransformer() {
        return mNumericTransformer;
    }


    public void setMax(int max) {
        mMax = max;
        if (mMax < mMin) {
            setMin(mMax - 1);
        }
        updateKeyboardRange();

        if (mValue < mMin || mValue > mMax) {
            setProgress(mMin);
        }
    }

    public int getMax() {
        return mMax;
    }


    public void setMin(int min) {
        mMin = min;
        if (mMin > mMax) {
            setMax(mMin + 1);
        }
        updateKeyboardRange();

        if (mValue < mMin || mValue > mMax) {
            setProgress(mMin);
        }
    }

    public int getMin() {
        return mMin;
    }


    public void setProgress(int progress) {
        setProgress(progress, false);
    }

    private void setProgress(int value, boolean fromUser) {
        value = Math.max(mMin, Math.min(mMax, value));
        if (isAnimationRunning()) {
            mPositionAnimator.cancel();
        }

        if (mValue != value) {
            mValue = value;
            notifyProgress(value, fromUser);
            updateProgressMessage(value);
            updateThumbPosFromCurrentProgress();
        }
    }


    public int getProgress() {
        return mValue;
    }


    public void setOnProgressChangeListener(@Nullable OnProgressChangeListener listener) {
        mPublicChangeListener = listener;
    }


    public void setThumbColor(int thumbColor, int indicatorColor) {
        mThumb.setColorStateList(ColorStateList.valueOf(thumbColor));
        mIndicator.setColors(indicatorColor, thumbColor);
    }


    public void setThumbColor(@NonNull ColorStateList thumbColorStateList, int indicatorColor) {
        mThumb.setColorStateList(thumbColorStateList);

        int thumbColor = thumbColorStateList.getColorForState(new int[]{PRESSED_STATE}, thumbColorStateList.getDefaultColor());
        mIndicator.setColors(indicatorColor, thumbColor);
    }


    public void setScrubberColor(int color) {
        mScrubber.setColorStateList(ColorStateList.valueOf(color));
    }


    public void setScrubberColor(@NonNull ColorStateList colorStateList) {
        mScrubber.setColorStateList(colorStateList);
    }


    public void setTrackColor(int color) {
        mTrack.setColorStateList(ColorStateList.valueOf(color));
    }


    public void setTrackColor(@NonNull ColorStateList colorStateList) {
        mTrack.setColorStateList(colorStateList);
    }


    public void setIndicatorPopupEnabled(boolean enabled) {
        this.mIndicatorPopupEnabled = enabled;
    }

    private void notifyProgress(int value, boolean fromUser) {
        if (mPublicChangeListener != null) {
            mPublicChangeListener.onProgressChanged(DiscreteSeekBar.this, value, fromUser);
        }
        onValueChanged(value);
    }

    private void notifyBubble(boolean open) {
        if (open) {
            onShowBubble();
        } else {
            onHideBubble();
        }
    }


    protected void onShowBubble() {
    }


    protected void onHideBubble() {
    }


    protected void onValueChanged(int value) {
    }

    private void updateKeyboardRange() {
        int range = mMax - mMin;
        if ((mKeyProgressIncrement == 0) || (range / mKeyProgressIncrement > 20)) {

            mKeyProgressIncrement = Math.max(1, Math.round((float) range / 20));
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int height = mThumb.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
        height += (mAddedTouchBounds * 2);
        setMeasuredDimension(widthSize, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            removeCallbacks(mShowIndicatorRunnable);
            if (!isInEditMode()) {
                mIndicator.dismissComplete();
            }
            updateFromDrawableState();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        super.scheduleDrawable(who, what, when);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int thumbWidth = mThumb.getIntrinsicWidth();
        int thumbHeight = mThumb.getIntrinsicHeight();
        int addedThumb = mAddedTouchBounds;
        int halfThumb = thumbWidth / 2;
        int paddingLeft = getPaddingLeft() + addedThumb;
        int paddingRight = getPaddingRight();
        int bottom = getHeight() - getPaddingBottom() - addedThumb;
        mThumb.setBounds(paddingLeft, bottom - thumbHeight, paddingLeft + thumbWidth, bottom);
        int trackHeight = Math.max(mTrackHeight / 2, 1);
        mTrack.setBounds(paddingLeft + halfThumb, bottom - halfThumb - trackHeight,
                getWidth() - halfThumb - paddingRight - addedThumb, bottom - halfThumb + trackHeight);
        int scrubberHeight = Math.max(mScrubberHeight / 2, 2);
        mScrubber.setBounds(paddingLeft + halfThumb, bottom - halfThumb - scrubberHeight,
                paddingLeft + halfThumb, bottom - halfThumb + scrubberHeight);


        updateThumbPosFromCurrentProgress();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (!isLollipopOrGreater) {
            mRipple.draw(canvas);
        }
        super.onDraw(canvas);
        mTrack.draw(canvas);
        mScrubber.draw(canvas);
        mThumb.draw(canvas);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateFromDrawableState();
    }

    private void updateFromDrawableState() {
        int[] state = getDrawableState();
        boolean focused = false;
        boolean pressed = false;
        for (int i : state) {
            if (i == FOCUSED_STATE) {
                focused = true;
            } else if (i == PRESSED_STATE) {
                pressed = true;
            }
        }
        if (isEnabled() && (focused || pressed) && mIndicatorPopupEnabled) {

            removeCallbacks(mShowIndicatorRunnable);
            postDelayed(mShowIndicatorRunnable, INDICATOR_DELAY_FOR_TAPS);
        } else {
            hideFloater();
        }
        mThumb.setState(state);
        mTrack.setState(state);
        mScrubber.setState(state);
        mRipple.setState(state);
    }

    private void updateProgressMessage(int value) {
        if (!isInEditMode()) {
            if (mNumericTransformer.useStringTransform()) {
                mIndicator.setValue(mNumericTransformer.transformToString(value));
            } else {
                mIndicator.setValue(convertValueToMessage(mNumericTransformer.transform(value)));
            }
        }
    }

    private String convertValueToMessage(int value) {
        String format = mIndicatorFormatter != null ? mIndicatorFormatter : DEFAULT_FORMATTER;

        if (mFormatter == null || !mFormatter.locale().equals(Locale.getDefault())) {
            int bufferSize = format.length() + String.valueOf(mMax).length();
            if (mFormatBuilder == null) {
                mFormatBuilder = new StringBuilder(bufferSize);
            } else {
                mFormatBuilder.ensureCapacity(bufferSize);
            }
            mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        } else {
            mFormatBuilder.setLength(0);
        }
        return mFormatter.format(format, value).toString();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                startDragging(event, isInScrollingContainer());
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragging()) {
                    updateDragging(event);
                } else {
                    final float x = event.getX();
                    if (Math.abs(x - mDownX) > mTouchSlop) {
                        startDragging(event, false);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopDragging();
                break;
        }
        return true;
    }

    private boolean isInScrollingContainer() {
        return SeekBarCompat.isInScrollingContainer(getParent());
    }

    private boolean startDragging(MotionEvent ev, boolean ignoreTrackIfInScrollContainer) {
        final Rect bounds = mTempRect;
        mThumb.copyBounds(bounds);

        bounds.inset(-mAddedTouchBounds, -mAddedTouchBounds);
        mIsDragging = (bounds.contains((int) ev.getX(), (int) ev.getY()));
        if (!mIsDragging && mAllowTrackClick && !ignoreTrackIfInScrollContainer) {

            mIsDragging = true;
            mDragOffset = (bounds.width() / 2) - mAddedTouchBounds;
            updateDragging(ev);

            mThumb.copyBounds(bounds);
            bounds.inset(-mAddedTouchBounds, -mAddedTouchBounds);
        }
        if (mIsDragging) {
            setPressed(true);
            attemptClaimDrag();
            setHotspot(ev.getX(), ev.getY());
            mDragOffset = (int) (ev.getX() - bounds.left - mAddedTouchBounds);
            if (mPublicChangeListener != null) {
                mPublicChangeListener.onStartTrackingTouch(this);
            }
        }
        return mIsDragging;
    }

    private boolean isDragging() {
        return mIsDragging;
    }

    private void stopDragging() {
        if (mPublicChangeListener != null) {
            mPublicChangeListener.onStopTrackingTouch(this);
        }
        mIsDragging = false;
        setPressed(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean handled = false;
        if (isEnabled()) {
            int progress = getAnimatedProgress();
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = true;
                    if (progress <= mMin) break;
                    animateSetProgress(progress - mKeyProgressIncrement);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = true;
                    if (progress >= mMax) break;
                    animateSetProgress(progress + mKeyProgressIncrement);
                    break;
            }
        }

        return handled || super.onKeyDown(keyCode, event);
    }

    private int getAnimatedProgress() {
        return isAnimationRunning() ? getAnimationTarget() : mValue;
    }


    boolean isAnimationRunning() {
        return mPositionAnimator != null && mPositionAnimator.isRunning();
    }

    void animateSetProgress(int progress) {
        final float curProgress = isAnimationRunning() ? getAnimationPosition() : getProgress();

        if (progress < mMin) {
            progress = mMin;
        } else if (progress > mMax) {
            progress = mMax;
        }


        if (mPositionAnimator != null) {
            mPositionAnimator.cancel();
        }

        mAnimationTarget = progress;
        mPositionAnimator = AnimatorCompat.create(curProgress,
                progress, new AnimatorCompat.AnimationFrameUpdateListener() {
                    @Override
                    public void onAnimationFrame(float currentValue) {
                        setAnimationPosition(currentValue);
                    }
                });
        mPositionAnimator.setDuration(PROGRESS_ANIMATION_DURATION);
        mPositionAnimator.start();
    }

    private int getAnimationTarget() {
        return mAnimationTarget;
    }

    void setAnimationPosition(float position) {
        mAnimationPosition = position;
        float currentScale = (position - mMin) / (float) (mMax - mMin);
        updateProgressFromAnimation(currentScale);
    }

    float getAnimationPosition() {
        return mAnimationPosition;
    }


    private void updateDragging(MotionEvent ev) {
        setHotspot(ev.getX(), ev.getY());
        int x = (int) ev.getX();
        Rect oldBounds = mThumb.getBounds();
        int halfThumb = oldBounds.width() / 2;
        int addedThumb = mAddedTouchBounds;
        int newX = x - mDragOffset + halfThumb;
        int left = getPaddingLeft() + halfThumb + addedThumb;
        int right = getWidth() - (getPaddingRight() + halfThumb + addedThumb);
        if (newX < left) {
            newX = left;
        } else if (newX > right) {
            newX = right;
        }

        int available = right - left;
        float scale = (float) (newX - left) / (float) available;
        if (isRtl()) {
            scale = 1f - scale;
        }
        int progress = Math.round((scale * (mMax - mMin)) + mMin);
        setProgress(progress, true);
    }

    private void updateProgressFromAnimation(float scale) {
        Rect bounds = mThumb.getBounds();
        int halfThumb = bounds.width() / 2;
        int addedThumb = mAddedTouchBounds;
        int left = getPaddingLeft() + halfThumb + addedThumb;
        int right = getWidth() - (getPaddingRight() + halfThumb + addedThumb);
        int available = right - left;
        int progress = Math.round((scale * (mMax - mMin)) + mMin);

        if (progress != getProgress()) {
            mValue = progress;
            notifyProgress(mValue, true);
            updateProgressMessage(progress);
        }
        final int thumbPos = (int) (scale * available + 0.5f);
        updateThumbPos(thumbPos);
    }

    private void updateThumbPosFromCurrentProgress() {
        int thumbWidth = mThumb.getIntrinsicWidth();
        int addedThumb = mAddedTouchBounds;
        int halfThumb = thumbWidth / 2;
        float scaleDraw = (mValue - mMin) / (float) (mMax - mMin);


        int left = getPaddingLeft() + halfThumb + addedThumb;
        int right = getWidth() - (getPaddingRight() + halfThumb + addedThumb);
        int available = right - left;

        final int thumbPos = (int) (scaleDraw * available + 0.5f);
        updateThumbPos(thumbPos);
    }

    private void updateThumbPos(int posX) {
        int thumbWidth = mThumb.getIntrinsicWidth();
        int halfThumb = thumbWidth / 2;
        int start;
        if (isRtl()) {
            start = getWidth() - getPaddingRight() - mAddedTouchBounds;
            posX = start - posX - thumbWidth;
        } else {
            start = getPaddingLeft() + mAddedTouchBounds;
            posX = start + posX;
        }
        mThumb.copyBounds(mInvalidateRect);
        mThumb.setBounds(posX, mInvalidateRect.top, posX + thumbWidth, mInvalidateRect.bottom);
        if (isRtl()) {
            mScrubber.getBounds().right = start - halfThumb;
            mScrubber.getBounds().left = posX + halfThumb;
        } else {
            mScrubber.getBounds().left = start + halfThumb;
            mScrubber.getBounds().right = posX + halfThumb;
        }
        final Rect finalBounds = mTempRect;
        mThumb.copyBounds(finalBounds);
        if (!isInEditMode()) {
            mIndicator.move(finalBounds.centerX());
        }

        mInvalidateRect.inset(-mAddedTouchBounds, -mAddedTouchBounds);
        finalBounds.inset(-mAddedTouchBounds, -mAddedTouchBounds);
        mInvalidateRect.union(finalBounds);
        SeekBarCompat.setHotspotBounds(mRipple, finalBounds.left, finalBounds.top, finalBounds.right, finalBounds.bottom);
        invalidate(mInvalidateRect);
    }


    private void setHotspot(float x, float y) {
        DrawableCompat.setHotspot(mRipple, x, y);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mThumb || who == mTrack || who == mScrubber || who == mRipple || super.verifyDrawable(who);
    }

    private void attemptClaimDrag() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private Runnable mShowIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            showFloater();
        }
    };

    private void showFloater() {
        if (!isInEditMode()) {
            mThumb.animateToPressed();
            mIndicator.showIndicator(this, mThumb.getBounds());
            notifyBubble(true);
        }
    }

    private void hideFloater() {
        removeCallbacks(mShowIndicatorRunnable);
        if (!isInEditMode()) {
            mIndicator.dismiss();
            notifyBubble(false);
        }
    }

    private MarkerDrawable.MarkerAnimationListener mFloaterListener = new MarkerDrawable.MarkerAnimationListener() {
        @Override
        public void onClosingComplete() {
            mThumb.animateToNormal();
        }

        @Override
        public void onOpeningComplete() {

        }

    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mShowIndicatorRunnable);
        if (!isInEditMode()) {
            mIndicator.dismissComplete();
        }
    }


    public boolean isRtl() {
        return (ViewCompat.getLayoutDirection(this) == LAYOUT_DIRECTION_RTL) && mMirrorForRtl;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomState state = new CustomState(superState);
        state.progress = getProgress();
        state.max = mMax;
        state.min = mMin;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(CustomState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        CustomState customState = (CustomState) state;
        setMin(customState.min);
        setMax(customState.max);
        setProgress(customState.progress, false);
        super.onRestoreInstanceState(customState.getSuperState());
    }

    static class CustomState extends BaseSavedState {
        private int progress;
        private int max;
        private int min;

        public CustomState(Parcel source) {
            super(source);
            progress = source.readInt();
            max = source.readInt();
            min = source.readInt();
        }

        public CustomState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel outcoming, int flags) {
            super.writeToParcel(outcoming, flags);
            outcoming.writeInt(progress);
            outcoming.writeInt(max);
            outcoming.writeInt(min);
        }

        public static final Creator<CustomState> CREATOR =
                new Creator<CustomState>() {

                    @Override
                    public CustomState[] newArray(int size) {
                        return new CustomState[size];
                    }

                    @Override
                    public CustomState createFromParcel(Parcel incoming) {
                        return new CustomState(incoming);
                    }
                };
    }
}
