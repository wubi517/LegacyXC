package com.newlegacyxc.activity.tvguide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.newlegacyxc.R;
import com.newlegacyxc.activity.live.LiveExoPlayActivity;
import com.newlegacyxc.activity.live.LiveIjkPlayActivity;
import com.newlegacyxc.activity.live.LivePlayActivity;
import com.newlegacyxc.adapter.CategoryListAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.PinDlg;
import com.newlegacyxc.epg.EPG;
import com.newlegacyxc.epg.EPGClickListener;
import com.newlegacyxc.models.CategoryModel;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TVGuideNewActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback, IVLCVout.Callback, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private EPG epg;
    private EPGChannel selectedEpgChannel;
    private ListView listView;
    private CategoryListAdapter categoryAdapter;
    private RelativeLayout ly_surface;
    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);
    private SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    LinearLayout def_lay;
    String ratio;
    String[] resolutions ;
    private int mVideoWidth;
    private int mVideoHeight;
    private MediaPlayer mMediaPlayer = null;
    LibVLC libvlc=null;
    private TextView txt_time;
    SimpleDateFormat time = new SimpleDateFormat("d MMM hh:mm a");
    private String TAG="TVGuideNew";
    private int categoryPos=0;
    private View rip_back;
    private ConstraintLayout main_lay;
    private PowerManager.WakeLock wl;
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_tv_guide_new);

        txt_time = findViewById(R.id.clock);
        main_lay = findViewById(R.id.main);
        //VLC
        def_lay = findViewById(R.id.def_lay);
        surfaceView = findViewById(R.id.surface_view);
        ly_surface = findViewById(R.id.ly_surface);
        ly_surface.setOnClickListener(this);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBX_8888);

        remote_subtitles_surface = findViewById(R.id.remote_subtitles_surface);
        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        ViewGroup.LayoutParams params = ly_surface.getLayoutParams();
