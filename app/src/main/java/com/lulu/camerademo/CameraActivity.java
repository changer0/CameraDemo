package com.lulu.camerademo;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.hardware.Camera.getCameraInfo;
import static android.hardware.Camera.getNumberOfCameras;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {

    private SurfaceView mSuraceView;

    private Camera mCamera;
    private int mCameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        mSuraceView = (SurfaceView) findViewById(R.id.camera_preview);
        if (mSuraceView != null) {
            mSuraceView.getHolder().addCallback(this);
        }

    }


    /**
     * 根据手机屏幕放心啊过, 来检测和设置摄像头预览的方法
     *
     * @
     */
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void initCamera() {
        int numberOfCameras = getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                mCameraId = i;
                break;
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            //只打开第一个找到的后置摄像头
            //mCamera = Camera.open();
            initCamera();
            //前置 根据open() 内部代码判断, facing是前置即可
            if (mCamera != null) {
                try {
                    //照相机的设置
                    //1. 预览设置
                    //根据手机屏幕方向来设置预览显示的方向
                    setCameraDisplayOrientation(this, mCameraId, mCamera);
                    // 1.2 设置预览回调接口. 通常录制, 二维码扫描 都使用这个接口
                    //mCamera.setPreviewCallbackWithBuffer(this);

                    //2. 拍照设置, 使用Parameters 来设置
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
                    //闪关灯模式
                    // 1) 设置闪关灯模式, 需要使用权限
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

                    //2) 设置拍照的图片格式, 针对拍照接口
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.setJpegQuality(100);
                    mCamera.setParameters(parameters);

                    mCamera.setPreviewDisplay(holder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        Canvas canvas = holder.lockCanvas();

//        canvas.drawColor(Color.BLUE);
//
//        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            //必须释放, 否则其他应用程序不能使用摄像头
            mCamera.release();
        }
    }

    public void btnTakePhoto(View view) {
        if (mCamera != null) {
            // 拍照, 默认有声音, 拍照完成, 调用回调接口
            //回调接口调用哪个是根据摄像头的配置来完成的
            //三个参数:
            //1. 快门接口;
            //2. 如果拍照格式是RAW 调用这个接口
            //3. 如果拍照格式是JPEG 那么调用这个接口
            mCamera.takePicture(null, null, this);
        }
    }

    // ---------------------
    // 摄像头预览出来的图像, 会回传到当前的方法
    // 预览的图片格式 通常是 YUV 格式 YCbCr
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //TODO: 二维码扫描, 视频直播
    }

    /**
     * 接收拍照之后实际的图像数据, 这个方法内部, 如果拍完照还需要继续预览和拍照的话
     * 内部必须再一次调用 Camera 的startPreview
     * @param data 图像数据, 对于PictureFormat 为 JPEG 的图像, 就是 JPEG 的图像
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        String state = Environment.getExternalStorageState();
        File dir = getFilesDir();//内部
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStorageDirectory();//外部
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File target = new File(dir, "img-" + SystemClock.currentThreadTimeMillis() + ".jpg");
        try{
            if (target != null) {
                target.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(target);
            fout.write(data);
            fout.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        // TODO: 2016/10/18 再次预览
        camera.startPreview();

    }
}
