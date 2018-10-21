import { Injectable } from '@angular/core';
import { AngularFireDatabase } from '@angular/fire/database';
import * as firebase from 'firebase';
import { User } from './UserList.User'
import { isNullOrUndefined } from 'util';

@Injectable({
  providedIn: 'root'
})
/*
  UserList API
  Schema: https://github.com/will-johnston/noted/blob/master/documentation/userList.schema.json
  Handles registering users into the userList database
*/
export class UserListService {
  private userListRef : any;
  constructor(private fireDatabase: AngularFireDatabase) {
   this.userListRef = fireDatabase.list('userList/');
  }
  /*Registers a user in the userList
    Notes: should call everytime a user logs in
  */
  register(user : User) : void {
    /*
      Procedure
      1. Handle null arguments
      2. Check if user is already registered (call get())
        1. If registered, exit
      3. push new user to the list
    */
   console.log("Called register with %o", user);
    if (user == null || user == undefined) {
      console.error("register() was given an invalid user :: null/undefined");
      return;
    }
    if (user.id == null || user.id == undefined) {
      console.error("register() was given an invalid user :: bad id");
      return;
    }
    if (user.email == null || user.email == undefined) {
      console.error("register() was given an invalid user :: bad email");
      return;
    }
    //Ignore per Issue #37
    /*if (user.name == null || user.name == undefined) {
      console.error("register() was given an invalid user :: bad name");
      return;
    }*/
    this.get(user.id).then(value => {
      //do nothing
      //console.log("Already registered!");
      return;
    }).catch(reason => {
      if (reason != null) {
        console.error("register() was unable to search user :: bad id");
        return;
      }
      //add to userList
      this.userListRef.set(user.id, { email : user.email, id : user.id, name : user.name});
      //console.log(`Added (${user.name}, ${user.id}) to userList`);
      return;
    });
  }
  /*finds a user in the userList and returns it
    user argument contains search options, must contain one valid field
      an empty user argument, will return null
      a filled email field, will return the first user with the given email
      a filled id field, will return the first user with the given id
      a filled name, will return the first user with the given name
    Notes
      Avoid multiple calls to search as we have to search the entire userList to find the user
      Avoid using the name field to get the user because multiple same names could exist
      Do search by email as those should be mutually exclusive (two users shouldn't share emails)
    Returns a Promise with then() and err() functions
  */
  search(user : User) : Promise<User> {
    /*
      Procedure
      1. Handle null arguments
      2. If id field is populated, return get(user.id)
      3. Subscribe to the values in userList/
      4. Iterate through entires, check if element matches search critieria
        1. If it matches, create a new User object and resolve the Promise with the new user
      5. If element doesn't exist, reject the Promise with null
    */
    if (user != null && !isNullOrUndefined(user.id)) {
      return this.get(user.id);
    }
    return new Promise<User>((resolve, reject) => {
      if (isNullOrUndefined(user)) {
        reject('search() was given an invalid user to search');
        return;
      }
      if (isNullOrUndefined(user.name) && isNullOrUndefined(user.email)) {
        reject('search() was given an invalid search term');
        return;
      }
      if (!isNullOrUndefined(user.email)) {
        //search by email
        this.userListRef.valueChanges().subscribe(value => {
          value.forEach(data => {
            if (data.email == user.email) {
              resolve(new User(data.email, data.id, data.name));
            }
          });
          reject(null);
        });
      }
      else if (!isNullOrUndefined(user.name)) {
        //search by name
        this.userListRef.valueChanges().subscribe(value => {
          value.forEach(data => {
            if (data.name == user.name) {
              resolve(new User(data.email, data.id, data.name));
            }
          });
          reject(null);
        });
      }
    });
  }
  /*Returns a given user and their stored info
    Returns a Promise with then() and err() functions
  */
  get(userID : string) : Promise<User> {
    /*
      Procedure
      1. Handle null arguments
      2. Get object from userList/userID
        1. If the object is null, reject the Promise with null
        2. Else, create a new User object and resolve the Promise with the new user
    */
    return new Promise((resolve, reject) => {
      if (userID == null || userID == undefined)
        reject('userID is invalid');
      else {
        let userObjectRef = this.fireDatabase.object(`userList/${userID}`);
        userObjectRef.valueChanges().subscribe(actions => {
          console.log("Value of get is %o", actions);
          if (actions == null)
            reject(null);
          else {
            var data : any = actions;
            resolve(new User(data.email, data.id, data.name));
          }
        });
      }
    });
  }
}
