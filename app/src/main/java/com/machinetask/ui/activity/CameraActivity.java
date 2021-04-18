package com.machinetask.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.common.util.concurrent.ListenableFuture;
import com.machinetask.R;
import com.machinetask.databinding.ActivityCameraBinding;
import com.machinetask.utils.Constant;
import com.machinetask.utils.FileUtils;
import com.machinetask.utils.Utility;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private ActivityCameraBinding binding;
    private Context mContext;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private CameraInfo cameraInfo;
    private CameraControl cameraControl;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        init();
    }

    private void init() {
        mContext = this;
        binding.previewView.post(() -> {
            updateCameraUi();
            setUpCamera();
        });
    }

    /* Initialize CameraX, and prepare to bind the camera use cases  */
    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mContext);
        cameraProviderFuture.addListener(() -> {
            try {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get();

                // Select lensFacing depending on the available cameras
                if (hasBackCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_BACK;
                } else if (hasFrontCamera()) {
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
                } else {
                    throw new IllegalStateException("Back and front camera are unavailable");
                }

                // Enable or disable switching between cameras
                updateCameraSwitchButton();

                // Build and bind the camera use cases
                bindCameraUseCases();

                // update flash light
                updateFlashLight();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(mContext));
    }

    /* Declare and bind preview, capture and analysis use cases */
    private void bindCameraUseCases() {

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        binding.previewView.getDisplay().getRealMetrics(metrics);
        Log.d(TAG, "Screen metrics:  Width" + metrics.widthPixels + "Height " + metrics.heightPixels);

        int screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels);
        Log.d(TAG, "Preview aspect ratio: " + screenAspectRatio);

        int rotation = binding.previewView.getDisplay().getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build();

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll();

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis);

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

            cameraInfo = camera.getCameraInfo();
            cameraControl = camera.getCameraControl();
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed" + e);
        }
    }

    private void updateCameraUi() {
        binding.ivCapture.setOnClickListener(v -> {
            File file = new File(getExternalFilesDir("") + "/" + System.currentTimeMillis() + Constant.PHOTO_EXTENSION);
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
            // Create output options object which contains file + metadata
            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(file)
                    .setMetadata(metadata)
                    .build();

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {

                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Uri uri = Uri.fromFile(file);
                    if (uri != null) {
                        String path = FileUtils.getPathFromURI(mContext, uri);
                        Intent intent = new Intent(mContext, EditPhotoActivity.class);
                        intent.putExtra(Constant.SELECTED_IMAGE_PATH, path);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {

                }
            });
        });

        binding.ivSwitch.setOnClickListener(v -> {
            if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                lensFacing = CameraSelector.LENS_FACING_BACK;
            } else {
                lensFacing = CameraSelector.LENS_FACING_FRONT;
            }

            bindCameraUseCases();
        });
    }

    private void updateFlashLight() {
        cameraInfo.getTorchState().observe(this, state -> {
            if (state == TorchState.ON)
                binding.ivFlash.setImageResource(R.drawable.ic_flash_off);
            else
                binding.ivFlash.setImageResource(R.drawable.ic_flash_on);
        });
        binding.ivFlash.setOnClickListener(v -> {
            if (cameraInfo.getTorchState().getValue() != null)
                cameraControl.enableTorch(cameraInfo.getTorchState().getValue() != TorchState.ON);
        });
    }

    private void updateCameraSwitchButton() {
        try {
            binding.ivSwitch.setEnabled(hasBackCamera() && hasFrontCamera());
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
    }

    private boolean hasBackCamera() throws CameraInfoUnavailableException {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
    }

    private boolean hasFrontCamera() throws CameraInfoUnavailableException {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - Constant.RATIO_4_3_VALUE) <= Math.abs(previewRatio - Constant.RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
}