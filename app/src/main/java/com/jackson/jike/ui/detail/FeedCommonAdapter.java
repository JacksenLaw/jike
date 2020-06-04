package com.jackson.jike.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jackson.common.extention.AbsPagedListAdapter;
import com.jackson.common.util.PixUtils;
import com.jackson.jike.databinding.LayoutFeedCommonListItemBinding;
import com.jackson.jike.model.Comment;
import com.jackson.jike.presenter.InteractionPresenter;
import com.jackson.jike.ui.MutableItemKeyedDataSource;
import com.jackson.jike.ui.login.UserManager;
import com.jackson.jike.ui.publish.PreviewActivity;

/**
 * Copyright (C), 2015-2020
 * FileName: FeedCommonAdapter
 * Author: Luo
 * Date: 2020/5/12 17:16
 * Description:
 */
public class FeedCommonAdapter extends AbsPagedListAdapter<Comment, FeedCommonAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context mContext;

    protected FeedCommonAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommonListItemBinding binding = LayoutFeedCommonListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        holder.mBinding.commentDelete.setOnClickListener(v -> {
            InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.commentId)
                    .observe((LifecycleOwner) mContext, aBoolean -> {
                        if (aBoolean) {
                            deleteAndRefreshList(item);
                        }
                    });
        });
        holder.mBinding.commentCover.setOnClickListener(v -> {
            boolean isVideo = item.commentType == Comment.COMMENT_TYPE_VIDEO;
            PreviewActivity.startActivityForResult((Activity) mContext, isVideo ? item.videoUrl : item.imageUrl, isVideo, null);
        });
    }

    /**
     * 从PageList中删除数据
     *
     * @param item
     */
    private void deleteAndRefreshList(Comment item) {
        //将评论对象从pageList中移除
        PagedList<Comment> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Comment> mutableItemKeyedDataSource
                = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Comment item) {
                return item.id;
            }
        };

        for (Comment comment : currentList) {
            if (comment != item) {
                mutableItemKeyedDataSource.data.add(comment);
            }
        }
        PagedList<Comment> pagedList = mutableItemKeyedDataSource.buildNewPagedList(getCurrentList().getConfig());
        submitList(pagedList);
    }

    /**
     * 往PageList中添加数据
     *
     * @param comment
     */
    public void addAndRefreshList(Comment comment) {
        //将新的评论对象添加到pageList中
        PagedList<Comment> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Comment> mutableItemKeyedDataSource
                = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Comment item) {
                return item.id;
            }
        };
        mutableItemKeyedDataSource.data.add(comment);
        mutableItemKeyedDataSource.data.addAll(currentList);
        PagedList<Comment> pagedList = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
        submitList(pagedList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutFeedCommonListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommonListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            boolean self = item.author != null && UserManager.get().getUserId() == item.author.userId;
            mBinding.authorLabel.setVisibility(self ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(self ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.imageUrl)) {
                mBinding.commentExt.setVisibility(View.VISIBLE);
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(item.width, item.height, 0, PixUtils.dp2px(200), PixUtils.dp2px(200), item.imageUrl);
                if (!TextUtils.isEmpty(item.videoUrl)) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
                mBinding.commentExt.setVisibility(View.GONE);
            }
        }
    }
}
