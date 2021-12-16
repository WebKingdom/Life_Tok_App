package com.sszabo.life_tok.ui.settings;

import androidx.lifecycle.ViewModel;

/**
 * View Model class for the Settings Fragment.
 */
public class SettingsViewModel extends ViewModel {

    private boolean profPicUpdated;

    /**
     * Constructor
     */
    public SettingsViewModel() {
        profPicUpdated = false;
    }

    public boolean isProfPicUpdated() {
        return profPicUpdated;
    }

    public void setProfPicUpdated(boolean profPicUpdated) {
        this.profPicUpdated = profPicUpdated;
    }
}
