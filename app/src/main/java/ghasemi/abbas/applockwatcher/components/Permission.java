package ghasemi.abbas.applockwatcher.components;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.widget.AppCompatImageView;

import ghasemi.abbas.applockwatcher.R;

public class Permission {

    public Permission(Activity activity, View.OnClickListener click) {
        this(activity, click, null, null, View.NO_ID);
    }

    public Permission(Activity activity, View.OnClickListener click, String title, String content, int logo) {
        this(activity, click, null, title, content, logo);
    }

    public Permission(Activity activity, View.OnClickListener click, View.OnClickListener appInfoClick,
                      String title, String content, int logo) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_permission);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int margin = Math.round(32 * activity.getResources().getDisplayMetrics().density);
            window.setLayout(activity.getResources().getDisplayMetrics().widthPixels - margin,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
        MaterialButton settings = dialog.findViewById(R.id.settings);
        settings.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Main-Bold.ttf"));
        settings.setOnClickListener(v -> {
            dialog.dismiss();
            click.onClick(v);
        });
        MaterialButton appInfo = dialog.findViewById(R.id.app_info);
        if (appInfoClick != null) {
            appInfo.setVisibility(View.VISIBLE);
            appInfo.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Main-Bold.ttf"));
            settings.setText(R.string.accessibility_settings_step);
            appInfo.setOnClickListener(v -> {
                dialog.dismiss();
                appInfoClick.onClick(v);
            });
        }
        MaterialButton close = dialog.findViewById(R.id.close);
        close.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Main-Bold.ttf"));
        close.setOnClickListener(view -> dialog.dismiss());

        TextView _title = dialog.findViewById(R.id.title);
        TextView _content = dialog.findViewById(R.id.content);
        AppCompatImageView _logo = dialog.findViewById(R.id.logo);

        if (title != null) {
            _title.setText(title);
        }
        if (content != null) {
            _content.setText(content);
        }
        if (appInfoClick != null) {
            _content.setGravity(Gravity.RIGHT);
            _content.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
            _content.setLineSpacing(3 * activity.getResources().getDisplayMetrics().density, 1f);
        }
        if (logo != View.NO_ID) {
            _logo.setImageResource(logo);
        }
    }
}
