package ghasemi.abbas.applockwatcher.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.FileLog;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.ui.Launcher;

public class AppAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (ApplicationLoader.context == null) {
            ApplicationLoader.context = getApplicationContext();
        }
        if (!TinyData.getInstance().getBool("appLockIsActive")) return;
        CharSequence packageId = event.getPackageName();
        FileLog.print(packageId);
        if (packageId != null && !packageId.equals(getPackageName())) {
            String lastPkgOnline = TinyData.getInstance().getString("lastPkgOnline");
            if (!packageId.toString().equals(lastPkgOnline)) {
                TinyData.getInstance().putString("lastPkgOnline", packageId.toString());
                Boolean lock = AppStatus.open().info(packageId.toString());
                if (lock == Boolean.TRUE) {
                    FileLog.print("App lock started.");
                    BuildApp.addHistory(packageId + "==0");
                    Intent launchLock = new Intent(getApplicationContext(), Launcher.class);
                    launchLock.putExtra("pkg", packageId);
                    launchLock.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        getApplicationContext().startActivity(launchLock);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

//    @Override
//    protected void onServiceConnected() {
//        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        info.notificationTimeout = 100;
//        setServiceInfo(info);
//    }
}
