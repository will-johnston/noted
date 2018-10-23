import { Injectable } from '@angular/core';
import { UserListService } from './user-list.service';
import { Notif } from './Notifications.Notif';
import { Observable } from 'rxjs';
import { isNullOrUndefined } from 'util';
import { AngularFireDatabase } from '@angular/fire/database';
import * as firebase from 'firebase';
import { User } from './UserList.User';

@Injectable({
  providedIn: 'root'
})
/*
  Notification API
  Schema: 
    notifications[]: 
  Handles getting notifications (via listeners) and clearing notifications
*/
export class NotificationsService {

  constructor(private userListService : UserListService, private fireDatabase: AngularFireDatabase) { }

  /*Sends a notification to a user

  */
  notify(userID : string, notification : Notif) : void {
    /*
      Procedure
      1. Handle null arguments
      2. Get user
      3. Check if user already has the notification
        1. if exists, exit
      4. Add notification to user's notifications[]
    */
    if (isNullOrUndefined(userID) || isNullOrUndefined(notification)) {
      console.error("notify() was given null arguments");
      return;
    }
    else {
      this.userListService.get(userID)
      .then(user => {
        if (isNullOrUndefined(user)) {
          console.error("UserListService.get() resolved a null user");
          return;
        }
        else {
          this._getNotification(userID, notification)
          .then(foundNotification => {
              //notification already exists, don't notify
              console.log("NotificationService.notify() :: user already has this notification, not notifying");
              return;
          })
          .catch(errNotification => {
              //check errNotification
              if (errNotification == null) {
                //notification doesn't exist already, we can add it
                let userNotifRef = this.fireDatabase.list(`users/${userID}/notifications`);
                userNotifRef.push({text : notification.text, type : notification.type});
                return;
              }
              else {
                console.error("_getNotification() returned an error: %s", errNotification);
              }
          });
        }
      })
      .catch(err => {
        if (err == null) {
          console.error("Couldn't notify user, %s couldn't be found in the userList", userID);
        }
        else {
          console.error("Couldn't notify user, err: %s", err);
        }
      });
    }
  }
  //delete a notification that a user has receieved
  clearNotification(userId : string, notification : Notif) : Promise<boolean> {
    /*
      Procedure
      1. Handle null arguments
      2. Get User
      3. getNotification
        1. If it doesn't exists, exit
      4. remove notification from user
    */
    throw new Error("To be Implemented");
  }


  //Get a notification, if it exists, and return it
  _getNotification(userID: string, notification : Notif) : Promise<Notif> {
    return new Promise((resolve, reject) => {
      if (isNullOrUndefined(userID) || isNullOrUndefined(notification)) {
        reject("_getNotification() was given null arguments");
        return;
      }
      let userNotifRef = this.fireDatabase.list(`users/${userID}/notifications`);
      userNotifRef.valueChanges().subscribe(arr => {
        if (arr == null) {
          reject('_getNotification() called on user with no notifications');
          return;
        }
        else {
          arr.forEach(data => {
            let value : any = data;
            if (!isNullOrUndefined(value)) {
              if (value.text === notification.text) {
                resolve(new Notif(value.text));
                return;
              }
            }
          });
          reject(null);
          return;
        }
      });
    });
  }
}
