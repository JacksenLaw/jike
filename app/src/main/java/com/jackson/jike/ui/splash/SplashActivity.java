package com.jackson.jike.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.button.MaterialButton;
import com.jackson.common.util.StatusBar;
import com.jackson.jike.MainActivity;
import com.jackson.jike.R;
import com.jackson.jike.databinding.ActivitySplashBinding;

import java.math.BigDecimal;

public class SplashActivity extends AppCompatActivity {

    private MaterialButton skipButton;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //由于 启动时设置了 R.style.launcher 的windowBackground属性
        //势必要在进入主页后,把窗口背景清理掉
        setTheme(R.style.AppTheme);
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        ActivitySplashBinding mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        skipButton = findViewById(R.id.skip_button);

        timer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                BigDecimal numerator = new BigDecimal(millisUntilFinished);
                BigDecimal denominator = new BigDecimal(1000);
                skipButton.setText(String.format("跳过 %1s", numerator.divide(denominator, 0, BigDecimal.ROUND_HALF_UP) + "")); //设置倒计时时间
            }

            @Override
            public void onFinish() {
                openActivity();
            }
        }.start();

        mBinding.skipButton.setOnClickListener(v -> {
            mBinding.skipButton.setEnabled(false);
            skipButton.setBackgroundColor(getResources().getColor(R.color.color_theme));
            if (timer != null) {
                timer.onFinish();
                return;
            }
            openActivity();
        });

    }

    private void openActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
