package ee.eki.heli.EKISpeak;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Respond to android.speech.tts.engine.CHECK_TTS_DATA
 */
public class CheckVoiceData extends Activity {

    private static final String[] SUPPORTED_LANGUAGES = {"est-EST"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> checkLanguages = Arrays.asList(SUPPORTED_LANGUAGES);

        ArrayList<String> available = new ArrayList<String>();
        ArrayList<String> unavailable = new ArrayList<String>();

        for (String lang : checkLanguages) {
            // This check is required because checkLanguages might contain
            // an arbitrary list of languages if the intent specified them
            // {@link #getCheckVoiceDataFor}.
            if (isLanguageSupported(lang)) {
                if (isDataInstalled(lang)) {
                    available.add(lang);
                } else {
                    unavailable.add(lang);
                }
            }
        }

        int result;
        if (!checkLanguages.isEmpty() && available.isEmpty()) {
            // No voices available at all.
            result = TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL;
        } else {
            // All voices are available.
            result = TextToSpeech.Engine.CHECK_VOICE_DATA_PASS;
        }

        // We now return the list of available and unavailable voices
        // as well as the return code.

        Intent returnData = new Intent();
        returnData.putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available);
        returnData.putStringArrayListExtra(
                TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES, unavailable);
        setResult(result, returnData);
        finish();
    }

    /**
     * Checks whether a given language is in the list of supported languages.
     */
    private boolean isLanguageSupported(String input) {
        for (String lang : SUPPORTED_LANGUAGES) {
            if (lang.equals(input)) {
                return true;
            }
        }

        return false;
    }

    // TODO: lang does not work
    private boolean isDataInstalled(String lang) {
        return TestInstall();
    }

    private boolean TestInstall() {
        final int[] id = {R.raw.eki, R.raw.format, R.raw.stems};
        final String[] name = {"eki.htsvoice", "format.gzxt", "stems.gzxt"};
        final int totalIds = 3;
        boolean ok = true;

        String dir;
        dir = getExternalFilesDir(null) + "";
        for (int i = 0; i < totalIds; i++) {
            final File file = new File(dir, name[i]);
            if (!file.exists()) {
                InputStream input = getResources().openRawResource(id[i]);
                try {
                    final OutputStream output = new FileOutputStream(file);
                    try {

                        final byte[] buffer = new byte[1024];
                        int read;

                        while ((read = input.read(buffer)) != -1)
                            output.write(buffer, 0, read);

                        output.flush();

                    } catch (Exception e) {
                        ok = false;
                        // e.printStackTrace();
                    } finally {
                        output.close();
                    }
                } catch (Exception e1) {
                    ok = false;
                    // e1.printStackTrace();
                } finally {
                    try {
                        input.close();
                    } catch (Exception e) {
                    }
                }
            }

        }
        return ok;
        // return dir + "/";
    }

}