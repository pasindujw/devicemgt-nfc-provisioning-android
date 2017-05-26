package org.wso2.iot.nfcprovisioning;

import android.content.Intent;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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
                startActivity();
            }
        }, AUTO_HIDE_DELAY_MILLIS);
    }

    private void startActivity() {
        if (!Preference.hasPreferenceKey(this, Constants.TOKEN_EXPIRED) &&
                Constants.AUTHENTICATOR_IN_USE.equals(Constants.MUTUAL_SSL_AUTHENTICATOR)) {
            instantiatedActivityClass = ProvisioningActivity.class;
        } else {
            instantiatedActivityClass = AuthenticationActivity.class;
        }
        Intent intent = new Intent(getApplicationContext(), instantiatedActivityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
