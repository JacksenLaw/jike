package com.jackson.common.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

public class StatusBar {

    public static void fitSystemBar(Activity activity) {
        fitSystemBar(activity, false, Color.WHITE);
    }

    public static void fitSystemBar(Activity activity, @ColorInt int color) {
        fitSystemBar(activity, false, color);
    }

    public static void fitSystemBarFullScreen(Activity activity) {
        fitSystemBar(activity, true, Color.BLACK);
    }

    /**
     * 6.0级以上的沉浸式布局
     *
     * @param activity activity
     * @param color    状态栏颜色
     */
    public static void fitSystemBar(Activity activity, boolean light, @ColorInt int color) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN -- 能够使得我们的页面布局延伸到状态栏之下，但不会隐藏状态栏。也就相当于状态栏是遮盖在布局之上的
        //View.SYSTEM_UI_FLAG_FULLSCREEN -- 能够使得我们的页面布局延伸到状态栏，但是会隐藏状态栏。 相当于WindowManager.LayoutParams.FLAG_FULLSCREEN
        int mSystemUiVisibility = decorView.getSystemUiVisibility();
        if (light) {
            mSystemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            mSystemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        int uiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(mSystemUiVisibility | uiVisibility);

        //允许window 对状态栏的背景开启绘制
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //指定状态栏背景色
        window.setStatusBarColor(color);
    }

    /**
     * 6.0及以上的状态栏色调
     *
     * @param activity activity
     * @param light    true:白字 , false:黑字
     */
    public static void lightStatusBar(Activity activity, boolean light) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = decorView.getSystemUiVisibility();
        if (light) {
            visibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(visibility);
    }
}
