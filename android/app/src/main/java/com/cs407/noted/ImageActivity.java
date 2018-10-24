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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private RequestQueue queue;

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

        queue = Volley.newRequestQueue(this);

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
                        bitmap = (Bitmap)msg.obj;
                        imageView.setImageBitmap(bitmap);
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

    public void onTranslateButtonClicked(View v) {
        try {
            String URL = "https://westcentralus.api.cognitive.microsoft.com/vision/v2.0/recognizeText?mode=Handwritten";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Log.d("hi", response);
                    String status = response.split("STATUS:")[1].split(":")[0];
                    if(status.equals("202")) {
                        String opLocUrl = response.split("HEADERS:")[1].split("Operation-Location=")[1].split(",")[0];
                        Log.d("hi", opLocUrl);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("hi", error.toString());
                    String t = new String(error.networkResponse.data);
                    Log.d("hi", t);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> headers = new HashMap<String, String>();
                    headers.put("Ocp-Apim-Subscription-Key", "2d57d7caaa694a8da9f438a2fb469892");
                    return headers;
                }
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        JSONObject jsonBody = new JSONObject();
                        jsonBody.put("url", "https://i.imgur.com/tcGJ4df.jpg");
                        return jsonBody.toString().getBytes("utf-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String str = "";
                    try {
                        str = "Response:\nSTATUS:" + Integer.toString(response.statusCode) + ":"
                                + "\nDATA:" + new String(response.data) + ":"
                                + "\nHEADERS:" + response.headers.toString();
                    } catch(Exception e) {}

                    return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
