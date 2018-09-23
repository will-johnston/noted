import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app-routing.module';
import { LoginComponent } from './login/login.component';
import { HomescreenComponent } from './homescreen/homescreen.component';
import { NoteComponent } from './note/note.component';

import { environment } from '../environments/environment';
import { AngularFireModule } from 'angularfire2';
import { AngularFireDatabaseModule } from 'angularfire2/database';
import { AngularFireAuthModule } from 'angularfire2/auth';
import { MyMaterialModule } from './material.module'; 

import {NgxElectronModule} from 'ngx-electron'

import { AuthService } from './services/auth.service';
import { FilesystemService } from './services/filesystem.service';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomescreenComponent,
    NoteComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    AngularFireModule.initializeApp(environment.firebase, 'angular-auth-firebase'),
    AngularFireDatabaseModule,
    AngularFireAuthModule,
    NgxElectronModule,
    MyMaterialModule
  ],
  providers: [AuthService, FilesystemService ],
  bootstrap: [AppComponent]
})
export class AppModule { }
