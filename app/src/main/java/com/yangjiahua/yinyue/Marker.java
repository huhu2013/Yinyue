package com.yangjiahua.yinyue;



import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


public class Marker extends ViewGroup implements MarkerDrawable.MarkerAnimationListener {
    private static final int PADDING_DP = 4;
    private static final int ELEVATION_DP = 8;
    private static final int SEPARATION_DP = 30;

    private TextView mNumber;

    private int mWidth;

    private int mSeparation;
    MarkerDrawable mMarkerDrawable;

    public Marker(Context context) {
        this(context, null);
    }

    public Marker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.discreteSeekBarStyle);
    }

    public Marker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, "0");
    }

    public Marker(Context context, AttributeSet attrs, int defStyleAttr, String maxValue) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiscreteSeekBar,
                R.attr.discreteSeekBarStyle, R.style.Widget_DiscreteSeekBar);

        int padding = (int) (PADDING_DP * displayMetrics.density) * 2;
        int textAppearanceId = a.getResourceId(R.styleable.DiscreteSeekBar_dsb_indicatorTextAppearance,
                R.style.Widget_DiscreteIndicatorTextAppearance);
        mNumber = new TextView(context);

        mNumber.setPadding(padding, 0, padding, 0);
        mNumber.setTextAppearance(context, textAppearanceId);
        mNumber.setGravity(Gravity.CENTER);
        mNumber.setText(maxValue);
        mNumber.setMaxLines(1);
        mNumber.setSingleLine(true);
        SeekBarCompat.setTextDirection(mNumber, TEXT_DIRECTION_LOCALE);
        mNumber.setVisibility(View.INVISIBLE);


        setPadding(padding, padding, padding, padding);

        resetSizes(maxValue);

        mSeparation = (int) (SEPARATION_DP * displayMetrics.density);
        int thumbSize = (int) (ThumbDrawable.DEFAULT_SIZE_DP * displayMetrics.density);
        ColorStateList color = a.getColorStateList(R.styleable.DiscreteSeekBar_dsb_indicatorColor);
        mMarkerDrawable = new MarkerDrawable(color, thumbSize);
        mMarkerDrawable.setCallback(this);
        mMarkerDrawable.setMarkerListener(this);
        mMarkerDrawable.setExternalOffset(padding);


        float elevation = a.getDimension(R.styleable.DiscreteSeekBar_dsb_indicatorElevation, ELEVATION_DP * displayMetrics.density);
        ViewCompat.setElevation(this, elevation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SeekBarCompat.setOutlineProvider(this, mMarkerDrawable);
        }
        a.recycle();
    }

    public void resetSizes(String maxValue) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        mNumber.setText("-" + maxValue);

        int wSpec = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, MeasureSpec.AT_MOST);
        int hSpec = MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, MeasureSpec.AT_MOST);
        mNumber.measure(wSpec, hSpec);
        mWidth = Math.max(mNumber.getMeasuredWidth(), mNumber.getMeasuredHeight());
        removeView(mNumber);
        addView(mNumber, new FrameLayout.LayoutParams(mWidth, mWidth, Gravity.LEFT | Gravity.TOP));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mMarkerDrawable.draw(canvas);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int widthSize = mWidth + getPaddingLeft() + getPaddingRight();
        int heightSize = mWidth + getPaddingTop() + getPaddingBottom();

        int diff = (int) ((1.41f * mWidth) - mWidth) / 2;
        setMeasuredDimension(widthSize, heightSize + diff + mSeparation);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();

        mNumber.layout(left, top, left + mWidth, top + mWidth);

        mMarkerDrawable.setBounds(left, top, right, bottom);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mMarkerDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        animateOpen();
    }

    public void setValue(CharSequence value) {
        mNumber.setText(value);
    }

    public CharSequence getValue() {
        return mNumber.getText();
    }

    public void animateOpen() {
        mMarkerDrawable.stop();
        mMarkerDrawable.animateToPressed();
    }

    public void animateClose() {
        mMarkerDrawable.stop();
        mNumber.setVisibility(View.INVISIBLE);
        mMarkerDrawable.animateToNormal();
    }

    @Override
    public void onOpeningComplete() {
        mNumber.setVisibility(View.VISIBLE);
        if (getParent() instanceof MarkerDrawable.MarkerAnimationListener) {
            ((MarkerDrawable.MarkerAnimationListener) getParent()).onOpeningComplete();
        }
    }

    @Override
    public void onClosingComplete() {
        if (getParent() instanceof MarkerDrawable.MarkerAnimationListener) {
            ((MarkerDrawable.MarkerAnimationListener) getParent()).onClosingComplete();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMarkerDrawable.stop();
    }

    public void setColors(int startColor, int endColor) {
        mMarkerDrawable.setColors(startColor, endColor);
    }
}

