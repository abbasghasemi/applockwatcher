package ghasemi.abbas.applockwatcher.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;
import ghasemi.abbas.applockwatcher.R;
import ghasemi.abbas.applockwatcher.builder.BuildApp;
import ghasemi.abbas.applockwatcher.components.TouchImageView;

public class ImageView extends BaseActivity {
    public String imagePath;

    private Bitmap bitmap;
    private boolean flip;

    private FloatingActionButton storeChange;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePath = getIntent().getStringExtra("imagePath");
        getWindow().setStatusBarColor(0xff2C2C2C);
        getWindow().setNavigationBarColor(0xff2C2C2C);
        BuildApp.windowInsetsControllerCompat(getWindow()).setAppearanceLightNavigationBars(false);
        setContentView(R.layout.image);
        bitmap = BitmapFactory.decodeFile(new File(imagePath).getAbsolutePath());
        TouchImageView touchImageView = findViewById(R.id.image);
        touchImageView.setImageBitmap(bitmap);

        findViewById(R.id.finishActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = rotateBitmap(bitmap, -90);
                touchImageView.setImageBitmap(bitmap);
                showBtn();
            }
        });

        findViewById(R.id.flip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = flipBitmap(bitmap, flip, !flip);
                flip = !flip;
                touchImageView.setImageBitmap(bitmap);
                showBtn();

            }
        });

        storeChange = findViewById(R.id.storeChange);
        storeChange.hide();
        storeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImage(bitmap, imagePath);
            }
        });
    }

    public Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flipBitmap(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flipBitmap(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap flipBitmap(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void storeImage(Bitmap bitmap, String path) {
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            hideBtn();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void showBtn() {
        if (!storeChange.isOrWillBeShown()) {
            storeChange.show();
            storeChange.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in));
        }
    }

    private void hideBtn() {
        if (storeChange.isOrWillBeShown()) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in);
            anim.setRepeatMode(Animation.REVERSE);
            storeChange.startAnimation(anim);
            storeChange.hide();
        }
    }
}