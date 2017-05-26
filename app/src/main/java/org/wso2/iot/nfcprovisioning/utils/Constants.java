package org.wso2.iot.nfcprovisioning.utils;

/**
 * Created by janak on 5/25/17.
 */

public class Constants {
    //TODO: add def host
    public static final String TOKEN_EXPIRED = "token_expired";
    public static final String AGENT_PACKAGE = "org.wso2.iot.nfcprovisioning";
    public static final String OAUTH_AUTHENTICATOR = "OAUTH_AUTHENTICATOR";
    public static final String MUTUAL_SSL_AUTHENTICATOR = "MUTUAL_SSL_AUTHENTICATOR";
    public static final String AUTHENTICATOR_IN_USE = OAUTH_AUTHENTICATOR;
    public static final String OAUTH_ENDPOINT = "/token";
    public final static String API_APPLICATION_CONTEXT =
            "/api-application-registration";
    public final static String API_APPLICATION_REGISTRATION_CONTEXT = API_APPLICATION_CONTEXT +
            "/register";
    public final static String API_APPLICATION_UNREGISTRATION_CONTEXT = API_APPLICATION_CONTEXT +
            "/unregister";

    public static final String USER_AGENT = "Mozilla/5.0 ( compatible ), Android";

    public static final String DEFAULT_HOST ="";//TODO: check how to implement def host
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CLIENT_NAME = "client_name";
    public static final String USERNAME = "username";
    public static final String CLOUD_MANAGER = null;//TODO:check cloud login
    public static final int SIGN_IN_NOTIFICATION_ID = 0;
    public final static String API_APPLICATION_NAME_PREFIX = "cdmf_android_";//TODO:check for alternate prefix
    public static final String STATUS = "status";
    public static final String RESPONSE = "response";

    /**
     * Request codes.
     */
    public static final int REGISTER_REQUEST_CODE = 300;
    public static final int IS_REGISTERED_REQUEST_CODE = 301;
    public static final int DYNAMIC_CLIENT_REGISTER_REQUEST_CODE = 302;
    public static final int UNREGISTER_REQUEST_CODE = 305;
    public static final int AUTHENTICATION_REQUEST_CODE = 311;


    public final class PreferenceFlag {
        private PreferenceFlag() {
            throw new AssertionError();
        }
        public static final String REG_ID = "regId";
        public static final String REGISTERED = "registered";
        public static final String IP = "ip";
        public static final String DEVICE_ACTIVE = "deviceActive";
        public static final String PORT = "serverPort";
        public static final String PROTOCOL = "serverProtocol";
        public static final String APPLIED_POLICY = "appliedPolicy";
        public static final String IS_AGREED = "isAgreed";
        public static final String NOTIFIER_TYPE = "notifierType";
        public static final String CURRENT_INSTALLING_APP = "installingApplication";
        public static final String LOCAL_NOTIFIER_INVOKED_PREF_KEY = "localNotificationInvoked";
        public static final String DEVICE_ID_PREFERENCE_KEY = "deviceId";
        public static final String LAST_SERVER_CALL = "lastServerCall";
    }

    /**
     * Sub Status codes
     */
    public final class Status {
        private Status(){
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
