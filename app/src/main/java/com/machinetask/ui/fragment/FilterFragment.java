package com.machinetask.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.machinetask.R;
import com.machinetask.databinding.FragmentFilterBinding;
import com.machinetask.utils.Constant;
import com.machinetask.utils.FileUtils;
import com.machinetask.utils.Loader;
import com.machinetask.utils.Utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FilterFragment extends Fragment {

    private FragmentFilterBinding binding;
    private String selectedImagePath;
    private Loader loader;

    private static boolean filterImage = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_filter, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        loader = new Loader(requireContext());
        if (getArguments() != null) {
            selectedImagePath = getArguments().getString(Constant.SELECTED_IMAGE_PATH);
            Glide.with(this)
                    .load(selectedImagePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.iv);
        }
        clickListener();
        filterSeekBar();
    }

    private void filterSeekBar() {
        binding.sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterImage = true;
                binding.iv.setColorFilter(filter(progress, binding.sbContrast.getProgress(), binding.sbSaturation.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.sbContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterImage = true;
                binding.iv.setColorFilter(filter(binding.sbBrightness.getProgress(), progress, binding.sbSaturation.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.sbSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterImage = true;
                binding.iv.setColorFilter(filter(binding.sbBrightness.getProgress(), binding.sbContrast.getProgress(), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void clickListener() {
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnSave.setOnClickListener(v -> {
            loader.show();
            new Thread(() -> {
                String path = "";
                if (filterImage) {
                    Bitmap original = Utility.getBitmapFromPath(selectedImagePath);
                    Bitmap filter = bitmapFilter(original, binding.sbBrightness.getProgress(), binding.sbContrast.getProgress(), binding.sbSaturation.getProgress());
                    String uri = Utility.getImagePathFromBitmap(requireContext(), filter);
                    path = FileUtils.getPathFromURI(requireContext(), Uri.parse(uri));
                } else {
                    path = selectedImagePath;
                }
                Bundle bundle = new Bundle();
                bundle.putString(Constant.SELECTED_IMAGE_PATH, path);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_filterFragment_to_saveImageFragment, bundle);
                loader.dismiss();
            }).start();
        });
    }

//    public static ColorMatrix setBrightness(int fb) {
//        ColorMatrix colorMatrix = new ColorMatrix();
//        colorMatrix.set(new float[]{
//                1, 0, 0, 0, fb,
//                0, 1, 0, 0, fb,
//                0, 0, 1, 0, fb,
//                0, 0, 0, 1, 0});
//
//        return colorMatrix;
//    }

    public static ColorMatrix setBrightnessContrast(float b, float c) {
        ColorMatrix matrix = new ColorMatrix();
        b = b - 100;
        c = c / 10f;
        float[] array = new float[]{
                c, 0, 0, 0, b,
                0, c, 0, 0, b,
                0, 0, c, 0, b,
                0, 0, 0, 1, 0};

        matrix.set(array);
        return matrix;
    }

    public static ColorMatrixColorFilter filter(int b, int c, int s) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.postConcat(setBrightnessContrast(b, c));
        matrix.postConcat(setSaturation(s));
        return new ColorMatrixColorFilter(matrix);
    }

    public static ColorMatrix setSaturation(float saturation) {
        float sat = saturation / 256;
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(sat);
        return cm;
    }


    public Bitmap bitmapFilter(Bitmap bmp, int b, int c, int s) {
        File file = new File(requireContext().getExternalFilesDir("") + "/" + System.currentTimeMillis() + Constant.PHOTO_EXTENSION);
        ColorMatrix matrix = new ColorMatrix();
        matrix.postConcat(setBrightnessContrast(b, c));
        matrix.postConcat(setSaturation(s));
        Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bmp, 0, 0, paint);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}