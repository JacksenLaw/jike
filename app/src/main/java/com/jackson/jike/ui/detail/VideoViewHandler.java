package com.jackson.jike.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.jackson.jike.R;
import com.jackson.jike.databinding.LayoutFeedDetailTypeVideoBinding;
import com.jackson.jike.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.jackson.jike.model.Feed;
import com.jackson.jike.exoplayer.FullScreenPlayerView;

/**
 * Copyright (C), 2015-2020
 * FileName: VideoViewHandler
 * Author: Luo
 * Date: 2020/5/12 17:13
 * Description:
 */
public class VideoViewHandler extends ViewHandler {

    private final LayoutFeedDetailTypeVideoBinding mVideoBinding;
    private String category;
    private String mCategory;
    private FullScreenPlayerView playerView;
    private final CoordinatorLayout coordinator;
    private boolean backPressed;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mVideoBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type_video);

        mInteractionBinding = mVideoBinding.bottomInteraction;
        mRecyclerView = mVideoBinding.recyclerView;
        playerView = mVideoBinding.playerView;
        coordinator = mVideoBinding.coordinator;
        View authorInfoView = mVideoBinding.authorInfo.getRoot();

        CoordinatorLayout.LayoutParams authorInfoViewLayoutParams = (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        authorInfoViewLayoutParams.setBehavior(new ViewAnchorBehavior(R.id.player_view));
        mVideoBinding.actionClose.setOnClickListener(v -> mActivity.finish());

        CoordinatorLayout.LayoutParams playerViewLayoutParams = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) playerViewLayoutParams.getBehavior();
        behavior.setViewZoomCallback(height -> {
            int bottom = playerView.getBottom();
            boolean moveUp = height < bottom;
            boolean fullscreen = moveUp ? height >= coordinator.getBottom() - mInteractionBinding.getRoot().getHeight()
                    : height >= coordinator.getBottom();
            setViewAppearance(fullscreen);
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);

        mCategory = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mVideoBinding.playerView.bindData(mCategory, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        //这里需要延迟一帧 等待布局完成，再来拿playerView的bottom值 和 coordinator的bottom值
        //做个比较。来校验是否进入详情页时时视频在全屏播放
        playerView.post(() -> {
            boolean fullscreen = playerView.getBottom() >= coordinator.getBottom();
            setViewAppearance(fullscreen);
        });

        //给headerView、 绑定数据并添加到列表之上
        LayoutFeedDetailTypeVideoHeaderBinding headerBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(
                LayoutInflater.from(mActivity),
                mRecyclerView,
                false);

        headerBinding.setFeed(mFeed);
        mListAdapter.addHeaderView(headerBinding.getRoot());

    }

    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mInteractionBinding.setFullscreen(fullscreen);
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);

        //底部互动区域的高度
        int inputHeight = mInteractionBinding.getRoot().getMeasuredHeight();
        //播放控制器的高度
        int ctrlViewHeight = playerView.getPlayController().getMeasuredHeight();
        //播放控制器的bottom值
        int bottom = playerView.getPlayController().getBottom();

        //全屏播放时，播放控制器需要处在底部互动区域的上面
        playerView.getPlayController().setY(fullscreen ? bottom - inputHeight - ctrlViewHeight
                : bottom - ctrlViewHeight);
        mInteractionBinding.inputView.setBackgroundResource(fullscreen ? R.drawable.bg_edit_view2 : R.drawable.bg_edit_view);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        //按了返回键后需要 恢复 播放控制器的位置。否则回到列表页时 可能会不正确的显示
        playerView.getPlayController().setTranslationY(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            playerView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        playerView.onActive();
    }
}
