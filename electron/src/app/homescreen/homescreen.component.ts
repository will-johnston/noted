import { Component, OnInit, NgModule } from '@angular/core';
import { FilesystemService } from '../services/filesystem.service';
import { Router } from '@angular/router';
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
  constructor(private filesystemService : FilesystemService, private router : Router) { }

  getNotes() {
    this.notes = this.filesystemService.notes;
    console.log("got notes " + this.notes);
  }
  getFolders() {
    this.folders = this.filesystemService.folders;
  }
  createFolder(name) {
    this.filesystemService.createFolder(name);
  }
  createNote(name) {
    this.filesystemService.createNote(name);
  }
  gotoNote(id) {
    console.log("navigate with id=%s", id);
    //this.router.navigate(['note']);
    this.router.navigate(['note', { id : id}]);
  }

  ngOnInit() {
    this.getNotes();
    this.getFolders();
  }

}
