package ghasemi.abbas.applockwatcher.builder;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.Dimension;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.components.TextView;
import ghasemi.abbas.applockwatcher.service.AppAccessibilityService;
import ghasemi.abbas.applockwatcher.ui.StarApp;

import static androidx.annotation.Dimension.DP;

public class BuildApp {

    private static float density = -1;

    public static float getDensity() {
        if (density == -1) {
            if (ApplicationLoader.context == null) {
                density = 1;
            } else {
                density = ApplicationLoader.context.getResources().getDisplayMetrics().density;
            }
        }
        return density;
    }

    public static int dp(@Dimension(unit = DP) float dp) {
        if (dp == 0) {
            return 0;
        }
        return (int) Math.ceil(getDensity() * dp);
    }

    public static float dpf2(@Dimension(unit = DP) float dp) {
        if (dp == 0) {
            return 0;
        }
        return getDensity() * dp;
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(context.getPackageName()) && enabledServiceInfo.name.equals(AppAccessibilityService.class.getName()))
                return true;
        }

        return false;
    }

    public static void toast(String msg) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
           Toast.makeText(ApplicationLoader.context, msg, Toast.LENGTH_SHORT).show();
       } else {
           Toast toast = new Toast(ApplicationLoader.context);
           TextView textView = new TextView(ApplicationLoader.context);
           textView.setTextColor(0xffffffff);
           textView.setBackgroundDrawable(ApplicationLoader.context.getResources().getDrawable(R.drawable.back_toast));
           textView.setPadding(dp(10), dp(10), dp(10), dp(10));
           toast.setView(textView);
           textView.setText(msg);
           toast.show();
       }
    }

    public static WindowInsetsControllerCompat windowInsetsControllerCompat(Window window){
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        return windowInsetsController;
    }

    public static void rateApp(AppCompatActivity activity) {
       new StarApp(activity).show();
    }

    public static void addHistory(String code) {
        if (!TinyData.getInstance().getBool("storedLogins")) {
            return;
        }
        String[] part = code.split("==");
        AppStatus.open().addHistory(part[0],part[1]);
    }

    public static String getString(@StringRes int id) {
        return ApplicationLoader.context.getResources().getString(id);
    }
}