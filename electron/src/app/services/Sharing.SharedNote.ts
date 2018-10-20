import { Path } from "../homescreen/Path";

export class SharedNote {
    public title : string;
    public path : Path;
    public filePath : Path;
    public noteID : string;
    constructor (title : string, path : Path, filePath : Path, noteID : string) {
        this.title = title;
        this.path = path;
        this.filePath = filePath;
        this.noteID = noteID;
    }
}