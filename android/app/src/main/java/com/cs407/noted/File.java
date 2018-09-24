package com.cs407.noted;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class File implements Comparable<File> {
    private String id;
    private String parent_id;
    private String title;
    private String lastEditedBy;
    private String type;  // using type instead of inheritance to make Firebase easier to manage
    private Map<String, File> children;

    /* these attributes are local only, so their getters and setters are 'Excluded' */
    private File parent; // we get the parent in order to traverse back up the file system easily
    private boolean hasListener;  // necessary to keep track of all levels of filesystem

    /* Empty constructor so firebase can initialize the object */
    public File() {
        this.hasListener = false;
    }

    public File(String id, String parent_id, String title, File parent, String lastEditedBy, String type, Map<String, File> children) {
        this.id = id;
        this.title = title;
        this.parent = parent;
        this.lastEditedBy = lastEditedBy;
        this.type = type;
        this.hasListener = false;
        if (children == null) {
            this.children = new HashMap<>();
        } else {
            this.children = children;
        }
    }

    @Override
    public int compareTo(@NonNull File o) {
        return this.getId().compareTo(o.getId());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    @Exclude
    public File getParent() {
        return parent;
    }
    @Exclude
    public void setParent(File parent) {
        this.parent = parent;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, File> getChildren() {
        return children;
    }

    public void setChildren(Map<String, File> children) {
        this.children = children;
    }

    public void addChild(File file) {
        children.put((String) file.getId(), file);
    }
    @Exclude
    public boolean hasListener() {
        return hasListener;
    }
    @Exclude
    public void setHasListener(boolean hasListener) {
        this.hasListener = hasListener;
    }
    @Exclude
    public boolean isHasListener() {
        return hasListener;
    }


}
