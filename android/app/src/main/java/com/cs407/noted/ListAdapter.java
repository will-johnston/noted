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

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
    private List<File> fileList;
    private File parent;
    Context context;

    public ListAdapter(List<File> fileList, File parent) {
        this.fileList = fileList == null ? new ArrayList<File>() : fileList;
        this.parent = parent;
    }

    public List<File> getItemList() {
        return fileList;
    }

//    public void goToParentDirectory() {
//        // we need to go up two levels, then get the top level's children
//        if (parent != null) {
//            if (parent.getType().equals(FileType.FOLDER.toString())) {
//                FileOld grandparent = parent.getParent();
//                if (grandparent != null) {
//                    if (grandparent.getType().equals(FileType.FOLDER.toString())) {
//                        // this is the list we want to load
//                        List<FileOld> parent_and_siblings = grandparent.getChildren();
//                        setItemList(parent_and_siblings);
//
//                        // check to see if we need to toggleHomeButton
//                        if (context instanceof MainActivity) {
//                            if (grandparent.getParent() == null) {
//                                // we are at the root directory
//                                ((MainActivity) context).toggleHomeButton(false);
//                            }
//                            ((MainActivity) context).changeActionBarTitle(grandparent.getTitle());
//                        }
//                        // change parent node
//                        this.parent = grandparent;
//                    }
//                }
//            }
//        }
//    }

    public void setItemList(List<File> fileList) {
        if (this.fileList == null) {
            this.fileList = new ArrayList<File>();
        } else {
            this.fileList.clear();
            this.fileList.addAll(fileList);
        }
        this.notifyDataSetChanged();
    }


    public void addFileToView(File file) {
        // if item's parent is root, we need to add the child to the root locally
        // since it's not in firebase
        if (this.parent.getId().equals("root")) {
            this.parent.addChild(file);
        }

        // only update the list if the file should be in this directory
        if (this.parent.getId().equals(file.getParent().getId())) {
            fileList.add(file);
            this.notifyDataSetChanged();
        }
    }


    public void addNewFile(File file, DatabaseReference ref) {
        // update file
        file.setParent(this.parent);
        String key = ref.push().getKey();
        file.setId(key);
        ref.child(key).setValue(file);
        if (!this.parent.getId().equals("root")) {
            // we want to update the parent's children here

            // this will start a childChanged call on the main activity
        }


    }



//    public void removeItemFromList(FileOld item) {
//        fileList.remove(item);
//        this.notifyDataSetChanged();
//    }

    public List<FileOld> getCorrectFileList(List<FileOld> files) {
        if (files == null) {
            return null;
        }
        String parentId = parent.getId();
        for (FileOld file: files) {
            if (file.getId().equals(parentId)) {
                return file.getChildren();
            } else {
                // see if we found it in deeper layer
                List<FileOld> list = getCorrectFileList(file.getChildren());
                if (list != null) {
                    return list;
                }
            }
        }
        return null;
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
        File file = fileList.get(position);
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
                File file = fileList.get(position);

                // if type is folder, change list to list file's children
                if (file.getType().equals(FileType.FOLDER.toString())) {
                    Toast.makeText(context, "Folder!", Toast.LENGTH_SHORT).show();
                    List<File> children;
                    if (file.getChildren() != null) {
                        children = new ArrayList<>();
                        children.addAll(file.getChildren().values());
                    } else {
                        children = new ArrayList<>();
                    }
                    setItemList(children);
                    parent = file;



                    if (context instanceof MainActivity) {
                        // update toolbar
                        ((MainActivity) context).toggleHomeButton(true);
                        ((MainActivity) context).changeActionBarTitle(file.getTitle());

                        // update firebase reference
                        ((MainActivity) context).updateDatabaseRefForward(file.getId());
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
                        fileList.remove(position);
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

    public int getIconId(String type) {
        switch (type) {
            case "FOLDER":
                return R.drawable.folder;
            case "DOCUMENT":
                return R.drawable.file;
            case "IMAGE":
                return R.drawable.image;
            default:
                return R.drawable.file;
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
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
