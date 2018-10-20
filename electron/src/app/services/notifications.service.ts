import { Injectable } from '@angular/core';
import { UserListService } from './user-list.service';
import { Notif } from './Notifications.Notif';

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

  constructor(private userListService : UserListService) { }

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
  }


  //Get a notification, if it exists, and return it
  _getNotification(userID: string, notification : Notif) : Promise<boolean> {
    return new Promise<boolean>((resolve, reject) => {
      reject(null);
    });
  }
}
