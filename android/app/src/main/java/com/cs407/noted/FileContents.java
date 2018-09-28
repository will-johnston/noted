package com.cs407.noted;

import java.util.List;

public class FileContents {
    String data;
    String owner;
    List<String> members;

    public FileContents(String data, String owner, List<String> members) {
        this.data = data;
        this.owner = owner;
        this.members = members;
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
}
