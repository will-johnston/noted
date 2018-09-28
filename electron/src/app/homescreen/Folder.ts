import { Note } from "../note/Note";

export class Folder {
    name : string;
    id : string;
    children : any[];       //Folder or Note (Document)
    childCount : number;
    type : string = "FOLDER";
    path : string = null;
    public notes : Note[];
    public folders : Folder[];
    constructor(name : string, id : string, children : any[], path : string) {
        this.notes = Array();
        this.folders = Array();
        this.name = name;
        this.id = id;
        this.childCount = 0;
        this.path = path;
        this.checkPath(this.path);
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
                console.log("Child (parent: %s) %o", this.name, value);
                if (value.type === "DOCUMENT") {
                    this.children.push(new Note(value.title, value.id, this, this.path + 'children/' + value.id + '/'));
                    this.childCount++;
                }
                else if (value.type === "FOLDER") {
                    this.children.push(new Folder(value.title, value.id, value.children, this.path + 'children/' + value.id + '/'));
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
        for (var i = 0; i < this.children.length; i++) {
            var element = this.children[i];
            if (element.id === id) {
                //remove
                this.children.splice(i);
                return true;
            }
        }
        return false;
    }
    createChildBindings() {
        if (this.children == null) {
            return;
        }
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child.type == "FOLDER") {
                this.folders.push(child);
            }
            else if (child.type == "NOTE") {
                this.notes.push(child);
            }
            else {
                console.error("Unknown child");
            }
        }
    }
}