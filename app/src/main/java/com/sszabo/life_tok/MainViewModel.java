package com.sszabo.life_tok;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private boolean isLoggingIn;

    public MainViewModel() {
        // TODO change to false if want login
        isLoggingIn = true;
    }

    public boolean getIsLoggingIn() {
        return isLoggingIn;
    }

    public void setIsLoggingIn(boolean loggingIn) {
        isLoggingIn = loggingIn;
    }
}
