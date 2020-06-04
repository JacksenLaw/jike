package com.jackson.jike.ui.publish;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.alibaba.fastjson.JSONObject;
import com.jackson.common.dialog.LoadingDialog;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.FileUtils;
import com.jackson.common.util.StatusBar;
import com.jackson.jike.R;
import com.jackson.jike.databinding.ActivityPublishBinding;
import com.jackson.jike.model.Feed;
import com.jackson.jike.model.TagList;
import com.jackson.jike.presenter.InteractionPresenter;
import com.jackson.nav_annotation.ActivityDestination;
import com.jackson.network.ApiResponse;
import com.jackson.network.JsonCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity {

    private ActivityPublishBinding mBinding;
    private int width, height;
    private String filePath, coverFilePath;
    private boolean isVideo;
    private UUID coverUploadUUID;//封面图片uuid
    private UUID fileUploadUUID;  //文件uuid
    private String coverUploadUrl, fileUploadUrl;
    private TagList mTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_publish);

        mBinding.actionClose.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.publish_exit_message))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).create().show();
        });

        mBinding.actionPublish.setOnClickListener(v -> {
            publish();
        });

        mBinding.actionDeleteFile.setOnClickListener(v -> {
            mBinding.actionAddFile.setVisibility(View.VISIBLE);
            mBinding.fileContainer.setVisibility(View.GONE);
            mBinding.cover.setImageDrawable(null);
            filePath = null;
            width = 0;
            height = 0;
            isVideo = false;
        });

        mBinding.actionAddFile.setOnClickListener(v -> {
            CaptureActivity.startActivityForResult(this);
        });

        mBinding.actionAddTag.setOnClickListener(v -> {
            TagBottomSheetDialogFragment fragment = new TagBottomSheetDialogFragment();
            fragment.setOnTagItemSelectedListener(tagList -> {
                mTagList = tagList;
                mBinding.actionAddTag.setText(tagList.title);
            });
            fragment.show(getSupportFragmentManager(), "tag_dialog");
        });
    }

    private LoadingDialog mLoadingDialog = null;

    private void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setLoadingText(getString(R.string.publishing));
        }
        mLoadingDialog.show();
    }

    private void dismissLoading() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } else {
            runOnUiThread(() -> {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            });
        }
    }

    private void publish() {

        if (TextUtils.isEmpty(filePath)) {
            if (TextUtils.isEmpty(mBinding.inputView.getText().toString())) {
                CommonUtil.showToast(getString(R.string.publish_say_something));
                return;
            }
            showLoading();
            publishFeed();
            return;
        }
        showLoading();
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (isVideo) {
            //截取视频封面
            FileUtils.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    coverFilePath = s;

                    //上传封面文件
                    OneTimeWorkRequest request = getOneTimeWorkRequest(coverFilePath);
                    coverUploadUUID = request.getId();

                    workRequests.add(request);
                    enqueue(workRequests);
                }
            });
        }
        //上传原始文件
        OneTimeWorkRequest request = getOneTimeWorkRequest(filePath);
        fileUploadUUID = request.getId();
        workRequests.add(request);
        //如果是视频文件则需要等待封面文件生成完毕后再一同提交到任务队列
        //否则 可以直接提交了
        if (!isVideo) {
            enqueue(workRequests);
        }

    }

    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation continuation = WorkManager.getInstance(this).beginWith(workRequests);
        continuation.enqueue();

        //观察每个任务的状态
        continuation.getWorkInfosLiveData().observe(this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                //状态：block running enuqued failed success finish
                int completedCount = 0;//标记有几个任务完成
                int failedCount = 0;
                for (WorkInfo workInfo : workInfos) {
                    //获取任务状态
                    WorkInfo.State state = workInfo.getState();
                    //获取任务输出结果
                    Data outputData = workInfo.getOutputData();
                    //获取任务id
                    UUID id = workInfo.getId();
                    if (state == WorkInfo.State.FAILED) {
                        if (id.equals(coverUploadUUID)) {
                            CommonUtil.showToast(getString(R.string.file_upload_cover_message));
                        } else if (id.equals(fileUploadUUID)) {
                            CommonUtil.showToast(getString(R.string.file_upload_original_message));
                        }
                        failedCount++;
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String fileUrl = outputData.getString("fileUrl");
                        if (id.equals(coverUploadUUID)) {
                            coverUploadUrl = fileUrl;
                        } else if (id.equals(fileUploadUUID)) {
                            fileUploadUrl = fileUrl;
                        }
                        completedCount++;
                    }


                }

                if (completedCount >= workInfos.size()) {
                    //发布帖子
                    publishFeed();
                } else if (failedCount > 0) {
                    dismissLoading();
                }
            }
        });
    }

    private void publishFeed() {
        InteractionPresenter.publishFeed(
                new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        finish();
                        dismissLoading();
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        dismissLoading();
                    }
                }, coverUploadUrl, fileUploadUrl, width, height, mTagList == null ? 0 : mTagList.tagId, mTagList == null ? "" : mTagList.title, mBinding.inputView.getText().toString(), isVideo ? Feed.TYPE_VIDEO : Feed.TYPE_IMAGE_TEXT);
    }

    private OneTimeWorkRequest getOneTimeWorkRequest(String filePath) {
        Data inputData = new Data.Builder()
                .putString("file", filePath)
                .build();

//        @SuppressLint("RestrictedApi")
//        Constraints constraints = new Constraints();
//        //设备存储空间充足的时候 才能执行 ,>15%
//        constraints.setRequiresStorageNotLow(true);
//        //必须在执行的网络条件下才能好执行,不计流量 ,wifi
//        constraints.setRequiredNetworkType(NetworkType.UNMETERED);
//        //设备的充电量充足的才能执行 >15%
//        constraints.setRequiresBatteryNotLow(true);
//        //只有设备在充电的情况下 才能允许执行
//        constraints.setRequiresCharging(true);
//        //只有设备在空闲的情况下才能被执行 比如息屏，cpu利用率不高
//        constraints.setRequiresDeviceIdle(true);
//        //workmanager利用contentObserver监控传递进来的这个uri对应的内容是否发生变化,当且仅当它发生变化了
//        //我们的任务才会被触发执行，以下三个api是关联的
//        constraints.setContentUriTriggers(null);
//        //设置从content变化到被执行中间的延迟时间，如果在这期间。content发生了变化，延迟时间会被重新计算
        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerContentUpdateDelay(0);
//        //设置从content变化到被执行中间的最大延迟时间
        //这个content就是指 我们设置的setContentUriTriggers uri对应的内容
//        constraints.setTriggerMaxContentDelay(0);
        OneTimeWorkRequest request = new OneTimeWorkRequest
                .Builder(UploadFileWorker.class)
                .setInputData(inputData)
//                .setConstraints(constraints)
//                //设置一个拦截器，在任务执行之前 可以做一次拦截，去修改入参的数据然后返回新的数据交由worker使用
//                .setInputMerger(null)
//                //当一个任务被调度失败后，所要采取的重试策略，可以通过BackoffPolicy来执行具体的策略
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
//                //任务被调度执行的延迟时间
//                .setInitialDelay(10, TimeUnit.SECONDS)
//                //设置该任务尝试执行的最大次数
//                .setInitialRunAttemptCount(2)
//                //设置这个任务开始执行的时间
//                //System.currentTimeMillis()
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
//                //指定该任务被调度的时间
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
//                //当一个任务执行状态编程finish时，又没有后续的观察者来消费这个结果，难么workamnager会在
//                //内存中保留一段时间的该任务的结果。超过这个时间，这个结果就会被存储到数据库中
//                //下次想要查询该任务的结果时，会触发workmanager的数据库查询操作，可以通过uuid来查询任务的状态
//                .keepResultsForAtLeast(10, TimeUnit.SECONDS)
                .build();
        return request;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CaptureActivity.REQ_CAPTURE && data != null) {
            width = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 0);
            height = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            isVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_TYPE, false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {

        if (TextUtils.isEmpty(filePath)) {
            return;
        }

        mBinding.actionAddFile.setVisibility(View.GONE);
        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.cover.setImageUrl(filePath);
        mBinding.videoIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        mBinding.cover.setOnClickListener(v ->
                PreviewActivity.startActivityForResult(PublishActivity.this, filePath, isVideo, null)
        );
    }

}
