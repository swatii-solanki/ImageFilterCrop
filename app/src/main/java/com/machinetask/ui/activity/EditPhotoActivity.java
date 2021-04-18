package com.machinetask.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.machinetask.R;
import com.machinetask.databinding.ActivityEditPhotoBinding;
import com.machinetask.utils.Constant;

public class EditPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.machinetask.databinding.ActivityEditPhotoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_photo);
        init();
    }

    private void init() {
        if (getIntent().hasExtra(Constant.SELECTED_IMAGE_PATH)) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            assert navHostFragment != null;
            NavController navController = navHostFragment.getNavController();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.SELECTED_IMAGE_PATH, getIntent().getStringExtra(Constant.SELECTED_IMAGE_PATH));
            navController.setGraph(navController.getGraph(), bundle);
        }
    }
}