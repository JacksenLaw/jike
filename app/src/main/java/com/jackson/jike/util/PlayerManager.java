package com.jackson.jike.util;

import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.jackson.common.AppGlobals;

/**
 * Copyright (C), 2015-2020
 * FileName: PlayerManager
 * Author: Luo
 * Date: 2020/5/8 23:17
 * Description: 播放系统音效
 */
public class PlayerManager {

    private MediaPlayer mediaPlayer;

    private static class Holder {
        private static PlayerManager INSTANCE = new PlayerManager();
    }

    public static PlayerManager get() {
        return Holder.INSTANCE;
    }

    /**
     * 播放默认铃声
     */
    public void play() {
        stop();
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(AppGlobals.getApplication(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        mediaPlayer.start();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 播放系统默认提示音
     */
    public void playDefaultRingtone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(AppGlobals.getApplication(), notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setVolume(0.5f);
        }
        r.play();
    }


    /**
     * 播放系统默认来电铃声
     */
    public void playDefaultCall() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone r = RingtoneManager.getRingtone(AppGlobals.getApplication(), notification);
        r.play();
    }

    /**
     * 播放系统默认闹钟铃声
     */
    public void playDefaultAlarm() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone r = RingtoneManager.getRingtone(AppGlobals.getApplication(), notification);
        r.play();
    }

}
