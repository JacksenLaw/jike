package com.jackson.jike.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;

/**
 * Copyright (C), 2015-2020
 * FileName: BaseActivity
 * Author: Luo
 * Date: 2020/5/9 20:43
 * Description:
 */
public abstract class BaseActivity<M extends ViewModel> extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

    }

    public abstract int getLayoutId();

}
