package com.jackson.jike.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.ZoomState;
import androidx.camera.core.impl.VideoCaptureConfig;
import androidx.camera.extensions.AutoImageCaptureExtender;
import androidx.camera.extensions.AutoPreviewExtender;
import androidx.camera.extensions.BeautyImageCaptureExtender;
import androidx.camera.extensions.BeautyPreviewExtender;
import androidx.camera.extensions.BokehImageCaptureExtender;
import androidx.camera.extensions.BokehPreviewExtender;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.extensions.HdrPreviewExtender;
import androidx.camera.extensions.NightImageCaptureExtender;
import androidx.camera.extensions.NightPreviewExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.common.util.concurrent.ListenableFuture;
import com.jackson.common.util.CommonUtil;
import com.jackson.common.util.KLog;
import com.jackson.common.util.StatusBar;
import com.jackson.jike.R;
import com.jackson.jike.databinding.ActivityCaptureBinding;
import com.jackson.jike.view.CameraXPreviewView;
import com.jackson.jike.view.FocusImageView;
import com.jackson.jike.view.RecordView;
import com.jackson.permission.LivePermission;
import com.jackson.permission.PermissionResult;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CaptureActivity extends AppCompatActivity {

    public static final int REQ_CAPTURE = 10001;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private static final String[] PERMISSION_TIPS = new String[]{"相机权限", "读写权限", "麦克风权限"};
    private static final double RATIO_4_3_VALUE = 4.0 / 3.0;
    private static final double RATIO_16_9_VALUE = 16.0 / 9.0;
    //分辨率
    private Size resolution = new Size(1080, 1920);
    private CameraXPreviewView mPreviewView;
    private RecordView mRecordView;
    private ImageView mCameraSwitchView, mGallery;
    /**
     * 照相
     */
    private ImageCapture mImageCapture;
    /**
     * 录制视频
     */
    private VideoCapture mVideoCapture;

    /**
     * camera 对象  用来查询和修改相机的某些功能，如对焦、变焦和闪光灯
     */
    private Camera mCamera;
    private CameraInfo mCameraInfo;
    private CameraControl mCameraControl;
    /**
     * 可以将一个camera跟任意的LifecycleOwner绑定的一个单例类
     */
    private ProcessCameraProvider mCameraProvider;
    /**
     * 摄像头朝向 默认向后
     */
    private int mLensFacing = CameraSelector.LENS_FACING_BACK;
    /**
     * 是否是照相
     */
    private boolean takingPicture;
    /**
     * 文件输出路径
     */
    private String outputFilePath;
    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";
    private ExecutorService mExecutorService;
    private CameraSelector mCameraSelector;
    private FocusImageView mFocusView;

    private CaptureViewModel viewModel;

    public static void startActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBar.hideBar(this);
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(CaptureViewModel.class);
        ActivityCaptureBinding mBinding = DataBindingUtil.setContentView(this, R.layout.activity_capture);
        mPreviewView = mBinding.preView;
        mRecordView = mBinding.recordView;
        mCameraSwitchView = mBinding.cameraSwitch;
        mGallery = mBinding.gallery;
        mFocusView = mBinding.focusView;
        mExecutorService = Executors.newSingleThreadExecutor();
        updateCameraUi();
        initListener();

        LivePermission livePermission = new LivePermission(this);
        livePermission.requestPermissions(PERMISSIONS, PERMISSION_TIPS)
                .observe(this, permissionResult -> {
                    if (permissionResult instanceof PermissionResult.Granted) {
                        setUpCamera();
                    } else if (permissionResult instanceof PermissionResult.Denied) {
                        mBinding.container.postDelayed(this::finish, 500);
                    } else {
                        CommonUtil.showToast("go permission setting");
                    }
                });

    }

    private void initCameraListener() {
        mCameraInfo = mCamera.getCameraInfo();
        mCameraControl = mCamera.getCameraControl();
        LiveData<ZoomState> zoomState = mCameraInfo.getZoomState();
        float maxZoomRatio = zoomState.getValue().getMaxZoomRatio();
        float minZoomRatio = zoomState.getValue().getMinZoomRatio();
        KLog.i("maxZoomRatio = " + maxZoomRatio);
        KLog.i("minZoomRatio = " + minZoomRatio);

        mPreviewView.setCustomTouchListener(new CameraXPreviewView.CustomTouchListener() {
            @Override
            public void zoomIn() {
                float zoomRatio = zoomState.getValue().getZoomRatio();
                if (zoomRatio < maxZoomRatio) {
                    mCameraControl.setZoomRatio((float) (zoomRatio + 0.1));
                }
            }

            @Override
            public void zoomOut() {
                float zoomRatio = zoomState.getValue().getZoomRatio();
                if (zoomRatio > minZoomRatio) {
                    mCameraControl.setZoomRatio((float) (zoomRatio - 0.1));
                }
            }

            @Override
            public void click(float x, float y) {
                MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(1.0f, 1.0f);
                MeteringPoint point = factory.createPoint(x, y);
                FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        // auto calling cancelFocusAndMetering in 3 seconds
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build();

                mFocusView.startFocus(new Point((int) x, (int) y));
                ListenableFuture future = mCameraControl.startFocusAndMetering(action);
                future.addListener(() -> {
                    try {
                        FocusMeteringResult result = (FocusMeteringResult) future.get();
                        if (result.isFocusSuccessful()) {
                            mFocusView.onFocusSuccess();
                        } else {
                            mFocusView.onFocusFailed();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, ContextCompat.getMainExecutor(CaptureActivity.this));
            }

            @Override
            public void doubleClick(float x, float y) {
                // 双击放大缩小
                float zoomRatio = zoomState.getValue().getZoomRatio();
                if (zoomRatio > minZoomRatio) {
                    mCameraControl.setLinearZoom(0f);
                } else {
                    mCameraControl.setLinearZoom(0.5f);
                }
            }

            @Override
            public void longClick(float x, float y) {

            }
        });
    }

    private void initListener() {

        mGallery.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            startActivityForResult(intent,111);
//            PhotoUtils.select(this, new Function3<Uri, Boolean, String, Unit>() {
//                @Override
//                public Unit invoke(Uri uri, Boolean aBoolean, String s) {
//                    PreviewActivity.startActivityForResult(CaptureActivity.this, s, false, "完成");
//                    return null;
//                }
//            });
        });

        mCameraSwitchView.setOnClickListener(v -> {
            if (mCameraSelector != null) {
                @SuppressLint("RestrictedApi") int currentLensFacing = mCameraSelector.getLensFacing();
                if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
                    mLensFacing = CameraSelector.LENS_FACING_FRONT;
                } else if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
                    mLensFacing = CameraSelector.LENS_FACING_BACK;
                }
                bindCameraUseCases();
            }
        });

        mRecordView.setOnRecordListener(new RecordView.OnRecordListener() {
            @Override
            public void onClick() {
                //拍照
                takingPicture = true;
                //创建图片保存的文件地址
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
                        System.currentTimeMillis() + ".jpeg");
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                if (mImageCapture == null) return;
                mImageCapture.takePicture(outputFileOptions, mExecutorService, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (savedUri == null) {
                            savedUri = Uri.fromFile(file);
                        }
                        outputFilePath = file.getAbsolutePath();
                        onFileSaved(savedUri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        KLog.e("Photo capture failed: " + exception.getMessage(), exception);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                //视频
                takingPicture = false;
                //创建视频保存的文件地址
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
                        System.currentTimeMillis() + ".mp4");
                if (mVideoCapture == null) return;
                mVideoCapture.startRecording(file, Executors.newSingleThreadExecutor(), new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull File file) {
                        outputFilePath = file.getAbsolutePath();
                        onFileSaved(Uri.fromFile(file));
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        KLog.i(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                //录制完成
                if (mVideoCapture == null) return;
                mVideoCapture.stopRecording();
            }
        });
    }

    private void updateCameraUi() {
        //必须先remove在add这样视频流画面才能正确的显示出来
        ViewGroup parent = (ViewGroup) mPreviewView.getParent();
        parent.removeView(mPreviewView);
        parent.addView(mPreviewView, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
            //当设备处于竖屏情况时，宽高的值 需要互换，横屏不需要
            intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight());
            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth());
            intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    private void onFileSaved(Uri savedUri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sendBroadcast(new Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri));
        }
