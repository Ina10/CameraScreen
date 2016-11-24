package com.example.irinalehmann.camerascreen;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    Camera camera;

    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;


    // Richten Activity so ein, dass es keine Kopfzeile hat und in Vollbild angezeigt wird.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        //Deklarieren den Surface
        //Bekommen seinen Holder
        //Bestimmen seinen Typ
        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Erzeugen das Objekt holderCallback
        //Durch das der Holder uns von dem Surface berichtet
        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);
    }


    // Bekommen den Zugang zu der Kamera (mithilfe der Methode "open") und übergeben die ID (falls wir mehrere Kameras haben (Front und Rück))
    // Theoretisch brauchen wir für unsere AG App immer nur die Rückkamera, also könnten wir hier auch einfach die Funktion "open" ohne Parameter aufrufen.
    // Die Funktion setPreviewSize ist weiter unten definiert, sie stellt die richtige Größe des "Surfes" ein
    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        setPreviewSize(FULL_SCREEN);
    }

    // Gibt die Kamera frei (mithilfe der Methode "release", so dass andere Apps sie benutzen können
    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    // Die Klasse HolderCallback implementiert das Interface  SurfaceHolder.Callback. (durch das der Holder uns von dem Surface berichtet)
    class HolderCallback implements SurfaceHolder.Callback {

        //Surface wird erzeugt.
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                // Übergeben der Kamera das Objekt "Holder"
                camera.setPreviewDisplay(holder);
                // Beginnen mit der Bild Übertragung
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Wenn das Format oder die Größe des Surfaces sich ändert
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // Dafür stoppen wir zuerst die Bild Übertragung
            camera.stopPreview();
            // Richten die Kamera (im Hinblick auf die Drehung) richtig ein
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                // Und starten erneut die Bild Übertragung
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Die Funktion benutzen wir nicht
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }


    // ?!
    void setPreviewSize(boolean fullScreen) {

        //Bekommen die Größe des Displays
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        //Finden die Größe der Preview Camera heraus
        Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        if (widthIsMax) {
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        if (!fullScreen) {
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        matrix.mapRect(rectPreview);

        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // Finden heraus wie stark der Display rotiert wurde von dem normalen Zustand (DefaultDisplay)
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        // Rück Kamera
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // Front Kamera
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }
}

