import { MatButtonModule, MatCheckboxModule, MatGridList, MatGridListModule, MatListModule } from '@angular/material';
import { MatIconModule } from '@angular/material';
import { NgModule } from '@angular/core';
@NgModule({
    imports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridListModule,
        MatIconModule
    ],
    exports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridList, 
        MatGridListModule,
        MatIconModule,
        MatListModule
    ]
})

export  class  MyMaterialModule { }