package com.newlegacyxc.activity.live;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.OSDDlg;
import com.newlegacyxc.dialog.PackageDlg;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;
import com.newlegacyxc.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.newlegacyxc.apps.Constants.catchupFormat;

public class LivePlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener, IVLCVout.Callback,View.OnClickListener, IVLCVout.OnNewVideoLayoutListener, View.OnFocusChangeListener {
    private static final String TAG = "LivePlayActivity";
    private MediaPlayer mMediaPlayer = null;
    public int mHeight;
    public int mWidth;
    LibVLC libvlc;
    private SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    SeekBar seekbar;
    LinearLayout ly_bottom, def_lay,ly_resolution,ly_audio,ly_subtitle,ly_fav,ly_audio_delay;
    LinearLayout btn_play;
    LinearLayout btn_rewind;
    LinearLayout btn_forward;
    private List<EPGEvent> epgModelList;

    MediaPlayer.TrackDescription[] tracks;
    MediaPlayer.TrackDescription[] subTracks;
    String ratio;
    String[] resolutions;
    int current_resolution = 0;
    boolean first = true;

    TextView txt_title,txt_dec,channel_title,txt_date,txt_time_passed,txt_remain_time,txt_last_time,txt_current_dec,txt_next_dec;
    ImageView image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker,delayTicker;
    private OSDDlg osdDlg;
    private long delay_time;
    Handler delay_handler = new Handler();
    String cont_url,title,rss="",start_time="";
    int duration_time = 0,selected_item = 0,msg_time = 0,mStream_id = 0;
    long start_mil;
    long now_mil;
    long current_mil;
    boolean is_create = true,is_long=false;
    boolean is_rss = false,is_msg = false, is_live = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private RelativeLayout lay_header;
    private FrameLayout mVideoSurfaceFrame = null;
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_BEST_FIT;
    SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd HH:mm");
    EPGChannel epgModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_player);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list1)));

        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        lay_header = findViewById(R.id.lay_header);
        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = () -> updateVideoSurfaces();

                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(view -> {
            if(ly_bottom.getVisibility()== View.VISIBLE){
                ly_bottom.setVisibility(View.GONE);
            }else {
                ly_bottom.setVisibility(View.VISIBLE);
                updateTimer();
            }
        });
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.RGBX_8888);
        def_lay = findViewById(R.id.def_lay);
        ly_bottom = findViewById(R.id.ly_bottom);

        remote_subtitles_surface = findViewById(R.id.remote_subtitles_surface);
        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        btn_play = findViewById(R.id.btn_play);
        btn_forward = findViewById(R.id.btn_forward);
        btn_rewind = findViewById(R.id.btn_rewind);

        btn_play.setOnClickListener(this);
        btn_play.setOnFocusChangeListener(this);
        btn_rewind.setOnClickListener(this);
        btn_rewind.setOnFocusChangeListener(this);
        btn_forward.setOnClickListener(this);
        btn_forward.setOnFocusChangeListener(this);


        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);
        ly_fav = findViewById(R.id.ly_fav);
        ly_audio = findViewById(R.id.ly_audio);
        ly_audio_delay = findViewById(R.id.ly_audio_delay);

        ly_fav.setOnClickListener(this);
        ly_subtitle.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_audio.setOnClickListener(this);
        ly_audio_delay.setOnClickListener(this);

        ly_resolution.setOnFocusChangeListener(this);
        ly_subtitle.setOnFocusChangeListener(this);
        ly_audio.setOnFocusChangeListener(this);
        ly_fav.setOnFocusChangeListener(this);

        btn_rewind.setNextFocusRightId(R.id.btn_play);
        btn_play.setNextFocusRightId(R.id.btn_forward);
        btn_play.setNextFocusLeftId(R.id.btn_rewind);
        btn_forward.setNextFocusLeftId(R.id.btn_play);

        btn_rewind.setNextFocusDownId(R.id.ly_fav);
        btn_play.setNextFocusDownId(R.id.ly_fav);
        btn_forward.setNextFocusDownId(R.id.ly_fav);

        ly_fav.setNextFocusUpId(R.id.btn_play);
        ly_resolution.setNextFocusUpId(R.id.btn_play);
        ly_subtitle.setNextFocusUpId(R.id.btn_play);
        ly_audio.setNextFocusUpId(R.id.btn_play);

        seekbar = findViewById(R.id.seekbar);
        seekbar.setMax(100);
        txt_title = findViewById(R.id.txt_title);
        txt_dec = findViewById(R.id.txt_dec);
        channel_title = findViewById(R.id.channel_title);
        txt_date = findViewById(R.id.txt_date);
        txt_time_passed = findViewById(R.id.txt_time_passed);
        txt_remain_time = findViewById(R.id.txt_remain_time);
        txt_last_time = findViewById(R.id.txt_last_time);
        txt_current_dec = findViewById(R.id.txt_current_dec);
        txt_next_dec = findViewById(R.id.txt_next_dec);

        mStream_id = getIntent().getIntExtra("stream_id",0);
        is_live = getIntent().getBooleanExtra("is_live",false);
        epgModel = MyApp.epgChannel;
        if(is_live){
            new Thread(this::getEpg).start();
        }else {
            start_mil = getIntent().getLongExtra("start_mil",0);
            now_mil = getIntent().getLongExtra("now_mil",0);
            duration_time = getIntent().getIntExtra("duration",0);
        }
        txt_title.setText(getIntent().getStringExtra("title"));
        txt_dec.setText(getIntent().getStringExtra("dec"));
        channel_title.setText(getIntent().getStringExtra("channel_title"));
        txt_current_dec.setText(getIntent().getStringExtra("current_dec"));
        txt_next_dec.setText(getIntent().getStringExtra("next_dec"));

        cont_url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");


        txt_rss = findViewById(R.id.txt_rss);
        txt_rss.setSingleLine(true);

        image_icon = findViewById(R.id.image_icon);
        if (!Constants.GetIcon(this).equals(""))
            Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image_icon);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        mHeight = displayMetrics.heightPixels;
        mWidth = displayMetrics.widthPixels;

        ratio = MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT;
        resolutions =  new String[]{"16:9", "4:3", ratio};

        Log.e(TAG,epgModel.is_favorite()+"");
        if (epgModel.is_favorite()) {
            ly_fav.setBackgroundResource(R.drawable.star);
        } else {
            ly_fav.setBackgroundResource(R.drawable.star_white);
        }
        playVideo(cont_url);
        FullScreencall();
