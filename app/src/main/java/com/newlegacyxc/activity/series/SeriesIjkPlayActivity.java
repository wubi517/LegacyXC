package com.newlegacyxc.activity.series;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
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

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.PackageDlg;
import com.newlegacyxc.ijklib.widget.media.AndroidMediaController;
import com.newlegacyxc.ijklib.widget.media.IjkVideoView;
import com.newlegacyxc.models.EpisodeModel;
import com.newlegacyxc.utils.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class SeriesIjkPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener, View.OnClickListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnCompletionListener{
    public int mHeight;
    public int mWidth;
    private IjkVideoView surfaceView;
    private AndroidMediaController mMediaController;
    private TextView txt_num;
    private TableLayout mHudView;
    private FrameLayout mVideoSurfaceFrame = null;

    LinearLayout ly_play,ly_resolution,ly_audio,ly_subtitle;
    RelativeLayout ly_header;
    ImageView img_lock,img_play;

    boolean first = true;
    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay;
    TextView title_txt, start_txt, end_txt;
    ImageView imageView,image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,stream_id,img,name,rss="";
    int dration_time = 0,pos,position,selected_item = 0,msg_time = 0;
    List<EpisodeModel> movieModels;
    boolean is_create = true;
    List<String> pkg_datas;
    boolean is_long =false,is_rss = false,is_msg = false;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_ijk_player);
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
        for (int i = 0; i < getResources().getStringArray(R.array.package_list2).length; i++) {
            pkg_datas.add(getResources().getStringArray(R.array.package_list2)[i]);
        }

        pos = MyApp.episode_pos;
        movieModels = MyApp.episodeModels;
        stream_id = movieModels.get(pos).getStream_id();
        img = movieModels.get(pos).getEpisodeInfoModel().getMovie_image();
        name =movieModels.get(pos).getTitle();
        String type = movieModels.get(pos).getContainer_extension();

        def_lay =  findViewById(R.id.def_lay);
        bottom_lay = findViewById(R.id.vod_bottom_lay);
        title_txt = findViewById(R.id.vod_channel_title);
        imageView =  findViewById(R.id.vod_channel_img);
        start_txt =  findViewById(R.id.vod_start_time);
        end_txt =  findViewById(R.id.vod_end_time);
        seekBar =  findViewById(R.id.vod_seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        surfaceView = findViewById(R.id.surface_view);

        ly_header = findViewById(R.id.ly_header);
        ly_audio = findViewById(R.id.ly_audio);
        ly_play = findViewById(R.id.ly_play);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);

        ly_play.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_subtitle.setOnClickListener(this);
        ly_audio.setOnClickListener(this);

        img_play = findViewById(R.id.img_play);

        txt_rss = findViewById(R.id.txt_rss);
        txt_rss.setSingleLine(true);
        image_icon = findViewById(R.id.image_icon);
        if (!Constants.GetIcon(this).equals(""))
            Picasso.with(this).load(Constants.GetIcon(this))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image_icon);

        title_txt.setText(name);
        cont_url = MyApp.instance.getIptvclient().buildSeriesStreamURL(MyApp.user,MyApp.pass,stream_id,type);
        title = name;
        try {
            Picasso.with(this).load(getIntent().getStringExtra("img"))
                    .placeholder(R.drawable.icon_default)
                    .error(R.drawable.icon_default)
                    .into(imageView);

        }catch (Exception e){
            Picasso.with(this).load(R.drawable.icon_default).into(imageView);
        }
        playVideo(cont_url);
        FullScreencall();
//        new Thread(this::getRespond).start();
    }

