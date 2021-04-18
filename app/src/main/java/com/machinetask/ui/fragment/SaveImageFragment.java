package com.machinetask.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.machinetask.R;
import com.machinetask.databinding.FragmentSaveImageBinding;
import com.machinetask.utils.Constant;
import com.machinetask.utils.Utility;

import java.io.IOException;

public class SaveImageFragment extends Fragment {

    private FragmentSaveImageBinding binding;
    private String selectedImagePath;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_save_image, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        if (getArguments() != null) {
            selectedImagePath = getArguments().getString(Constant.SELECTED_IMAGE_PATH);
            Glide.with(this)
                    .load(selectedImagePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.iv);
        }

        binding.btnSave.setOnClickListener(v -> new Thread(() -> {
            try {
                Utility.addImageToGallery(requireContext(), binding.iv);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), getString(R.string.save_image_success), Toast.LENGTH_LONG).show();
                    requireActivity().finish();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start());
    }

}