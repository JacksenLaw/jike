package com.jackson.permission;

import android.content.pm.PackageManager;

/**
 * Copyright (C), 2015-2020
 * FileName: PermissionResult
 * Author: Luo
 * Date: 2020/6/10 14:31
 * Description:
 */
public class PermissionResult {

    public int State = -1;

    public @interface Result {
        //允许
        int GRANTED = PackageManager.PERMISSION_GRANTED;
        //拒绝
        int DENIED = PackageManager.PERMISSION_DENIED;
        //拒绝并且不再提示
        int RATIONALE = 1;
    }

    public static class Grant extends PermissionResult {
        public Grant() {
            State = Result.GRANTED;
        }
    }

    public static class Deny extends PermissionResult {
        public String [] permissions;
        public Deny(String... permission) {
            State = Result.DENIED;
            permissions = permission;
        }
    }

    public static class Rationale extends PermissionResult {
        public String [] permissions;
        public Rationale(String... permission) {
            State = Result.RATIONALE;
            permissions = permission;
        }
    }

}
