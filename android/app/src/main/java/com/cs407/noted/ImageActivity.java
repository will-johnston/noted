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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    private ProgressBar spinner;
    private Button translateButton;
    private Bitmap bitmap;
    private RequestQueue queue;
    private String bitmapURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        translateButton = (Button)findViewById(R.id.translateButton);

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
                bitmapURL = uri.toString();
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
                        spinner.setVisibility(View.GONE);
                        translateButton.setVisibility(View.VISIBLE);
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
        Toast.makeText(getApplicationContext(), "Image is being translated", Toast.LENGTH_LONG).show();

        try {
            String URL = "https://westcentralus.api.cognitive.microsoft.com/vision/v2.0/recognizeText?mode=Handwritten";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Log.d("hi", response);
                    String status = response.split("STATUS:<")[1].split(">:")[0];
                    if(status.equals("202")) {
                        String opLocUrl = response.split("HEADERS:<")[1].split("Operation-Location=")[1].split(",")[0];
                        Log.d("hi", opLocUrl);
                        startBackgroundRequests(opLocUrl);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("hi", error.toString());
                    String t = new String(error.networkResponse.data);
                    Log.d("hi", t);
                    Toast.makeText(getApplicationContext(), "Error occurred while translating image", Toast.LENGTH_LONG).show();
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
                        jsonBody.put("url", bitmapURL);
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
                        str = "Response:\nSTATUS:<" + Integer.toString(response.statusCode) + ">:"
                                + "\nDATA:<" + new String(response.data) + ">:"
                                + "\nHEADERS:<" + response.headers.toString();
                    } catch(Exception e) {}

                    return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            queue.add(stringRequest);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error occurred while translating image", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void startBackgroundRequests(final String URL) {
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        final Handler requestHandler = new Handler(handlerThread.getLooper());
        final int delay = 3000;

        final Handler responseHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("hi", "done");
            }
        };

        final Runnable thread = new Runnable() {
            Runnable currThread = this;
            @Override
            public void run() {
                try  {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Log.d("hi", response);
                            String data = response.split("DATA:<")[1].split(">")[0];

                            try {
                                JSONObject json = new JSONObject(data);
                                String status = json.getString("status");

                                if(status.equals("Succeeded")) {
                                    JSONObject result = json.getJSONObject("recognitionResult");
                                    JSONArray lines = result.getJSONArray("lines");
                                    List<OCRLine> resultLines = new ArrayList<OCRLine>();

                                    for(int i = 0; i < lines.length(); i++) {
                                        JSONObject line = lines.getJSONObject(i);
                                        String text = line.getString("text");
                                        int y = line.getJSONArray("boundingBox").getInt(1);
                                        resultLines.add(new OCRLine(text, y));
                                    }

                                    Collections.sort(resultLines);
                                    String resultString = "";
                                    for(int i = 0; i < resultLines.size(); i++) {
                                        resultString += resultLines.get(i).text;

                                        if(i != lines.length() - 1)
                                            resultString += "\n";
                                    }

                                    Log.d("hi", resultString);

                                    Toast.makeText(getApplicationContext(), "Image has been translated", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Log.d("hi", "not succeeded");
                                    requestHandler.postDelayed(currThread, delay);
                                }

                            } catch(Exception e) {
                                e.printStackTrace();
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
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String str = "";
                            try {
                                str = "Response:\nSTATUS:<" + Integer.toString(response.statusCode) + ">:"
                                        + "\nDATA:<" + new String(response.data) + ">:"
                                        + "\nHEADERS:<" + response.headers.toString();
                            } catch(Exception e) {}

                            return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    queue.add(stringRequest);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        requestHandler.postDelayed(thread, delay);
    }
}
