package com.ghondar.vlc;

import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.ghondar.vlc.R;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCUtil;

import java.util.ArrayList;

public class VlcPlayerView extends FrameLayout implements IVLCVout.Callback, LifecycleEventListener, MediaPlayer.EventListener {

    private boolean pausedState;

    public enum Events {
        EVENT_PROGRESS("onVLCProgress"),
        EVENT_ENDED("onVLCEnded"),
        EVENT_STOPPED("onVLCStopped"),
        EVENT_PLAYING("onVLCPlaying"),
        EVENT_BUFFERING("onVLCBuffering"),
        EVENT_PAUSED("onVLCPaused"),
        EVENT_ERROR("onVLCError"),
        EVENT_VOLUME_CHANGED("onVLCVolumeChanged"),
        EVENT_SEEK("onVLCVideoSeek");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    public static final String EVENT_PROP_DURATION = "duration";
    public static final String EVENT_PROP_CURRENT_TIME = "currentTime";
    public static final String EVENT_PROP_POSITION = "position";
    public static final String EVENT_PROP_END = "endReached";
    public static final String EVENT_PROP_SEEK_TIME = "seekTime";

    private ThemedReactContext mThemedReactContext;
    private RCTEventEmitter mEventEmitter;

    private String mSrcString;

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;

    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;
    private int mVideoHeight;
    private int mVideoWidth;

    private int counter = 0;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;

    private int mCurrentSize = SURFACE_BEST_FIT;
    private Media media;
    private boolean autoPlay;

    public VlcPlayerView(ThemedReactContext context) {
        super(context);
        mThemedReactContext = context;
        mEventEmitter = mThemedReactContext.getJSModule(RCTEventEmitter.class);
        mThemedReactContext.addLifecycleEventListener(this);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.player, this);

        mSurface = (SurfaceView) findViewById(R.id.vlc_surface);
        holder = mSurface.getHolder();
        initializePlayerIfNeeded();
    }

