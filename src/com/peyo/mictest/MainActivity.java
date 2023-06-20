package com.peyo.mictest;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;

public class MainActivity extends Activity {
    private static final String TAG = "MicTest";

    Context mContext;
    Ringtone mRingtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
        mPlaying = false;

        mContext = this;
        Uri uri = Uri.fromFile(new File("/product/media/audio/ui/KeypressInvalid.ogg"));
        mRingtone = RingtoneManager.getRingtone(mContext, uri);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    boolean mPlaying;

    public void onEchoClicked(View v) {
        Log.i(TAG, "onPlayClicked()");

        if (mPlaying) return;

        mPlaying = true;

        mRingtone.play();
    }

    public void onStopClicked(View v) {
        Log.i(TAG, "onStopClicked()");
        mPlaying = false;
        mRingtone.stop();
    }

}
