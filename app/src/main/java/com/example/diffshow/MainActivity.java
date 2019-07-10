package com.example.diffshow;

import android.app.Activity;
import android.graphics.Point;
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

public class MainActivity extends Activity {
    public  final String TAG = "READ_DIFF_JAVA";
    //TextView tv;
    short diffData[] = new short[32*18];
    CapacityView capacityView;
    int screenWidth;
    int screenHeight;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private   Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            capacityView.invalidate();
        }
    };
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

        timeStart = System.currentTimeMillis();
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

        myProcessDiff();
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

    long timeStart, timeNow, timeCnt = 0;
    boolean logging = false;
    FileOutputStream logger = null;

    void myProcessDiff() {
        timeCnt++;
        timeNow = System.currentTimeMillis();
        int timeDelta = (int)(timeNow - timeStart);
        //double fps = timeCnt / (timeDelta / 1000.0);
        //if (timeCnt % 10 == 0) Log.d("mydiffshow", "fps: " + fps);

        byte[] buffer = new byte[diffData.length * 2 + 4];
        buffer[0] = (byte) ((timeDelta & 0xff000000) >> 24);
        buffer[1] = (byte) ((timeDelta & 0x00ff0000) >> 16);
        buffer[2] = (byte) ((timeDelta & 0x0000ff00) >>  8);
        buffer[3] = (byte) ((timeDelta & 0x000000ff) >>  0);
        for (int i = 0; i < diffData.length; i++) {
            buffer[i * 2 + 4] = (byte) ((diffData[i] & 0xff00) >> 8);
            buffer[i * 2 + 5] = (byte) ((diffData[i] & 0x00ff) >> 0);
        }
        if (logging) {
            try {
                if (logger != null) logger.write(buffer);
            } catch (Exception e) {
                Log.d("mydiffshow", e.toString());
            }
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
