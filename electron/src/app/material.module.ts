import { MatButtonModule, MatCheckboxModule, MatGridList, MatGridListModule } from '@angular/material';
import { NgModule } from '@angular/core';
@NgModule({
    imports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridListModule
    ],
    exports : [
        MatButtonModule, 
        MatCheckboxModule,
        MatGridList, 
        MatGridListModule
    ]
})

export  class  MyMaterialModule { }