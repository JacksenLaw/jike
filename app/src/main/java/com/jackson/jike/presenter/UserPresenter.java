package com.jackson.jike.presenter;

import com.jackson.common.util.CommonUtil;
import com.jackson.jike.model.User;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;
import com.jackson.network.JsonCallback;
import com.jackson.network.schedulers.Scheduler;

/**
 * Copyright (C), 2015-2020
 * FileName: UserPresenter
 * Author: Luo
 * Date: 2020/5/14 15:22
 * Description:
 */
public class UserPresenter {

    public static void login(JsonCallback<User> callback, String nickname, String avatar, String openid, long expires_time) {
        ApiService.get("/user/insert")
                .addParam("name", nickname)
                .addParam("avatar", avatar)
                .addParam("qqOpenId", openid)
                .addParam("expires_time", expires_time)
                .observerOn(Scheduler.mainThread())
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        if (response.body != null) {
                            UserManager.get().save(response.body);
                            callback.onSuccess(response);
                        } else {
                            CommonUtil.showToast("登录失败");
                        }
                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        CommonUtil.showToast("登录失败,msg:" + response.message);
                    }
                });
    }

}
