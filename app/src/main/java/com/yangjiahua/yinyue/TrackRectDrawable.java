package com.yangjiahua.yinyue;


import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;


public class TrackRectDrawable extends StateDrawable {
    public TrackRectDrawable(@NonNull ColorStateList tintStateList) {
        super(tintStateList);
    }

    @Override
    void doDraw(Canvas canvas, Paint paint) {
        canvas.drawRect(getBounds(), paint);
    }

}

