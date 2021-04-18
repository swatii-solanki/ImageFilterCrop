package com.machinetask.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.machinetask.R;
import com.machinetask.databinding.FragmentEditPhotoBinding;
import com.machinetask.utils.Constant;

public class EditPhotoFragment extends Fragment {

    private FragmentEditPhotoBinding binding;
    private String selectedImagePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedImagePath = getArguments().getString(Constant.SELECTED_IMAGE_PATH);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_edit_photo, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        Glide.with(this)
                .load(selectedImagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.iv);

        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.btnCrop.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.SELECTED_IMAGE_PATH, selectedImagePath);
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_editPhotoFragment_to_cropFragment, bundle);
        });
    }
}