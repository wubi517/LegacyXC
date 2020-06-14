package com.newlegacyxc.activity.live;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.PackageDlg;
import com.newlegacyxc.ijklib.widget.media.AndroidMediaController;
import com.newlegacyxc.ijklib.widget.media.IjkVideoView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.newlegacyxc.apps.Constants.catchupFormat;


public class LiveIjkPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener,View.OnClickListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnCompletionListener,
        View.OnFocusChangeListener{
    private static final String TAG = "LivePlayActivity";
    private IjkVideoView surfaceView;
    private AndroidMediaController mMediaController;
    private TextView txt_num;
    private TableLayout mHudView;
    SeekBar seekbar;
    LinearLayout ly_bottom, def_lay,ly_resolution,ly_audio,ly_subtitle,ly_fav;
    LinearLayout btn_play;
    LinearLayout btn_rewind;
    LinearLayout btn_forward;
    private List<EPGEvent> epgModelList;
    TextView txt_title,txt_dec,channel_title,txt_date,txt_time_passed,txt_remain_time,txt_last_time,txt_current_dec,txt_next_dec;
    ImageView image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,rss="",start_time="";
    int duration_time = 0,msg_time = 0,mStream_id = 0;
    long start_mil;
    long now_mil;
    long current_mil;
    boolean is_create = true;
    boolean is_rss = false,is_msg = false, is_live = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private RelativeLayout lay_header;
    private FrameLayout mVideoSurfaceFrame = null;
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;
    SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd HH:mm");
    EPGChannel epgModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_ijk_player);
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
        mMediaController = new AndroidMediaController(this, false);
        txt_num = findViewById(R.id.toast_text_view);
        mHudView = findViewById(R.id.hud_view);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list1)));

        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        lay_header = findViewById(R.id.lay_header);
        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                    }
                };

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
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ly_bottom.getVisibility()== View.VISIBLE){
                    ly_bottom.setVisibility(View.GONE);
                }else {
                    ly_bottom.setVisibility(View.VISIBLE);
                    updateTimer();
                }
            }
        });

        def_lay = findViewById(R.id.def_lay);
        ly_bottom = findViewById(R.id.ly_bottom);

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

        ly_fav.setOnClickListener(this);
        ly_subtitle.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_audio.setOnClickListener(this);

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

        txt_title.setText(getIntent().getStringExtra("title"));
        cont_url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        is_live = getIntent().getBooleanExtra("is_live",false);

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


    private void playVideo(String path) {
        if(def_lay.getVisibility()==View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
        toggleFullscreen(true);
        try {
            surfaceView.setMediaController(mMediaController);
            surfaceView.setHudView(mHudView);
            mMediaController.hide();
            surfaceView.setVideoPath(path);
            surfaceView.setOnCompletionListener(this);
            surfaceView.setOnErrorListener(this);
            surfaceView.start();
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
            if (surfaceView != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm a EEE MM/dd");
                txt_date.setText(dateFormat.format(new Date()));
                current_mil = System.currentTimeMillis();
                int pass_min = (int) ((current_mil - now_mil)/(1000*60));
                int remain_min = duration_time/60 - pass_min;
                int progress;
                if (duration_time!=0) progress = pass_min * 100/(pass_min+remain_min);
                else progress = 0;
                txt_last_time.setText(getFromDate1(now_mil + duration_time/60*60*1000));
                seekbar.setProgress(progress);
                txt_time_passed.setText("Started " + pass_min +" mins ago");
                txt_remain_time.setText("+"+remain_min+"min");
                mHandler.postDelayed(this, 500);
            }
        }
    };

    private String getFromDate1(long millisecond){
        Date date = new Date();
        date.setTime(millisecond);
        String formattedDate=sdf1.format(date);
        return formattedDate;
    }

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
    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (surfaceView != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
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
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
    @Override
    protected void onUserLeaveHint()
    {
        releaseMediaPlayer();
        finish();
        super.onUserLeaveHint();
    }
    private void releaseMediaPlayer() {
        if (surfaceView == null)
            return;
        surfaceView.release(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                break;
            case R.id.ly_subtitle:
                break;
            case R.id.ly_resolution:
                surfaceView.toggleAspectRatio();
                break;
            case R.id.btn_play:
                if (surfaceView.isPlaying()) {
                    surfaceView.pause();
                } else {
                    surfaceView.start();
                }
                break;
            case R.id.ly_fav:
                break;
            case R.id.btn_rewind:
                if(surfaceView.getVisibility()==View.VISIBLE && !is_live){
                    mHandler.removeCallbacks(mTicker);
                    Date date1 = new Date();
                    if (date1.getTime() > now_mil + 60 * 1000) {
                        now_mil += 60 * 1000;
                        if (surfaceView != null) {
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
                        if(is_live){
                            updateProgressBarLive();
                        }else {
                            updateProgressBar();
                        }
                        startTimer();
                    }
                }
                break;
            case R.id.btn_forward:
                if(surfaceView.getVisibility()==View.VISIBLE && !is_live){
                    mHandler.removeCallbacks(mTicker);
                    now_mil -= 60*1000;
                    if (surfaceView != null) {
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
                    if(is_live){
                        updateProgressBarLive();
                    }else {
                        updateProgressBar();
                    }
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        long totalDuration = surfaceView.getDuration();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        surfaceView.seekTo(currentPosition);
//        updateProgressBar();
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
                        if (ly_bottom.getVisibility() == View.GONE){
                            ly_bottom.setVisibility(View.VISIBLE);
                            btn_play.requestFocus();
                        }
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
                        PackageDlg packageDlg = new PackageDlg(this, pkg_datas, (dialog, position) -> {
                            dialog.dismiss();
                            switch (position) {
                                case 0:
                                    addFav();
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 3:
                                    break;
                            }
                        });
                        packageDlg.show();
                        break;
                }
            }
        }catch (Exception e){

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

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        releaseMediaPlayer();
        onResume();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        releaseMediaPlayer();
        def_lay.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean focused) {
        Log.e("focue",""+focused);
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
}
