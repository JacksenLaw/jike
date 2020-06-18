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

    public int state = -1;

    public @interface Result {
        //允许
        int GRANTED = PackageManager.PERMISSION_GRANTED;
        //拒绝
        int DENIED = PackageManager.PERMISSION_DENIED;
        //拒绝并且不再提示
        int RATIONALE = 1;
    }

    public static class Granted extends PermissionResult {
        public Granted() {
            state = Result.GRANTED;
        }
    }

    public static class Denied extends PermissionResult {
        public String [] permissions;
        public Denied(String... permission) {
            state = Result.DENIED;
            permissions = permission;
        }
    }

    public static class Rationale extends PermissionResult {
        public String [] permissions;
        public Rationale(String... permission) {
            state = Result.RATIONALE;
            permissions = permission;
        }
    }

}
