package com.cs407.noted;

import java.util.HashMap;
import java.util.Map;

public class File {
    private String id;
    private String title;
    private File parent;
    private String lastEditedBy;
    private String type;  // using type instead of inheritance to make Firebase easier to manage
    private Map<String, File> children;

    public File() {
    }

    public File(String id, String title, File parent, String lastEditedBy, String type, Map<String, File> children) {
        this.id = id;
        this.title = title;
        this.parent = parent;
        this.lastEditedBy = lastEditedBy;
        this.type = type;
        if (children == null) {
            this.children = new HashMap<>();
        } else {
            this.children = children;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public File getParent() {
        return parent;
    }

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
        children.put(file.getId(), file);
    }
}
