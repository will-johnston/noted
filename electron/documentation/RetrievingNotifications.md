# Retrieving Notifications
Per the current UI design, retrieving notifications should be handled by the AppComponent. The AppComponent is persistent across the Application Lifecycle and already contains the UI elements for a notification drawer.

## How
1. Subscribe to /users/{userID}/notifications and listen for events (snapshotChanges)
2. For each event, check the type
    - If it's an add event
        - If the notification doesn't already exist, add the notification to the notification drawer 
    - If it's a delete event
        - Remove the notification from the notification drawer

## Must Haves
1. A working notification drawer, the basic HTML is already in app.component.html
2. A clear notification button on the notification object within the drawer

## Suggested Improvements
It would be great to have toast notifications popup whenever a user gets a notification but this may require changes to NotificationsService and the database schema.