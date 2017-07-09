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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.iot.nfcprovisioning.api.TenantResolverCallback;
import org.wso2.iot.nfcprovisioning.api.TenantResolverHandler;
import org.wso2.iot.nfcprovisioning.beans.Tenant;
import org.wso2.iot.nfcprovisioning.proxy.IdentityProxy;
import org.wso2.iot.nfcprovisioning.proxy.beans.CredentialInfo;
import org.wso2.iot.nfcprovisioning.proxy.interfaces.APIAccessCallBack;
import org.wso2.iot.nfcprovisioning.proxy.interfaces.APIResultCallBack;
import org.wso2.iot.nfcprovisioning.beans.ApiRegistrationProfile;
import org.wso2.iot.nfcprovisioning.utils.CommonDialogUtils;
import org.wso2.iot.nfcprovisioning.utils.CommonUtils;
import org.wso2.iot.nfcprovisioning.utils.Constants;
import org.wso2.iot.nfcprovisioning.api.DeviceInfo;
import org.wso2.iot.nfcprovisioning.utils.AndroidAgentException;
import org.wso2.iot.nfcprovisioning.utils.DynamicClientManager;
import org.wso2.iot.nfcprovisioning.utils.Preference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity that captures serverip, username, password and device ownership details
 * and handles authentication.
 */
