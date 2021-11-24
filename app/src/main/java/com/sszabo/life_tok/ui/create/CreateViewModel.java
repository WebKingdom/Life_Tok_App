package com.sszabo.life_tok.ui.create;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    private boolean isRecording;

    public CreateViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public LiveData<String> getText() {
        return mText;
    }
}
