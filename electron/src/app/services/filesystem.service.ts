import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { AngularFireDatabase } from '@angular/fire/database';
import * as firebase from 'firebase';
import { Observable } from 'rxjs';
import { Note } from '../note/Note';
import { Folder } from '../homescreen/Folder'
import { Path, PathType } from '../homescreen/Path';
import { UserHelperService } from './userhelper.service';

@Injectable({
  providedIn: 'root'
})
export class FilesystemService {
  private userDetails: firebase.User = null;
  private notes: Note[];         //notes that are in the root level
  private folders: Folder[];     //folders and the rest of the notes
  public currentNotes : Note[];
  public currentFolders : Folder[];
  private currentPath : Path;
  private currentFolder : Folder; 
  public userid : string;
  private userRef : any;
  private values : Observable<any>;
  private subscribed : boolean;

  constructor(private fireDatabase: AngularFireDatabase, private userHelper : UserHelperService) {
    this.notes = Array();
    this.folders = Array();
    this.currentNotes = Array();
    this.currentFolders = Array();
    this.currentPath = new Path(PathType.users);
    /*if (userHelper.currentUser != null) {
      this.userid = userHelper.currentUser.uid;
      this.currentPath = Path.RootPath(this.userid);
      //this.updateCurrentState(['/']);
      this.startSubscription();
    }*/
      firebase.auth().onAuthStateChanged((user) => {
        if (user) {
          console.log("userid : %s", user.uid);
          this.userid = user.uid;
          //this.updateCurrentState(['/']);
          this.currentPath = Path.RootPath(user.uid);
          this.startSubscription();
          this.currentNotes = this.notes;
          this.currentFolders = this.folders;
        }
      });
   }
  /*getNotes() {
      //this.notes.push({ id: 0, name : "note1", folder : null});
      for (var i = 0; i < 10; i++) {
       /* var note = new Note();
        note.folder = null;
        note.id = i.toString();
        note.name = "note " + i;
        this.notes.push(note);*/
      /*}
      return this.notes;
  }*/

