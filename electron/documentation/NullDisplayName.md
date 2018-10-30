# NullDisplayName
Per [Issue #37](https://github.com/will-johnston/noted/issues/37), firebase is not returning the name of a user. Until this is fixed, userList.name will be null.

## Workarounds
Must use user.email instead of user.name for anything identifying a user. For example, sending a notification would be `ryaneverett33@outlook.com has ...` instead of the more preferable `Ryan Everett has ... `.