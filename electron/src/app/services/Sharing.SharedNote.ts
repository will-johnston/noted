import { Path } from "../homescreen/Path";

export class SharedNote {
    public title : string;          //the title of the file
    public path : Path;             //the absolute location of the file
    public filePath : Path;         //the fileContents/ location of the file
    public noteID : string;         //the ID of the file (can be derived from path or filePath)
    constructor (title : string, path : Path, filePath : Path, noteID : string) {
        this.title = title;
        this.path = path;
        this.filePath = filePath;
        this.noteID = noteID;
    }
    public isValid() : boolean {
        if (this.title == null)
            return false;
        if (this.path == null)
            return false;
        if (this.filePath == null)
            return false;
        if (this.noteID == null)
            return false;
        return true;
    }
}