//        params.height = MyApp.SURFACE_HEIGHT;
//        params.width = MyApp.SURFACE_WIDTH;
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        ratio = mVideoWidth + ":"+ mVideoHeight;
        resolutions =  new String[]{"16:9", "4:3", ratio};
        Log.e(TAG, String.valueOf(MyApp.SCREEN_HEIGHT));
        listView = findViewById(R.id.category_recyclerview);
        categoryAdapter = new CategoryListAdapter(this,MyApp.live_categories_filter);
        listView.setOnItemClickListener(this);
        listView.setOnItemSelectedListener(this);
        listView.setAdapter(categoryAdapter);
        epg = (EPG) findViewById(R.id.epg);
        epg.setEPGClickListener(new EPGClickListener() {
            @Override
            public void onChannelClicked(int channelPosition, EPGChannel epg_channel) {
                epg.selectChannel(epg_channel,true);
                Log.e("onChannelClicked","true");
                playVideo(epg_channel);
            }

            @Override
            public void onEventClicked(int channelPosition, int programPosition, EPGEvent epgEvent) {
                if (epgEvent!=null) {
                    epg.selectEvent(epgEvent, true);
                    playVideo(epgEvent.getChannel());
                    Log.e("onEventClicked","true " + epgEvent.getTitle());
                }else playVideo(selectedEpgChannel);
            }

            @Override
            public void onResetButtonClicked() {
                epg.recalculateAndRedraw(true);
                Log.e("onResetButtonClicked","true");
            }
        });
        TextView currentEventTextView = (TextView) findViewById(R.id.textView7);
        TextView currentEventTimeTextView = (TextView) findViewById(R.id.textView4);
        TextView content = findViewById(R.id.textView8);
        ImageView current_channel_image = findViewById(R.id.current_channel_image);
        TextView channel_name = findViewById(R.id.channel_name);
        epg.setCurrentEventTextView(currentEventTextView);
        epg.setCurrentEventTimeTextView(currentEventTimeTextView);
        epg.setCurrentEventContentTextView(content);
        epg.setCurrent_channel_image(current_channel_image);
        epg.setChannel_name(channel_name);
        // Do initial load of data.
        Log.e("TVGuideActivity","epgdatas "+MyApp.epgDatas.size()+"");
        listView.performItemClick(
                listView.getAdapter().getView(1, null, null),
                1,
                listView.getAdapter().getItemId(1));
        rip_back = findViewById(R.id.rip_back);
        rip_back.setOnClickListener(this);
        FullScreencall();
        Thread myThread;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "INFO");
        wl.acquire(24*60*60*1000L /*1day*/);

        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("name");
        try{
            kl.disableKeyguard();
        }
        catch (SecurityException e)
        {
            //kindle code goes here
            e.printStackTrace();
        }
    }

    private void toggleCateRecy(boolean b) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(main_lay);
        float ratio;
        if (b) {
            ratio=0.0f;
        }
        else {
            ratio=0.2f;
        }
        epg.setIs_epg(b);
        constraintSet.setGuidelinePercent(R.id.guideline1, ratio);// 7% // range: 0 <-> 1
        constraintSet.applyTo(main_lay);
    }

    private void playVideo(EPGChannel epgChannel) {
        Log.e(TAG,"Start Video "+epgChannel.getName());
        if (selectedEpgChannel!=null && selectedEpgChannel.getStream_id().equals(epgChannel.getStream_id())){
            startVideo();
        }else {
            if (epgChannel.is_locked() && MyApp.live_categories_filter.get(categoryPos).getId().equals(Constants.all_id)){
                PinDlg pinDlg = new PinDlg(TVGuideNewActivity.this, new PinDlg.DlgPinListener() {
                    @Override
                    public void OnYesClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                        String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                        if(pin_code.equalsIgnoreCase(pin)){
                            dialog.dismiss();
                            playChannel(epgChannel);
                        }else {
                            dialog.dismiss();
                            Toast.makeText(TVGuideNewActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void OnCancelClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                    }
                });
                pinDlg.show();
            }else playChannel(epgChannel);
        }
    }

    private void releaseMediaPlayer() {
        if (libvlc == null)
            return;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
        }
        holder = null;

        libvlc.release();
        libvlc = null;

    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CategoryModel categoryModel = MyApp.live_categories_filter.get(position);
        if (categoryModel.getId().equals(Constants.xxx_category_id)){
            PinDlg pinDlg = new PinDlg(TVGuideNewActivity.this, new PinDlg.DlgPinListener() {
                @Override
                public void OnYesClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                    String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                    if(pin_code.equalsIgnoreCase(pin)){
                        dialog.dismiss();
                        toggleCateRecy(true);
                        categoryPos=position;
                        epg.SetEPGData(MyApp.epgDatas.get(categoryPos));
                        playChannel(epg.getEpgData().getChannel(0));
                    }else {
                        dialog.dismiss();
                        Toast.makeText(TVGuideNewActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void OnCancelClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                }
            });
            pinDlg.show();
        }else {
            toggleCateRecy(true);
            categoryPos=position;
            epg.SetEPGData(MyApp.epgDatas.get(categoryPos));
            playChannel(epg.getEpgData().getChannel(0));
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void doWork() {
        runOnUiThread(() -> {
            try {
                txt_time.setText(time.format(new Date()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startVideo() {
        String url = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                selectedEpgChannel.getStream_id()+"","ts");
        Log.e(getClass().getSimpleName(),url);
        int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
        Intent intent;
        switch (current_player){
            case 0:
            default:
                intent = new Intent(this, LivePlayActivity.class);
                break;
            case 1:
                intent = new Intent(this, LiveIjkPlayActivity.class);
                break;
            case 2:
                intent = new Intent(this, LiveExoPlayActivity.class);
                break;
        }
        MyApp.epgChannel = selectedEpgChannel;
        int now_pos = Constants.findNowEvent(selectedEpgChannel.getEvents());
        if (now_pos!=-1 && selectedEpgChannel.getEvents().size()>now_pos){
            EPGEvent epgEvent = selectedEpgChannel.getEvents().get(now_pos);
            int duration = (int) ((epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/1000);
            long start_mil = epgEvent.getStartTime().getTime();
            long now_mil = System.currentTimeMillis();
            intent.putExtra("channel_title", selectedEpgChannel.getName());
            intent.putExtra("img",selectedEpgChannel.getStream_icon());
            intent.putExtra("img", selectedEpgChannel.getStream_icon());
            intent.putExtra("url",url);
            intent.putExtra("duration",duration);
            intent.putExtra("start_mil",start_mil);
            intent.putExtra("now_mil",now_mil);
            intent.putExtra("stream_id", selectedEpgChannel.getStream_id());
            intent.putExtra("is_live",false);
            intent.putExtra("title", epgEvent.getTitle());
            intent.putExtra("current_dec", epgEvent.getTitle());
            intent.putExtra("dec", epgEvent.getDec());
            if(selectedEpgChannel.getEvents().size()>now_pos+1){
                intent.putExtra("next_dec", selectedEpgChannel.getEvents().get(now_pos+1).getTitle());
            }else {
                intent.putExtra("next_dec","No Information");
            }
        }else {
            long now_mil = System.currentTimeMillis();
            intent.putExtra("channel_title", selectedEpgChannel.getName());
            intent.putExtra("img",selectedEpgChannel.getStream_icon());
            intent.putExtra("img", selectedEpgChannel.getStream_icon());
            intent.putExtra("url",url);
            intent.putExtra("duration",0);
            intent.putExtra("start_mil",0);
            intent.putExtra("now_mil",now_mil);
            intent.putExtra("stream_id", selectedEpgChannel.getStream_id());
            intent.putExtra("is_live",false);
            intent.putExtra("title","No Information");
            intent.putExtra("current_dec", "No Information");
            intent.putExtra("dec", "No Information");
            intent.putExtra("next_dec","No Information");
        }
        startActivity(intent);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        try {
//            epg.setIs_epg(getCurrentFocus().getId()==R.id.epg);
//        }catch (Exception e){
//            e.printStackTrace();
//            epg.setIs_epg(true);
//        }
        if (event.getAction()==KeyEvent.ACTION_DOWN){
            if (epg != null) {
                if (epg.getIs_epg()) {
                    if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
                        toggleCateRecy(false);
                        rip_back.requestFocus();
                    }
                    return epg.dispatchKeyEvent(event);
                }else {
                    if (event.getKeyCode()==KeyEvent.KEYCODE_BACK) {
                        onBackPressed();
                    }
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void FullScreencall() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        if (epg != null) {
            epg.clearEPGImageCache();
        }
        wl.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_surface:
                startVideo();
                break;
            case R.id.rip_back:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedEpgChannel!=null){
            if (libvlc != null) {
                releaseMediaPlayer();
            }
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.addCallback(this);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int SCREEN_HEIGHT = displayMetrics.heightPixels;
            int SCREEN_WIDTH = displayMetrics.widthPixels;
            holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            mVideoHeight = displayMetrics.heightPixels;
            mVideoWidth = displayMetrics.widthPixels;
            if (!mMediaPlayer.isPlaying())playChannel(selectedEpgChannel);
        }
    }

    private void playChannel(EPGChannel epgChannel) {
        selectedEpgChannel = epgChannel;
        String contentUri = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                epgChannel.getStream_id() + "", "ts");
        Log.e("url", contentUri);
        if(def_lay.getVisibility()== View.VISIBLE) def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
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

            libvlc = new LibVLC(this, options);

            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);
            vout.setWindowSize(mVideoWidth, mVideoHeight);
            vout.addCallback(this);
            vout.attachViews();


            Media m = new Media(libvlc, Uri.parse(contentUri));
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<TVGuideNewActivity> mOwner;

        public MediaPlayerListener(TVGuideNewActivity owner) {
            mOwner = new WeakReference<TVGuideNewActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            TVGuideNewActivity player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releaseMediaPlayer();
                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    break;
                case MediaPlayer.Event.Buffering:
                    break;
                case MediaPlayer.Event.EncounteredError:
                    player.def_lay.setVisibility(View.VISIBLE);
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
