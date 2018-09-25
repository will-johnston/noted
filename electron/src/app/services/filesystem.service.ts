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
  private userid : string;
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
  containsNote(id) {
    for (var i = 0; i < this.notes.length; i++) {
      if (this.notes[i].id === id)
        return true;
    }
    return false;
  }
  containsFolder(id) {
    for (var i = 0; i < this.folders.length; i++) {
      if (this.folders[i].id === id)
        return true;
    }
    return false;
  }
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
            var element = action.payload.val();
            //console.log("type: %s, title: %s, id: %s, hasChildren: %s", element.type, element.title, element.id, element.children != null);
            if (element.type === "DOCUMENT") {
              if (!this.containsNote(element.id)) {
                var note = new Note(element.title, element.id, null);
                this.notes.push(note);
              }
            }
            else if (element.type === "FOLDER") {
              if (!this.containsFolder(element.id)) {
                var folder = new Folder(element.title, element.id, element.children);
                this.folders.push(folder);
              }
            }
            else {
              console.error("Don't know how to handle type: %s", element.type);
            }
          }
        });
        /*console.log("Value: %o", value);
        for (var i = 0; i < value.length; i++) {
          var element = value[i];
          console.log("type: %s, title: %s, id: %s, hasChildren: %s", element.type, element.title, element.id, element.children != null);
          if (element.type === "DOCUMENT") {
            var note = new Note(element.title, element.id, null);
            this.notes.push(note);
          }
          else if (element.type === "FOLDER") {
            var folder = new Folder(element.title, element.id, element.children);
            this.folders.push(folder);
          }
          else {
            console.error("Don't know how to handle type: %s", element.type);
          }
        }*/
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
}
