package com.jackson.jike.presenter;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.jackson.common.AppGlobals;
import com.jackson.common.extention.LiveDataBus;
import com.jackson.common.util.CommonUtil;
import com.jackson.jike.R;
import com.jackson.jike.model.Comment;
import com.jackson.jike.model.Feed;
import com.jackson.jike.model.TagList;
import com.jackson.jike.model.User;
import com.jackson.jike.ui.login.UserManager;
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
public class InteractionPresenter {

    public static final String DATA_FROM_INTERACTION = "data_from_interaction";

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    private static final String URL_SHARE = "/ugc/increaseShareCount";

    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    private static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()) {
            return true;
        } else {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            if (owner != null) {
                liveData.observe(owner, observer);
            } else {
                liveData.observeForever(observer);
            }
        }
        return false;
    }

    /**
     * 给一个帖子点赞/取消点赞，它和给帖子点踩一踩是互斥的
     */
    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, user ->
                toggleFeedLikeInternal(feed))) {
        } else {
            toggleFeedLikeInternal(feed);
        }
    }

    private static void toggleFeedLikeInternal(Feed feed) {
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
        if (!isLogin(owner, user ->
                toggleFeedDissInternal(feed))) {
        } else {
            toggleFeedDissInternal(feed);
        }
    }

    private static void toggleFeedDissInternal(Feed feed) {
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
//        ShareDialog1 dialog1 = new ShareDialog1();
//        dialog1.show(((AppCompatActivity)context).getSupportFragmentManager(),"share");
    }

    /**
     * 给一个帖子的评论点赞/取消点赞
     *
     * @param owner
     * @param comment
     */
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleCommentLikeInternal(comment);
            }
        })) {
        } else {
            toggleCommentLikeInternal(comment);
        }
    }

    private static void toggleCommentLikeInternal(Comment comment) {

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
        if (!isLogin(owner, user ->
                toggleFeedFavorite(feed))) {
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
     *
     * @param owner
     * @param feed
     */
    public static void toggleFollowUser(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFollowUser(feed);
            }
        })) {
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
     * 发布一条评论
     *
     * @param comment  内容
     * @param itemId   帖子id
     * @param coverUrl
     * @param fileUrl
     * @param width    宽
     * @param height   高
     */
    public static void publishComment(JsonCallback<Comment> callback, LifecycleOwner owner, String comment, long itemId, String coverUrl, String fileUrl, int width, int height) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                publishComment(callback, comment, itemId, coverUrl, fileUrl, width, height);
            }
        })) {
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
     * 删除一个帖子
     *
     * @param context
     * @param itemId  帖子id
     * @return
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
     * @param context
     * @param itemId    帖子id
     * @param commentId 评论id
     * @return
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
     *
     * @param callback
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

    /**
     * 关注/取消关注一个帖子标签
     *
     * @param owner
     * @param tagList
     */
    public static void toggleTagLike(LifecycleOwner owner, TagList tagList) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleTagLikeInternal(tagList);
            }
        })) ;
        else {
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
