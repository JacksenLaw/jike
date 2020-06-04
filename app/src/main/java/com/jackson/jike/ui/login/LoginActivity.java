package com.jackson.jike.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.jackson.common.util.StatusBar;
import com.jackson.jike.R;
import com.jackson.jike.model.User;
import com.jackson.jike.presenter.UserPresenter;
import com.jackson.common.util.KLog;
import com.jackson.network.ApiResponse;
import com.jackson.network.JsonCallback;
import com.jakewharton.rxbinding3.view.RxView;

import java.util.Calendar;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private ProgressBar loading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBar.fitSystemBar(this);
        setContentView(R.layout.activity_login);

        KLog.i("UUID = " + UUID.randomUUID());

        loading = findViewById(R.id.loading);

        RxView.clicks(findViewById(R.id.back))
                .subscribe(unit -> finish());

        RxView.clicks(findViewById(R.id.login_qq))
//                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(unit -> toggle());
    }

    private void toggle() {
        if (loading.getVisibility() == View.GONE) loading.setVisibility(View.VISIBLE);
        save("我乐苦多", "avatar", "openid", Calendar.getInstance().getTimeInMillis());
    }

    private void save(String nickname, String avatar, String openid, long expires_time) {
        UserPresenter.login(new JsonCallback<User>() {
            @Override
            public void onSuccess(ApiResponse<User> response) {
                loading.setVisibility(View.GONE);
                finish();
            }
        }, nickname, avatar, openid, expires_time);
    }

}
