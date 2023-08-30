package ghasemi.abbas.applockwatcher.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.builder.BuildApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DocumentDrawable extends Drawable {
    private String name;
    private Drawable thumbDrawable;
    private TextPaint paint;

    public DocumentDrawable(Drawable drawable, String name) {
        thumbDrawable = drawable;
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.createFromAsset(ApplicationLoader.context.getAssets(),"fonts/Main-Light.ttf"));
        if (name.length() > 5) {
            this.name = name.substring(0, 4) + "..";
            paint.setTextSize(BuildApp.dp(13));
        } else {
            this.name = name;
            paint.setTextSize(BuildApp.dp(15));
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        android.graphics.Rect bounds = getBounds();
        int width = bounds.width();
        int height = bounds.height();
        canvas.save();
        canvas.translate(bounds.left, bounds.top);

        thumbDrawable.setBounds(BuildApp.dp(10), BuildApp.dp(10), width - BuildApp.dp(10), height - BuildApp.dp(10));
        thumbDrawable.draw(canvas);
        int w = (int) Math.ceil(paint.measureText(name));
        canvas.drawText(name, (width - w) / 2, height - BuildApp.dp(25), paint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
