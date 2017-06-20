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

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.wso2.iot.nfcprovisioning.utils.AppCompatPreferenceActivity;
import org.wso2.iot.nfcprovisioning.uielements.EditTextPreference;
import org.wso2.iot.nfcprovisioning.utils.Constants;

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
        wifiPasswordPref = (EditTextPreference) findPreference(Constants.ConfigKey.WIFI_PASSWORD);
        preferenceScreen = getPreferenceScreen();
        wifiSecurityTypePref = (ListPreference) findPreference(Constants.ConfigKey.WIFI_SECURITY_TYPE);
        addRemoveWiFiPasswordPref(wifiSecurityTypePref.getValue());
        wifiSecurityTypePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                                                       public boolean onPreferenceChange(Preference preference, Object newValue) {
                                                                           addRemoveWiFiPasswordPref(newValue.toString());
                                                                           return true;
                                                                       }
                                                                   });
        if (!Constants.PUSH_KIOSK_APP) {
            EditTextPreference kioskAppUrlPref = (EditTextPreference) findPreference(
                    Constants.ConfigKey.KIOSK_APP_DOWNLOAD_LOCATION);
            preferenceScreen.removePreference(kioskAppUrlPref);
        }

        if (Constants.CLOUD_ENABLED){
            preferenceScreen.removePreference(findPreference(Constants.ConfigKey.PACKAGE_NAME));
            preferenceScreen.removePreference(findPreference(Constants.ConfigKey.PACKAGE_DOWNLOAD_LOCATION));
            preferenceScreen.removePreference(findPreference(Constants.ConfigKey.PACKAGE_CHECKSUM));
        }
    }

    /**
     * This method is used to add or remove the wifi password pref
     * depending on the wifi security type.
     */
    private void addRemoveWiFiPasswordPref(String val) {
        if (val.equals(Constants.WIFI_SECURITY_TYPE_NONE)) {
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
        switch (item.getItemId()) {
            case R.id.action_advanced_settings:
                Intent intent = new Intent(SettingsActivity.this, AdvancedSettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                finish();
                return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu_settings, menu);
        if (Constants.CLOUD_ENABLED){
            MenuItem itemS = menu.findItem(R.id.action_advanced_settings);
            itemS.setVisible(true);
        }
        return true;
    }
}