package com.cs407.noted;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    private List<ListItem> itemList;
    Context context;


    public ListAdapter(List<ListItem> itemList) {
        this.itemList = itemList;
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

    public void removeItemFromList(ListItem item) {
        itemList.remove(item);
        this.notifyDataSetChanged();

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.custom_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        ListItem listItem = itemList.get(position);
        holder.title.setText(listItem.getTitle());
        holder.icon.setImageResource(listItem.getIconId());
        final ImageButton button = holder.menuButton;
        holder.menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopup(button, holder);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createPopup(button, holder);
                return true;
            }
        });

    }

    public void createPopup(ImageButton button, final MyViewHolder holder) {
        PopupMenu popup = new PopupMenu(context, button);

        popup.inflate(R.menu.list_actions);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_remove:
                        int position = holder.getAdapterPosition();
                        itemList.remove(position);
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                        return true;
                    default:
                        return false;
                }

            }
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        ImageButton menuButton;
        View listItem;

        public MyViewHolder(View itemView) {
            super(itemView);

            listItem = itemView.findViewById(R.id.listItemView);
            title = itemView.findViewById(R.id.listTitle);
            icon = itemView.findViewById(R.id.listIcon);
            menuButton = itemView.findViewById(R.id.menuButton);
            // itemView.setOnClickListener(this);
        }
    }
}
