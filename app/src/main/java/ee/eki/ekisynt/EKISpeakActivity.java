package ee.eki.ekisynt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Demo activity. This is a simple activity, for more serious testing install [TODO].
 */
public class EKISpeakActivity extends Activity {

    private static final String UTT_ID = "SIMPLE_UTTERANCE_ID";
    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 123;

    private TextToSpeech mTts;
    private TextView mTvMessages;
    private EditText mText;

    private View mBtnStart;
    private View mBtnStop;
    private View mProgress;
    private CheckBox mInFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        mProgress = findViewById(R.id.progress_layout);

        mText = (EditText) findViewById(R.id.etText);
        mTvMessages = (TextView) findViewById(R.id.tvMessages);

        mInFile = (CheckBox) findViewById(R.id.chb_in_file);

        mBtnStart = findViewById(R.id.bStart);
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInFile.isChecked()) {
//                    synthFile(mText.getText().toString());
                    checkPermissions();
                } else {
                    if (!mTts.isSpeaking()) {
                        String text = validateText(mText.getText().toString());
                        say(text);
                    } else {
                        Toast.makeText(EKISpeakActivity.this, "Please wait until speaking is finished", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mBtnStop = findViewById(R.id.bStop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        ((Button) findViewById(R.id.bSettings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
    private String validateText(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch < 383) { // < ž
                builder.append(ch);
            }
        }
        return builder.toString();
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
//        log("onResume");
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
        } else {
            mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    log("onUtteranceCompleted: " + utteranceId);
                }
            });
        }
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTT_ID);
        int maxTextSize = mTts.getMaxSpeechInputLength();
        if (text.length() > maxTextSize) {
            text = text.substring(0, maxTextSize);
        }
        mText.setText(text);
        log("Max text length: " + TextToSpeech.getMaxSpeechInputLength());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTT_ID);
        } else {
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    @SuppressLint("NewApi")
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

            HashMap<String, String> myHashRender = new HashMap<String, String>();

            myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTT_ID);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                    @Override
                    public void onDone(String utteranceId) {
                        mProgress.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.setVisibility(View.GONE);
                            }
                        });
                        log("onDone: " + utteranceId);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        mProgress.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.setVisibility(View.GONE);
                            }
                        });
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
                        mProgress.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgress.setVisibility(View.VISIBLE);
                            }
                        });

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

            int maxTextSize = mTts.getMaxSpeechInputLength();
            if (text.length() > maxTextSize) {
                text = text.substring(0, maxTextSize);
            }
            mText.setText(text);
            log("Max text length: " + TextToSpeech.getMaxSpeechInputLength());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTts.synthesizeToFile(text, null, out, UTT_ID);
            } else {
                mTts.synthesizeToFile(text, myHashRender, outFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                String text = mTvMessages.getText().toString();
                if (text.length() > 500 ) {
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
            synthFile(validateText(mText.getText().toString()));
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
                return;
            }
        }
    }
}