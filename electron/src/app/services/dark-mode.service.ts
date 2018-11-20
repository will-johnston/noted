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
  constructor() { 
    this.state = DarkModeState.Light;
    this.__updateStyleSheet();
  }
  private __updateStyleSheet() {
    switch (this.state) {
      case DarkModeState.Light:
        (<HTMLLinkElement>document.getElementById("colorSheet")).href = this.lightColors;
        return;
      case DarkModeState.Dark:
        (<HTMLLinkElement>document.getElementById("colorSheet")).href = this.darkColors;
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
