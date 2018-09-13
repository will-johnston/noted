package com.cs407.noted;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    private List<ListItem> itemList;
    Context context;


    public ListAdapter(List<ListItem> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    public List<ListItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<ListItem> itemList) {
        this.itemList.clear();
        this.itemList.addAll(itemList);
        this.notifyDataSetChanged();
    }

    public void addItemToList(ListItem item) {
        itemList.add(item);
        Toast.makeText(context, "added item", Toast.LENGTH_SHORT).show();
        Log.e("added text", "added text");
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ListItem listItem = itemList.get(position);
        holder.title.setText(listItem.getTitle());
        holder.icon.setImageResource(listItem.getIconId());

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listTitle);
            icon = itemView.findViewById(R.id.listIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
        }
    }
}
