import { MatButtonModule, MatMenuModule, MatCheckboxModule, MatGridList, MatGridListModule, MatListModule, MatBadgeModule } from '@angular/material';
import { MatIconModule, MatToolbarModule } from '@angular/material';
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
        MatBadgeModule
    ]
})

export  class  MyMaterialModule { }