import { Component, OnInit, OnDestroy, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { ElectronService } from 'ngx-electron';
import { AngularFireStorage } from 'angularfire2/storage';

import { QuillEditorComponent } from 'ngx-quill';
import Quill from 'quill';

declare var MediaRecorder: any;
declare var Blob: any;

// override p with div tag
const Parchment = Quill.import('parchment');
let Block = Parchment.query('block');

Block.tagName = 'DIV';
// or class NewBlock extends Block {}; NewBlock.tagName = 'DIV';
Quill.register(Block /* or NewBlock */, true);

// Add fonts to whitelist
var Font = Quill.import('formats/font');
// We do not add Aref Ruqaa since it is the default
Font.whitelist = ['mirza', 'aref', 'sans-serif', 'monospace', 'serif'];
Quill.register(Font, true);

@Component({
  selector: 'app-note',
  templateUrl: './note.component.html',
  styleUrls: ['./note.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class NoteComponent implements OnInit, OnDestroy {

  private id: string;
  private sub: any;

  @ViewChild('editor') editor: QuillEditorComponent

  constructor(
    private _electronService: ElectronService,
    private storage: AngularFireStorage,
    private route: ActivatedRoute
  ) {
    this.sub = this.route.params.subscribe(params => {
      if (params['id']) {
        this.id = params['id'];
      }
    });

  }

  ngOnInit() {
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  setFocus($event) {
    $event.focus();
  }

  public start() {
    // toggle start button
    var start = <HTMLInputElement>document.getElementById("start");
    start.disabled = true;

    navigator.getUserMedia({ audio: true }, (stream) => {
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorder.start();

      // gather chunks
      const audioChunks = [];
      mediaRecorder.addEventListener("dataavailable", event => {
        audioChunks.push(event.data);
      });

      // when recording is stopped
      mediaRecorder.addEventListener("stop", () => {
        const audioBlob = new Blob(audioChunks);
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = document.querySelector('audio');
        audio.src = audioUrl;

        // store the recording in firebase storage
        // metadata
        //console.log("USER: " + this.user)
        console.log("NOTE: " + this.id);
        /*
        var metadata = {
          customMetadata: {
            'note': this.id
          }
        }
        */

        var uploadTask = this.storage.ref('audio/' + this.id).put(audioBlob /*,metadata*/);

      });

      // toggle stop button
      var stop = <HTMLInputElement>document.getElementById("stop");
      stop.disabled = false;

      stop.onclick = function () {
        // stop recording
        mediaRecorder.stop();

        // toggle buttons
        var stop = <HTMLInputElement>document.getElementById("stop");
        stop.disabled = true;
        var start = <HTMLInputElement>document.getElementById("start");
        start.disabled = false;
      }
    }, this.handleError);
  };

  handleError(e) {
    console.log(e)
  }

}
