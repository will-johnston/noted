export class Path {
    private type : PathType;
    private inRootDirectory : boolean;          //if we're at '/';
    public addedChild : boolean;
    private addedUserId : boolean;
    list : string[];
    constructor(type : PathType) {
        this.type = type;
        this.inRootDirectory = true;
        this.addedChild = false;
        this.list = new Array();
        if (type == PathType.users)
            this.list.push('users/');
        else
            this.list.push('fileContents/');
        //console.log("path list: %o", this.list);
    }
    addUserId(id : string) {
        if (id == null || id === "undefined") {
            console.error("Attempted to add null id");
            console.trace();
        }
        if (this.addedUserId)
            return false;
        //console.log("adding user id %s", id);
        if (this.type == PathType.users) {
            this.list.push(id + '/');
            this.addedUserId = true;
            return true;
        }
        else
            return false;           //fileContents can only addId();
        //console.log("path list: %o", this.list);
    }
    addId(id : string) {
        if (id == null || id === "undefined") {
            console.error("Attempted to add null id");
            console.trace();
        }
        //console.log("added id %s", id);
        this.list.push(id + '/');
        this.inRootDirectory = false;
        //console.log("path list: %o", this.list);
    }
    addChild(id : string) {
        if (id == null || id === "undefined") {
            console.error("Attempted to add null id");
            console.trace();
        }
        //console.log("added child %s", id);
        if (this.type == PathType.users) {
            this.list.push('children/' + id + '/');
            this.addedChild = true;
        }   
        else
            return false;           //fileContents can only addId();
        this.inRootDirectory = false;
        //console.log("path list: %o", this.list);
    }

    //Flattens the path-arguments list to a string
    //use when setting an object's path (a Note object's path field will be this value)
    toString() {
        var path_str : string = "";
        //flatten list
        for (var i = 0; i < this.list.length; i++) {
            path_str += this.list[i];
        }
        return path_str;
    }
    //returns a string that respects the children flag
    //use when inserting a new value
    toInsertString() {
        var path_str = this.toString();
        if (this.inRootDirectory) {
            //console.log("insert string: %s", path_str);
            return path_str;
        }
        else {
            //console.log("insert string: %s", path_str + 'children/');
            return path_str + 'children/';
        }  
    }
    //returns an insert string pointing to the root directory (users/)
    static RootPath(userid : string) : Path {
        var path : Path = new Path(PathType.users);
        path.addUserId(userid);
        return path;
    }
    //returns a path object given an insertion string
    //assumes pathStr is an insertion string
    static FromString(pathStr : string) : Path {
        var path : Path;
        if (pathStr == null) {
            console.error("FromString() was given a null pathStr to deserialize");
            return null;
        }
        var pathStrSplit : string[] = pathStr.split('/');
        if (pathStrSplit[0] === "users") {
            path = new Path(PathType.users);
        }
        else if (pathStrSplit[1] === "fileContents") {
            path = new Path(PathType.fileContents);
        }
        else {
            console.error("FromString() was given an invalid pathStr to deserialize. No type!");
        }
        for (var i = 1; i < pathStrSplit.length; i++) {
            //keywords : children
            //key locations : userid [1]

            //check key locations
            if (i == 1) {
                path.addUserId(pathStrSplit[i]);
            }
            else {
                //check keywords
                if (pathStrSplit[i] === 'children') {
                    if (i + 1 >= pathStrSplit.length) {
                        //was given a children tag but no child id
                        console.error("FromString() was given an invalid pathStr to deserialize. No id for the given child tag, EOF");
                        return null;
                    }
                    else {
                        path.addChild(pathStrSplit[i+1]);
                        i++;
                    }
                }
                else {
                    path.addId(pathStrSplit[i]);
                }
            }
        }
        return path;
    }
}
export enum PathType {
    users,
    fileContents
}