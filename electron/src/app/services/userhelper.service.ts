import * as firebase from 'firebase'
import { Injectable } from '@angular/core';
@Injectable({
    providedIn: 'root'
  })

//a Helper service for getting userdata without having to listen to firebase.authChanged()
export class UserHelperService {
    public currentUser : firebase.User;
}