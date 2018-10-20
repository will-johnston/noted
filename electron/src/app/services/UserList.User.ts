export class User {
    public email : string;
    public id : string;
    public name : string;
    constructor (email : string, id : string, name : string) {
        this.email = email;
        this.id = id;
        this.name = name;
    }
}