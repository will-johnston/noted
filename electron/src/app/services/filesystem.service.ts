import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import * as firebase from 'firebase/app';
import { Note } from '../note/Note';

@Injectable({
  providedIn: 'root'
})
export class FilesystemService {
  private userDetails: firebase.User = null;
  public notes: Note[] = new Array();

  constructor(private authService: AuthService) {
    this.userDetails = authService.getUserDetails();
    this.getNotes();
   }
  getNotes() {
      //this.notes.push({ id: 0, name : "note1", folder : null});
      for (var i = 0; i < 10; i++) {
        var note = new Note();
        note.folder = null;
        note.id = i.toString();
        note.name = "note " + i;
        this.notes.push(note);
      }
      return this.notes;
  }
}
