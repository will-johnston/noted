package com.cs407.noted;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DataFormatter {
    public List<FileOld> getFileData(DataSnapshot dataSnapshot, FileOld root) {
        if (dataSnapshot == null) {
            return null;
        }
        List<FileOld> fileOlds = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            FileOld fileOld = convertSnapShotToFile(ds);
            updateParents(fileOld, root);
            fileOlds.add(fileOld);
        }
        root.setChildren(fileOlds);
        return fileOlds;
    }

    private void updateParents(FileOld fileOld, FileOld parent) {
        if (fileOld != null && parent != null) {
            fileOld.setParent(parent);
            if (fileOld.getChildren() != null) {
                for (FileOld child : fileOld.getChildren()) {
                    updateParents(child, fileOld);
                }
            }
        }
    }

    private FileOld convertSnapShotToFile(DataSnapshot ds) {
        String title = (String) ds.child("title").getValue();
        String id = (String) ds.getKey();
        String lastEditedBy = (String) ds.child("lastEditedBy").getValue();
        String type = (String) ds.child("type").getValue();

        HashMap rawChildren = (HashMap) ds.child("children").getValue();  // this is the hash map we get from firebase
        List<FileOld> children = null;  // this will be the formatted data if there are any children
        if (rawChildren != null) {
            children = recursivelyGetChildren(rawChildren);
        }

        return new FileOld(id, title, lastEditedBy, type, children);
    }

    private List<FileOld> recursivelyGetChildren(HashMap fileList) {
        if (fileList == null) {
            return null;
        }

        Set keySet = fileList.keySet();
        List<FileOld> list = new ArrayList<>();
        for (Object keyObj: keySet) {
            String id = keyObj.toString();
            HashMap file = (HashMap) fileList.get(id);
            String title = (String) file.get("title");
            String type = (String) file.get("type");
            String lastEditedBy = (String) file.get("lasEditedBy");
            HashMap rawChildren = (HashMap) file.get("children");
            if (rawChildren != null) {
                List<FileOld> children = recursivelyGetChildren(rawChildren);
                list.add(new FileOld(id, title, lastEditedBy, type, children));
            } else {
                list.add(new FileOld(id, title, lastEditedBy, type, null));
            }
        }
        return list;
    }
}
