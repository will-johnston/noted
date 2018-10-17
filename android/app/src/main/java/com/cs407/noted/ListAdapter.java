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

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


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

    public void goToParentDirectory() {
        // we need to go up two levels, then get the top level's children
        if (parent != null) {
            if (parent.getType().equals(FileType.FOLDER.toString())) {
                File grandparent = parent.getParent();
                if (grandparent != null) {
                    if (grandparent.getType().equals(FileType.FOLDER.toString())) {
                        // this is the list we want to load
                        List<File> parent_and_siblings = new ArrayList<>();
                        parent_and_siblings.addAll(grandparent.getChildren().values());
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



    public void setItemListMaintainCurrentDirectory(List<File> files) {
        if (files.isEmpty()) {
            // we must be at the root directory, but force it anyways
            // TODO: set ref back to root
            setItemList(new ArrayList<File>());

        } else {
            File file = files.get(0);
            File root = file.getParent();  // this will always be the root node of the user
            if (root == null) {
                return; // something is terribly wrong– the root should be initialized
            }
            List<File> children = new ArrayList<>();
            String parent_id = this.parent.getId();
            // find parent, set its children accordingly
            File parent = bfs(parent_id, root);
            if (parent != null) {
                if (parent.getChildren() != null) {
                    children.addAll(parent.getChildren().values());
                    setItemList(children);
                }
            } else {
                setItemList(new ArrayList<File>());
            }
        }
    }

    public File findFile(String id) {
        // get to root directory
        //TODO: see if this changes parent
        File root = parent;
        Log.e("parent before", parent.getId());
        while (!root.getId().equals("root")) {
            root = root.getParent();
        }
        Log.e("parent after", parent.getId());
        return bfs(id, root);
    }

    public void setItemList(List<File> fileList) {
        if (this.fileList == null) {
            this.fileList = new ArrayList<File>();
        } else {
            this.fileList.clear();
            this.fileList.addAll(fileList);
            Collections.sort(this.fileList);
        }
        this.notifyDataSetChanged();
    }


    public void addNewFile(File file, DatabaseReference ref, String uid, DatabaseReference fileContents) {
        // get the key in the database, which will serve as the id of the new file
        String key = ref.push().getKey();

        // set id and parent id to help with file traversal and database listener
        file.setId(key);
        file.setParent_id(this.parent.getId());

        // add file to database (listener will add it to the view)
        try {
            // add file to user's file system
            ref.child(key).setValue(file);
            // set user as owner of the file
            fileContents.child(file.getId()).child("owner").setValue(uid);
        } catch (com.google.firebase.database.DatabaseException e) {
            Toast.makeText(context, "Can't add file, maximum depth exceeded", Toast.LENGTH_SHORT).show();
        }
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
                }
                else if(file.getType().equals(FileType.DOCUMENT.toString())) {
                    Intent intent = new Intent(context, DocumentActivity.class);
                    intent.putExtra("title", file.getTitle());
                    intent.putExtra("id", file.getId());
                    context.startActivity(intent);
                }
                else if(file.getType().equals(FileType.IMAGE.toString())) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    intent.putExtra("title", file.getTitle());
                    intent.putExtra("id", file.getId());
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
                        File file = fileList.get(position);
                        if (context instanceof MainActivity) {
                            // tell main activity to remove file with myRef
                            ((MainActivity) context).removeFile(file, parent);
                        }
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

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    /* utilities */
    private File bfs(String id, File root) {
        Queue<File> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            File current = queue.poll();
            if (current.getId().equals(id)) {
                return current;
            }
            if (current.getChildren() != null) {
                queue.addAll(current.getChildren().values());
            }
        }
        return null;
    }
}
