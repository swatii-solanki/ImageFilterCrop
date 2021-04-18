package com.machinetask.ui.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.machinetask.R;
import com.machinetask.databinding.FragmentCropBinding;
import com.machinetask.ui.adapter.CropAdapter;
import com.machinetask.ui.modal.MCrop;
import com.machinetask.utils.Constant;
import com.machinetask.utils.Loader;
import com.machinetask.utils.Utility;
import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;

import java.io.File;
import java.util.ArrayList;

public class CropFragment extends Fragment implements CropAdapter.OnItemCropClickedListener {

    private FragmentCropBinding binding;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private Loader loader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_crop, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        clickListener();
        loader = new Loader(requireContext());
        mGestureCropImageView = binding.iv.getCropImageView();
        mOverlayView = binding.iv.getOverlayView();
        mGestureCropImageView.setScaleEnabled(true);
        mGestureCropImageView.setRotateEnabled(true);
        mGestureCropImageView.setImageToWrapCropBounds(true);
        mOverlayView.setShowCropGrid(true);
        try {
            if (getArguments() != null) {
                String selectedImagePath = getArguments().getString(Constant.SELECTED_IMAGE_PATH);
                mGestureCropImageView.setImageUri(Uri.fromFile(new File(selectedImagePath)), Uri.fromFile(new File(getActivity().getCacheDir(), Constant.SAMPLE_FILTER_IMAGE_NAME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setRecyclerView();
    }

    private void clickListener() {
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.btnFilter.setOnClickListener(v -> {
            loader.show();
            mGestureCropImageView.cropAndSaveImage(Bitmap.CompressFormat.PNG, 100, new BitmapCropCallback() {
                @Override
                public void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight) {
                    new Thread(() -> {
                        try {
                            Bitmap bitmap = Utility.getBitmapFromContentUri(requireActivity().getContentResolver(), resultUri, false);
                            assert bitmap != null;
                            File file = Utility.saveBitmapToFile(requireContext(), bitmap);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constant.SELECTED_IMAGE_PATH, file.getPath());
                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(R.id.action_cropFragment_to_filterFragment, bundle);
                            loader.dismiss();
                        } catch (Exception e) {
                            loader.dismiss();
                            e.printStackTrace();
                        }
                    }).start();
                }

                @Override
                public void onCropFailure(@NonNull Throwable t) {
                    requireActivity().onBackPressed();
                }
            });
        });
    }

    private void setRecyclerView() {
        ArrayList<MCrop> list = new ArrayList<>();
        CropAdapter cropAdapter = new CropAdapter();
        cropAdapter.setOnItemCropClickedListener(this);
        binding.rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        binding.rv.setAdapter(cropAdapter);
//        new StartSnapHelper().attachToRecyclerView(binding.rv);
        list.add(new MCrop(R.drawable.crop11_selected, "1:1", MCrop.Type.TYPE11));
        list.add(new MCrop(R.drawable.crop23_selected, "2:3", MCrop.Type.TYPE23));
        list.add(new MCrop(R.drawable.crop32_selected, "3:2", MCrop.Type.TYPE32));
        list.add(new MCrop(R.drawable.crop43_selected, "4:3", MCrop.Type.TYPE43));
        list.add(new MCrop(R.drawable.crop34_selected, "3:4", MCrop.Type.TYPE34));
        list.add(new MCrop(R.drawable.crop169_selected, "16:9", MCrop.Type.TYPE169));
        list.add(new MCrop(R.drawable.crop916_selected, "9:16", MCrop.Type.TYPE916));
        cropAdapter.setList(list);
    }

    private void changeCropType(float crop) {
        mOverlayView.setTargetAspectRatio(crop);
        mGestureCropImageView.setTargetAspectRatio(crop);
    }

    @Override
    public void onItemCropClicked(MCrop.Type type) {
        switch (type) {
            case TYPE11:
                changeCropType(1f / 1f);
                break;
            case TYPE23:
                changeCropType(2f / 3f);
                break;
            case TYPE32:
                changeCropType(3f / 2f);
                break;
            case TYPE43:
                changeCropType(4f / 3f);
                break;
            case TYPE34:
                changeCropType(3f / 4f);
                break;
            case TYPE169:
                changeCropType(16f / 9f);
                break;
            case TYPE916:
                changeCropType(9f / 16f);
                break;
        }
    }
}

