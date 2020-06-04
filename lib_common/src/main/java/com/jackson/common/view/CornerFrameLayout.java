package com.jackson.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jackson.common.util.ViewHelper;

/**
 * Copyright (C), 2015-2020
 * FileName: CornerFrameLayout
 * Author: Luo
 * Date: 2020/5/9 10:46
 * Description:圆角FrameLayout
 */
public class CornerFrameLayout extends FrameLayout {

    public CornerFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        ViewHelper.setViewOutLine(this, attrs, defStyleAttr, defStyleRes);
    }

    public void setViewOutline(int radius, int radiusSide) {
        ViewHelper.setViewOutLine(this, radius, radiusSide);
    }

}
