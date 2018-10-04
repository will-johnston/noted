import { MatButtonModule, MatMenuModule, MatCheckboxModule, MatGridList, MatGridListModule, MatListModule } from '@angular/material';
import { MatIconModule, MatToolbarModule } from '@angular/material';
import { NgModule } from '@angular/core';
@NgModule({
    imports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridListModule,
        MatIconModule,
    ],
    exports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridList, 
        MatGridListModule,
        MatIconModule,
        MatListModule,
        MatMenuModule,
        MatToolbarModule
    ]
})

export  class  MyMaterialModule { }