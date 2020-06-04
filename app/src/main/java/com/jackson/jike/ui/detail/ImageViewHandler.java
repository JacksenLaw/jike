package com.jackson.jike.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.jackson.jike.R;
import com.jackson.jike.databinding.LayoutFeedDetailTypeImageBinding;
import com.jackson.jike.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.jackson.jike.model.Feed;
import com.jackson.jike.view.PPImageView;

/**
 * Copyright (C), 2015-2020
 * FileName: ImageViewHandler
 * Author: Luo
 * Date: 2020/5/12 17:12
 * Description:
 */
public class ImageViewHandler extends ViewHandler {

    private LayoutFeedDetailTypeImageBinding mImageBinding;
    private LayoutFeedDetailTypeImageHeaderBinding mHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);
        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_image);
        mRecyclerView = mImageBinding.recyclerView;
        mInteractionBinding = mImageBinding.bottomInteractionLayout;
        mImageBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mImageBinding.setFeed(feed);

        mHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        mHeaderBinding.setFeed(feed);

        PPImageView headerImage = mHeaderBinding.headerImage;
        headerImage.bindData(mFeed.width, mFeed.height, mFeed.width > mFeed.height ? 0 : 16, mFeed.cover);
        mListAdapter.addHeaderView(mHeaderBinding.getRoot());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean visible = mHeaderBinding.getRoot().getTop() <= -mImageBinding.titleLayout.getMeasuredHeight();
                mImageBinding.authorInfoLayout.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
                mImageBinding.title.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });

        handleEmptyView(true);

    }
}
