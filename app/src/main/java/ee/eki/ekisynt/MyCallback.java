package ee.eki.ekisynt;

import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;

public class MyCallback {
	private SynthesisCallback callback;
	private boolean mStopRequested = false;
	private String mPath;

	public MyCallback() {
	}
	
	public MyCallback(SynthesisCallback callback) {
		this.callback = callback;
		
	}

	public void setCallback(SynthesisCallback callback) {
		this.callback = callback;
		mStopRequested = false;
	}
	
	public void setStop(boolean state) {
		this.mStopRequested = state;
	}

	//return 0 to stop, 1 to continue 
	public int audioAvailable(byte[] buff, int off, int len) {
		if (mStopRequested) {
			Util.stopAnyWork();
			return 0;
		} else {
			if (callback.audioAvailable(buff, off, len) == TextToSpeech.ERROR) {
				mStopRequested = true;
				Util.stopAnyWork();
				return 0;
			}
			return 1;
		}
		
	}
}
