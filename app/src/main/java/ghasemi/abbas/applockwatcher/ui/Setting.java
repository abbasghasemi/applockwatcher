package ghasemi.abbas.applockwatcher.ui;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.farasource.component.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import ghasemi.abbas.applockwatcher.BuildConfig;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.LauncherIconController;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.EditText;
import ghasemi.abbas.applockwatcher.components.Permission;
import ghasemi.abbas.applockwatcher.components.Switch;
import ghasemi.abbas.applockwatcher.components.TextView;
import ghasemi.abbas.applockwatcher.service.LoginReceiver;

public class Setting extends BaseActivity {

    private Switch save, history, deleteSwitch;
    private DevicePolicyManager dp;
    private ComponentName cn;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            }
    );

    private final ActivityResultLauncher<Intent> launchActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                history.setChecked(TinyData.getInstance().getBool("storedLogins"));
                save.setChecked(TinyData.getInstance().getBool("recordedImages"));
            }
    );

    private final ActivityResultLauncher<Intent> adminDevice = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    deleteSwitch.setChecked(false);
                }
            }
    );

    private final ActivityResultLauncher<Intent> imageChooser = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    boolean error = true;
                    try {
                        Bitmap bitmap;
                        if (result.getData() != null) {
                            if (result.getData().getData() == null) {
                                bitmap = (Bitmap) result.getData().getExtras().get("data");
                            } else {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getData().getData());
                            }
                        } else {
                            bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/wallpaper.jpg");
                        }
                        if (bitmap == null) {
                            return;
                        }
                        BuildApp.toast("پس زمینه با موفقیت تنطیم شد.");
                        TinyData.getInstance().putString("backgroundImagePath", storeImage(bitmap));
                        TinyData.getInstance().putLong("position_image_uri", 0);
                        error = false;
                        new Handler().post(() -> {
                            Intent intent = new Intent(Setting.this, ImageView.class);
                            intent.putExtra("imagePath", TinyData.getInstance().getString("backgroundImagePath"));
                            startActivity(intent);
                        });
                    } catch (IOException e) {
                        //
                    } finally {
                        if (error) BuildApp.toast("انتخاب تصویر با مشکل روبرو شد.");
                    }
                }
            }
    );

    @Override
    protected void onCreate() {
        dp = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        cn = new ComponentName(this, LoginReceiver.class);
        setLayout(R.layout.setting);
        setTitle(BuildApp.getString(R.string.settings));
        findViewById(R.id.changePass).setOnClickListener(view -> MainActivity.showDialogPassword(Setting.this, launcher, true));

        findViewById(R.id.forgetPass).setOnClickListener(view -> {
            final BottomSheetDialog dialog = new BottomSheetDialog(Setting.this, R.style.BottomSheetDialogTheme);
            dialog.setContentView(LayoutInflater.from(Setting.this).inflate(R.layout.forget_pass, null));
            dialog.setCancelable(false);
            dialog.show();
            final TextView help = dialog.findViewById(R.id.help);
            final EditText question = dialog.findViewById(R.id.question);
            question.setText(TinyData.getInstance().getString("userQuestion"));
            final EditText answer = dialog.findViewById(R.id.answer);
            answer.setText(TinyData.getInstance().getString("answerQuestion"));
            MaterialButton save = dialog.findViewById(R.id.save);
            save.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
            save.setOnClickListener(v -> {
                if (question.getText().toString().trim().isEmpty()) {
                    help.setTextColor(Color.RED);
                    help.setText("متن سوال نمی تواند خالی باشد");
                } else if (answer.getText().toString().trim().isEmpty()) {
                    help.setTextColor(Color.RED);
                    help.setText("پاسخ نمی تواند خالی باشد");
                } else {
                    TinyData.getInstance().putString("userQuestion", question.getText().toString().trim());
                    TinyData.getInstance().putString("answerQuestion", answer.getText().toString().trim());
                    dialog.dismiss();
                }
            });
            MaterialButton close = dialog.findViewById(R.id.close);
            close.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
            close.setOnClickListener(v -> dialog.dismiss());
        });
        final RadioButton exitApp = findViewById(R.id.exitApp);
        exitApp.setChecked(TinyData.getInstance().getBool("lockedApplicationAfterExit"));
        final RadioButton offScreen = findViewById(R.id.Off);
        offScreen.setChecked(!TinyData.getInstance().getBool("lockedApplicationAfterExit"));

        findViewById(R.id.exit_app).setOnClickListener(view -> {
            if (!TinyData.getInstance().getBool("lockedApplicationAfterExit")) {
                AppStatus.open().update(null, true);
                exitApp.setChecked(true);
                offScreen.setChecked(false);
                TinyData.getInstance().putBool("lockedApplicationAfterExit", true);
            }
        });
        findViewById(R.id.off).setOnClickListener(view -> {
            if (TinyData.getInstance().getBool("lockedApplicationAfterExit")) {
                AppStatus.open().update(null, true);
                exitApp.setChecked(false);
                offScreen.setChecked(true);
                TinyData.getInstance().putBool("lockedApplicationAfterExit", false);
            }
        });

        findViewById(R.id.rate).setOnClickListener(view -> {
            boolean huc = TinyData.getInstance().getBool("hasUserCommented");
            TinyData.getInstance().putBool("hasUserCommented", false);
            BuildApp.rateApp(Setting.this);
            TinyData.getInstance().putBool("hasUserCommented", huc);
        });

        findViewById(R.id.otherAppa).setOnClickListener(view -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(BuildConfig.FLAVOR.equals("cafebazaar") ? "https://cafebazaar.ir/developer/654337025886" : "https://myket.ir/developer/dev-74572"));
                startActivity(intent);
            } catch (Exception e) {
                //
            }
        });

        findViewById(R.id.wallpaper).setOnClickListener(view -> {
            final BottomSheetDialog dialog = new BottomSheetDialog(Setting.this, R.style.BottomSheetDialogTheme);
            dialog.setContentView(R.layout.dialog_gallery);
            dialog.show();
            dialog.findViewById(R.id.gallery).setOnClickListener(v -> {
                dialog.dismiss();
                try {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                    disableLockActivity();
                    imageChooser.launch(chooserIntent);
                } catch (Exception e) {
                    BuildApp.toast("Camera Exception:" + e);
                }
            });
            dialog.findViewById(R.id.camera).setOnClickListener(v -> {
                dialog.dismiss();
                takePicture(true);
            });
            dialog.findViewById(R.id.custom).setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(Setting.this, Gallery.class));
            });
            dialog.findViewById(R.id.reset).setOnClickListener(v -> {
                dialog.dismiss();
                if (TinyData.getInstance().getLong("position_image_uri") != 0 ||
                !TextUtils.isEmpty(TinyData.getInstance().getString("backgroundImagePath"))) {
                    TinyData.getInstance().putString("backgroundImagePath", "");
                    TinyData.getInstance().putLong("position_image_uri", 0);
                    BuildApp.toast("پس زمینه به پیش فرض تغییر یافت.");
                }
            });
        });

        final LinearLayout History = findViewById(R.id.History);
        History.setOnClickListener(view -> {
            disableLockActivity();
            launchActivity.launch(new Intent(Setting.this, History.class));
        });
        final LinearLayout Save = findViewById(R.id.Save);
        Save.setOnClickListener(view -> {
            disableLockActivity();
            launchActivity.launch(new Intent(Setting.this, Foucault.class));
        });

        history = findViewById(R.id.history);
        history.setChecked(TinyData.getInstance().getBool("storedLogins"));
        history.setOnCheckedChangeListener((compoundButton, b) -> TinyData.getInstance().putBool("storedLogins", b));
        save = findViewById(R.id.save);
        save.setChecked(TinyData.getInstance().getBool("recordedImages"));
        save.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && ContextCompat.checkSelfPermission(Setting.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                new Permission(Setting.this, v -> {
                    disableLockActivity();
                    requestPermissions(result -> save.setChecked(ContextCompat.checkSelfPermission(Setting.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED), Manifest.permission.CAMERA);
                }, "مجوز camera", "برای ظبط تصویر هنگام اشتباه وارد شدن گذرواژه، نیاز است دسترسی به دوربین به برنامه اهدا گردد.", R.drawable.ic_round_camera_24);
                save.setChecked(false);
            } else {
                TinyData.getInstance().putBool("recordedImages", b);
            }
        });

        LinearLayout changeIcon = findViewById(R.id.changeIcon);
        final Switch changeIconSwitch = findViewById(R.id.changeIconSwitch);
        changeIconSwitch.setChecked(LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.CALCULATOR));
        changeIcon.setOnClickListener(v -> changeIconSwitch.setChecked(!changeIconSwitch.isChecked()));
        changeIconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> LauncherIconController.setIcon(isChecked ? LauncherIconController.LauncherIcon.CALCULATOR : LauncherIconController.LauncherIcon.DEFAULT));

        LinearLayout vibrator = findViewById(R.id.vibrator);
        final Switch vibratorSwitch = findViewById(R.id.vibratorSwitch);
        vibratorSwitch.setChecked(TinyData.getInstance().getBool("useVibrator", true));
        vibrator.setOnClickListener(v -> vibratorSwitch.setChecked(!vibratorSwitch.isChecked()));
        vibratorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> TinyData.getInstance().putBool("useVibrator", isChecked));

        LinearLayout visible = findViewById(R.id.visible);
        final Switch visibleSwitch = findViewById(R.id.visibleSwitch);
        visibleSwitch.setChecked(TinyData.getInstance().getBool("canSeenPattern"));
        visible.setOnClickListener(v -> visibleSwitch.setChecked(!visibleSwitch.isChecked()));
        visibleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> TinyData.getInstance().putBool("canSeenPattern", isChecked));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints() && keyguardManager != null && keyguardManager.isKeyguardSecure()) {
                LinearLayout finger = findViewById(R.id.finger);
                finger.setVisibility(View.VISIBLE);
                findViewById(R.id.view).setVisibility(View.VISIBLE);
                final Switch fingerSwitch = findViewById(R.id.fingerSwitch);
                fingerSwitch.setChecked(TinyData.getInstance().getBool("userFingerprint", true));
                finger.setOnClickListener(v -> fingerSwitch.setChecked(!fingerSwitch.isChecked()));
                fingerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> TinyData.getInstance().putBool("userFingerprint", isChecked));
            }
        }

        LinearLayout delete = findViewById(R.id.delete);
        deleteSwitch = findViewById(R.id.deleteSwitch);
        deleteSwitch.setChecked(dp.isAdminActive(cn));
        delete.setOnClickListener(v -> deleteSwitch.setChecked(!deleteSwitch.isChecked()));
        deleteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (dp.isAdminActive(cn) && !isChecked) {
                dp.removeActiveAdmin(cn);
            } else if (!dp.isAdminActive(cn) && isChecked) {
                buttonView.setChecked(false);
                String t = "شما با اهدای دسترسی های زیر به AppLock موافقت می نمایید:\n" + "- غیرفعال شدن حذف اپلیکیشن\n" + "- کنترل قفل صفحه";
                new Permission(Setting.this, v -> {
                    disableLockActivity();
                    try {
                        Intent intent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
                        intent.putExtra("android.app.extra.DEVICE_ADMIN", cn);
                        intent.putExtra("android.app.extra.ADD_EXPLANATION", t);
                        disableLockActivity();
                        adminDevice.launch(intent);
                    } catch (Exception e) {
                        //
                    }
                }, "مجوز device admin", t, R.drawable.ic_round_security_24);
            }
        });

    }

    private void takePicture(boolean permission) {
        try {
            if (ContextCompat.checkSelfPermission(Setting.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (permission) {
                    new Permission(Setting.this, v -> {
                        disableLockActivity();
                        requestPermissions(result -> takePicture(false), Manifest.permission.CAMERA);
                    }, "مجوز camera", "برای ظبط تصویر به عنوان پسزمینه، نیاز است دسترسی به دوربین به برنامه اهدا گردد.", R.drawable.ic_round_camera_24);

                }
                return;
            }
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String filename = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/wallpaper.jpg";
            Uri fileProvider = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", new File(filename));
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileProvider);
            Intent chooserIntent = Intent.createChooser(cameraIntent, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{cameraIntent});
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            disableLockActivity();
            imageChooser.launch(chooserIntent);
        } catch (Exception e) {
            BuildApp.toast("Camera Exception:" + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dp != null && cn != null) {
            if (deleteSwitch != null) {
                deleteSwitch.setChecked(dp.isAdminActive(cn));
            }
        }
    }

    private String storeImage(Bitmap bitmap) {
        File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            FileOutputStream outputStream = new FileOutputStream(file.getPath() + "/wallpaper.jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            //
        }
        return file.getPath() + "/wallpaper.jpg";
    }

}