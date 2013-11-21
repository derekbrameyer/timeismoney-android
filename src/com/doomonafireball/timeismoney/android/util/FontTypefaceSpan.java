package com.doomonafireball.timeismoney.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import oak.util.OakUtils;
import oak.widget.TextViewWithFont;

/**
 * User: derek Date: 5/7/13 Time: 12:52 PM
 */
public class FontTypefaceSpan extends MetricAffectingSpan {

    private Typeface mTypeface;

    public FontTypefaceSpan(Context context, String typefaceName) {
        mTypeface = OakUtils.getStaticTypeFace(context, typefaceName);
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(mTypeface);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(mTypeface);
    }
}