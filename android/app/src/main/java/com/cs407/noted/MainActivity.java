package com.cs407.noted;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.getbase.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;  // this will store the database reference at the current path
    private DatabaseReference fileContents;
    private List<SharedFile> sharedFiles;
    private List<com.cs407.noted.File> convertedSharedFiles;

    private com.cs407.noted.File root;
    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int CAMERA_REQUEST = 3;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 4;
    private static final int BOTH_REQUEST = 5;
    private Uri imageUri;
    private File output=null;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();
        setupRecyclerView();
        setUpFloatingActionMenu();
        firebaseStorage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        sharedFiles = new ArrayList<>();
        if (checkLogin()) {
            // user is logged in so we can access their data
            setupDatabase();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //check if user is logged in
        if (checkLogin()) {
            int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
            int storagePermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (cameraPermission == PackageManager.PERMISSION_DENIED && storagePermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, BOTH_REQUEST);
            } else if (cameraPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
            } else if (storagePermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    private boolean checkLogin() {
        // get instance of firebase authenticator
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            //show login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return false;
        } else {
            verifyUserPII(currentUser);
            return true;
        }
    }

    private void verifyUserPII(final FirebaseUser user) {
        // verifies that the users personally identifiable info is in the database
        String path = String.format("userList/%s", user.getUid());
        final DatabaseReference userListRef = database.getReference(path);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("id")) {
                    // The user isn't in the database
                    addUserPII(user, userListRef);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        userListRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void addUserPII(FirebaseUser user, DatabaseReference userListRef) {
        User userObj = new User(user.getDisplayName(), user.getEmail(), user.getUid());
        userListRef.setValue(userObj);
    }

    private void onSignOut() {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        //app crashes here because the client variable doesn't persist
        GoogleAuthSingleton.getInstance().client.signOut();

        //show login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        root = new com.cs407.noted.File("root", "", "noted", null, null, FileType.FOLDER.toString(), null);
        root.setId("root");
        listAdapter = new ListAdapter(null, root);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDatabase() {

        // database setup
        fileContents = database.getReference("fileContents");
        String path = String.format("users/%s", currentUser.getUid());
        this.myRef = database.getReference(path);
        DatabaseReference filesRef = database.getReference(path);
        // add listener for file system
        filesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<com.cs407.noted.File> files = new ArrayList<>();
                ArrayList<SharedFile> shared = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals("shared")) {
                        // we have reached the shared items
                        for (DataSnapshot ds2: ds.getChildren()) {
                            SharedFile sharedFile = ds2.getValue(SharedFile.class);
                            shared.add(sharedFile);
                        }
                    } else {
                        com.cs407.noted.File file = ds.getValue(com.cs407.noted.File.class);
                        files.add(file);
                    }
                }
                // calls to convert the shared files to regular files, then add them to the view
                convertSharedFilesUpdateItemList(files, shared);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void convertSharedFilesUpdateItemList(List<com.cs407.noted.File> files,
                                                  List<SharedFile> shared) {
        int size = shared.size();
        if (size == 0) {
            updateView(files);
        } else {
            this.convertedSharedFiles = new ArrayList<>();
            for (SharedFile sharedFile : shared) {
                if (!alreadyListening(sharedFile)) {
                    DatabaseReference databaseReference = database.getReference(sharedFile.getPath());
                    // get data of shared file
                    databaseReference.addValueEventListener(
                            getValueEventListenerForConvertingSharedFiles(size, files, sharedFile));
                }
            }
        }

    }

    private boolean alreadyListening(SharedFile sharedFile) {
        for (SharedFile file: sharedFiles) {
            if (file.getNoteID().equals(sharedFile.getNoteID())) {
                return true;
            }
        }
        return false;
    }

    private ValueEventListener getValueEventListenerForConvertingSharedFiles(
            final int size, final List<com.cs407.noted.File> files, final SharedFile sharedFile) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // get the file
                com.cs407.noted.File convertedFile = dataSnapshot.getValue(com.cs407.noted.File.class);
                if (alreadyListening(sharedFile)) {
                    //convertedFile.setParent(root);
                    com.cs407.noted.File convertedAndUpdatedFile = setFileParents(convertedFile, root);
                    listAdapter.triggerUpdate(convertedAndUpdatedFile);
                }

                if (convertedFile != null) {
                    convertedSharedFiles.add(convertedFile);
                    sharedFiles.add(sharedFile);
                    if (convertedSharedFiles.size() == size) {
                        // we have added all the files we need, so now we update the recycler view
                        convertedSharedFiles.addAll(files);
                        // add files as root's children
                        updateView(convertedSharedFiles);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    public void updateView(List<com.cs407.noted.File> files) {
        root.setChildren(null);
        // update parents
        List<com.cs407.noted.File> updatedFiles = new ArrayList<>();
        for (com.cs407.noted.File file : files) {
            root.addChild(file);
            updatedFiles.add(setFileParents(file, root));
        }
        listAdapter.setItemListMaintainCurrentDirectory(updatedFiles);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                listAdapter.goToParentDirectory();
                updateDatabaseRefBackwards();
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                onSignOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpFloatingActionMenu() {
        final FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.fam);
        final FloatingActionButton action_folder = (FloatingActionButton) findViewById(R.id.action_folder);
        final FloatingActionButton action_doc = (FloatingActionButton) findViewById(R.id.action_doc);
        final FloatingActionButton action_select_image = (FloatingActionButton) findViewById(R.id.action_select_image);
        final FloatingActionButton action_take_picture = (FloatingActionButton) findViewById(R.id.action_take_picture);
        action_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respondToFolderOrDocumentClick(floatingActionsMenu, FileType.FOLDER);
            }
        });
        action_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                respondToFolderOrDocumentClick(floatingActionsMenu, FileType.DOCUMENT);
            }
        });
        action_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        action_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                try {
                    File tempFile = File.createTempFile("my_app", ".jpg");
                    imageUri = Uri.fromFile(tempFile);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File dir =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    output = new File(dir, "picture.jpeg");
                    imageUri = Uri.fromFile(output);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, TAKE_PICTURE);
                }
                catch(IOException e) {

                }
            }
        });
    }


    private com.cs407.noted.File setFileParents(com.cs407.noted.File file, com.cs407.noted.File parent) {
        if (file == null) { return null; }
        // set parent
        file.setParent(parent);
        Map<String, com.cs407.noted.File> children = file.getChildren();
        // update children's parents
        if (children != null) {
            Map<String, com.cs407.noted.File> newKids = new HashMap<>();
            Set keys = children.keySet();
            for (Object key : keys) {
                com.cs407.noted.File child = children.get(key);
                child = setFileParents(child, file);
                newKids.put((String) key, child);
            }
            file.setChildren(newKids);
        }
        return file;
    }

    public void toggleHomeButton(boolean enable) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
        getSupportActionBar().setDisplayShowHomeEnabled(enable);
    }

    public void changeActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /* used for when a user clicks to go to a child directory */
    public void updateDatabaseRefForward(String child) {
        // move reference to the file, and then to the children attribute
        myRef = myRef.child(child).child("children");
    }

    /* used for when a user clicks to go to parent directory */
    public void updateDatabaseRefBackwards() {
        // we want to do this twice, since we go from children attribute to folder
        // and then folder to actual parent dir
        myRef = myRef.getParent().getParent();
    }


    public void removeFile(com.cs407.noted.File file, com.cs407.noted.File parent) {
        try {
            String id = file.getId();
            // check to see if file is a shared file
            for (SharedFile sf: sharedFiles) {
                if (sf.getNoteID().equals(file.getId())) {
                    // remove value event listeners for file
                    // DatabaseReference databaseReference = database.getReference(sf.getPath());
                    // databaseReference.removeEventListener(getValueEventListenerForConvertingSharedFiles());
                    // it is a shared file
                    searchAndRemoveFileFromShared(file.getId(), currentUser.getUid());
                    // remove user from sharedUsers for the file
                    removeUserFromSharedUsers(file.getId(), currentUser.getUid());
                    return;
                }
            }
            // if we got here, we know the current user is the owner
            // remove all shared users from file
            findAndRemoveSharedUsersFromFile(file.getId());
            // now remove file from current users file system and from fileContents in firebase
            myRef.child(id).removeValue(getDatabaseCompletionListener(file));
            fileContents.child(id).removeValue();
            removeUserFromSharedUsers(file.getId(), currentUser.getUid());
        } catch (DatabaseException e) {
            e.printStackTrace();
            String err = String.format("Failed to remove %s", file.getTitle());
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        }
    }

    private void findAndRemoveSharedUsersFromFile(final String fileID) {
        String path = String.format("fileContents/%s/sharedUsers", fileID);
        DatabaseReference df = database.getReference(path);
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String userID = (String) ds.getValue();
                    searchAndRemoveFileFromShared(fileID, userID);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void removeUserFromSharedUsers(String fileID, final String userID) {
        String path = String.format("fileContents/%s/sharedUsers", fileID);
        final DatabaseReference df = database.getReference(path);
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = null;
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.getValue().equals(userID)) {
                        // we found it
                        key = ds.getKey();
                        break;
                    }
                }
                if (key != null) {
                    // then we found the user and can remove it
                    try {
                        df.child(key).removeValue();
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private DatabaseReference.CompletionListener getDatabaseCompletionListener(final com.cs407.noted.File file) {
        final Context context = this;
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                String succ = String.format("Removed %s", file.getTitle());
                Toast.makeText(context, succ, Toast.LENGTH_SHORT).show();
            }
        };
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_CANCELED) {
            return;
        }

        if(requestCode == PICK_IMAGE) {
            final Uri imageUri = data.getData();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Enter Image Name");

            // Set up the input
            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String input_text = input.getText().toString();
                    int icon = R.drawable.image;
                    com.cs407.noted.File file = new com.cs407.noted.File(
                            null, null, input_text, null, null, FileType.IMAGE.toString(), null);
                    listAdapter.addNewFile(file, myRef, currentUser.getUid(), fileContents, sharedFiles, database);

                    //upload the picture to Firebase storage
                    StorageReference ref = firebaseStorage.getReference().child("androidImages/" + file.getId());
                    try {
                        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(imageUri);
                        ref.putStream(inputStream);
                    }
                    catch(FileNotFoundException e1) {
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        else if(requestCode == TAKE_PICTURE) {
            final ContentResolver cr = this.getContentResolver();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Enter Image Name");

            // Set up the input
            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String input_text = input.getText().toString();
                    com.cs407.noted.File file = new com.cs407.noted.File(
                            null, null, input_text, null, null, FileType.IMAGE.toString(), null);
                    listAdapter.addNewFile(file, myRef, currentUser.getUid(), fileContents, sharedFiles, database);

                    try {
                        //rotate the image
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                        //upload the picture to Firebase storage
                        StorageReference ref = firebaseStorage.getReference().child("androidImages/" + file.getId());
                        ref.putBytes(baos.toByteArray());
                    }
                    catch(IOException e) {}
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }


    private void respondToFolderOrDocumentClick(FloatingActionsMenu floatingActionsMenu, final FileType type) {
        floatingActionsMenu.collapse();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        if (type.equals(FileType.DOCUMENT)) {
            builder.setTitle("Enter Document Name");
        } else {
            builder.setTitle("Enter Folder Name");
        }

        // Set up the input
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addFolderOrDocument(input.getText().toString(), type);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    public boolean addFolderOrDocument(String text, FileType type) {

        if (! (type.equals(FileType.DOCUMENT) || type.equals(FileType.FOLDER)) ) {
            return false;
        }
        TextLength verify = checkTextLength(text);
        if (verify.equals(TextLength.NIL) || verify.equals(TextLength.EMPTY)) {
            if (type.equals(FileType.DOCUMENT)) {
                text = "Untitled document";
            } else {
                text = "Untitled folder";
            }
        }

        // now that we added valid title to NIL and EMPTY, we can add new file for those and VALID
        if (!verify.equals(TextLength.TOO_LARGE)) {
            // if length is between 0 and 255, we will add the file
            String input_text = text;
            com.cs407.noted.File file = new com.cs407.noted.File(
                    null, null, input_text, null, null,
                    type.toString(), null);
            listAdapter.addNewFile(file, myRef, currentUser.getUid(), fileContents, sharedFiles, database);
            return true;
        } else {
            // the text is too large, so don't accept it
            Toast.makeText(this, "File name is too long", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private TextLength checkTextLength(String text) {
        if (text == null) {
            return TextLength.NIL;
        }
        int len = text.length();
        if (len < 1) {
            return TextLength.EMPTY;
        }
        else if (len > 255) {
            return TextLength.TOO_LARGE;
        } else {
            return TextLength.VALID;
        }


    }

    public String getName() {
        return this.getName();
    }

    public com.cs407.noted.File getRoot() {
        return root;
    }


    /* SHARING */


    /*
    Here, the function loads the list of users, and then calls for the search dialog to show
    Note this function takes in a file that we want to share. This is because when the user to
    share with is selected, we will share the file with him/her
    file.
    TODO: change from file to reference
    TODO: share even if you're not the owner
     */
    public void loadSearchDialog(final com.cs407.noted.File file) {
        //TODO: add loading bar
        String path = "userList";
        final DatabaseReference userListRef = database.getReference(path);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<SearchModel> items = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    // add all users except the current user
                   if (user != null && !user.getId().equals(currentUser.getUid())) {
                        String userTitle;
                        if (user.getName() != null) {
                             userTitle = String.format("%s (%s)", user.getEmail(), user.getName());
                        }  else {
                            userTitle = String.format("%s", user.getEmail());
                        }
                        SearchModel searchModel = new SearchModel(userTitle, user.getId());
                        items.add(searchModel);
                   }
                }
                // make sure there are items that we can search for
                if (items.size() == 0) {
                    Toast.makeText(MainActivity.this, "No users to share with!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showSearchDialog(items, file);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        userListRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void showSearchDialog(ArrayList<SearchModel> items, final com.cs407.noted.File file) {
        new SimpleSearchDialogCompat<>(MainActivity.this, "Search Users",
                "Enter a user's name or email",null, items,
                new SearchResultListener<SearchModel>() {
                    @Override
                    public void onSelected(BaseSearchDialogCompat dialog, SearchModel item, int position) {
                        convertSharedFileThenVerifyAndShare(file, item.getId());
                        dialog.dismiss();
                    }
                }).show();
    }

    private void convertSharedFileThenVerifyAndShare(com.cs407.noted.File file, String userID) {
        // convert file to SharedFile
        String title = file.getTitle();
        String fileContentsPath = String.format("fileContents/%s", file.getId());
        String noteID = file.getId();
        // setting the path to null, because we set that information later
        SharedFile sharedFile = new SharedFile(title, null, fileContentsPath, noteID);

        // make sure the file isn't already shared with the user
        // then once that's verified, share the file
        verifyAndShare(file, userID, sharedFile);
    }


    private void verifyAndShare(final com.cs407.noted.File file, final String userID, final SharedFile sharedFile) {
        List<String> IDList = flattenFileStructureExtractIDsExcludeParent(file);


        ValueEventListener valueEventListenerForSharedChildren =
                getValueEventListenerForSharedChildren(userID);
        ValueEventListener valueEventListenerForSharedFile =
                getValueEventListenerForSharedFile(file, userID, sharedFile);

        // make sure all the parents children aren't already shared with the user as well
        // and if so, remove it from the 'shared' list
        for (String id: IDList) {
            String path = String.format("fileContents/%s", id);
            final DatabaseReference sharedFilesRef = database.getReference(path);
            sharedFilesRef.addListenerForSingleValueEvent(valueEventListenerForSharedChildren);
        }
        String path = String.format("fileContents/%s", file.getId());
        final DatabaseReference sharedFilesRef = database.getReference(path);
        sharedFilesRef.addListenerForSingleValueEvent(valueEventListenerForSharedFile);
    }

    // value event listener used to remove files from a user A's "shared" list if the file being
    // shared with A is the parent of another shared file
    private ValueEventListener getValueEventListenerForSharedChildren(final String userID) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ownerID = null;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals("sharedUsers")) {
                        // make sure the user is not already on the shared users list
                        for (DataSnapshot d1: ds.getChildren()) {
                            String sharedID = (String) d1.getValue();
                            if (sharedID.equals(userID)) {
                                // user has already been shared this file, remove it from shared
                                searchAndRemoveFileFromShared(dataSnapshot.getKey(), userID);
                                return;
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
    }

    // value event listener to verify that the file has not already been shared with the user
    // if it hasn't call the shareFile method
    private ValueEventListener getValueEventListenerForSharedFile(final com.cs407.noted.File file,
                                                                  final String userID,
                                                                  final SharedFile sharedFile) {
        final Context context = this;
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ownerID = null;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals("sharedUsers")) {
                        // make sure the user is not already on the shared users list
                        for (DataSnapshot d1: ds.getChildren()) {
                            String sharedID = (String) d1.getValue();
                            if (sharedID.equals(userID)) {
                                // user has already been shared this file
                                Toast.makeText(context, "User already has access this file!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    } else {
                        // get owner id here
                        ownerID = (String) ds.getValue();
                    }
                }
                // if we get here, the user doesn't already have access, so share it
                shareFile(file, userID, sharedFile, ownerID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

    }

    // find the file in the shared folder for the user, then remove it
    private void searchAndRemoveFileFromShared(final String fileID, final String userID) {
        String path = String.format("users/%s/shared", userID);
        DatabaseReference df = database.getReference(path);
        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    SharedFile sf = ds.getValue(SharedFile.class);
                    String id = sf.getNoteID();
                    if (id.equals(fileID)) {
                        String path = String.format("users/%s/shared/%s", userID, ds.getKey());
                        DatabaseReference df = database.getReference(path);
                        df.removeValue();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private List<String> flattenFileStructureExtractIDsExcludeParent(com.cs407.noted.File file) {
        ArrayList<String> list = new ArrayList<>();
        Queue<com.cs407.noted.File> queue = new LinkedList<>();
        queue.add(file);

        while (!queue.isEmpty()) {
            com.cs407.noted.File current = queue.poll();
            if (current.getId() != file.getId()) {
                list.add(current.getId());
            }
            if (current.getChildren() != null) {
                queue.addAll(current.getChildren().values());
            }
        }
        return list;
    }

    private void shareFile(com.cs407.noted.File file, String userID, SharedFile sharedFile,
                           String ownerID) {
        if (currentUser.getUid().equals(ownerID)) {
            // this means the current user is also the owner, so we get the current user's path
            // to the file and add it to the shared file
            sharedFile.setPath(getOwnerPath(file, ownerID));
        } else {
            // we need to find the file id in the current user's shared file's list
            // and then get the correct ownerPath
            for (SharedFile sf: this.sharedFiles) {
                if (sf.getNoteID().equals(file.getId())) {
                    // we have found the file to add
                    sharedFile.setPath(sf.getPath());
                }
            }
            // if we can't find it, abort
            if (sharedFile.getPath() == null) {
                return;
            }
        }
        // create reference for the shared user
        String refPath = String.format("users/%s/shared", userID);
        DatabaseReference sharedRef = database.getReference(refPath);
        // add to 'shared' list in user's file system
        sharedRef.push().setValue(sharedFile);

        // add user to shared files list for the file and its children
        addUserToSharedFilesList(file, userID);
        Toast.makeText(this, "File shared!", Toast.LENGTH_SHORT).show();
    }

    private void addUserToSharedFilesList(com.cs407.noted.File file, String userID) {
        String path = String.format("fileContents/%s/sharedUsers", file.getId());
        DatabaseReference databaseReference = database.getReference(path);
        databaseReference.push().setValue(userID);
        List<com.cs407.noted.File> children;

        // recursively add user to file's children's shared list as well
        if (file.getChildren() != null) {
            children = new ArrayList<>();
            children.addAll(file.getChildren().values());
            for (com.cs407.noted.File child: children) {
                addUserToSharedFilesList(child, userID);
            }
        }

    }

    private String getOwnerPath(com.cs407.noted.File file, String uid) {
        String path = file.getId();
        com.cs407.noted.File fileRef = file;
        String id;

        // add to the files path starting from the from bottom ((now we here)) in reverse order
        while (! (id = fileRef.getParent().getId()).equals("root")) {
            path = String.format("%s/children/%s", id, path);
            fileRef = fileRef.getParent();
        }

        // add the root of the ref
        path = String.format("users/%s/%s", uid, path);
        return path;
    }



}
