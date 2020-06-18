package com.jackson.jike.presenter;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSONObject;
import com.jackson.common.extention.LiveDataBus;
import com.jackson.common.util.CommonUtil;
import com.jackson.jike.model.Comment;
import com.jackson.jike.model.Feed;
import com.jackson.jike.model.TagList;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.jike.ui.share.ShareBottomSheetDialog;
import com.jackson.jike.ui.share.ShareDialog;
import com.jackson.network.ApiResponse;
import com.jackson.network.ApiService;
import com.jackson.network.JsonCallback;
import com.jackson.network.schedulers.Scheduler;

import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: AbsListFragment
 * Author: Luo
 * Date: 2020/5/09 9:35
 * Description: 网络请求
 */
public class InteractionPresenter extends BasePresenter {

    public static final String DATA_FROM_INTERACTION = "data_from_interaction";

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_SHARE = "/ugc/increaseShareCount";

    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    /**
     * 给一个帖子点赞/取消点赞，它和给帖子点踩一踩是互斥的
     */
    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, user -> toggleFeedLike(feed))) {
        } else {
            toggleFeedLike(feed);
        }
    }

    private static void toggleFeedLike(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKE)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked");
                            feed.getUgc().setHasLiked(hasLiked);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 给一个帖子踩一踩
     */
    public static void toggleFeedDiss(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, user -> toggleFeedDiss(feed))) {
        } else {
            toggleFeedDiss(feed);
        }
    }

    private static void toggleFeedDiss(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked");
                            feed.getUgc().setHasdiss(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 分享
     */
    public static void openShare(Context context, Feed feed) {
        String shareContent = feed.feeds_text;
        if (!TextUtils.isEmpty(feed.url)) {
            shareContent = feed.url;
        } else if (!TextUtils.isEmpty(feed.cover)) {
            shareContent = feed.cover;
        }
//        ShareBottomSheetDialog shareDialog = new ShareBottomSheetDialog();
        ShareDialog shareDialog = new ShareDialog(context);
        shareDialog.setShareContent(shareContent);
        shareDialog.setShareItemClickListener(v ->
                ApiService.get(URL_SHARE)
                        .addParam("itemId", feed.itemId)
                        .execute(new JsonCallback<JSONObject>() {
                            @Override
                            public void onSuccess(ApiResponse<JSONObject> response) {
                                if (response.body != null) {
                                    int count = response.body.getIntValue("count");
                                    feed.getUgc().setShareCount(count);
                                }
                            }

                            @Override
                            public void onError(ApiResponse<JSONObject> response) {
                                CommonUtil.showToast(response.message);
                            }
                        }));

        shareDialog.show();
//        shareDialog.show(((FragmentActivity)context).getSupportFragmentManager(),"share");
    }

    /**
     * 给一个帖子的评论点赞/取消点赞
     */
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (!isLogin(owner, user -> toggleCommentLike(comment))) {
        } else {
            toggleCommentLike(comment);
        }
    }

    private static void toggleCommentLike(Comment comment) {

        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 收藏帖子
     */
    public static void toggleFeedFavourite(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, user -> toggleFeedFavorite(feed))) {
        } else {
            toggleFeedFavorite(feed);
        }
    }

    private static void toggleFeedFavorite(Feed feed) {
        ApiService.get("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 关注/取消关注一个用户
     */
    public static void toggleFollowUser(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, user -> toggleFollowUser(feed))) {
        } else {
            toggleFollowUser(feed);
        }
    }

    private static void toggleFollowUser(Feed feed) {
        ApiService.get("/ugc/toggleUserFollow")
                .addParam("followUserId", UserManager.get().getUserId())
                .addParam("userId", feed.author.userId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFollow = response.body.getBooleanValue("hasLiked");
                            feed.getAuthor().setHasFollow(hasFollow);
                            LiveDataBus.get().with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 删除一个帖子
     *
     * @param itemId 帖子id
     */
    public static LiveData<Boolean> deleteFeed(Context context, long itemId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", (dialog, which) -> {
                    dialog.dismiss();
                    deleteFeedInternal(liveData, itemId);
                }).setPositiveButton("取消", (dialog, which) -> dialog.dismiss()).setMessage("确定要删除这条帖子吗？").create().show();
        return liveData;
    }

    private static void deleteFeedInternal(MutableLiveData<Boolean> liveData, long itemId) {
        ApiService.get("/feeds/deleteFeed")
                .addParam("itemId", itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean success = response.body.getBoolean("result");
                            liveData.postValue(success);
                            CommonUtil.showToast("删除成功");
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 删除某个帖子的一个评论
     *
     * @param itemId    帖子id
     * @param commentId 评论id
     */
    public static LiveData<Boolean> deleteFeedComment(Context context, long itemId, long commentId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", (dialog, which) -> {
                    dialog.dismiss();
                    deleteFeedCommentInternal(liveData, itemId, commentId);
                }).setPositiveButton("取消", (dialog, which) -> dialog.dismiss()).setMessage("确定要删除这条评论吗？").create().show();
        return liveData;
    }

    private static void deleteFeedCommentInternal(LiveData liveData, long itemId, long commentId) {
        ApiService.get("/comment/deleteComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("commentId", commentId)
                .addParam("itemId", itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean result = response.body.getBooleanValue("result");
                            ((MutableLiveData) liveData).postValue(result);
                            CommonUtil.showToast("评论已删除");
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

    /**
     * 获取发布页面话题列表数据
     */
    public static void queryTagList(JsonCallback<List<TagList>> callback) {
        ApiService.get("/tag/queryTagList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", 100)
                .addParam("tagId", 0)
                .observerOn(Scheduler.mainThread())
                .execute(new JsonCallback<List<TagList>>() {
                    @Override
                    public void onSuccess(ApiResponse<List<TagList>> response) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(ApiResponse<List<TagList>> response) {
                        CommonUtil.showToast(response.message);
                        callback.onError(response);
                    }
                });
    }

    /**
     * 关注/取消关注一个帖子标签
     */
    public static void toggleTagLike(LifecycleOwner owner, TagList tagList) {
        if (!isLogin(owner, user -> toggleTagLikeInternal(tagList))) {
        } else {
            toggleTagLikeInternal(tagList);
        }
    }

    private static void toggleTagLikeInternal(TagList tagList) {
        ApiService.get("/tag/toggleTagFollow")
                .addParam("tagId", tagList.tagId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            Boolean follow = response.body.getBoolean("hasFollow");
                            tagList.setHasFollow(follow);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        CommonUtil.showToast(response.message);
                    }
                });
    }

}
