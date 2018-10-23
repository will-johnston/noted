# Creating Notifications
Notifications should be created (before they're sent) by the static methods defined in Notifications.Notif. These static methods handle the process of getting emails/names for those involved.

## Custom Notifications
A notification currently has only one field, `text`. So creating a new, and valid, notification is as easy as:
```ts
let notification : Notif = new Notif('ya got notified');
```
However, using static methods is preferable as it keeps notification text consistent.