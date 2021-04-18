package com.machinetask;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.machinetask.databinding.ActivityMainBinding;
import com.machinetask.ui.activity.CameraActivity;
import com.machinetask.ui.activity.EditPhotoActivity;
import com.machinetask.utils.Constant;
import com.machinetask.utils.FileUtils;
import com.machinetask.utils.Utility;

public class MainActivity extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] REQUIRED_CAMERA_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_PERMISSION = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;
    private static final int PICK_IMAGE = 1003;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        init();
    }

    private void init() {
        binding.btnChooseGallery.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
                return;
            }
            chooseGallery();
        });
        binding.btnCaptureImage.setOnClickListener(v -> {
            if (!cameraPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION);
                return;
            }
            openCamera();
        });
    }

    private boolean allPermissionsGranted() {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean cameraPermissionsGranted() {
        for (String perm : REQUIRED_CAMERA_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) chooseGallery();
            else Utility.showSnackBar(this, binding.layout, getString(R.string.storage_permission));
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (cameraPermissionsGranted()) openCamera();
            else
                Utility.showSnackBar(this, binding.layout, getString(R.string.camera_storage_permission));
        }
    }

    private void chooseGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void openCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    String path = FileUtils.getPathFromURI(this, selectedImageUri);
                    Intent intent = new Intent(this, EditPhotoActivity.class);
                    intent.putExtra(Constant.SELECTED_IMAGE_PATH, path);
                    startActivity(intent);
                }
            }
        }
    }

}