package com.cs407.noted;

public class SharedFile {
    String title;
    String path;
    String filePath;
    String noteID;

    // for firebase to accept the object
    public SharedFile() {
    }

    public SharedFile(String title, String path, String filePath, String noteID) {
        this.title = title;
        this.path = path;
        this.filePath = filePath;
        this.noteID = noteID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNoteID() {
        return noteID;
    }

}
