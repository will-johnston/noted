# Services
This is a collection of common services for general usage. Some API functions may not be implemented yet and should be documented as such (TBI).

## File System
This service is a genuine mess and should be reworked altogether, so I'm not going to spend time documenting it. This is Ryan's monstrosity, just let him patch the pieces up.

## Notifications
This service is responsible for sending and destroying notifications (TBI). It does not handling retrieving notifications (see [Note](/electron/documentation/RetrievingNotifications.md)).
### API
- notify()

Sends a notification to a user with a given Notifications.Notif object.
- clearNotification() (TBI)

Deletes a notification from a user.
### Types
- Notifications.Notif

Represents a notification stored in the database. Should be used for creating notifications to be sent as well. 

  #### Methods
  ShareNoteNotification() : Creates a new notification for sharing notes. The created notification is then sent by NotificationsService.notify()
  
  Ex: `Sally has shared 'prisoners' with you`

## Sharing
This service is responsible for sharing notes, deleting shared notes (TBI), and getting users who have been shared the note
### API
- getSharedUsers()

Gets all the users that have been shared a given note.
- shareNote()

Shared a note from one user to another user. Calls notify() to inform the new user.
- deleteSharedNote() (TBI)

Deletes a shared Note and removes share access from all users.
### Types
- Sharing.SharedNote
Represents a shared note stored in the database. Useful for retrieving the filePath of a note or the note name.

## UserList
This service is responsible for registering, searching for, and getting users from the userList database object.
### API
- register()

Registers a user to the userList if they are not already on the list.
- search()

Searches for userInfo based on given criteria. ([IMPORTANT NOTE](/electron/documentation/NullDisplayName.md))
Ex: `search 'ryaneverett33@outlook.com' -> UserList.User(email, name, userID)`
As of time of writing, any searches based on name will fail.
- get()

Gets user info given the userID.
### Types
- UserList.User

Not to be confused with firebase.User!! This type represents a stored user info in the userList object.

## Theme
This Service is an rxjs service that changes theme dynamically. This user story is stored in the branch UI-theme and is not yet merged with the master due to the style configuration change from css to scss. All css file can work as scss file. Will merge after some refactoring with other functionalities.
