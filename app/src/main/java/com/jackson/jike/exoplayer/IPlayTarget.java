package com.jackson.jike.exoplayer;

import android.view.ViewGroup;

/**
 * Copyright (C), 2015-2020
 * FileName: IPlayTarget
 * Author: Luo
 * Date: 2020/5/9 13:36
 * Description:
 */
public interface IPlayTarget {

    //获取PlayerView所在容器
    ViewGroup getOwner();

    //活跃状态 视频可播放
    void onActive();

    //非活跃状态 暂停
    void inActive();

    boolean isPlaying();

}