//        getRespond();
    }

    @Override
    public void onFocusChange(View v, boolean focused) {
        switch (v.getId()){
            case R.id.btn_play:
                if(focused){
                    btn_play.setBackgroundResource(R.drawable.play_channel1);
                }else {
                    btn_play.setBackgroundResource(R.drawable.play_channel);
                }
                break;
            case R.id.btn_forward:
                if(focused){
                    btn_forward.setBackgroundResource(R.drawable.forward1);
                }else {
                    btn_forward.setBackgroundResource(R.drawable.forward);
                }
                break;
            case R.id.btn_rewind:
                if(focused){
                    btn_rewind.setBackgroundResource(R.drawable.rewind1);
                }else {
                    btn_rewind.setBackgroundResource(R.drawable.rewind);
                }
                break;
            case R.id.ly_audio:
                if(focused){
                    ly_audio.setBackgroundResource(R.drawable.ic_music_video_black_24dp1);
                }else {
                    ly_audio.setBackgroundResource(R.drawable.ic_music_video_black_24dp);
                }
                break;
            case R.id.ly_subtitle:
                if(focused){
                    ly_subtitle.setBackgroundResource(R.drawable.ic_subtitles_black_24dp1);
                }else {
                    ly_subtitle.setBackgroundResource(R.drawable.ic_subtitles_black_24dp);
                }
                break;
            case R.id.ly_resolution:
                if(focused){
                    ly_resolution.setBackgroundResource(R.drawable.ic_switch_video_black_24dp1);
                }else {
                    ly_resolution.setBackgroundResource(R.drawable.ic_switch_video_black_24dp);
                }
                break;
            case R.id.ly_fav:
                if(focused) addFav();
                break;
        }

    }

