package com.example.hindsightv2;


import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class Voice extends ActionBarActivity {
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice);
		
		displaySpeechRecognizer();
		
		//Get the message from the intent
		//Intent intent = getIntent();
		//String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_VOICE);
		
		//Create the text view
		//TextView textView = new TextView(this);
	    //textView.setTextSize(40);
	    //textView.setText(message);

	 // Set the text view as the activity layout
	    //setContentView(textView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voice, menu);
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
	
	private static final int SPEECH_REQUEST_CODE = 0;

	// Create an intent that can start the Speech Recognizer activity
	private void displaySpeechRecognizer() {
	    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    /*Bundle extras = new Bundle();
	    extras.putString(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    extras.putString(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,500);
	    extras.putString(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 200);
	    intent.putExtras(extras);*/

	    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,500);
	    intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 200);
	    
	// Start the activity, the intent will be populated with the speech text
	    startActivityForResult(intent, SPEECH_REQUEST_CODE);
	}

	// This callback is invoked when the Speech Recognizer returns.
	// This is where you process the intent and extract the speech text from the intent.
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent data) {
	    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
	        List<String> results = data.getStringArrayListExtra(
	                RecognizerIntent.EXTRA_RESULTS);
	        String spokenText = results.get(0);
	        TextView textViewVoice = new TextView(this);
		    textViewVoice.setTextSize(40);
		    textViewVoice.setText(spokenText);
	        textViewVoice.setText(spokenText);

	   	 // Set the text view as the activity layout
	   	    setContentView(textViewVoice);
	        // Do something with spokenText
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
}
