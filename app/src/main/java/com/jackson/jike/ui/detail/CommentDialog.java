package com.jackson.jike.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.blankj.utilcode.util.KeyboardUtils;
import com.jackson.common.AppGlobals;
import com.jackson.common.dialog.LoadingDialog;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.FileUploadManager;
import com.jackson.common.util.FileUtils;
import com.jackson.common.util.PixUtils;
import com.jackson.common.util.ViewHelper;
import com.jackson.jike.R;
import com.jackson.jike.databinding.LayoutCommentDialogBinding;
import com.jackson.jike.model.Comment;
import com.jackson.jike.presenter.PublishPresenter;
import com.jackson.jike.ui.publish.CaptureActivity;
import com.jackson.network.ApiResponse;
import com.jackson.network.JsonCallback;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C), 2015-2020
 * FileName: CommentDialog
 * Author: Luo
 * Date: 2020/5/14 11:00
 * Description: 评论对话框
 */
public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private LayoutCommentDialogBinding mBinding;
    private long itemId;
    private commentAddListener mListener;
    private static final String KEY_ITEM_ID = "key_item_id";
    private String filePath;
    private int width, height;
    private boolean isVideo;
    private String coverUrl;
    private String fileUrl;
    private LoadingDialog loadingDialog;
    @SuppressLint("RestrictedApi")
    private Executor ioExecutor = ArchTaskExecutor.getIOThreadExecutor();
    @SuppressLint("RestrictedApi")
    private Executor mainExecutor = ArchTaskExecutor.getMainThreadExecutor();
    private Context mContext = AppGlobals.getApplication();

    public static CommentDialog newInstance(long itemId) {

        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        //得到dialog对应的window
        Window window = getDialog().getWindow();
        //可以去除自带margin
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //得到LayoutParams
            WindowManager.LayoutParams params = window.getAttributes();

            //修改gravity
            params.gravity = Gravity.BOTTOM;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutCommentDialogBinding.inflate(inflater, container, false);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        if (getArguments() != null) {
            this.itemId = getArguments().getLong(KEY_ITEM_ID);
        }

        ViewHelper.setViewOutLine(mBinding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);

        mBinding.getRoot().postDelayed(() -> KeyboardUtils.showSoftInput(mBinding.inputView), 200);

        dismissWhenPressBack();
        return mBinding.getRoot();
    }

    private void dismissWhenPressBack() {
        mBinding.inputView.setOnBackKeyEventListener(() -> {
            mBinding.inputView.postDelayed(this::dismiss, 200);
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.comment_send) {
            publishComment();
        } else if (v.getId() == R.id.comment_video) {
            CaptureActivity.startActivityForResult(getActivity());
        } else if (v.getId() == R.id.comment_delete) {
            filePath = null;
            isVideo = false;
            width = 0;
            height = 0;
            mBinding.commentCover.setImageDrawable(null);
            mBinding.commentExtLayout.setVisibility(View.GONE);

            mBinding.commentVideo.setEnabled(true);
            mBinding.commentVideo.setImageAlpha(255);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
                width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
                height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
                isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);
            }

            if (!TextUtils.isEmpty(filePath)) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUrl(filePath);
                if (isVideo) {
                    mBinding.commentVideo.setVisibility(View.VISIBLE);
                }
            }

            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setImageAlpha(80);
        }
    }

    private void publishComment() {

        if (TextUtils.isEmpty(mBinding.inputView.getText())) {
            return;
        }

        if (isVideo && !TextUtils.isEmpty(filePath)) {
            FileUtils.generateVideoCover(filePath).observe(this, coverPath ->
                    uploadFile(coverPath, filePath)
            );
        } else if (!TextUtils.isEmpty(filePath)) {
            uploadFile(null, filePath);
        } else {
            publish();
        }
    }

    private void uploadFile(String coverPath, String filePath) {
//        AtomicInteger, CountDownLatch, CyclicBarrier
        showLoadingDialog();
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ioExecutor.execute(() -> {
                int remain = count.decrementAndGet();
                coverUrl = FileUploadManager.upload(coverPath);
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        CommonUtil.showToast(getString(R.string.file_upload_failed));
                    }
                }
            });
        }
        ioExecutor.execute(() -> {
            int remain = count.decrementAndGet();
            fileUrl = FileUploadManager.upload(filePath);
            if (remain <= 0) {
                if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                    publish();
                } else {
                    dismissLoadingDialog();
                    CommonUtil.showToast(getString(R.string.file_upload_failed));
                }
            }
        });

    }

    private void publish() {
        String commentText = mBinding.inputView.getText().toString();
        PublishPresenter.publishComment(new JsonCallback<Comment>() {
            @Override
            public void onSuccess(ApiResponse<Comment> response) {
                super.onSuccess(response);
                onCommentSuccess(response.body);
                dismissLoadingDialog();
                CommonUtil.showToast(getString(R.string.comment_add_success));
            }

            @Override
            public void onError(ApiResponse<Comment> response) {
                super.onError(response);
                dismissLoadingDialog();
                CommonUtil.showToast(getString(R.string.comment_add_failed));
            }
        }, this, commentText, itemId, isVideo ? coverUrl : fileUrl, isVideo ? fileUrl : null, width, height);
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
            loadingDialog.setCancelable(false);
            loadingDialog.setLoadingText(getString(R.string.publishing));
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setCancelable(false);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) {
            //dismissLoadingDialog  的调用可能会出现在异步线程调用
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mainExecutor.execute(() -> {
                    if (loadingDialog != null && loadingDialog.isShowing()) {
                        loadingDialog.dismiss();
                    }
                });
            } else if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

    private void onCommentSuccess(Comment body) {
        mainExecutor.execute(() -> {
            if (mListener != null) {
                mListener.onAddComment(body);
            }
            dismiss();
        });
    }

    @Override
    public void dismiss() {
        KeyboardUtils.hideSoftInput(mBinding.inputView);
        dismissLoadingDialog();
        mBinding.inputView.setText("");
        filePath = null;
        fileUrl = null;
        coverUrl = null;
        isVideo = false;
        width = 0;
        height = 0;
        super.dismiss();
    }

    public interface commentAddListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(commentAddListener listener) {
        mListener = listener;
    }
}