//    private void getRespond(){
//        if (!MyApp.is_announce_enabled) return;
//        String url = "";
//        switch (MyApp.firstServer){
//            case first:
//                url= Constants.GetUrl1(this);
//                break;
//            case second:
//                url=Constants.GetUrl2(this);
//                break;
//            case third:
//                url=Constants.GetUrl3(this);
//                break;
//        }
//        try{
//            String response = MyApp.instance.getIptvclient().login(url);
//            MyApp.instance.getIptvclient().setUrl(serverUrl);
//            Log.e("response",response);
//            try {
//                JSONObject object = new JSONObject(response);
//                if (object.getBoolean("status")) {
//                    JSONObject data_obj = object.getJSONObject("data");
//                    String msg=data_obj.getString("message");
//                    try {
//                        msg_time = Integer.parseInt(data_obj.getString("message_time"));
//                    }catch (Exception e){
//                        msg_time = 20;
//                    }
//                    is_msg = !data_obj.getString("message_on_off").isEmpty() && data_obj.getString("message_on_off").equalsIgnoreCase("1");
//                    if (msg.equals("")) msg=getString(R.string.app_name);
//                    String finalMsg = msg;
//                    runOnUiThread(()->{
//                        String rss_feed = "                 "+ finalMsg +"                 ";
//                        if(rss.equalsIgnoreCase(rss_feed)){
//                            lay_header.setVisibility(View.GONE);
////                            image_icon.setVisibility(View.GONE);
////                            txt_rss.setVisibility(View.GONE);
//                            is_rss = false;
//                        }else {
//                            rss =rss_feed;
//                            is_rss = true;
//                            lay_header.setVisibility(View.VISIBLE);
//                        }
//
//                        if(is_msg){
//                            lay_header.setVisibility(View.VISIBLE);
//                            txt_rss.setText(rss);
//                            Animation bottomToTop = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
//                            txt_rss.clearAnimation();
//                            txt_rss.startAnimation(bottomToTop);
//                        }else {
//                            lay_header.setVisibility(View.GONE);
//                        }
//                        rssTimer();
//                    });
//                } else {
//                    Toast.makeText(this, "Server Error!", Toast.LENGTH_SHORT).show();
//                }
//            }catch (JSONException e){
//                e.printStackTrace();
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//    int rss_time;
//    private void rssTimer() {
//        rss_time = msg_time;
//        rssTicker = new Runnable() {
//            public void run() {
//                if (rss_time < 1) {
//                    lay_header.setVisibility(View.GONE);
//                    return;
//                }
//                runRssTicker();
//            }
//        };
//        rssTicker.run();
//    }
//
//    private void runRssTicker() {
//        rss_time --;
//        long next = SystemClock.uptimeMillis() + 1000;
//        rssHandler.postAtTime(rssTicker, next);
//    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceView.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surfaceView.setLayoutParams(lp);
        if (remote_subtitles_surface != null)
            remote_subtitles_surface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        surfaceView.invalidate();
        if (remote_subtitles_surface != null)
            remote_subtitles_surface.invalidate();
    }

    private void playVideo(String path) {
        if(def_lay.getVisibility()==View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
//        Log.e("url",path);
        toggleFullscreen(true);
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

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);


            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);

            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.setWindowSize(mWidth, mHeight);
            vout.addCallback(this);
            vout.attachViews(this);
