package com.jackson.jike.ui.detail;

import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jackson.common.extention.LiveDataBus;
import com.jackson.common.util.PixUtils;
import com.jackson.common.view.EmptyView;
import com.jackson.jike.R;
import com.jackson.jike.databinding.LayoutFeedDetailInteractionBinding;
import com.jackson.jike.databinding.LayoutFeedInteractionBinding;
import com.jackson.jike.model.Feed;
import com.jackson.jike.presenter.InteractionPresenter;
import com.jackson.jike.ui.home.FeedAdapter;

/**
 * Copyright (C), 2015-2020
 * FileName: ViewHandler
 * Author: Luo
 * Date: 2020/5/12 17:11
 * Description:
 */
public abstract class ViewHandler {

    protected FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailInteractionBinding mInteractionBinding;
    protected FeedCommonAdapter mListAdapter;
    private final FeedDetailViewModel viewModel;
    private EmptyView mEmptyView;
    private CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity) {
        mActivity = activity;
        viewModel = ViewModelProviders.of(activity).get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
        mInteractionBinding.setFeed(feed);
        mInteractionBinding.setLifeCycleOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        mListAdapter = new FeedCommonAdapter(mActivity);
        mRecyclerView.setAdapter(mListAdapter);

        viewModel.setItemId(mFeed.itemId);
        viewModel.getPageData().observe(mActivity, comments -> {
            mListAdapter.submitList(comments);
            handleEmptyView(comments.size() <= 0);
        });

        mInteractionBinding.inputView.setOnClickListener(v -> {
            if (commentDialog == null) {
                commentDialog = CommentDialog.newInstance(feed.itemId);
            }
            commentDialog.setCommentAddListener(comment -> {
                mListAdapter.addAndRefreshList(comment);
            });
            commentDialog.show(mActivity.getSupportFragmentManager(), "commentDialog");

        });

    }

    protected void handleEmptyView(boolean show) {
        if (show) {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = PixUtils.dp2px(40);
                mEmptyView.setLayoutParams(layoutParams);
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
            }
            mListAdapter.addHeaderView(mEmptyView);
        } else {
            if (mEmptyView != null) {
                mListAdapter.removeHeaderView(mEmptyView);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (commentDialog != null && commentDialog.isAdded()) {
            commentDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onBackPressed() {

    }

}
