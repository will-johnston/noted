package com.cs407.noted;

import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
    private List<ListItem> itemList;
    private Folder parent;
    Context context;

    public ListAdapter(List<ListItem> itemList, Folder parent) {
        this.itemList = itemList == null ? new ArrayList<ListItem>() : itemList;
        this.parent = parent;
    }

    public List<ListItem> getItemList() {
        return itemList;
    }

    public void goToParentDirectory() {
        // we need to go up two levels, then get the top level's children
        Log.e("got to parent function", "got to parent function");
        if (parent != null) {
            if (parent instanceof Folder) {
                Log.e("parent dir", "parent not null & is a folder");
                ListItem grandparent = parent.getParent();
                if (grandparent != null) {
                    if (grandparent instanceof Folder) {
                        Log.e("grandparent dir", "grandparent not null & is a folder");
                        // this is the list we want to load
                        List<ListItem> parent_and_siblings = ((Folder) grandparent).children;
                        setItemList(parent_and_siblings);

                        // check to see if we need to toggleHomeButton
                        if (context instanceof MainActivity) {
                            if (grandparent.getParent() == null) {
                                // we are at the root directory
                                ((MainActivity) context).toggleHomeButton(false);
                            }
                            ((MainActivity) context).changeActionBarTitle(grandparent.getTitle());
                        }
                        // change parent node
                        this.parent = (Folder) grandparent;
                    }
                }
            }
        }
    }

    public void setItemList(List<ListItem> itemList) {
        this.itemList = itemList == null ? new ArrayList<ListItem>() : itemList;
        this.notifyDataSetChanged();
    }

    public void addItemToList(ListItem item) {
        item.setParent(this.parent);
        // this.parent.addChild(item);
        itemList.add(item);
        parent.setChildren(itemList);
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
                // get list item at holder position
                int position = holder.getAdapterPosition();
                ListItem item = itemList.get(position);

                // if type is folder, change list to list item's children
                if (item instanceof Folder) {
                    Toast.makeText(context, "folder!", Toast.LENGTH_SHORT).show();
                    List<ListItem> children = ((Folder) item).getChildren();
                    setItemList(children);
                    parent = (Folder) item;
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).toggleHomeButton(true);
                        ((MainActivity) context).changeActionBarTitle(item.getTitle());
                    }

                }
                else if(item instanceof Document) {
                    Intent intent = new Intent(context, DocumentActivity.class);
                    context.startActivity(intent);
                }
                else if(item instanceof Image) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    context.startActivity(intent);
                }
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
        }
    }
}
