import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { AngularFireDatabase } from '@angular/fire/database';
import * as firebase from 'firebase';
import { Observable } from 'rxjs';
import { Note } from '../note/Note';
import { Folder } from '../homescreen/Folder'

@Injectable({
  providedIn: 'root'
})
export class FilesystemService {
  private userDetails: firebase.User = null;
  public notes: Note[];         //notes that are in the root level
  public folders: Folder[];     //folders and the rest of the notes
  userid : string;
  userRef : any;
  values : Observable<any>;
  private subscribed : boolean;

  constructor(private fireDatabase: AngularFireDatabase) {
    this.notes = Array();
    this.folders = Array();
    //this.userid = 'PSkJKXOw66gP0Y862X5GJMNViXJ3';
    //this.startSubscription();
    firebase.auth().onAuthStateChanged((user) => {
      if (user) {
        console.log("userid : %s", user.uid);
        this.userid = user.uid;
        this.startSubscription();
      }
    });
    //fireDatabase.list('')
    //this.getNotes();
   }
  getNotes() {
      //this.notes.push({ id: 0, name : "note1", folder : null});
      for (var i = 0; i < 10; i++) {
       /* var note = new Note();
        note.folder = null;
        note.id = i.toString();
        note.name = "note " + i;
        this.notes.push(note);*/
      }
      return this.notes;
  }
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
  containsNote(id) {
    for (var i = 0; i < this.notes.length; i++) {
      if (this.notes[i].id === id)
        return true;
    }
    return false;
  }
  //only works on root level
  //FIXME
  containsFolder(id) {
    for (var i = 0; i < this.folders.length; i++) {
      if (this.folders[i].id === id)
        return true;
    }
    return false;
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
        console.log("Action %o", actions);
        actions.forEach(action => {
          /*console.log("Action Key %s", action.key);
          console.log("Action Type %s", action.type);
          console.log("Payload Value %o", action.payload.val());*/
          if (action.payload.val().id == null) {
            console.log("null id on insert, must set id");
            this.fireDatabase.list('users/' + this.userid).update(action.key, {id: action.key});
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
            var element = action.payload.val();
            console.log("element: %o", element);
            if (element.type === "DOCUMENT") {
              if (!this.containsNote(element.id)) {
                var note = new Note(element.title, element.id, null, 'users/' + this.userid + '/' + element.id + '/');
                this.notes.push(note);
              }
            }
            else if (element.type === "FOLDER") {
              if (!this.containsFolder(element.id)) {
                var folder = new Folder(element.title, element.id, element.children, 'users/' + this.userid + '/' + element.id + '/');
                this.folders.push(folder);
              }
            }
            else {
              console.error("Don't know how to handle type: %s", element.type);
            }
          }
        });
      });
      this.subscribed = true;
    }
  }
  createNote(name : string) {
    this.fireDatabase.list('users/' + this.userid).push({ title : name, type : "DOCUMENT", id : null});
    //this.userRef.push({ title : name, type : "DOCUMENT"});
  }
  createFolder(name : string) {
    this.fireDatabase.list('users/' + this.userid).push({ title : name, type : "FOLDER", children : null});
    //this.folders.push(new Folder(name, null, null));
  }
  deleteNote(note : Note) {
    var noteRef = this.fireDatabase.object(note.path);
    noteRef.remove();
    return this.deleteLocalNote(note);
  }
  deleteNoteFromId(id : string) {
    var note = this.getNote(id);
    if (note == null) {
      console.error("Unable to retrieve note in the local file system");
      return false;
    }
    this.deleteNote(note);
  }
  //deletes the local copy of the note
  deleteLocalNote(note : Note) {
    var actualNote = this.getNote(note.id);
    var folder = actualNote.folder;
    if (folder == null) {
      //remove from the root folder
      for (var i = 0; i < this.notes.length; i++) {
        var noteRef = this.notes[i];
        if (noteRef.id === note.id) {
          this.notes.splice(i);
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
}
