package ghasemi.abbas.applockwatcher;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import ghasemi.abbas.applockwatcher.builder.LauncherIconController;
import ghasemi.abbas.applockwatcher.service.OnOffScreen;


public class ApplicationLoader extends Application {

    @SuppressLint("StaticFieldLeak")
    public static volatile Context context;

    @SuppressLint("StaticFieldLeak")
    public static volatile OnOffScreen screen;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        LauncherIconController.tryFixLauncherIconIfNeeded();

        if (screen == null) {
            screen = new OnOffScreen();
            OnOffScreen.register(this, screen);
        }
    }

}
