package ghasemi.abbas.applockwatcher.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.TinyData;

public class OnOffScreen extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ApplicationLoader.context == null) ApplicationLoader.context = context;
        if (!TinyData.getInstance().getBool("lockedApplicationAfterExit")) {
            AppStatus.open().update(null, true);
//            if (ApplicationLoader.screen != null) {
//                unregister(context, ApplicationLoader.screen);
//                ApplicationLoader.screen = null;
//            }
        }
    }

    public static void register(Context context, OnOffScreen onOffScreen) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onOffScreen, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(onOffScreen, intentFilter);
        }
    }

    public static void unregister(Context context, OnOffScreen onOffScreen) {
        context.unregisterReceiver(onOffScreen);
    }

}
