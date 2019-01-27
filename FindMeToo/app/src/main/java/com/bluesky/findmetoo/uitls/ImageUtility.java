package com.bluesky.findmetoo.uitls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ImageUtility {
    private static final PictureQuality Quality = PictureQuality.High;

    public static Bitmap resizeAndCompressImageBeforeSend(Context context, String filePath) {
        final int MAX_IMAGE_SIZE = 800 * 600; // max final file size in kilobytes

        // First decode with inJustDecodeBounds=true to check dimensions of image
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize(First we are going to resize the image to 800x800 image, in order to not have a big but very low quality image.
        //resizing the image will already reduce the file size, but after resizing we will check the file size and start to compress image
//        options.inSampleSize = calculateInSampleSize(options, 800, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bmpPic = BitmapFactory.decodeFile(filePath, options);

        int compressQuality = 100; // quality decreasing by 5 every loop.
        int streamLength;
        do {
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            Log.d("compressBitmap", "Quality: " + compressQuality);
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            compressQuality -= 5;
            Log.d("compressBitmap", "Size: " + streamLength / 1024 + " kb");
        } while (streamLength >= MAX_IMAGE_SIZE);

        Toast.makeText(context, "image byte count : " + bmpPic.getByteCount(), Toast.LENGTH_SHORT);
        return bmpPic;
//        try {
//            //save the resized and compressed file to disk cache
//            Log.d("compressBitmap", "cacheDir: " + context.getCacheDir());
//            FileOutputStream bmpFile = new FileOutputStream(context.getCacheDir() + fileName);
//            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile);
//            bmpFile.flush();
//            bmpFile.close();
//        } catch (Exception e) {
//            Log.e("compressBitmap", "Error on saving file");
//        }
//        //return the path of resized and compressed file
//        return context.getCacheDir() + fileName;
    }

    public static Bitmap resizeAndCompressImageBeforeSend(Context context, Bitmap bmpPic) {
        final int MAX_IMAGE_SIZE = 800 * 600; // max final file size in kilobytes

        int compressQuality = 100; // quality decreasing by 5 every loop.
        int streamLength;
        do {
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            Log.d("compressBitmap", "Quality: " + compressQuality);
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            compressQuality -= 5;
            Log.d("compressBitmap", "Size: " + streamLength / 1024 + " kb");
        } while (streamLength >= MAX_IMAGE_SIZE);

        return bmpPic;
//        try {
//            //save the resized and compressed file to disk cache
//            Log.d("compressBitmap", "cacheDir: " + context.getCacheDir());
//            FileOutputStream bmpFile = new FileOutputStream(context.getCacheDir() + fileName);
//            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile);
//            bmpFile.flush();
//            bmpFile.close();
//        } catch (Exception e) {
//            Log.e("compressBitmap", "Error on saving file");
//        }
//        //return the path of resized and compressed file
//        return context.getCacheDir() + fileName;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        String debugTag = "MemoryInformation";
        // Image nin islenmeden onceki genislik ve yuksekligi
        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(debugTag, "image height: " + height + "---image width: " + width);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(debugTag, "inSampleSize: " + inSampleSize);
        return inSampleSize;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }







    public static void scaleImageToResolution(Context context, File image, int dstWidth, int dstHeight) {
        if (dstHeight > 0 && dstWidth > 0 && image != null) {

            Bitmap result = null;
            try {
                //Get Image Properties
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                int photoH = bmOptions.outHeight;
                int photoW = bmOptions.outWidth;

                bmOptions.inJustDecodeBounds = false;
                bmOptions.inPurgeable = true;
                //Smaller Image Size in Memory with Config
                bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;

                //Is resolution not the same like 16:9 == 4:3 then crop otherwise fit
                ScalingLogic scalingLogic = getScalingLogic(photoW, photoH, dstWidth, dstHeight);
                //Get Maximum automatic downscaling that it's still bigger then this requested resolution
                bmOptions.inSampleSize = calculateScalingSampleSize(photoW, photoH, dstWidth, dstHeight, scalingLogic);

                //Get unscaled Bitmap
                result = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                //Scale Bitmap to requested Resolution
                result = scaleImageToResolution(context, result, scalingLogic, dstWidth, dstHeight);

                if (result != null) {
                    //Save Bitmap with quality
                    saveImageWithQuality(context, result, image);
                }
            } finally {
                //Clear Memory
                if (result != null)
                    result.recycle();
            }
        }
    }

    public static void saveImageWithQuality(Bitmap bitmap, String path, int compressQuality) {
        try {
            FileOutputStream fOut;
            fOut = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException ex) {
                Log.e("saveImageWithQuality", "Error while saving compressed Picture: " + ex.getMessage());
        }
    }

    public static void saveImageWithQuality(Context context, Bitmap bitmap, File file) {
        saveImageWithQuality(bitmap, file.getAbsolutePath(), getCompressQuality());
    }

    public static void saveImageWithQuality(Context context, Bitmap bitmap, String path) {
        saveImageWithQuality(bitmap, path, getCompressQuality());
    }

    private static int calculateScalingSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            } else {
                return srcHeight / dstHeight;
            }
        } else {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return srcHeight / dstHeight;
            } else {
                return srcWidth / dstWidth;
            }
        }
    }

    private static Bitmap scaleImageToResolution(Context context, Bitmap unscaledBitmap, ScalingLogic scalingLogic, int dstWidth, int dstHeight) {
        //Do Rectangle of original picture when crop
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        //Do Rectangle to fit in the source rectangle
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        //insert source rectangle into new one
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
        //Recycle the unscaled Bitmap afterwards
        unscaledBitmap.recycle();

        return scaledBitmap;
    }

    private static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            if (srcWidth >= srcHeight) {
                //Horizontal
                final float srcAspect = (float) srcWidth / (float) srcHeight;
                final float dstAspect = (float) dstWidth / (float) dstHeight;

                if (srcAspect < dstAspect || isResolutionEqual(srcAspect, dstAspect)) {
                    final int srcRectHeight = (int) (srcWidth / dstAspect);
                    final int scrRectTop = (srcHeight - srcRectHeight) / 2;
                    return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
                } else {
                    final int srcRectWidth = (int) (srcHeight * dstAspect);
                    final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                    return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
                }
            } else {
                //Vertikal
                final float srcAspect = (float) srcHeight / (float) srcWidth;
                final float dstAspect = (float) dstWidth / (float) dstHeight;

                if (srcAspect < dstAspect || isResolutionEqual(srcAspect, dstAspect)) {
                    final int srcRectWidth = (int) (srcHeight / dstAspect);
                    final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                    return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
                } else {
                    final int srcRectHeight = (int) (srcWidth * dstAspect);
                    final int scrRectTop = (srcHeight - srcRectHeight) / 2;
                    return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
                }
            }
        } else {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }

    private static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            if (srcWidth > srcHeight) {
                //Vertikal
                final float srcAspect = (float) srcWidth / (float) srcHeight;
                final float dstAspect = (float) dstWidth / (float) dstHeight;

                if (srcAspect < dstAspect || isResolutionEqual(srcAspect, dstAspect)) {
                    return new Rect(0, 0, (int) (dstHeight * srcAspect), dstHeight);
                } else {
                    return new Rect(0, 0, dstWidth, (int) (dstWidth / srcAspect));
                }
            } else {
                //Horizontal
                final float srcAspect = (float) srcHeight / (float) srcWidth;
                final float dstAspect = (float) dstWidth / (float) dstHeight;

                if (srcAspect < dstAspect || isResolutionEqual(srcAspect, dstAspect)) {
                    return new Rect(0, 0, (int) (dstHeight / srcAspect), dstHeight);
                } else {
                    return new Rect(0, 0, dstWidth, (int) (dstWidth * srcAspect));
                }
            }
        } else {
            if (srcWidth >= srcHeight)
                return new Rect(0, 0, dstWidth, dstHeight);
            else
                return new Rect(0, 0, dstHeight, dstWidth);
        }
    }

    private static ScalingLogic getScalingLogic(int imageWidth, int imageHeight, int dstResolutionWidth, int dstResolutionHeight) {
        if (imageWidth >= imageHeight) {
            //Bild horizontal
            final float srcAspect = (float) imageWidth / (float) imageHeight;
            final float dstAspect = (float) dstResolutionWidth / (float) dstResolutionHeight;
            if (!isResolutionEqual(srcAspect, dstAspect)) {
                return ScalingLogic.CROP;
            } else {
                return ScalingLogic.FIT;
            }
        } else {
            //Bild vertikal
            final float srcAspect = (float) imageHeight / (float) imageWidth;
            final float dstAspect = (float) dstResolutionWidth / (float) dstResolutionHeight;
            if (!isResolutionEqual(srcAspect, dstAspect)) {
                return ScalingLogic.CROP;
            } else {
                return ScalingLogic.FIT;
            }
        }
    }

    public enum PictureQuality {
        High,
        Medium,
        Low
    }

    public enum ScalingLogic {
        CROP,
        FIT
    }

    //Does resolution match
    private static boolean isResolutionEqual(float v1, float v2) {
        // Falls a 1.999999999999 and b = 2.000000000000
        return v1 == v2 || Math.abs(v1 - v2) / Math.max(Math.abs(v1), Math.abs(v2)) < 0.01;
    }

    public static int getCompressQuality() {
        if (Quality == PictureQuality.High)
            return 100;
        else if (Quality == PictureQuality.Medium)
            return 50;
        else if (Quality == PictureQuality.Low)
            return 25;
        else return 0;
    }
}
