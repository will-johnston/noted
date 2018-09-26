import { Component, OnInit, NgModule } from '@angular/core';
import { FilesystemService } from '../services/filesystem.service';
import { Router } from '@angular/router';
import { Note } from '../note/Note'
import { Folder } from './Folder'
import * as firebase from 'firebase';

@Component({
  selector: 'app-homescreen',
  templateUrl: './homescreen.component.html',
  styleUrls: ['./homescreen.component.css']
})
export class HomescreenComponent implements OnInit {

  notes : Note[];
  folders: Folder[];
  userid : string = null;
  constructor(private filesystemService : FilesystemService, private router : Router) { }

  getNotes() {
    this.notes = this.filesystemService.notes;
    console.log("got notes " + this.notes);
  }
  getFolders() {
    this.folders = this.filesystemService.folders;
  }
  createFolder(name) {
    this.filesystemService.createFolder(name);
  }
  createNote(name) {
    this.filesystemService.createNote(name);
  }
  gotoNote(id) {
    console.log("navigate with id=%s", id);
    //this.router.navigate(['note']);
    var note = this.filesystemService.getNote(id);
    if (note == null) {
      //we have an error
      alert("Failed to retrieve note from filesystemService");
    }
    else {
      this.router.navigate(['note', { userid: this.userid, noteid : note.id, notepath: note.path}]);
    }
  }
  logout() {
    firebase.auth().signOut().then(function() {
      // Sign-out successful.
      console.log("LOGOUT!")
    }, function(error) {
      // An error happened.
      console.log(error.log)
    });
  }

  ngOnInit() {
    this.userid = this.filesystemService.userid;
    this.getNotes();
    this.getFolders();
  }

}
