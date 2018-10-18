package com.cs407.noted;

import ir.mirrajabi.searchdialog.core.Searchable;

public class SearchModel implements Searchable {

    private String title;
    private String id;

    public SearchModel(String title, String id) {
        this.title = title;
        this.id = id;
    }


    @Override
    public String getTitle() {
        return this.title;
    }

    public SearchModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getId() {
        return id;
    }
}
