package com.jackson.jike.ui.share;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
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
public class ShareDialog1 extends BottomSheetDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CornerFrameLayout frameLayout = new CornerFrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.WHITE);
        return frameLayout;
    }
}
