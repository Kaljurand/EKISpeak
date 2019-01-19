package ee.eki.ekisynt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.TextView;

import java.util.Locale;

public class ProcessTextActivity extends Activity {

    private static final String UTT_ID = "PROCESS_TEXT_UTT_ID";

    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_text);
        TextView mText = findViewById(R.id.tvProcessText);
        final String text;

        String textAux = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
        int maxTextSize = TextToSpeech.getMaxSpeechInputLength();
        if (textAux.length() > maxTextSize) {
            text = textAux.substring(0, maxTextSize);
            mText.setText(text + "\n\n(reading only the first " + maxTextSize + " characters)");
        } else {
            text = textAux;
            mText.setText(text);
        }

        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTts.setLanguage(new Locale("et-EE"));
                say(text);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTts != null) {
            mTts.shutdown();
        }
    }

    private void say(String text) {
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onDone(String utteranceId) {
                Log.i("onDone: " + utteranceId);
                done();
            }

            @Override
            public void onError(String utteranceId) {
                Log.i("onError: " + utteranceId);
                done();
            }

            @Override
            public void onError(String utteranceId, int errorCode) {
                Log.i("onError: " + utteranceId + "\n errorCode: " + errorCode);
                done();
            }

            @Override
            public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                Log.i("onBeginSynthesis: " + utteranceId + " sampleRateInHz: " + sampleRateInHz
                        + " format: " + audioFormat + " channelCount: " + channelCount);
            }

            @Override
            public void onStart(String utteranceId) {
                Log.i("onStart: " + utteranceId);
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                Log.i("onStop: " + utteranceId + "\n interrupted: " + interrupted);
            }
        });

        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTT_ID);
    }

    private void done() {
        finish();
    }
}