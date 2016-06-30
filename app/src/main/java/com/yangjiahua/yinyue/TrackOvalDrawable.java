package com.yangjiahua.yinyue;


import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;


public class TrackOvalDrawable extends StateDrawable {
    private RectF mRectF = new RectF();

    public TrackOvalDrawable(@NonNull ColorStateList tintStateList) {
        super(tintStateList);
    }

    @Override
    void doDraw(Canvas canvas, Paint paint) {
        mRectF.set(getBounds());
        canvas.drawOval(mRectF, paint);
    }

}

