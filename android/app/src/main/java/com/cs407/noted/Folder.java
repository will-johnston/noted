package com.cs407.noted;

import java.util.List;

public class Folder extends ListItem {
    List<ListItem> items;

    public Folder(String title, int iconId, List<ListItem> items) {
        super(title, iconId);
        this.items = items;
    }


    public List<ListItem> getItems() {
        return items;
    }

    public void setItems(List<ListItem> items) {
        this.items = items;
    }

    // Purpose of subclass:
    /*if (obj instanceof Folder) {
        refresh recyclerview with its contents
        add back button to take back to previous contents
    }*/

}



