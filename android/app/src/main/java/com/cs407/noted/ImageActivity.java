package com.cs407.noted;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String id = intent.getStringExtra("id");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(title);

        //fetch image from firebase
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference ref = firebaseStorage.getReference().child("androidImages/" + id);

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                final Uri uriFinal = uri;

                HandlerThread handlerThread = new HandlerThread("HandlerThread");
                handlerThread.start();
                Handler requestHandler = new Handler(handlerThread.getLooper());

                final Handler responseHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        ImageView imageView = (ImageView)findViewById(R.id.image);
                        imageView.setImageBitmap((Bitmap)msg.obj);
                    }
                };

                Runnable thread = new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            //Your code goes here
                            InputStream in = new java.net.URL(uriFinal.toString()).openStream();
                            Bitmap image = BitmapFactory.decodeStream(in);
                            Message msg = new Message();
                            msg.obj = image;
                            responseHandler.sendMessage(msg);

                        }
                        catch (Exception e) {
                        }
                    }
                };

                requestHandler.post(thread);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "Failed to retrieve image", Toast.LENGTH_LONG).show();
            }
        });
    }
}
