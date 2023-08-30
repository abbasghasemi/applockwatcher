package ghasemi.abbas.applockwatcher.service;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;

public class LockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, LockWidgetProvider.class));
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.lock_widget);
        remoteViews.setImageViewResource(R.id.lock, R.drawable.lock_screen);
        for (int widgetId : allWidgetIds) {
            Intent intent = new Intent(context, LockWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
            intent.putExtra("First_Second", widgetId);
            remoteViews.setOnClickPendingIntent(R.id.lock, PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    public void onReceive(Context context, Intent intent) {
        DevicePolicyManager dp = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(context, LoginReceiver.class);
        try {
            if (intent.getIntExtra("First_Second", 0) != 0) {
                if (dp.isAdminActive(cn)) {
                    dp.lockNow();
                } else {
                    BuildApp.toast("دسترسی لازم است،ابتدا جلوگیری از حذف برنامه را در تنظیمات فعال کنید.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onReceive(context, intent);
    }

}

