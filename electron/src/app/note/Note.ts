import { Folder } from '../homescreen/Folder'
export class Note {
    name : string;
    id : string;
    parentId : string;      //Not-null if a child
    folder : Folder;        //this value may be null (root notes always have a null folder)
    type = "DOCUMENT";
    path : string;
    text : string;
    constructor(name : string, id : string, path : string, folder : Folder) { 
      this.name = name;
      this.id = id;
      this.path = path;
      this.folder = folder;
      if (folder != null) 
        this.parentId = folder.id;
      else
        this.parentId = null;
      console.log("pathof(%s) is %s", this.name, this.path);
      //this.checkPath(this.path);
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