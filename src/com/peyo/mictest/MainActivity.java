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
import android.widget.EditText;

public class MainActivity extends Activity {
    private static final String TAG = "MicTest";
    private EditText mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
        mNumber = (EditText)findViewById(R.id.freq);
        mPlaying = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    boolean mPlaying;
    float mFreq;

    public void onEchoClicked(View v) {
        Log.i(TAG, "onPlayClicked()");

        if (mPlaying) return;

        mPlaying = true;

        String num = mNumber.getText().toString();
        mFreq = Integer.parseInt(num);

        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] buffer = new short[1024];
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, SAMPLE_ENCODING, getBufferSize(), AudioTrack.MODE_STREAM);
                float increment = (float)((2.0 * Math.PI) * mFreq / 48000.0); // angular increment for each sample
                float angle = 0;
                float samples[] = new float[1024];

                track.play();

                while (mPlaying) {
                    for (int i = 0; i < samples.length; i++) {
                        samples[i] = (float) Math.sin(angle);   //the part that makes this a sine wave....
                        buffer[i] = (short) (samples[i] * Short.MAX_VALUE / 2);
                        angle += increment;
                    }
                    track.write( buffer, 0, samples.length );  //write to the audio buffer.... and start all over again!

                }
            }
        }).start();
    }

    private static final int SAMPLE_RATE = 48000;
    private static final int SAMPLE_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int getBufferSize() {
                int outSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, SAMPLE_ENCODING);
                outSize = Integer.highestOneBit(outSize) * 2;

                return outSize;
            }


    public void onStopClicked(View v) {
        Log.i(TAG, "onStopClicked()");
        mPlaying = false;
    }

}
