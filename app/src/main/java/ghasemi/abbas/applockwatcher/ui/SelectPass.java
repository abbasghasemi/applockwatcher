package ghasemi.abbas.applockwatcher.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.farasource.component.button.MaterialButton;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import ghasemi.abbas.applockwatcher.components.patternlockview.PatternLockView;
import ghasemi.abbas.applockwatcher.components.patternlockview.listener.PatternLockViewListener;
import ghasemi.abbas.applockwatcher.components.patternlockview.utils.ResourceUtils;
import ghasemi.abbas.applockwatcher.components.pinlockview.IndicatorDots;
import ghasemi.abbas.applockwatcher.components.pinlockview.PinLockListener;
import ghasemi.abbas.applockwatcher.components.pinlockview.PinLockView;
import ghasemi.abbas.applockwatcher.components.EditText;
import ghasemi.abbas.applockwatcher.components.TextView;

import java.util.List;


public class SelectPass extends BaseActivity {
    public static String TYPE_PASS = "typePass";
    public static String PIN = "pin";
    public static String PATTERN = "pattern";
    public static String CUSTOM = "custom";
    private LinearLayout pass;
    private int height, count;
    private boolean lastPass;
    private TextView msg;
    private String password;
    private PatternLockView mPatternLockView;
    private PinLockView mIndicatorDots;
    private EditText editText;