//        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap
                .getFileExtensionFromUrl(savedUri.getPath()));
        MediaScannerConnection.scanFile(this, new String[]{new File(outputFilePath).getAbsolutePath()},
                new String[]{mimeType}, (path, uri) ->
                        KLog.i("Image capture scanned into media store: $uri = " + uri)
        );
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }

    private void setUpCamera() {
        //Future表示一个异步的任务，ListenableFuture可以监听这个任务，当任务完成的时候执行回调
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                mCameraProvider = cameraProviderFuture.get();
                //选择摄像头的朝向
                mLensFacing = getLensFacing();
                if (mLensFacing == -1) {
                    CommonUtil.showToast("无可用的设备cameraId!,请检查设备的相机是否被占用");
                    return;
                }
                // 构建并绑定照相机用例
                bindCameraUseCases();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraUseCases() {
        //获取屏幕的分辨率
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mPreviewView.getDisplay().getRealMetrics(displayMetrics);
        //获取宽高比
        int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
        //获取屏幕旋转方向
        int rotation = mPreviewView.getDisplay().getRotation();

        if (mCameraProvider == null) {
            CommonUtil.showToast("相机初始化失败");
            return;
        }

        mCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(mLensFacing)
                .build();

        Preview preview = initPreview(screenAspectRatio, rotation);

        initImageCapture(screenAspectRatio, rotation);

        initImageAnalysis(screenAspectRatio, rotation);

        initVideoCapture(screenAspectRatio, rotation);

        //重新绑定之前必须先取消绑定
        mCameraProvider.unbindAll();

        mCamera = mCameraProvider.bindToLifecycle(this,
                mCameraSelector, preview, mImageCapture, mVideoCapture);
        initCameraListener();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider(mCamera.getCameraInfo()));
    }

    private Preview initPreview(int aspectRatio, int rotation) {

        Preview.Builder pBuilder = new Preview.Builder();

        setPreviewExtender(pBuilder, mCameraSelector);

        return pBuilder
                //设置宽高比
                .setTargetAspectRatio(aspectRatio)
                //设置当前屏幕的旋转
                .setTargetRotation(rotation)
                .build();
    }

    /**
     * 构建图像捕获用例
     */
    private void initImageCapture(int aspectRatio, int rotation) {
        ImageCapture.Builder builder = new ImageCapture.Builder();

        setImageCaptureExtender(builder, mCameraSelector);

        mImageCapture = builder
                //优化捕获速度，可能降低图片质量
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                //设置宽高比
                .setTargetAspectRatio(aspectRatio)
                //设置初始的旋转角度
                .setTargetRotation(rotation)
                //设置闪光灯模式：自动
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build();
    }

    /**
     * 构建图像分析用例(可用于二维码识别等用途)
     * 注意：Analyzer回掉方法中如果不调用image.close()将不会获取到下一张图片
     */
    private void initImageAnalysis(int aspectRatio, int rotation) {
        ImageAnalysis mImageAnalysis = new ImageAnalysis.Builder()
                //设置宽高比
                .setTargetAspectRatio(aspectRatio)
                //设置旋转角度
                .setTargetRotation(rotation)
                // 仅将最新图像传送到分析仪，并在到达图像时将其丢弃。
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        mImageAnalysis.setAnalyzer(mExecutorService, image -> {
            //将图像转换到正确方向所需的旋转角度
            int rotationDegrees = image.getImageInfo().getRotationDegrees();
            KLog.e("将图像转换到正确方向所需的旋转角度 = " + rotationDegrees);
            ImageProxy.PlaneProxy[] planes = image.getPlanes();

            ByteBuffer buffer = planes[0].getBuffer();
            // 转为byte[]
            byte[] b = new byte[buffer.remaining()];
            // TODO: 分析完成后关闭图像参考，否则会阻塞其他图像的产生
            image.close();
        });
    }

    /**
     * 构建视频捕获用例
     */
    @SuppressLint("RestrictedApi")
    private void initVideoCapture(int aspectRatio, int rotation) {
        mVideoCapture = new VideoCaptureConfig.Builder()
                //设置当前旋转
                .setTargetRotation(rotation)
                //设置宽高比
                .setTargetAspectRatio(aspectRatio)
                //设置当前旋转
//                .setTargetResolution(resolution)
                //视频帧率  越高视频体积越大
                .setVideoFrameRate(25)
                //bit率  越大视频体积越大
                .setBitRate(3 * 1024 * 1024)
                .build();
    }

    /**
     * 给预览设置外部扩展
     */
    private void setPreviewExtender(Preview.Builder builder, CameraSelector cameraSelector) {
        AutoPreviewExtender extender = AutoPreviewExtender.create(builder);
        if (extender.isExtensionAvailable(cameraSelector)) {
            extender.enableExtension(cameraSelector);
        }
        BokehPreviewExtender bokehPreviewExtender = BokehPreviewExtender.create(builder);
        if (bokehPreviewExtender.isExtensionAvailable(cameraSelector)) {
            bokehPreviewExtender.enableExtension(cameraSelector);
        }
        HdrPreviewExtender hdrPreviewExtender = HdrPreviewExtender.create(builder);
        if (hdrPreviewExtender.isExtensionAvailable(cameraSelector)) {
            hdrPreviewExtender.enableExtension(cameraSelector);
        }
        BeautyPreviewExtender beautyPreviewExtender = BeautyPreviewExtender.create(builder);
        if (beautyPreviewExtender.isExtensionAvailable(cameraSelector)) {
            beautyPreviewExtender.enableExtension(cameraSelector);
        }
        NightPreviewExtender nightPreviewExtender = NightPreviewExtender.create(builder);
        if (nightPreviewExtender.isExtensionAvailable(cameraSelector)) {
            nightPreviewExtender.enableExtension(cameraSelector);
        }
    }

    /**
     * 给拍照设置外部预览
     */
    private void setImageCaptureExtender(ImageCapture.Builder builder, CameraSelector cameraSelector) {
        AutoImageCaptureExtender autoImageCaptureExtender = AutoImageCaptureExtender.create(builder);
        if (autoImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            autoImageCaptureExtender.enableExtension(cameraSelector);
        }
        BokehImageCaptureExtender bokehImageCaptureExtender = BokehImageCaptureExtender.create(builder);
        if (bokehImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            bokehImageCaptureExtender.enableExtension(cameraSelector);
        }
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }
        BeautyImageCaptureExtender beautyImageCaptureExtender = BeautyImageCaptureExtender.create(builder);
        if (beautyImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            beautyImageCaptureExtender.enableExtension(cameraSelector);
        }
        NightImageCaptureExtender nightImageCaptureExtender = NightImageCaptureExtender.create(builder);
        if (nightImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            nightImageCaptureExtender.enableExtension(cameraSelector);
        }
    }

    /**
     * 长宽比
     *
     * @param widthPixels  宽
     * @param heightPixels 高
     */
    private int aspectRatio(int widthPixels, int heightPixels) {
        double previewRatio = (double) Math.max(widthPixels, heightPixels) / (double) Math.min(widthPixels, heightPixels);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    /**
     * 获取可用摄像头
     */
    private int getLensFacing() {
        if (hasBackCamera()) {
            return CameraSelector.LENS_FACING_BACK;
        }
        if (hasFrontCamera()) {
            return CameraSelector.LENS_FACING_FRONT;
        }
        return -1;
    }

    /**
     * 是否有后摄像头
     */
    private boolean hasBackCamera() {
        if (mCameraProvider == null) {
            return false;
        }
        try {
            return mCameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否有前摄像头
     */
    private boolean hasFrontCamera() {
        if (mCameraProvider == null) {
            return false;
        }
        try {
            return mCameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        mExecutorService.shutdown();
        super.onDestroy();
    }
}
