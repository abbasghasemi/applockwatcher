package ghasemi.abbas.applockwatcher.ui;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
public class Help extends BaseActivity {

    @Override
    protected void onCreate() {
        setLayout(R.layout.help);
        setTitle(BuildApp.getString(R.string.app_usage_quide));
    }
}
