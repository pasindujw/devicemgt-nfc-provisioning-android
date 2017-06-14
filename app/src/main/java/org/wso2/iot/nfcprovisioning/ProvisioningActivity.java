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

import android.annotation.NonNull;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.skyfishjy.library.RippleBackground;
import org.wso2.iot.nfcprovisioning.proxy.IdentityProxy;
import org.wso2.iot.nfcprovisioning.proxy.beans.Token;
import org.wso2.iot.nfcprovisioning.proxy.interfaces.TokenCallBack;
import org.wso2.iot.nfcprovisioning.proxy.utils.Constants;
import org.wso2.iot.nfcprovisioning.utils.CommonDialogUtils;
import org.wso2.iot.nfcprovisioning.utils.Preference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Activity that handles the nfc provisioning action
 */
public class ProvisioningActivity extends AppCompatActivity implements TokenCallBack,
        NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = ProvisioningActivity.class.getSimpleName();
    private Context context;
    private String token;
    private RelativeLayout activityProvisioning;
    private Snackbar snackbar;
    private RippleBackground rippleBackground;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioning);
        context = ProvisioningActivity.this;
        Activity activity = ProvisioningActivity.this;

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        activityProvisioning =  (RelativeLayout)findViewById(R.id.activity_provisioning);
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter != null) {
            nfcAdapter.setNdefPushMessageCallback(this, activity);
        }

        if (org.wso2.iot.nfcprovisioning.utils.Constants.USE_REMOTE_CONFIG){
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder( )
                    .setDeveloperModeEnabled (true)
                    .build();
            mFirebaseRemoteConfig.setConfigSettings (configSettings);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(ProvisioningActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                logOut();
                return true;

            case R.id.action_remote_config:
                getRemoteConfig();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getRemoteConfig(){
        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, getResources().getString(
                                    R.string.toast_remote_config_added),
                                    Toast.LENGTH_SHORT).show();
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Toast.makeText(context, getResources().getString(
                                    R.string.toast_remote_config_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                        setRemoteConfigValues();
                    }
                });
    }

    private void setRemoteConfigValues() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getResources().getString(R.string.pref_key_package_name),
                mFirebaseRemoteConfig.getString("prov_package_name"));
        editor.putString(getResources().getString(R.string.pref_key_package_download_location),
                mFirebaseRemoteConfig.getString("prov_package_download_location"));
        editor.putString(getResources().getString(R.string.pref_key_package_checksum),
                mFirebaseRemoteConfig.getString("prov_package_checksum"));
        editor.putString(getResources().getString(R.string.pref_key_wifi_ssid),
                mFirebaseRemoteConfig.getString("prov_package_wifi_ssid"));
        editor.putString(getResources().getString(R.string.pref_key_wifi_security_type),
                mFirebaseRemoteConfig.getString("prov_wifi_security_type"));
        editor.putString(getResources().getString(R.string.pref_key_wifi_password),
                mFirebaseRemoteConfig.getString("prov_wifi_password"));
        editor.putString(getResources().getString(R.string.pref_key_time_zone),
                mFirebaseRemoteConfig.getString("prov_time_zone"));
        editor.putString(getResources().getString(R.string.pref_key_locale),
                mFirebaseRemoteConfig.getString("prov_locale"));
        editor.putBoolean(getResources().getString(R.string.pref_key_encryption),
                mFirebaseRemoteConfig.getBoolean("prov_encryption"));
        editor.putString(getResources().getString(R.string.pref_key_kiosk_app_download_location),
                mFirebaseRemoteConfig.getString("prov_kiosk_app_download_location"));
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu, menu);
        if (org.wso2.iot.nfcprovisioning.utils.Constants.USE_REMOTE_CONFIG){
            //MenuItem itemC = menu.findItem(R.id.action_settings);
            //itemC.setVisible(false);
            MenuItem itemS = menu.findItem(R.id.action_remote_config);
            itemS.setVisible(true);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (org.wso2.iot.nfcprovisioning.utils.Constants.AUTHENTICATOR_IN_USE.equals
                (org.wso2.iot.nfcprovisioning.utils.Constants.OAUTH_AUTHENTICATOR)) {

            //On oauth authentication mode token is always verified
            //when app resumes to maintain a valid token
            verifyToken();
        }
    }

    @Override
    protected void onPause() {
        token = null;
        super.onPause();
    }

    @Override
    public void onReceiveTokenResult(Token token, String status, String message) {
        if (Constants.REQUEST_SUCCESSFUL.equals(status)) {
            this.token = token.getAccessToken();

            if (snackbar != null && snackbar.isShown()){
                snackbar.dismiss();
            }

            if(rippleBackground!=null && !rippleBackground.isRippleAnimationRunning()){
                rippleBackground.startRippleAnimation();
            }

        } else if (Constants.INTERNAL_SERVER_ERROR.equals(status)){

            this.token = null;

            if(rippleBackground!=null && rippleBackground.isRippleAnimationRunning()){
                rippleBackground.stopRippleAnimation();
            }

            snackbar = Snackbar
                    .make(activityProvisioning, "Internal server error", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           verifyToken();
                        }
                    });
            snackbar.setActionTextColor(getResources().getColor(R.color.red));

            snackbar.show();

        } else if (Constants.SERVER_UNREACHABLE.equals(status)){

            this.token = null;

            if(rippleBackground!=null && rippleBackground.isRippleAnimationRunning()){
                rippleBackground.stopRippleAnimation();
            }

            snackbar = Snackbar
                    .make(activityProvisioning, "Server unreachable", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            verifyToken();
                        }
                    });
            snackbar.setActionTextColor(getResources().getColor(R.color.red));

            snackbar.show();

        } else if (Constants.NO_CONNECTION.equals(status)){

            this.token = null;

            if(rippleBackground!=null && rippleBackground.isRippleAnimationRunning()){
                rippleBackground.stopRippleAnimation();
            }

            snackbar = Snackbar
                    .make(activityProvisioning, "Network connectivity unavailable", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            verifyToken();
                        }
                    });
            snackbar.setActionTextColor(getResources().getColor(R.color.yellow));

            snackbar.show();

        } else /*if (Constants.ACCESS_FAILURE.equals(status)) */{
            Log.w(TAG, "Bad request: " + message);
            Preference.putBoolean(context, org.wso2.iot.nfcprovisioning.utils.Constants.TOKEN_EXPIRED, true);
            Toast.makeText(context, context.getResources().getString(R.string.msg_need_to_sign_in),
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProvisioningActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Properties properties = new Properties();
        Map<String, String> provisioningValues = getProvisioningValues();
        Properties props = new Properties();

        //Adds the access token to the provisioning values if authentication mode is oauth
        if (org.wso2.iot.nfcprovisioning.utils.Constants.AUTHENTICATOR_IN_USE.equals
                (org.wso2.iot.nfcprovisioning.utils.Constants.OAUTH_AUTHENTICATOR)) {
            if (token == null) {
                return null;
            }
            props.put("android.app.extra.token", token);
        }

        if (org.wso2.iot.nfcprovisioning.utils.Constants.PUSH_KIOSK_APP) {
            String appUrl = sharedPref.getString(getResources().getString(R.string.pref_key_kiosk_app_download_location), "");
            if (!appUrl.equals("")) {
                props.put("android.app.extra.appurl", appUrl);
            }
        }

        if (!props.isEmpty()) {
            StringWriter sw = new StringWriter();
            try {
                props.store(sw, "admin extras bundle");
                provisioningValues.put(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE,
                        sw.toString());
                Log.d(TAG, "Admin extras bundle=" + provisioningValues.get(
                        DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE));
            } catch (IOException e) {
                Log.e(TAG, "Unable to build admin extras bundle");
            }
        }

        for (Map.Entry<String, String> e : provisioningValues.entrySet()) {
            if (!TextUtils.isEmpty(e.getValue())) {
                String value;
                if (e.getKey().equals(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_SSID)) {
                    // Make sure to surround SSID with double quotes
                    value = e.getValue();
                    if (!value.startsWith("\"") || !value.endsWith("\"")) {
                        value = "\"" + value + "\"";
                    }
                } else {
                    value = e.getValue();
                }
                properties.put(e.getKey(), value);
            }
        }
        // Make sure to put local time in the properties. This is necessary on some devices to
        // reliably download the device owner APK from an HTTPS connection.
        if (!properties.contains(DevicePolicyManager.EXTRA_PROVISIONING_LOCAL_TIME)) {
            properties.put(DevicePolicyManager.EXTRA_PROVISIONING_LOCAL_TIME,
                    String.valueOf(System.currentTimeMillis()));
        }
        try {
            properties.store(stream, getString(R.string.nfc_comment));
            NdefRecord record = NdefRecord.createMime(
                    DevicePolicyManager.MIME_TYPE_PROVISIONING_NFC, stream.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Any new provisioning values can be directly added here.
    private Map<String, String> getProvisioningValues() {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                sharedPref.getString(getResources().getString(R.string.pref_key_package_name), ""));
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION,
                sharedPref.getString(getResources().getString(R.string.pref_key_package_download_location), ""));
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM,
                sharedPref.getString(getResources().getString(R.string.pref_key_package_checksum), ""));
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_SSID,
                sharedPref.getString(getResources().getString(R.string.pref_key_wifi_ssid), ""));
        String wifiSecurityType = sharedPref.getString(getResources().getString(R.string.pref_key_wifi_security_type), "");
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_SECURITY_TYPE, wifiSecurityType);
        if (!wifiSecurityType.equals("NONE")) {
            valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_PASSWORD,
                    sharedPref.getString(getResources().getString(R.string.pref_key_wifi_password), ""));
        }
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_TIME_ZONE, sharedPref.getString(getResources().getString(R.string.pref_key_time_zone), ""));
        valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_LOCALE, sharedPref.getString(getResources().getString(R.string.pref_key_locale), ""));
        if (Build.VERSION.SDK_INT >= 23) {
            valuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                    (sharedPref.getBoolean(getResources().getString(R.string.pref_key_encryption), false) ? "false" : "true"));
        }
        return valuesMap;
    }

    private void verifyToken() {
        String clientKey = Preference.getString(context, Constants.CLIENT_ID);
        String clientSecret = Preference.getString(context, Constants.CLIENT_SECRET);
        if (IdentityProxy.getInstance().getContext() == null) {
            IdentityProxy.getInstance().setContext(context);
        }
        IdentityProxy.getInstance().setRequestCode
                (org.wso2.iot.nfcprovisioning.utils.Constants.TOKEN_VERIFICATION_REQUEST_CODE);
        IdentityProxy.getInstance().requestToken(IdentityProxy.getInstance().getContext(), this,
                clientKey,
                clientSecret);
    }

    private void logOut() {
        final AlertDialog.Builder builder = CommonDialogUtils.getAlertDialogWithTwoButtonAndTitle(context,
                getResources().getString(
                        R.string.dialog_title_log_out),
                getResources().getString(
                        R.string.dialog_message_log_out),
                getResources().getString(
                        R.string.button_no),
                getResources().getString(
                        R.string.button_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Preference.putBoolean(context, org.wso2.iot.nfcprovisioning.utils.Constants.TOKEN_EXPIRED, true);
                        Intent intent = new Intent(ProvisioningActivity.this, AuthenticationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.show();
    }
}