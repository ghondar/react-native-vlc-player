package com.ghondar.vlcplayer;

import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.videolan.libvlc.LibVLC;

import java.io.File;

public class VLCPlayer extends ReactContextBaseJavaModule {

    private ReactApplicationContext context;
    private LibVLC mLibVLC = null;

    public VLCPlayer(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;

        try {
            mLibVLC = new LibVLC();
        } catch(IllegalStateException e) {
            Toast.makeText(reactContext,
                    "Error initializing the libVLC multimedia framework!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String getName() {
        return "VLCPlayer";
    }

    @ReactMethod
    public void play(String path) {
        Intent intent = new Intent(this.context, PlayerActivity.class);
        intent.putExtra(PlayerActivity.LOCATION, path);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intent);
    }

}
