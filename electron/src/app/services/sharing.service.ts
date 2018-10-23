import { Injectable } from '@angular/core';
import { Path } from '../homescreen/Path';
import { Observable } from 'rxjs';
import { UserListService } from './user-list.service';
import { NotificationsService } from './notifications.service';
import { SharedNote } from './Sharing.SharedNote';
import { isNullOrUndefined } from 'util';
import { AngularFireDatabase } from '@angular/fire/database';
import * as firebase from 'firebase';
import { Notif } from './Notifications.Notif';

@Injectable({
  providedIn: 'root'
})
/*
  Sharing API
  Schemas 
    sharedWith[]: https://github.com/will-johnston/noted/blob/master/documentation/file.schema.json#L4 
    shared[]: https://github.com/will-johnston/noted/blob/master/documentation/user.schema.json#L32
  Handles sharing notes and getting shared notes.
*/
export class SharingService {

  constructor(private userListService : UserListService, private notificationsService : NotificationsService, private fireDatabase: AngularFireDatabase) {

  }
  /*Get's the shared users for a given File (deals with shared[])
    Return a promise with then() and err() functions
  */
  getSharedUsers(path : Path) : Promise<string[]> {
    /*
      Procedure
      1. Resolve path - make sure it's a legal path
      2. Get the object (fileContents/{fileID})
      3. Get the sharedUsers field of the object
        1. Handle if null && if the field is empty
      4. Create an actual array from the field
        //Field is an object (treat like an array), key/value pairs
        1. Iterate through keys, add values to array
      5. Resolve promise with created array
    */
    return new Promise((resolve, reject) => {
      if (isNullOrUndefined(path)) {
        reject("Null path");
        return;
      }
      let fileSharedRef = this.fireDatabase.list(`${path.toString()}/sharedWith`);
      let users : string[] = Array<string>();
      fileSharedRef.valueChanges().subscribe(value => {
        if (value == null) {
          reject(null);
          return;
        }
        else {
          value.forEach(data => {
            //each data should be a userID
            users.push((data as string));
          });
          resolve(users);
        }
      });
    });
  }
  /*share a note, notify the user as well
    shareTo : userID of who is being shared the file
    noteInfo : info about the note being shared
    shareFrom : name/email of the person sharing the file
    notification : the notification that will be sent to the sharedUser
  */
  shareNote(shareTo : string, noteInfo : SharedNote, shareFrom : string, notification : Notif) : Promise<void> {
    /*
      Procedure
      1. Handle null arguments
      2. Update fileContents/ sharedWith[] of file
      3. Update user/ shared[] of sharedTo
      4. Send notification to shareTo 
    */
    return new Promise((resolve, reject) => {
      if (isNullOrUndefined(shareTo) || isNullOrUndefined(noteInfo) || isNullOrUndefined(shareFrom)) {
        reject("shareNote() was given a null argument");
        return;
      }
      if (!noteInfo.isValid()) {
        reject("shareNote() was given an invalid noteInfo");
        return;
      }
      //check if the user has already been shared the file
      this._getSharedNote(shareTo, noteInfo.noteID).then((found) => {
        if (found) {
          //note has already been shared
          reject("Note has already been shared to user");
          return;
        }
      }).catch(err => {
        if (err == null) {
          //note hasn't been shared already
          let userSharedRef = this.fireDatabase.list(`users/${shareTo}/shared`);
          let fileContentsShared = this.fireDatabase.list(`${noteInfo.filePath}/sharedWith`);
          userSharedRef.push({title : noteInfo.title, path : noteInfo.path.toString(), filePath : noteInfo.filePath.toString(), noteID: noteInfo.noteID});
          fileContentsShared.push(shareTo);
          this.notificationsService.notify(shareTo, notification);
          resolve();
        }
        else {
          reject(err);
          return;
        }
      });

    });
  }
  /*delete a shared note info and remove from shared users
    Doesn't delete a notification if it exists
  */
  deleteSharedNote() : void {

  }

  //gets a shared note if it exists
  _getSharedNote(userID : string, noteID : string) : Promise<SharedNote> {
    //get note from users/ shared[]
    return new Promise((resolve, reject) => {
      if (isNullOrUndefined(userID) || isNullOrUndefined(noteID)) {
        reject("_getSharedNote() was given null arguments");
        return;
      }
      let userSharedRef = this.fireDatabase.list(`users/${userID}/shared`);
      userSharedRef.valueChanges().subscribe(arr => {
        console.log("data : %o", arr);
        if (arr == null) {
          reject('_getSharedNote() called on user with no shared notes');
          return;
        }
        else {
          arr.forEach(data => {
            if ((data as SharedNote).noteID === noteID) {
              let value : any = (data as any);
              resolve(new SharedNote(value.title, Path.FromString(value.path), Path.FromString(value.filePath), value.noteID));
              return;
            }
          });
          reject(null);
          return;
        }
      });
    });
  }
}
