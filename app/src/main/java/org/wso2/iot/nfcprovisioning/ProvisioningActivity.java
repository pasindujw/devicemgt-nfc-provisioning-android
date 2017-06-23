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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.support.annotation.NonNull;
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
import org.wso2.iot.nfcprovisioning.utils.CommonDialogUtils;
import org.wso2.iot.nfcprovisioning.utils.Preference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.wso2.iot.nfcprovisioning.utils.Constants.ConfigKey;
import org.wso2.iot.nfcprovisioning.utils.Constants;

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
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private long remoteConfigCacheExpiration;
    private final String TSTRING = "string";
    private final String TBOOLEAN = "boolean";
    private boolean isNewRemoteConfigutarionsAdded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provisioning);
        context = ProvisioningActivity.this;
        Activity activity = ProvisioningActivity.this;
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        activityProvisioning = (RelativeLayout) findViewById(R.id.activity_provisioning);
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter != null) {
            nfcAdapter.setNdefPushMessageCallback(this, activity);
        }
        if (Constants.USE_REMOTE_CONFIG) {
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(Constants.DEBUG_MODE_ENABLED)
                    .build();
            firebaseRemoteConfig.setConfigSettings(configSettings);
            if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
                remoteConfigCacheExpiration = 0;
            } else {
                remoteConfigCacheExpiration = Constants.remoteConfigCacheExpiration;
            }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method retrieves the remote configuration from Firebase.
     */
    private void getRemoteConfig() {
        firebaseRemoteConfig.fetch(remoteConfigCacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            firebaseRemoteConfig.activateFetched();
                            setRemoteConfigValues();
                        } else {
                            Toast.makeText(context, getResources().getString(R.string.toast_remote_config_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * This method sets the remote config values to app preferences.
     */
    private void setRemoteConfigValues() {
        isNewRemoteConfigutarionsAdded = false;
        if (Constants.CLOUD_MANAGER != null && !Constants.CLOUD_MANAGER.isEmpty()) {
            applyRemoteConfig(ConfigKey.PACKAGE_NAME, TSTRING);
            applyRemoteConfig(ConfigKey.PACKAGE_DOWNLOAD_LOCATION, TSTRING);
            applyRemoteConfig(ConfigKey.PACKAGE_CHECKSUM, TSTRING);
        } else {
            applyRemoteConfig(ConfigKey.PACKAGE_NAME, TSTRING);
            applyRemoteConfig(ConfigKey.PACKAGE_DOWNLOAD_LOCATION, TSTRING);
            applyRemoteConfig(ConfigKey.PACKAGE_CHECKSUM, TSTRING);
            applyRemoteConfig(ConfigKey.WIFI_SSID, TSTRING);
            applyRemoteConfig(ConfigKey.WIFI_SECURITY_TYPE, TSTRING);
            applyRemoteConfig(ConfigKey.WIFI_PASSWORD, TSTRING);
            applyRemoteConfig(ConfigKey.TIME_ZONE, TSTRING);
            applyRemoteConfig(ConfigKey.LOCALE, TSTRING);
            applyRemoteConfig(ConfigKey.ENCRYPTION, TBOOLEAN);
            applyRemoteConfig(ConfigKey.KIOSK_APP_DOWNLOAD_LOCATION, TSTRING);
        }
        if (isNewRemoteConfigutarionsAdded) {
            Toast.makeText(context, getResources().getString(
                    R.string.toast_remote_config_added),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, getResources().getString(
                    R.string.toast_remote_config_no_changes),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is used to check whether the incoming remote config can be applied and applies
     * the value if possible. Sets a flag if a new config was applied
     */
    private void applyRemoteConfig(String config, String type) {
        switch (type) {
            case TSTRING:
                if (!firebaseRemoteConfig.getString(config).isEmpty() &&
                        !Preference.getDefPrefString(context, config)
                                .equals(firebaseRemoteConfig.getString(config))) {
                    Preference.putDefPrefString(context, config,
                            firebaseRemoteConfig.getString(config));
                    isNewRemoteConfigutarionsAdded = true;
                }
                break;
            case TBOOLEAN:
                if (!firebaseRemoteConfig.getString(config).isEmpty() &&
                        Preference.getDefPrefBoolean(context, config)
                                != (firebaseRemoteConfig.getBoolean(config))) {
                    Preference.putDefPrefBoolean(context, config,
                            firebaseRemoteConfig.getBoolean(config));
                    isNewRemoteConfigutarionsAdded = true;
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.AUTHENTICATOR_IN_USE.equals
                (Constants.OAUTH_AUTHENTICATOR)) {
            //On oauth authentication mode token is always verified
            //when app resumes to maintain a valid token
            verifyToken();
        }
        if (Constants.USE_REMOTE_CONFIG) {
            getRemoteConfig();
        }
    }

    @Override
    protected void onPause() {
        token = null;
        super.onPause();
    }

    @Override
    public void onReceiveTokenResult(Token token, String status, String message) {
        if (org.wso2.iot.nfcprovisioning.proxy.utils.Constants.REQUEST_SUCCESSFUL.equals(status)) {
            this.token = token.getAccessToken();
            dismissSnackbar();
            startRippleAnimation();
        } else if (org.wso2.iot.nfcprovisioning.proxy.utils.Constants.INTERNAL_SERVER_ERROR.equals(status)) {
            this.token = null;
            stopRippleAnimation();
            setSnackbar(getResources().getString(R.string.internal_server_error),
                    getResources().getString(R.string.retry),
                    getResources().getColor(R.color.red));
        } else if (org.wso2.iot.nfcprovisioning.proxy.utils.Constants.SERVER_UNREACHABLE.equals(status)) {
            this.token = null;
            stopRippleAnimation();
            setSnackbar(getResources().getString(R.string.server_unreachable),
                    getResources().getString(R.string.retry),
                    getResources().getColor(R.color.red));
        } else if (org.wso2.iot.nfcprovisioning.proxy.utils.Constants.NO_CONNECTION.equals(status)) {
            this.token = null;
            stopRippleAnimation();
            setSnackbar(getResources().getString(R.string.network_connectivity_unavailable),
                    getResources().getString(R.string.retry),
                    getResources().getColor(R.color.yellow));
        } else {
            Log.w(TAG, "Bad request: " + message);
            Preference.putBoolean(context, Constants.TOKEN_EXPIRED, true);
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
        if (Constants.AUTHENTICATOR_IN_USE.equals
                (Constants.OAUTH_AUTHENTICATOR)) {
            if (token == null) {
                return null;
            }
            props.put(Constants.ANDROID_APP_EXTRA_TOKREN, token);
        }
        if (Constants.PUSH_KIOSK_APP) {
            String appUrl = Preference.getDefPrefString(context, ConfigKey.KIOSK_APP_DOWNLOAD_LOCATION);
            if (!appUrl.equals("")) {
                props.put(Constants.ANDROID_APP_EXTRA_APPURL, appUrl);
            }
        }
        if (!props.isEmpty()) {
            StringWriter sw = new StringWriter();
            try {
                props.store(sw, Constants.ADMIN_EXTRA_BUNDLE);
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

    /**
     * This method retrieves the provisioning values.
     * Any new provisioning values can be directly added here.
     */
    private Map<String, String> getProvisioningValues() {
        Map<String, String> provValuesMap = new HashMap<>();
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                Preference.getDefPrefString(context, ConfigKey.PACKAGE_NAME));
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION,
                Preference.getDefPrefString(context, ConfigKey.PACKAGE_DOWNLOAD_LOCATION));
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM,
                Preference.getDefPrefString(context, ConfigKey.PACKAGE_CHECKSUM));
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_SSID,
                Preference.getDefPrefString(context, ConfigKey.WIFI_SSID));
        String wifiSecurityType = Preference.getDefPrefString(context, ConfigKey.WIFI_SECURITY_TYPE);
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_SECURITY_TYPE, wifiSecurityType);
        if (!wifiSecurityType.equals(Constants.WIFI_SECURITY_TYPE_NONE)) {
            provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_WIFI_PASSWORD,
                    Preference.getDefPrefString(context, ConfigKey.WIFI_PASSWORD));
        }
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_TIME_ZONE,
                Preference.getDefPrefString(context, ConfigKey.TIME_ZONE));
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_LOCALE,
                Preference.getDefPrefString(context, ConfigKey.LOCALE));
        provValuesMap.put(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                (Preference.getDefPrefBoolean(context, ConfigKey.ENCRYPTION) ? "false" : "true"));
        return provValuesMap;
    }

    /**
     * This method is used to verify the validity of the access token.
     */
    private void verifyToken() {
        String clientKey = Preference.getString(context, Constants.CLIENT_ID);
        String clientSecret = Preference.getString(context, Constants.CLIENT_SECRET);
        if (IdentityProxy.getInstance().getContext() == null) {
            IdentityProxy.getInstance().setContext(context);
        }
        IdentityProxy.getInstance().setRequestCode(Constants.TOKEN_VERIFICATION_REQUEST_CODE);
        IdentityProxy.getInstance().requestToken(IdentityProxy.getInstance().getContext(), this,
                clientKey, clientSecret);
    }

    /**
     * This method is used to log out from  the app.
     */
    private void logOut() {
        final AlertDialog.Builder builder = CommonDialogUtils.getAlertDialogWithTwoButtonAndTitle(context,
                getResources().getString(R.string.dialog_title_log_out),
                getResources().getString(R.string.dialog_message_log_out),
                getResources().getString(R.string.button_no),
                getResources().getString(R.string.button_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Preference.putBoolean(context, Constants.TOKEN_EXPIRED, true);
                        Intent intent = new Intent(ProvisioningActivity.this, AuthenticationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.show();
    }

    /**
     * This method is used to set and show the snackbar with action to verifyToken()
     */
    private void setSnackbar(String message, String actionTitle, int actionColor) {
        snackbar = Snackbar
                .make(activityProvisioning, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(actionTitle, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        verifyToken();
                    }
                });
        snackbar.setActionTextColor(actionColor);
        snackbar.show();
    }

    /**
     * This method is used to dismiss the snackbar.
     */
    private void dismissSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    /**
     * This method is used to start ripple animation in the UI if it is inactive.
     */
    private void startRippleAnimation() {
        if (rippleBackground != null && !rippleBackground.isRippleAnimationRunning()) {
            rippleBackground.startRippleAnimation();
        }
    }

    /**
     * This method is used to stop ripple animation in the UI if it is active.
     */
    private void stopRippleAnimation() {
        if (rippleBackground != null && rippleBackground.isRippleAnimationRunning()) {
            rippleBackground.stopRippleAnimation();
        }
    }
}