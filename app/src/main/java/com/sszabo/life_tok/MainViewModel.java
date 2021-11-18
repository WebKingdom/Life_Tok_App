package com.sszabo.life_tok;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private FirebaseAuth.AuthStateListener authListener;

    private boolean isLoggingIn;

    public MainViewModel() {
        isLoggingIn = false;
        authListener = null;
    }

    public boolean getIsLoggingIn() {
        return isLoggingIn;
    }

    public void setIsLoggingIn(boolean loggingIn) {
        isLoggingIn = loggingIn;
    }
}
