package com.cs407.noted;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public class GoogleAuthSingleton {
    private static GoogleAuthSingleton instance;

    public GoogleSignInClient client;

    private GoogleAuthSingleton(){}  //private constructor.

    public static GoogleAuthSingleton getInstance(){
        if (instance == null){ //if there is no instance available... create new one
            instance = new GoogleAuthSingleton();
        }

        return instance;
    }
}
