export class Path {
    private type : PathType;
    private inRootDirectory : boolean;          //if we're at '/';
    list : string[];
    constructor(type : PathType) {
        this.type = type;
        this.inRootDirectory = true;
        this.list = new Array();
        if (type == PathType.users)
            this.list.push('users/');
        else
            this.list.push('fileContents/');
        console.log("path list: %o", this.list);
    }
    addUserId(id : string) {
        console.log("adding user id %s", id);
        if (this.type == PathType.users)
            this.list.push(id + '/');
        else
            return false;           //fileContents can only addId();
        console.log("path list: %o", this.list);
    }
    addId(id : string) {
        console.log("added id %s", id);
        this.list.push(id + '/');
        this.inRootDirectory = false;
        console.log("path list: %o", this.list);
    }
    addChild(id : string) {
        console.log("added child %s", id);
        if (this.type == PathType.users)
            this.list.push('children/' + id + '/');
        else
            return false;           //fileContents can only addId();
        this.inRootDirectory = false;
        console.log("path list: %o", this.list);
    }

    toString() {
        var path_str : string = "";
        //flatten list
        for (var i = 0; i < this.list.length; i++) {
            path_str += this.list[i];
        }
        return path_str;
    }
    //returns a string that respects the children flag
    toInsertString() {
        var path_str = this.toString();
        if (this.inRootDirectory) {
            console.log("insert string: %s", path_str);
            return path_str;
        }
        else {
            console.log("insert string: %s", path_str + 'children/');
            return path_str + 'children/';
        }  
    }
}
export enum PathType {
    users,
    fileContents
}