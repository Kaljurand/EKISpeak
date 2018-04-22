package ee.eki.ekisynt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

/**
 *
 */
public class GetSampleText extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("GetSampleText");
        int result = TextToSpeech.LANG_AVAILABLE;
        Intent returnData = new Intent();

        Intent i = getIntent();
        String language = i.getExtras().getString("language");

        Log.i("GetSampleText language: " + language);

        if (language.equals(EKISpeakService.DEFAULT_LANG)) {
            returnData.putExtra("sampleText", getString(R.string.est_sample));
        } else {
            result = TextToSpeech.LANG_NOT_SUPPORTED;
            returnData.putExtra("sampleText", "");
        }

        setResult(result, returnData);

        finish();
    }
}