  //returns the full Note object for a given id
  //null if the note doesn't exist
  getNote(id : string) {
    for (var i = 0; i < this.notes.length; i++) {
      var note = this.notes[i];
      if (note.id === id) 
        return note;
    }
    for (var i = 0; i < this.folders.length; i++) {
      //'recursively' check folders to see if they contain the note
      var folder = this.folders[i];
      var innerchild = folder.getNote(id);
      if (innerchild == null)
        continue;
      else if (innerchild.id === id) {
        return innerchild;
      }
      else {
        console.error("innerchild (%s/%s) was returned but id's don't match", innerchild.name, innerchild.id);
        console.error("innerchild.id (%s) vs requested id (%s)", innerchild.id, id);
      }
    }
    return null;
  }
  //only works on root level
  //FIXME
  containsNote(id) : boolean {
    for (var i = 0; i < this.notes.length; i++) {
      if (this.notes[i].id === id)
        return true;
    }
    return false;
  }
  //only works on root level
  //FIXME
  containsFolder(id) : boolean {
    for (var i = 0; i < this.folders.length; i++) {
      if (this.folders[i].id === id)
        return true;
    }
    return false;
  }
  //recursive folder retrieval
  getFolder(id : string) : Folder {
    for (var i = 0; i < this.folders.length; i++) {
        if (this.folders[i].id === id)
            return this.folders[i];
        var folder : Folder = this.folders[i].getFolder(id);
        if (folder != null)
            return folder;
    }
    return null;
  }
  inRootDirectory() : boolean {
    return (this.notes == this.currentNotes && this.folders == this.currentFolders);
  }
  //constructs a path of a child given it's parentId (folder id)
  constructPathFromChild(childId : string, parentId : string, parentFolder : Folder) : Path {
    //var path : Path = new Path(PathType.users);
    //path.addUserId(this.userid);
    var path : Path;
    var folder : Folder = parentFolder;
    if (folder == null) {
      //find parentFolder of child
      folder = this.getFolder(parentId);
      if (folder == null) {
        //failed to get parent folder
        console.error("constructPathFromChild() failed to get parentFolder (id=%s) for child (id=%s)", parentId, childId);
        return null;
      }
    }
    path = Path.FromString(folder.path);
    if (path == null) {
      console.error("constructPathFromChild() failed calling Path.FromString() with folder.path=%s", folder.path);
      return null;
    }
    console.log("Parent path: %s", path.toString());
    path.addChild(childId);
    console.log("Path after adding child: %s", path.toString());
    return path;
  }
  //makes a local Note|Folder object from raw firebase data
  makeElement(element : any) : Note | Folder {
    if (element == null) {
      console.error("makeElement() given null element to translate");
      return null;
    }
    if (element.type === "DOCUMENT") {
      var note : Note;
      if (element.parent_id != null) {
        //element is a child, set parentId, set folder, and calculate it's path
        var parentFolder : Folder = this.getFolder(element.parent_id);
        if (parentFolder == null) {
          console.error("makeElement failed to get parentFolder (id=%s) for child (id=%s)", element.parent_id, element.id);
          return null;
        }
        note = new Note(element.title, element.id, this.constructPathFromChild(element.id, element.parent_id, parentFolder).toString(), parentFolder);
      }
      else {
        //element exists in the root directory
        var path : Path = Path.RootPath(this.userid);
        path.addId(element.id);
        note = new Note(element.title, element.id, path.toString(), null);
      }
      return note;
    }
    else if (element.type === "FOLDER") {
      var folder : Folder;
      if (element.parent_id != null) {
        //element is a child, set parentId, set folder, and calculate it's path
        var parentFolder : Folder = this.getFolder(element.parent_id);
        if (parentFolder == null) {
          console.error("makeElement failed to get parentFolder (id=%s) for child (id=%s)", element.parent_id, element.id);
          return null;
        }
        folder = new Folder(element.title, element.id, element.children, this.constructPathFromChild(element.id, element.parent_id, parentFolder).toString(), parentFolder);
      }
      else {
        //element exists in the root directory
        var path : Path = Path.RootPath(this.userid);
        path.addId(element.id);
        folder = new Folder(element.title, element.id, element.children, path.toString(), null);
      }
      return folder;
    }
    else {
      console.error("makeElement() doesn't know how to handle type: %s", element.type);
      return null;
    }
  }
  //resolvePath()
  startSubscription() {
    if (!this.subscribed) {
      this.userRef = this.fireDatabase.list(this.userid);
      /*this.userRef.snapshotChanges(['child_added'])
      .subscribe(action => {
        console.log(action.type);
        console.log(action.key);
        console.log(action.payload);
      });*/
      this.values = this.fireDatabase.list('users/' + this.userid).snapshotChanges();
      this.values.subscribe(actions => {
        //console.log("Action %o", actions);
        if (actions == null) {
          return;
        }
        actions.forEach(action => {
          console.log("Action Type %s", action.type);
          if (action.type === "child_changed") {
            /*if (action.payload.val().id == null) {
              console.log("null id on child_changed, must set id");
              this.fireDatabase.list('users/' + this.userid).update(action.key, {id: action.key});
            }*/
            //console.log("Action type: %s, key: %s, payload: %o", action.type, action.key, action.payload.val());
            //console.log("Action.children: %o, keys: %o", action.payload.val().children, Object.keys(action.payload.val().children));
            
            if (action.payload.val() != null) {
              var folder : Folder = this.getFolder(action.payload.val().id);
              if (folder != null && folder.children != null) {
                //check
                //have all children been deleted?
                //has one child been deleted?
                if (action.payload.val().children == null) {
                  console.log("All children have been killed!");
                  folder.killKids();
                  return;
                }
                var keys = Object.keys(action.payload.val().children);
                console.log("keys.length: %d, children.length: %d", keys.length, folder.children.length);
                if (keys.length < folder.children.length) {
                  console.log("A child has been killed!");
                  folder.killKid(keys);
                  return;
                }
                if (keys.length == folder.children.length) {

                }
              }
              if (action.payload.val().children == null) {
                //no children, so 'child_changed' shouldn't have been called
                console.log("Action: %o", action);
                console.log("payload: %o", action.payload.val());
                var element = this.makeElement(action.payload.val());
                if (element.type === "DOCUMENT") {
                  this.notes.push(element as Note);
                }
                else {
                  this.folders.push(element as Folder);
                }
                return;
              }


              var keys = Object.keys(action.payload.val().children);
              for (var i = 0; i < keys.length; i++) {
                var child = action.payload.val().children[keys[i]];
                console.log("Child Object: %o", child);
                console.log("Parent Object: %o", action.payload.val());
                if (child.id == null || child.parent_id == null) {
                  console.log("must set id");
                  this.fireDatabase.list(this.currentPath.toInsertString()).update(keys[i], { id : keys[i], parent_id : action.payload.val().id});
                }
                else {
                  console.log("ids set, must update folder")
                  //get folder
                  //var folder : Folder = this.getFolder(child.parent_id);
                  var folder_element = folder.containsLocalElement(child.id);
                  if (folder_element != null) {
                    //skip, element already exists
                  }
                  else {
                    //add the element
                    var actualElement : Folder | Note = this.makeElement(child);
                    if (actualElement == null) {
                      console.error("Unable to create element from firebase data");
                    }
                    else if (actualElement.folder == null) {
                      console.error("Failed to resolve parent folder of child from firebase data");
                    }
                    else {
                      actualElement.folder.addChild(actualElement);
                    }
                  }
                }
              }
            }
          }
          if (action.type === "value") {
            //inital add
            var rawElement = action.payload.val();
            console.log("element: %o", rawElement);
            var element : Note | Folder = this.makeElement(rawElement);
            if (element == null) {
              console.error("Unable to create element from firebase data");
            }
            else {
              if (element.type === "DOCUMENT") {
                if (element.folder != null) {
                  //it's a child, add to folder.notes
                  if (!this.containsNote(element.id))
                    element.folder.notes.push(element as Note);
                }
                else {
                  //it exists in the root directory, add to notes
                  if (!this.containsNote(element.id))
                    this.notes.push(element as Note);
                }
              }
              else if (element.type === "FOLDER") {
                if (element.folder != null) {
                  //it's a child, add to folder.folders
                  if (!this.containsFolder(element.id))
                    element.folder.folders.push(element as Folder);
                }
                else {
                  //it exists in the root directory, add to folders
                  if (!this.containsFolder(element.id))
                    this.folders.push(element as Folder);
                }
              }
              else {
                console.error("Created an element of unknown type, unable to handle");
              }
            }
            /*if (element.type === "DOCUMENT") {
              if (!this.containsNote(element.id)) {
                //TODO set proper path
                //TODO add to relevant folder
                var note = new Note(element.title, element.id, 'users/' + this.userid + '/' + element.id + '/', null);
                this.notes.push(note);
              }
            }
            else if (element.type === "FOLDER") {
              //TODO change containsFolder to do recursive search
              if (!this.containsFolder(element.id)) {
                //TODO set proper path
                //TODO add to relevant folder
                var folder = new Folder(element.title, element.id, element.children, 'users/' + this.userid + '/' + element.id + '/', null);
                this.folders.push(folder);
              }
            }
            else {
              console.error("Don't know how to handle type: %s", element.type);
            }*/
          }
          else if (action.type === "child_added") {
              //must set id
              console.log("Called child_added which doesn't really matter");
              /*console.log("called child_added");
              if (action.payload.val().id == null) {
                console.log("null id on insert, must set id");
                this.fireDatabase.list('users/' + this.userid).update(action.key, {id: action.key});
              }*/
              console.log("Action: %o", action);
              var child = action.payload.val();
                console.log("Child Object: %o", child);
                if (child.id == null) {
                  console.log("must set id, currentpath: %o", this.currentPath);
                  this.fireDatabase.list(this.currentPath.toString()).update(action.key, { id : action.key});
                }
          }
          else if (action.type === "child_removed") {
            console.log("Called child_removed which doesn't really matter");
            //this doesn't get called by firebase for some reason
            //console.log("remove that shit");
          }
          else {
            /*
              Paths are the full database path to retrieving an object
              objects here at the root path (users/{userid}/)
              So an object will have it's path be (users/{userid}/{elementId})
              Paths should always be terminated with a forward slash (users/abcd/elementid343/)
              Child paths are different in that there is also the additional value (users/abcd/elementid232/children/childelement4324)
              If using the firebase cli (specifically database:get) use a forward slash at the start of the path (/users/{userid}/)
            */
            
          }
        });
      });
      this.subscribed = true;
    }
  }
  createNote(name : string) {
    //this.fireDatabase.list('users/' + this.userid).push({ title : name, type : "DOCUMENT", id : null});
    console.log("Current path: %o", this.currentPath.list);
    if (this.currentPath.addedChild) {
      this.fireDatabase.list(this.currentPath.toInsertString()).push({ title : name, type : "DOCUMENT", id : null});
    }
    else {
      this.fireDatabase.list(Path.RootPath(this.userid).toString()).push({ title : name, type : "DOCUMENT", id : null});
    }
  }
  createFolder(name : string) {
    //this.fireDatabase.list('users/' + this.userid).push({ title : name, type : "FOLDER", children : null});
    if (this.currentPath.addedChild)
      this.fireDatabase.list(this.currentPath.toInsertString()).push({ title : name, type : "FOLDER", children : null});
    else
      this.fireDatabase.list(Path.RootPath(this.userid).toString()).push({ title : name, type : "FOLDER", children : null});
  }
  deleteNote(note : Note) : boolean {
    var noteRef = this.fireDatabase.object(note.path);
    var noteFileRef = this.fireDatabase.object("fileContents/" + note.id);
    noteRef.remove();
    noteFileRef.remove();
    return this.deleteLocalNote(note);
  }
  deleteNoteFromId(id : string) : boolean {
    var note = this.getNote(id);
    if (note == null) {
      console.error("Unable to retrieve note in the local file system");
      return false;
    }
    this.deleteNote(note);
  }
  //deletes the local copy of the note
  deleteLocalNote(note : Note) : boolean {
    var actualNote = this.getNote(note.id);
    var folder = actualNote.folder;
    if (folder == null) {
      //remove from the root folder
      for (var i = 0; i < this.notes.length; i++) {
        var noteRef = this.notes[i];
        if (noteRef.id === note.id) {
          this.notes.splice(i, 1);
          return true;
        }
      }
      console.error("Failed to find note in root level, couldn't deleteNote");
      return false;
    }
    else {
      if (actualNote.folder.removeNote(note.id)) {
        return true;
      }
      else {
        console.error("Failed to remove note!");
        return false;
      }
    }
  }

