package com.lulu.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Created by Lulu on 2016/10/18.
 */

public final class BitmapUtil {

    public BitmapUtil() {
    }

    public static Bitmap loadBitmapWithScale(File imageFile, int reqWidth, int reqHeight) {
        Bitmap ret = null;
        String filePath = imageFile.getAbsolutePath();
        if (imageFile != null && imageFile.exists() && imageFile.canRead()) {
            if (reqWidth >= 0 && reqHeight >= 0) {
                // 1. 第一次采样
                BitmapFactory.Options options = new BitmapFactory.Options();
                // 只获取尺寸
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                // 2. 获取图片原始尺寸, 计算采样数值
                options.inSampleSize = calcInSampleSize(options, reqWidth, reqHeight);

                //3. 再次设置, 要加载图像到内存
                options.inJustDecodeBounds = false;

                // 4. 可以使用inPreferredConfig来进一步降低图片像素内存
                // png图片都可以支持透明
                // jpg图片不支持透明
                String type = options.outMimeType;
                if (type != null) {
                    //根据 mimeType 来判断图像的文件格式
                    // image/png
                    // image/jpeg
                    if (type.endsWith("png")) {
                        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    } else if (type.endsWith("jpeg")) {
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                    }

                }

                ret = BitmapFactory.decodeFile(filePath, options);

            }
        }

        return ret;
    }

    private static int calcInSampleSize(BitmapFactory.Options op, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        int width = op.outWidth;
        int height = op.outHeight;
        if (height > reqHeight || width > reqWidth) {
            int halfW = width >> 1;
            int halfH = height >> 1;

            while ((halfW / inSampleSize) >= reqWidth && (halfH / inSampleSize) >= reqHeight) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