//    private void getRespond(){
//        String url = "";
//        switch (MyApp.firstServer){
//            case first:
//                url=Constants.GetUrl1(this);
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
//                            ly_header.setVisibility(View.GONE);
////                            image_icon.setVisibility(View.GONE);
////                            txt_rss.setVisibility(View.GONE);
//                            is_rss = false;
//                        }else {
//                            rss =rss_feed;
//                            is_rss = true;
//                            ly_header.setVisibility(View.VISIBLE);
//                        }
//
//                        if(is_msg){
//                            ly_header.setVisibility(View.VISIBLE);
//                            txt_rss.setText(rss);
//                            Animation bottomToTop = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
//                            txt_rss.clearAnimation();
//                            txt_rss.startAnimation(bottomToTop);
//                        }else {
//                            ly_header.setVisibility(View.GONE);
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
//
//        }
//
//    }
//    int rss_time;
//    private void rssTimer() {
//        rss_time = msg_time;
//        rssTicker = () -> {
//            if (rss_time < 1) {
//                txt_rss.setText("");
//                txt_rss.setBackgroundResource(R.color.trans_parent);
//                ly_header.setVisibility(View.GONE);
//                image_icon.setVisibility(View.GONE);
//                return;
//            }
//            runRssTicker();
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
        if(def_lay.getVisibility()== View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
        Log.e("url",path);
        toggleFullscreen(true);
        try {
            surfaceView.setMediaController(mMediaController);
            surfaceView.setHudView(mHudView);
            mMediaController.hide();
            surfaceView.setVideoPath(path);
            surfaceView.setOnCompletionListener(this);
            surfaceView.setOnErrorListener(this);
            surfaceView.start();
//            mediaSeekTo();
            updateProgressBar();
            updateTimer();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (surfaceView!=null && surfaceView.isPlaying()) {
                long totalDuration = surfaceView.getDuration();
                long currentDuration = surfaceView.getCurrentPosition();
                end_txt.setText("" + Utils.milliSecondsToTimer(totalDuration));
                start_txt.setText("" + Utils.milliSecondsToTimer(currentDuration));
                int progress = (int) (Utils.getProgressPercentage(currentDuration, totalDuration));
                seekBar.setProgress(progress);
                mHandler.postDelayed(this, 500);
            }
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
            surfaceView = findViewById(R.id.surface_view);
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

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }
    private void releaseMediaPlayer() {

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

            case R.id.ly_play:
                if (surfaceView.isPlaying()) {
                    surfaceView.pause();
                    img_play.setImageResource(R.drawable.exo_play);
                } else {
                    surfaceView.start();
                    img_play.setImageResource(R.drawable.exo_pause);
                }
                if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
                break;
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
        long totalDuration = surfaceView.getDuration();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        surfaceView.seekTo(currentPosition);
        updateProgressBar();
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
                    if (bottom_lay.getVisibility() == View.VISIBLE)
                        bottom_lay.setVisibility(View.GONE);
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
            long curr_pos = surfaceView.getCurrentPosition();
            long max_pos = surfaceView.getDuration();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (surfaceView.isPlaying()) {
                            surfaceView.pause();
                            img_play.setImageResource(R.drawable.exo_play);
                        } else {
                            surfaceView.start();
                            img_play.setImageResource(R.drawable.exo_pause);
                        }
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        dration_time += 30;
                        if (curr_pos < dration_time * 1000)
                            surfaceView.seekTo(1);
                        else {
                            int st = (int) (curr_pos - (long) dration_time * 1000);
                            surfaceView.seekTo(st);
                        }
                        dration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        dration_time += 30;
                        if (max_pos < dration_time * 1000)
                            surfaceView.seekTo((int) (max_pos - 10));
                        else surfaceView.seekTo((int) (curr_pos + (long) dration_time * 1000));
                        dration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if(bottom_lay.getVisibility()== View.VISIBLE){
                            bottom_lay.setVisibility(View.GONE);
                            return true;
                        }
                        releaseMediaPlayer();
                        finish();
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        PackageDlg packageDlg = new PackageDlg(SeriesIjkPlayActivity.this, pkg_datas, new PackageDlg.DialogPackageListener() {
                            @Override
                            public void OnItemClick(Dialog dialog, int position) {
                                dialog.dismiss();
                                is_long = false;
                                switch (position) {
                                    case 0:

                                        break;
                                    case 1:

                                        break;
                                    case 2:
                                        surfaceView.toggleAspectRatio();
                                        break;
                                }
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
        if(pos<movieModels.size()-1){
            MyApp.episode_pos = pos+1;
            pos = pos+1;
            recreate();
        }else {
            releaseMediaPlayer();
            finish();
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        releaseMediaPlayer();
        def_lay.setVisibility(View.VISIBLE);
        return false;
    }
}
