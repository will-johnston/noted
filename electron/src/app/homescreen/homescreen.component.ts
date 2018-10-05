import { Component, OnInit, NgModule, Inject, Input } from '@angular/core';
import { FilesystemService } from '../services/filesystem.service';
import { Router, ActivatedRoute, NavigationStart } from '@angular/router';
import { Note } from '../note/Note'
import { Folder } from './Folder'
import { NavList } from './NavList';
import * as firebase from 'firebase';

import { ConfirmationDialogService } from '../confirmation-dialog/confirmation-dialog.service';


@Component({
  selector: 'app-homescreen',
  templateUrl: './homescreen.component.html',
  styleUrls: ['./homescreen.component.css']
})
export class HomescreenComponent implements OnInit {
  @Input() note: Note;
  notes : Note[];
  folders: Folder[];
  userid : string = null;
  //navLoc : string[];
  navList : NavList<string>;
  navLoc : string[];
  constructor(private filesystemService : FilesystemService, private router : Router, private activeRoute : ActivatedRoute, private confirmationDialogService: ConfirmationDialogService) {
    this.navList = new NavList<string>();
    this.navLoc = this.navList.list;
    this.navList.push("/");
    this.router.events.subscribe(event => {
      //console.log("Router event: %o", event);
      if (event instanceof NavigationStart) {
        if (event.url.indexOf('homescreen')) {
          console.log("navigated to the homescreen");
          //TODO handle renavigate
          //this.ngOnInit();
        }
      }
    });
   }

  getNotes() {
    this.notes = this.filesystemService.currentNotes;
    //console.log("got notes " + this.notes);
  }
  getFolders() {
    this.folders = this.filesystemService.currentFolders;
  }
  createFolder(name) {
    this.filesystemService.createFolder(name);
    (<HTMLInputElement>document.getElementById("folderName")).value = "";
    //folderName.value = "";
  }
  createNote(name) {
    this.filesystemService.createNote(name);
    (<HTMLInputElement>document.getElementById("noteName")).value = "";
    //noteName.value = "";
  }
  gotoFolder(name) {
    this.navList.push(name);
    this.filesystemService.updateCurrentState(this.navList.list);
    this.getNotes();
    this.getFolders();
  }
  goBackTo(name) {
    this.navList.goto(name);
    console.log('navlist is %o', this.navList.list);
    this.filesystemService.updateCurrentState(this.navList.list);
    this.getNotes();
    this.getFolders();
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
  deleteNote(id : string, name :string ) {
    /*
      TODO:
        1. Create a button to delete a note
          I've created a temporary button for testing, make an actual button
        2. Create a ‘confirm deletion’ modal to confirm the user’s intention to delete the note
        3. If confirm, call do what's below, else do nothing
    */
    this.confirmationDialogService.confirm('Please confirm', 'Sure you want to deletei ' + name+'?')
    .then((confirmed)=> {if (confirmed) {this.filesystemService.deleteNoteFromId(id)}});
    /*if (this.filesystemService.deleteNoteFromId(id)) {
      //works
    }
    else {
      //didn't work
    }*/
  }

  ngOnInit() {
    this.filesystemService.homescreen = this;
    this.filesystemService.onReady.push(function(filesystemService) {
      var t : HomescreenComponent = filesystemService.homescreen;
      t.userid = t.filesystemService.userid;
      t.goBackTo('/');
    });
  }


}
