package com.cs407.noted;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.getbase.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference myRef;  // this will store the database reference at the current path
    private File root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        // recycler view setup
        recyclerView = findViewById(R.id.recycler_view);
        root = new File("root", "", "noted", null, null, FileType.FOLDER.toString(), null);
        root.setId("root");
        // root.setHasListener(true);  // we have a child event listener for the first level of file system
        listAdapter = new ListAdapter(null, root);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // fab setup
        setUpFloatingActionMenu();
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
                        File file = new File(null, null, input_text, null, null, FileType.FOLDER.toString(), null);

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
                        File file = new File(null, null, input_text, null, null, FileType.DOCUMENT.toString(), null);
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

        firebaseAuth = FirebaseAuth.getInstance();
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
        }
        else {
            String displayName = currentUser.getDisplayName();

            Snackbar.make(findViewById(R.id.main_layout), "Logged in as " + displayName, Snackbar.LENGTH_SHORT).show();

            // database setup
            database = FirebaseDatabase.getInstance();
            String path = String.format("users/%s", currentUser.getUid());
            this.myRef = database.getReference(path);
            final Context context = this;

            DatabaseReference filesRef = database.getReference(path);
            filesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null) {
                        return;
                    }
                    List<File> files = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        File file = ds.getValue(File.class);
                        // locally set the parents
                        file = setFileParents(file, root);
                        files.add(file);
                    }
                    // add files as root's children
                    for (File file: files) {
                        root.addChild(file);
                    }

                    listAdapter.setItemListMaintainCurrentDirectory(files);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private File setFileParents(File file, File parent) {
        if (file == null) { return null; }
        // set parent
        file.setParent(parent);
        Map<String, File> children = file.getChildren();
        // update children's parents
        if (children != null) {
            Map<String, File> newKids = new HashMap<>();
            Set keys = children.keySet();
            for (Object key : keys) {
                File child = children.get(key);
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




    private void onSignOut() {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        GoogleAuthSingleton.getInstance().client.signOut();

        //show login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
