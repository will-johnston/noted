import { Component, OnInit, OnDestroy, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

import { ElectronService } from 'ngx-electron';
import { AngularFireStorage } from 'angularfire2/storage';

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

  constructor(
    private _electronService: ElectronService,
    private storage: AngularFireStorage,
    private route: ActivatedRoute
  ) {
    // get params
    this.sub = this.route.params.subscribe(params => {
      if (params['id']) {
        this.id = params['id'];
      }
    });

    this.loadAudio();
  }

  ngOnInit() {
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

}
