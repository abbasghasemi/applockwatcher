package ghasemi.abbas.applockwatcher.ui;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import ghasemi.abbas.applockwatcher.ApplicationLoader;
import ghasemi.abbas.applockwatcher.builder.AppStatus;
import ghasemi.abbas.applockwatcher.builder.FileLog;
import ghasemi.abbas.applockwatcher.builder.TinyData;


public class FrontCamera {
    private android.hardware.Camera camera;
    private String appId;

    public FrontCamera(SurfaceView surfaceView,String appId) {
        this.appId = appId;
        if (ApplicationLoader.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) && TinyData.getInstance().getBool("recordedImages")) {
            final int cameraId = getFrontCameraId();
            if (cameraId != -1) {
                SurfaceHolder holder = surfaceView.getHolder();
                holder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        camera = android.hardware.Camera.open(cameraId);
                        try {
                            camera.setPreviewDisplay(surfaceHolder);
                        } catch (IOException e) {
                            //
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                        Camera.Parameters cameraParameters = camera.getParameters();
                        camera.setParameters(cameraParameters);
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                        stopCamera();
                    }
                });
            }
        }
    }

    public void takePicture() {
        if (camera == null) {
            return;
        }
        try {
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270.0f);
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                    try {
                        AppStatus.open().addImgfoucault(appId, bm);
                    } catch (Exception e) {
                        //
                    }
                }
            });
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void stopCamera() {
        if (camera == null) {
            return;
        }
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private int getFrontCameraId() {
        int camId = -1;
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        CameraInfo ci = new CameraInfo();

        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
                camId = i;
            }
        }

        return camId;
    }

}