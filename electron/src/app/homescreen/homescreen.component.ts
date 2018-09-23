import { Component, OnInit } from '@angular/core';
import { FilesystemService } from '../services/filesystem.service';
import { Note } from '../note/Note'
import { Folder } from './Folder'

@Component({
  selector: 'app-homescreen',
  templateUrl: './homescreen.component.html',
  styleUrls: ['./homescreen.component.css']
})
export class HomescreenComponent implements OnInit {

  notes : Note[];
  folders: Folder[];
  constructor(private filesystemService : FilesystemService) { }

  getNotes() {
    this.notes = this.filesystemService.notes;
    console.log("got notes " + this.notes);
  }
  getFolders() {
    this.folders = this.filesystemService.folders;
  }
  createFolder(name) {
    this,this.filesystemService.createFolder(name);
  }
  createNote(name) {
    this.filesystemService.createNote(name);
  }

  ngOnInit() {
    this.getNotes();
    this.getFolders();
  }

}
