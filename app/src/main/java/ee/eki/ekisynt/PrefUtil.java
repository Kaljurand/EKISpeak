package ee.eki.ekisynt;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class PrefUtil {
    private static final String PREF = "ekispeak.";
    private static final String HTS_VOICE = PREF + "hts_voice";
    private static final String INIT_FOLDER = PREF + "init_folder";


    public static void setHtsVoice(Context context, String voice) {
        putString(context, HTS_VOICE, voice);
    }

    public static String getHtsVoice(Context context) {
        String voice = getString(context, HTS_VOICE);
        if (TextUtils.isEmpty(voice)) {
            voice = context.getString(R.string.voice_tnu);
            setHtsVoice(context, voice);
        }

        return voice;
    }

    public static void setInitFolder(Context context, String path) {
        putString(context, INIT_FOLDER, path);
    }

    public static String getInitFolder(Context context) {
        return getString(context, INIT_FOLDER);
    }

    //----------------------------------------------------------------------------------------

    private static void putString(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    private static String getString(Context context, String key) {
        return getPrefs(context).getString(key, "");
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
