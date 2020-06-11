package com.jackson.permission;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;

/**
 * Copyright (C), 2015-2020
 * FileName: PermissionFragment
 * Author: Luo
 * Date: 2020/6/10 14:30
 * Description: 权限回调fragment
 */
public class PermissionFragment extends Fragment {

    public MutableLiveData<PermissionResult> liveData;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    public void requestPermissions(String... permissions) {
        liveData = new MutableLiveData<>();
        ArrayList<String> tempPermission = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionFragment", "permission = " + permission);
                tempPermission.add(permission);
            }
        }
        if (tempPermission.isEmpty()) {
            liveData.postValue(new PermissionResult.Grant());
        } else {
            String[] array = new String[tempPermission.size()];
            requestPermissions(tempPermission.toArray(array), PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) return;

        ArrayList<String> denyPermission = new ArrayList<>();
        ArrayList<String> rationalePermission = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            Log.i("PermissionFragment", "请求的权限 = " + permissions[i]);
            if (!isGranted(permissions[i])) {
                Log.i("PermissionFragment", "已被拒绝权限 = " + permissions[i]);
                //用户勾选了不再提示
                if (!shouldShowRequestPermissionRationale(permissions[i])) {
                    Log.i("PermissionFragment", "用户勾选了不再提示 = " + permissions[i]);
                    rationalePermission.add(permissions[i]);
                } else {
                    Log.i("PermissionFragment", "已被拒绝权限 = " + permissions[i]);
                    denyPermission.add(permissions[i]);
                }
            }
        }

        if (denyPermission.isEmpty() && rationalePermission.isEmpty()) {
            liveData.postValue(new PermissionResult.Grant());
        } else {
            if (!rationalePermission.isEmpty()) {
                String[] array = new String[rationalePermission.size()];
                liveData.postValue(new PermissionResult.Rationale(rationalePermission.toArray(array)));
            } else {
                String[] array = new String[denyPermission.size()];
                liveData.postValue(new PermissionResult.Deny(denyPermission.toArray(array)));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        final FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        }
        return fragmentActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isRevoked(String permission) {
        final FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        }
        return fragmentActivity.getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }

    public void removeObserver(Observer observer) {
        if(liveData != null){
            liveData.removeObserver(observer);
        }
    }
}
