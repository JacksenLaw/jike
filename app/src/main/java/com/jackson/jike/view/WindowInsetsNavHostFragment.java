package com.jackson.jike.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Copyright (C), 2015-2020
 * FileName: WindowInsetsNavHostFragment
 * Author: Luo
 * Date: 2020/5/8 23:17
 * Description: 解决沉浸栏问题， 将沉浸式事件(fitSystemWindows)分发给所有的子view
 */
public class WindowInsetsNavHostFragment extends NavHostFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        WindowInsetsFrameLayout layout = new WindowInsetsFrameLayout(inflater.getContext());
        layout.setId(getId());
        return layout;
    }
}
