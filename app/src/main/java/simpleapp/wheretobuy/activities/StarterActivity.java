package simpleapp.wheretobuy.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.constants.Constants;

public class StarterActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        ActivityStarter starter = new ActivityStarter();
        starter.start();
    }

    private class ActivityStarter extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(Constants.SPLASH_TIME);
            } catch (Exception e) {
                Log.e("SplashScreen", e.getMessage());
            }

            Intent intent = new Intent(StarterActivity.this, MapActivity.class);
            StarterActivity.this.startActivity(intent);
            StarterActivity.this.finish();
        }
    }
}