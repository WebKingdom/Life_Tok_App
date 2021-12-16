package com.sszabo.life_tok.ui.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateViewModel extends ViewModel {

    private boolean isRecording;

    /**
     * Constructor for the create view model
     */
    public CreateViewModel() {
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }
}
