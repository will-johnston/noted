package com.cs407.noted;

import java.util.ArrayList;
import java.util.List;

public class Folder extends ListItem {
    List<ListItem> children;

    public Folder(String title, int iconId, List<ListItem> children, ListItem parent) {
        super(title, iconId, parent);
        this.children = children;
    }

    public Folder(String title, int iconId, ListItem parent) {
        super(title, iconId, parent);
        this.children = new ArrayList<>();
    }

    public List<ListItem> getChildren() {
        return children;
    }

    public void setChildren(List<ListItem> children) {
        this.children = children;
    }
}



