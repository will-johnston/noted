import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { QuillEditorComponent } from 'ngx-quill';
import Quill from 'quill';

import { AppComponent } from '../app.component';
import { ElementRef } from '@angular/core';
import { Router } from "@angular/router";
import { Http, Response, Headers, RequestOptions, URLSearchParams } from "@angular/http";
import { Observable } from "rxjs";
declare var $: any;
declare var recorderObject: any;
declare function startRecording(button): void;
declare function stopRecording(button): void;

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

  // Recording Stuff
  isOn: boolean;
  isOff: boolean;

  constructor(
    fb: FormBuilder,
    private elRef: ElementRef,
    private appComponent: AppComponent,
    private router: Router,
    private http: Http) { }

  @ViewChild('editor') editor: QuillEditorComponent

  ngOnInit() {
    this.editor.onContentChanged

    // recorder object stuff
    this.isOn = false;
    this.isOff = true;
    recorderObject.recorder();
    //this.appComponent.isLogin = true;
    //this.appComponent.wrapper = 'page-container';
  }

  setFocus($event) {
    $event.focus();
  }

  public start(button) {
    startRecording(button);
    this.isOn = true;
    this.isOff = false;
  };

  public stop(button) {
    stopRecording(button);
    this.isOn = false;
    this.isOff = true;
  };

}
