package ghasemi.abbas.applockwatcher.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import ghasemi.abbas.applockwatcher.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class TextView extends AppCompatTextView {

    public TextView(Context context) {
        this(context, null);
    }

    public TextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TextView, defStyleAttr, 0);
        boolean b = a.getBoolean(R.styleable.TextView_bold, false);
        int color = a.getColor(R.styleable.TextView_color,0xFF414141);
        a.recycle();
        if (b) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Main-Bold.ttf"),Typeface.BOLD);
        } else {
            setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Main-Light.ttf"));
        }
        setTextColor(color);
    }


}
