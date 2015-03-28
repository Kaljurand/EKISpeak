package ee.eki.heli.EKISpeak;

import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class EKISpeakService extends TextToSpeechService {

    public static final String DEFAULT_LANG = "est";
    public static final String DEFAULT_COUNTRY = "EST";
    public static final Locale DEFAULT_LOCALE = new Locale(DEFAULT_LANG, DEFAULT_COUNTRY);
    public static final String[] DEFAULT_LOCALE_TRIPLE = new String[]{DEFAULT_LANG, DEFAULT_COUNTRY, ""};

    private static final int SAMPLING_RATE_HZ = 48000;

    private volatile String[] mCurrentLanguage = null;

    private volatile boolean mStopRequested = false;

    private MyCallback jniCallback = new MyCallback();


    @Override
    public void onCreate() {
        Log.i("onCreate");
        super.onCreate();
        //onLoadLanguage(DEFAULT_LANG, DEFAULT_COUNTRY, "");
    }

    @Override
    public void onDestroy() {
        Log.i("onDestroy");
        super.onDestroy();
        ShutdownHTS();
    }

    @Override
    protected String[] onGetLanguage() {
        Log.i("onGetLanguage");
        if (mCurrentLanguage == null) {
            return DEFAULT_LOCALE_TRIPLE;
        }
        return mCurrentLanguage;
    }

    @Override
    protected int onIsLanguageAvailable(String lang, String country, String variant) {
        Log.i("onIsLanguageAvailable: " + lang + "/" + country + "/" + variant);
        if (DEFAULT_LANG.equals(lang)) {
            if (DEFAULT_COUNTRY.equals(country)) {
                return TextToSpeech.LANG_COUNTRY_AVAILABLE;
            }
            return TextToSpeech.LANG_AVAILABLE;
        }
        return TextToSpeech.LANG_NOT_SUPPORTED;
    }

    /*
     * Note that this method is synchronized, as is onSynthesizeText because
     * onLoadLanguage can be called from multiple threads (while
     * onSynthesizeText is always called from a single thread only).
     */
    @Override
    protected synchronized int onLoadLanguage(String lang, String country, String variant) {
        Log.i("onLoadLanguage: " + lang + "/" + country + "/" + variant);
        final int isLanguageAvailable = onIsLanguageAvailable(lang, country, variant);

        if (isLanguageAvailable == TextToSpeech.LANG_NOT_SUPPORTED) {
            return isLanguageAvailable;
        }

        String loadCountry = country;
        if (isLanguageAvailable == TextToSpeech.LANG_AVAILABLE) {
            loadCountry = DEFAULT_COUNTRY;
        }

        // If we've already loaded the requested language, we can return early.
        if (mCurrentLanguage != null) {
            if (mCurrentLanguage[0].equals(lang) && mCurrentLanguage[1].equals(country)) {
                return isLanguageAvailable;
            }
        }

        Log.i("InitHTS");

        InitHTS(getFolderW());

        Log.i("InitHTS:ok");

        mCurrentLanguage = new String[]{lang, loadCountry, ""};

        return isLanguageAvailable;
    }

    /**
     * Copy files from raw to external, only if not there yet.
     */
    protected String getFolderW() {

        final int[] id = {R.raw.eki, R.raw.format, R.raw.stems};
        final String[] name = {"eki.htsvoice", "format.gzxt", "stems.gzxt"};
        final int totalIds = 3;

        String dir;
        dir = getExternalFilesDir(null) + "";
        for (int i = 0; i < totalIds; i++) {
            final File file = new File(dir, name[i]);
            if (!file.exists()) {
                InputStream input = getResources().openRawResource(id[i]);
                try {
                    final OutputStream output = new FileOutputStream(file);
                    try {
                        try {
                            final byte[] buffer = new byte[1024];
                            int read;

                            while ((read = input.read(buffer)) != -1)
                                output.write(buffer, 0, read);

                            output.flush();
                        } finally {
                            output.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                    }
                }
            }

        }
        return dir + "/";
    }

    @Override
    protected void onStop() {
        Log.i("onStop");
        mStopRequested = true;
        jniCallback.setStop(mStopRequested);
    }

    @Override
    protected synchronized void onSynthesizeText(SynthesisRequest request, SynthesisCallback callback) {
        // TODO: map digits to words, etc.
        String text = replaceNumbers(request.getText()) + ".";

        Log.i("onSynthesizeText: " + text);
        int load = onLoadLanguage(request.getLanguage(), request.getCountry(), request.getVariant());

        if (load == TextToSpeech.LANG_NOT_SUPPORTED) {
            callback.error();
            return;
        }

        callback.start(SAMPLING_RATE_HZ, AudioFormat.ENCODING_PCM_16BIT, 1);

        // Someone called onStop, end the current synthesis and return.
        if (mStopRequested) {//teisiti vaja
            // return false;
        }

        Log.i("Start");
        // Get the maximum allowed size of data we can send across in
        // audioAvailable.
        //final int
        int maxBufferSize = callback.getMaxBufferSize();

        if (maxBufferSize > 16000) {
            maxBufferSize = 16000;
        }
        jniCallback.setCallback(callback);
        jniCallback.setStop(false);

        SynthHTS(text, maxBufferSize, jniCallback);

        Log.i("Done");
        callback.done();
    }

    private static String replaceNumbers(String str) {
        return str.replaceAll("[0-9]+", " number ");
    }

    public static native int InitHTS(String voiseroot);

    public static native int SynthHTS(String lab, int maxbufflen, MyCallback callback);

    public static native void ShutdownHTS();

    static {
        System.loadLibrary("ekihts");
    }

}