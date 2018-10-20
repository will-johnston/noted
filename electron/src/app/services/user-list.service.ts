import { Injectable } from '@angular/core';
import { User } from './UserList.User'

@Injectable({
  providedIn: 'root'
})
/*
  UserList API
  Schema: https://github.com/will-johnston/noted/blob/master/documentation/userList.schema.json
  Handles registering users into the userList database
*/
export class UserListService {

  constructor() {
    /*
      TODO
      Setup reference to userList/
    */
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
    return new Promise<User>((resolve, reject) => {
      reject(null);
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
    return null;
  }
}
