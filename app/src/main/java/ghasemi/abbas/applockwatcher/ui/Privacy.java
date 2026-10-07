package ghasemi.abbas.applockwatcher.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import ghasemi.abbas.applockwatcher.R;

public class Privacy extends BaseActivity {
    private boolean english;

    @Override
    protected void onCreate() {
        setLayout(R.layout.privacy);
        Locale deviceLocale = Build.VERSION.SDK_INT >= 24
                ? getResources().getConfiguration().getLocales().get(0)
                : getResources().getConfiguration().locale;
        english = "en".equals(deviceLocale.getLanguage());
        createItemActionBar(R.drawable.ic_language, view -> {
            english = !english;
            showLanguage();
        });
        showLanguage();
    }

    private void showLanguage() {
        Configuration configuration = new Configuration(getResources().getConfiguration());
        configuration.setLocale(english ? Locale.ENGLISH : new Locale("fa"));
        Resources strings = createConfigurationContext(configuration).getResources();
        findViewById(R.id.privacyRoot).setLayoutDirection(english
                ? View.LAYOUT_DIRECTION_LTR : View.LAYOUT_DIRECTION_RTL);
        setTitle(strings.getString(R.string.privacy_title));
        findViewById(R.id.more).setContentDescription(strings.getString(R.string.privacy_switch_language));
        setText(R.id.privacyIntro, R.string.privacy_intro, strings);
        setText(R.id.privacyLocalTitle, R.string.privacy_local_title, strings);
        setText(R.id.privacyLocalBody, R.string.privacy_local_body, strings);
        setText(R.id.privacyPermissionsTitle, R.string.privacy_permissions_title, strings);
        setText(R.id.privacyPermissionsBody, R.string.privacy_permissions_body, strings);
        setText(R.id.privacyLauncherTitle, R.string.privacy_launcher_title, strings);
        setText(R.id.privacyLauncherBody, R.string.privacy_launcher_body, strings);
        setText(R.id.privacyNetworkTitle, R.string.privacy_network_title, strings);
        setText(R.id.privacyNetworkBody, R.string.privacy_network_body, strings);
        setText(R.id.privacyControlTitle, R.string.privacy_control_title, strings);
        setText(R.id.privacyControlBody, R.string.privacy_control_body, strings);
    }

    private void setText(int viewId, int stringId, Resources strings) {
        ((TextView) findViewById(viewId)).setText(strings.getString(stringId));
    }
}
