package com.jackson.jike.ui.share;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jackson.common.util.PixUtils;
import com.jackson.common.util.ViewHelper;
import com.jackson.common.view.CornerFrameLayout;
import com.jackson.jike.R;
import com.jackson.jike.view.PPImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2015-2020
 * FileName: ShareDialog
 * Author: Luo
 * Date: 2020/5/9 10:40
 * Description:分享dialog
 */
public class ShareBottomSheetDialog extends BottomSheetDialogFragment {

    private List<ResolveInfo> shareItems = new ArrayList<>();
    private ShareAdapter adapter;
    private String shareContent;
    private View.OnClickListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        CornerFrameLayout layout = new CornerFrameLayout(getContext());
        layout.setBackgroundColor(Color.WHITE);
        layout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP);

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new ShareAdapter();
        recyclerView.setAdapter(adapter);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = PixUtils.dp2px(20);

        params.leftMargin = params.topMargin = params.rightMargin = params.bottomMargin = margin;
        params.gravity = Gravity.CENTER;
        layout.addView(recyclerView, params);

        dialog.setContentView(layout);
//        ViewGroup parent = (ViewGroup) layout.getParent();
//        BottomSheetBehavior<ViewGroup> behavior = BottomSheetBehavior.from(parent);
//        behavior.setPeekHeight(PixUtils.getScreenHeight() / 3);
//        behavior.setHideable(false);
//
//        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
//        layoutParams.height = PixUtils.getScreenHeight() / 3 * 2;
//        parent.setLayoutParams(layoutParams);

        queryShareItems();
        return dialog;
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

    private void queryShareItems() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");


        List<ResolveInfo> resolveInfos = getContext().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName, "com.tencent.mm") || TextUtils.equals(packageName, "com.tencent.mobileqq")) {
                shareItems.add(resolveInfo);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public void setShareItemClickListener(View.OnClickListener listener) {
        mListener = listener;
    }


    private class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final PackageManager packageManager;

        public ShareAdapter() {
            packageManager = getContext().getPackageManager();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_item, parent, false);
            return new RecyclerView.ViewHolder(inflate) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ResolveInfo resolveInfo = shareItems.get(position);
            PPImageView imageView = holder.itemView.findViewById(R.id.share_icon);
            Drawable drawable = resolveInfo.loadIcon(packageManager);
            imageView.setImageDrawable(drawable);

            TextView shareText = holder.itemView.findViewById(R.id.share_text);
            shareText.setText(resolveInfo.loadLabel(packageManager));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pkg = resolveInfo.activityInfo.packageName;
                    String cls = resolveInfo.activityInfo.name;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setComponent(new ComponentName(pkg, cls));
                    intent.putExtra(Intent.EXTRA_TEXT, shareContent);

                    getContext().startActivity(intent);

                    if (mListener != null) {
                        mListener.onClick(v);
                    }

                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return shareItems == null ? 0 : shareItems.size();
        }
    }
}
