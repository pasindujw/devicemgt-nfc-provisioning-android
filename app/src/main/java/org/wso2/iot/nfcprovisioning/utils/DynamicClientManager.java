/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.iot.nfcprovisioning.utils;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.iot.nfcprovisioning.proxy.IDPTokenManagerException;
import org.wso2.iot.nfcprovisioning.proxy.IdentityProxy;
import org.wso2.iot.nfcprovisioning.proxy.beans.EndPointInfo;
import org.wso2.iot.nfcprovisioning.proxy.interfaces.APIResultCallBack;
import org.wso2.iot.nfcprovisioning.proxy.utils.ServerUtilities;
import org.wso2.iot.nfcprovisioning.beans.ApiRegistrationProfile;
import org.wso2.iot.nfcprovisioning.beans.ServerConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to register and unregister oauth application.
 */
public class DynamicClientManager implements APIResultCallBack {

    private static final String TAG = DynamicClientManager.class.getSimpleName();
    private static final int MAX_RETRIES = 3;

    /**
     * This method is used to register an oauth application in the backend.
     *
     * @param apiRegistrationProfile Payload of the register request.
     *
     * @return returns consumer key and consumer secret if success. Else returns null
     *         if it fails to register.
     * @throws AndroidAgentException
     */
    public void getClientCredentials(String username, String password,
                                     Context context, APIResultCallBack apiResultCallback,
                                     ApiRegistrationProfile apiRegistrationProfile)
            throws AndroidAgentException {
        IdentityProxy.getInstance().setContext(context);
        EndPointInfo endPointInfo = new EndPointInfo();
        String endPoint = ServerConfig.getAPIServerURL(context) +
                Constants.API_APPLICATION_REGISTRATION_CONTEXT;
        endPointInfo.setHttpMethod(org.wso2.iot.nfcprovisioning.proxy.utils.Constants.HTTP_METHODS.POST);
        endPointInfo.setEndPoint(endPoint);
        endPointInfo.setRequestParams(apiRegistrationProfile.toJSON());
        sendRequest(endPointInfo, apiResultCallback, Constants.DYNAMIC_CLIENT_REGISTER_REQUEST_CODE,
                username, password);
    }

    /**
     * This method is used to send requests to backend.
     * The reason to use this method because the function which is already
     * available for sending requests is secured with token. Therefor this can be used
     * to send requests without tokens.
     */
    private void sendRequest(final EndPointInfo endPointInfo,
                             final APIResultCallBack apiResultCallback, final int requestCode,
                             final String username, final String password) {
        RequestQueue queue =  null;
        int requestMethod = 0;
        org.wso2.iot.nfcprovisioning.proxy.utils.Constants.HTTP_METHODS httpMethod = endPointInfo.getHttpMethod();
        switch (httpMethod) {
            case POST:
                requestMethod = Request.Method.POST;
                break;
            case DELETE:
                requestMethod = Request.Method.DELETE;
                break;
        }

        try {
            queue = ServerUtilities.getCertifiedHttpClient();
        } catch (IDPTokenManagerException e) {
            Log.e(TAG, "Failed to retrieve HTTP client", e);
        }

        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(requestMethod, endPointInfo.getEndPoint(),
                                            (endPointInfo.getRequestParams() != null) ?
                                            new JSONObject(endPointInfo.getRequestParams()) : null,
                                                      new Response.Listener<JSONObject>() {
                                                          @Override
                                                          public void onResponse(JSONObject response) {
                                                              Log.d(TAG, response.toString());
                                                          }
                                                      },
                                                      new Response.ErrorListener() {
                                                          @Override
                                                          public void onErrorResponse(VolleyError error) {
                                                              Log.d(TAG, error.toString());
                                                              Map<String, String> responseParams = new HashMap<>();
                                                              String statusCode = "500";
                                                              if (error.networkResponse != null) {
                                                                  statusCode = String.valueOf(error.networkResponse.statusCode);
                                                              }
                                                              responseParams.put(
                                                                      org.wso2.iot.nfcprovisioning.proxy.utils.Constants.SERVER_RESPONSE_STATUS,
                                                                      statusCode
                                                              );
                                                              if (com.android.volley.ParseError.class.isInstance(error)) {
                                                                  responseParams.put(
                                                                          org.wso2.iot.nfcprovisioning.proxy.utils.Constants.SERVER_RESPONSE_BODY,
                                                                          "Invalid tenant domain"
                                                                  );
                                                              } else if (error.getMessage() != null) {
                                                                  responseParams.put(
                                                                          org.wso2.iot.nfcprovisioning.proxy.utils.Constants.SERVER_RESPONSE_BODY,
                                                                          error.getMessage()
                                                                  );
                                                              }
                                                              apiResultCallback.onReceiveAPIResult(responseParams, requestCode);
                                                          }
                                                      })
            {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    String result = new String(response.data);
                    if(org.wso2.iot.nfcprovisioning.proxy.utils.Constants.DEBUG_ENABLED) {
                        if(result != null && !result.isEmpty()) {
                            Log.d(TAG, "Result :" + result);
                        }
                    }
                    Map<String, String> responseParams = new HashMap<>();
                    responseParams.put(org.wso2.iot.nfcprovisioning.proxy.utils.Constants.SERVER_RESPONSE_BODY, result);
                    responseParams.put(org.wso2.iot.nfcprovisioning.proxy.utils.Constants.SERVER_RESPONSE_STATUS, String.valueOf(
                            response.statusCode));
                    apiResultCallback.onReceiveAPIResult(responseParams, requestCode);
                    return super.parseNetworkResponse(response);
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(Constants.HttpHeaders.CONTENT_TYPE, Constants.HttpHeaders.APPLICATION_JSON);
                    headers.put(Constants.HttpHeaders.ACCEPT, Constants.HttpHeaders.APPLICATION_JSON);
                    headers.put(Constants.HttpHeaders.USER_AGENT, Constants.HttpHeaders.USER_AGENT_VAL);
                    if (username != null && password != null) {
                        String basicAuthValue = "Basic " +
                                new String(Base64.encodeBase64((username + ":" + password)
                                        .getBytes()));
                        headers.put("Authorization", basicAuthValue);
                    }
                    return headers;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(
                    org.wso2.iot.nfcprovisioning.proxy.utils.Constants.HttpClient.DEFAULT_TIME_OUT,
                    MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse request JSON", e);
        }
        request.setRetryPolicy(new DefaultRetryPolicy(
                org.wso2.iot.nfcprovisioning.proxy.utils.Constants.HttpClient.DEFAULT_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    @Override
    public void onReceiveAPIResult(Map<String, String> result, int requestCode) {}
}