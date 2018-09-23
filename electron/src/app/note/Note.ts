import { Folder } from '../homescreen/Folder'
export class Note {
    name : string;
    id : string;
    folder : Folder;        //this value may be null (root notes always have a null folder)
    type = "DOCUMENT";
    path : string;
    constructor(name : string, id : string, folder : Folder, path : string) { 
      this.name = name;
      this.id = id;
      this.folder = folder;
      this.path = path;
      this.checkPath(this.path);
    }
    checkPath(path : string) {
      if (path[path.length - 1] != '/') {
          console.error('Note has invalid path');
          this.path = this.path + '/';
      }
      else {
          //console.log("Note has valid path: %s", this.path);
      }
  }
  }