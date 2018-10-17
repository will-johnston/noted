# Database Schemas
The following are the schemas for the database 'tables'

## User
[JSON Schema](../../documentation/user.schema.json)

### Types
The three types of fields in the user 'table' are DOCUMENT/FOLDER/IMAGE case-sensitive.

#### DOCUMENT
1. Type must be 'DOCUMENT'
2. lastEditedBy may be filled in, its value is a userID
3. children must be null

#### FOLDER
1. Type must be 'FOLDER'
2. lastEditedBy must be null
3. children may be non-null and is an array of DOCUMENT/FOLDER types

#### IMAGE
1. lastEditedBy and children must be null

## File
[JSON Schema](file.schema.json)

Note's have their data contents stored seperately in the fileContents/ folder under the same id. So (users/{userid}/{fileid}) has its text stored in (fileContents/{fileid}). Per this decision, deletion/creation takes 2x operations.

File's don't have types, so their type must be implied from the one-way binding of users/ -> fileContents.

### DOCUMENT
1. Text contents of the document are stored in the "data" field as an html encoded string.
