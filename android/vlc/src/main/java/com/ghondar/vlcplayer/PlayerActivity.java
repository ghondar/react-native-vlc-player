package com.ghondar.vlcplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vlcplayer.R;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.libvlc.util.VLCUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PlayerActivity extends Activity implements IVLCVout.Callback, LibVLC.HardwareAccelerationError {

    public final static String LOCATION = "srcVideo";

    private String mFilePath;

    // display surface
    // display surface
    private LinearLayout layout;
    private FrameLayout frameLayout2;
    private SurfaceView mSurface;
    private SurfaceHolder holder;
    private ImageView vlcButtonPlayPause;
    private Handler handlerOverlay;
    private Runnable runnableOverlay;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    /*************
     * Activity
     *************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBaselineAligned(false);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        FrameLayout frameLayout1 = new FrameLayout(this);
        frameLayout1.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mSurface = new SurfaceView(this);
        mSurface.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.CENTER));

        frameLayout2 = new FrameLayout(this);
        frameLayout2.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        int paddingPixel = 80;
        float density = this.getApplication().getResources().getDisplayMetrics().density;
        int paddingDp = (int)(paddingPixel * density);

        vlcButtonPlayPause = new ImageView(this);
        vlcButtonPlayPause.setLayoutParams(new FrameLayout.LayoutParams(paddingDp, paddingDp, Gravity.CENTER));
        vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause_over_video));

        frameLayout2.addView(vlcButtonPlayPause);

        frameLayout1.addView(mSurface);
        frameLayout1.addView(frameLayout2);

        layout.addView(frameLayout1);

        setContentView(layout);

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getExtras().getString(LOCATION);
//        holder = mSurface.getHolder();
        playMovie();
        //holder.addCallback(this);
    }

    public void playMovie() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            return ;
        layout.setVisibility(View.VISIBLE);
        holder = mSurface.getHolder();
//        holder.addCallback(this);
        createPlayer(mFilePath);
    }

    private void toggleFullscreen(boolean fullscreen)
    {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen)
        {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else
        {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    private void setupControls() {
        // PLAY PAUSE
        vlcButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play_over_video));
                } else {
                    mMediaPlayer.play();
                    vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause_over_video));
                }
            }
        });

        // OVERLAY
        handlerOverlay = new Handler();
        runnableOverlay = new Runnable() {
            @Override
            public void run() {
                frameLayout2.setVisibility(View.GONE);
                toggleFullscreen(true);
            }
        };
        final long timeToDisappear = 3000;
        handlerOverlay.postDelayed(runnableOverlay, timeToDisappear);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameLayout2.setVisibility(View.VISIBLE);

                handlerOverlay.removeCallbacks(runnableOverlay);
                handlerOverlay.postDelayed(runnableOverlay, timeToDisappear);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        createPlayer(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    /*************
     * Surface
     *************/
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(holder == null || mSurface == null)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    /*************
     * Player
     *************/

    private void createPlayer(String media) {
        releasePlayer();
        setupControls();
        try {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>(50);
            int deblocking = getDeblocking(-1);

            int networkCaching = pref.getInt("network_caching_value", 0);
            if (networkCaching > 60000)
                networkCaching = 60000;
            else if (networkCaching < 0)
                networkCaching = 0;
            //options.add("--subsdec-encoding <encoding>");
              /* CPU intensive plugin, setting for slow devices */
            options.add("--audio-time-stretch");
            options.add("--avcodec-skiploopfilter");
            options.add("" + deblocking);
            options.add("--avcodec-skip-frame");
            options.add("0");
            options.add("--avcodec-skip-idct");
            options.add("0");
            options.add("--subsdec-encoding");
//            options.add(subtitlesEncoding);
            options.add("--stats");
        /* XXX: why can't the default be fine ? #7792 */
            if (networkCaching > 0)
                options.add("--network-caching=" + networkCaching);
            options.add("--androidwindow-chroma");
            options.add("RV32");

            options.add("-vv");

            libvlc = new LibVLC(options);
            libvlc.setOnHardwareAccelerationError(this);

            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            if (!vout.areViewsAttached()) {
                vout.setVideoView(mSurface);
                vout.addCallback(this);
                vout.attachViews();
            }
            //vout.setSubtitlesView(mSurfaceSubtitles);

            Media m = new Media(libvlc, media);
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private static int getDeblocking(int deblocking) {
        int ret = deblocking;
        if (deblocking < 0) {
            /**
             * Set some reasonable sDeblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            VLCUtil.MachineSpecs m = VLCUtil.getMachineSpecs();
            if (m == null)
                return ret;
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips)
                ret = 4;
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1;
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1;
            } else
                ret = 3;
        } else if (deblocking > 4) { // sanity check
            ret = 3;
        }
        return ret;
    }

    // TODO: handle this cleaner
    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /*************
     * Events
     *************/

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<PlayerActivity> mOwner;

        public MyPlayerListener(PlayerActivity owner) {
            mOwner = new WeakReference<PlayerActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            PlayerActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    @Override
    public void eventHardwareAccelerationError() {
        // Handle errors with hardware acceleration
        this.releasePlayer();
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }
}
