package com.jackson.jike.ui.publish;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jackson.common.util.PixUtils;
import com.jackson.jike.R;
import com.jackson.jike.model.TagList;
import com.jackson.jike.presenter.InteractionPresenter;
import com.jackson.network.ApiResponse;
import com.jackson.network.JsonCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: TagBottomSheetFragment
 * Author: Luo
 * Date: 2020/5/29 14:45
 * Description:
 */
public class TagBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RecyclerView recyclerView;
    private TagsAdapter tagsAdapter;
    private List<TagList> mTagLists = new ArrayList<>();
    private OnTagItemSelectedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_bottom_sheet_dialog, null, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tagsAdapter = new TagsAdapter();
        recyclerView.setAdapter(tagsAdapter);

        dialog.setContentView(view);
        ViewGroup parent = (ViewGroup) view.getParent();
        BottomSheetBehavior<ViewGroup> behavior = BottomSheetBehavior.from(parent);
        behavior.setPeekHeight(PixUtils.getScreenHeight() / 3);
        behavior.setHideable(false);


        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
        layoutParams.height = PixUtils.getScreenHeight() / 3 * 2;
        parent.setLayoutParams(layoutParams);

        queryTagList();
        return dialog;
    }

    private void queryTagList() {
        InteractionPresenter.queryTagList(new JsonCallback<List<TagList>>() {
            @Override
            public void onSuccess(ApiResponse<List<TagList>> response) {
                if (response.body != null) {
                    List<TagList> list = response.body;
                    mTagLists.addAll(list);
                    tagsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    class TagsAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(13);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_000));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(new RecyclerView.LayoutParams(-1, PixUtils.dp2px(45)));

            return new RecyclerView.ViewHolder(textView) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            TagList tagList = mTagLists.get(position);
            textView.setText(tagList.title);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTagItemSelected(tagList);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTagLists.size();
        }
    }

    public void setOnTagItemSelectedListener(OnTagItemSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnTagItemSelectedListener {
        void onTagItemSelected(TagList tagList);
    }
}
