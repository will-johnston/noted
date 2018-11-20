# Styles
This document refers to the styling of HTML objects and where they should be defined.

## Colors
With the introduction of the dark mode, a UI element now has two different color states; Light and Dark. This requires two colors for each UI element. In order to handle the two colors, we have two global stylesheets solely for coloring. These are located at:
- `/assets/light.css - For Light colors`
- `/assets/dark.css - For Dark colors`
The stylesheet reloading is handled automatically by the DarkModeService. When coloring an object, a corresponding CSS class/element should be placed in both of these stylesheets with defined colors respectively.

## Component Stylesheets
A component should only get its style from the generated stylesheet associated with itself. A component should load in external stylesheets beyond the global stylesheets already installed. 