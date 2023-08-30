package ghasemi.abbas.applockwatcher.components;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.farasource.component.button.MaterialButton;

import androidx.appcompat.widget.AppCompatImageView;

import ghasemi.abbas.applockwatcher.R;

public class Permission {

    public Permission(Activity activity, View.OnClickListener click) {
        this(activity, click, null, null, View.NO_ID);
    }

    public Permission(Activity activity, View.OnClickListener click, String title, String content, int logo) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_permission);
        dialog.show();
        MaterialButton settings = dialog.findViewById(R.id.settings);
        settings.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Main-Bold.ttf"));
        settings.setOnClickListener(v -> {
            dialog.dismiss();
            click.onClick(v);
        });
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
        if (logo != View.NO_ID) {
            _logo.setImageResource(logo);
        }
    }
}