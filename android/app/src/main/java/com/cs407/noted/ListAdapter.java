package com.cs407.noted;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
    private List<File> itemList;
    private File parent;
    Context context;

    public ListAdapter(List<File> itemList, File parent) {
        this.itemList = itemList == null ? new ArrayList<File>() : itemList;
        this.parent = parent;
    }

    public List<File> getItemList() {
        return itemList;
    }

    public void goToParentDirectory() {
        // we need to go up two levels, then get the top level's children
        if (parent != null) {
            if (parent.getType() == FileType.FOLDER) {
                File grandparent = parent.getParent();
                if (grandparent != null) {
                    if (grandparent.getType() == FileType.FOLDER) {
                        // this is the list we want to load
                        List<File> parent_and_siblings = grandparent.getChildren();
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
                        this.parent = grandparent;
                    }
                }
            }
        }
    }

    public void setItemList(List<File> itemList) {
        this.itemList = itemList == null ? new ArrayList<File>() : itemList;
        this.notifyDataSetChanged();
    }

    public void addItemToList(File item) {
        item.setParent(this.parent);
        // this.parent.addChild(item);
        itemList.add(item);
        parent.setChildren(itemList);
        this.notifyDataSetChanged();
    }

    public void removeItemFromList(File item) {
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
        File file = itemList.get(position);
        holder.title.setText(file.getTitle());
        holder.icon.setImageResource(getIconId(file.getType()));
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
                File item = itemList.get(position);

                // if type is folder, change list to list item's children
                if (item.getType() == FileType.FOLDER) {
                    Toast.makeText(context, "Folder!", Toast.LENGTH_SHORT).show();
                    List<File> children = item.getChildren();
                    setItemList(children);
                    parent = item;
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).toggleHomeButton(true);
                        ((MainActivity) context).changeActionBarTitle(item.getTitle());
                    }

                } else {
                    Toast.makeText(context, "Not folder!", Toast.LENGTH_SHORT).show();
                }

                // if type is document, load rich text editor

                // if type is image, load image
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

    public int getIconId(FileType type) {
        switch (type) {
            case FOLDER:
                return R.drawable.folder;
            case DOCUMENT:
                return R.drawable.file;
            case IMAGE:
                return R.drawable.image;
            default:
                return R.drawable.file;
        }
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
