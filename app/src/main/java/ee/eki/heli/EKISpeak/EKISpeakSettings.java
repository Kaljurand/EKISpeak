package ee.eki.heli.EKISpeak;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

/*
 * This class is referenced via a meta data section in the manifest.
 * A settings screen is optional, and if a given engine has no settings,
 * there is no need to implement such a class.
 */
public class EKISpeakSettings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }
}
