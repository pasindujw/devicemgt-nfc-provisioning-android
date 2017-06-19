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
package org.wso2.iot.nfcprovisioning.utils;

import org.wso2.iot.nfcprovisioning.BuildConfig;

public class Constants {

    public static final boolean DEBUG_MODE_ENABLED = BuildConfig.DEBUG_MODE_ENABLED;
    public static final String APP_PACKAGE = BuildConfig.APP_PACKAGE;
    public static final String OAUTH_AUTHENTICATOR = "OAUTH_AUTHENTICATOR";
    public static final String MUTUAL_SSL_AUTHENTICATOR = "MUTUAL_SSL_AUTHENTICATOR";
    public static final String AUTHENTICATOR_IN_USE = OAUTH_AUTHENTICATOR;
    public static final String OAUTH_ENDPOINT = "/token";
    public final static String API_APPLICATION_CONTEXT =
            "/api-application-registration";
    public final static String API_APPLICATION_REGISTRATION_CONTEXT = API_APPLICATION_CONTEXT +
            "/register";
    public static final String USER_AGENT = "Mozilla/5.0 ( compatible ), Android";
    public static final String DEFAULT_HOST = BuildConfig.DEFAULT_HOST;
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CLIENT_NAME = "client_name";
    public static final String USERNAME = "username";
    public final static String API_APPLICATION_NAME_PREFIX = "cdmf_android_";//TODO:check for alternate prefix
    public static final String STATUS = "status";
    public static final String RESPONSE = "response";
    public static final String TOKEN_EXPIRED = "token_expired";
    public static final String IS_REGISTERED = "is_registered";
    public static final boolean PUSH_KIOSK_APP = BuildConfig.PUSH_KIOSK_APP;
    public static final boolean USE_REMOTE_CONFIG = BuildConfig.USE_REMOTE_CONFIG;

    /**
     * Request codes.
     */
    public static final int DYNAMIC_CLIENT_REGISTER_REQUEST_CODE = 302;
    public static final int TOKEN_VERIFICATION_REQUEST_CODE = 306;


    public final class PreferenceFlag {
        private PreferenceFlag() {
            throw new AssertionError();
        }

        public static final String IP = "ip";
    }

    /**
     * Sub Status codes
     */
    public final class Status {
        private Status() {
            throw new AssertionError();
        }

        public static final String SUCCESSFUL = "200";
        public static final String CREATED = "201";
        public static final String ACCEPT = "202";
        public static final String AUTHENTICATION_FAILED = "400";
        public static final String UNAUTHORIZED = "401";
        public static final String INTERNAL_SERVER_ERROR = "500";
    }
}