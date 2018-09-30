# filesystem.service::startSubscription()
This one function is getting so complicated, I'm expanding it's documentation

## Paths
- All objects stored in children[], notes[], or folders[] arrays have paths given by Path.toString().
- createNote(), createFolder(), and updating ids use Path.toInsertString() to update object data within firebase.

## Action
An action is the object given by snapshotChanges() whenever the database changes. It has three properties; type, key, and payload. startSubscription() handles two of the four types: 'value' and 'child_changed'. Due to the listener's setup, 'child_added' and 'child_removed' are never called. The payload comes with some metadata about the database data and the data itself. We use payload.val() to get the actual data and ignore the reset of the metadata. 

### value
This is the vast majority of actions performed, it simply contains values (payload) that we must store in the filesystem. 
#### Handling

### child_changed
This occurs whenever a note within a folder is created/removed, a folder within a folder is created/removed, or deeper levels still. We can have a folder within a folder within a folder within a folder (4 levels beyond root) be returned as one action. Deleting from firebase also invokes this. 

#### Handling
1. Check if a child has been killed
2. Check if all children have been killed
3. Check if a child has proper ids
4. Update folder with child after id resolve

#### TODO
Figure out how to deal within deep recursive create note within folder within folder. This may be implemented in a different sprint.