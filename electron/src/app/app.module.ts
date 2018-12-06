import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppRoutingModule } from './app-routing.module';
import { LoginComponent } from './login/login.component';
import { HomescreenComponent, CreateFolderDialog, CreateNoteDialog } from './homescreen/homescreen.component';
import { NoteComponent, ImageDialog, ShareDialog, ShareFailureSnack, ShareSuccessSnack } from './note/note.component';

import { environment } from '../environments/environment';
import { AngularFireModule } from 'angularfire2';
import { AngularFireDatabaseModule } from 'angularfire2/database';
import { AngularFireAuthModule } from 'angularfire2/auth';
import { AngularFireStorageModule } from 'angularfire2/storage';
import { FormsModule } from '@angular/forms'
import { MyMaterialModule } from './material.module';


import { NgxElectronModule } from 'ngx-electron';
import { QuillModule } from 'ngx-quill';

import { AuthService } from './services/auth.service';
import { FilesystemService } from './services/filesystem.service';

import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';
import { ConfirmationDialogService } from './confirmation-dialog/confirmation-dialog.service';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomescreenComponent,
    NoteComponent,
    ConfirmationDialogComponent,
    ImageDialog,
    ShareDialog,
    ShareFailureSnack,
    ShareSuccessSnack,
    CreateFolderDialog,
    CreateNoteDialog
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    AngularFireModule.initializeApp(environment.firebase, 'angular-auth-firebase'),
    AngularFireDatabaseModule,
    AngularFireAuthModule,
    AngularFireStorageModule,
    NgxElectronModule,
    MyMaterialModule,
    QuillModule,
    HttpClientModule,
    RouterModule,
    FormsModule,
    NgbModule.forRoot()
  ],
  entryComponents : [
    ConfirmationDialogComponent,
    ImageDialog,
    ShareDialog,
    ShareFailureSnack,
    ShareSuccessSnack,
    CreateFolderDialog,
    CreateNoteDialog
  ],
  providers: [AuthService, FilesystemService, ConfirmationDialogService],
  bootstrap: [AppComponent]
})
export class AppModule { }
