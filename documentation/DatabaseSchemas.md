# Database Schemas
The following are the schemas for the database 'tables'

## Information Covered
More indepth documentation can be found [here](/electron/documentation/DatabaseSchemas.md) as this document will cover some proposed changes.

## User
[JSON Schema](user.schema.json)

### Sharing & Notifications Changes
These are the proposed changes to the users/ object relating to how notes are shared and notifications are handled.

#### notifications[]
The notifications array is meant to be a simple and fast way to quickly add and remove (once dismissed) notifications. Notifications are not meant to linger. Once a notification is viewed by the user, it should be removed from this list. 

Fields:
1. text
- What the notification will say. See [Notifications](Notifications.md) for "standard messages".
2. type
- Currently null. Not sure if this is needed, but may be used in the future.

#### shared[]
The shared array is meant as a way for the filesystem to display notes shared with the user. It is meant to be similar to Google Drive's "Shared with Me" tab. Whenever a note is shared with a user, this array must be updated. Whenever a shared note is deleted, this array must be updated to reflect the change. The filesystem must listen for changes to handle these events.

When to update the array:
1. Sharing a new note
2. Deleting the shared note
3. Changing the title of the note

Fields:
1. title
- The title of the note, what the filesystem will show it as.
- Ex: `a study in scarlet`
2. path
- The full path of the note. Used in updating lastEditedBy as well as other note metadata.
- Ex: `users/vs1RclX9B9cM4rkQcvWb5CZuMur1/-LO3tpAXVFl_yHTd4ZuY/children/-LO3uMMyWJReguUJtdem`
3. filePath
- the full path to the file contents of the note.
- Ex: `fileContents/-LO3uMMyWJReguUJtdem`
4. owner
- The userID of the person who created this note, doesn't have to be the person that shared it to you.
- Ex: `vs1RclX9B9cM4rkQcvWb5CZuMur1`
5. noteID
- the elementID of the shared note.
- Ex: `-LO3uMMyWJReguUJtdem`

#### lastEditedBy
Never null. This field stores the userID of whoever edited the note last. When the note is first created, lastEditedBy is set to the userid of the creator. 

## UserList
[JSON Schema](userList.schema.json)

### Description
The userList is used as a centralized way to get information about any user at a given time. This is critical for sharing notes as individual user data must be updated. Whenever a user first uses noted, they are added to this list.

### Proposed Service API
1. `register(User) : void`
Checks if a user is in the userList
- If not in the list, the user and their details are added to the list
- If in the list, do nothing
2. `search(User) : User`
Searches for a User based on given search criteria and returns the full User info based if found
- Criteria: username || email
- Returns username, email, id, name || null
3. `get(userID) : User`
Returns a User with a given id.
- Returns username, email, id, name || null

### Issues
1. Search is expensive
The currently implementation only allows for linear searching (O(n)) and may require retrieving the entire list. This sucks, but may not be a big issue for our low-scale project.
- Could be lessened with caching (implemented as a cloud function)
- Could use an additional SQL based database hosted elsewhere to get better search performance
- Could restructure to use binary search
- Could use [ElasticSearch](https://www.elastic.co/products/elasticsearch)

## File
[JSON Schema](file.schema.json)

### Sharing and Notification Changes