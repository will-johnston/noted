package com.cs407.noted;

import java.util.List;

public class File {
    private String id;
    private String title;
    private File parent;
    private String lastEditedBy;
    private String type;  // using type instead of inheritance to make Firebase easier to manage
    private List<File> children;

    public File(String id, String title, String lastEditedBy, String type, List<File> children) {
        this.id = id;
        this.title = title;
        this.lastEditedBy = lastEditedBy;
        this.type = type;
        this.children = children;
    }


    public File(String title, File parent, String type, List<File> children) {
        this.title = title;
        this.parent = parent;
        this.type = type;
        this.children = children;
    }

    public File(String title, File parent, String type) {
        this.title = title;
        this.parent = parent;
        this.type = type;
    }

    public File(String title, String type) {
        this.title = title;
        this.type = type;
    }

    public File() {
    }


    // getters and setters
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

    public List<File> getChildren() {
        return children;
    }

    public void setChildren(List<File> children) {
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

