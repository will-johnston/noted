import { MatButtonModule, MatCheckboxModule, MatGridList, MatGridListModule } from '@angular/material';
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
        MatIconModule
    ]
})

export  class  MyMaterialModule { }