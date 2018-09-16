# Routing

## Declarations
Routes are defined in [app-routing.module.ts](./src/app-routing.module.ts)

## Adding Routes
To add a route, first import the component in [app-routing.module.ts](./src/app-routing.module.ts) and then add the route information to routes (within the same file).

## Route Navigation
Instead of using 
```html
<a href=''>
```
use
```html
<a routerLink=''>
```