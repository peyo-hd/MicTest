package com.peyo.mictest;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
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
                AudioTrack track = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(SAMPLE_ENCODING)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(getBufferSize())
                        .build();

                short[] buffer = new short[1024];
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
