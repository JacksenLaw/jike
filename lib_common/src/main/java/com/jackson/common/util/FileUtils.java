package com.jackson.common.util;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.FileIOUtils;
import com.jackson.common.AppGlobals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class FileUtils {
    /**
     * 截取视频文件的封面图
     *
     * @param filePath
     * @return
     */
    @SuppressLint("RestrictedApi")
    public static LiveData<String> generateVideoCover(final String filePath) {
        final MutableLiveData<String> liveData = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            //bugfix:此处应该使用{getFrameAtTime} 获取默认的第一个关键帧，手快写错
            Bitmap frame = retriever.getFrameAtTime();
            if (frame != null) {
                //压缩到200k以下，再存储到本地文件中
                byte[] bytes = compressBitmap(frame, 200);
                File file = new File(AppGlobals.getApplication().getCacheDir(), System.currentTimeMillis() + ".jpeg");

                FileIOUtils.writeFileFromBytesByStream(file, bytes, progress -> {
                    KLog.i("progress = " + progress);
                    if (progress == 1.0) {
                        liveData.postValue(file.getAbsolutePath());
                    }
                });
            } else {
                liveData.postValue(null);
            }
        });
        return liveData;
    }

    //循环压缩
    private static byte[] compressBitmap(Bitmap frame, int limit) {
        if (frame != null && limit > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            frame.compress(Bitmap.CompressFormat.JPEG, options, baos);
            while (baos.toByteArray().length > limit * 1024) {
                baos.reset();
                options -= 5;
                frame.compress(Bitmap.CompressFormat.JPEG, options, baos);
            }

            byte[] bytes = baos.toByteArray();
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos = null;
            }
            return bytes;
        }
        return null;
    }
}
