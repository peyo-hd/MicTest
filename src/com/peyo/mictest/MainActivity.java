package com.peyo.mictest;

import android.Manifest;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    private static final String TAG = "MicTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
        requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, 0);
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

    private static final int SAMPLE_RATE = 8000;
    private static final int SAMPLE_ENCODING = AudioFormat.ENCODING_PCM_8BIT;

    AudioRecord mRecorder;
    AudioTrack mTrack;
    byte mBuffer[];
    boolean mEchoing;

    public void onEchoClicked(View v) {
        Log.i(TAG, "onEchoClicked()");

        if (mEchoing) return;

        mEchoing = true;

        int bufSize = getBufferSize();

        mBuffer = new byte[bufSize];

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, SAMPLE_ENCODING, mBuffer.length);

        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                SAMPLE_ENCODING, mBuffer.length, AudioTrack.MODE_STREAM);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mTrack.play();
                mRecorder.startRecording();
                while (mEchoing) {
                    mRecorder.read(mBuffer, 0, mBuffer.length);
                    mTrack.write(mBuffer, 0, mBuffer.length);
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
        int outSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, SAMPLE_ENCODING);
        outSize = Integer.highestOneBit(outSize) * 2;

        int inSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, SAMPLE_ENCODING);
        inSize = Integer.highestOneBit(inSize) * 2;

        int bufSize = (outSize > inSize) ? outSize : inSize;

        Log.i(TAG, "bufferSize " + bufSize);
        return bufSize;
    }


}
