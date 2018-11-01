import { Note } from "../note/Note";

export class Folder {
    name : string;
    id : string;
    parentId : string;      //Not-null if a child
    children : any[];       //Folder or Note (Document)
    childCount : number;
    type : string = "FOLDER";
    path : string = null;
    public folder : Folder; //this value may be null (root notes always have a null folder)
    public notes : Note[];
    public folders : Folder[];
    constructor(name : string, id : string, children : any[], path : string, folder : Folder) {
        this.notes = Array();
        this.folders = Array();
        this.name = name;
        this.folder = folder;
        this.id = id;
        if (folder != null) 
            this.parentId = folder.id;
        else
            this.parentId = null;
        this.childCount = 0;
        this.path = path;
        //console.log("pathof(%s) is %s", this.name, this.path);
        //this.checkPath(this.path);
        this.resolveChildren(children);
    }
    checkPath(path : string) {
        if (path[path.length - 1] != '/') {
            console.error('Folder has invalid path');
            this.path = this.path + '/';
        }
        else {
            //console.log("Folder has valid path %s", this.path);
        }
    }
    resolveChildren(children: any[]) {
        if (children == null) {
            this.children = null;
            return;
        }
        else {
            this.children = Array();
            for (let key in children) {
                let value = children[key];
                //console.log("Child (parent: %s) %o", this.name, value);
                if (value.type === "DOCUMENT") {
                    this.children.push(new Note(value.title, value.id, this.path + 'children/' + value.id + '/', value.parentId));
                    this.childCount++;
                }
                else if (value.type === "FOLDER") {
                    this.children.push(new Folder(value.title, value.id, value.children, this.path + 'children/' + value.id + '/', value.parentId));
                    this.childCount++;
                }
                else {
                    console.error("Don't know how to resolve this type! Type %s", value.type);
                }
            }
        }
        this.createChildBindings();
    }
    getNote(id : string) {
        //console.log("getNote called on %s", this.name);
        if (this.children == null) {
            console.error("getNote called but children is null");
            return null;
        }
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child == null)
                continue;
            else {
                if (child.type === "DOCUMENT") {
                    if (child.id === id)
                        return child;
                }
                else if (child.type === "FOLDER") {
                    var innerchild = child.getNote(id);
                    if (innerchild == null)
                        continue;
                    else if (innerchild.id === id)
                        return innerchild;
                    else {
                        console.error("innerchild (%s/%s) was returned but id's don't match", innerchild.name, innerchild.id);
                        console.error("innerchild.id (%s) vs requested id (%s)", innerchild.id, id);
                    }
                }
                else {
                    console.error("Folder %s has a child of invalid type %s", this.name, child.type);
                }
            }
        }
        return null;
    }
    //remove note from children (depth=1, don't go recursive)
    removeNote(id : string) {
        if (this.children == null)
            return false;
        //remove from this.children
        var removedFromChildren, removedFromNotes : boolean = false;
        for (var i = 0; i < this.children.length; i++) {
            var element = this.children[i];
            if (element.id === id) {
                //remove from this.children
                this.children.splice(i, 1);
                removedFromChildren = true;
                break;
            }
        }
        if (!removedFromChildren)
            return false;
        for (var i = 0; i < this.notes.length; i++) {
            var note : Note = this.notes[i];
            if (note.id === id) {
                //remove from this.notes
                this.notes.splice(i, 1);
                removedFromNotes = true;
                break;
            }
        }
        if (!removedFromNotes)
            return false;
        return true;
    }
    //loads this.folders and this.notes with the contents of this.children
    createChildBindings() {
        if (this.children == null) {
            return;
        }
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child.type == "FOLDER") {
                this.folders.push(child);
            }
            else if (child.type == "DOCUMENT") {
                this.notes.push(child);
            }
            else {
                console.error("Unknown child");
            }
        }
    }
    //recursive search to find a folder
    getFolder(id : string) {
        if (this.folders == null)
            return null;
        for (var i = 0; i < this.folders.length; i++) {
            if (this.folders[i].id === id)
                return this.folders[i];
            var folder : Folder = this.folders[i].getFolder(id);
            if (folder != null)
                return folder;
        }
        return null;
    }
    //If an element exists locally (non-recursive search), return it. Else, return null 
    containsLocalElement(id : string) : Folder | Note {
        if (this.children == null)
            return null;
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child.id === id) {
                return child;
            }
        }
        return null;
    }
    //adds to notes | folders and children
    addChild(child : Note | Folder) {
        if (this.children == null) {
            this.children = new Array();
        }
        if (child.type === "DOCUMENT") {
            this.notes.push(child as Note);
        }
        else {
            this.folders.push(child as Folder);
        }
        this.children.push(child);
    }
    killKids() {
        if (this.children == null)
            return;
        else {
            this.children.splice(0);
            this.notes.splice(0);
            this.folders.splice(0);
        }
    }

    //This is needed whenever firebase deletes a child but the filesystem doesn't

    //remove child which is not in the list of ids
    killKid(ids : string[]) {
        //construct array of ids currently in this.children
        var currentIds = new Array(this.children.length);
        for (var i = 0; i < this.children.length; i++) {
            currentIds[i] = this.children[i].id;
        }
        var diffed = this.diff(ids, currentIds);
        if (diffed == null) {
            console.error("killKid(), can't determine which kid to kill!");
        }
        else {
            console.log("killKid, removing %d kids\n", diffed.length);
            for (var i = 0 ; i < diffed.length; i++) {
                //TODO removeFolder
                this.removeNote(diffed[i]);
            }
        }
    }
    //https://github.com/Rafase282/My-FreeCodeCamp-Code/wiki/Bonfire-Diff-Two-Arrays
    diff(arr1, arr2) {
        var newArr = arr1.concat(arr2);
        function check(item) {
            if (arr1.indexOf(item) === -1 || arr2.indexOf(item) === -1) {
                return item;
            }
        }
        return newArr.filter(check);
    }
}