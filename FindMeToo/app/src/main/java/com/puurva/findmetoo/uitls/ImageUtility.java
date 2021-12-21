package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
//import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUtility {
    private static final PictureQuality Quality = PictureQuality.High;

    public static  Bitmap scaleImageToResolution(Bitmap image){
        return scaleImageToResolution(image, image.getWidth(), image.getHeight());
    }

    public static Bitmap scaleImageToResolution(Bitmap image, int dstWidth, int dstHeight) {

        Bitmap result = null;
        if (dstHeight > 0 && dstWidth > 0 && image != null) {
            try {
                //Get Image Properties
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
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
                result = image;

                //Scale Bitmap to requested Resolution
                result = scaleImageToResolution(result, scalingLogic, dstWidth, dstHeight);

            } finally {
            }
        }
        return result;
    }

    public static Bitmap scaleImageToResolution(Bitmap image, int dstWidth, int dstHeight, File fileToWrite) {

        Bitmap result = null;
        if (dstHeight > 0 && dstWidth > 0 && image != null) {
            try {
                //Get Image Properties
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
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
                result = image;

                //Scale Bitmap to requested Resolution
                result = scaleImageToResolution(result, scalingLogic, dstWidth, dstHeight);

                if (result != null) {
                    //Save Bitmap with quality
                    saveImageWithQuality(result, fileToWrite);
                }
            } finally {
            }
        }
        return result;
    }

//    public static void saveImageWithQuality(Bitmap bitmap, String path) {
//        saveImageWithQuality(bitmap, path, getCompressQuality());
//    }
//
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

    public static void saveImageWithQuality(Bitmap bitmap, File file) {
        saveImageWithQuality(bitmap, file.getAbsolutePath(), getCompressQuality());
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

    private static Bitmap scaleImageToResolution(Bitmap unscaledBitmap, ScalingLogic scalingLogic, int dstWidth, int dstHeight) {
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


//    @Nullable
//    public static ExifInterface getExifInterface(Context context, Uri uri) {
//        try {
//            String path = uri.toString();
//            if (path.startsWith("file://")) {
//                return new ExifInterface(path);
//            }
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                if (path.startsWith("content://")) {
//                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
//                    return new ExifInterface(inputStream);
//                }
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public static float getExifAngle(Context context, Uri uri) {
//        try {
//            ExifInterface exifInterface = getExifInterface(context, uri);
//            if(exifInterface == null) {
//                return -1f;
//            }
//
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_UNDEFINED);
//
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    return 90f;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    return 180f;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    return 270f;
//                case ExifInterface.ORIENTATION_NORMAL:
//                    return 0f;
//                case ExifInterface.ORIENTATION_UNDEFINED:
//                    return -1f;
//                default:
//                    return -1f;
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return -1f;
//        }
//    }

    public static float getExifAngle(Context context, String filePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            if(exifInterface == null) {
                return -1f;
            }

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90f;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180f;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270f;
                case ExifInterface.ORIENTATION_NORMAL:
                    return 0f;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    return -1f;
                default:
                    return -1f;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1f;
        }
    }

//    public static Bitmap rotateImage(Bitmap source, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
//                matrix, true);
//    }

    public static Bitmap rotateImage(String filePath, float angle) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap source = BitmapFactory.decodeFile(filePath, options);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
//        if (source != null) {
//            //Save Bitmap with quality
//            saveImageWithQuality(source, filePath, getCompressQuality());
//        }
        return source;
    }

//    Unused overloads. Could be useful in future.
    public static void scaleImageToResolution(File image, int dstWidth, int dstHeight) {
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
                result = scaleImageToResolution(result, scalingLogic, dstWidth, dstHeight);

                if (result != null) {
                    //Save Bitmap with quality
                    saveImageWithQuality(result, image);
                }
            } finally {
                //Clear Memory
                if (result != null)
                    result.recycle();
            }
        }
    }

    public static void GetProfileImage(final ImageButton imageButton, String token, String deviceID, final int width, final int height) {
            ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
            Call<ResponseBody> call = apiService.getProfileImage("Bearer " + token, deviceID);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {

                        boolean FileDownloaded = false;
                        if (response.body() != null) {
                            FileDownloaded = DownloadImage(response.body(), imageButton, width, height);
                        }

                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("onFailure", t.toString());
                }
            });
    }

    public static boolean GetActivityImage(final String imagePath, final ImageButton imageButton, String token, final int width, final int height) {
        try {
            if(imagePath != null && !imagePath.isEmpty()) {
                String deviceId = imagePath.split("\\\\")[0];
                final String fileName = imagePath.split("\\\\")[1];
                ApiInterface apiService =
                        HttpClient.getClient().create(ApiInterface.class);
                Call<ResponseBody> call = apiService.getMatchingImages("Bearer " + token, deviceId, fileName);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            Log.d("onResponse", "Response came from server");

                            if (response.body() != null) {
                                DownloadImage(response.body(), fileName, imageButton, width, height);
                            }
//                        marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("onFailure", t.toString());
                        return;
                    }
                });
            }
        } catch (Exception e) {
            Log.d("onResponse", "There is an error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean DownloadImage(ResponseBody body, ImageButton imageButton, int width, int height) {

        try {
            Log.d("DownloadImage", "Reading and writing file");
            if (body != null) {
                // display the image data in a ImageView or save it
                Bitmap bmp = BitmapFactory.decodeStream(body.byteStream());

                bmp = ImageUtility.scaleImageToResolution(bmp, width, height);
                imageButton.setMaxWidth(bmp.getWidth());
                imageButton.setMaxHeight(bmp.getHeight());
                imageButton.setImageBitmap(bmp);
            }
            return true;

        } catch (Exception e) {
            Log.d("DownloadImage", e.toString());
            return false;
        }
    }

    public static boolean DownloadImage(ResponseBody body, String filename, ImageButton imageButton, int width, int height) {

        try {
            Log.d("DownloadImage", "Reading and writing file");
            if (body != null) {
                // display the image data in a ImageView or save it
                Bitmap bmp = BitmapFactory.decodeStream(body.byteStream());

                try  {
                    String imagePath = CommonUtility.GetFilePath() + filename + ".png";
                    FileOutputStream out = new FileOutputStream(imagePath);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                    SetImage(imagePath, imageButton, width, height);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return true;

        } catch (Exception e) {
            Log.d("DownloadImage", e.toString());
            return false;
        }
    }

    public static boolean SetImage(String path, ImageButton imageButton, int width, int height) {
        File imgFile = new  File(path);

        if(imgFile.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            bmp = ImageUtility.scaleImageToResolution(bmp, width, height);
            imageButton.setMaxWidth(bmp.getWidth());
            imageButton.setMaxHeight(bmp.getHeight());
            imageButton.setImageBitmap(bmp);
            imageButton.invalidate();
            return  true;
        } else {
            return false;
        }
    }

}
