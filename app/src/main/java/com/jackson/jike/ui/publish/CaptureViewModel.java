package com.jackson.jike.ui.publish;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Copyright (C), 2015-2020
 * FileName: CaptureViewModel
 * Author: Luo
 * Date: 2020/6/11 9:55
 * Description:
 */
public class CaptureViewModel extends ViewModel {


    public MutableLiveData<Boolean> liveData;

    public CaptureViewModel() {
        liveData = new MutableLiveData<>();

    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
