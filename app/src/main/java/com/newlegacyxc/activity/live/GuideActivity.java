package com.newlegacyxc.activity.live;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.newlegacyxc.adapter.DateListAdapter;
import com.newlegacyxc.adapter.EpgListAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.listner.SimpleGestureFilter;
import com.newlegacyxc.listner.SimpleGestureFilter.SimpleGestureListener;

import com.newlegacyxc.R;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import static com.newlegacyxc.apps.Constants.catchupFormat;

public class GuideActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,View.OnClickListener,SimpleGestureListener,
      IVLCVout.Callback{
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    int selected_item = 0;
    LibVLC libvlc;
    private SimpleGestureFilter detector;
    Context context = null;
    Button btn_rewind,btn_play,btn_forward;
    public static SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    SeekBar seekbar;
    LinearLayout def_lay,ly_bottom,ly_resolution,ly_audio,ly_subtitle;
    MediaPlayer.TrackDescription[] traks;
    MediaPlayer.TrackDescription[] subtraks;
    String[] resolutions = new String[]{"16:9", "4:3", "16:9"};
    int current_resolution = 0;
    boolean first = true;
    List<EPGEvent> epgModelList;
    ListView date_list, epg_list;
    DateListAdapter dateListAdapter;
    EpgListAdapter epgListAdapter;
    List<String > date_datas,pkg_datas;
    String mStream_id,epg_date,contentUri,channel_name,start_time;
    TextView txt_epg_data,txt_channel,txt_time,txt_progress,txt_title,txt_dec,channel_title,txt_date,txt_time_passed,txt_remain_time,txt_last_time,
            txt_current_dec,txt_next_dec;
    ImageView btn_back;
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd HH:mm");
    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    int page,selected_num,osd_time,date_pos,dates, duration = 0;
    boolean  is_create = true,item_sel = false,is_start = false;
    Handler mHandler = new Handler();
    Runnable mTicker;
    long current_time,startMill;
    private Map<String, List<EPGEvent>> map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        osd_time = (int) MyApp.instance.getPreference().get(Constants.OSD_TIME);
        detector = new SimpleGestureFilter(GuideActivity.this, GuideActivity.this);
        context = this;
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list)));
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
        surfaceView = findViewById(R.id.surface_view);
        remote_subtitles_surface = findViewById(R.id.remote_subtitles_surface);
        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        ly_audio = findViewById(R.id.ly_audio);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);

        ly_subtitle.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_audio.setOnClickListener(this);
        findViewById(R.id.ly_fav).setOnClickListener(this);
        findViewById(R.id.ly_back).setOnClickListener(this);

        btn_rewind = findViewById(R.id.btn_rewind);
        btn_forward = findViewById(R.id.btn_forward);
        btn_play = findViewById(R.id.btn_play);
        btn_rewind.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_forward.setOnClickListener(this);
        findViewById(R.id.channel_play).setOnClickListener(this);
        surfaceView.setOnClickListener(view -> {
            if(ly_bottom.getVisibility()==View.GONE){
                ly_bottom.setVisibility(View.VISIBLE);
            }else {
                ly_bottom.setVisibility(View.GONE);
            }
        });

        def_lay = findViewById(R.id.def_lay);
        ly_bottom = findViewById(R.id.ly_bottom);
        txt_channel = findViewById(R.id.txt_channel);
        EPGChannel epgChannel = MyApp.epgChannel;
        if (epgChannel.getEvents()==null || epgChannel.getEvents().size()==0){
            Toast.makeText(this,"No Programs!",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mStream_id = epgChannel.getStream_id();
        txt_channel.setText(epgChannel.getName());
        channel_name = epgChannel.getName();

        txt_epg_data = findViewById(R.id.txt_epg_data);
        txt_time = findViewById(R.id.txt_time);
        txt_progress = findViewById(R.id.txt_progress);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        date_list = findViewById(R.id.date_list);
        date_list.setOnItemClickListener(this);
        epg_list = findViewById(R.id.epg_list);
        epg_list.setOnItemClickListener(this);
        date_list.setNextFocusRightId(R.id.epg_list);
        epg_list.setNextFocusLeftId(R.id.date_list);
        epg_date = sdf.format(new Date());
        page = 0;
//        if(!getApplicationContext().getPackageName().equalsIgnoreCase(new String(Base64.decode(new String (Base64.decode("LmdvbGQua2RTG1kdmJHUXVhMlJZMjl0TG1kdmJHUXVhMlJ6TlRFM0xuTm9hWHA2WDI1bGR3PT0=".substring(11),Base64.DEFAULT)).substring(11),Base64.DEFAULT)))){
//            return;
//        }
//        new Thread(this::getEpgData).start();
        epgModelList = epgChannel.getEvents();
        if(epgModelList.size()>0){
            map = getFilteredModels(epgModelList);
            date_datas = new ArrayList<>(map.keySet());
//            dates = (int) (System.currentTimeMillis()-epgModelList.get(0).getStartTime().getTime()*1000)/(24*3600*1000);
//            int divide =  (int) (System.currentTimeMillis()-epgModelList.get(0).getEndTime().getTime()*1000)%(24*3600*1000);
//            if(divide>0){
//                dates++;
//            }
//            date_datas = new ArrayList<>();
//            for(int i = 0;i<dates;i++){
//                long millisecond = epgModelList.get(0).getStartTime().getTime()*1000 + i*24*3600*1000;
//                date_datas.add(getFromDate(millisecond));
//            }

            dateListAdapter = new DateListAdapter(this,date_datas);
            date_list.setAdapter(dateListAdapter);

            for(int i = 0;i<date_datas.size();i++){
                if(getFromDate(System.currentTimeMillis()).equalsIgnoreCase(date_datas.get(i))){
                    date_pos = i;
                }
            }
            dateListAdapter.selectItem(date_pos);
            printEpgData();
        }
        FullScreencall();
        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();

    }

    private Map<String, List<EPGEvent>> getFilteredModels(List<EPGEvent> epgModelList) {
        Map<String, List<EPGEvent>> map = new HashMap<>();
        String date ="";
        List<EPGEvent> epgEvents = new ArrayList<>();
        for (EPGEvent epgEvent:epgModelList){
            String instDate = getStrFromDate(epgEvent.getStartTime());
            if (instDate.equals(date)){
                epgEvents.add(epgEvent);
            }else {
                if (!date.equals("")) {
                    map.put(date,epgEvents);
                    epgEvents = new ArrayList<>();
                }
                date = instDate;
            }
        }
        map = new TreeMap<>(map);
        return map;
    }

    private String getStrFromDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(calendar.getTime());
    }
