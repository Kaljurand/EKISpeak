package ee.eki.ekisynt;

import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.text.TextUtils;

import java.util.Locale;

public class EKISpeakService extends TextToSpeechService {

    public static final String DEFAULT_LANG = "est";
    public static final String DEFAULT_COUNTRY = "EST";
    public static final Locale DEFAULT_LOCALE = new Locale(DEFAULT_LANG, DEFAULT_COUNTRY);
    public static final String[] DEFAULT_LOCALE_TRIPLE = new String[]{DEFAULT_LANG, DEFAULT_COUNTRY, ""};

    private static final int SAMPLING_RATE_HZ = 48000;

    private static final int ORIGINAL_RATE_NORM = 100;
    private static final double MAPPED_RATE_NORM = 1.1d;
    private static final int ORIGINAL_RATE_LARGE_STEP = 50;
    private static final int ORIGINAL_RATE_SMALL_STEP = 20;

    private static final int ORIGINAL_HT_NORM = 100;
    private static final float MAPPED_HT_NORM = 1.0f;
    private static final int ORIGINAL_HT_LARGE_STEP = 50;
    private static final int ORIGINAL_HT_SMALL_STEP = 20;

    private volatile String[] mCurrentLanguage = null;

    private volatile boolean mStopRequested = false;

    private MyCallback jniCallback = new MyCallback();

    private boolean mIsInitialized = false;


    @Override
    public void onCreate() {
        Log.i("onCreate");
        super.onCreate();
        //onLoadLanguage(DEFAULT_LANG, DEFAULT_COUNTRY, "");

        if (TextUtils.isEmpty(PrefUtil.getInitFolder(getApplicationContext()))) {
            String initFolder = Util.getFolderW(this.getApplicationContext());
            PrefUtil.setInitFolder(getApplicationContext(), initFolder);
        }
    }

    @Override
    public void onDestroy() {
        Log.i("onDestroy");
        super.onDestroy();
        Util.shutDownHTS();
    }

    @Override
    protected String[] onGetLanguage() {
        Log.i("onGetLanguage: " + mCurrentLanguage);
        if (mCurrentLanguage == null) {
            return DEFAULT_LOCALE_TRIPLE;
        }
        return mCurrentLanguage;
    }

    @Override
    protected int onIsLanguageAvailable(String lang, String country, String variant) {
//        Log.i("onIsLanguageAvailable: " + lang + "/" + country + "/" + variant);
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

        if (!mIsInitialized) {
            Log.i("InitHTS");

            if (TextUtils.isEmpty(PrefUtil.getInitFolder(getApplicationContext()))) {
                String initFolder = Util.getFolderW(this.getApplicationContext());
                PrefUtil.setInitFolder(getApplicationContext(), initFolder);
            }
            String voice = PrefUtil.getHtsVoice(getApplicationContext());
            mIsInitialized = Util.initHTS(PrefUtil.getInitFolder(getApplicationContext()), "et.dct", "et3.dct", voice);
            Log.i("InitHTS ok : " + mIsInitialized);
        }

        mCurrentLanguage = new String[]{lang, loadCountry, ""};

        return isLanguageAvailable;
    }

    @Override
    protected void onStop() {
        Log.i("onStop");
        mStopRequested = true;
        jniCallback.setStop(mStopRequested);
        Util.stopAnyWork();
    }

    @Override
    protected synchronized void onSynthesizeText(SynthesisRequest request, SynthesisCallback callback) {
        if (request == null) return;
        String text = request.getText();

        Log.i("onSynthesizeText (old): " + text.length() + ", <" + text + ">");
        text = sanitize(text);
        Log.i("onSynthesizeText (new): " + text.length() + ", <" + text + ">");

        if (TextUtils.isEmpty(text)) {
            Log.i("Break! Text is empty");
            done(callback);
            return;
        }

        int load = onLoadLanguage(request.getLanguage(), request.getCountry(), request.getVariant());

        if (load == TextToSpeech.LANG_NOT_SUPPORTED) {
            callback.error();
            done(callback);
            return;
        }

        callback.start(SAMPLING_RATE_HZ, AudioFormat.ENCODING_PCM_16BIT, 1);

        // Someone called onStop, end the current synthesis and return.
        if (mStopRequested) {//teisiti vaja
            // return false;
        }

        int rate = request.getSpeechRate();
        double mappedRate = mapRate(rate);
        Log.i("RATE: " + rate + " MAPPED RATE: " + mappedRate);
        int ht = request.getPitch();
        float mappedHt = mapHT(ht);
        // log("HT: " + ht + " MAPPED HT: " + mappedHt);

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

        Util.synthTextHTS(text, text.length(), maxBufferSize, mappedRate, mappedHt, jniCallback);
        done(callback);
    }

    private void done(SynthesisCallback callback) {
        Log.i("Done");
        callback.done();
    }

    /**
     * This is a temporary hack to avoid libsynthts_et.so crashes on certain inputs, e.g.
     * - text that starts with a period "."
     * - text == "..."
     */
    private static String sanitize(String text) {
        text = text.trim();
        if (text.matches("^[\".!?-]+$")) {
            return "aaah joru kirjavahemärke aaah";
        }
        return text.replaceFirst("^\\.+", "").trim();
    }

    private double mapRate(int rate) {
        if (rate >= ORIGINAL_RATE_NORM) {
            return MAPPED_RATE_NORM +
                    (1.0d * rate - ORIGINAL_RATE_NORM) / ORIGINAL_RATE_LARGE_STEP * 0.1;
        } else {
            return MAPPED_RATE_NORM -
                    (1.0d * ORIGINAL_RATE_NORM - rate) / ORIGINAL_RATE_SMALL_STEP * 0.15;
        }
    }

    private float mapHT(int ht) {
        if (ht >= ORIGINAL_HT_NORM) {
            return MAPPED_HT_NORM +
                    (1.0f * ht - ORIGINAL_HT_NORM) / ORIGINAL_HT_LARGE_STEP;
        } else {
            return MAPPED_HT_NORM -
                    (1.0f * ORIGINAL_HT_NORM - ht) / ORIGINAL_HT_SMALL_STEP;
        }
    }
}