package com.jackson.permission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * Copyright (C), 2015-2020
 * FileName: LivePermission
 * Author: Luo
 * Date: 2020/6/10 14:45
 * Description:
 */
public class LivePermission {

    private static final String TAG = "LivePermission";

    private volatile PermissionFragment liveFragment;

    public LivePermission(AppCompatActivity activity) {
        liveFragment = (PermissionFragment) getInstance(activity.getSupportFragmentManager());
    }

    public LivePermission(Fragment fragment) {
        liveFragment = (PermissionFragment) getInstance(fragment.getFragmentManager());
    }

    private Fragment getInstance(FragmentManager fragmentManager) {
        if (liveFragment == null) {
            synchronized (LivePermission.class) {
                if (liveFragment == null) {
                    Fragment fragment = fragmentManager.findFragmentByTag(TAG);
                    if (fragment == null) {
                        fragmentManager.beginTransaction().add(new PermissionFragment(), TAG).commitNow();
                        return fragmentManager.findFragmentByTag(TAG);
                    } else {
                        return fragmentManager.findFragmentByTag(TAG);
                    }
                }
            }
        }
        return null;
    }

    public MutableLiveData<PermissionResult> request(String permission) {
        return requestArray(permission);
    }

    public MutableLiveData<PermissionResult> requestArray(String... permission) {
        liveFragment.requestPermissions(permission);
        return liveFragment.liveData;
    }

    public void removeObserver(Observer observer){
        liveFragment.removeObserver(observer);
    }

}
