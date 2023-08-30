package ghasemi.abbas.applockwatcher.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.farasource.component.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.DateHelper;
import ghasemi.abbas.applockwatcher.builder.FileLog;
import ghasemi.abbas.applockwatcher.builder.LauncherIconController;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.EditText;
import ghasemi.abbas.applockwatcher.components.TextView;
import ghasemi.abbas.applockwatcher.components.patternlockview.PatternLockView;
import ghasemi.abbas.applockwatcher.components.patternlockview.listener.PatternLockViewListener;
import ghasemi.abbas.applockwatcher.components.patternlockview.utils.ResourceUtils;
import ghasemi.abbas.applockwatcher.components.pinlockview.IndicatorDots;
import ghasemi.abbas.applockwatcher.components.pinlockview.PinLockListener;
import ghasemi.abbas.applockwatcher.components.pinlockview.PinLockView;


public class Launcher extends AppCompatActivity {

    private Vibrator vibrator;
    private PatternLockView mPatternLockView;
    private PinLockView pinLockView;
    private IndicatorDots indicatorDots;
    private FrontCamera frontCamera;
    private boolean isActive = true;
    private boolean forOtherApps = true;
    private BiometricPrompt biometricPrompt = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (TextUtils.isEmpty(TinyData.getInstance().getString("backgroundImagePath"))) {
                BuildApp.windowInsetsControllerCompat(getWindow()).setAppearanceLightStatusBars(true);
            } else {
                BuildApp.windowInsetsControllerCompat(getWindow()).setAppearanceLightNavigationBars(false);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().setStatusBarColor(Color.GRAY);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        String pkg = getIntent().getStringExtra("pkg");
        if (pkg == null) {
            forOtherApps = false;
            pkg = getPackageId();
        }
        int height = (int) getResources().getDimension(R.dimen.height_enter_password);

        if (TinyData.getInstance().getBool("hasPassword")) {
            BuildApp.addHistory(getPackageId() + "==0");
            setContentView(R.layout.lock_app);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                findViewById(R.id.statusBar).setVisibility(View.GONE);
            } else {
                @SuppressLint("InternalInsetResource") int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    ViewGroup.LayoutParams layoutParams = findViewById(R.id.statusBar).getLayoutParams();
                    layoutParams.height = getResources().getDimensionPixelSize(resourceId);
                    findViewById(R.id.statusBar).setLayoutParams(layoutParams);
                }
            }
            View root = findViewById(R.id.root);
            AppCompatImageView icon = findViewById(R.id.icon);
            TextView name = findViewById(R.id.name);
            PackageManager packageManager = getPackageManager();
            if (forOtherApps) {
                try {
                    Drawable drawable = packageManager.getApplicationIcon(pkg);
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkg, 0);
                    CharSequence n = packageManager.getApplicationLabel(applicationInfo);
                    name.setText(n);
                    icon.setImageDrawable(drawable);
                } catch (PackageManager.NameNotFoundException e) {
                    FileLog.e(e);
                }
            } else {
                boolean isIconDef = LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.DEFAULT);
                icon.setImageResource(isIconDef ? LauncherIconController.LauncherIcon.DEFAULT.icon : LauncherIconController.LauncherIcon.CALCULATOR.icon);
                name.setText(isIconDef ? LauncherIconController.LauncherIcon.DEFAULT.title : LauncherIconController.LauncherIcon.CALCULATOR.title);
            }
            LinearLayout linearLayout = findViewById(R.id.pass);

            if (TinyData.getInstance().getString(SelectPass.TYPE_PASS).equals(SelectPass.PATTERN)) {
                mPatternLockView = new PatternLockView(this);
                mPatternLockView.setLayoutParams(new LinearLayout.LayoutParams(height, height));
                mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
                    @Override
                    public void onStarted() {

                    }

                    @Override
                    public void onProgress(List<PatternLockView.Dot> progressPattern) {

                    }

                    @Override
                    public void onComplete(List<PatternLockView.Dot> pattern) {
                        StringBuilder pass = new StringBuilder();
                        for (PatternLockView.Dot dot : pattern) {
                            pass.append(dot.getId()).append(dot.getColumn());
                        }
                        if (pass.toString().equals(TinyData.getInstance().getString("password"))) {
                            BuildApp.addHistory(getPackageId() + "==3");
                            go();
                        } else {
                            startVibrator();
                            mPatternLockView.clearPattern();
                        }
                    }

                    @Override
                    public void onCleared() {

                    }
                });
                linearLayout.addView(mPatternLockView);
                mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                mPatternLockView.setDotCount(Integer.parseInt(TinyData.getInstance().getString("patternRowsCount", "3")));
                mPatternLockView.setInStealthMode(TinyData.getInstance().getBool("canSeenPattern"));
                mPatternLockView.setAspectRatioEnabled(true);
                mPatternLockView.setEnableHapticFeedback(TinyData.getInstance().getBool("useVibrator", true));
                mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
                mPatternLockView.setNormalStateColor(0xFF38455C);
                mPatternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.colorAccent));
                mPatternLockView.setWrongStateColor(ResourceUtils.getColor(this, R.color.pomegranate));
                mPatternLockView.setDotAnimationDuration(150);
                mPatternLockView.setPathEndAnimationDuration(100);
            } else if (TinyData.getInstance().getString(SelectPass.TYPE_PASS).equals(SelectPass.PIN)) {
                indicatorDots = new IndicatorDots(this);
                indicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
                pinLockView = new PinLockView(this);
                pinLockView.setPinLength(15);
                pinLockView.setTextColor(0xFF38455C);
                pinLockView.attachIndicatorDots(indicatorDots);
                pinLockView.setDeleteButtonPressedColor(getResources().getColor(R.color.colorPrimary));
                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (!pin.equals(TinyData.getInstance().getString("password"))) {
                            startVibrator();
                            pinLockView.resetPinLockView();
                        }
                    }

                    @Override
                    public void onEmpty() {

                    }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) {
                        if (intermediatePin.equals(TinyData.getInstance().getString("password"))) {
                            BuildApp.addHistory(getPackageId() + "==3");
                            go();
                        }
                    }

                    @Override
                    public void onGo(String pin) {

                    }
                });
                indicatorDots.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(indicatorDots);
                pinLockView.setLayoutParams(new LinearLayout.LayoutParams(height, height));
                linearLayout.addView(pinLockView);
            } else {
                View view = LayoutInflater.from(this).inflate(R.layout.custom_password, null);
                final EditText editText = view.findViewById(R.id.code);
                if (!TextUtils.isEmpty(TinyData.getInstance().getString("backgroundImagePath"))) {
                    editText.setTextColor(getResources().getColor(R.color.white));
                    TextInputLayout inputCode = view.findViewById(R.id.inputCode);
                    inputCode.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    inputCode.setBoxStrokeColorStateList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                }
                linearLayout.setGravity(Gravity.CENTER | Gravity.TOP);
                linearLayout.setPadding(0, BuildApp.dp(50), 0, 0);
                linearLayout.addView(view);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().equals(TinyData.getInstance().getString("password"))) {
                            BuildApp.addHistory(getPackageId() + "==3");
                            go();
                        } else if (s.length() >= 30) {
                            startVibrator();
                            editText.setText("");
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }

            if (TinyData.getInstance().getBool("userFingerprint", true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Executor executor = ContextCompat.getMainExecutor(this);
                biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {

                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        if (isActive) {
                            BuildApp.addHistory(getPackageId() + "==3");
                            go();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        if (isActive) startVibrator();
                    }
                });
                new Handler().post(() -> biometricPrompt.authenticate(new BiometricPrompt.PromptInfo.Builder().setTitle("ورود با اثرانگشت").setNegativeButtonText("Use account password").build()));
            }
            DateHelper.YearMonthDate helper = DateHelper.getCurrentJalaliDate();
            TextView date = findViewById(R.id.date);
            date.setText(String.format("%s - %s %s %s", helper.getWeekText(), helper.getDate(), helper.getMonthText(), helper.getYear()));
            String url = TinyData.getInstance().getString("backgroundImagePath");
            if (!TextUtils.isEmpty(url)) {
                File imgFile = new File(url);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ImageView imageView = findViewById(R.id.image);
                    imageView.setImageBitmap(myBitmap);
                    TextView textView = findViewById(R.id.name);
                    textView.setTextColor(0xffffffff);
                    date.setTextColor(0xffffffff);
                    findViewById(R.id.view).setBackgroundColor(0x50000000);
                    if (mPatternLockView != null) {
                        mPatternLockView.setNormalStateColor(0xffffffff);
                    } else if (pinLockView != null) {
                        pinLockView.setTextColor(0xffffffff);
                        indicatorDots.setColor(0xffffffff);
                    }
                } else {
                    TinyData.getInstance().putString("backgroundImagePath", "");
                    TinyData.getInstance().putLong("position_image_uri", 0);
                }
            }
            if (!TinyData.getInstance().getString("userQuestion").isEmpty()) {
                MaterialButton button = new MaterialButton(this);
                button.setText("فراموشی گذرواژه");
                button.setBackgroundTintList(ColorStateList.valueOf(0xffF44336));
                button.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
                button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                button.setOnClickListener(v -> {
                    final BottomSheetDialog dialog = new BottomSheetDialog(Launcher.this, R.style.BottomSheetDialogTheme);
                    dialog.setContentView(LayoutInflater.from(Launcher.this).inflate(R.layout.forget_password, null));
                    dialog.show();
                    TextView question = dialog.findViewById(R.id.question);
                    question.setText(TinyData.getInstance().getString("userQuestion"));
                    final EditText answer = dialog.findViewById(R.id.answer);
                    final TextView help1 = dialog.findViewById(R.id.help);
                    MaterialButton check = dialog.findViewById(R.id.check);
                    check.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
                    check.setOnClickListener(v1 -> {
                        if (TinyData.getInstance().getString("answerQuestion").equals(answer.getText().toString().trim())) {
                            dialog.dismiss();
                            BuildApp.addHistory(getPackageId() + "==3");
                            go();
                        } else {
                            startVibrator();
                            answer.setText("");
                        }
                    });
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, BuildApp.dp(30));
                params.leftMargin = BuildApp.dp(25);
                params.rightMargin = BuildApp.dp(25);
                button.setPadding(BuildApp.dp(10), 0, BuildApp.dp(10), 0);
                linearLayout.addView(button, params);
            }
            root.startAnimation(AnimationUtils.loadAnimation(ApplicationLoader.context, R.anim.in));
        } else {
            go();
        }

    }

    @Override
    protected void onStop() {
        isActive = false;
        //        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //        activityManager.moveTaskToFront(Process.myPid(), 0);
        super.onStop();
    }

    private void startVibrator() {
        frontCamera.takePicture();
        BuildApp.addHistory(getPackageId() + "==2");
        if (TinyData.getInstance().getBool("useVibrator", true)) {
            if (vibrator == null) {
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            }
            vibrator.vibrate(180);
        }
    }

    void go() {
        if (forOtherApps) {
            AppStatus.open().update(getPackageId(), TinyData.getInstance().getBool("lockedApplicationAfterExit"));
            finishAffinity();
        } else {
            if (getIntent().getBooleanExtra("FLAG_ACTIVITY_LOADER", true)) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, R.anim.out2);
            }
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (forOtherApps) {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (Exception e) {
                //
            }
        }
        finishAffinity();
    }

    @Override
    protected void onResume() {
        if (frontCamera == null) {
            frontCamera = new FrontCamera((SurfaceView) findViewById(R.id.surfaceView), getPackageId());
        }
        isActive = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (forOtherApps) {
            super.onPause();
            finish();
        } else {
            isActive = false;
            super.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (frontCamera != null) {
            frontCamera.stopCamera();
        }
        if (biometricPrompt != null) {
            biometricPrompt.cancelAuthentication();
            biometricPrompt = null;
        }
        isActive = false;
        super.onDestroy();
    }

    public String getPackageId() {
        if (forOtherApps) {
            return getIntent().getStringExtra("pkg");
        }
        return getPackageName();
    }

}
