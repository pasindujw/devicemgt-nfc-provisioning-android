package org.wso2.iot.nfcprovisioning;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.wso2.iot.nfcprovisioning.utils.Constants;
import org.wso2.iot.nfcprovisioning.utils.Preference;

import java.util.Calendar;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static Class<?> instantiatedActivityClass = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = SplashActivity.this;

        String footerText = String.format(
                getResources().getString(R.string.footer_text),
                BuildConfig.VERSION_NAME,
                Calendar.getInstance().get(Calendar.YEAR)
        );
        TextView textViewFooter = (TextView) findViewById(R.id.textViewFooter);
        textViewFooter.setText(footerText);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
            /* New Handler to start the WorkProfileSelectionActivity
             * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
                startActivity();
            }
        }, AUTO_HIDE_DELAY_MILLIS);
    }

    private void startActivity() {

        if((!Preference.getBoolean(this, Constants.IS_REGISTERED) &&
                Constants.AUTHENTICATOR_IN_USE.equals(Constants.OAUTH_AUTHENTICATOR))
                ||Preference.hasPreferenceKey(this, Constants.TOKEN_EXPIRED)){
            instantiatedActivityClass = AuthenticationActivity.class;
        }else {
            instantiatedActivityClass = ProvisioningActivity.class;
        }

        Intent intent = new Intent(getApplicationContext(), instantiatedActivityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
