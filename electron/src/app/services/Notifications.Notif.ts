export class Notif {
    public text : string;               //what will be displayed
    public type : string;
    constructor (text: string) {
        this.text = text;
        this.type = null;
    }
}