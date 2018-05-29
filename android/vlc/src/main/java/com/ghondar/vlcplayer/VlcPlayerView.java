package com.ghondar.vlcplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vlcplayer.R;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VlcPlayerView extends FrameLayout implements IVLCVout.Callback {

    public final static String LOCATION = "srcVideo";

    // display surface
    private LinearLayout layout;
    private FrameLayout vlcOverlay;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private ImageView vlcButtonPlayPause;
    private ImageButton vlcButtonScale;
    private Handler handlerOverlay;
    private Runnable runnableOverlay;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    private int counter = 0;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;

    private int mCurrentSize = SURFACE_BEST_FIT;
    private String url;


    public VlcPlayerView(@NonNull Context context) {
        super(context);
        initLayout();
    }

    public VlcPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    private void initLayout() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.player, this, false);
        addView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout = (LinearLayout) findViewById(R.id.vlc_container);
        mTextureView = (TextureView) findViewById(R.id.vlc_surface);

        vlcOverlay = (FrameLayout) findViewById(R.id.vlc_overlay);
        vlcButtonPlayPause = (ImageView) findViewById(R.id.vlc_button_play_pause);
        vlcButtonScale = (ImageButton) findViewById(R.id.vlc_button_scale);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                mSurfaceTexture = surfaceTexture;
                createPlayer(url);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                mSurfaceTexture = null;
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });
        // 开启硬件加速
        if (getContext() instanceof Activity) {
            ((Activity) getContext()).getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }

    public void playMovie(String url) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            return;
        layout.setVisibility(View.VISIBLE);
        this.url = url;
        if (mSurfaceTexture != null) {
            createPlayer(url);
        }
    }

    public void toggleFullscreen(boolean fullscreen) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            if (fullscreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
            } else {
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            }
            activity.getWindow().setAttributes(attrs);
        }
    }

    private void setupControls() {
        // PLAY PAUSE
        vlcButtonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer == null) {
                    return;
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play_over_video));
                } else {
                    mMediaPlayer.play();
                    vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause_over_video));
                }
            }
        });

        vlcButtonScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentSize < SURFACE_ORIGINAL) {
                    mCurrentSize++;
                } else {
                    mCurrentSize = 0;
                }
                changeSurfaceSize(true);
            }
        });
        // OVERLAY
        handlerOverlay = new Handler();
        runnableOverlay = new Runnable() {
            @Override
            public void run() {
                vlcOverlay.setVisibility(View.GONE);
                toggleFullscreen(true);
            }
        };
        final long timeToDisappear = 3000;
        handlerOverlay.postDelayed(runnableOverlay, timeToDisappear);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vlcOverlay.setVisibility(View.VISIBLE);

                handlerOverlay.removeCallbacks(runnableOverlay);
                handlerOverlay.postDelayed(runnableOverlay, timeToDisappear);
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeSurfaceLayout();
    }

    /**
     * 恢复播放
     */
    public void resumePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.play();
            vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause_over_video));
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            vlcButtonPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play_over_video));
        }
    }

    /**
     * 判断是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * 保存当前视频播放的这一帧到本地
     */
    public void capturePic() {
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            File allDir = new File(Environment.getExternalStorageDirectory(), Constant.DIR);
            if (!allDir.exists() || !allDir.isDirectory()) {
                allDir.mkdirs();
            }
            File screenShotsDir = new File(allDir, Constant.SCREEN_SHOTS);
            if (!screenShotsDir.exists() || !screenShotsDir.isDirectory()) {
                screenShotsDir.mkdirs();
            }
            if (mTextureView != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                File file = new File(screenShotsDir, dateFormat.format(new Date()) + ".png");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Bitmap bmp = mTextureView.getBitmap();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    Log.e("capture", "capture 7777");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            Log.e("capture", "capture failure");
        }
    }

    /**
     * 启动另一个界面，实现全屏播放
     */
    public void fullScreen() {
        Player2Activity.go(getContext(), url);
    }

    /*************
     * Surface
     *************/
    @SuppressWarnings("SuspiciousNameCombination")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceSize(boolean message) {
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();
        if (mMediaPlayer != null) {
            final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.setWindowSize(screenWidth, screenHeight);
        }
        double displayWidth = screenWidth, displayHeight = screenHeight;
        if (screenWidth < screenHeight) {
            displayWidth = screenHeight;
            displayHeight = screenWidth;
        }
        // sanity check
        if (displayWidth * displayHeight <= 1 || mVideoWidth * mVideoHeight <= 1) {
            return;
        }
        // compute the aspect ratio
        double aspectRatio, visibleWidth;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            visibleWidth = mVideoVisibleWidth;
            aspectRatio = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            visibleWidth = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            aspectRatio = visibleWidth / mVideoVisibleHeight;
        }
        // compute the display aspect ratio
        double displayAspectRatio = displayWidth / displayHeight;
        counter++;
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (counter > 2)
                    showToast("Best Fit");
                if (displayAspectRatio < aspectRatio)
                    displayHeight = displayWidth / aspectRatio;
                else
                    displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_FIT_HORIZONTAL:
                showToast("Fit Horizontal");
                displayHeight = displayWidth / aspectRatio;
                break;
            case SURFACE_FIT_VERTICAL:
                showToast("Fit Horizontal");
                displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_FILL:
                showToast("Fill");
                break;
            case SURFACE_16_9:
                showToast("16:9");
                aspectRatio = 16.0 / 9.0;
                if (displayAspectRatio < aspectRatio)
                    displayHeight = displayWidth / aspectRatio;
                else
                    displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_4_3:
                showToast("4:3");
                aspectRatio = 4.0 / 3.0;
                if (displayAspectRatio < aspectRatio)
                    displayHeight = displayWidth / aspectRatio;
                else
                    displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_ORIGINAL:
                showToast("Original");
                displayHeight = mVideoVisibleHeight;
                displayWidth = visibleWidth;
                break;
        }

        // set display size
        int finalWidth = (int) Math.ceil(displayWidth * mVideoWidth / mVideoVisibleWidth);
        int finalHeight = (int) Math.ceil(displayHeight * mVideoHeight / mVideoVisibleHeight);
        mSurfaceTexture.setDefaultBufferSize(finalWidth, finalHeight);
        ViewGroup.LayoutParams lp = mTextureView.getLayoutParams();
        lp.width = finalWidth;
        lp.height = finalHeight;
        mTextureView.setLayoutParams(lp);
        mTextureView.invalidate();
    }

    private int getScreenWidth() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    private int getScreenHeight() {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    private void changeSurfaceLayout() {
        changeSurfaceSize(false);
    }

    /*************
     * Player
     *************/

    private void createPlayer(String media) {
        releasePlayer();
        setupControls();
        try {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
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

            mTextureView.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            //mMediaPlayer.setFormat(PixelFormat.RGBX_8888);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            if (!vout.areViewsAttached()) {
                vout.setVideoView(mTextureView);
                vout.addCallback(this);
                vout.attachViews();
            }
            //vout.setSubtitlesView(mTextureViewSubtitles);
            Uri uri = Uri.parse(media);
            Media m = new Media(libvlc, uri);
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            showToast("Error creating player!");
            e.printStackTrace();
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

    /**
     * 释放播放器
     */
    public void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();

        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);

        vout.detachViews();
        mSurfaceTexture = null;
        libvlc.release();
        libvlc = null;
        mMediaPlayer.release();
        mMediaPlayer = null;
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
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
        changeSurfaceLayout();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VlcPlayerView> mOwner;

        public MyPlayerListener(VlcPlayerView owner) {
            mOwner = new WeakReference<VlcPlayerView>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VlcPlayerView player = mOwner.get();
            if (player != null) {
                switch (event.type) {
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
    }

    public void onHardwareAccelerationError(IVLCVout vout) {
        // Handle errors with hardware acceleration
        this.releasePlayer();
        showToast("Error with hardware acceleration");
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

}
