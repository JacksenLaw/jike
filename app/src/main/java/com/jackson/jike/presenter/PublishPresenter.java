package com.jackson.jike.presenter;

import androidx.lifecycle.LifecycleOwner;

import com.alibaba.fastjson.JSONObject;
import com.jackson.common.AppGlobals;
import com.jackson.common.util.CommonUtil;
import com.jackson.jike.R;
import com.jackson.jike.model.Comment;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;
import com.jackson.network.JsonCallback;

/**
 * Copyright (C), 2015-2020
 * FileName: PublishPresenter
 * Author: Luo
 * Date: 2020/6/18 16:28
 * Description:
 */
public class PublishPresenter extends BasePresenter {

    /**
     * 发布一条评论
     *
     * @param comment  内容
     * @param itemId   帖子id
     * @param coverUrl 缩略图路径
     * @param fileUrl  文件路径
     * @param width    宽
     * @param height   高
     */
    public static void publishComment(JsonCallback<Comment> callback, LifecycleOwner owner, String comment, long itemId, String coverUrl, String fileUrl, int width, int height) {
        if (!isLogin(owner, user -> publishComment(callback, comment, itemId, coverUrl, fileUrl, width, height))) {
        } else {
            publishComment(callback, comment, itemId, coverUrl, fileUrl, width, height);
        }

    }

    private static void publishComment(JsonCallback<Comment> callback, String comment, long itemId, String coverUrl, String fileUrl, int width, int height) {
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", itemId)
                .addParam("commentText", comment)
                .addParam("image_url", coverUrl)
                .addParam("video_url", fileUrl)
                .addParam("width", width)
                .addParam("height", height)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        CommonUtil.showToast("评论失败:" + response.message);
                        callback.onError(response);
                    }
                });
    }

    /**
     * 发布一条帖子
     */
    public static void publishFeed(JsonCallback<JSONObject> callback, String coverUploadUrl, String fileUploadUrl, int width, int height, long tagId, String tagTitle, String feedText, int feedType) {
        ApiService.post("/feeds/publish")
                .addParam("coverUrl", coverUploadUrl)
                .addParam("fileUrl", fileUploadUrl)
                .addParam("fileWidth", width)
                .addParam("fileHeight", height)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", tagId)
                .addParam("tagTitle", tagTitle)
                .addParam("feedText", feedText)
                .addParam("feedType", feedType)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(AppGlobals.getApplication().getString(R.string.publish_success));
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                        callback.onError(response);
                    }
                });
    }

}
