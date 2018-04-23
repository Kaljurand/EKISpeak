package ee.eki.ekisynt;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {

    static {
        System.loadLibrary("synthts_et");
    }

    public static native boolean initHTS(String initFolder, String lexDct, String lexdDct, String htsvoice);

    public static native void synthTextHTS(String text, int textLength, int maxBufferSize, double rate, float ht, MyCallback jniCallback);

    public static native void shutDownHTS();

    public static native void stopAnyWork();

    public static String getFolderW(Context context) {

        final int[] id = {R.raw.eki_et_eva, R.raw.eki_et_tnu, R.raw.et, R.raw.et3};
        final String[] name = {"eki_et_eva.htsvoice", "eki_et_tnu.htsvoice", "et.dct", "et3.dct"};
        final int totalIds = name.length;

        String dir;
        dir = context.getExternalFilesDir(null) + "";
        for (int i = 0; i < totalIds; i++) {
            final File file = new File(dir, name[i]);
            if (!file.exists()) {
                InputStream input = context.getResources().openRawResource(id[i]);
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
}