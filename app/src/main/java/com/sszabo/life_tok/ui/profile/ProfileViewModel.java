package com.sszabo.life_tok.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.Hashtable;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private Hashtable<Integer, File> hashtable;

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profile fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
