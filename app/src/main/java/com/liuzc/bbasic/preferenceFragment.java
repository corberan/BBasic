package com.liuzc.bbasic;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class preferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            addPreferencesFromResource(R.xml.preference);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
