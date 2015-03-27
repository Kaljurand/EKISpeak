package ee.eki.heli.EKISpeak;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;

import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.util.Log;

/**
 * It exercises all aspects of the Text to speech engine API
 * {@link android.speech.tts.TextToSpeechService}.
 */
public class EKISpeakService extends TextToSpeechService {
	private static final String TAG = "EKITtsService";

	private static final int SAMPLING_RATE_HZ = 48000;

	private volatile String[] mCurrentLanguage = null;
	private volatile boolean mStopRequested = false;
	private SharedPreferences mSharedPrefs = null;

	private MyCallback jniCallback = new MyCallback() ;
	
	@Override
	public void onCreate() {
		Log.w("ss", "onCreate: ");
		super.onCreate();
		mSharedPrefs = getSharedPreferences(
				GeneralSettingsFragment.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

		onLoadLanguage("est", "EST", "");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w("ss", "onDestroy:");
		ShutdownHTS();
	}

	@Override
	protected String[] onGetLanguage() {
		// Note that mCurrentLanguage is volatile because this can be called
		// from
		// multiple threads.
		return mCurrentLanguage;
	}

	@Override
	protected int onIsLanguageAvailable(String lang, String country,
			String variant) {
		if ("est".equals(lang)) {
			if ("EST".equals(country)) {
				return TextToSpeech.LANG_COUNTRY_AVAILABLE;
			}
			return TextToSpeech.LANG_AVAILABLE;
		}
		return TextToSpeech.LANG_AVAILABLE;
		// return TextToSpeech.LANG_NOT_SUPPORTED;
	}

	/*
	 * Note that this method is synchronized, as is onSynthesizeText because
	 * onLoadLanguage can be called from multiple threads (while
	 * onSynthesizeText is always called from a single thread only).
	 */
	@Override
	protected synchronized int onLoadLanguage(String lang, String country,
			String variant) {
		Log.w("ss", "onLoadLanguage: " + lang + " " + country + " " + variant);
		final int isLanguageAvailable = onIsLanguageAvailable(lang, country,
				variant);

		if (isLanguageAvailable == TextToSpeech.LANG_NOT_SUPPORTED) {
			return isLanguageAvailable;
		}

		String loadCountry = country;
		if (isLanguageAvailable == TextToSpeech.LANG_AVAILABLE) {
			loadCountry = "EST";
		}

		// If we've already loaded the requested language, we can return early.
		if (mCurrentLanguage != null) {
			if (mCurrentLanguage[0].equals(lang)
					&& mCurrentLanguage[1].equals(country)) {
				return isLanguageAvailable;
			}
		}

		Log.w("ss", "InitHTS:");
		InitHTS(getFolderW());

		Log.w("ss", "InitHTS:ok");

		mCurrentLanguage = new String[] { lang, loadCountry, "" };

		return isLanguageAvailable;
	}

	protected String getFolderW() {

		final int[] id = { R.raw.eki, R.raw.format, R.raw.stems };
		final String[] name = { "eki.htsvoice", "format.gzxt", "stems.gzxt" };
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
		mStopRequested = true;
		jniCallback.setStop(mStopRequested);
	}

	@Override
	protected synchronized void onSynthesizeText(SynthesisRequest request,
			SynthesisCallback callback) {
		Log.w("ss", "onSynthesizeText: " + request.getText());
		int load = onLoadLanguage(request.getLanguage(), request.getCountry(),
				request.getVariant());

		if (load == TextToSpeech.LANG_NOT_SUPPORTED) {
			callback.error();
			return;
		}

		callback.start(SAMPLING_RATE_HZ, AudioFormat.ENCODING_PCM_16BIT, 1);
		
		

		// Someone called onStop, end the current synthesis and return.
		if (mStopRequested) {//teisiti vaja
			// return false;
		}
		Log.w("ss", "Hakka seletama: ");
		// Get the maximum allowed size of data we can send across in 
		// audioAvailable.
		//final int
		int maxBufferSize = callback.getMaxBufferSize();
		
		if(maxBufferSize > 16000){
			maxBufferSize = 16000;
		}
		jniCallback.setCallback(callback);
		jniCallback.setStop(false);
		
		SynthHTS(request.getText()+".", maxBufferSize, jniCallback);

		Log.w("ss", "Seletatud: ");
		callback.done();

	}

	public static native int InitHTS(String voiseroot);

	public static native int SynthHTS(String lab, int maxbufflen,
			MyCallback callback);

	public static native void ShutdownHTS();

	/** Load jni .so on initialization */
	static {
		System.loadLibrary("ekihts");
	}

}
