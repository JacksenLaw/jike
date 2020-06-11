package com.jackson.common.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * Copyright (C), 2015-2020
 * FileName: FileProviderUtils
 * Author: Luo
 * Date: 2020/6/10 11:47
 * Description: FileProvider工具类,用于适配Android7.0的文件访问变更
 */
public class FileProviderUtils {
    /**
     * 获取合适的File Uri
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(context, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    /**
     * 获取File Uri from 安卓7及以上版本
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getUriForFile24(Context context, File file) {
        return FileProvider.getUriForFile(context, "com.jackson.jike.fileprovider", file);
    }

    /**
     * 用户安装apk场景
     *
     * @param context
     * @param intent
     * @param type
     * @param file
     * @param writeAble
     */
    public static void setIntentDataAndType(Context context,
                                            Intent intent,
                                            String type,
                                            File file,
                                            boolean writeAble) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(context, file), type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }

}
