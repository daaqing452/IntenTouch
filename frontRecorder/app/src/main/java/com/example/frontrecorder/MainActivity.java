package com.example.frontrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.PrintWriter;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    TextView textLogStatus;
    Button buttonStart;

    long timeStart, timeNow;
    boolean logging = false;
    PrintWriter logger;
    int label = 0;
    boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RadioGroup group = findViewById(R.id.radioGroupLabel);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioUnintentional:
                        label = 0;
                        break;
                    case R.id.radioIntentional:
                        label = 1;
                        break;
                }
                Log.d("myfrontrecorder", "change label " + label);
            }
        });

        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeRecordStatus();
            }
        });

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        textLogStatus = findViewById(R.id.textLogStatus);

        timeStart = System.currentTimeMillis();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                changeLogStatus();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    void changeLogStatus() {
        if (logging == false) {
            try {
                String fileName = (String) DateFormat.format("yyMMdd-kkmmss", new Date());
                fileName = "frontrecorder-" + fileName + ".txt";
                logger = new PrintWriter(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
            } catch (Exception e) {
                Log.d("myfrontrecorder", e.toString());
            }
            logging = true;
            Log.d("myfrontrecorder", "start logging");
            textLogStatus.setText("Logging...");
        } else {
            try {
                if (logger != null) logger.close();
            } catch (Exception e) {
                Log.d("myfrontrecorder", e.toString());
            }
            logger = null;
            logging = false;
            Log.d("myfrontrecorder", "stop logging");
            textLogStatus.setText("Not log...");
        }
    }

    void changeRecordStatus() {
        if (!logging || logger == null) return;
        timeNow = System.currentTimeMillis();
        int timeDelta = (int)(timeNow - timeStart);

        if (recording == false) {
            logger.write(timeDelta + " start " + label + "\n");
            buttonStart.setText("End");
            recording = true;
        } else {
            logger.write(timeDelta + " end\n");
            buttonStart.setText("Start");
            recording = false;
        }
    }
}
