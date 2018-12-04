import { Component, OnInit, OnDestroy, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { AngularFireDatabase, AngularFireObject } from '@angular/fire/database';
import { ElectronService } from 'ngx-electron';
import { AngularFireStorage } from 'angularfire2/storage';
import { MatDialog } from '@angular/material';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import * as firebase from 'firebase';
import { FilesystemService } from '../services/filesystem.service';
import { Note } from './Note';

//import Quill from 'quill';
import { Observable, Subject } from 'rxjs';
import { AppComponent } from '../app.component';
import { UserListService } from '../services/user-list.service';
import { SharingService } from '../services/sharing.service';

// override p with div tag
//const Parchment = Quill.import('parchment');
//let Block = Parchment.query('block');

import { QuillEditorComponent } from 'ngx-quill';

import { ConfirmationDialogService } from '../confirmation-dialog/confirmation-dialog.service';
import { User } from '../services/UserList.User';
import { SharedNote } from '../services/Sharing.SharedNote';
import { Notif } from '../services/Notifications.Notif';

declare var MediaRecorder: any;
declare var Blob: any;

@Component({
  selector: 'app-note',
  templateUrl: './note.component.html',
  styleUrls: ['./note.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class NoteComponent implements OnInit, OnDestroy {

  private audioBlob: Blob;
  private recording: Boolean = false;
  private startTime: number;

  public editor;

  userid: string = null;       //database userid of the note
  noteid: string = null;       //database id of the note
  notepath: string = null;     //database path for the note
  filepath: string = null;     //fileContents path of note
  noteInfo: Note = null;
  noteRef: AngularFireObject<any>;
  noteTextRef: AngularFireObject<any>;
  subscribed: boolean = false;
  text: string;
  html: string;
  lastEditedBy: string;
  private lastEditedByUID: string;
  private currentUser: firebase.User;
  public viewingSharedNote : boolean = false;
  public imageUrl : string;
  public doesNotHaveImage : boolean = true;
  private viewingImage : boolean = false;

  edits: Array<any>;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fireDatabase: AngularFireDatabase,
    private _electronService: ElectronService,
    private storage: AngularFireStorage,
    private filesystemService: FilesystemService,
    private appComponent: AppComponent,
    private confirmationDialogService: ConfirmationDialogService,
    private userListService: UserListService,
    private sharingService: SharingService,
    private dialog: MatDialog
  ) {
    this.text = "";
    this.html = "";
    this.imageUrl = "";
    this.edits = new Array();
    this.currentUser = firebase.auth().currentUser;
  }

  ngOnInit() {
    this.route.params.forEach((params: Params) => {
      if (params['userid'] !== undefined || params['userid'] !== null) {
        console.log("User ID: %s", params['userid']);
        this.userid = params['userid'];
      }
      if (params['noteid'] !== undefined) {
        console.log("Note ID: %s", params['noteid']);
        this.noteid = params['noteid'];
      }
      if (params['notepath'] !== undefined) {
        console.log("Note path: %s", params['notepath']);
        this.notepath = params['notepath'];
      }
      if (params['filepath'] !== undefined) {
        console.log("File path: %s", params['filepath']);
        this.filepath = params['filepath'];
      }
      if (params['isSharedNote'] !== undefined) {
        console.log("viewing a shared note");
        this.viewingSharedNote = true;
      }
    });
    //fuck you javascript
    if (this.filepath == null || this.filepath == "undefined" || this.filepath == "null" || this.filepath == undefined) {
      this.filepath = "fileContents/" + this.noteid;
    }
    this.startSubscription(this.notepath);
    this.loadAudio();
    this.highlightIfAudio();
  }

  //This is definitely cheating, but it seems to work... [Ryan]
  updateEditorText(text: string, range) {
    if (text != null) {
      this.editor.root.innerHTML = text;
      // set the cursor back to the original position
      if (range) { // cursor is somewhere
        console.log("range: " + range.index);
        this.editor.setSelection(range.index, range.length, 'silent');
        this.editor.format('background', false, 'silent');
      } else { // editor wasn't in focus
        this.editor.setSelection(false, 'silent');
      }
    }
    this.highlightIfAudio();
  }

  highlightIfAudio() {
    // get audio edits
    this.fireDatabase.list('/audioTracking/' + this.noteid).valueChanges().subscribe(res => {
      var curPos = this.editor.getSelection();
      // clear any existing highlighting
      this.unhighlightAllEdits(curPos);
      this.edits = [];

      // fetch edits from firebase
      res.forEach(item => {
        let edit = {};
        for (let [key, value] of Object.entries(item)) {
            if (key == "timestamp") { // log the timestamp
              edit["timestamp"] = value;
            }
            else if (value[0].retain && value[1].insert) { // log the ops
              edit["index"] = value[0].retain;
              edit["content"] = value[1].insert;
            }
        }
        // push the edit to global edits array
        if (Object.keys(edit).length != 0) {
          this.edits.push(edit);
        }
      });

      // highlight audio edits within note
      if (this.edits.length > 0) {
        this.highlightEdits(this.edits, curPos);
      }
    });
  }

  highlightEdits(edits, range) {
    if (edits.length > 0) {
      for (var x = 0; x < edits.length; x++) {
        this.editor.setSelection(edits[x].index, edits[x].content.length, 'silent');
        this.editor.format('background', 'rgb(153, 204, 255)', 'silent');
      }
      // set the cursor back to the original position
      if (range) { // cursor is somewhere
        this.editor.setSelection(range.index, range.length, 'silent');
        this.editor.format('background', false, 'silent');
      } else { // editor wasn't in focus
        this.editor.setSelection(false, 'silent');
      }
    }
  }

  unhighlightEdits(edits, range) {
    if (edits.length > 0) {
      for (var x = 0; x < edits.length; x++) {
        this.editor.setSelection(edits[x].index, edits[x].content.length, 'silent');
        this.editor.format('background', false, 'silent');
      }
      if (range) {
        this.editor.setSelection(range.index, range.length, 'silent');
        this.editor.format('background', false, 'silent');
      } else {
        this.editor.setSelection(false, 'silent')
      }
    }
  }

  /* 
    Helper function - Unhighlighting all edits 
    (may not work correctly until audio edits have finished loading from firebase) 
  */
  unhighlightAllEdits(range) {
    this.unhighlightEdits(this.edits, range);
  }

  createFileContents(value) {
    //console.log("value == null %s, value.key == null %s", value == null, value.key == null);
    //console.log("payload: %o", value.payload.val());
    this.fireDatabase.object("/fileContents/" + this.noteid).set({ data: "", owner: this.currentUser.uid, lastEditedBy: this.currentUser.uid})
      .then(_ => {
        console.log("created File contents successfully");
        console.log("creating at %s", "fileContents/" + this.noteid);
        //this.noteTextRef = this.fireDatabase.object("fileContents/" + this.noteid);
      })
      .catch(err => {
        console.log("createFileContents err: %s", err);
      });
  }
  resetShareNoteValue() {
    //(<HTMLInputElement>document.getElementById("noteName")).value = "";
    (<HTMLInputElement>document.getElementById("shareEmail")).value = "";
  }
  shareNote(email) {
    console.log(`Called share Note with email: ${email}`);
    this.userListService.search(new User(email, null, null)).then(user => {
      //console.log("Found user to share note with!");
      let sharedNote : SharedNote = new SharedNote(this.noteInfo.name, this.noteInfo.path, "fileContents/" + this.noteid, this.noteInfo.id);
      Notif.ShareNoteNotification(this.userListService, this.currentUser.uid, this.noteInfo.name, false)
      .then(notification => {
        this.sharingService.shareNote(user.id, sharedNote, this.currentUser.uid, notification)
        .then(() => {
          alert("Successfully shared note!");
          this.resetShareNoteValue();
        })
        .catch(err => {
          alert(`Unable to share note, error: ${err}`);
        });
      })
      .catch(err => {
        alert(`Unable to share note, notification error: ${err}`);
      });
    })
    .catch(err => {
      alert("Can't find user to share note with!");
    });
  }

  startSubscription(notepath: string) {
    if (!this.subscribed) {
      //this.noteRef = this.fireDatabase.object(notepath).valueChanges();
      this.noteRef = this.fireDatabase.object(notepath);
      this.noteRef.valueChanges()
        .subscribe(value => {
          if (value == null) {
            this.__delete();
            this.noteInfo = null;
            this.appComponent.noteTitle = "";

            //note has been destroyed
            //do nothing out of respect
          }
          else {
            this.noteInfo = new Note(value.title, value.id, notepath, null);
          }
        });
      //this.noteTextRef = this.fireDatabase.object("fileContents/" + this.noteid);
      this.noteTextRef = this.fireDatabase.object(this.filepath);
      this.noteTextRef.valueChanges()
        .subscribe(value => {
          if (value === null) {
            if (this.noteInfo !== null) {
              this.createFileContents(value);
            }
            else {
              //this will get called concurrently with noteRef.valueChanges()
              //So when a note is deleted from the database (external to the user deleting it), both subscriptions will get the deletion event at the same time
              //noteRef handles deleting the file whenever it's been destroyed
              //console.error("SHOULD HAVE BEEN DESTROYED");
              return;
            }
          }
          else {
            this.noteInfo.text = value.data;
            this.lastEditedByUID = value.lastEditedBy;
            if (value.image != null) {
              var tmp = this.storage.ref(`androidImages/${value.image}`).getDownloadURL();
              tmp.subscribe(value => {
                if (value != null) {
                  this.imageUrl = value;
                  console.log(this.imageUrl);
                  this.doesNotHaveImage = false;
                }
              });
            }
            this.userListService.get(this.lastEditedByUID).then(user => {
              this.lastEditedBy = user.email;
            }).catch(err => {
              //leave the same
            }); 
            this.appComponent.noteTitle = this.noteInfo.name;
            this.updateEditorText(this.noteInfo.text, this.editor.getSelection());
          }
        });
    }
  }

  ngOnDestroy() {
    // unsubscribe somehow
  }

  setFocus(quill) {
    quill.focus();
    this.editor = quill;
    this.editor.root.placeholder = "Start writing your masterpiece!";
  }

  editorContentChanged({ editor, html, text, content, delta, oldDelta, source }) {
    this.text = text;
    this.html = html;
    if (this.recording) { // currently recording audio
      var fullTimestamp = Date.now() - this.startTime;
      var roundedTimestamp = Math.floor(fullTimestamp / 1000); // timestamp in seconds from start
      this.fireDatabase.object("/audioTracking/" + this.noteid + "/" + fullTimestamp).set({ delta: delta.ops, timestamp: roundedTimestamp })
        .then(_ => {
          console.log("Tracked edit at: " + fullTimestamp);
          for (let i = 0; i < delta.ops.length; i++) {
            const element = delta.ops[i];
            console.log(element)
          }
          this.highlightIfAudio();
        }).catch(err => {
          console.log("Audio Tracking Error: %s", err);
        });
    }
  }

  editorSelectionChanged({ editor, range, oldRange, source }) {
    if (source == "user" && this.recording == false) {
      this.edits.forEach(element => {
        // check if you're at the end of an edit
        if (range.index == element.index + element.content.length) {
          this.editor.format('background', false, 'silent');
        }
        if (range.index >= element.index && range.index < element.index + element.content.length) {
          const audio = document.querySelector('audio');
          audio.currentTime = element.timestamp;
        }
      });
    }
  }

  /* Audio Player Starts Recording */
  public start() {
    this.toggleButton("start");

    navigator.getUserMedia({ audio: true }, (stream) => {
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorder.start();

      // gather chunks
      const audioChunks = [];
      mediaRecorder.addEventListener("dataavailable", event => {
        audioChunks.push(event.data);
      });

      // when recording starts
      mediaRecorder.addEventListener("start", () => {
        // start tracking user changes @ now o'clock
        this.recording = true;
        this.startTime = Date.now();

        // delete any previous tracking data
        this.unhighlightAllEdits(this.editor.getSelection());
        this.edits = [];
        this.fireDatabase.object("/audioTracking/" + this.noteid).remove().then(() => {
          this.highlightIfAudio();
        });
      });

      // when recording is stopped
      mediaRecorder.addEventListener("stop", () => {
        this.recording = false;

        if (!this.audioBlob) { // audio doesn't exist already
          console.log("Uploading audio file: " + this.noteid);

          // make blob & URL from chunks
          const audioBlob = new Blob(audioChunks, { type: 'audio/mpeg' });
          const audioUrl = URL.createObjectURL(audioBlob);
          this.audioBlob = audioBlob;

          // populate audio element
          const audio = document.querySelector('audio');
          audio.src = audioUrl;

          // Upload to firebase
          if (this.noteid) {
            var uploadTask = this.storage.ref('audio/' + this.noteid).put(audioBlob);
          }
        } else { // audio exists already

          // delete note's previously tracked edits

          // make blob & URL from chunks
          const audioBlob = new Blob(audioChunks, { type: 'audio/mpeg' });
          const audioUrl = URL.createObjectURL(audioBlob);
          this.audioBlob = audioBlob;

          // populate audio element
          const audio = document.querySelector('audio');
          audio.src = audioUrl;

          // Upload to firebase
          if (this.noteid) {
            var uploadTask = this.storage.ref('audio/' + this.noteid).put(audioBlob);
          }
        }
      });

      // toggle stop button
      this.toggleButton("stop");
      var stop = <HTMLInputElement>document.getElementById("stop");
      stop.onclick = () => {
        // stop recording
        mediaRecorder.stop();

        // toggle buttons
        this.toggleButton("stop");
        this.toggleButton("start");
      }
    }, this.handleError);
  };

  handleError(e) {
    console.log(e)
  }

  toggleButton(btnString) {
    var button = <HTMLInputElement>document.getElementById(btnString);
    if (button.disabled) {
      button.disabled = false;
    } else {
      button.disabled = true;
    }
  }

  loadAudio() {
    // load audio from database
    var audioRef = this.storage.ref('audio/' + this.noteid);
    if (audioRef) {
      var url = audioRef.getDownloadURL();
      url.toPromise().then((url) => {
        // load from url
        var xhr = new XMLHttpRequest();
        xhr.responseType = 'blob';
        xhr.onload = (event) => {
          var blob = xhr.response;
          this.audioBlob = blob;
          console.log("BLOB: " + blob)

          // use blob to populate audio element
          const audio = document.querySelector('audio');
          const audioUrl = URL.createObjectURL(blob);
          audio.src = audioUrl;
        };
        xhr.open('GET', url);
        xhr.send();
      }).catch(function (error) {
        switch (error.code) {
          case 'storage/object_not_found':
            console.log("ERROR: Audio File Does Not Exist.");
            break;

          case 'storage/unauthorized':
            console.log("ERROR: User Does Not Have Permission To Access This Audio File.")
            break;

          case 'storage/unknown':
            console.log("ERROR: An Unknown Error Occured While Loading The Audio File.")
            break;
        }
      });
    }
  }
  //Save the file in firebase
  saveNote() {
    var cleanHtml = this.html
      .replace('<span style=\"background-color: rgb(153, 204, 255);\">', '')
      .replace('</span>', '');
    this.noteTextRef.update({ data: cleanHtml, lastEditedBy: this.currentUser.uid });
  }
  deleteNote(id: string, name: string) {
    this.confirmationDialogService.confirm('Confirm', "Are you sure you want to delete the note '" + name + "'?")
      .then((confirmed) => { if (confirmed) { this.__delete(); } });
  }
  //Permanently deletes a note
  __delete() {
    if (this.filesystemService.deleteNote(this.noteInfo)) {

      this.router.navigate(['homescreen']);
    }
    else {
      console.error("Couldn't delete note from local filesystem");
      this.router.navigate(['homescreen']);
    }
  }
  viewImage() : void {
    if (this.viewingImage)
      return;
    //console.log("Clicked viewImage");
    const dialogRef = this.dialog.open(ImageDialog);
    dialogRef.componentInstance.imageUrl = this.imageUrl;
    this.viewingImage = true;
    dialogRef.afterClosed().subscribe(result => {
      this.viewingImage = false;
    });
  }
}

@Component({
  selector: 'image-dialog',
  templateUrl: './image-dialog.html'
})
export class ImageDialog {
  public imageUrl: string;
}