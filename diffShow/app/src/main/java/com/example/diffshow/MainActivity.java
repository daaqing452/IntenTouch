package com.example.diffshow;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    public final String TAG = "READ_DIFF_JAVA";
    //TextView tv;
    short diffData[] = new short[32*18];
    CapacityView capacityView;
    int screenWidth;
    int screenHeight;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            capacityView.invalidate();
        }
    };

    InertialSensor inertialSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        capacityView = findViewById(R.id.capacityView);
        // Example of a call to a native method

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        capacityView.screenHeight = screenHeight;
        capacityView.screenWidth = screenWidth;
        capacityView.diffData = diffData;
        capacityView.father = this;

        readDiffStart();

        inertialSensor = new InertialSensor(this);
        timeStart = System.currentTimeMillis();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                record();
            }
        }, 500, 10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        readDiffStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            Log.d(TAG, "------  Down Point Id:" + event.getPointerId(event.getActionIndex()));
        }
        for (int i = 0; i < event.getPointerCount(); ++i) {
            Log.d(TAG, "------  Id:" + event.getPointerId(i) + "  x:" + event.getX(i) + "  y:" + event.getY(i));
        }
        return super.onTouchEvent(event);
    }

    public void processDiff(short[] data){
        diffData = capacityView.diffData = data;
        myHandler.obtainMessage(0).sendToTarget();
        //Log.d(TAG,"processDiff touchNum :"+data.touchNum);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void readDiffStart();
    public native void readDiffStop(); // Java_com_example_diffshow_MainActivity_readDiffStop



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                changeLogStatus();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    long timeStart, timeNow;
    boolean logging = false;
    FileOutputStream logger = null;

    int bufferAdd(byte[] buffer, int bi, int d) {
        buffer[bi++] = (byte) ((d & 0xff000000) >> 24);
        buffer[bi++] = (byte) ((d & 0x00ff0000) >> 16);
        buffer[bi++] = (byte) ((d & 0x0000ff00) >>  8);
        buffer[bi++] = (byte) ((d & 0x000000ff) >>  0);
        return bi;
    }

    void record() {
        if (!logging || logger == null) return;
        timeNow = System.currentTimeMillis();
        int timeDelta = (int)(timeNow - timeStart);

        byte[] buffer = new byte[4 + 3*4 + 3*4 + diffData.length*2];
        int bi = 0;
        bi = bufferAdd(buffer, bi, timeDelta);
        for (int i = 0; i < 3; i++) {
            int d = Float.floatToIntBits(inertialSensor.dataLinearAccelerometer[i]);
            bi = bufferAdd(buffer, bi, d);
            Log.d("mydiffshow", buffer[bi-4] + " " + buffer[bi-3] + " " + buffer[bi-2] + " " + buffer[bi-1]);
            Log.d("mydiffshow", d + " " + inertialSensor.dataLinearAccelerometer[i]);
        }
        for (int i = 0; i < 3; i++) {
            int d = Float.floatToIntBits(inertialSensor.dataGyroscope[i]);
            bi = bufferAdd(buffer, bi, d);
        }
        for (int i = 0; i < diffData.length; i++) {
            buffer[bi++] = (byte) ((diffData[i] & 0xff00) >> 8);
            buffer[bi++] = (byte) ((diffData[i] & 0x00ff) >> 0);
        }
        try {
            logger.write(buffer);
        } catch (Exception e) {
            Log.d("mydiffshow", e.toString());
        }
    }

    void changeLogStatus() {
        if (logging == false) {
            try {
                String fileName = (String) DateFormat.format("yyMMdd-kkmmss", new Date());
                fileName = "diffshow-" + fileName + ".d";
                logger = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
            } catch (Exception e) {
                Log.d("mydiffshow", e.toString());
            }
            logging = true;
            Log.d("mydiffshow", "start logging");
        } else {
            try {
                if (logger != null) logger.close();
            } catch (Exception e) {
                Log.d("mydiffshow", e.toString());
            }
            logger = null;
            logging = false;
            Log.d("mydiffshow", "stop logging");
        }
    }

}
