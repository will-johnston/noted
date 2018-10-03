package com.cs407.noted;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.util.Log;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;  // this will store the database reference at the current path
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

        // toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // get instance of firebase authenticator
        firebaseAuth = FirebaseAuth.getInstance();

        //get instance of firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // recycler view setup
        recyclerView = findViewById(R.id.recycler_view);
        root = new com.cs407.noted.File("root", "", "noted", null, null, FileType.FOLDER.toString(), null);
        root.setId("root");
        // root.setHasListener(true);  // we have a child event listener for the first level of file system
        listAdapter = new ListAdapter(null, root);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // fab setup
        setUpFloatingActionMenu();

        //check if user is logged in
        currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            //show login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else {
            String displayName = currentUser.getDisplayName();
            //Snackbar.make(findViewById(R.id.main_layout), "Logged in as " + displayName, Snackbar.LENGTH_SHORT).show();


            // database setup
            database = FirebaseDatabase.getInstance();
            String path = String.format("users/%s", currentUser.getUid());
            this.myRef = database.getReference(path);
            DatabaseReference filesRef = database.getReference(path);
            // add listener
            filesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null) {
                        return;
                    }
                    List<com.cs407.noted.File> files = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        com.cs407.noted.File file = ds.getValue(com.cs407.noted.File.class);
                        // locally set the parents
                        files.add(file);
                    }
                    // add files as root's children
                    root.setChildren(null);

                    // update parent
                    List<com.cs407.noted.File> updatedFiles = new ArrayList<>();
                    for (com.cs407.noted.File file : files) {
                        root.addChild(file);
                        updatedFiles.add(setFileParents(file, root));
                    }

                    listAdapter.setItemListMaintainCurrentDirectory(updatedFiles);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



    @Override
    public void onStart() {
        super.onStart();

        //check if user is logged in
        currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            //show login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {

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
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            //show login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
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
                floatingActionsMenu.collapse();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter Folder Name");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input_text = input.getText().toString();
                        com.cs407.noted.File file = new com.cs407.noted.File(null, null, input_text, null, null, FileType.FOLDER.toString(), null);

                        listAdapter.addNewFile(file, myRef);
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
        });
        action_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter Document Name");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input_text = input.getText().toString();
                        com.cs407.noted.File file = new com.cs407.noted.File(null, null, input_text, null, null, FileType.DOCUMENT.toString(), null);
                        listAdapter.addNewFile(file, myRef);

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

    public String getDatabaseRefPath() {
        return myRef.toString();
    }

    public void removeFile(com.cs407.noted.File file, com.cs407.noted.File parent) {
        // TODO: remove user from fileContents list

        try {
            if (parent.getId().equals("root")) {
                // no child field in database
                myRef.child(file.getId()).removeValue(getDatabaseCompletionListener(file));
            } else {
                myRef.child(file.getId()).removeValue(getDatabaseCompletionListener(file));
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            String err = String.format("Failed to remove %s", file.getTitle());
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        }
    }

//    private com.cs407.noted.File getffffRootFromFile(com.cs407.noted.File file) {
//        while (!file.getId().equals("root")) {
//            file = file
//        }
//    }

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
                    listAdapter.addNewFile(file, myRef);

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
                    listAdapter.addNewFile(file, myRef);

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
}
