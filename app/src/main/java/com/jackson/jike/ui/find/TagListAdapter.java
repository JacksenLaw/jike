package com.jackson.jike.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jackson.common.extention.AbsPagedListAdapter;
import com.jackson.jike.databinding.LayoutTagListItemBinding;
import com.jackson.jike.model.TagList;

/**
 * Copyright (C), 2015-2020
 * FileName: TagListAdapter
 * Author: Luo
 * Date: 2020/6/1 16:52
 * Description:
 */
public class TagListAdapter extends AbsPagedListAdapter<TagList, TagListAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId == newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding itemBinding = LayoutTagListItemBinding.inflate(mInflater, parent, false);

        return new ViewHolder(itemBinding.getRoot(), itemBinding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        TagList tagList = getItem(position);
        holder.bindData(tagList);
        holder.mItemBinding.setLifeCycleOwner((LifecycleOwner) mContext);
//        holder.mItemBinding.actionFollow.setOnClickListener(v -> InteractionPresenter.toggleTagLike((LifecycleOwner) mContext, tagList));
        holder.itemView.setOnClickListener(v -> TagFeedListActivity.startActivity(mContext, tagList));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutTagListItemBinding mItemBinding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding itemBinding) {
            super(itemView);
            mItemBinding = itemBinding;
        }

        public void bindData(TagList item) {
            mItemBinding.setTagList(item);
        }
    }
}
