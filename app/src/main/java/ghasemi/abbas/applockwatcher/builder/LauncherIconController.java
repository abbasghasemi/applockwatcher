package ghasemi.abbas.applockwatcher.builder;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.ApplicationLoader;

public class LauncherIconController {

    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }
        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.context;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.context;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.mipmap.ic_launcher, R.string.app_name),
        CALCULATOR("CalculatorIcon", R.mipmap.ic_calculator, R.string.app_calculator_name);

        public final String key;
        public final int icon;
        public final int title;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), ctx.getPackageName() + "." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int icon, int title) {
            this.key = key;
            this.icon = icon;
            this.title = title;
        }
    }
}
