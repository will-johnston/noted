package com.cs407.noted;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.github.mthli.knife.KnifeText;

public class DocumentActivity extends AppCompatActivity {
    // document properties
    private KnifeText knife;
    private String title;
    private String id;
    private DatabaseReference ref;
    private String imageId;

    // cursor positions
    private int startPosition;  //the start position of the cursor
    private int endPosition;  // the end position of the cursor,
                              // could be different than start if text is highlighted

    // local vs. database text
    private String currentHtml;  // local text
    private String changedHtml;  // saved text in database

    // 'saving' variables
    private static long delay = 1000; // delay to save after user stops typing
    private long last_edit_time = 0;  // timestamp when user last edited text
    Handler handler;  // used for runnable object that determines time to save


    @Override

    //TODO: create on resume and on restart methods
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        // set document information
        Intent intent = getIntent();
        this.title = intent.getStringExtra("title");
        this.id = intent.getStringExtra("id");
        currentHtml = null;
        handler = new Handler();

        verifyUser();  // make sure user is still logged in
        setupKnife();  // initialize knife and its properties
        setupDatabase();  // sets database properties
        setupActionBar();
    }



    /*
        This listens for when there is a change in the database and updates the local text accordingly
        If there data exists, calls updateFromRemote changes to sync with the local knife text
     */
    private ValueEventListener getListener() {;
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FileContents fc = dataSnapshot.getValue(FileContents.class);

                if (currentHtml  == null) {
                    // local text has not been initialized, so initialize it
                    currentHtml = knife.toHtml();
                }
                if (fc == null) {
                    // the file is no longer available
                    //go back to main activity
                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    myIntent.putExtra("flag", "deleted");
                    startActivityForResult(myIntent, 0);
                    return;
                }
                if (fc.getData() == null) {
                    // no data in database yet
                    return;
                }
                updateFromRemoteChanges(fc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    /*
    Checks to see if there are remote changes that need to be added. Updates cursor position if this
    is the case
     */
    private void updateFromRemoteChanges(FileContents fc) {
        // get the cursors currently position (for local text)
        this.startPosition = knife.getSelectionStart();
        this.endPosition = knife.getSelectionEnd();
        Log.e("START/END SET", String.format("%d, %d", this.startPosition, this.endPosition));


        changedHtml = fc.getData();
        if (!currentHtml.equals(changedHtml)) {
            // if the local version is different, update it to be the database version
            Log.e("TEXT CHANGE START/END", String.format("%d, %d", startPosition, endPosition));
            String current = String.valueOf(currentHtml);  // have this here, because next line triggers a change in currentHtml
            knife.fromHtml(changedHtml, knife);
            // move cursor to its appropriate posotion
            updateCursor(startPosition, endPosition, current, changedHtml);
            currentHtml = changedHtml;
        }
    }

    private void setStartEnd(int start, int end) {
        this.startPosition = start;
        this.endPosition = end;
        Log.e("START/END SET", String.format("%d, %d", start, end));
    }


    /*
     Put the cursor in the correct position based on insertions and deletions remotely
     */
    private void updateCursor(int start, int end, String current, String changed) {
        Log.e("START/END BEFORE BEFORE", String.format("%d, %d", start, end));
        // strip strings of html tags and get length
        int currentLen = current.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").length();
        int changedLen = changed.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").length();
        int diff = Math.abs(currentLen - changedLen);
        int[] updatedStartEnd = updateStartEnd(start, end, currentLen, changedLen, diff, current, changed);
        start = updatedStartEnd[0];
        end = updatedStartEnd[1];
        if (start == -1 || end == -1) {
            // means we got an invalid cursor position...eagerly stopping early
            return;
        }
        try {
            this.knife.setSelection(start, end);
        } catch (IndexOutOfBoundsException e) {
            // can occur if the user is typing absurdly quickly
            // happens when the cursor position is negative or greater than the changed string length
            e.printStackTrace();
        }
    }

    /*
    Finds the positions the cursor start and end needs to move to
     */
    public int[] updateStartEnd(int start, int end, int currentLen, int changedLen, int diff, String current, String changed) {
        Log.e("START/END BEFORE", String.format("%d, %d", start, end));
        if (start > end || start < 0) {
            return new int[]{-1, -1};
        }

        if (changedLen > currentLen) {
            // text was added
            if (!changed.substring(0, end).equals(current.substring(0, end))) {
                // text was added before the cursor
                start += diff;
                end += diff;
            }
        } else {
            // text was deleted
            if (end > changedLen || (end <= changedLen &&
                    !current.substring(0, end).equals(changed.substring(0, end)))) {
                //if (!current.substring(0, changedLen).equals(changed)) {
                // text was removed before the cursor
                start -= diff;
                end -= diff;
            }
        }
        Log.e("START/END AFTER", String.format("%d, %d", start, end));
        return new int[]{start, end};
    }

    /*
     Looks for local changes to the text, updates the text on the database after change stops
      */
    private TextWatcher getTextWatcher() {

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(input_finish_checker); // remove this to run only once
            }

            @Override
            public void afterTextChanged(Editable s) {
                last_edit_time = System.currentTimeMillis();
                // run input_finish_checker after the delay
                handler.postDelayed(input_finish_checker, delay);
            }
        };
    }

    /*
    Checks if it's time to save. If it is time, it adds the changes to the database
     */
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            // if its been at least a half second since the last edit time...
            // should be the case since we do a postDelayed for a second
            if (System.currentTimeMillis() > (last_edit_time + delay - 500)) {
                addChangeToDatabase();
                Log.e("DOC","adding changes");
            }
        }
    };

    public void addChangeToDatabase() {
        currentHtml = knife.toHtml();
        if (!currentHtml.equals(changedHtml)) {
            ref.child("data").setValue(currentHtml);
        }
    }





    /*
    Handles undo/redo and exit selections
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //go back to main activity
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                myIntent.putExtra("flag", "normal");
                startActivityForResult(myIntent, 0);
                break;
            case R.id.undo:
                knife.undo();
                break;
            case R.id.redo:
                knife.redo();
                break;
            case R.id.showOriginalImage:
                Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("id", imageId);
                getApplicationContext().startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }

    public String getCurrentHtml() {
        return currentHtml;
    }


    /* SETUP FUNCTIONS*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*check if the node contains a "image" property
        if so, then this document was generated with an image
            the image id is the value of the image property
            show the view original image button in the toolbar
        if not, then this document was not generated with an image
            so no need to do anything*/
        final Menu menuFinal = menu;
        DatabaseReference imageRef = ref.child("image");
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    imageId = (String)dataSnapshot.getValue();
                    Log.d("hi", imageId);
                    MenuItem item = menuFinal.findItem(R.id.showOriginalImage);
                    item.setVisible(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        imageRef.addListenerForSingleValueEvent(eventListener);

        getMenuInflater().inflate(R.menu.menu_document, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void verifyUser() {
        // verify user is logged in still
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            //show login activity
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    private void setupKnife() {
        knife = findViewById(R.id.knife);
        knife.addTextChangedListener(getTextWatcher());
        knife.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //knife.setImeOptions();
        // knife.setPaintFlags(0);
        setupBold();
        setupItalic();
        setupUnderline();
        setupStrikethrough();
        setupBullet();
        setupQuote();
        // setupLink();
        // setupClear();
    }

    private void setupDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String path = String.format("fileContents/%s", this.id);
        this.ref = database.getReference(path);
        this.ref.addValueEventListener(getListener());

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);
    }

    private void setupBold() {
        ImageButton bold = (ImageButton) findViewById(R.id.bold);

        bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.bold(!knife.contains(KnifeText.FORMAT_BOLD));
            }
        });

        bold.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_bold, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupItalic() {
        ImageButton italic = (ImageButton) findViewById(R.id.italic);

        italic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.italic(!knife.contains(KnifeText.FORMAT_ITALIC));
            }
        });

        italic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_italic, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupUnderline() {
        ImageButton underline = (ImageButton) findViewById(R.id.underline);

        underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.underline(!knife.contains(KnifeText.FORMAT_UNDERLINED));
            }
        });

        underline.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_underline, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupStrikethrough() {
        ImageButton strikethrough = (ImageButton) findViewById(R.id.strikethrough);

        strikethrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.strikethrough(!knife.contains(KnifeText.FORMAT_STRIKETHROUGH));
            }
        });

        strikethrough.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_strikethrough, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }



    private void setupBullet() {
        ImageButton bullet = (ImageButton) findViewById(R.id.bullet);

        bullet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.bullet(!knife.contains(KnifeText.FORMAT_BULLET));
            }
        });


        bullet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_bullet, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupQuote() {
        ImageButton quote = (ImageButton) findViewById(R.id.quote);

        quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.quote(!knife.contains(KnifeText.FORMAT_QUOTE));
            }
        });

        quote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_quote, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupLink() {
        ImageButton link = (ImageButton) findViewById(R.id.link);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinkDialog();
            }
        });

        link.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_insert_link, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setupClear() {
        ImageButton clear = (ImageButton) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                knife.clearFormats();
            }
        });

        clear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DocumentActivity.this, R.string.toast_format_clear, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void showLinkDialog() {
        final int start = knife.getSelectionStart();
        final int end = knife.getSelectionEnd();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.dialog_link, null, false);
        final EditText editText = (EditText) view.findViewById(R.id.edit);
        builder.setView(view);
        builder.setTitle(R.string.dialog_title);

        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String link = editText.getText().toString().trim();
                if (TextUtils.isEmpty(link)) {
                    return;
                }

                // When KnifeText lose focus, use this method
                knife.link(link, start, end);
            }
        });

        builder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DO NOTHING HERE
            }
        });

        builder.create().show();
    }
}