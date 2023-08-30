package ghasemi.abbas.applockwatcher.service;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class LoginReceiver extends DeviceAdminReceiver {

    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "در صورت غیرفعال کردن،برنامه قابلیت حذف را خواهد داشت.\n آیا مایل به حذف این دسترسی هستید؟";
    }

}