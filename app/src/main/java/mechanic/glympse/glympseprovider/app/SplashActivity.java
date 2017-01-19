package mechanic.glympse.glympseprovider.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import mechanic.glympse.glympseprovider.R;
import mechanic.glympse.glympseprovider.data.local.PrefsHelper;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;


public class SplashActivity extends AppCompatActivity {
    private PrefsHelper prefsHelper;
    private boolean isLoggedIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefsHelper = new PrefsHelper(this);
        isLoggedIn = prefsHelper.getPref(ApplicationMetadata.LOGIN,false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isLoggedIn){
                    Intent in=new Intent(SplashActivity.this,MainActivity.class);
                    in.putExtra("message","2");
                    startActivity(in);
                    finish();
                }else {

                    Intent in = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(in);
                    finish();
                }

            }
        },1000);
    }
}
