package com.cs407.noted;

import java.util.List;

public class FileOld {
    private String id;
    private String title;
    private FileOld parent;
    private String lastEditedBy;
    private String type;  // using type instead of inheritance to make Firebase easier to manage
    private List<FileOld> children;

    public FileOld(String id, String title, String lastEditedBy, String type, List<FileOld> children) {
        this.id = id;
        this.title = title;
        this.lastEditedBy = lastEditedBy;
        this.type = type;
        this.children = children;
    }


    public FileOld(String title, FileOld parent, String type, List<FileOld> children) {
        this.title = title;
        this.parent = parent;
        this.type = type;
        this.children = children;
    }

    public FileOld(String title, FileOld parent, String type) {
        this.title = title;
        this.parent = parent;
        this.type = type;
    }

    public FileOld(String title, String type) {
        this.title = title;
        this.type = type;
    }

    public FileOld() {
    }


    // getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FileOld getParent() {
        return parent;
    }

    public void setParent(FileOld parent) {
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

    public List<FileOld> getChildren() {
        return children;
    }

    public void setChildren(List<FileOld> children) {
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

