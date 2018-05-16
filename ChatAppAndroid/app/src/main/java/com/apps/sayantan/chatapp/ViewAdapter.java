package com.apps.sayantan.chatapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter {

    private List<ItemDataModel> models;
    private Context context;

    ViewAdapter(List<ItemDataModel> modelClasses) {
        models = modelClasses;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        context = parent.getContext();
        return new ItemDataBinder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemDataBinder) holder).bindData(models.get(position), context);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.list_item;
    }
}
