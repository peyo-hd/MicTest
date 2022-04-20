package com.peyo.mictest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends Activity {
    private static final String TAG = "MicTest";
    private TextView textView;
    private ArrayList<String> dumpLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
        requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, 0);

        textView = findViewById(R.id.dump);
        dumpLines = new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7","8","9","0",
                "1","2","3","4","5","6","7","8","9","0"));
    }

    private void addDump(String newLine) {
        String dumpText = dumpLines.remove(0);
        dumpText += "\n";

        dumpLines.add(newLine);
        for (String line : dumpLines) {
            dumpText += line + "\n";
        }
        final String finalDumpText = dumpText;
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              textView.setText(finalDumpText);
                          }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        mEchoing = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEchoing = false;
    }

    private static final int SAMPLE_RATE = 16000;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_STEREO;
    private static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_STEREO;

    AudioRecord mRecorder;
    AudioTrack mTrack;
    byte mBuffer[];
    boolean mEchoing;

    @SuppressLint("MissingPermission")
    public void onEchoClicked(View v) {
        Log.i(TAG, "onEchoClicked()");

        final AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        AudioDeviceInfo[] devices = manager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        Log.i(TAG, "number of input devices : " + devices.length);
        Log.i(TAG, "isMicrophoneMute() ? " + manager.isMicrophoneMute());

        if (mEchoing) return;

        mEchoing = true;

        int bufSize = getBufferSize();

        mBuffer = new byte[bufSize];

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                CHANNEL_IN, ENCODING, mBuffer.length);

        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                CHANNEL_OUT, ENCODING, mBuffer.length, AudioTrack.MODE_STREAM);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                mTrack.play();
                mRecorder.startRecording();
                while (mEchoing) {
                    mRecorder.read(mBuffer, 0, mBuffer.length);
                    mTrack.write(mBuffer, 0, mBuffer.length);
                    if (((++count) % 10)==0) {
                        addDump(Byte.toString(mBuffer[0]));
                    }
                }
                mRecorder.stop();
                mTrack.stop();
            }
        }).start();
    }


    public void onStopClicked(View v) {
        Log.i(TAG, "onStopClicked()");
        mEchoing = false;
    }

    private int getBufferSize() {
        int outSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, ENCODING);
        outSize = Integer.highestOneBit(outSize) * 2;

        int inSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, ENCODING);
        inSize = Integer.highestOneBit(inSize) * 2;

        int bufSize = (outSize > inSize) ? outSize : inSize;

        Log.i(TAG, "bufferSize " + bufSize);
        return bufSize;
    }


}
