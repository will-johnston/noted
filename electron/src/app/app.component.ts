import { Component } from '@angular/core';
import { RouterModule, Routes, Router, NavigationStart } from '@angular/router';
import * as firebase from 'firebase'
import { UserHelperService } from './services/userhelper.service';
import { UserListService } from './services/user-list.service';
import { User } from './services/UserList.User';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'noted';
  public noteTitle = "";

  constructor(private router : Router, private userHelper : UserHelperService, private userListService : UserListService) {
    // init firebase
    var config = {
      apiKey: "AIzaSyBv0Xkso50BFpf6lc4_w1LJwEBHDN5IkOQ",
      authDomain: "noted-a0a2a.firebaseapp.com",
      databaseURL: "https://noted-a0a2a.firebaseio.com",
      projectId: "noted-a0a2a",
      storageBucket: "noted-a0a2a.appspot.com",
      messagingSenderId: "83083800156"
    };
    firebase.initializeApp(config);

    // when a user signs in or out
    firebase.auth().onAuthStateChanged((user) => {
      if (user) {
        // If there is a user logged in send them to the home page
        //TODO: send to home page
        console.log("User is logged in!");
        //console.log(user.email);
        //console.log("Logged in with %o", user);
        //console.log("Current user: %o", firebase.auth().currentUser);
        userListService.register(this.createUser(firebase.auth().currentUser));
        userHelper.currentUser = user;
        this.router.navigate(['homescreen']);
      } else {
        // If there is no user logged in send them to the login page
        //TODO: send to login page
        console.log("User is logged out");
        userHelper.currentUser = null;
        this.router.navigate(['login']);
      }
    });
    this.router.events.subscribe(event => {
      //console.log("Router event: %o", event);
      if (event instanceof NavigationStart) {
        this.noteTitle = "";
      }
    });
  }
  logout() {
    firebase.auth().signOut().then(function() {
      // Sign-out successful.
      console.log("LOGOUT!")
    }, function(error) {
      // An error happened.
      console.log(error.log)
    });
  }
  createUser(user : firebase.User) : User {
    let u : User = new User(user.email, user.uid, user.displayName);
    if (u.name == null) {
      user.providerData.forEach(data => {
        if (data.displayName != null) {
          u.name = data.displayName;
        }
      });
    }
    console.log(`created User, displayName: ${u.name}`);
    return u;
  }
}
