package ghasemi.abbas.applockwatcher.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.FileLog;
import ghasemi.abbas.applockwatcher.builder.TinyData;
import androidx.core.content.ContextCompat;


public class FrontCamera {
    private static final ExecutorService IMAGE_WRITER = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final String appId;
    private final SurfaceHolder holder;
    private final SurfaceHolder.Callback surfaceCallback;
    private final int cameraId;
    private Camera camera;
    private boolean capturePending;
    private boolean capturing;
    private boolean released;
    private final Runnable shutter = () -> {
        if (camera == null || released) return;
        try {
            camera.takePicture(null, null, (data, capturedCamera) -> {
                stopCamera();
                if (data != null && data.length > 0) IMAGE_WRITER.execute(() -> savePicture(data));
            });
        } catch (RuntimeException e) {
            FileLog.e(e);
            stopCamera();
        }
    };

    public FrontCamera(SurfaceView surfaceView, String appId) {
        this.appId = appId;
        holder = surfaceView.getHolder();
        cameraId = ApplicationLoader.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                && TinyData.getInstance().getBool("recordedImages")
                && ContextCompat.checkSelfPermission(ApplicationLoader.context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED ? getFrontCameraId() : -1;
        surfaceCallback = new SurfaceHolder.Callback() {
            @Override public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (capturePending) capture();
            }
            @Override public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) { }
            @Override public void surfaceDestroyed(SurfaceHolder surfaceHolder) { stopCamera(); }
        };
        holder.addCallback(surfaceCallback);
    }

    public void takePicture() {
        if (released || cameraId < 0 || capturing) return;
        if (ContextCompat.checkSelfPermission(ApplicationLoader.context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) return;
        capturePending = true;
        capture();
    }

    private void capture() {
        if (released || cameraId < 0 || capturing || !capturePending || !holder.getSurface().isValid()) return;
        capturing = true;
        try {
            camera = Camera.open(cameraId);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            main.postDelayed(shutter, 250);
        } catch (Exception e) {
            FileLog.e(e);
            stopCamera();
        }
    }

    public void stopCamera() {
        main.removeCallbacks(shutter);
        capturePending = false;
        capturing = false;
        if (camera != null) {
            try { camera.stopPreview(); } catch (RuntimeException ignored) { }
            try { camera.release(); } catch (RuntimeException ignored) { }
            camera = null;
        }
    }

    public void release() {
        released = true;
        holder.removeCallback(surfaceCallback);
        stopCamera();
    }

    private void savePicture(byte[] data) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
            int sample = 1;
            while (Math.max(bounds.outWidth, bounds.outHeight) / sample > 1280) sample *= 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (source == null) return;
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            Bitmap image = source;
            if (info.orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(info.orientation);
                image = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                if (image != source) source.recycle();
            }
            try { AppStatus.open().addImgfoucault(appId, image); }
            finally { image.recycle(); }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private int getFrontCameraId() {
        int camId = -1;
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            CameraInfo ci = new CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, ci);
                if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    camId = i;
                    break;
                }
            }
        } catch (RuntimeException e) { FileLog.e(e); }
        return camId;
    }

}
