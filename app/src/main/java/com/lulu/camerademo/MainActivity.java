package com.lulu.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private File mTargetFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mImageView = (ImageView) findViewById(R.id.main_image);

    }

    @OnClick(R.id.main_btn_take_photo_1)
    public void btnTakeOnClick(View v) {

        //隐式意图拍照, 获取简单的缩略图
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //缩略图是BitMap对象, 使用startActivityForResult启动
        startActivityForResult(intent, 0);
    }

    @OnClick(R.id.main_btn_take_photo_2)
    public void btnTakeFullyOnClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //使用 EXTRA_OUTPUT 指定 Uri 位置, 可以把大尺寸原图保存到 Uri 指定的位置
        String state = Environment.getExternalStorageState();
        File dir = getFilesDir();//内部
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStorageDirectory();//外部
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        mTargetFile = new File(dir, "img-" + SystemClock.currentThreadTimeMillis() + ".jpg");
        Uri uri = Uri.fromFile(mTargetFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            //获取缩略图
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    //如果Intent data有数据, 代表内部包含缩略图
                    Bitmap bitmap = data.getParcelableExtra("data");
                    if (bitmap != null) {
                        mImageView.setImageBitmap(bitmap);
                    }
                }
            }

        } else if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                //图片已经拍照并且保存
                Toast.makeText(this, "拍照成功", Toast.LENGTH_SHORT).show();

                if (mTargetFile.exists()) {
                    //加载图片, 显示(!!! 图片的压缩, 内存的优化, 图片二次采样)

                    // 案例 1: 使用 Options 参数直接设置inSimpleSize

//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 2;
//
//                    options.inPreferredConfig = Bitmap.Config.RGB_565;
//
//                    //inSampleSize
//                    Bitmap bitmap = BitmapFactory.decodeFile(mTargetFile.getAbsolutePath(), options);
//                    mImageView.setImageBitmap(bitmap);
                    //案例2. 使用图片内存压缩来显示图片的

                    Bitmap bitmap = BitmapUtil.loadBitmapWithScale(mTargetFile, 100, 100);
                    mImageView.setImageBitmap(bitmap);
                }

            }

        }
    }
}
