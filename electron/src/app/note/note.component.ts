import { Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { AngularFireDatabase, AngularFireObject } from '@angular/fire/database';
import { AngularFireStorageModule } from '@angular/fire/storage';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import * as firebase from 'firebase';
import { Note } from './Note';

import { QuillEditorComponent } from 'ngx-quill';
import Quill from 'quill';
import { Observable } from 'rxjs';

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

  userid : string = null;       //database userid of the note
  noteid : string = null;       //database id of the note
  notepath : string = null;     //database path for the note
  noteInfo : Note = null;
  noteRef : AngularFireObject<any>;
  subscribed : boolean = false;
  text : string;
  html : string;
  constructor(private route: ActivatedRoute, private router: Router, private fireDatabase: AngularFireDatabase) { 
    this.text = "";
    this.html = "";
    console.log(Quill);
  }

  @ViewChild('editor') editor: QuillEditorComponent

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
      if (params['notepath'] !== undefined) {
        console.log("Note path: %s", params['notepath']);
        this.notepath = params['notepath'];
        this.startSubscription(params['notepath']);
      }
      if (params['noteid'] !== undefined) {
        console.log("Note ID: %s", params['noteid']);
        this.noteid = params['noteid'];
      }
    });
  }

  //This is definitely cheating, but it seems to work... [Ryan]
  updateEditorText(text : string) {
    if (text != null) {
      this.editor.quillEditor.root.innerHTML = text;
    }
  }
  startSubscription(notepath : string) {
    if (!this.subscribed) {
      //this.noteRef = this.fireDatabase.object(notepath).valueChanges();
      this.noteRef = this.fireDatabase.object(notepath);
      this.noteRef.valueChanges()
      .subscribe(value => {
        //console.log(value);
        this.noteInfo = new Note(value.title, value.id, null, notepath);
        this.noteInfo.text = value.htmltext;
        this.updateEditorText(this.noteInfo.text);
      });
    }
  }

  setFocus($event) {
    $event.focus();
  }
  //Save the file in firebase
  saveNote() {
    //this.fireDatabase.list('users/' + this.userid).push({ title : name, type : "DOCUMENT", id : null});
    this.noteRef.update({ htmltext : this.html});
  }
}
