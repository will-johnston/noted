package com.cs407.noted;

import java.util.List;

public class ListItem {
    private String title;
    private int iconId;
    private ListItem parent;

    public ListItem(String title, int iconId, ListItem parent) {
        this.title = title;
        this.iconId = iconId;
        this.parent = parent;
    }

    public ListItem(String title, int iconId) {
        this.title = title;
        this.iconId = iconId;
    }

    public ListItem(String title) {
        this.title = title;
    }


    // getters and setters
    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParent(ListItem parent) {
        this.parent = parent;
    }

    public ListItem getParent() {
        return parent;
    }
}