  //sets currentNotes and currentFolders to the contents of whatever folder is requested
  //folderPath is an array corresponding to the path the folder is at
  //folderPath : /folder1/folder2 refers to the contents of folder2 which is in folder1 which is in the root folder
  //folderPath : / refers to the root folder
  updateCurrentState(folderPath : string[]) {
    if (folderPath == null) {
      console.error("filesystem can't updateCurrentState with a null folderPath");
    }
    //console.log("Given folderPath: %o", folderPath);
    var lastFolder : Folder;
    //keep track for easy reference with regards to insert/delete
    var path : Path = new Path(PathType.users);
    path.addUserId(this.userid);
    for (var i = 0; i < folderPath.length; i++) {
      var folderName : string = folderPath[i];
      if (i == 0) {
        if (folderPath[i] != '/') {
          console.error("folderPath must start with the root folder '/'");
          return;
        }
        if (i == folderPath.length - 1) {
          //update to the root folder
          this.currentNotes = this.notes;
          this.currentFolders = this.folders;
          this.currentPath = Path.RootPath(this.userid);
          this.currentFolder = null;
          //this.currentPath = '/';
          return;
        }
        else {
          lastFolder = null;
          continue;
        }        
      }

      //Check if folder exists and grab its reference
      //find in this.folders
      if (lastFolder == null) {
        for (var j = 0; j < this.folders.length; j++) {
          if (this.folders[j].name === folderName) {
            lastFolder = this.folders[j];
            path.addId(lastFolder.id);
            break;
          }
        }
      }
      //find in lastFolder
      else {
        for (var j = 0; j < lastFolder.children.length; j++) {
          if (lastFolder.children[j].type === "FOLDER" && lastFolder.children[j].name == folderName) {
            lastFolder = lastFolder.children[j];
            path.addChild(lastFolder.id);
            break;
          }
        }
      }
      //check if not found
      if (lastFolder.name !== folderName) {
        console.error("Unable to find folder %s", folderName);
        return;
      }

      //apply changes if last element of folderPath
      if (i == folderPath.length - 1) {
        this.currentFolders = lastFolder.folders;
        this.currentNotes = lastFolder.notes;
        this.currentFolder = lastFolder;
        this.currentPath = path;
      }
    }
  }
}
