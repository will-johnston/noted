import { Injectable } from '@angular/core';

export enum DarkModeState {
  Dark,
  Light
}
@Injectable({
  providedIn: 'root'
})
export class DarkModeService {
  public state : DarkModeState;
  private darkColors : string = "/assets/dark.css";
  private lightColors : string = "/assets/light.css";
  private darkTheme : string = '../../node_modules/@angular/material/prebuilt-themes/deeppurple-amber.css';
  private lightTheme : string = "../../node_modules/@angular/material/prebuilt-themes/indigo-pink.css";
  constructor() { 
    this.state = DarkModeState.Light;
    this.__updateStyleSheet();
  }
  private __updateStyleSheet() {
    switch (this.state) {
      case DarkModeState.Light:
        (<HTMLLinkElement>document.getElementById("colorSheet")).href = this.lightColors;
        (<HTMLLinkElement>document.getElementById("angularMaterialTheme")).href = this.lightTheme;
        return;
      case DarkModeState.Dark:
        (<HTMLLinkElement>document.getElementById("colorSheet")).href = this.darkColors;
        (<HTMLLinkElement>document.getElementById("angularMaterialTheme")).href = this.darkTheme;
        return;
      default:
        console.error(`DarkModeService encountered an invalid state: ${this.state}`);
    }
  }
  public Toggle() : void {
    if (this.state == DarkModeState.Light)
      this.state = DarkModeState.Dark;
    else
      this.state = DarkModeState.Light;
    this.__updateStyleSheet();
  }
}
