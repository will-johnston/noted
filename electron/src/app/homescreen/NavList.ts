export class NavList<T> {
    list : T[];
    constructor() {
        this.list = new Array<T>();
    }
    push(element : T) {
        return this.list.push(element);
    }
    pop() {
        return this.list.pop();
    }
    peek() {
        if (this.list.length == 0)
            return null;
        else
            return this.list[this.list.length - 1];
    }
    //rewind to index of T
    goto(element : T) {
        var index = this.list.indexOf(element);
        if (index < 0) {
            return false;
        }
        for (var i = this.list.length; i > (index + 1); i--) {
            this.pop();
        }
        return true;
    }
}