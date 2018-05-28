package com.ghondar.vlcplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

public class Player2Activity extends Activity {

    public static void go(Context context, String url) {
        Intent intent = new Intent(context, Player2Activity.class);
        intent.putExtra(LOCATION, url);
        context.startActivity(intent);
    }

    public final static String LOCATION = "srcVideo";
    private VlcPlayerView playerView;
    private String url;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        Log.e("Player", "this is Player2Activity");
        playerView = new VlcPlayerView(this);
        playerView.toggleFullscreen(true);
        setContentView(playerView);
        Intent intent = getIntent();
        url = intent.getExtras().getString(LOCATION);
        playerView.playMovie(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerView != null) {
            playerView.resumePlay();
        }
        Log.e("Player2Activity", " onresume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerView != null) {
            playerView.pausePlay();
        }
        Log.e("Player2Activity", "pause");
    }

    @Override
    protected void onDestroy() {
        if (playerView != null) {
            playerView.releasePlayer();
        }
        super.onDestroy();
        Log.e("Player2Activity", "destroy");
    }
}
