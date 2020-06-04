package com.jackson.jike.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.jackson.common.AppGlobals;
import com.jackson.common.util.KLog;
import com.jackson.jike.R;

/**
 * Copyright (C), 2015-2020
 * FileName: StringConvert
 * Author: Luo
 * Date: 2020/3/26 13:03
 * Description: 数字转换
 */
public class StringConvert {

    public static String convertFeedUgc(int count) {
        KLog.i("count = " + count);
        if (count < 10000) {
            return String.valueOf(count);
        }
        return count / 10000 + "w";
    }

    public static String convertTagFeedList(int count) {
        if (count < 10000) {
            return count + AppGlobals.getApplication().getString(R.string.look_num);
        } else {
            return count / 10000 + AppGlobals.getApplication().getString(R.string.look_num_w);
        }
    }

    public static CharSequence convertSpannable(int count, String intro) {
        String countStr = String.valueOf(count);
        SpannableString ss = new SpannableString(countStr + intro);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new AbsoluteSizeSpan(16, true), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, countStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

}
