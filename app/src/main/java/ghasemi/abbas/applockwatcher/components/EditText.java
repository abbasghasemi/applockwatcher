package ghasemi.abbas.applockwatcher.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class EditText extends AppCompatEditText {

    {
        setTextColor(Color.BLACK);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Main-Light.ttf"));
    }


    public EditText(Context context) {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
