import { NgModule } from '@angular/core';
import { RouterModule, Routes, Router } from '@angular/router';
import { HomescreenComponent } from './homescreen/homescreen.component';
import { LoginComponent } from './login/login.component';
import { NoteComponent } from './note/note.component';

const routes: Routes = [
  { path: '', redirectTo: '/homescreen', pathMatch: 'full' },
  { path: 'homescreen', component: HomescreenComponent},
  { path: 'login', component: LoginComponent},
  { path: 'note', component: NoteComponent },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes),
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule { 
  
}
