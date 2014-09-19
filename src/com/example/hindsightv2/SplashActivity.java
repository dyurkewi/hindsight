package com.example.hindsightv2;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import com.jcraft.jsch.*;
import java.awt.*;
import java.util.Properties;
import android.util.Log;


import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;
import com.example.hindsightv2.MjpegActivity.DoRead;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
 
public class SplashActivity extends Activity {
	String username = "pi";
    String password = "Da1ni9el!";
    String hostname = "192.168.0.12";
    int port = 22;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new initSSH().execute( hostname, password, hostname);
        
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
		        }, 5000); //set in ms
        
         
        
    }


    public class initSSH extends AsyncTask<String, Void, MjpegInputStream> {
    	protected MjpegInputStream doInBackground( String... params){
    		try{
                JSch sshChannel = new JSch();
                Session session=sshChannel.getSession(username, hostname, 22);
                session.setPassword(password);
                Properties prop = new Properties();
                prop.setProperty("StrictHostKeyChecking", "no");
                session.setConfig(prop);
               
                session.connect(30000); //30 seconds
                Channel channel=session.openChannel("exec");
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                //Exec command needs to be BEFORE connection.
                ((ChannelExec) channel).setCommand("sudo /bin/gst-server.sh");
                channel.connect(3*1000);
                //Channel channel=session.openChannel("exec"); 
            	//channel.setOutputStream(System.out);
            	 
                 
                //ChannelExec channel = (ChannelExec)session.openChannel("exec"); //or exec?
                //channel.connect(3*1000);
                //channel.setOutputStream(System.out);
                //((ChannelExec) channel).setCommand("sudo -S -p ''/bin/gst-server.sh");
                /*
                channel.connect() ;
                InputStream input = channel.getInputStream();
                int data = input.read();
    			
                while(data != -1)
                {
                	outputBuffer.append((char)data);
                	data = input.read();
                }

                channel.disconnect();
                */
                
                return null;
            } catch(JSchException e){
            	System.out.println(e);
            }
    		return null;
    	}
    }

     
    @Override
    protected void onDestroy() {
         
        super.onDestroy();
         
    }
}