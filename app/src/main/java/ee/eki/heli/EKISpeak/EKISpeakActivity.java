package ee.eki.heli.EKISpeak;

//http://www.androidhive.info/2012/01/android-text-to-speech-tutorial/

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EKISpeakActivity extends Activity implements TextToSpeech.OnInitListener {
	
	private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
        btnSpeak = (Button) findViewById(R.id.SpeakButton);
        
        txtText = (EditText) findViewById(R.id.speakText);
		
        //tts = new TextToSpeech(this, this);
	}

	public void onResume (){
		super.onResume(); 
		tts = new TextToSpeech(this, this);
	}
	
	public void onSettingsClicked(View v) {
		Intent intent = new Intent();
		intent.setAction("com.android.settings.TTS_SETTINGS");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}

	
	public void onSpeakClicked(View v) {
		speakOut();
	}

    private void speakOut() {
        String text = txtText.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
	
	
	@Override
	public void onInit(int status) {
		 //peale keeleseadete muutmist tuleks uuesti kontrollida.
		
        if (status == TextToSpeech.SUCCESS) {
             int result = tts.setLanguage(new Locale("est","EST"));
             if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                btnSpeak.setEnabled(false);
            } else {
                btnSpeak.setEnabled(true);
                //speakOut();
            }
         } else {
            Log.e("TTS", "Initilization Failed!");
        }
		
	}
	

}