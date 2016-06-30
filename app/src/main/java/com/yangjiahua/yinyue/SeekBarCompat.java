package com.yangjiahua.yinyue;


import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;


public class SeekBarCompat {


    public static void setOutlineProvider(View view, final MarkerDrawable markerDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SeekBarCompatDontCrash.setOutlineProvider(view, markerDrawable);
        }
    }


    public static Drawable getRipple(ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return SeekBarCompatDontCrash.getRipple(colorStateList);
        } else {
            return new AlmostRippleDrawable(colorStateList);
        }
    }


    public static void setHotspotBounds(Drawable drawable, int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //We don't want the full size rect, Lollipop ripple would be too big
            int size = (right - left) / 8;
            DrawableCompat.setHotspotBounds(drawable, left + size, top + size, right - size, bottom - size);
        } else {
            drawable.setBounds(left, top, right, bottom);
        }
    }


    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            SeekBarCompatDontCrash.setBackground(view, background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }


    public static void setTextDirection(TextView textView, int textDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            SeekBarCompatDontCrash.setTextDirection(textView, textDirection);
        }
    }

    public static boolean isInScrollingContainer(ViewParent p) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return SeekBarCompatDontCrash.isInScrollingContainer(p);
        }
        return false;
    }

    public static boolean isHardwareAccelerated(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return SeekBarCompatDontCrash.isHardwareAccelerated(view);
        }
        return false;
    }
}

