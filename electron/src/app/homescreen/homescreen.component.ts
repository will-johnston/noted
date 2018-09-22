import { Component, OnInit } from '@angular/core';
import { FilesystemService } from '../services/filesystem.service';
import { Note } from '../note/Note'

@Component({
  selector: 'app-homescreen',
  templateUrl: './homescreen.component.html',
  styleUrls: ['./homescreen.component.css']
})
export class HomescreenComponent implements OnInit {

  notes : Note[];
  constructor(private filesystemService : FilesystemService) { }

  getNotes() {
    this.notes = this.filesystemService.notes;
    console.log("got notes " + this.notes);
  }

  ngOnInit() {
    this.getNotes();
  }

}
