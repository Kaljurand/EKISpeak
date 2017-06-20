package ee.eki.heli.EKISpeak;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

/**
 * Demo activity. This is a simple activity, for more serious testing install [TODO].
 */
public class EKISpeakActivity extends Activity {

    private static final String UTT_COMPLETED_FEEDBACK = "UTT_COMPLETED_FEEDBACK";

    private TextToSpeech mTts;
    private TextView mTvMessages;
    private EditText mText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mText = findViewById(R.id.etText);
        mTvMessages = findViewById(R.id.tvMessages);

        findViewById(R.id.bStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                say(mText.getText().toString());
            }
        });

        findViewById(R.id.bStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        findViewById(R.id.bSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        shutdown();
        mTvMessages.setText("");
    }


    @Override
    public void onResume() {
        super.onResume();
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO: print out available languages, and enable the buttons
                log(mTts.getLanguage().toString());
                String localeAsString = "et-EE";
                log(localeAsString + " is available? " + isLanguageAvailable(localeAsString));
                setLanguage(new Locale(localeAsString));

            }
        });
    }

    @SuppressLint("NewApi")
    private void say(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onDone(String utteranceId) {
                    log("onDone: " + utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    log("onError: " + utteranceId);
                }

                @Override
                public void onStart(String utteranceId) {
                    log("onStart: " + utteranceId);
                }
            });
        } else {
            mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    log("onUtteranceCompleted: " + utteranceId);
                }
            });
        }
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTT_COMPLETED_FEEDBACK);
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }


    /**
     * Interrupts the current utterance and discards other utterances in the queue.
     *
     * @return {@ERROR} or {@SUCCESS}
     */
    public int stop() {
        return mTts.stop();
    }


    public boolean isLanguageAvailable(String localeAsStr) {
        return mTts.isLanguageAvailable(new Locale(localeAsStr)) >= 0;
    }


    public void setLanguage(Locale locale) {
        mTts.setLanguage(locale);
    }


    public void shutdown() {
        if (mTts != null) {
            mTts.shutdown();
        }
    }

    private void log(final String str) {
        mTvMessages.post(new Runnable() {
            @Override
            public void run() {
                mTvMessages.setText(mTvMessages.getText() + "\n" + str);
            }
        });
    }
}