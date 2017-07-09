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
    public final static String API_APPLICATION_CONTEXT = "/api-application-registration";
    public final static String API_APPLICATION_REGISTRATION_CONTEXT = API_APPLICATION_CONTEXT + "/register";
    public static final String DEFAULT_HOST = BuildConfig.DEFAULT_HOST;
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CLIENT_NAME = "client_name";
    public static final String USERNAME = "username";
    public final static String API_APPLICATION_NAME_PREFIX = "cdmf_android_nfc_";
    public final static String DEVICE_SCOPE_PREFIX = "device_";
    public static final String STATUS = "status";
    public static final String RESPONSE = "response";
    public static final String TOKEN_EXPIRED = "token_expired";
    public static final String IS_REGISTERED = "is_reg";
    public static final boolean PUSH_KIOSK_APP = BuildConfig.PUSH_KIOSK_APP;
    public static final boolean USE_REMOTE_CONFIG = BuildConfig.USE_REMOTE_CONFIG;
    public static final String CLIENT_CREDENTIALS = "clientCredentials";
    public static final long remoteConfigCacheExpiration = 3600;
    public static final String ANDROID_APP_EXTRA_TOKREN = "android.app.extra.token";
    public static final String ANDROID_APP_EXTRA_APPURL = "android.app.extra.appurl";
    public static final String ADMIN_EXTRA_BUNDLE = "admin extras bundle";
    public static final String WIFI_SECURITY_TYPE_NONE = "NONE";
    public static final String CLOUD_MANAGER = BuildConfig.CLOUD_MANAGER;
    public static final String BUMP_SENARIO = BuildConfig.BUMP_SENARIO;
    /**
     * Request codes.
     */
    public static final int DYNAMIC_CLIENT_REGISTER_REQUEST_CODE = 302;
    public static final int TOKEN_VERIFICATION_REQUEST_CODE = 306;

    /**
     * Shared Pref Flags.
     */
    public final class PreferenceFlag {
        private PreferenceFlag() {
            throw new AssertionError();
        }

        public static final String IP = "ip";
        public static final String USERNAME_FOR_AUTHENTICATION ="";
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

    /**
     * Configuration Keys.
     */
    public final class ConfigKey {
        private ConfigKey() {
            throw new AssertionError();
        }

        public static final String PACKAGE_NAME = "prov_package_name";
        public static final String PACKAGE_DOWNLOAD_LOCATION = "prov_package_download_location";
        public static final String PACKAGE_CHECKSUM = "prov_package_checksum";
        public static final String WIFI_SSID = "prov_package_wifi_ssid";
        public static final String WIFI_SECURITY_TYPE = "prov_wifi_security_type";
        public static final String WIFI_PASSWORD = "prov_wifi_password";
        public static final String TIME_ZONE = "prov_time_zone";
        public static final String LOCALE = "prov_locale";
        public static final String ENCRYPTION = "prov_encryption";
        public static final String KIOSK_APP_DOWNLOAD_LOCATION = "prov_kiosk_app_download_location";
    }

    /**
     * UI class values.
     */
    public final class UI {
        private UI() {
            throw new AssertionError();
        }

        public static final float ALPHA_ZERO = 0.0F;
        public static final float ANIMATE_ALPHA = 1.0F;
        public static final long ANIMATE_DURATION = 200L;
        public static final int ACCESSIBILITY_EVENT_TYPE = 2048;
        public static final String TIME_FORMAT = "GMT%+d:%02d %s";
        public static final String PASSWORD = "password";
    }

    /**
     * Http Headers class values.
     */
    public final class HttpHeaders {
        private HttpHeaders() {
            throw new AssertionError();
        }

        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String APPLICATION_JSON = "application/json";
        public static final String USER_AGENT = "User-Agent";
        public static final String USER_AGENT_VAL = "Mozilla/5.0 ( compatible ), Android";
    }
}