//            vout.setSubtitlesView(tv_subtitle);


            Log.e("VideoPlay",path);
            Media m = new Media(libvlc, Uri.parse(path));
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();
            if(is_live){
                updateProgressBarLive();
            }else {
                updateProgressBar();
            }
            updateTimer();
        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }

    }


    public void updateProgressBar() {
        if (epgModel.is_favorite()) {
            ly_fav.setBackgroundResource(R.drawable.star);
        } else {
            ly_fav.setBackgroundResource(R.drawable.star_white);
        }
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mMediaPlayer != null) {
                if (tracks == null && subTracks == null) {
                    first = false;
                    tracks = mMediaPlayer.getAudioTracks();
                    subTracks = mMediaPlayer.getSpuTracks();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm a EEE MM/dd");
                txt_date.setText(dateFormat.format(new Date()));
                current_mil = System.currentTimeMillis();
                int pass_min = (int) ((current_mil - now_mil)/(1000*60));
                int remain_min = (duration_time/(60)) - pass_min;
                int progress;
                if (duration_time!=0) progress = pass_min * 100/(pass_min+remain_min);
                else progress = 0;
                txt_last_time.setText(getFromDate1(now_mil + duration_time*1000));
                seekbar.setProgress(progress);
                txt_time_passed.setText("Started " + pass_min +" mins ago");
                txt_remain_time.setText("+"+remain_min+"min");
                mHandler.postDelayed(this, 500);
            }
        }
    };

    private void updateProgressBarLive() {
        if (epgModel.is_favorite()) {
            ly_fav.setBackgroundResource(R.drawable.star);
        } else {
            ly_fav.setBackgroundResource(R.drawable.star_white);
        }
        mHandler.removeCallbacks(mUpdateTimeTask_live);
        mHandler.postDelayed(mUpdateTimeTask_live, 0);
    }

    private Runnable mUpdateTimeTask_live = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if (surfaceView != null) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm a EEE MM/dd");
                Calendar now = Calendar.getInstance();
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                long nowLocalTimeStamp = now.getTimeInMillis();
                if(epgModelList!=null && epgModelList.size()>0){
                    try {
                        EPGEvent epgEvent = epgModelList.get(0);
                        start.setTime(epgEvent.getStartTime());
                        end.setTime(epgEvent.getEndTime());
                        long startStamp =start.getTime().getTime();
                        long endStamp = end.getTime().getTime();
                        if(nowLocalTimeStamp>startStamp){
                            txt_title.setText(epgEvent.getTitle());
                            txt_dec.setText(epgEvent.getDec());
                            try {
                                txt_title.setText(title);
                            }catch (Exception e1){
                                txt_title.setText("    ");
                            }
                            txt_date.setText(dateFormat.format(new Date()));
                            int pass_min = (int) ((nowLocalTimeStamp - startStamp)/(1000*60));
                            int remain_min = (int)(endStamp-nowLocalTimeStamp)/(1000*60);
                            int progress = (int) pass_min*100/(int)((epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/60/1000);
                            seekbar.setProgress(progress);
                            txt_time_passed.setText("Started " + pass_min +" mins ago");
                            txt_remain_time.setText("+"+remain_min+" min");
                            txt_last_time.setText(sdf.format(new Date(endStamp)));
                            txt_current_dec.setText(epgModelList.get(Constants.findNowEvent(epgModelList)).getTitle());
                            txt_next_dec.setText(epgModelList.get(Constants.findNowEvent(epgModelList)+1).getTitle());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    txt_title.setText("No Information");
                    txt_dec.setText("No Information");
                    try {
                        txt_title.setText(title);
                    }catch (Exception e2){
                        txt_title.setText("    ");
                    }
                    txt_date.setText(dateFormat.format(new Date()));
                    txt_time_passed.setText("      mins ago");
                    txt_remain_time.setText("      min");
                    txt_last_time.setText("         ");
                    seekbar.setProgress(0);
                    txt_current_dec.setText("No Information");
                    txt_next_dec.setText("No Information");
                }
            }
            mHandler.postDelayed(this, 500);
        }
    };

    private String getFromDate1(long millisecond){
        Date date = new Date();
        date.setTime(millisecond);
        String formattedDate=sdf1.format(date);
        return formattedDate;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (libvlc != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            surfaceView = findViewById(R.id.surface_view);
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.RGBX_8888);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int SCREEN_HEIGHT = displayMetrics.heightPixels;
            int SCREEN_WIDTH = displayMetrics.widthPixels;
            holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            mHeight = displayMetrics.heightPixels;
            mWidth = displayMetrics.widthPixels;
            playVideo(cont_url);
        } else {
            is_create = false;
        }
    }
    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("AUDIO_TRACK", 0);
        editor.commit();

        SharedPreferences pref2 = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = pref2.edit();
        editor1.putInt("SUB_TRACK", 0);
        releaseMediaPlayer();
    }
    @Override
    protected void onUserLeaveHint()
    {
        releaseMediaPlayer();
//        finish();
        super.onUserLeaveHint();
    }
    private void releaseMediaPlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
