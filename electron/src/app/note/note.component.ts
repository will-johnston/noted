import { Component, OnInit, OnDestroy, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { AngularFireDatabase, AngularFireObject } from '@angular/fire/database';
import { AngularFireStorageModule } from '@angular/fire/storage';
import { ElectronService } from 'ngx-electron';
import { AngularFireStorage } from 'angularfire2/storage';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import * as firebase from 'firebase';
import { FilesystemService } from '../services/filesystem.service';
import { Note } from './Note';

import Quill from 'quill';
import { Observable } from 'rxjs';

// override p with div tag
const Parchment = Quill.import('parchment');
let Block = Parchment.query('block');

import { QuillEditorComponent } from 'ngx-quill';

declare var MediaRecorder: any;
declare var Blob: any;
declare var ConcatenateBlobs: any;

@Component({
  selector: 'app-note',
  templateUrl: './note.component.html',
  styleUrls: ['./note.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class NoteComponent implements OnInit, OnDestroy {

  private id: string;
  private sub: any;
  private audioBlob: Blob;
  private audioContext: AudioContext;
  private edits: any;
  
  public editor;

  userid : string = null;       //database userid of the note
  noteid : string = null;       //database id of the note
  notepath : string = null;     //database path for the note
  noteInfo : Note = null;
  noteRef : AngularFireObject<any>;
  noteTextRef : AngularFireObject<any>;
  subscribed : boolean = false;
  text : string;
  html : string;
  constructor(
    private route: ActivatedRoute, 
    private router: Router, 
    private fireDatabase: AngularFireDatabase, 
    private _electronService: ElectronService,
    private storage: AngularFireStorage,
    private filesystemService : FilesystemService
  ) { 
      this.text = "";
      this.html = "";
      console.log(Quill);
      
      this.loadAudio();
  }

  ngOnInit() {
    console.log(this.editor);
    this.editor
      .onContentChanged
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(data => {
        //console.log('view child + directly subscription', data)
        this.text = data.text;
        this.html = data.html;
        console.log("text %s, html %s", data.text, data.html);
    });
    this.editor.placeholder = "Start writing your masterpiece!";
    //this.editor.content = "Loading Note...";
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
        this.startSubscription(params['notepath']);
      }
    });
  }

  //This is definitely cheating, but it seems to work... [Ryan]
  updateEditorText(text : string) {
    if (text != null) {
      this.editor.quillEditor.root.innerHTML = text;
    }
  }
  createFileContents(value) {
    //console.log("value == null %s, value.key == null %s", value == null, value.key == null);
    //console.log("payload: %o", value.payload.val());
    this.fireDatabase.object("/fileContents/" + this.noteid).set({data : ""})
    .then(_ => {
      console.log("created File contents successfully");
      console.log("creating at %s", "fileContents/" + this.noteid);
      //this.noteTextRef = this.fireDatabase.object("fileContents/" + this.noteid);
    })
    .catch(err => {
      console.log("createFileContents err: %s", err);
    });
  }
  startSubscription(notepath : string) {
    if (!this.subscribed) {
      //this.noteRef = this.fireDatabase.object(notepath).valueChanges();
      this.noteRef = this.fireDatabase.object(notepath);
      this.noteRef.valueChanges()
      .subscribe(value => {
        if (value == null) {
          this.noteInfo = null;
          //note has been destroyed
          //do nothing out of respect
        }
        else {
          this.noteInfo = new Note(value.title, value.id, null, notepath);
        }
      });
      this.noteTextRef = this.fireDatabase.object("fileContents/" + this.noteid);
      console.log("noteTextRef %s", "fileContents/" + this.noteid);
      this.noteTextRef.valueChanges()
      .subscribe(value => {
        console.log("noteTextRef value: %o", value);
        if (value === null) {
          if (this.noteInfo !== null) {
            this.createFileContents(value);
          }
          else {
            //has probably been destroyed
          }
        }
        else {
          this.noteInfo.text = value.data;
          this.updateEditorText(this.noteInfo.text);
        }
      });
    }
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  setFocus(quill) {
    quill.focus();
    this.editor = quill;
  }

  editorContentChanged({ editor, html, text, content, delta, oldDelta, source }) {
    for (let i = 0; i < delta.ops.length; i++) {
      const element = delta.ops[i];
      console.log(element)
    }
  }

  public start() {
    this.toggleButton("start");

    this.edits = [];

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
        // start tracking user changes
      });

      // when recording is stopped
      mediaRecorder.addEventListener("stop", () => {
        if (!this.audioBlob) { // audio doesn't exist already
          console.log("Uploading audio file: " + this.id);

          // make blob & URL from chunks
          const audioBlob = new Blob(audioChunks);
          const audioUrl = URL.createObjectURL(audioBlob);
          this.audioBlob = audioBlob;

          // populate audio element
          const audio = document.querySelector('audio');
          audio.src = audioUrl;

          // Upload to firebase
          if (this.id) {
            var uploadTask = this.storage.ref('audio/' + this.id).put(audioBlob);
          }
        } else { // audio exists already

          // delete note's previously tracked edits

          // make blob & URL from chunks
          const audioBlob = new Blob(audioChunks);
          const audioUrl = URL.createObjectURL(audioBlob);
          this.audioBlob = audioBlob;

          // populate audio element
          const audio = document.querySelector('audio');
          audio.src = audioUrl;

          // Upload to firebase
          if (this.id) {
            var uploadTask = this.storage.ref('audio/' + this.id).put(audioBlob);
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
    // load audio context
    this.audioContext = new AudioContext;

    // load audio from database
    var audioRef = this.storage.ref('audio/' + this.id);
    var url = audioRef.getDownloadURL();
    url.toPromise().then((url) => {
      // load from url
      var xhr = new XMLHttpRequest();
      xhr.responseType = 'blob';
      xhr.onload = (event) => {
        var blob = xhr.response;
        this.audioBlob = blob;

        // use blob to populate audio element
        const audio = document.querySelector('audio');
        const audioUrl = URL.createObjectURL(this.audioBlob);
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
  //Save the file in firebase
  saveNote() {
    //this.fireDatabase.list('users/' + this.userid).push({ title : name, type : "DOCUMENT", id : null});
    //this.noteRef.update({ htmltext : this.html});
    this.noteTextRef.update({data : this.html});
  }
  deleteNote() {
    /*
      TODO:
        1. Create a button to delete a note 
          I've created a temporary button for testing, make an actual button
        2. Create a ‘confirm deletion’ modal to confirm the user’s intention to delete the note
        3. If confirm, call do what's below, else do nothing
    */
    this.__delete();
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
}
