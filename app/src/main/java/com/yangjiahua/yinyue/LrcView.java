package com.yangjiahua.yinyue;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.List;


public class LrcView extends View implements ILrcView{

	private List<LrcRow> mLrcRows;

	private static final String DEFAULT_TEXT = "听音乐 就在动动音乐";

	private static final float SIZE_FOR_DEFAULT_TEXT = 35;


	private Paint mPaintForHighLightLrc;

	private static final float DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC = 35;

	private float mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC;

	private static final int DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC = 0xffEE4000;

	private int mCurColorForHightLightLrc = DEFAULT_COLOR_FOR_HIGHT_LIGHT_LRC;


	private Paint mPaintForOtherLrc;

	private static final float DEFAULT_SIZE_FOR_OTHER_LRC = 30;

	private float mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC;

	private static final int DEFAULT_COLOR_FOR_OTHER_LRC = Color.WHITE;

	private int mCurColorForOtherLrc = DEFAULT_COLOR_FOR_OTHER_LRC;



	private Paint mPaintForTimeLine;

	private static final int COLOR_FOR_TIME_LINE = 0xffD02090;

	private static final int SIZE_FOR_TIME = 18;

	private boolean mIsDrawTimeLine = false;


	private static final float DEFAULT_PADDING = 20;

	private float mCurPadding = DEFAULT_PADDING;


	public static final float MAX_SCALING_FACTOR = 1.5f;

	public static final float MIN_SCALING_FACTOR = 0.5f;

	private static final float DEFAULT_SCALING_FACTOR = 1.0f;

	private float mCurScalingFactor = DEFAULT_SCALING_FACTOR;


	private Scroller mScroller;

	private static final int DURATION_FOR_LRC_SCROLL = 1500;

	private static final int DURATION_FOR_ACTION_UP = 400;


	private float mCurFraction = 0;
	private int mTouchSlop;

