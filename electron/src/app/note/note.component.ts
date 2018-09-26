import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';

import { QuillEditorComponent } from 'ngx-quill';
import Quill from 'quill';

declare var MediaRecorder: any;
declare var Blob: any;

import { ElectronService } from 'ngx-electron';

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
export class NoteComponent implements OnInit {

  constructor(
    private _electronService: ElectronService
  ) { }

  @ViewChild('editor') editor: QuillEditorComponent

  ngOnInit() {
    this.editor.onContentChanged
  }

  setFocus($event) {
    $event.focus();
  }

  public start() {
    var start = <HTMLInputElement>document.getElementById("start");
    start.disabled = true;
    navigator.getUserMedia({ audio: true }, (stream) => {
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorder.start();

      const audioChunks = [];
      mediaRecorder.addEventListener("dataavailable", event => {
        audioChunks.push(event.data);
      });

      mediaRecorder.addEventListener("stop", () => {
        const audioBlob = new Blob(audioChunks);
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = document.querySelector('audio');
        audio.src = audioUrl;
      });

      var stop = <HTMLInputElement>document.getElementById("stop");
      stop.disabled = false;
      stop.onclick = function () {
        var stop = <HTMLInputElement>document.getElementById("stop");
        stop.disabled = true;
        var start = <HTMLInputElement>document.getElementById("start");
        start.disabled = false;
        console.log("HERE");
        mediaRecorder.stop();
      }
    }, this.handleError);
  };

  handleError(e) {
    console.log(e)
  }

}
