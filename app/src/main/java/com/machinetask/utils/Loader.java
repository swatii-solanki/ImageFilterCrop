package com.machinetask.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.databinding.DataBindingUtil;

import com.machinetask.R;
import com.machinetask.databinding.ItemLoaderBinding;


public class Loader {

    private ItemLoaderBinding binding;
    private Dialog dialog;
    private Context mContext;

    public Loader(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = DataBindingUtil.inflate(inflater, R.layout.item_loader, null, false);
        mContext = context;
        try {
            dialog = new Dialog(mContext);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(binding.getRoot());
//            dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
//            dialog.getWindow().setLayout(-1, -2);
            dialog.setCancelable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(String msg) {
//        binding.txtMessage.setVisibility(View.VISIBLE);
//        binding.txtMessage.setText(msg);
        dialog.show();
    }

    public void show() {
        binding.txtMessage.setVisibility(View.GONE);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
