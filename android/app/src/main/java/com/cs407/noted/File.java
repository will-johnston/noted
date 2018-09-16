package com.cs407.noted;

import java.util.List;

public class File {
    private String title;
    private File parent;
    private String lastEditedBy;
    private FileType type;  // using type instead of inheritance to make Firebase easier to manage
    private List<File> children;

    public File(String title, File parent, FileType type, List<File> children) {
        this.title = title;
        this.parent = parent;
        this.type = type;
        this.children = children;
    }

    public File(String title, File parent, FileType type) {
        this.title = title;
        this.parent = parent;
        this.type = type;
    }

    public File(String title, FileType type) {
        this.title = title;
        this.type = type;
    }

    // getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParent(File parent) {
        this.parent = parent;
    }

    public File getParent() {
        return parent;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public List<File> getChildren() {
        return children;
    }

    public void setChildren(List<File> children) {
        this.children = children;
    }

}
