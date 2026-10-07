package ghasemi.abbas.applockwatcher.builder;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        boolean normal = isEnabled(LauncherIcon.DEFAULT);
        boolean calculator = isEnabled(LauncherIcon.CALCULATOR);
        if (normal == calculator) {
            setIcon(TinyData.getInstance().getBool("calculatorLauncherIcon")
                    ? LauncherIcon.CALCULATOR : LauncherIcon.DEFAULT);
        }
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.context;
        int state = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.context;
        PackageManager pm = ctx.getPackageManager();
        pm.setComponentEnabledSetting(icon.getComponentName(ctx),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        for (LauncherIcon item : LauncherIcon.values()) {
            if (item != icon) {
                pm.setComponentEnabledSetting(item.getComponentName(ctx),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
        TinyData.getInstance().putBool("calculatorLauncherIcon", icon == LauncherIcon.CALCULATOR);
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.mipmap.ic_launcher, R.string.app_name),
        CALCULATOR("CalculatorIcon", R.mipmap.ic_calculator, R.string.app_calculator_name);

        public final String key;
        public final int icon;
        public final int title;

        LauncherIcon(String key, int icon, int title) {
            this.key = key;
            this.icon = icon;
            this.title = title;
        }

        public ComponentName getComponentName(Context ctx) {
            return new ComponentName(ctx.getPackageName(), ctx.getPackageName() + "." + key);
        }
    }
}