public class AuthenticationActivity extends AppCompatActivity implements APIAccessCallBack,
        APIResultCallBack {

    private Button btnSignIn;
    private EditText etUsername;
    private EditText etDomain;
    private EditText etPassword;
    private EditText etServerIP;
    private Context context;
    private String username;
    private String usernameVal;
    private String passwordVal;
    private ProgressDialog progressDialog;
    private boolean isReLogin = false;
    private DeviceInfo deviceInfo;
    private static final String TAG = AuthenticationActivity.class.getSimpleName();
    private static final String[] SUBSCRIBED_API = new String[]{"android"};
    private Tenant currentTenant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_authentication);

        deviceInfo = new DeviceInfo(context);
        etServerIP = (EditText) findViewById(R.id.serverIP);
        etDomain = (EditText) findViewById(R.id.organization);
        etDomain.setText("carbon.super");
        etUsername = (EditText) findViewById(R.id.username);
        etPassword = (EditText) findViewById(R.id.password);
        btnSignIn = (Button) findViewById(R.id.username_sign_in_button);
        btnSignIn.setOnClickListener(onClickAuthenticate);

        if (Constants.DEFAULT_HOST != null && !Constants.DEFAULT_HOST.equals("")) {
            etServerIP.setText(Constants.DEFAULT_HOST);
            etDomain.setFocusable(true);
            etDomain.requestFocus();
        } else {
            etServerIP.setFocusable(true);
            etServerIP.requestFocus();
        }

        if (Preference.hasPreferenceKey(context, Constants.TOKEN_EXPIRED)) {
            etDomain.setTextColor(ContextCompat.getColor(this, R.color.black));
            etUsername.setTextColor(ContextCompat.getColor(this, R.color.black));
            etPassword.setFocusable(true);
            etPassword.requestFocus();
            String tenantedUserName = Preference.getString(context, Constants.USERNAME);
            int tenantSeparator = tenantedUserName.lastIndexOf('@');
            etUsername.setText(tenantedUserName.substring(0, tenantSeparator));
            etDomain.setText(tenantedUserName.substring(tenantSeparator + 1, tenantedUserName.length()));
            isReLogin = true;
        } else if (Constants.CLOUD_MANAGER != null && !Constants.CLOUD_MANAGER.isEmpty()) {
            etDomain.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else {
            progressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonDialogUtils.stopProgressDialog(progressDialog);
        progressDialog = null;
    }

    private OnClickListener onClickAuthenticate = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (etServerIP.getText() != null && !etUsername.getText().toString().trim().isEmpty() &&
                    ((Constants.CLOUD_MANAGER != null && !Constants.CLOUD_MANAGER.isEmpty()) ||
                            etDomain.getText() != null && !etDomain.getText().toString().trim().isEmpty()) &&
                    etUsername.getText() != null && !etUsername.getText().toString().trim().isEmpty() &&
                    etPassword.getText() != null && !etPassword.getText().toString().trim().isEmpty()) {

                String serverIP = etServerIP.getText().toString().trim();
                Preference.putString(context, Constants.PreferenceFlag.IP, serverIP);

                passwordVal = etPassword.getText().toString().trim();
                usernameVal = etUsername.getText().toString().trim();

                saveToPreferences(usernameVal);

                if (Constants.CLOUD_MANAGER != null && !Constants.CLOUD_MANAGER.isEmpty()) {
                    obtainTenantDomain(usernameVal, passwordVal);
                } else {
                    proceedToAuthentication();
                }
            } else {
                if (etServerIP.getText() != null && etServerIP.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, getResources().getString(R.string.error_server_ip),
                            Toast.LENGTH_LONG).show();
                } else if (!(Constants.CLOUD_MANAGER != null && !Constants.CLOUD_MANAGER.isEmpty()) ||
                        etDomain.getText() != null && etDomain.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, getResources().getString(R.string.error_organization),
                            Toast.LENGTH_LONG).show();
                } else if (etUsername.getText() != null && etUsername.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, getResources().getString(R.string.error_username),
                            Toast.LENGTH_LONG).show();
                } else if (etPassword.getText() != null && etPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, getResources().getString(R.string.error_password),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private void saveToPreferences(String usernameVal){
        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_WORLD_READABLE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString(Constants.USERNAME, usernameVal);
        prefsEditor.commit();
    }
    /**
     * This method is used to resolve cloud tenant domain.
     */
    private void obtainTenantDomain(String username, String password) {
        // Check network connection availability before calling the API.
        currentTenant = null;
        if (CommonUtils.isNetworkAvailable(context)) {
            progressDialog = ProgressDialog.show(context,
                    getResources().getString(R.string.dialog_authenticate),
                    getResources().getString(R.string.dialog_message_please_wait), true);
            IdentityProxy.getInstance().setContext(this.getApplicationContext());
            TenantResolverHandler tenantResolverHandler = new TenantResolverHandler(new TenantResolverCallback() {
                @Override
                public void onTenantResolved(final List<Tenant> tenants) {
                    if (tenants.isEmpty()) {
                        showEnrollmentFailedErrorMessage(getResources().getString(R.string.dialog_tenants_not_available));
                    } else if (tenants.size() == 1) {
                        currentTenant = tenants.get(0);
                        CommonDialogUtils.stopProgressDialog(progressDialog);
                        etDomain.setText(currentTenant.getTenantDomain());
                        proceedToAuthentication();
                    } else {
                        CommonDialogUtils.stopProgressDialog(progressDialog);
                        List<String> tenantNames = new ArrayList<>();
                        for (Tenant t : tenants) {
                            tenantNames.add(t.getDisplayName());
                        }
                        CommonDialogUtils.getAlertDialogWithSingleChoices(context,
                                getResources().getString(R.string.dialog_select_tenant),
                                tenantNames.toArray(new String[0]),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        currentTenant = tenants.get(which);
                                        etDomain.setText(currentTenant.getTenantDomain());
                                        proceedToAuthentication();
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                }

                @Override
                public void onAuthenticationSuccess() {
                    CommonDialogUtils.stopProgressDialog(progressDialog);
                    progressDialog = ProgressDialog.show(context,
                            getResources().getString(R.string.dialog_tenant_resolving),
                            getResources().getString(R.string.dialog_message_please_wait), true);
                }

                @Override
                public void onAuthenticationFail() {
                    showAuthenticationError();
                }

                @Override
                public void onFailure(AndroidAgentException exception) {
                    showEnrollmentFailedErrorMessage(exception.getMessage());
                }
            });
            tenantResolverHandler.resolveTenantDomain(username, password);
        } else {
            CommonDialogUtils.showNetworkUnavailableMessage(context);
        }
    }

    /**
     * This method sets the tenant domain to username and
     * invokes the method to register a dynamic client
     */
    private void proceedToAuthentication() {
        if (etDomain.getText() != null && !etDomain.getText().toString().trim().isEmpty()) {
            usernameVal += getResources().getString(R.string.intent_extra_at)
                    + etDomain.getText().toString().trim();
        }
        getClientCredentials();
    }

    /**
     * Start authentication process.
     */
    private void startAuthentication() {
        // Check network connection availability before calling the API.
        if (CommonUtils.isNetworkAvailable(context)) {
            String clientId = Preference.getString(context, Constants.CLIENT_ID);
            String clientSecret = Preference.getString(context, Constants.CLIENT_SECRET);

            if (clientId == null || clientSecret == null) {
                String clientCredentials = Preference.getString(context, Constants.CLIENT_CREDENTIALS);
                if (clientCredentials != null) {
                    try {
                        JSONObject payload = new JSONObject(clientCredentials);
                        clientId = payload.getString(Constants.CLIENT_ID);
                        clientSecret = payload.getString(Constants.CLIENT_SECRET);

                        if (clientId != null && !clientId.isEmpty() &&
                                clientSecret != null && !clientSecret.isEmpty()) {
                            initializeIDPLib(clientId, clientSecret);
                        }
                    } catch (JSONException e) {
                        String msg = "error occurred while parsing client credential payload";
                        Log.e(TAG, msg, e);
                        CommonDialogUtils.stopProgressDialog(progressDialog);
                        showInternalServerErrorMessage();
                    }
                } else {
                    String msg = "error occurred while retrieving client credentials";
                    Log.e(TAG, msg);
                    CommonDialogUtils.stopProgressDialog(progressDialog);
                    showInternalServerErrorMessage();
                }
            } else {
                initializeIDPLib(clientId, clientSecret);
            }

        } else {
            CommonDialogUtils.stopProgressDialog(progressDialog);
            CommonDialogUtils.showNetworkUnavailableMessage(context);
        }
    }

    /**
     * Initialize the Android IDP SDK by passing credentials,client ID and
     * client secret.
     *
     * @param clientKey    client id value to access APIs..
     * @param clientSecret client secret value to access APIs.
     */
    private void initializeIDPLib(String clientKey, String clientSecret) {
        String serverIP = Preference.getString(context.getApplicationContext(),
                Constants.PreferenceFlag.IP);

        if (serverIP != null && !serverIP.isEmpty()) {
            String serverURL = serverIP + Constants.OAUTH_ENDPOINT;
            Editable tenantDomain = etDomain.getText();

            if (tenantDomain != null && !tenantDomain.toString().trim().isEmpty()) {
                username =
                        etUsername.getText().toString().trim() +
                                context.getResources().getString(R.string.intent_extra_at) + tenantDomain.toString().trim();

            } else {
                username = etUsername.getText().toString().trim();
            }

            Preference.putString(context, Constants.CLIENT_ID, clientKey);
            Preference.putString(context, Constants.CLIENT_SECRET, clientSecret);

            CredentialInfo info = new CredentialInfo();
            info.setClientID(clientKey);
            info.setClientSecret(clientSecret);
            info.setUsername(username);
            info.setPassword(passwordVal);
            info.setTokenEndPoint(serverURL);

            //adding device-specific scope
            String deviceScope = Constants.DEVICE_SCOPE_PREFIX + deviceInfo.getDeviceId();
            info.setScopes(deviceScope);
            if (tenantDomain != null && !tenantDomain.toString().trim().isEmpty()) {
                info.setTenantDomain(tenantDomain.toString().trim());
            }
            IdentityProxy.getInstance().init(info, AuthenticationActivity.this, this.getApplicationContext());
        }
    }

    @Override
    public void onAPIAccessReceive(String status) {
        if (status != null) {
            if (status.trim().equals(Constants.Status.SUCCESSFUL)) {
                CommonDialogUtils.stopProgressDialog(progressDialog);
                if (isReLogin) {
                    Preference.removePreference(context, Constants.TOKEN_EXPIRED);
                    loadProvisioningActivity();
                } else {
                    Preference.putString(context, Constants.USERNAME, username);
                    Preference.putBoolean(this, Constants.IS_REGISTERED, true);
                    loadProvisioningActivity();
                }
            } else if (status.trim().equals(Constants.Status.AUTHENTICATION_FAILED)) {
                showAuthenticationError();
                // clearing client credentials from shared memory
                CommonUtils.clearClientCredentials(context);
            } else if (status.trim().equals(Constants.Status.INTERNAL_SERVER_ERROR)) {
                showInternalServerErrorMessage();
            } else {
                showAuthCommonErrorMessage();
            }
        } else {
            showAuthCommonErrorMessage();
        }
    }

    @Override
    public void onReceiveAPIResult(Map<String, String> result, int requestCode) {
        if (requestCode == Constants.DYNAMIC_CLIENT_REGISTER_REQUEST_CODE) {
            manipulateDynamicClientResponse(result);
        }
    }

    /**
     * Manipulates the dynamic client registration response received from server.
     *
     * @param result the result of the dynamic client request
     */
    private void manipulateDynamicClientResponse(Map<String, String> result) {
        String responseStatus;
        if (result != null) {
            responseStatus = result.get(Constants.STATUS);
            if (Constants.Status.CREATED.equals(responseStatus)) {
                String dynamicClientResponse = result.get(Constants.RESPONSE);
                if (dynamicClientResponse != null) {
                    Preference.putString(context, Constants.CLIENT_CREDENTIALS, dynamicClientResponse);
                    startAuthentication();
                }
            } else if (Constants.Status.UNAUTHORIZED.equals(responseStatus)) {
                showAuthenticationError();
            } else if (!Constants.Status.SUCCESSFUL.equals(responseStatus)) {
                if (result.containsKey(Constants.RESPONSE)) {
                    showEnrollmentFailedErrorMessage("Code: " + responseStatus + "\nError: " + result.get(Constants.RESPONSE));
                } else {
                    showEnrollmentFailedErrorMessage("Code: " + responseStatus);
                }
            }
        } else {
            showEnrollmentFailedErrorMessage(null);
        }
    }

    /**
     * This method is used launch the provisioning activity.
     */
    private void loadProvisioningActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, ProvisioningActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.USERNAME, usernameVal);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !isReLogin) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private DialogInterface.OnClickListener senderIdFailedClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                    btnSignIn.setBackgroundResource(R.drawable.btn_orange);
                    btnSignIn.setTextColor(ContextCompat.getColor(AuthenticationActivity.this, R.color.white));
                    btnSignIn.setEnabled(true);
                }
            };

    /**
     * Shows enrollment failed error.
     */
    private void showEnrollmentFailedErrorMessage(String message) {
        CommonDialogUtils.stopProgressDialog(progressDialog);
        final String messageDescription;
        String descriptionText = getResources().getString(
                R.string.error_enrollment_failed_detail);
        if (message != null) {
            messageDescription = descriptionText + " " + message;
        } else {
            messageDescription = descriptionText;
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                        getResources().getString(
                                R.string.error_enrollment_failed),
                        messageDescription,
                        getResources().getString(
                                R.string.button_ok),
                        senderIdFailedClickListener);
            }
        });
    }

    /**
     * Shows internal server error message for authentication.
     */
    private void showInternalServerErrorMessage() {
        CommonDialogUtils.stopProgressDialog(progressDialog);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                        getResources().getString(
                                R.string.title_head_connection_error),
                        getResources().getString(
                                R.string.error_internal_server),
                        getResources().getString(
                                R.string.button_ok),
                        null);
            }
        });

    }

    /**
     * Shows credentials error message for authentication.
     */
    private void showAuthenticationError() {
        CommonDialogUtils.stopProgressDialog(progressDialog);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                        getResources().getString(R.string.title_head_authentication_error),
                        getResources().getString(R.string.error_authentication_failed),
                        getResources().getString(R.string.button_ok),
                        null);
            }
        });
    }

    /**
     * Shows common error message for authentication.
     */
    private void showAuthCommonErrorMessage() {
        CommonDialogUtils.stopProgressDialog(progressDialog);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                        getResources().getString(
                                R.string.title_head_authentication_error),
                        getResources().getString(
                                R.string.error_for_all_unknown_authentication_failures),
                        getResources().getString(
                                R.string.button_ok),
                        null);
            }
        });

    }

    /**
     * This method is used to retrieve consumer-key and consumer-secret.
     */
    private void getClientCredentials() {
        String ipSaved = Preference.getString(context.getApplicationContext(),
                Constants.PreferenceFlag.IP);
        String authenticationTitle = getResources().getString(R.string.dialog_authenticate);
        progressDialog = ProgressDialog.show(context, authenticationTitle, getResources().
                getString(R.string.dialog_message_please_wait), true);
        if (ipSaved != null && !ipSaved.isEmpty()) {
            String applicationName = Constants.API_APPLICATION_NAME_PREFIX +
                    deviceInfo.getDeviceId();
            ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
            apiRegistrationProfile.setApplicationName(applicationName);
            apiRegistrationProfile.setIsAllowedToAllDomains(false);
            apiRegistrationProfile.setIsMappingAnExistingOAuthApp(false);
            apiRegistrationProfile.setTags(SUBSCRIBED_API);
            DynamicClientManager dynamicClientManager = new DynamicClientManager();
            try {
                dynamicClientManager.getClientCredentials(usernameVal, passwordVal, context,
                        AuthenticationActivity.this, apiRegistrationProfile);
                Preference.putString(context, Constants.CLIENT_NAME, applicationName);
            } catch (AndroidAgentException e) {
                String message = "Client credentials generation failed";
                Log.e(TAG, message, e);
                showEnrollmentFailedErrorMessage(message);
            }
        } else {
            String message = "There is no valid IP to contact the server";
            Log.e(TAG, message);
            showEnrollmentFailedErrorMessage(message);
        }
    }
}