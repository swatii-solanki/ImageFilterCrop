package com.machinetask.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.machinetask.R;
import com.machinetask.databinding.ItemCropBinding;
import com.machinetask.ui.modal.MCrop;

import java.util.ArrayList;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.ViewHolder> {

    private ArrayList<MCrop> list;
    private static int lastSelectedPosition = -1;
    private OnItemCropClickedListener onItemCropClickedListener;
    private Context mContext;

    public void setOnItemCropClickedListener(OnItemCropClickedListener onItemCropClickedListener) {
        this.onItemCropClickedListener = onItemCropClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCropBinding  binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_crop, parent, false);
        mContext = parent.getContext();
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MCrop mCrop = list.get(position);
        Glide.with(mContext)
                .load(mCrop.getImg())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.binding.ivRatio);
        holder.binding.tvRatio.setText(mCrop.getName());
        if (position == lastSelectedPosition) {
            holder.binding.ivRatio.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.light_blue)));
            holder.binding.tvRatio.setTextColor(ContextCompat.getColor(mContext, R.color.light_blue));
        } else {
            holder.binding.tvRatio.setTextColor(ContextCompat.getColor(mContext, R.color.light_grey));
            holder.binding.ivRatio.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.light_grey)));
        }
        holder.binding.ll.setOnClickListener(v -> {
            if (position == lastSelectedPosition) return;
            lastSelectedPosition = position;
            notifyDataSetChanged();
            onItemCropClickedListener.onItemCropClicked(list.get(lastSelectedPosition).getType());
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void setList(ArrayList<MCrop> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCropBinding binding;

        public ViewHolder(@NonNull ItemCropBinding cropBinding) {
            super(cropBinding.getRoot());
            binding = cropBinding;
        }
    }

    public interface OnItemCropClickedListener {
        void onItemCropClicked(MCrop.Type type);
    }
}
