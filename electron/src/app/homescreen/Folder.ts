import { Note } from "../note/Note";

export class Folder {
    name : string;
    id : string;
    children : any[];       //Folder or Note (Document)
    childCount : number;
    type : string = "FOLDER";
    constructor(name : string, id : string, children : any[]) {
        this.name = name;
        this.id = id;
        this.childCount = 0;
        this.resolveChildren(children);
    }
    resolveChildren(children : any[]) {
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
                    this.children.push(new Note(value.title, value.id, this));
                    this.childCount++;
                }
                else if (value.type === "FOLDER") {
                    this.children.push(new Folder(value.title, value.id, value.children));
                    this.childCount++;
                }
                else {
                    console.error("Don't know how to resolve this type! Type %s", value.type);
                }
              }
        }
    }
}