package com.jackson.common.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jackson.common.R;

/**
 * Copyright (C), 2015-2020
 * FileName: EmptyView
 * Author: Luo
 * Date: 2020/3/26 21:24
 * Description: 数据加载错误时的空布局
 */
public class EmptyView extends LinearLayout {

    private ImageView icon;
    private TextView title;
    private Button action;

    public EmptyView(@NonNull Context context) {
        this(context,null);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_empty_view,this,true);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        icon = findViewById(R.id.empty_icon);
        title = findViewById(R.id.empty_text);
        action = findViewById(R.id.empty_action);

    }

    public void setIcon(@DrawableRes int iconRes){
        icon.setImageResource(iconRes);
    }

    public void setTitle(String text) {
        if (TextUtils.isEmpty(text)) {
            title.setVisibility(GONE);
        } else {
            title.setText(text);
            title.setVisibility(VISIBLE);
        }

    }

    public void setButton(String text, View.OnClickListener listener) {
        if (TextUtils.isEmpty(text)) {
            action.setVisibility(GONE);
        } else {
            action.setText(text);
            action.setVisibility(VISIBLE);
            action.setOnClickListener(listener);
        }

    }

}
