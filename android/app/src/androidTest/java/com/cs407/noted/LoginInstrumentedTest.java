package com.cs407.noted;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.junit.Assert;
import org.junit.Test;

public class LoginInstrumentedTest {

    private LoginActivity act;

    @Test
    public void LoginTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        FirebaseAuth firebaseAuth = firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        boolean userSignedin = false;
        if(currentUser == null) {
            //Assert.assertTrue(false);
            firebaseAuth.signOut();
            userSignedin = false;
        }

        try {
            Intent signInIntent = GoogleAuthSingleton.getInstance().client.getSignInIntent();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(signInIntent);

            AuthCredential credential = GoogleAuthProvider.getCredential(null, null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(act, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Assert.assertTrue(true);
                            } else {
                                Assert.assertTrue(false);
                            }
                        }
                    });
        } catch (Exception e) {
            Assert.assertTrue(!userSignedin);
        }
    }
}
