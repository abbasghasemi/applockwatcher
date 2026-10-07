package ghasemi.abbas.applockwatcher.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Map;
import java.util.Random;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.BuildConfig;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.FilesCenter;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.Permission;
import ghasemi.abbas.applockwatcher.components.Switch;
import ghasemi.abbas.applockwatcher.components.TextView;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    private Switch statusSwitch;
    private Disposable disposable;
    private boolean showAccessibilityGuideAfterAppInfo;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() == null) {
                    showDialogPassword();
                }
            }
    );

    private void showDialogPassword() {
        showDialogPassword(this, launcher, false);
    }
    public static void showDialogPassword(BaseActivity context, ActivityResultLauncher<Intent> launcher, boolean start) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(start);
        dialog.setCanceledOnTouchOutside(start);
        dialog.setContentView(R.layout.dialog_select_pass);
        final Intent intent = new Intent(context, SelectPass.class);
        dialog.findViewById(R.id.pin).setOnClickListener(view -> {
            intent.putExtra(SelectPass.TYPE_PASS, SelectPass.PIN);
            dialog.dismiss();
            context.disableLockActivity();
            launcher.launch(intent);
        });
        dialog.findViewById(R.id.pattern).setOnClickListener(view -> {
            intent.putExtra(SelectPass.TYPE_PASS, SelectPass.PATTERN);
            dialog.dismiss();
            context.disableLockActivity();
            launcher.launch(intent);
        });
        dialog.findViewById(R.id.custom).setOnClickListener(view -> {
            intent.putExtra(SelectPass.TYPE_PASS, SelectPass.CUSTOM);
            dialog.dismiss();
            context.disableLockActivity();
            launcher.launch(intent);
        });
        dialog.show();
    }

    @Override
    protected void onCreate() {
        setTitle(getResources().getString(R.string.app_name));
        setTitle("قفل برنامه ها");
        hideBackBtn();
        setLayout(R.layout.loader);
        LinearLayout content = findViewById(R.id.root);
        content.getChildAt(content.getChildCount() - 1).setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setView(new RecommendedAppsView(this));
        ((TextView) findViewById(R.id.text)).setText(getString(R.string.app_name)
                + " " + BuildConfig.VERSION_NAME);

        LinearLayout status = findViewById(R.id.status);
        statusSwitch = findViewById(R.id.statusSwitch);
        status.setOnClickListener(v -> statusSwitch.setChecked(!statusSwitch.isChecked()));

        boolean hasPassword = TinyData.getInstance().getBool("hasPassword");
        if (!hasPassword) {
            showDialogPassword();
        }

        createItemActionBar(R.drawable.ic_help, view -> startActivity(new Intent(MainActivity.this, Help.class)));

        CardView application = findViewById(R.id.application);
        application.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Apps.class));
        });
        CardView settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Setting.class)));


        CardView file = findViewById(R.id.file);
        file.setOnLongClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    new Permission(MainActivity.this, v12 -> {
                        disableLockActivity();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }, getString(R.string.file_access_permission_title),
                            getString(R.string.file_access_permission_guide), R.drawable.ic_round_storage_24);
                    return true;
                }
            } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                new Permission(MainActivity.this, v1 -> {
                    disableLockActivity();
                    requestPermissions(result -> {

                    }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }, "مجوز read/write storage", "برنامه برای خدمات قفل فایل ها به مجوز 'read/write external storage' نیاز دارد.", R.drawable.ic_round_storage_24);
                return true;
            }
            BuildApp.toast(getString(R.string.file_recovery_start));
            disposable = Single.fromCallable(() -> FilesCenter.findFileHidden(FilesCenter.root))
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        if (success) TinyData.getInstance().putBool("hiddenFilesFound", true);
                        BuildApp.toast(getString(success ? R.string.file_recovery_done : R.string.file_recovery_failed));
                    }, error -> BuildApp.toast(getString(R.string.file_recovery_failed)));
            return true;
        });
        file.setOnClickListener(v -> startActivityHidden("file", R.string.hidden_file_list, R.string.device));
        CardView audio = findViewById(R.id.audio);
        audio.setOnClickListener(v -> startActivityHidden("audio", R.string.hidden_audio_list, R.string.my_audio));
        CardView video = findViewById(R.id.video);
        video.setOnClickListener(v -> startActivityHidden("video", R.string.hidden_video_list, R.string.my_video));
        CardView image = findViewById(R.id.image);
        image.setOnClickListener(v -> startActivityHidden("image", R.string.hidden_image_list, R.string.my_image));

        startCheck();
    }

    private void startActivityHidden(String action, int titleId, int title2Id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new Permission(MainActivity.this, v -> {
                    disableLockActivity();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }, getString(R.string.file_access_permission_title),
                        getString(R.string.file_access_permission_guide), R.drawable.ic_round_storage_24);
                return;
            }
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            new Permission(MainActivity.this, v -> {
                disableLockActivity();
                requestPermissions(result -> {

                }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }, "مجوز read/write storage", "برنامه برای خدمات قفل فایل ها به مجوز 'read/write external storage' نیاز دارد.", R.drawable.ic_round_storage_24);
            return;
        }
        Intent intent = new Intent(MainActivity.this, Hidden.class);
        intent.putExtra("action", action);
        intent.putExtra("add", true);
        intent.putExtra("title", BuildApp.getString(titleId));
        intent.putExtra("title2", BuildApp.getString(title2Id));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (disposable != null) disposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        statusSwitch.setOnCheckedChangeListener(null);
        statusSwitch.setChecked(BuildApp.isAccessibilityServiceEnabled(MainActivity.this) &&
                TinyData.getInstance().getBool("appLockIsActive"));
        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                TinyData.getInstance().putBool("appLockIsActive", isChecked);
                if (isChecked && !BuildApp.isAccessibilityServiceEnabled(MainActivity.this)) {
                    permission();
                    statusSwitch.setOnCheckedChangeListener(null);
                    statusSwitch.setChecked(false);
                    statusSwitch.setOnCheckedChangeListener(this);
                }
            }
        });
        if (showAccessibilityGuideAfterAppInfo) {
            showAccessibilityGuideAfterAppInfo = false;
            if (!BuildApp.isAccessibilityServiceEnabled(this)) permission();
        }
    }

    void permission() {
        new Permission(this, view -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }, view -> {
            showAccessibilityGuideAfterAppInfo = true;
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
            }
        }, getString(R.string.accessibility_setup_title),
                getString(R.string.accessibility_setup_guide), R.drawable.ic_round_app_registration_24);
    }

    private void startCheck() {
        boolean b = new Random().nextInt(10) > 7;
        if (b && AppStatus.open().has()) {
            new Handler().postDelayed(() -> BuildApp.rateApp(MainActivity.this), 250);
        }
    }
}
