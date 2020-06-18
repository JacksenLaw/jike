package com.jackson.permission;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

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
    private String[] permissionTips;

    /**
     * 判断是不是M及以上版本
     */

    private boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public void requestPermissions(String[] permissions, String[] permissionTips) {
        this.permissionTips = permissionTips;
        liveData = new MutableLiveData<>();
        if (!isM()) {
            Log.i("PermissionFragment", "no M");
            liveData.postValue(new PermissionResult.Granted());
            return;
        }
        ArrayList<String> tempPermission = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionFragment", "permission = " + permission);
                tempPermission.add(permission);
            }
        }
        if (tempPermission.isEmpty()) {
            liveData.postValue(new PermissionResult.Granted());
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
        ArrayList<String> denyPermissionTips = new ArrayList<>();
        ArrayList<String> rationalePermission = new ArrayList<>();
        ArrayList<String> rationalePermissionTips = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (!isGranted(permissions[i])) {
                //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
                //可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                if (!shouldShowRequestPermissionRationale(permissions[i])) {
                    rationalePermission.add(permissions[i]);
                    rationalePermissionTips.add(permissionTips[i]);
                } else {
                    //用户拒绝权限请求，但未选中“不再提示”选项
                    denyPermission.add(permissions[i]);
                    denyPermissionTips.add(permissionTips[i]);
                }
            }
        }

        if (denyPermission.isEmpty() && rationalePermission.isEmpty()) {
            //所有权限全部授权
            liveData.postValue(new PermissionResult.Granted());
        } else {
            if (!rationalePermission.isEmpty()) {
                //不再提示部分或全部授权
                String[] array = new String[rationalePermission.size()];
                showRationaleDialog(rationalePermission.toArray(array), rationalePermissionTips.toArray(array));
            } else {
                //禁止部分或全部权限
                String[] array = new String[denyPermission.size()];
                showDenyDialog(denyPermission.toArray(array), denyPermissionTips.toArray(array));
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

    /**
     * 显示授权失败提示框
     *
     * @param permissions 权限
     */
    private void showDenyDialog(String[] permissions, String[] permissionTips) {
        StringBuilder tip = new StringBuilder();
        for (String permissionTip : permissionTips) {
            tip.append("\t\t").append(permissionTip).append("\n\n");
        }
        new AlertDialog.Builder(getContext())
                .setMessage("使用本应用须打开:\n\n" +
                        tip + "\n等 " + permissions.length + "项权限")
                .setPositiveButton("去授权", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                    intent.setData(uri);
                    getContext().startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    liveData.postValue(new PermissionResult.Denied(permissions));
                })
                .setCancelable(false).show();
    }

    /**
     * 用户点击“不在授权”后显示提示框
     *
     * @param permissions 权限
     */
    private void showRationaleDialog(String[] permissions, String[] permissionTips) {
        StringBuilder tip = new StringBuilder();
        for (String permissionTip : permissionTips) {
            tip.append("\t\t").append(permissionTip).append("\n\n");
        }
        new AlertDialog.Builder(getContext())
                .setMessage("系统获取相关权限失败:\n\n" +
                        tip +
                        "\n授权失败将导致部分功能无法正常使用，需要到设置页面手动授权")
                .setPositiveButton("去授权", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                    intent.setData(uri);
                    getContext().startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    liveData.postValue(new PermissionResult.Rationale(permissions));
                })
                .setCancelable(false).show();
    }

}
