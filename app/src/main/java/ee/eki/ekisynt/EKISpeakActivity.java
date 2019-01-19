package ee.eki.ekisynt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

/**
 * Demo activity. This is a simple activity, for more serious testing install [TODO].
 * TODO: split the demo text into multiple pieces and speak then in separate utterances.
 */
public class EKISpeakActivity extends Activity {

    private static final String UTT_ID = "SIMPLE_UTTERANCE_ID";
    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 123;

    private TextToSpeech mTts;
    private TextView mTvMessages;
    private EditText mText;

    private CheckBox mInFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mText = findViewById(R.id.etText);
        mTvMessages = findViewById(R.id.tvMessages);

        mInFile = findViewById(R.id.chb_in_file);

        findViewById(R.id.bStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInFile.isChecked()) {
                    checkPermissions();
                } else {
                    if (!mTts.isSpeaking()) {
                        say(mText.getText().toString());
                    } else {
                        Toast.makeText(EKISpeakActivity.this, "Please wait until speaking is finished", Toast.LENGTH_SHORT).show();
                    }
                }
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

//        mTvMessages.setText("");
//        log("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        shutdown();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO: print out available languages, and enable the buttons
                log(mTts.getLanguage().toString());
                String localeAsString = "et-EE";
                log(localeAsString + " is available? " + isLanguageAvailable(localeAsString)
                        + " status: " + status);
                setLanguage(new Locale(localeAsString));

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void say(String text) {
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
            public void onError(String utteranceId, int errorCode) {
                log("onError: " + utteranceId + "\n errorCode: " + errorCode);
            }

            @Override
            public void onBeginSynthesis(String utteranceId, int sampleRateInHz, int audioFormat, int channelCount) {
                super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount);
                log("onBeginSynthesis: " + utteranceId + " sampleRateInHz: " + sampleRateInHz
                        + " format: " + audioFormat + " channelCount: " + channelCount);
            }

            @Override
            public void onStart(String utteranceId) {
                log("onStart: " + utteranceId);
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                log("onStop: " + utteranceId + "\n interrupted: " + interrupted);
            }
        });
        mTts.speak(Util.truncateIfNeeded(text), TextToSpeech.QUEUE_FLUSH, null, UTT_ID);
    }

    // TODO: share code with say/1
    private void synthFile(String text) {
        try {
            String outFile = Environment.getExternalStorageDirectory() +
                    File.separator + "EKISpeak";
            new File(outFile).mkdir();
            outFile += File.separator + "audio.wav";
            File out = new File(outFile);
            if (out.exists()) {
                out.delete();
            }
            out.createNewFile();

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
                    mTvMessages.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvMessages.setText("");
                        }
                    });
                    log("onStart: " + utteranceId);
                }
            });
            mTts.synthesizeToFile(Util.truncateIfNeeded(text), null, out, UTT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Interrupts the current utterance and discards other utterances in the queue.
     *
     * @return {@code ERROR} or {@code SUCCESS}
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
                String text = mTvMessages.getText().toString();
                if (text.length() > 500) {
                    text = text.substring(text.length() - 500);
                }
                text += "\n" + str;
                mTvMessages.setText(text);
            }
        });
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_STORAGE);
        } else {
            synthFile(mText.getText().toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    synthFile(mText.getText().toString());
                }
            }
        }
    }
}