    private void initializePlayerIfNeeded() {
        if (mMediaPlayer == null) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            // Create LibVLC
            ArrayList<String> options = new ArrayList<>(50);
            int deblocking = getDeblocking(-1);

            int networkCaching = pref.getInt("network_caching_value", 0);
            if (networkCaching > 60000) networkCaching = 60000;
            else if (networkCaching < 0) networkCaching = 0;
            options.add("--audio-time-stretch");
            options.add("--avcodec-skiploopfilter");
            options.add("" + deblocking);
            options.add("--avcodec-skip-frame");
            options.add("0");
            options.add("--avcodec-skip-idct");
            options.add("0");
            options.add("--subsdec-encoding");
            options.add("--stats");
            if (networkCaching > 0) options.add("--network-caching=" + networkCaching);
            options.add("--androidwindow-chroma");
            options.add("RV32");

            options.add("-vv");

            libvlc = new LibVLC(options);

            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
            mMediaPlayer.setEventListener(this);
        }
    }

    private void setMedia(String filePath) {
        // Set up video output
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        if (!vout.areViewsAttached()) {
            vout.setVideoView(mSurface);
            vout.addCallback(this);
            vout.attachViews();
        }
        Uri uri = Uri.parse(filePath);
        media = new Media(libvlc, uri);
        mMediaPlayer.setMedia(media);
        if (autoPlay) {
            mMediaPlayer.play();
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
            if (m == null) return ret;
            if ((m.hasArmV6 && !(m.hasArmV7)) || m.hasMips) ret = 4;
            else if (m.frequency >= 1200 && m.processors > 2) ret = 1;
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1;
            } else ret = 3;
        } else if (deblocking > 4) { // sanity check
            ret = 3;
        }
        return ret;
    }

    private void releasePlayer() {
        if (libvlc == null) return;
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

    private void changeSurfaceSize(int width, int height) {
        int screenWidth = width;
        int screenHeight = height;
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = width;
        mVideoVisibleHeight = height;

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
                if (counter > 2) if (displayAspectRatio < aspectRatio) displayHeight = displayWidth / aspectRatio;
                else displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_FIT_HORIZONTAL:
                displayHeight = displayWidth / aspectRatio;
                break;
            case SURFACE_FIT_VERTICAL:
                displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                aspectRatio = 16.0 / 9.0;
                if (displayAspectRatio < aspectRatio) displayHeight = displayWidth / aspectRatio;
                else displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_4_3:
                aspectRatio = 4.0 / 3.0;
                if (displayAspectRatio < aspectRatio) displayHeight = displayWidth / aspectRatio;
                else displayWidth = displayHeight * aspectRatio;
                break;
            case SURFACE_ORIGINAL:
                displayHeight = mVideoVisibleHeight;
                displayWidth = visibleWidth;
                break;
        }

        // set display size
        int finalWidth = (int) Math.ceil(displayWidth * mVideoWidth / mVideoVisibleWidth);
        int finalHeight = (int) Math.ceil(displayHeight * mVideoHeight / mVideoVisibleHeight);

        SurfaceHolder holder = mSurface.getHolder();
        holder.setFixedSize(finalWidth, finalHeight);

        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
        lp.width = finalWidth;
        lp.height = finalHeight;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    public void changeSurfaceLayout(int width, int height) {
        changeSurfaceSize(width, height);
    }

    public void setFilePath(String filePath) {
        this.mSrcString = filePath;
        setMedia(mSrcString);
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public void setCurrentSize(int currentSize) {
        mCurrentSize = currentSize;
    }

    /**
     * Play or pause the media.
     */
    public void setPaused(boolean paused) {
        pausedState = paused;
        if (paused) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        } else {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.play();
            }
        }
    }

    public void onDropViewInstance() {
        releasePlayer();
    }

    public void seek(float seek) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, mMediaPlayer.getTime());
        event.putDouble(EVENT_PROP_SEEK_TIME, seek);
        mEventEmitter.receiveEvent(getId(), Events.EVENT_SEEK.toString(), event);
        mMediaPlayer.setTime((long) (mMediaPlayer.getLength() * seek));
    }

    public void setRate(float rate) {
        mMediaPlayer.setRate(rate);
    }

    public float getRate() {
        return mMediaPlayer.getRate();
    }

    public void setVolume(int volume) {
        mMediaPlayer.setVolume(volume);
    }

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0) return;

        // store video size
        mSarNum = sarNum;
        mSarDen = sarDen;
        changeSurfaceLayout(width, height);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vout) {
        // Handle errors with hardware acceleration
        this.releasePlayer();
        Toast.makeText(getContext(), "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onHostResume() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // Restore original state
                setPaused(pausedState);
            }
        });
    }

    @Override
    public void onHostPause() {
        setPaused(true);
    }

    @Override
    public void onHostDestroy() {

    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        WritableMap eventMap = Arguments.createMap();
        switch (event.type) {
            case MediaPlayer.Event.EndReached:
                pausedState = false;
                eventMap.putBoolean(EVENT_PROP_END, true);
                mEventEmitter.receiveEvent(getId(), Events.EVENT_ENDED.toString(), eventMap);
                break;
            case MediaPlayer.Event.Stopped:
                mEventEmitter.receiveEvent(getId(), Events.EVENT_STOPPED.toString(), null);
                break;
            case MediaPlayer.Event.Playing:
                eventMap.putDouble(EVENT_PROP_DURATION, mMediaPlayer.getLength());
                mEventEmitter.receiveEvent(getId(), Events.EVENT_PLAYING.toString(), eventMap);
                break;
//            case MediaPlayer.Event.Buffering:
//                mEventEmitter.receiveEvent(getId(), Events.EVENT_PLAYING.toString(), null);
//                break;
            case MediaPlayer.Event.Paused:
                mEventEmitter.receiveEvent(getId(), Events.EVENT_PAUSED.toString(), null);
                break;
            case MediaPlayer.Event.EncounteredError:
                mEventEmitter.receiveEvent(getId(), Events.EVENT_ERROR.toString(), null);
                break;
            case MediaPlayer.Event.TimeChanged:
                eventMap.putDouble(EVENT_PROP_CURRENT_TIME, mMediaPlayer.getTime());
                eventMap.putDouble(EVENT_PROP_DURATION, mMediaPlayer.getLength());
                eventMap.putDouble(EVENT_PROP_POSITION, mMediaPlayer.getPosition());
                mEventEmitter.receiveEvent(getId(), Events.EVENT_PROGRESS.toString(), eventMap);
                break;
        }
    }
}
