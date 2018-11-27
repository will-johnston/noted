package com.cs407.noted;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {
    private List<Notification> data;
    private String user;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView textView;
        private ImageButton imageButton;

        public MyViewHolder(View v, final NotificationAdapter adapter) {
            super(v);
            textView = (TextView) v.findViewById(R.id.notification_title);
            imageButton = (ImageButton) v.findViewById(R.id.notification_remove);

            imageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    adapter.removeNotification(pos);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationAdapter(List<Notification> data, String user) {
        this.data = data;
        this.user = user;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
       View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v, this);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText(data[position]);

        holder.textView.setText(data.get(position).text);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }

    private void removeNotification(int index) {
        String id = data.get(index).id;
        String path = String.format("users/%s/notifications/%s", user, id);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);
        ref.removeValue();
        data.remove(index);
        notifyDataSetChanged();
    }
}
