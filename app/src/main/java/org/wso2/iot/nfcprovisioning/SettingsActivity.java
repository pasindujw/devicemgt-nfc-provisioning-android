/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.iot.nfcprovisioning;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import org.wso2.iot.nfcprovisioning.utils.AppCompatPreferenceActivity;
import org.wso2.iot.nfcprovisioning.uielements.EditTextPreference;

/**
 * Activity that handles the initial configurations of the provisioning values
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    ListPreference wifiSecurityTypePref;
    EditTextPreference wifiPasswordPref;
    PreferenceScreen preferenceScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setupActionBar();
        wifiPasswordPref = (EditTextPreference) findPreference(getResources().getString(R.string.pref_key_wifi_password));
        preferenceScreen = getPreferenceScreen();
        wifiSecurityTypePref = (ListPreference) findPreference(getResources().getString(R.string.pref_key_wifi_security_type));
        addRemoveWiFiPasswordPref(wifiSecurityTypePref.getValue());
        wifiSecurityTypePref.setOnPreferenceChangeListener(new
                                                                   Preference.OnPreferenceChangeListener() {
                                                                       public boolean onPreferenceChange(Preference preference, Object newValue) {
                                                                           addRemoveWiFiPasswordPref(newValue.toString());
                                                                           return true;
                                                                       }
                                                                   });
    }

    private void addRemoveWiFiPasswordPref(String val) {
        if (val.equals("NONE")) {
            preferenceScreen.removePreference(wifiPasswordPref);
        } else {
            preferenceScreen.addPreference(wifiPasswordPref);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}