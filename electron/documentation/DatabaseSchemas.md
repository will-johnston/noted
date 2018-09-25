# Database Schemas
The following are the schemas for the database 'tables'

## User
(JSON Schema)[user.schema.json]

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
- Not sure if we need to store files in the database