//        mMediaPlayer.release();
        holder = null;
        libvlc.release();
        libvlc = null;

        mWidth = 0;
        mHeight = 0;
    }
    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }


    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<LivePlayActivity> mOwner;

        public MediaPlayerListener(LivePlayActivity owner) {
            mOwner = new WeakReference<LivePlayActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            LivePlayActivity player = mOwner.get();
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releaseMediaPlayer();
                    player.is_create = false;
                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
//                    Toast.makeText(player, "Playing", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
//                    Toast.makeText(player, "Stop", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Buffering:
//                    Toast.makeText(player, "Buffering", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.EncounteredError:
//                    Toast.makeText(player, "Error", Toast.LENGTH_SHORT).show();
                    player.def_lay.setVisibility(View.VISIBLE);
                    break;

                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    //Log.d(TAG, "PositionChanged");
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    private void delayTimer(){
        delay_handler.removeCallbacks(delayTicker);
        updateDelayTimer();
    }
    int delay_max;
    private void updateDelayTimer(){
        delay_max = 5;
        delayTicker = () -> {
            Log.e("delay_time",String.valueOf(delay_max));
            if(delay_max<1){
                if(osdDlg.isShowing()){
                    osdDlg.dismiss();
                }
                return;
            }
            runNextDelayTicker();
        };
        delayTicker.run();
    }

    private void runNextDelayTicker(){
        delay_max--;
        long next = SystemClock.uptimeMillis() + 1000;
        delay_handler.postAtTime(delayTicker, next);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                if (tracks != null) {
                    if (tracks.length > 0) {
                        showAudioTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.ly_subtitle:
                if (subTracks != null) {
                    if (subTracks.length > 0) {
                        showSubTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.ly_resolution:
                current_resolution++;
                if (current_resolution == resolutions.length)
                    current_resolution = 0;

                mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                break;

            case R.id.btn_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
//                    btn_play.setBackgroundResource(R.drawable.exo_play);
                } else {
                    mMediaPlayer.play();
//                    btn_play.setBackgroundResource(R.drawable.exo_pause);
                }
                break;
            case R.id.ly_audio_delay:
                osdDlg = new OSDDlg(this, delay_time / 1000, (dialog, episode_num) -> {
                    if(mMediaPlayer!=null){
                        delay_time = episode_num*1000;
                        mMediaPlayer.setAudioDelay(delay_time);
                    }
                });
                osdDlg.show();
                delayTimer();
                break;
            case R.id.ly_fav:
                break;
            case R.id.btn_rewind:
                if(surfaceView.getVisibility()==View.VISIBLE && !is_live){
                    mHandler.removeCallbacks(mTicker);
                    Date date1 = new Date();
                    if (date1.getTime() > now_mil + 60 * 1000) {
                        now_mil += 60 * 1000;
                        if (libvlc != null) {
                            releaseMediaPlayer();
                            surfaceView = null;
                        }
                        surfaceView = findViewById(R.id.surface_view);
                        start_mil = start_mil-60*1000;
                        start_time = getFromCatchDate(start_mil);
                        duration_time+=60;
                        cont_url = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass, mStream_id+"",start_time, duration_time);
                        playVideo(cont_url);
                        ly_bottom.setVisibility(View.VISIBLE);
                        updateProgressBar();
                        startTimer();
                    }
                }
                break;
            case R.id.btn_forward:
                if(surfaceView.getVisibility()==View.VISIBLE && !is_live){
                    mHandler.removeCallbacks(mTicker);
                    now_mil -= 60*1000;
                    if (libvlc != null) {
                        releaseMediaPlayer();
                        surfaceView = null;
                    }
                    surfaceView = findViewById(R.id.surface_view);
                    start_mil = start_mil+60*1000;
                    start_time = getFromCatchDate(start_mil);
                    duration_time-=60;
                    cont_url = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass, mStream_id+"",start_time, duration_time);
                    playVideo(cont_url);
                    surfaceView.setVisibility(View.VISIBLE);
                    ly_bottom.setVisibility(View.VISIBLE);
                    updateProgressBar();
                    startTimer();
                }
                break;
        }
    }

    private String getFromCatchDate(long millisecond){
        Date date = new Date();
        date.setTime(millisecond);
        String formattedDate=catchupFormat.format(date);
        return formattedDate;
    }

    private void getEpg(){
        try {
            String map = MyApp.instance.getIptvclient().getShortEPG(MyApp.user,MyApp.pass,
                    mStream_id+"",4);
            Log.e(getClass().getSimpleName(),map);
            Gson gson=new Gson();
            map=map.replaceAll("[^\\x00-\\x7F]", "");
            if (!map.contains("null_error_response")){
                Log.e("response",map);
                try {
                    JSONObject jsonObject= new JSONObject(map);
                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
                    epgModelList = new ArrayList<>();
                    epgModelList.addAll(gson.fromJson(jsonArray.toString(), new TypeToken<List<EPGEvent>>(){}.getType()));
                    if (is_live){
                        try {
                            txt_current_dec.setText(epgModelList.get(Constants.findNowEvent(epgModelList)).getTitle());
                            txt_dec.setText(epgModelList.get(Constants.findNowEvent(epgModelList)).getDec());
                            try {
                                txt_next_dec.setText(epgModelList.get(Constants.findNowEvent(epgModelList)+1).getTitle());
                            }catch (Exception e){
                                txt_next_dec.setText("No information");
                            }
                        }catch (Exception e){
                            txt_dec.setText("No information");
                            txt_current_dec.setText("No information");
                            txt_next_dec.setText("No information");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        long totalDuration = mMediaPlayer.getLength();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mMediaPlayer.setTime(currentPosition);
        if(is_live){
            updateProgressBarLive();
        }else {
            updateProgressBar();
        }
    }


    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }


    private void updateTimer() {
        handler.removeCallbacks(mTicker);
        startTimer();
    }

    int maxTime;
    private void startTimer() {
        maxTime = 10;
        mTicker = new Runnable() {
            public void run() {
                if (maxTime < 1) {
                    if (ly_bottom.getVisibility() == View.VISIBLE)
                        ly_bottom.setVisibility(View.GONE);
                    return;
                }
                runNextTicker();
            }
        };
        mTicker.run();
    }

    private void runNextTicker() {
        maxTime --;
        long next = SystemClock.uptimeMillis() + 1000;
        handler.postAtTime(mTicker, next);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (ly_bottom.getVisibility() == View.GONE) ly_bottom.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if(ly_bottom.getVisibility()==View.VISIBLE){
                            ly_bottom.setVisibility(View.GONE);
                            return true;
                        }
                        releaseMediaPlayer();
                        finish();
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        if (epgModel.is_favorite()) {
                            pkg_datas.set(0, "Remove from Fav");
                        }else pkg_datas.set(0, "Add to Fav");
                        PackageDlg packageDlg = new PackageDlg(LivePlayActivity.this, pkg_datas, (dialog, position) -> {
                            dialog.dismiss();
                            is_long = false;
                            switch (position) {
                                case 0:
                                    addFav();
                                    break;
                                case 1:
                                    if (subTracks != null) {
                                        if (subTracks.length > 0) {
                                            showSubTracksList();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                case 2:
                                    if (tracks != null) {
                                        if (tracks.length > 0) {
                                            showAudioTracksList();
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                    }
                                    break;
                                case 3:
                                    showResolutionDlg();
                                    break;
                                case 4:
                                    osdDlg = new OSDDlg(LivePlayActivity.this,  delay_time/1000, (dialog1, episode_num) -> {
                                        if(mMediaPlayer!=null){
                                            delay_time = episode_num*1000;
                                            mMediaPlayer.setAudioDelay(delay_time);
                                        }
                                    });
                                    osdDlg.show();
                                    delayTimer();
                                    break;
                            }
                        });
                        packageDlg.show();
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return super.dispatchKeyEvent(event);
    }

    private void addFav(){
        if (epgModel.is_favorite()) {
            pkg_datas.set(0, "Add to Fav");
            epgModel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i < Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().size(); i++) {
                if (Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().get(i).getName().equals(epgModel.getName())) {
                    is_exist = true;
                    pp = i;
                }
            }
            if (is_exist)
                Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().remove(pp);
            MyApp.instance.getPreference().put(Constants.getFavChannelNames(), Constants.getListStrFromListEpg(Constants.getFavFullModel(MyApp.fullModels_filter).getChannels()));
            Toast.makeText(this,"This movie has been removed from favorites.", Toast.LENGTH_SHORT).show();
            ly_fav.setBackgroundResource(R.drawable.star_white);
        } else {
            epgModel.setIs_favorite(true);
            Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().add(epgModel);
            MyApp.instance.getPreference().put(Constants.getFavChannelNames(), Constants.getListStrFromListEpg(Constants.getFavFullModel(MyApp.fullModels_filter).getChannels()));
            Toast.makeText(this,"This movie has been added to favorites.", Toast.LENGTH_SHORT).show();
            pkg_datas.set(0, "Remove from Fav");
            ly_fav.setBackgroundResource(R.drawable.star);
        }
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else  {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void showAudioTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LivePlayActivity.this);
        builder.setTitle("Audio track");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < tracks.length; i++) {
            names.add(tracks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("AUDIO_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("AUDIO_TRACK", selected_item);
                editor.commit();

                mMediaPlayer.setAudioTrack(tracks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSubTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LivePlayActivity.this);
        builder.setTitle("Subtitle");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < subTracks.length; i++) {
            names.add(subTracks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("SUB_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("SUB_TRACK", selected_item);
                editor.commit();
                mMediaPlayer.setSpuTrack(subTracks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showResolutionDlg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LivePlayActivity.this);
        builder.setTitle("Select Screen Mode");
        String[] screen_mode_list = {"16:9", "4:3", "Full Screen"};
        builder.setSingleChoiceItems(screen_mode_list, current_resolution,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        current_resolution=which;
                    }
                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                                                current_resolution=which;
                mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
