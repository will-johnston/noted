package com.cs407.noted;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DataFormatter {
    public List<File> getFileData(DataSnapshot dataSnapshot, File root) {
        if (dataSnapshot == null) {
            return null;
        }
        List<File> files = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            File file = convertSnapShotToFile(ds);
            updateParents(file, root);
            files.add(file);
        }
        root.setChildren(files);
        return files;
    }

    private void updateParents(File file, File parent) {
        if (file != null && parent != null) {
            file.setParent(parent);
            if (file.getChildren() != null) {
                for (File child : file.getChildren()) {
                    updateParents(child, file);
                }
            }
        }
    }

    private File convertSnapShotToFile(DataSnapshot ds) {
        String title = (String) ds.child("title").getValue();
        String id = (String) ds.getKey();
        String lastEditedBy = (String) ds.child("lastEditedBy").getValue();
        String type = (String) ds.child("type").getValue();

        HashMap rawChildren = (HashMap) ds.child("children").getValue();  // this is the hash map we get from firebase
        List<File> children = null;  // this will be the formatted data if there are any children
        if (rawChildren != null) {
            children = recursivelyGetChildren(rawChildren);
        }

        return new File(id, title, lastEditedBy, type, children);
    }

    private List<File> recursivelyGetChildren(HashMap fileList) {
        if (fileList == null) {
            return null;
        }

        Set keySet = fileList.keySet();
        List<File> list = new ArrayList<>();
        for (Object keyObj: keySet) {
            String id = keyObj.toString();
            HashMap file = (HashMap) fileList.get(id);
            String title = (String) file.get("title");
            String type = (String) file.get("type");
            String lastEditedBy = (String) file.get("lasEditedBy");
            HashMap rawChildren = (HashMap) file.get("children");
            if (rawChildren != null) {
                List<File> children = recursivelyGetChildren(rawChildren);
                list.add(new File(id, title, lastEditedBy, type, children));
            } else {
                list.add(new File(id, title, lastEditedBy, type, null));
            }
        }
        return list;
    }
}