    @Override
    protected void onCreate() {
        setLayout(R.layout.change_pass);
        setTitle(BuildApp.getString(R.string.password_settings));
        msg = findViewById(R.id.msg);
        pass = findViewById(R.id.pass);

        Intent intent = getIntent();
        height = (int) getResources().getDimension(R.dimen.height_enter_password);
        if (intent.getStringExtra(TYPE_PASS).equals(PIN)) {
            msg.setText("گذرواژه خود را وارد کنید");
            final IndicatorDots indicatorDots = new IndicatorDots(this);
            indicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
            mIndicatorDots = new PinLockView(this);
            mIndicatorDots.setTextColor(0xFF38455C);
            mIndicatorDots.attachIndicatorDots(indicatorDots);
            mIndicatorDots.setPinLength(15);
            mIndicatorDots.setGoButton(true);
            mIndicatorDots.setDeleteButtonPressedColor(getResources().getColor(R.color.colorPrimary));
            mIndicatorDots.setPinLockListener(new PinLockListener() {
                @Override
                public void onComplete(String pin) {
                    if (lastPass) {
                        return;
                    }
                    BuildApp.toast("طول رمز به 15 عدد رسیده");
                }

                @Override
                public void onEmpty() {

                }

                @Override
                public void onPinChange(int pinLength, String intermediatePin) {
                    if (lastPass) {
                        if (password.equals(intermediatePin)) {
                            BuildApp.toast("پسورد با موفقیت ذخیره شد.");
                            TinyData.getInstance().putString("password", password);
                            TinyData.getInstance().putString(TYPE_PASS, PIN);
                            TinyData.getInstance().putBool("hasPassword", true);
                            setResult(2021, new Intent());
                            finish();
                        }
                    }
                }

                @Override
                public void onGo(String pin) {
                    mIndicatorDots.setGoButton(false);
                    mIndicatorDots.resetPinLockView();
                    lastPass = true;
                    password = pin;
                    msg.setText("لطفا مجددا پسورد خود را تکرار نمائید");
                }

            });
            indicatorDots.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            pass.addView(indicatorDots);
            mIndicatorDots.setLayoutParams(new LinearLayout.LayoutParams(height, height));
            pass.addView(mIndicatorDots);
        } else if (intent.getStringExtra(TYPE_PASS).equals(PATTERN)) {
            count = Integer.parseInt(TinyData.getInstance().getString("patternRowsCount", "3"));
            Spinner seekBar = findViewById(R.id.spinner);
            seekBar.setVisibility(View.VISIBLE);
            seekBar.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, new String[]{
                    "الگوی 3x3",
                    "الگوی 4x4",
                    "الگوی 5x5"
            }));
            seekBar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    count = position + 3;
                    pass.addView(getPatternLockView());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            pass.addView(getPatternLockView());
        } else {
            msg.setText("حروف کوچک و بزرگ اهمیت دارند \n گذرواژه خود را وارد کنید");
            View view = LayoutInflater.from(this).inflate(R.layout.custom_password, null);
            editText = view.findViewById(R.id.code);
            pass.addView(view);
            MaterialButton button = new MaterialButton(this);
            button.setText("تائید");
            button.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Main-Bold.ttf"));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str = editText.getText().toString();
                    if (str.trim().length() < 4) {
                        BuildApp.toast("گذرواژه نمی تواند کمتر از 4 حرف باشد");
                    } else if (str.trim().length() > 30) {
                        BuildApp.toast("گذرواژه نمی تواند بیش از 30 حرف باشد");
                    } else if (!lastPass) {
                        editText.setText("");
                        msg.setText("لطفا مجددا گذرواژه خود را تکرار نمائید");
                        lastPass = true;
                        password = str;
                    } else if(password.equals(str)){
                        BuildApp.toast("پسورد با موفقیت ذخیره شد");
                        TinyData.getInstance().putString("password", password);
                        TinyData.getInstance().putString(TYPE_PASS, CUSTOM);
                        TinyData.getInstance().putBool("hasPassword", true);
                        setResult(2021, new Intent());
                        finish();
                    }else {
                        editText.setText("");
                    }
                }
            });
            button.setPadding(BuildApp.dp(5), BuildApp.dp(5), BuildApp.dp(5), BuildApp.dp(5));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BuildApp.dp(50));
            params.leftMargin = BuildApp.dp(25);
            params.rightMargin = BuildApp.dp(25);
            params.topMargin = BuildApp.dp(5);
            pass.addView(button, params);
        }
    }

    private PatternLockView getPatternLockView() {
        pass.removeAllViews();
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
                if (pass.length() < 7) {
                    mPatternLockView.clearPattern();
                    msg.setText("الگوی وارد شده ساده است");
                } else if (lastPass) {
                    if (password.equals(pass.toString())) {
                        TinyData.getInstance().putString("patternRowsCount", "" + count);
                        BuildApp.toast("الگو با موفقیت ذخیره شد");
                        TinyData.getInstance().putString("password", password);
                        TinyData.getInstance().putString(TYPE_PASS, PATTERN);
                        TinyData.getInstance().putBool("hasPassword", true);
                        setResult(2021, new Intent());
                        finish();
                    } else {
                        mPatternLockView.clearPattern();
                        msg.setText("الگو مطابقت ندارد");
                    }
                } else {
                    findViewById(R.id.spinner).setVisibility(View.GONE);
                    mPatternLockView.clearPattern();
                    lastPass = true;
                    password = pass.toString();
                    msg.setText("لطفا مجددا الگوی خود را تکرار نمائید");
                }
            }

            @Override
            public void onCleared() {

            }
        });
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
        mPatternLockView.setDotCount(count);
        mPatternLockView.setAspectRatioEnabled(true);
        mPatternLockView.setEnableHapticFeedback(TinyData.getInstance().getBool("useVibrator", true));
        mPatternLockView.setAspectRatio(PatternLockView.AspectRatio.ASPECT_RATIO_HEIGHT_BIAS);
        mPatternLockView.setNormalStateColor(0xFF38455C);
        mPatternLockView.setCorrectStateColor(ResourceUtils.getColor(this, R.color.colorAccent));
        mPatternLockView.setWrongStateColor(ResourceUtils.getColor(this, R.color.pomegranate));
        mPatternLockView.setDotAnimationDuration(150);
        mPatternLockView.setPathEndAnimationDuration(100);
        return mPatternLockView;
    }

    @Override
    public void onBackPressed() {
        if (lastPass) {
            lastPass = false;
            if (getIntent().getStringExtra(TYPE_PASS).equals(PIN)) {
                mIndicatorDots.setGoButton(true);
                mIndicatorDots.resetPinLockView();
                msg.setText("گذرواژه خود را وارد کنید");
            } else if (getIntent().getStringExtra(TYPE_PASS).equals(PATTERN)) {
                findViewById(R.id.spinner).setVisibility(View.VISIBLE);
                mPatternLockView.clearPattern();
                msg.setText("الگوی خود را رسم کنید");
            } else {
                editText.setText("");
                msg.setText("گذرواژه خود را وارد کنید");
            }
            return;
        }
        setResult(2021, null);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        setResult(2021, null);
        finish();
    }

}
