package com.newlegacyxc.activity.multi;

import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.newlegacyxc.R;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.MulteScreenMenuDialog;
import com.newlegacyxc.dialog.SelectChannelDialog;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MultiScreenActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, IVLCVout.Callback, View.OnFocusChangeListener {

    private String TAG=this.getClass().getSimpleName();
    private List<SurfaceHolder> surfaceHolderList=new ArrayList<>(4);
    private List<SurfaceView> surfaceList=new ArrayList<>(4);
    private List<String> contentUriList=new ArrayList<>(4);
    private List<ImageView> imageViewList=new ArrayList<>(4);
    private List<Boolean> muteList=new ArrayList<>(4);
    private List<LibVLC> libVlcList=new ArrayList<>(4);
    private List<MediaPlayer> mMediaPlayerList = new ArrayList<>(4);
    private List<Integer> categoryPosList=new ArrayList<>(4);
    private List<Integer> channelPosList=new ArrayList<>(4);
    private List<RelativeLayout> laySurfaceList =new ArrayList<>(4);
    private boolean is_create= true;
    private int num_screen;
    private List<MediaPlayer.EventListener> mPlayerListenerList = new ArrayList<>(4);
    private int mVideoWidth,mVideoHeight;
    private boolean is_full=false;
    private int[] coordinates=new int[2];
    private int[] size = new int[2];
    private int selected_screen_id=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        num_screen=getIntent().getIntExtra("num_screen",4);
        if (num_screen==4) setContentView(R.layout.activity_multi_screen_four);
        else if (num_screen==3) setContentView(R.layout.activity_multi_screen_three);
        else setContentView(R.layout.activity_multi_screen_two);
        FullScreencall();
        SurfaceView surfaceView1, surfaceView2, surfaceView3, surfaceView4;
        SurfaceHolder holder1,holder2, holder3, holder4;
        for (int i=0;i<num_screen;i++){
            categoryPosList.add(0);
            channelPosList.add(0);
            contentUriList.add("");
            libVlcList.add(null);
            mMediaPlayerList.add(null);
            mPlayerListenerList.add(new MediaPlayerListener(this));
            muteList.add(true);
        }
        findViewById(R.id.lay1).setOnClickListener(this);
        findViewById(R.id.lay1).setOnFocusChangeListener(this);
        imageViewList.add(0,(ImageView) findViewById(R.id.mute1));
        findViewById(R.id.mute1).setOnClickListener(this);
        surfaceView1=findViewById(R.id.surface_view1);
        surfaceList.add(0, surfaceView1);
        holder1=surfaceView1.getHolder();
        holder1.addCallback(this);
        holder1.setFormat(PixelFormat.RGBX_8888);
        laySurfaceList.add((RelativeLayout) findViewById(R.id.lay1));
        surfaceHolderList.add(0,holder1);

        findViewById(R.id.lay2).setOnClickListener(this);
        findViewById(R.id.lay2).setOnFocusChangeListener(this);
        imageViewList.add(1,(ImageView) findViewById(R.id.mute2));
        findViewById(R.id.mute2).setOnClickListener(this);
        surfaceView2=findViewById(R.id.surface_view2);
        surfaceList.add(1, surfaceView2);
        holder2=surfaceView2.getHolder();
        holder2.addCallback(this);
        holder2.setFormat(PixelFormat.RGBX_8888);
        laySurfaceList.add((RelativeLayout) findViewById(R.id.lay2));
        surfaceHolderList.add(1,holder2);

        if (num_screen==4){
            findViewById(R.id.lay3).setOnClickListener(this);
            findViewById(R.id.lay3).setOnFocusChangeListener(this);
            findViewById(R.id.mute3).setOnClickListener(this);
            imageViewList.add(2,(ImageView) findViewById(R.id.mute3));
            surfaceView3=findViewById(R.id.surface_view3);
            surfaceList.add(2, surfaceView3);
            holder3=surfaceView3.getHolder();
            holder3.addCallback(this);
            holder3.setFormat(PixelFormat.RGBX_8888);
            laySurfaceList.add((RelativeLayout) findViewById(R.id.lay3));
            surfaceHolderList.add(2,holder3);

            findViewById(R.id.lay4).setOnClickListener(this);
            findViewById(R.id.lay4).setOnFocusChangeListener(this);
            findViewById(R.id.mute4).setOnClickListener(this);
            imageViewList.add(3,(ImageView) findViewById(R.id.mute4));
            surfaceView4=findViewById(R.id.surface_view4);
            surfaceList.add(3, surfaceView4);
            holder4=surfaceView4.getHolder();
            holder4.addCallback(this);
            holder4.setFormat(PixelFormat.RGBX_8888);
            laySurfaceList.add((RelativeLayout) findViewById(R.id.lay4));
            surfaceHolderList.add(3,holder4);
        }else if (num_screen==3){
            findViewById(R.id.lay3).setOnClickListener(this);
            findViewById(R.id.lay3).setOnFocusChangeListener(this);
            findViewById(R.id.mute3).setOnClickListener(this);
            imageViewList.add(2,(ImageView) findViewById(R.id.mute3));
            surfaceView3=findViewById(R.id.surface_view3);
            surfaceList.add(2, surfaceView3);
            holder3=surfaceView3.getHolder();
            holder3.addCallback(this);
            holder3.setFormat(PixelFormat.RGBX_8888);
            laySurfaceList.add((RelativeLayout) findViewById(R.id.lay3));
            surfaceHolderList.add(2,holder3);
        }
        for (int i=0;i<num_screen;i++){
            setSize(surfaceHolderList.get(i));
        }
        //For initial video
        setChannel(0);
    }

    public void FullScreencall() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mute1:
                showMenu(0);
                break;
            case R.id.mute2:
                showMenu(1);
                break;
            case R.id.mute3:
                showMenu(2);
                break;
            case R.id.mute4:
                showMenu(3);
                break;
            case R.id.lay1:
                setChannel(0);
                break;
            case R.id.lay2:
                setChannel(1);
                break;
            case R.id.lay3:
                setChannel(2);
                break;
            case R.id.lay4:
                setChannel(3);
                break;
        }
    }

    private void setSize(SurfaceHolder holder){
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels/2;
        mVideoWidth = displayMetrics.widthPixels/2;
        Log.e(TAG,"size "+mVideoWidth+" : "+mVideoHeight);
        holder.setFixedSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        for (int i=0;i<num_screen;i++){
//            releaseMediaPlayer(i);
//        }
//    }

    private void releaseMediaPlayer(int i) {
        Log.e(TAG,"release player "+i);
        if (libVlcList.size()!=num_screen || libVlcList.get(i) == null)
            return;
        if (mMediaPlayerList.get(i) != null) {
            mMediaPlayerList.get(i).stop();
            final IVLCVout vout = mMediaPlayerList.get(i).getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
        }

        if (surfaceHolderList.size()!=num_screen)surfaceHolderList.add(i,null);
        else surfaceHolderList.set(i,null);
        libVlcList.get(i).release();
        if (libVlcList.size()!=num_screen) libVlcList.add(i,null);
        else libVlcList.set(i,null);
    }

    private void playVideo(int i){
        try {
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("0");//this option is used to show the first subtitle track
            options.add("--subsdec-encoding");

            libVlcList.set(i,new LibVLC(this, options));

            mMediaPlayerList.set(i,new MediaPlayer(libVlcList.get(i)));
            mMediaPlayerList.get(i).setEventListener(mPlayerListenerList.get(i));
            mMediaPlayerList.get(i).setAspectRatio(MyApp.SCREEN_WIDTH+":"+ MyApp.SCREEN_HEIGHT);

            // Seting up video output
            final IVLCVout vout = mMediaPlayerList.get(i).getVLCVout();
            vout.setVideoView(surfaceList.get(i));
            vout.setWindowSize(mVideoWidth, mVideoHeight);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libVlcList.get(i), Uri.parse(contentUriList.get(i)));
            mMediaPlayerList.get(i).setMedia(m);
            mMediaPlayerList.get(i).play();

        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void toggleMute(int i) {
        Log.e(TAG,"mute clicked "+i);
        ImageView imageView=imageViewList.get(i);
        if (muteList.get(i)){
            if (mMediaPlayerList.get(i)!=null) {
                muteList.set(i,false);
                imageView.setImageResource(R.drawable.sound_mute);
                mMediaPlayerList.get(i).setVolume(0);
            }
        }else {
            if (mMediaPlayerList.get(i)!=null) {
                muteList.set(i,true);
                imageView.setImageResource(R.drawable.sound);
                mMediaPlayerList.get(i).setVolume(100);
            }
        }
    }

    private void setChannel(final int i) {
//        if ( mMediaPlayerList.get(i)!=null) mMediaPlayerList.get(i).pause();
        Log.e(TAG,"setchannel clicked "+i);
        FragmentManager fm = getSupportFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("fragment_alert");
        if (prev != null) {
            ft.remove(prev);
            ft.addToBackStack(null);
            return;
        }
        SelectChannelDialog selectChannelDialog = SelectChannelDialog.newInstance(i,categoryPosList.get(i),channelPosList.get(i));
        selectChannelDialog.setSelectChannelListener((category_pos, channel_pos, channelModel) -> {
            releaseMediaPlayer(i);
            categoryPosList.set(i,category_pos);
            channelPosList.set(i,channel_pos);
            contentUriList.set(i, MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                    channelModel.getStream_id(),"ts"));                playVideo(i);
            laySurfaceList.get(i).requestFocus();
        });
        selectChannelDialog.show(fm, "fragment_alert");
        laySurfaceList.get(i).requestFocus();
    }

    private void showMenu(final int i) {
        selected_screen_id=i;
        FragmentManager fm = getSupportFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("fragment_menu");
        if (prev != null) {
            ft.remove(prev);
            ft.addToBackStack(null);
            return;
        }
        boolean isPlaying;
        if (mMediaPlayerList.get(i)!=null) isPlaying=mMediaPlayerList.get(i).isPlaying();
        else isPlaying=true;
        MulteScreenMenuDialog selectChannelDialog = MulteScreenMenuDialog.newInstance(isPlaying, muteList.get(i));
        selectChannelDialog.setMultiScreenMenuListenerListener(new MulteScreenMenuDialog.MultiScreenMenuListener() {
            @Override
            public void onPlus() {
                setChannel(i);
            }

            @Override
            public void onFullScreen() {
                toggleFullScreen(selected_screen_id);
                laySurfaceList.get(i).requestFocus();
            }

            @Override
            public void onPlayPause(boolean is_playing) {
                if (is_playing) mMediaPlayerList.get(i).pause();
                else mMediaPlayerList.get(i).play();
            }

            @Override
            public void onSoundMute(boolean is_mute) {
                toggleMute(i);
            }
        });
        selectChannelDialog.show(fm, "fragment_menu");
    }

    private void setMute(boolean b, int i) {
        Log.e(TAG,"mute clicked "+i);
        ImageView imageView=imageViewList.get(i);
        if (b){
            if (mMediaPlayerList.get(i)!=null) {
                muteList.set(i,false);
                imageView.setImageResource(R.drawable.sound_mute);
                mMediaPlayerList.get(i).setVolume(0);
            }
        }else {
            if (mMediaPlayerList.get(i)!=null) {
                muteList.set(i,true);
                imageView.setImageResource(R.drawable.sound);
                mMediaPlayerList.get(i).setVolume(100);
            }
        }
    }

    private void toggleFullScreen(int i) {
        if (!is_full){
            is_full = true;
            for (int j=0;j<num_screen;j++) {
                if (i==j) {
//                    playVideo(i);
                    laySurfaceList.get(j).setVisibility(View.VISIBLE);
                    surfaceList.get(j).setVisibility(View.VISIBLE);
                }
                else {
                    releaseMediaPlayer(j);
                    laySurfaceList.get(j).setVisibility(View.GONE);
                    surfaceList.get(j).setVisibility(View.GONE);
                }
            }
            switch (num_screen){
                case 2:
                    ConstraintLayout rootview=findViewById(R.id.rootview);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    if (i==0){
                        constraintSet.setGuidelinePercent(R.id.guideline1, 1.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
                    }else {
                        constraintSet.setGuidelinePercent(R.id.guideline1, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 3:
                    rootview=findViewById(R.id.rootview);
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    if (i==0){
                        constraintSet.setGuidelinePercent(R.id.guideline1, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline4, 1.0f);
                    }else if (i==1){
                        constraintSet.setGuidelinePercent(R.id.guideline2, 1.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline4, 0.0f);
                    }else {
                        constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline4, 0.0f);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 4:
                    LinearLayout linearLayout1, linearLayout2;
                    linearLayout1=findViewById(R.id.linearLayout1);
                    linearLayout2=findViewById(R.id.linearLayout2);
                    linearLayout1.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    for (int j=0;j<4;j++) {
                        if (i==j) {
                            if (i<2) {
                                linearLayout2.setVisibility(View.GONE);
                            }else {
                                linearLayout1.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
            }
        }else {
            is_full = false;
            for (int j=0;j<num_screen;j++) {
                if (i!=j) {
                    laySurfaceList.get(j).setVisibility(View.VISIBLE);
                    surfaceList.get(j).setVisibility(View.VISIBLE);
                    playVideo(j);
                }
            }
            switch (num_screen){
                case 2:
                    ConstraintLayout rootview=findViewById(R.id.rootview);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    constraintSet.setGuidelinePercent(R.id.guideline1, 0.5f);
                    constraintSet.setGuidelinePercent(R.id.guideline2, 0.25f);
                    constraintSet.setGuidelinePercent(R.id.guideline3, 0.75f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 3:
                    rootview=findViewById(R.id.rootview);
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    constraintSet.setGuidelinePercent(R.id.guideline1, 0.25f);
                    constraintSet.setGuidelinePercent(R.id.guideline2, 0.5f);
                    constraintSet.setGuidelinePercent(R.id.guideline3, 0.75f);
                    constraintSet.setGuidelinePercent(R.id.guideline4, 0.5f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 4:
                    LinearLayout linearLayout1, linearLayout2;
                    linearLayout1=findViewById(R.id.linearLayout1);
                    linearLayout2=findViewById(R.id.linearLayout2);
                    linearLayout1.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    switch (view.getId()){
                        case R.id.lay1:
                            showMenu(0);
                            break;
                        case R.id.lay2:
                            showMenu(1);
                            break;
                        case R.id.lay3:
                            showMenu(2);
                            break;
                        case R.id.lay4:
                            showMenu(3);
                            break;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (!is_full){
                        for (int i=0;i<num_screen;i++) {
                            releaseMediaPlayer(i);
                        }
                        finish();
                    }else {
                        for (int i=0;i<num_screen;i++) {
                            if (i!=selected_screen_id) releaseMediaPlayer(i);
                        }
                        toggleFullScreen(selected_screen_id);
                        return false;
                    }
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus){
            int i=0;
            switch (view.getId()){
                case R.id.lay1:
                    i=0;
                    break;
                case R.id.lay2:
                    i=1;
                    break;
                case R.id.lay3:
                    i=2;
                    break;
                case R.id.lay4:
                    i=3;
                    break;
            }
            for (int j=0;j<num_screen;j++){
                if (j==i) setMute(false,j);
                else setMute(true,j);
            }
        }
    }

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<MultiScreenActivity> mOwner;

        public MediaPlayerListener(MultiScreenActivity owner) {
            mOwner = new WeakReference<MultiScreenActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            MultiScreenActivity player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releaseMediaPlayer(0);
                    player.is_create = false;
//                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    break;
                case MediaPlayer.Event.Buffering:
                    break;
                case MediaPlayer.Event.EncounteredError:
                    break;
                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    break;
                default:
                    break;
            }
        }
    }
}
