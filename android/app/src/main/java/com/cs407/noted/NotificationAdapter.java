package com.cs407.noted;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private List<String> data;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;

        public MyViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.notification_title);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationAdapter(List<String> data) {
        this.data = data;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
       View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText(data[position]);

        holder.textView.setText(data.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }
}
