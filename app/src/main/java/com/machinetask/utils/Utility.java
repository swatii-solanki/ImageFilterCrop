package com.machinetask.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.snackbar.Snackbar;
import com.machinetask.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utility {
    private static Snackbar snackbar;

    public static void showSnackBar(Context mContext, View v, String msg) {

        snackbar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG).setAction(mContext.getString(R.string.go_to_settings), view -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
            intent.setData(uri);
            mContext.startActivity(intent);
            snackbar.dismiss();
        });
        snackbar.setActionTextColor(ContextCompat.getColor(mContext, R.color.purple));
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbar.getView().getLayoutParams();
        params.setMargins(12, 12, 12, 12);
        snackbar.getView().setLayoutParams(params);

        TextView message = snackbar.getView().findViewById(R.id.snackbar_text);
        TextView action = snackbar.getView().findViewById(R.id.snackbar_action);
        message.setMaxLines(3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            message.setTextAppearance(R.style.snackBarStyle);
            action.setTextAppearance(R.style.snackBarActionStyle);
        }
        snackbar.show();
    }

    public static Bitmap getBitmapFromPath(String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(url, options);
    }

    public static Bitmap scaleDown(Bitmap realImage) {
        float ratio = Math.min(1000.0F / (float) realImage.getWidth(), 1000.0F / (float) realImage.getHeight());
        int width = Math.round(ratio * (float) realImage.getWidth());
        int height = Math.round(ratio * (float) realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, true);
    }

    public static String getImagePathFromBitmap(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "FilterImage", null);
    }

    @Nullable
    public static Bitmap getBitmapFromContentUri(ContentResolver contentResolver, Uri imageUri, boolean flip)
            throws IOException {
        Bitmap decodedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
        if (decodedBitmap == null) {
            return null;
        }

        int orientation = getExifOrientationTag(contentResolver, imageUri);
        int rotationDegrees;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_TRANSPOSE:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                rotationDegrees = -90;
                break;
            default:
                rotationDegrees = 0;
                break;
        }

        return rotateBitmap(decodedBitmap, rotationDegrees, flip, false);
    }

    private static int getExifOrientationTag(ContentResolver resolver, Uri imageUri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme()) && !ContentResolver.SCHEME_FILE.equals(imageUri.getScheme())) {
            return 0;
        }

        ExifInterface exif;
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            if (inputStream == null) {
                return 0;
            }

            exif = new ExifInterface(inputStream);
        } catch (IOException e) {
            Log.e("TAG", "failed to open file to read rotation meta data: " + imageUri, e);
            return 0;
        }

        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipHorizontally, boolean flipVertically) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        matrix.postScale(flipHorizontally ? -1 : 1, flipVertically ? -1 : 1);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }


    public static File saveBitmapToFile(Context context, Bitmap croppedImage) {
        File file = new File(context.getExternalFilesDir("") + "/" + System.currentTimeMillis() + Constant.PHOTO_EXTENSION);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        croppedImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        try {
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static void addImageToGallery(Context context, ImageView imageView) throws IOException {
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/MachineTask");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        outStream = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        context.sendBroadcast(intent);
    }
}

