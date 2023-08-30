package ghasemi.abbas.applockwatcher.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;

public class Lock extends View {

    private boolean isLock;
    private boolean isAnimate;
    private int angle = 145;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (angle == 145 || angle == 170) {
                isAnimate = false;
            }
            invalidate();
        }
    };
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();

    public Lock(Context context) {
        this(context, null);
    }

    public Lock(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Lock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        rectF.set(BuildApp.dp(7), BuildApp.dp(2), w - BuildApp.dp(7), h -  BuildApp.dp(5));
        paint.setStrokeWidth(BuildApp.dp(2.5f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(isLock ? 0xff2FDF84 : 0xffBDC6CC);
        if (isAnimate) {
            if (isLock) {
                angle++;
            } else {
                angle--;
            }
            canvas.drawArc(rectF, 180, angle, false, paint);
            handler.postDelayed(runnable, 3);
        } else {
            canvas.drawArc(rectF, 180, isLock ? 180 : 145, false, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        rectF.set(BuildApp.dp(3), h / 2 - BuildApp.dp(2), w - BuildApp.dp(3), h);
        canvas.drawRoundRect(rectF, BuildApp.dp(5), BuildApp.dp(5), paint);
        paint.setColor(Color.WHITE);
        if (isLock) {
            canvas.drawCircle(w / 2, h / 2 + h / 4, BuildApp.dp(3), paint);
        } else {
            rectF.set(w / 2 - BuildApp.dp(3.5f), h / 2 + h / 4 - BuildApp.dp(2), w / 2 + BuildApp.dp(3.5f), h / 2 + h / 4 + BuildApp.dp(2));
            canvas.drawRoundRect(rectF, BuildApp.dp(1.5f), BuildApp.dp(1.5f), paint);
        }
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean lock) {
        setLock(lock, false);
    }

    public void setLock(boolean lock, boolean animate) {
        isLock = lock;
        isAnimate = animate;
        angle = isLock ? 145 : 170;
        invalidate();
        if (animate) {
            startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.lock));
        }
    }
}
