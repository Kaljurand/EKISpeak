package ee.eki.ekisynt;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class TtsSettingsActivity extends Activity {

    private RadioGroup mVoicesContainer;
    private View mProgress;

    private String mInitFolder;

    private InitHtsAsyncTask mInitTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts_settings);

        mProgress = findViewById(R.id.progress_layout);

        mInitFolder = PrefUtil.getInitFolder(this);
        if (TextUtils.isEmpty(mInitFolder)) {
            mInitFolder = Util.getFolderW(this);
            PrefUtil.setInitFolder(this, mInitFolder);
        }

        mVoicesContainer = (RadioGroup) findViewById(R.id.hts_voices);
        String voice = PrefUtil.getHtsVoice(this);
        if (voice.equals(getString(R.string.voice_tnu))) {
            ((RadioButton)findViewById(R.id.hts_tnu)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.hts_eva)).setChecked(true);
        }

        mVoicesContainer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                String voice = null;
                switch (checkedId) {
                    case R.id.hts_eva:
                        voice = getString(R.string.voice_eva);
                        break;
                    case R.id.hts_tnu:
                        voice = getString(R.string.voice_tnu);
                        break;
                }

                PrefUtil.setHtsVoice(TtsSettingsActivity.this, voice);

                if (mInitTask != null) {
                    mInitTask.cancel(true);
                }
                mInitTask = new InitHtsAsyncTask();

                mInitTask.execute(voice, mInitFolder);
            }
        });
    }


    private class InitHtsAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            String voice = params[0];
            String initFolder = params[1];

            Util.shutDownHTS();

            Util.initHTS(initFolder, "et.dct", "et3.dct", voice);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mProgress.setVisibility(View.GONE);

            Toast.makeText(TtsSettingsActivity.this, "Valik tehtud!", Toast.LENGTH_SHORT).show();
        }
    };
}