	public LrcView(Context context) {
		super(context);
		init();
	}
	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}



	@Override
	public void init() {
		mScroller = new Scroller(getContext());
		mPaintForHighLightLrc = new Paint();
		mPaintForHighLightLrc.setColor(mCurColorForHightLightLrc);
		mPaintForHighLightLrc.setTextSize(mCurSizeForHightLightLrc);

		mPaintForOtherLrc = new Paint();
		mPaintForOtherLrc.setColor(mCurColorForOtherLrc);
		mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);

		mPaintForTimeLine = new Paint();
		mPaintForTimeLine.setColor(COLOR_FOR_TIME_LINE);
		mPaintForTimeLine.setTextSize(SIZE_FOR_TIME);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	private int mTotleDrawRow;
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mLrcRows == null || mLrcRows.size() == 0){

			mPaintForOtherLrc.setTextSize(SIZE_FOR_DEFAULT_TEXT);
			float textWidth = mPaintForOtherLrc.measureText(DEFAULT_TEXT);
			float textX = (getWidth()-textWidth)/2;
			canvas.drawText(DEFAULT_TEXT, textX, getHeight()/2, mPaintForOtherLrc);
			return;
		}
		if(mTotleDrawRow == 0){

			mTotleDrawRow = (int) (getHeight()/(mCurSizeForOtherLrc+mCurPadding))+4;
		}

		int minRaw = mCurRow - (mTotleDrawRow-1)/2;
		int maxRaw = mCurRow + (mTotleDrawRow-1)/2;
		minRaw = Math.max(minRaw, 0);
		maxRaw = Math.min(maxRaw, mLrcRows.size()-1);

		int count = Math.max(maxRaw-mCurRow, mCurRow-minRaw);

		int alpha = (0xFF-0x11)/count;

		float rowY = getHeight()/2 + minRaw*(mCurSizeForOtherLrc + mCurPadding);
		for (int i = minRaw; i <= maxRaw; i++) {

			if(i == mCurRow){

				float textSize = mCurSizeForOtherLrc + (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction;
				mPaintForHighLightLrc.setTextSize(textSize);

				String text = mLrcRows.get(i).getContent();
				float textWidth = mPaintForHighLightLrc.measureText(text);
				if(textWidth > getWidth()){

					canvas.drawText(text, mCurTextXForHighLightLrc, rowY, mPaintForHighLightLrc);
				}else{

					float textX =  (getWidth()-textWidth)/2;
					canvas.drawText(text, textX, rowY, mPaintForHighLightLrc);
				}
			}else{
				if(i == mLastRow){

					float textSize = mCurSizeForHightLightLrc - (mCurSizeForHightLightLrc - mCurSizeForOtherLrc)*mCurFraction;
					mPaintForOtherLrc.setTextSize(textSize);
				}else{
					mPaintForOtherLrc.setTextSize(mCurSizeForOtherLrc);
				}
				String text = mLrcRows.get(i).getContent();
				float textWidth = mPaintForOtherLrc.measureText(text);
				float textX = (getWidth()-textWidth)/2;

				textX = Math.max(textX, 0);

				int curAlpha = 255-(Math.abs(i-mCurRow)-1)*alpha;
				mPaintForOtherLrc.setColor(0x1000000*curAlpha+0xffffff);
				canvas.drawText(text, textX, rowY, mPaintForOtherLrc);
			}

			rowY += mCurSizeForOtherLrc + mCurPadding;
		}


		if(mIsDrawTimeLine){
			float y = getHeight()/2 + getScrollY();
			canvas.drawText(mLrcRows.get(mCurRow).getTimeStr(), 0, y-5, mPaintForTimeLine);
			canvas.drawLine(0, y, getWidth(), y, mPaintForTimeLine);
		}

	}
	

	private boolean canDrag = false;

	private float firstY;

	private float lastY;
	private float lastX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mLrcRows == null || mLrcRows.size() == 0){
			return false;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			firstY = event.getRawY();
			lastX = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			if(!canDrag){
				if(Math.abs(event.getRawY() - firstY) > mTouchSlop && Math.abs(event.getRawY()-firstY) > Math.abs(event.getRawX()-lastX)){
					canDrag = true;
					mIsDrawTimeLine = true;
					mScroller.forceFinished(true);
					stopScrollLrc();
					mCurFraction = 1;
				}
				lastY = event.getRawY();
			}

			if(canDrag){
				float offset = event.getRawY() - lastY;
				if( getScrollY() - offset < 0){ 
					if(offset > 0){
						offset = offset/3;
					}
				}else if(getScrollY() - offset >mLrcRows.size()*(mCurSizeForOtherLrc+mCurPadding)-mCurPadding){
					if(offset < 0 ){
						offset = offset/3;
					}
				}
				scrollBy(getScrollX(), -(int) offset);
				lastY = event.getRawY();
				int currentRow = (int) (getScrollY()/(mCurSizeForOtherLrc+mCurPadding));
				currentRow = Math.min(currentRow, mLrcRows.size()-1);
				currentRow = Math.max(currentRow, 0);
				seekTo(mLrcRows.get(currentRow).getTime(), false,false);
				return true;
			}
			lastY = event.getRawY();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(!canDrag){
				if(onLrcClickListener != null){
					onLrcClickListener.onClick();
				}
			}else{
				if(onSeekToListener!= null && mCurRow != -1){
					onSeekToListener.onSeekTo(mLrcRows.get(mCurRow).getTime());
				}
				if(getScrollY() < 0){
					smoothScrollTo(0,DURATION_FOR_ACTION_UP);
				}else if(getScrollY() >mLrcRows.size()*(mCurSizeForOtherLrc+mCurPadding)-mCurPadding){
					smoothScrollTo((int) (mLrcRows.size()*(mCurSizeForOtherLrc+mCurPadding)-mCurPadding),DURATION_FOR_ACTION_UP);
				}

				canDrag = false;
				mIsDrawTimeLine = false;
				invalidate();
			}
			break;
		}
		return true;
	}

	@Override
	public void setLrcRows(List<LrcRow> lrcRows) {
		reset();
		this.mLrcRows = lrcRows;
		invalidate();
	}


	private int mCurRow =-1;

	private int mLastRow = -1;
	
	@Override
	public void seekTo(int progress,boolean fromSeekBar,boolean fromSeekBarByUser) {
		if(mLrcRows == null || mLrcRows.size() == 0){
			return;
		} 

		if(fromSeekBar && canDrag){
			return;
		}
		for (int i = mLrcRows.size()-1; i >= 0; i--) {

			if(progress >= mLrcRows.get(i).getTime()){
				if(mCurRow != i){
					mLastRow = mCurRow;
					mCurRow = i;
					log("mCurRow=i="+mCurRow);
					if(fromSeekBarByUser){
						if(!mScroller.isFinished()){
							mScroller.forceFinished(true);
						}
						scrollTo(getScrollX(), (int) (mCurRow * (mCurSizeForOtherLrc+mCurPadding)));
					}else{
						smoothScrollTo( (int) (mCurRow * (mCurSizeForOtherLrc+mCurPadding)), DURATION_FOR_LRC_SCROLL);
					}

					float textWidth = mPaintForHighLightLrc.measureText(mLrcRows.get(mCurRow).getContent());
					log("textWidth="+textWidth+"getWidth()="+getWidth());
					if(textWidth > getWidth()){
						if(fromSeekBarByUser){
							mScroller.forceFinished(true);
						}
						log("开始水平滚动歌词:"+mLrcRows.get(mCurRow).getContent());
						startScrollLrc(getWidth()-textWidth, (long) (mLrcRows.get(mCurRow).getTotalTime()*0.6));
					}
					invalidate();
				}
				break;
			}
		}

	}


	private ValueAnimator mAnimator;

	private void startScrollLrc(float endX,long duration){
		if(mAnimator == null){
			mAnimator = ValueAnimator.ofFloat(0,endX);
			mAnimator.addUpdateListener(updateListener);
		}else{
			mCurTextXForHighLightLrc = 0;
			mAnimator.cancel();
			mAnimator.setFloatValues(0,endX);
		}
		mAnimator.setDuration(duration);
		mAnimator.setStartDelay((long) (duration*0.3));
		mAnimator.start();
	}


	private void stopScrollLrc(){
		if(mAnimator != null){
			mAnimator.cancel();
		}
		mCurTextXForHighLightLrc = 0;
	}

	private float mCurTextXForHighLightLrc;

	AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			//TODO
			mCurTextXForHighLightLrc = (Float) animation.getAnimatedValue();
			log("mCurTextXForHighLightLrc="+mCurTextXForHighLightLrc);
			invalidate();
		}
	};

	@Override
	public void setLrcScalingFactor(float scalingFactor) {
		mCurScalingFactor = scalingFactor;
		mCurSizeForHightLightLrc = DEFAULT_SIZE_FOR_HIGHT_LIGHT_LRC * mCurScalingFactor;
		mCurSizeForOtherLrc = DEFAULT_SIZE_FOR_OTHER_LRC * mCurScalingFactor;
		mCurPadding = DEFAULT_PADDING * mCurScalingFactor;
		mTotleDrawRow = (int) (getHeight()/(mCurSizeForOtherLrc+mCurPadding))+3;
		log("mRowTotal="+mTotleDrawRow);
		scrollTo(getScrollX(), (int) (mCurRow*(mCurSizeForOtherLrc+mCurPadding)));
		invalidate();
		mScroller.forceFinished(true);
	}

	@Override
	public void reset() {
		if(!mScroller.isFinished()){
			mScroller.forceFinished(true);
		}
		mLrcRows = null;
		scrollTo(getScrollX(), 0);
		invalidate();
	}



	private void smoothScrollTo(int dstY,int duration){
		int oldScrollY = getScrollY();
		int offset = dstY - oldScrollY;
		mScroller.startScroll(getScrollX(), oldScrollY, getScrollX(), offset, duration);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldY = getScrollY();
				int y = mScroller.getCurrY();
				if (oldY != y && !canDrag) {
					scrollTo(getScrollX(), y);
				}
				mCurFraction = mScroller.timePassed()*3f/DURATION_FOR_LRC_SCROLL;
				mCurFraction = Math.min(mCurFraction, 1F);
				invalidate();
			}
		} 
	}

	public float getmCurScalingFactor() {
		return mCurScalingFactor;
	}
	
	private OnSeekToListener onSeekToListener;
	public void setOnSeekToListener(OnSeekToListener onSeekToListener) {
		this.onSeekToListener = onSeekToListener;
	}

	public interface OnSeekToListener{
		void onSeekTo(int progress);
	}

	private OnLrcClickListener onLrcClickListener;
	public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
		this.onLrcClickListener = onLrcClickListener;
	}

	public interface OnLrcClickListener{
		void onClick();
	}
	public void log(Object o){
		Log.d("LrcView", o+"");
	}
}
