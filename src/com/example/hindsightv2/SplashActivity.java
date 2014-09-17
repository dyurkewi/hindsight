package com.example.hindsightv2;

import com.camera.simplemjpeg.MjpegView;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
 
public class SplashActivity extends Activity {
		
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        


		new Handler().postDelayed(new Runnable() {
		            @Override
		            public void run() {
		            	
		            	
		            	
		            	// receive parameters from PreferenceActivity
		                Bundle bundle = getIntent().getExtras();
		                String hostname = bundle.getString( PreferenceActivity.KEY_HOSTNAME);
		                String portnum =  bundle.getString( PreferenceActivity.KEY_PORTNUM);
		        		// launch MjpegActivity
		        		Intent intent = new Intent( SplashActivity.this, MjpegActivity.class);
		        		intent.putExtra( PreferenceActivity.KEY_HOSTNAME, hostname);
		        		intent.putExtra( PreferenceActivity.KEY_PORTNUM, portnum);
		        		SplashActivity.this.startActivity( intent);
		        		SplashActivity.this.finish();
		            }
		        }, 1000); //set in ms
        
    }
     
    @Override
    protected void onDestroy() {
         
        super.onDestroy();
         
    }
}