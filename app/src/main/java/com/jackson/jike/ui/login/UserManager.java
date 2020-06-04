package com.jackson.jike.ui.login;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jackson.common.AppGlobals;
import com.jackson.common.util.CommonUtil;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;
import com.jackson.network.JsonCallback;
import com.jackson.network.cache.CacheManager;
import com.jackson.jike.model.User;

import java.util.Calendar;

public class UserManager {
    private static final String KEY_CACHE_USER = "cache_user";
    private static UserManager mUserManager = new UserManager();
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private User mUser;

    public static UserManager get() {
        return mUserManager;
    }

    private UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null && cache.expires_time < Calendar.getInstance().getTimeInMillis()) {
            mUser = cache;
        }
    }

    public void save(User user) {
        mUser = user;
        mUser.avatar = "https://wx1.sinaimg.cn/mw690/c89af13egy1gfc66rqj8nj21900u0e8a.jpg";
        mUser.description = "陈信宏小迷弟一枚~";
        CacheManager.save(KEY_CACHE_USER, user);
        if (userLiveData.hasObservers()) {
            userLiveData.postValue(user);
        }
    }

    public LiveData<User> login(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return userLiveData;
    }

    public boolean isLogin() {
        return mUser != null && mUser.expires_time < Calendar.getInstance().getTimeInMillis();
    }

    public User getUser() {
        return isLogin() ? mUser : null;
    }

    public long getUserId() {
        return isLogin() ? mUser.userId : 0;
    }


    public LiveData<User> refresh() {
        if (!isLogin()) {
            return login(AppGlobals.getApplication());
        }
        MutableLiveData<User> liveData = new MutableLiveData<>();
        ApiService.get("/user/query")
                .addParam("userId", getUserId())
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        save(response.body);
                        liveData.postValue(getUser());
                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        CommonUtil.showToast(response.message);
                        liveData.postValue(null);
                    }
                });
        return liveData;
    }

    public void logout() {
        CacheManager.delete(KEY_CACHE_USER, mUser);
        mUser = null;
    }
}
