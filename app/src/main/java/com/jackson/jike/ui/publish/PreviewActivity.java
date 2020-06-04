package com.jackson.jike.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.KLog;
import com.jackson.jike.R;
import com.jackson.jike.databinding.ActivityLayoutPreviewBinding;

import java.io.File;

/**
 * Copyright (C), 2015-2020
 * FileName: PreviewActivity
 * Author: Luo
 * Date: 2020/5/14 17:09
 * Description:
 */
public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLayoutPreviewBinding mPreviewBinding;
    public static final String KEY_PREVIEW_URL = "preview_url";
    public static final String KEY_PREVIEW_VIDEO = "preview_video";
    public static final String KEY_PREVIEW_BTNTEXT = "preview_btntext";
    public static final int REQ_PREVIEW = 1000;
    private SimpleExoPlayer player;

    public static void startActivityForResult(Activity activity, String previewUrl, boolean isVideo, String btnText) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL, previewUrl);
        intent.putExtra(KEY_PREVIEW_VIDEO, isVideo);
        intent.putExtra(KEY_PREVIEW_BTNTEXT, btnText);
        activity.startActivityForResult(intent, REQ_PREVIEW);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_preview);
        String previewUrl = getIntent().getStringExtra(KEY_PREVIEW_URL);
        if (TextUtils.isEmpty(previewUrl)) previewUrl = "";
        boolean isVideo = getIntent().getBooleanExtra(KEY_PREVIEW_VIDEO, false);
        String btnText = getIntent().getStringExtra(KEY_PREVIEW_BTNTEXT);

        if (TextUtils.isEmpty(btnText)) {
            mPreviewBinding.actionOk.setVisibility(View.GONE);
        } else {
            mPreviewBinding.actionOk.setVisibility(View.VISIBLE);
            mPreviewBinding.actionOk.setText(btnText);
            mPreviewBinding.actionOk.setOnClickListener(this);
        }

        mPreviewBinding.actionClose.setOnClickListener(this);

        if (isVideo) {
            previewVideo(previewUrl);
        } else {
            previewImage(previewUrl);
        }
    }

    private void previewImage(String previewUrl) {
        mPreviewBinding.preView.setVisibility(View.VISIBLE);
        Glide.with(this).load(previewUrl).into(new ImageViewTarget<Drawable>(mPreviewBinding.preView) {
            @Override
            protected void setResource(@Nullable Drawable resource) {
                mPreviewBinding.preView.setImageDrawable(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                CommonUtil.showToast(getString(R.string.view_error));
                mPreviewBinding.preView.postDelayed(() -> finish(), 500);
            }
        });
    }

    private void previewVideo(String previewUrl) {
        mPreviewBinding.playerView.setVisibility(View.VISIBLE);
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());

        Uri uri = null;
        File file = new File(previewUrl);
        if (file.exists()) {
            DataSpec dataSpec = new DataSpec(Uri.fromFile(file));
            FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri = fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        } else {
            uri = Uri.parse(previewUrl);
        }

        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this, Util.getUserAgent(this, getPackageName())));
        ProgressiveMediaSource mediaSource = factory.createMediaSource(uri);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
        mPreviewBinding.playerView.setPlayer(player);
        mPreviewBinding.playerView.getPlayer().addListener(new Player.EventListener() {
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                KLog.e("播放失败： " + error.getMessage());
                CommonUtil.showToast(getString(R.string.play_error));
                finish();
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop(true);
            player.release();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_close) {
            finish();
        } else if (v.getId() == R.id.action_ok) {
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }
}
