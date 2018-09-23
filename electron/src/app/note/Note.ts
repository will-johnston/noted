import { Folder } from '../homescreen/Folder'
export class Note {
    name : string;
    id : string;
    folder : Folder;
    type = "DOCUMENT";
    constructor(name : string, id : string, folder : Folder) { 
      this.name = name;
      this.id = id;
      this.folder = folder;
    }
  }