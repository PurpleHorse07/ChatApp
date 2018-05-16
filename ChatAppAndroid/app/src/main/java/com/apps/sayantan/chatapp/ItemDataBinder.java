package com.apps.sayantan.chatapp;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

class ItemDataBinder extends RecyclerView.ViewHolder {

    private ImageView img;
    private TextView msg;
    private TextView name;

    ItemDataBinder(View itemView) {
        super(itemView);
        img = itemView.findViewById(R.id.photo);
        msg = itemView.findViewById(R.id.message);
        name = itemView.findViewById(R.id.name);
    }

    void bindData(final ItemDataModel viewModel, final Context context) {
        msg.setText(viewModel.getMessage());
        name.setText(viewModel.getName());
        if(viewModel.getPhotoUrl()!=null){
            Glide.with(context)
                    .load(viewModel.getPhotoUrl())
                    .thumbnail(0.1f)
                    .into(img);
        }
    }
}
