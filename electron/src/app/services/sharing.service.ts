import { Injectable } from '@angular/core';
import { Path } from '../homescreen/Path';
import { Observable } from 'rxjs';
import { UserListService } from './user-list.service';
import { NotificationsService } from './notifications.service';
import { SharedNote } from './Sharing.SharedNote';

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

  constructor(private userListService : UserListService, private notificationsService : NotificationsService) {

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
    return null;
  }
  //share a note, notify the user as well
  shareNote() : Promise<boolean> {
    return null;
  }
  /*delete a shared note info and remove from shared users
    Doesn't delete a notification if it exists
  */
  deleteSharedNote() : void {

  }
  //gets all shared notes for a given user
  getSharedNotes(userID : string) : Observable<SharedNote[]> {
    return new Observable(() => {

    });
  }
}
