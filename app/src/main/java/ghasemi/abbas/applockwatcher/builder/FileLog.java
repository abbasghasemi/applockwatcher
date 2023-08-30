package ghasemi.abbas.applockwatcher.builder;

import android.text.TextUtils;
import android.util.Log;

public class FileLog {
    private static boolean LOG = false;
    private static String TAG = "appLock";

    public static void e(Exception e) {
        if (FileLog.LOG) {
            Log.e(TAG, e.toString());
        }
    }

    public static void d(Exception e) {
        if (FileLog.LOG) {
            Log.d(TAG, e.toString());
        }
    }

    public static void v(Exception e) {
        if (FileLog.LOG) {
            Log.v(TAG, e.toString());
        }
    }

    public static void i(Exception e) {
        if (FileLog.LOG) {
            Log.i(TAG, e.toString());
        }
    }

    public static void w(Exception e) {
        if (FileLog.LOG) {
            Log.w(TAG, e.toString());
        }
    }

    public static void print(CharSequence msg) {
        if (FileLog.LOG) {
            if (TextUtils.isEmpty(msg)) {
                msg = "Null Or Empty";
            }
            Log.e(TAG, msg.toString());
        }
    }
}
