package com.cs407.noted;

import java.util.List;

public class FileContents {
    private String data;
    private String owner;
    private List<String> members;
    private String lastEditedBy;

    public FileContents(String data, String owner, List<String> members, String lastEditedBy) {
        this.data = data;
        this.owner = owner;
        this.members = members;
        this.lastEditedBy = lastEditedBy;
    }

    public FileContents() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }
}