//    private void getEpgData(){
//        try {
//            String map = MyApp.instance.getIptvclient().getAllEPGOfStream(MyApp.user,MyApp.pass,mStream_id);
//            Log.e(getClass().getSimpleName(),map);
//            map=map.replaceAll("[^\\x00-\\x7F]", "");
//            if (!map.contains("null_error_response")){
//                try {
//                    JSONObject jsonObject= new JSONObject(map);
//                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
//                    epgModelList = new ArrayList<>();
//                    if(jsonArray.length() > 0){
//                        for(int i = 0;i<jsonArray.length();i++){
//                            try {
//                                JSONObject e_p = (JSONObject) jsonArray.get(i);
//                                EpgModel epgModel = new EpgModel();
//                                epgModel.setId((String )e_p.get("id"));
//                                epgModel.setCh_id((String )e_p.get("channel_id"));
//                                epgModel.setCategory((String )e_p.get("epg_id"));
//                                epgModel.setT_time((String )e_p.get("start"));
//                                epgModel.setT_time_to((String )e_p.get("end"));
//                                byte[] desc_byte = Base64.decode((String )e_p.get("description"), Base64.DEFAULT);
//                                String desc = new String(desc_byte);
//                                epgModel.setDescr(desc);
//                                byte[] title_byte = Base64.decode((String )e_p.get("title"), Base64.DEFAULT);
//                                String title = new String(title_byte);
//                                epgModel.setName(title);
//                                epgModel.setStart_timestamp(e_p.get("start_timestamp").toString());
//                                epgModel.setStop_timestamp(e_p.get("stop_timestamp").toString());
//                                int duration = ((Integer.parseInt(e_p.get("stop_timestamp").toString())) - (Integer.parseInt(e_p.get("start_timestamp").toString())));
//                                epgModel.setDuration(duration);
//                                if(e_p.has("has_archive")) {
//                                    Double d = Double.parseDouble(e_p.get("has_archive").toString());
//                                    epgModel.setMark_archive(d.intValue());
//                                }
//                                epgModelList.add(epgModel);
//                            }catch (Exception e){
//                                Log.e("error","epg_parse_error");
//                            }
//                        }
//                        runOnUiThread(()->{
//                            if(epgModelList.size()>0){
//                                dates = (int) (System.currentTimeMillis()-Long.parseLong(epgModelList.get(0).getStart_timestamp())*1000)/(24*3600*1000);
//                                int divide =  (int) (System.currentTimeMillis()-Long.parseLong(epgModelList.get(0).getStart_timestamp())*1000)%(24*3600*1000);
//                                if(divide>0){
//                                    dates++;
//                                }
//                                date_datas = new ArrayList<>();
//                                for(int i = 0;i<dates;i++){
//                                    long millisecond = Long.parseLong(epgModelList.get(0).getStart_timestamp())*1000 + i*24*3600*1000;
//                                    date_datas.add(getFromDate(millisecond));
//                                }
//
//                                dateListAdapter = new DateListAdapter(this,date_datas);
//                                date_list.setAdapter(dateListAdapter);
//
//                                for(int i = 0;i<date_datas.size();i++){
//                                    if(getFromDate(System.currentTimeMillis()).equalsIgnoreCase(date_datas.get(i))){
//                                        date_pos = i;
//                                    }
//                                }
//                                dateListAdapter.selectItem(date_pos);
//                                printEpgData();
//                            }
//                        });
//                    }
//                }catch (Exception e){
//                }
//            }
//        }catch (Exception e){
//
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_back:
                finish();
                break;
            case R.id.btn_rewind:
                if(surfaceView.getVisibility()==View.VISIBLE){
                    mHandler.removeCallbacks(mTicker);
                    Date date1 = new Date();
                    if (date1.getTime() > current_time + 60 * 1000) {
                        current_time += 60 * 1000;
                        if (libvlc != null) {
                            releaseMediaPlayer();
                            surfaceView = null;
                        }
                        surfaceView = findViewById(R.id.surface_view);
                        startMill = startMill-60*1000;
                        start_time = getFromCatchDate(startMill);
                        duration = duration+1;
                        contentUri = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,mStream_id,start_time,duration);
                        item_sel = true;
                        playVideo(contentUri);
                        ly_bottom.setVisibility(View.VISIBLE);
                        updateProgressBar();
                        listTimer();
                    }
                }
                break;
            case R.id.btn_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.play();
                }
                break;
            case R.id.btn_forward:
                is_start = true;
                if(surfaceView.getVisibility()==View.VISIBLE){
                    mHandler.removeCallbacks(mTicker);
                    current_time -= 60*1000;
                    if (libvlc != null) {
                        releaseMediaPlayer();
                        surfaceView = null;
                    }
                    surfaceView = (SurfaceView) findViewById(R.id.surface_view);
                    startMill = startMill+60*1000;
                    start_time = getFromCatchDate(startMill);
                    duration = duration-1;
                    contentUri = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,mStream_id,start_time,duration);
                    item_sel = true;
                    playVideo(contentUri);
                    surfaceView.setVisibility(View.VISIBLE);
                    ly_bottom.setVisibility(View.VISIBLE);
                    updateProgressBar();
                    listTimer();
                }
                break;
            case R.id.channel_play:
                is_start = false;
                releaseMediaPlayer();
                is_start = false;
                if(surfaceView!=null){
                    surfaceView = null;
                }
                surfaceView = findViewById(R.id.surface_view);
                startMill = map.get(date_datas.get(date_pos)).get(selected_num).getStartTime().getTime();
                start_time = getFromCatchDate(startMill);
                duration = map.get(date_datas.get(date_pos)).get(selected_num).getDuration()/60;
                contentUri = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,mStream_id,start_time,duration);
                playVideo(contentUri);
                surfaceView.setVisibility(View.VISIBLE);
                remote_subtitles_surface.setVisibility(View.VISIBLE);
                ly_bottom.setVisibility(View.VISIBLE);
                date_list.setVisibility(View.GONE);
                epg_list.setVisibility(View.GONE);
                Date date = new Date();
                current_time = date.getTime();
                updateProgressBar();
                listTimer();
                break;
            case R.id.ly_audio:
                if (traks != null) {
                    if (traks.length > 0) {
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
                if (subtraks != null) {
                    if (subtraks.length > 0) {
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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView == date_list){
            selected_num = 0;
            date_pos = i;
            printEpgData();
            dateListAdapter.selectItem(date_pos);
        }else if(adapterView==epg_list){
            epgListAdapter.selectItem(i);
            selected_num = i;
            printEpgDataInDetail();
        }
    }

    private void printEpgData(){
        txt_progress.setVisibility(View.GONE);
        epgListAdapter = new EpgListAdapter(this,map.get(date_datas.get(date_pos)));
        epg_list.setAdapter(epgListAdapter);
        epgListAdapter.selectItem(selected_num);
        printEpgDataInDetail();
    }

    private void printEpgDataInDetail(){
        String epgtime = Constants.Offset(true,map.get(date_datas.get(date_pos)).get(selected_num).getStartTime()) + "-" + Constants.Offset(true,map.get(date_datas.get(date_pos)).get(selected_num).getEndTime());
        String epgData = map.get(date_datas.get(date_pos)).get(selected_num).getDec();
        String epgText = epgtime + " - " + epgData;

        Spannable spannable = new SpannableString(epgText);
        spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, epgtime.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txt_epg_data.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onSwipe(int direction) {

    }

    @Override
    public void onDoubleTap() {

    }

    private String getFromDate(long millisecond){
        Date date = new Date();
        date.setTime(millisecond);
        String formattedDate=new SimpleDateFormat("yyyy-MM-dd").format(date);
        return formattedDate;
    }

    private String getFromCatchDate(long millisecond){
        Date date = new Date();
        date.setTime(millisecond);
        String formattedDate=catchupFormat.format(date);
        return formattedDate;
    }

    private String getFromDate1(long millisecond){
        Date date = new Date();
        date.setTime(millisecond);
        String formattedDate=sdf1.format(date);
        return formattedDate;
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }

    public void doWork() {
        runOnUiThread(() -> {
            try {
                txt_time.setText(time.format(new Date()));
            } catch (Exception e) {
            }
        });
    }

    public void FullScreencall() {
        if( Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if(ly_bottom.getVisibility()==View.VISIBLE){
                        ly_bottom.setVisibility(View.GONE);
                        return true;
                    }
                    if(surfaceView.getVisibility() == View.VISIBLE){
                        releaseMediaPlayer();
                        def_lay.setVisibility(View.GONE);
                        surfaceView.setVisibility(View.GONE);
                        remote_subtitles_surface.setVisibility(View.GONE);
                        epg_list.setVisibility(View.VISIBLE);
                        date_list.setVisibility(View.VISIBLE);
                        return true;
                    }
                    finish();
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if(view==epg_list){
                        if(selected_num<map.get(date_datas.get(date_pos)).size()-1){
                            selected_num++;
                        }
                        epgListAdapter.selectItem(selected_num);
                        printEpgDataInDetail();
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_UP:
                    if(view==epg_list){
                        if(selected_num>0){
                            selected_num--;
                        }
                        epgListAdapter.selectItem(selected_num);
                        printEpgDataInDetail();
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if(view==epg_list){
                        is_start = false;
                        releaseMediaPlayer();
                        is_start = false;
                        if(surfaceView!=null){
                            surfaceView = null;
                        }
                        surfaceView = findViewById(R.id.surface_view);
                        startMill = map.get(date_datas.get(date_pos)).get(selected_num).getStartTime().getTime();
                        start_time = getFromCatchDate(startMill);
                        duration = map.get(date_datas.get(date_pos)).get(selected_num).getDuration()/60;
                        contentUri = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,mStream_id,start_time,duration);
                        playVideo(contentUri);
                        surfaceView.setVisibility(View.VISIBLE);
                        ly_bottom.setVisibility(View.VISIBLE);
                        date_list.setVisibility(View.GONE);
                        epg_list.setVisibility(View.GONE);
                        Date date = new Date();
                        current_time = date.getTime();
                        updateProgressBar();
                        listTimer();
                    }else {
                        if(surfaceView.getVisibility()==View.VISIBLE){
                            if (mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();
                            } else {
                                mMediaPlayer.play();
                            }
                            ly_bottom.setVisibility(View.VISIBLE);
                            listTimer();
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if(surfaceView.getVisibility()==View.VISIBLE){
                        current_time -= 60*1000;
                        releaseMediaPlayer();
                        is_start = false;
                        if (surfaceView != null) {
                            surfaceView = null;
                        }
                        surfaceView =  findViewById(R.id.surface_view);
                        startMill = startMill+60*1000;
                        start_time = getFromCatchDate(startMill);
                        duration = duration+1;
                        contentUri = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,mStream_id,start_time,duration);
                        playVideo(contentUri);
                        surfaceView.setVisibility(View.VISIBLE);
                        ly_bottom.setVisibility(View.VISIBLE);
                        date_list.setVisibility(View.GONE);
                        epg_list.setVisibility(View.GONE);
                        updateProgressBar();
                        listTimer();
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if(surfaceView.getVisibility()==View.VISIBLE){
                        Date date1 = new Date();
                        if (date1.getTime() > current_time + 60 * 1000) {
                            current_time += 60 * 1000;
                            releaseMediaPlayer();
                            is_start = false;
                            if (surfaceView != null) {
                                surfaceView = null;
                            }
                            surfaceView = findViewById(R.id.surface_view);
                            startMill = startMill-60*1000;
                            start_time = getFromCatchDate(startMill);
                            duration = duration-1;
                            contentUri = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,mStream_id,start_time,duration);
                            playVideo(contentUri);
                            surfaceView.setVisibility(View.VISIBLE);
                            ly_bottom.setVisibility(View.VISIBLE);
                            date_list.setVisibility(View.GONE);
                            epg_list.setVisibility(View.GONE);
                            updateProgressBar();
                            listTimer();

                        }
                    }else if(view==date_list){
                        finish();
                    }

                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    int maxTime;

    private void listTimer() {
        maxTime = 10;
        mTicker = new Runnable() {
            public void run() {
                if (maxTime < 1) {
                    if(ly_bottom.getVisibility()==View.VISIBLE){
                        ly_bottom.setVisibility(View.GONE);
                    }
                    return;
                }
                runNextTicker();
            }
        };
        mTicker.run();
    }
    private void runNextTicker() {
        maxTime--;
        long next = SystemClock.uptimeMillis() + 1000;
        mHandler.postAtTime(mTicker, next);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (libvlc != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            playVideo(contentUri);
        } else {
            is_create = false;
        }
    }

    private void playVideo(String path) {
        releaseMediaPlayer();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;
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

            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);


            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);

            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.setWindowSize(mVideoWidth, mVideoHeight);
            vout.addCallback(this);
            vout.attachViews();
//            vout.setSubtitlesView(tv_subtitle);


            Media m = new Media(libvlc, Uri.parse(path));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        releaseMediaPlayer();
        super.onUserLeaveHint();
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
    public void onDestroy() {
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

    private void releaseMediaPlayer() {
        //라이브러리가 없다면
        //바로 종료
        if (libvlc == null)
            return;
        if (mMediaPlayer != null) {
            //플레이 중지

            mMediaPlayer.stop();

            final IVLCVout vout = mMediaPlayer.getVLCVout();
            //콜백함수 제거
            vout.removeCallback(this);

            //연결된 뷰 분리
            vout.detachViews();
        }

        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<GuideActivity> mOwner;

        public MediaPlayerListener(GuideActivity owner) {
            mOwner = new WeakReference<GuideActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            GuideActivity player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    //동영상 끝까지 재생되었다면..
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
                    player.releaseMediaPlayer();
//                    Toast.makeText(player, "Error", Toast.LENGTH_SHORT).show();
                    player.def_lay.setVisibility(View.VISIBLE);
                    break;

                //아래 두 이벤트는 계속 발생됨
                case MediaPlayer.Event.TimeChanged: //재생 시간 변화시
                    break;
                case MediaPlayer.Event.PositionChanged: //동영상 재생 구간 변화시
                    //Log.d(TAG, "PositionChanged");
                    break;
                default:
                    break;
            }
        }
    }


    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mMediaPlayer != null) {
                if (traks == null && subtraks == null) {
                    first = false;
                    traks = mMediaPlayer.getAudioTracks();
                    subtraks = mMediaPlayer.getSpuTracks();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm a EEE MM/dd");
                long totalDuration = System.currentTimeMillis();
                if(map.get(date_datas.get(date_pos))!=null && map.get(date_datas.get(date_pos)).size()>0){
                    txt_date.setText(dateFormat.format(new Date()));
                    int pass_min = (int) ((totalDuration - current_time)/(1000*60));
                    int remain_min = (int)((map.get(date_datas.get(date_pos)).get(selected_num).getEndTime().getTime()-map.get(date_datas.get(date_pos)).get(selected_num).getStartTime().getTime())/60/1000) - pass_min;
                    int progress = pass_min * 100/(pass_min+remain_min);
                    seekbar.setProgress(progress);
                    txt_time_passed.setText("Started " + pass_min +" mins ago");
                    txt_remain_time.setText("+"+remain_min+"min");
                    txt_last_time.setText(getFromDate1(current_time + map.get(date_datas.get(date_pos)).get(selected_num).getDuration()*1000));
                    txt_dec.setText(map.get(date_datas.get(date_pos)).get(selected_num).getDec());
                    txt_title.setText(map.get(date_datas.get(date_pos)).get(selected_num).getTitle());
                    channel_title.setText(channel_name);
                    txt_current_dec.setText(map.get(date_datas.get(date_pos)).get(selected_num).getTitle());
                    try {
                        txt_next_dec.setText(map.get(date_datas.get(date_pos)).get(selected_num+1).getTitle());
                    }catch (Exception e){
                        txt_next_dec.setText("No Information");
                    }
                }else {
                    txt_title.setText("No Information");
                    txt_dec.setText("No Information");
                    channel_title.setText(channel_name);
                    txt_date.setText(dateFormat.format(new Date()));
                    txt_time_passed.setText("");
                    txt_remain_time.setText("");
                    txt_last_time.setText("");
                    seekbar.setProgress(0);
                    txt_current_dec.setText("No Information");
                    txt_next_dec.setText("No Information");
                }
            }
            mHandler.postDelayed(this, 500);
        }
    };

    private void showAudioTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GuideActivity.this);
        builder.setTitle("Audio track");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < traks.length; i++) {
            names.add(traks[i].name);
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

                mMediaPlayer.setAudioTrack(traks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSubTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GuideActivity.this);
        builder.setTitle("Subtitle");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < subtraks.length; i++) {
            names.add(subtraks[i].name);
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
                mMediaPlayer.setSpuTrack(subtraks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
