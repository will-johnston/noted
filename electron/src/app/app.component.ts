import { Component } from '@angular/core';
import { RouterModule, Routes, Router } from '@angular/router';
import * as firebase from 'firebase'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'noted';

  constructor(private router : Router) {
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
        console.log(user.email);
        this.router.navigate(['homescreen']);
      } else {
        // If there is no user logged in send them to the login page
        //TODO: send to login page

      }
    });
  }
}
