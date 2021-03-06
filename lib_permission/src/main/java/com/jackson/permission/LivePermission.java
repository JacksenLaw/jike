package com.jackson.permission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

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

    public MutableLiveData<PermissionResult> requestPermission(String permission, String permissionTip) {
        return requestPermissions(new String[]{permission}, new String[]{permissionTip});
    }

    public MutableLiveData<PermissionResult> requestPermissions(String[] permission, String[] permissionTips) {
        if (permission.length != permissionTips.length) {
            throw new RuntimeException("The length of permission must be the same as the length of permissionTips");
        }
        liveFragment.requestPermissions(permission, permissionTips);
        return liveFragment.liveData;
    }

    public boolean checkSelfPermission(String permission) {
        return liveFragment.isGranted(permission);
    }

}
