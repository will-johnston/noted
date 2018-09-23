import { Injectable, NgZone } from '@angular/core';
import { Router } from "@angular/router";

import { AngularFireAuth } from 'angularfire2/auth';
import * as firebase from 'firebase/app';
import { Observable } from 'rxjs';

import { ElectronService } from 'ngx-electron'

@Injectable()
export class AuthService {
    private user: Observable<firebase.User>;
    private userDetails: firebase.User = null;

    constructor(private _firebaseAuth: AngularFireAuth, private router: Router, private _electronService: ElectronService, private ngZone: NgZone) {
        this.user = _firebaseAuth.authState;

        this.user.subscribe(
            (user) => {
                if (user) {
                    this.userDetails = user;
                    console.log(this.userDetails);
                }
                else {
                    this.userDetails = null;
                }
            }
        );
    }

    signInWithGoogle() {
        this._electronService.ipcRenderer.send('google-auth', 'ping');
    }

    listenForToken() {
        this._electronService.ipcRenderer.on('token', (event, token) => {
            this.useToken(token);
        });
    }

    useToken(gAccessToken) {
        var credential = firebase.auth.GoogleAuthProvider.credential(gAccessToken.id_token);
        var refreshToken = gAccessToken.refresh_token;
    
        console.log(credential);
        // Sign in with credential from the Google user.
        firebase.auth().signInWithCredential(credential).then(() => {
            this.ngZone.run(() => {
                // do once logged in
                this.router.navigate(['homescreen']);
            })
        }).catch(function (error) {
          if (error != null) {
            alert("ERROR REGISTERING ACCOUNT AT " + error.lineNumber);
            console.log(error.message);
            return;
          }
        });
      }

    isLoggedIn() {
        if (this.userDetails == null) {
            return false;
        } else {
            return true;
        }
    }

    logout() {
        this._firebaseAuth.auth.signOut()
            .then((res) => this.router.navigate(['/']));
    }
}