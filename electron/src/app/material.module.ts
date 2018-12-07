import { MatButtonModule, MatMenuModule, MatCheckboxModule, MatGridList, MatGridListModule, MatListModule, MatBadgeModule, MatSnackBarModule } from '@angular/material';
import { MatIconModule, MatToolbarModule, MatDialogModule, MatFormFieldModule, MatInputModule } from '@angular/material';
import { NgModule } from '@angular/core';
@NgModule({
    imports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridListModule,
        MatIconModule,
        MatBadgeModule
    ],
    exports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridList, 
        MatGridListModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatToolbarModule,
        MatBadgeModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule
    ]
})

export  class  MyMaterialModule { }