package com.jackson.jike.ui.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jackson.common.extention.AbsPagedListAdapter;
import com.jackson.common.extention.LiveDataBus;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.KLog;
import com.jackson.jike.R;
import com.jackson.jike.databinding.LayoutFeedTypeImageBinding;
import com.jackson.jike.databinding.LayoutFeedTypeVideoBinding;
import com.jackson.jike.model.Feed;
import com.jackson.jike.presenter.InteractionPresenter;
import com.jackson.jike.ui.detail.FeedDetailActivity;
import com.jackson.jike.ui.publish.PreviewActivity;
import com.jackson.jike.exoplayer.ListPlayerView;

/**
 * Copyright (C), 2015-2020
 * FileName: FeedAdapter
 * Author: Luo
 * Date: 2020/3/26 22:02
 * Description:
 */
public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    protected String mCategory;//区分页面
    protected Context mContext;

    protected FeedAdapter(Context context, String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            /**
             * 两个item是不是同一个
             * @param oldItem
             * @param newItem
             * @return
             */
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id == newItem.id;
            }

            /**
             * item中内容是否相同  复写Feed的equals方法
             * @param oldItem
             * @param newItem
             * @return
             */
            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCategory = category;
    }

    @Override
    public int getItemViewType2(int position) {
        Feed feed = getItem(position);
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            return R.layout.layout_feed_type_image;
        } else if (feed.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(mInflater, viewType, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        final Feed feed = getItem(position);
        KLog.i("feed = " + feed.toString());

        holder.bindData(feed);

        holder.itemView.setOnClickListener(v -> {
            openDetail(feed);
        });
    }

    /**
     * 通知HomeFragment在onPause时不结束播放视频
     *
     * @param feed
     */
    public void onStartFeedDetailActivity(Feed feed) {

    }

    private FeedObserver mFeedObserver;

    private static class FeedObserver implements Observer<Feed> {

        private Feed mFeed;

        @Override
        public void onChanged(Feed newOne) {
            if (mFeed.id != newOne.id)
                return;
            mFeed.author = newOne.author;
            mFeed.ugc = newOne.ugc;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) {
            mFeed = feed;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;
        public ImageView feedImage;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            //这里之所以手动绑定数据的原因是 图片 和视频区域都是需要计算的
            //而dataBinding的执行默认是延迟一帧的。
            //当列表上下滑动的时候 ，会明显的看到宽高尺寸不对称的问题

            //填充数据
            mBinding.setVariable(com.jackson.jike.BR.feed, item);
            mBinding.setVariable(com.jackson.jike.BR.lifeCycleOwner, mContext);
            KLog.i("Context = " + mContext);
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                //图片数据绑定
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
                feedImage = imageBinding.feedImage;

                ((LayoutFeedTypeImageBinding) mBinding).layoutHotComment.groupImage.setOnClickListener(v ->
                        PreviewActivity.startActivityForResult((Activity) mContext, item.topComment.imageUrl, false, null)
                );
                ((LayoutFeedTypeImageBinding) mBinding).layoutHotComment.getRoot().setOnClickListener(v ->
                        CommonUtil.showToast("这是热评")
                );
                ((LayoutFeedTypeImageBinding) mBinding).layoutAuthor.getRoot().setOnClickListener(v ->
                        CommonUtil.showToast("查看作者")
                );
                ((LayoutFeedTypeImageBinding) mBinding).layoutHotComment.groupAuthor.setOnClickListener(v ->
                        CommonUtil.showToast("查看热评作者")
                );
                ((LayoutFeedTypeImageBinding) mBinding).layoutInteraction.btnComment.setOnClickListener(v -> {
                    openDetail(item);
                });
            } else if (mBinding instanceof LayoutFeedTypeVideoBinding) {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                //图片数据绑定
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
                listPlayerView = videoBinding.listPlayerView;

                ((LayoutFeedTypeVideoBinding) mBinding).layoutHotComment.groupImage.setOnClickListener(v ->
                        PreviewActivity.startActivityForResult((Activity) mContext, item.topComment.videoUrl, true, null)
                );
                ((LayoutFeedTypeVideoBinding) mBinding).layoutHotComment.getRoot().setOnClickListener(v ->
                        CommonUtil.showToast("这是热评")
                );
                ((LayoutFeedTypeVideoBinding) mBinding).layoutAuthor.getRoot().setOnClickListener(v ->
                        CommonUtil.showToast("查看作者")
                );
                ((LayoutFeedTypeVideoBinding) mBinding).layoutHotComment.groupAuthor.setOnClickListener(v ->
                        CommonUtil.showToast("查看热评作者")
                );
                ((LayoutFeedTypeVideoBinding) mBinding).layoutInteraction.btnComment.setOnClickListener(v -> {
                    openDetail(item);
                });
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }

    }

    protected void openDetail(Feed item) {
        FeedDetailActivity.startFeedDetailActivity(mContext, item, mCategory);
        onStartFeedDetailActivity(item);
        if (mFeedObserver == null) {
            mFeedObserver = new FeedObserver();
            LiveDataBus.get()
                    .with(InteractionPresenter.DATA_FROM_INTERACTION)
                    .observe((LifecycleOwner) mContext, mFeedObserver);
        }
        mFeedObserver.setFeed(item);
    }

    @Override
    public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
        super.onCurrentListChanged(previousList, currentList);
        KLog.i("previousList = " + previousList);
        KLog.i("currentList = " + currentList);
    }
}
