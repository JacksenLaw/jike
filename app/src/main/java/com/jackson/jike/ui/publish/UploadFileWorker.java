package com.jackson.jike.ui.publish;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jackson.common.util.FileUploadManager;

/**
 * Copyright (C), 2015-2020
 * FileName: UploadFileWorker
 * Author: Luo
 * Date: 2020/5/29 15:09
 * Description:
 */
public class UploadFileWorker extends Worker {

    public UploadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String filePath = inputData.getString("file");
        String fileUrl = FileUploadManager.upload(filePath);
        if (TextUtils.isEmpty(filePath)) {
            return Result.failure();
        }

        Data outputData = new Data.Builder()
                .putString("fileUrl", fileUrl)
                .build();

        return Result.success(outputData);
    }
}
