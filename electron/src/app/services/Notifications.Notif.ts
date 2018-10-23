import { UserListService } from './user-list.service';
import { isNullOrUndefined } from 'util';
import { User } from './UserList.User';

export class Notif {
    public text : string;               //what will be displayed
    public type : string;
    constructor (text: string) {
        this.text = text;
        this.type = null;
    }
    //Creates a sharing note notification
    //Example 'b@blank.com has shared a 'study in scarlet' with you
    //shareTo : userID of person who will receive the notification
    //noteName : name of the note being shared
    //useName : whether to use userList.name or userList.email (Always false, see Issue 37)
    static ShareNoteNotification(userListService: UserListService, shareTo : string, noteName : string, useName : boolean) : Promise<Notif> {
        return new Promise((resolve, reject) => {
            if (isNullOrUndefined(userListService)) {
                reject("ShareNoteNotification was given a null UserListService");
                return;
            }
            if (isNullOrUndefined(shareTo) || isNullOrUndefined(noteName)) {
                reject("ShareNoteNotification was given null arguments");
                return;
            }
            //get userinfo from userListService
            userListService.get(shareTo)
            .then(user => {
                if (isNullOrUndefined(user)) {
                    reject("UserListService.get() returned a null User");
                    return;
                }
                else {
                    //Disabled due to Issue 37
                    if (useName && false) {
                        //use user.name
                        resolve(new Notif(`${user.name} has shared '${noteName}' with you.`));
                        return;
                    }
                    else {
                        //use user.email 
                        resolve(new Notif(`${user.email} has shared '${noteName}' with you.`))
                        return;
                    }
                }
            })
            .catch(err => {
                reject(err);
                return;
            });
        });
    }
}