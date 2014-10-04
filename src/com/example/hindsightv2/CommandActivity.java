package com.example.hindsightv2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import com.camera.simplemjpeg.MjpegInputStream;
import com.example.hindsightv2.SplashActivity.initSSH;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class CommandActivity extends Activity {

	public static final String TAG="CommandActivity";
	private Socket client;
	private FileInputStream fileInputStream;
	private BufferedInputStream bufferedInputStream;
	private OutputStream outputStream;
	private Button button;
	private TextView text;
	
    public static final String SERVERIP = "192.168.0.17";
    public static final int SERVERPORT = 3490; //
    public String message = "Hello World!";

	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_command);
	

		button = (Button) findViewById(R.id.send_command_button);   //reference to the send button
		text = (TextView) findViewById(R.id.send_command_textview);   //reference to the text view
		EditText editText = (EditText) findViewById(R.id.command_message);
		
		//Button press event listener
		button.setOnClickListener(new View.OnClickListener() {
		   public void onClick(View v) {
			   //new doSendCommand().execute( null, null);
			   new Thread(new Client()).start(); // start thread to do networking
		   }
		  });
	}
	
	public class Client implements Runnable {
	    @Override
	    public void run() {
	        try {

	            // send message to Pi
	            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
	            DatagramSocket clientSocket = new DatagramSocket();
	            byte[] sendData = new byte[1024];
	            String sentence = message;
	            sendData = sentence.getBytes();
	            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, SERVERPORT);
	            clientSocket.send(sendPacket);

	            // get reply back from Pi
	            byte[] receiveData1 = new byte[1024];
	            DatagramPacket receivePacket = new DatagramPacket(receiveData1, receiveData1.length);
	            clientSocket.receive(receivePacket);
	            Log.d(TAG, "Received Back from Pi");
	            clientSocket.close();
	        } 
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public class doSendCommand extends AsyncTask<String, Void, FileInputStream> {
    	protected FileInputStream doInBackground( String... params){
    		//File file = new File("/sendParameters/Hello.txt"); //create file instance, file to transfer or any data
    		File file = new File("/mnt/messagetest/helloworld");

		    try {

		     client = new Socket("192.168.0.17", 22);//4444);// ip address and port number of ur hardware device

		     byte[] mybytearray = new byte[(int) file.length()]; //create a byte array to file //"Hello World!"


		     fileInputStream = new FileInputStream(file);
		     bufferedInputStream = new BufferedInputStream(fileInputStream);  

		     bufferedInputStream.read(mybytearray, 0, mybytearray.length); //read the file

		     outputStream = client.getOutputStream();

		     outputStream.write(mybytearray, 0, mybytearray.length); //write file to the output stream byte by byte
		     outputStream.flush();
		     bufferedInputStream.close();
		     outputStream.close();
		           client.close();

		           Log.d(TAG, "File Sent");


		    } catch (UnknownHostException e) {
		    	Log.d(TAG, "UnknownHost");
		     e.printStackTrace();
		    } catch (IOException e) {
		    	Log.d(TAG, "IOException");
		     e.printStackTrace();
		    }
		    Log.d(TAG, "Can't Send");
    		return null;
    	}
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.command, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
