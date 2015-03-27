package ee.eki.heli.EKISpeak;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class GetSampleText extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.w("ss", "GetSampleText:" );
		int result = TextToSpeech.LANG_AVAILABLE;
		Intent returnData = new Intent();

		Intent i = getIntent();
		String language = i.getExtras().getString("language");

		Log.w("ss", "GetSampleText language="+language);
		if (language.equals("est")) {
			returnData.putExtra("sampleText", getString(R.string.est_sample));
		} else {
			result = TextToSpeech.LANG_NOT_SUPPORTED;
			returnData.putExtra("sampleText", "");
		}
		
		setResult(result, returnData);

		finish();
	}
}