import { Component } from '@angular/core';
import { RouterModule, Routes, Router, NavigationStart } from '@angular/router';
import * as firebase from 'firebase'
import { User } from './services/UserList.User';
import { AngularFireDatabase } from '@angular/fire/database';
import { Notif } from './services/Notifications.Notif';
import { isNullOrUndefined } from 'util';
import { NotificationsService } from './services/notifications.service';
import { DarkModeService, DarkModeState } from './services/dark-mode.service';
import { UserListService } from './services/user-list.service';
import { UserHelperService } from './services/userhelper.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'noted';
  public noteTitle = "";
  public notifications : Notif[] = Array<Notif>();
  private notificationsRef : any;
  public darkModeIcon : string = "wb_incandescent";    //wb_incandescent or wb_iridescent

  constructor(private router : Router,
    private userListService : UserListService,
    private userHelper : UserHelperService,
    private fireDatabase: AngularFireDatabase, 
    private notificationsService: NotificationsService,
    private darkModeService : DarkModeService) {
    darkModeService.state == DarkModeState.Light ? this.darkModeIcon = "wb_incandescent" : this.darkModeIcon = "wb_iridescent";
    
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
        this.userListService.register(this.createUser(firebase.auth().currentUser));
        this.userHelper.currentUser = user;
        this.listenForNotifications();
        this.router.navigate(['homescreen']);
      } else {
        // If there is no user logged in send them to the login page
        //TODO: send to login page
        console.log("User is logged out");
        userHelper.currentUser = null;
        this.stopListeningForNotifications();
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
  _containsNotification(notification : Notif) : boolean {
    for (let i = 0; i < this.notifications.length; i++) {
      let tmp : Notif = this.notifications[i];
      if (tmp.text == notification.text)
        return true;
    }
    return false;
  }

  //handle notifications
  listenForNotifications() {
    this.notificationsRef = this.fireDatabase.list('users/' + firebase.auth().currentUser.uid + '/notifications').valueChanges();
    this.notificationsRef.subscribe(values => {
      this.notifications.slice(0, this.notifications.length);
      for (let i = 0; i < values.length; i++) {
        let data = values[i];
        console.log("Data: %o", data);
        if (isNullOrUndefined(data) || data.text == null)
          continue;
        //add notification
        let notification : Notif = new Notif(data.text);
        notification.type = data.type;
        if (!this._containsNotification(notification))
          this.notifications.push(notification);
      }
    });
  }
  stopListeningForNotifications() {
    try {
      this.notificationsRef.unsubscribe();
    }
    catch (err) {}
  }
  clearNotification(notification : Notif) {
    this.notificationsService.clearNotification(firebase.auth().currentUser.uid, notification)
    .then((success) => {
      //remove notification from array
      for (var i = 0; i < this.notifications.length; i++) {
        var tmp : Notif = this.notifications[i];
        if (tmp.text == notification.text) {
          this.notifications.splice(i,1);
        }
      }
    })
    .catch(() => {
      //don't remove it
    });
  }
  darkModeToggle() {
    /*if (this.darkModeIcon == "wb_incandescent")
      this.darkModeIcon = "wb_iridescent"
    else
      this.darkModeIcon = "wb_incandescent"*/
    this.darkModeService.state == DarkModeState.Light ? this.darkModeIcon = "wb_incandescent" : this.darkModeIcon = "wb_iridescent";
    this.darkModeService.Toggle();
  }
}
