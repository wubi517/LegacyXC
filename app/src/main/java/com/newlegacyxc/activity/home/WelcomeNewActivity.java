package com.newlegacyxc.activity.home;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.newlegacyxc.BuildConfig;
import com.newlegacyxc.MainActivity;
import com.newlegacyxc.R;
import com.newlegacyxc.activity.LoginActivity;
import com.newlegacyxc.activity.SplashActivity;
import com.newlegacyxc.activity.catchup.CatchUpActivity;
import com.newlegacyxc.activity.live.LiveExoPlayActivity;
import com.newlegacyxc.activity.live.LiveIjkPlayActivity;
import com.newlegacyxc.activity.live.LivePlayActivity;
import com.newlegacyxc.activity.live.PreviewChannelActivity;
import com.newlegacyxc.activity.live.PreviewChannelExoActivity;
import com.newlegacyxc.activity.live.PreviewChannelIJKActivity;
import com.newlegacyxc.activity.multi.MultiScreenActivity;
import com.newlegacyxc.activity.series.PreviewSeriesNewActivity;
import com.newlegacyxc.activity.tvguide.TVGuideNewActivity;
import com.newlegacyxc.activity.vod.PreviewVodNewActivity;
import com.newlegacyxc.activity.vod.VideoExoPlayActivity;
import com.newlegacyxc.activity.vod.VideoIjkPlayActivity;
import com.newlegacyxc.activity.vod.VideoPlayActivity;
import com.newlegacyxc.adapter.AutoScrollPagerAdapter;
import com.newlegacyxc.adapter.CategoryListAdapter;
import com.newlegacyxc.adapter.HomeListAdapter;
import com.newlegacyxc.adapter.MenuListAdapter;
import com.newlegacyxc.apps.CategoryType;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.ConnectionDlg;
import com.newlegacyxc.dialog.HideCategoryDlg;
import com.newlegacyxc.dialog.ParentControlDlg;
import com.newlegacyxc.dialog.PinDlg;
import com.newlegacyxc.dialog.PinMultiScreenDlg;
import com.newlegacyxc.dialog.ReloadDlg;
import com.newlegacyxc.dialog.SearchDlg;
import com.newlegacyxc.dialog.SettingDlg;
import com.newlegacyxc.models.AppInfoModel;
import com.newlegacyxc.models.CategoryModel;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.FullModel;
import com.newlegacyxc.models.MovieModel;
import com.newlegacyxc.models.SeriesModel;
import com.newlegacyxc.models.SideMenu;
import com.newlegacyxc.utils.AutoScrollViewPager;
import com.newlegacyxc.vpn.fastconnect.core.OpenConnectManagementThread;
import com.newlegacyxc.vpn.fastconnect.core.OpenVpnService;
import com.newlegacyxc.vpn.fastconnect.core.VPNConnector;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.newlegacyxc.activity.vod.PreviewVodNewActivity.checkAddedRecent;
import static com.newlegacyxc.apps.Constants.getLiveFilter;
import static com.newlegacyxc.apps.Constants.getSeriesFilter;
import static com.newlegacyxc.apps.Constants.getVodFilter;
import static com.newlegacyxc.apps.MyApp.num_server;

public class WelcomeNewActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, IVLCVout.Callback {

    private static int AUTO_SCROLL_THRESHOLD_IN_MILLI = 5000;

    private TextView txt_time, channel_title, channel_body;
    private ImageView channel_icon, image_ad1, icon;
    private RelativeLayout ly_surface;
    public static SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    LinearLayout def_lay;
    String ratio;
    String[] resolutions ;
    private int mVideoWidth;
    private int mVideoHeight;
    private MediaPlayer mMediaPlayer = null;
    LibVLC libvlc=null;
    private String contentUri="";
    private CategoryListAdapter categoryListAdapter;
    private MenuListAdapter menuListAdapter;
    SimpleDateFormat time = new SimpleDateFormat("h:mm a");
    int category_pos;
    private int current_player = 0;
    private ListView menu_recyclerview;
    List<String > settingDatas;
    private int current_position = 0;

    String[] category_names;
    String[] category_ids;
    boolean[] checkedItems;
    List<String> selectedIds= new ArrayList<>();
    private VPNConnector mConn;
    private HomeListAdapter homeListAdapter;
    private AppInfoModel appInfoModel;
    private AutoScrollPagerAdapter autoScrollPagerAdapter;
    private AutoScrollViewPager viewPager;
    private TabLayout tabs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnOnStrictMode();
        setContentView(R.layout.activity_welcome_new);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        menu_recyclerview = findViewById(R.id.page_recyclerview);
//        menu_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        final List<SideMenu> list = new ArrayList<>();
        list.add(new SideMenu("Live TV"));
        list.add(new SideMenu("Multi TV"));
        list.add(new SideMenu("Movies"));
        list.add(new SideMenu("Series"));
        list.add(new SideMenu("Tv Guide"));
        list.add(new SideMenu("Sports Guide"));
        list.add(new SideMenu("Settings"));
        list.add(new SideMenu("Refresh Playlist"));
        list.add(new SideMenu("Catch Up"));
        channel_icon = findViewById(R.id.channel_icon);
        channel_title = findViewById(R.id.channel_title);
        channel_body = findViewById(R.id.channel_body);
        icon = findViewById(R.id.icon);
//        if (!Constants.GetIcon(this).equals(""))
//            Picasso.with(this).load(Constants.GetIcon(this))
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .error(R.drawable.icon)
//                    .into(icon);
        image_ad1 = findViewById(R.id.image_ad1);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        settingDatas = new ArrayList<>();
        settingDatas.addAll(Arrays.asList(getResources().getStringArray(R.array.setting_list)));


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
        Log.e("height", String.valueOf(MyApp.SCREEN_HEIGHT));
        appInfoModel = (AppInfoModel) MyApp.instance.getPreference().get(Constants.getAppInfoModel());
        AUTO_SCROLL_THRESHOLD_IN_MILLI=appInfoModel.getAppInfo().getSliderTime()*1000;
        categoryListAdapter = new CategoryListAdapter(this, new ArrayList<>());
        menuListAdapter = new MenuListAdapter(this, list);
        menu_recyclerview.setAdapter(menuListAdapter);
        menu_recyclerview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        menu_recyclerview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (findViewById(R.id.btn_back).getVisibility()!=View.VISIBLE){
                    SideMenu sideMenu = list.get(position);
                    switch (sideMenu.getName()) {
                        case "Multi TV":
                            FullModel fullModel = Constants.getAllFullModel(MyApp.fullModels);
                            if (fullModel!=null && fullModel.getChannels().size()==0){
                                Toast.makeText(WelcomeNewActivity.this,"No Channels",Toast.LENGTH_LONG).show();

                            }
                            showScreenModeList();
                            return;
                        case "Live TV":
                            fullModel = Constants.getAllFullModel(MyApp.fullModels);
                            if (fullModel != null && fullModel.getChannels().size() == 0) {
                                Toast.makeText(WelcomeNewActivity.this, "No Channels", Toast.LENGTH_LONG).show();

                            }
                            if (MyApp.instance.getPreference().get(Constants.getCategory_POS()) == null) {
                                category_pos = 0;
                            } else {
                                category_pos = (int) MyApp.instance.getPreference().get(Constants.getCategory_POS());
                            }
                            getLiveFilter();
                            categoryListAdapter.setStatus(sideMenu.getName(), MyApp.live_categories_filter);
                            findViewById(R.id.btn_search).setVisibility(View.VISIBLE);
                            break;
                        case "Movies":
                            if (MyApp.movieModels != null && MyApp.movieModels.size() == 0) {
                                Toast.makeText(WelcomeNewActivity.this, "No Movies", Toast.LENGTH_LONG).show();

                            }
                            if (MyApp.instance.getPreference().get(Constants.getVodPos()) == null) {
                                category_pos = 0;
                            } else {
                                category_pos = (int) MyApp.instance.getPreference().get(Constants.getVodPos());
                            }
                            getVodFilter();
                            categoryListAdapter.setStatus(sideMenu.getName(), MyApp.vod_categories_filter);
                            break;
                        case "Series":
                            if (MyApp.seriesModels != null && MyApp.seriesModels.size() == 0) {
                                Toast.makeText(WelcomeNewActivity.this, "No Series", Toast.LENGTH_LONG).show();

                            }
                            if (MyApp.instance.getPreference().get(Constants.getSeriesPos()) == null) {
                                category_pos = 0;
                            } else {
                                category_pos = (int) MyApp.instance.getPreference().get(Constants.getSeriesPos());
                            }
                            getSeriesFilter();
                            categoryListAdapter.setStatus(sideMenu.getName(), MyApp.series_categories_filter);
                            break;
                        case "Sports Guide":
                            startActivity(new Intent(WelcomeNewActivity.this, WebViewActivity.class));
                            return;
                        case "Tv Guide":
                            Log.e("WelcomNewActivity",Constants.getAllFullModel(MyApp.fullModels_filter).getCatchable_count()+"");
                            startActivity(new Intent(WelcomeNewActivity.this, TVGuideNewActivity.class));
//                            if (Constants.getAllFullModel(MyApp.fullModels_filter).getCatchable_count()!=0)
//                                startActivity(new Intent(WelcomeNewActivity.this, TVGuideNewActivity.class));
//                            else Toast.makeText(WelcomeNewActivity.this,"There aren't any programs in channels",Toast.LENGTH_LONG).show();
                            return;
                        case "Settings":
                            showSettingDlg();
                            return;
                        case "Refresh Playlist":
                            ReloadDlg(null);
                            return;
                        case "Catch Up":
                            startActivity(new Intent(WelcomeNewActivity.this, CatchUpActivity.class));
                            return;
                    }
                    menu_recyclerview.setAdapter(categoryListAdapter);
                    menu_recyclerview.performClick();
                    findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
                }else {
                    current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
                    CategoryModel categoryModel = (CategoryModel)adapterView.getItemAtPosition(position);
                    switch (categoryListAdapter.getStatus()){
                        case "Live TV":
                            category_pos = position;
                            MyApp.instance.getPreference().put(Constants.getCategory_POS(),category_pos);

                            if(categoryModel.getName().toLowerCase().contains("adult")){
                                PinDlg pinDlg = new PinDlg(WelcomeNewActivity.this, new PinDlg.DlgPinListener() {
                                    @Override
                                    public void OnYesClick(Dialog dialog, String pin_code) {
                                        String pin = (String )MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                                        if(pin_code.equalsIgnoreCase(pin)){
                                            dialog.dismiss();
                                            MyApp.instance.getPreference().remove(Constants.getChannel_Pos());
                                            switch (current_player){
                                                case 0:
                                                    startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelActivity.class));
                                                    break;
                                                case 1:
                                                    startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelIJKActivity.class));
                                                    break;
                                                case 2:
                                                    startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelExoActivity.class));
                                                    break;
                                            }
                                        }else {
                                            Toast.makeText(WelcomeNewActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    @Override
                                    public void OnCancelClick(Dialog dialog, String pin_code) {
                                        dialog.dismiss();
                                    }
                                });
                                pinDlg.show();
                            }else {
                                MyApp.instance.getPreference().remove(Constants.getChannel_Pos());
                                switch (current_player){
                                    case 0:
                                        startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelActivity.class));
                                        break;
                                    case 1:
                                        startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelIJKActivity.class));
                                        break;
                                    case 2:
                                        startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelExoActivity.class));
                                        break;
                                }
                            }
                            break;
                        case "Movies":
                            if (categoryModel.getMovieModels()==null || categoryModel.getMovieModels().size()==0) {
                                Toast.makeText(getBaseContext(),"No Movies",Toast.LENGTH_LONG).show();
                                return;
                            }
                            if(categoryModel.getId().equals(Constants.xxx_vod_category_id)){
                                PinDlg pinDlg = new PinDlg(WelcomeNewActivity.this, new PinDlg.DlgPinListener() {
                                    @Override
                                    public void OnYesClick(Dialog dialog, String pin_code) {
                                        String pin = (String )MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                                        if(pin_code.equalsIgnoreCase(pin)){
                                            dialog.dismiss();
                                            category_pos = position;
                                            MyApp.instance.getPreference().put(Constants.getVodPos(),category_pos);
                                            startActivity(new Intent(WelcomeNewActivity.this, PreviewVodNewActivity.class));
                                        }else {
                                            Toast.makeText(WelcomeNewActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    @Override
                                    public void OnCancelClick(Dialog dialog, String pin_code) {
                                        dialog.dismiss();
                                    }
                                });
                                pinDlg.show();
                            }else {
                                category_pos = position;
                                MyApp.instance.getPreference().put(Constants.getVodPos(),category_pos);
                                startActivity(new Intent(WelcomeNewActivity.this, PreviewVodNewActivity.class));
                            }
                            break;
                        case "Series":
                            category_pos = position;
                            MyApp.instance.getPreference().put(Constants.getSeriesPos(),category_pos);
                            startActivity(new Intent(WelcomeNewActivity.this,PreviewSeriesNewActivity.class));
                            break;
                    }
                }
            }
        });
        txt_time = findViewById(R.id.clock);
        Thread myThread;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
        FullScreencall();

        //Play last channel
        boolean IS_NULL_RECENT = false;
        try {
            EPGChannel epgChannel = Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().get(0);
            if (epgChannel.is_locked()){
                PinDlg pinDlg = new PinDlg(WelcomeNewActivity.this, new PinDlg.DlgPinListener() {
                    @Override
                    public void OnYesClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                        String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                        if(pin_code.equalsIgnoreCase(pin)){
                            dialog.dismiss();
                            contentUri = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                                    epgChannel.getStream_id(),"ts");
                            setDescription(epgChannel.getStream_icon(),epgChannel.getName(), epgChannel.getNum(),Constants.GetAd1(WelcomeNewActivity.this),contentUri);
                        }else {
                            dialog.dismiss();
                            Toast.makeText(WelcomeNewActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void OnCancelClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                    }
                });
                pinDlg.show();
            }else {
                contentUri = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                        epgChannel.getStream_id(),"ts");
                setDescription(epgChannel.getStream_icon(),epgChannel.getName(), epgChannel.getNum(),Constants.GetAd1(WelcomeNewActivity.this),contentUri);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("recentmodel","null");
            IS_NULL_RECENT = true;
        }

        homeListAdapter = new HomeListAdapter(this,IS_NULL_RECENT, (categoryPos, itemPos, epgChannel, movieModel, seriesModel) -> {
            switch (categoryPos){
                case 0:
                    if (epgChannel.is_locked()){
                        PinDlg pinDlg = new PinDlg(WelcomeNewActivity.this, new PinDlg.DlgPinListener() {
                            @Override
                            public void OnYesClick(Dialog dialog, String pin_code) {
                                dialog.dismiss();
                                String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                                if(pin_code.equalsIgnoreCase(pin)){
                                    dialog.dismiss();
                                    String url = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                                            epgChannel.getStream_id()+"","ts");
                                    if (!contentUri.equals(url))
                                        setDescription(epgChannel.getStream_icon(),epgChannel.getName(), epgChannel.getNum(),Constants.GetAd1(WelcomeNewActivity.this),url);
                                    else gotoLivePlayActivity(epgChannel,contentUri);
                                }else {
                                    dialog.dismiss();
                                    Toast.makeText(WelcomeNewActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void OnCancelClick(Dialog dialog, String pin_code) {
                                dialog.dismiss();
                            }
                        });
                        pinDlg.show();
                    }else {
                        String url = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                                epgChannel.getStream_id(),"ts");
                        if (!contentUri.equals(url))
                            setDescription(epgChannel.getStream_icon(),epgChannel.getName(), epgChannel.getNum(),Constants.GetAd1(WelcomeNewActivity.this),url);
                        else gotoLivePlayActivity(epgChannel,contentUri);
                    }

                    break;
                case 1:
                    MyApp.subMovieModels = new ArrayList<>();
                    MyApp.subMovieModels.add(movieModel);
                    String vod_url = MyApp.instance.getIptvclient().buildMovieStreamURL(MyApp.user,MyApp.pass,movieModel.getStream_id(),movieModel.getExtension());
                    if (!contentUri.equals(vod_url))
                        setDescription(movieModel.getStream_icon(),movieModel.getName(), movieModel.getNum(),Constants.GetAd2(WelcomeNewActivity.this),vod_url);
                    else gotoVodPlayActivity(movieModel,vod_url);
                    break;
                case 2:
                    List<SeriesModel> seriesModels = new ArrayList<>();
                    seriesModels.add(seriesModel);
                    MyApp.selectedSeriesModelList = seriesModels;
                    break;
            }
            return null;
        });
        RecyclerView home_recyclerview = findViewById(R.id.main_recyclerview);

        home_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        home_recyclerview.setAdapter(homeListAdapter);


        //Set up viewpager
        autoScrollPagerAdapter = new AutoScrollPagerAdapter(getSupportFragmentManager(), viewPager, appInfoModel);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(autoScrollPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("WelcomeActivity","tab pos "+tab.getPosition());
                Picasso.with(WelcomeNewActivity.this)
                        .load(appInfoModel.getAppInfo().getSlider().get(tab.getPosition()).getImageurl())
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .error(R.drawable.ad2)
                        .into(image_ad1);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // start auto scroll
        viewPager.startAutoScroll();
        // set auto scroll time in mili
        viewPager.setInterval(AUTO_SCROLL_THRESHOLD_IN_MILLI);
        // enable recycling using true
        viewPager.setCycle(true);
    }

    private void getRespond() {
        try {
            long startTime = System.nanoTime();
            String responseBody = null;
            responseBody = MyApp.instance.getIptvclient().login(Constants.GetUrl1(this));
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),responseBody);
            Log.e("BugCheck","authenticate success "+MethodeDuration);
            try{
                JSONObject jsonObject = new JSONObject(responseBody);
                AppInfoModel appInfoModel0 = new AppInfoModel();
                if(jsonObject.getString("status").toString().equalsIgnoreCase("success")){
                    appInfoModel0.setSuccess(true);
                }else {
                    appInfoModel0.setSuccess(false);
                }
                AppInfoModel.AppInfoEntity appInfo = new AppInfoModel.AppInfoEntity();
                appInfo.setPin2(jsonObject.getString("dual_screen"));
                appInfo.setPin3(jsonObject.getString("tri_screen"));
                appInfo.setPin4(jsonObject.getString("four_way_screen"));
                String slider_time = jsonObject.getString("slider_time");
                if(!slider_time.isEmpty()){
                    appInfo.setSliderTime(Integer.parseInt(slider_time));
                }else {
                    appInfo.setSliderTime(10);
                }

                JSONArray image_array = jsonObject.getJSONArray("images");
                if(image_array.length() > 1){
                    List<AppInfoModel.SliderEntity> sliderEntities = new ArrayList<>();
                    for(int i = 1;i<image_array.length();i++){
                        AppInfoModel.SliderEntity sliderEntity = new AppInfoModel.SliderEntity();
                        sliderEntity.setImageurl(image_array.get(i).toString());
                        sliderEntity.setBody("Welcome to "+ getResources().getString(R.string.app_name));
                        sliderEntity.setHeader("Welcome "+ getResources().getString(R.string.app_name));
                        sliderEntities.add(sliderEntity);
                    }
                    appInfo.setSlider(sliderEntities);
                }
                appInfoModel0.setAppInfo(appInfo);

                appInfoModel = appInfoModel0;
                AUTO_SCROLL_THRESHOLD_IN_MILLI=appInfoModel.getAppInfo().getSliderTime()*1000;
                MyApp.instance.getPreference().put(Constants.getAppInfoModel(),appInfo);
                runOnUiThread(()->{
                    autoScrollPagerAdapter = new AutoScrollPagerAdapter(getSupportFragmentManager(),viewPager,appInfoModel);
//                    autoScrollPagerAdapter.setAppInfoModel(appInfoModel);
                    viewPager.setAdapter(autoScrollPagerAdapter);
                    viewPager.setInterval(AUTO_SCROLL_THRESHOLD_IN_MILLI);
                    tabs.removeAllTabs();
                    tabs.setupWithViewPager(viewPager);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(()->Toast.makeText(WelcomeNewActivity.this, "Network Error!", Toast.LENGTH_SHORT).show());
            finish();
        }
    }

    private void turnOnStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .detectActivityLeaks()
                    .build());
        }
    }

    private void gotoVodPlayActivity(MovieModel showmodel, String vod_url){
        checkAddedRecent(showmodel);
        Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels().add(0,showmodel);
        //get recent channel names list
        List<MovieModel> recent_channel_models = new ArrayList<>(Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels());
        //set
        MyApp.instance.getPreference().put(Constants.getRecentMovies(), recent_channel_models);
        String vod_title = showmodel.getName();
        String vod_image = showmodel.getStream_icon();
        String type = showmodel.getExtension();
        MyApp.vod_model = showmodel;
        Intent intent = new Intent();
        switch (current_player){
            case 0:
                intent = new Intent(WelcomeNewActivity.this, VideoPlayActivity.class);
                break;
            case 1:
                intent = new Intent(WelcomeNewActivity.this, VideoIjkPlayActivity.class);
                break;
            case 2:
                intent = new Intent(WelcomeNewActivity.this, VideoExoPlayActivity.class);
                break;
        }
        intent.putExtra("title",vod_title);
        intent.putExtra("img",vod_image);
        intent.putExtra("url",vod_url);
        startActivity(intent);
    }

    private void gotoLivePlayActivity(EPGChannel selectedEpgChannel, String url) {
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
        intent.putExtra("title", selectedEpgChannel.getName());
        intent.putExtra("img", selectedEpgChannel.getStream_icon());
        intent.putExtra("url",url);
        intent.putExtra("stream_id", selectedEpgChannel.getStream_id());
        intent.putExtra("is_live",true);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    private void showSettingDlg() {
        SettingDlg settingDlg = new SettingDlg(WelcomeNewActivity.this, settingDatas, (dialog, position) -> {
            switch (position){
                case 0:
                    ParentControlDlg dlg = new ParentControlDlg(WelcomeNewActivity.this, new ParentControlDlg.DialogUpdateListener() {
                        @Override
                        public void OnUpdateNowClick(Dialog dialog, int code) {
                            if(code==1){
                                dialog.dismiss();
                            }
                        }
                        @Override
                        public void OnUpdateSkipClick(Dialog dialog, int code) {
                            dialog.dismiss();
                        }
                    });
                    dlg.show();
                    break;
                case 1:
                    ReloadDlg(dialog);
                    break;
                case 2:
                    showSortingDlg();
                    break;
                case 3:
                    showInternalPlayers();
                    break;
                case 4:
                    //TODO
                    showMultiSelection(CategoryType.live);
                    break;
                case 5:
                    //TODO
                    showMultiSelection(CategoryType.vod);
                    break;
                case 6:
                    //TODO
                    showMultiSelection(CategoryType.series);
                    break;
//                case 7:
//                    AccountDlg accountDlg = new AccountDlg(WelcomeNewActivity.this, dialog1 -> {
//                    });
//                    accountDlg.show();
//                    break;
                case 7:
                    startActivity(new Intent(WelcomeNewActivity.this,VpnActivity.class));
                    dialog.dismiss();
                    break;
                case 8:
                    MyApp.instance.getPreference().remove(Constants.getLoginInfo());
                    MyApp.instance.getPreference().remove(Constants.getCategory_POS());
                    MyApp.instance.getPreference().remove(Constants.getSeriesPos());
                    MyApp.instance.getPreference().remove(Constants.getVodPos());
                    startActivity(new Intent(WelcomeNewActivity.this, LoginActivity.class));
                    finish();
                    dialog.dismiss();
                    break;
            }
        });
        settingDlg.show();
    }

    private void stopVPN() {
        try {
            if (mConn.service.getConnectionState() ==
                    OpenConnectManagementThread.STATE_DISCONNECTED) {
                mConn.service.startReconnectActivity(this);
            } else {
                mConn.service.stopVPN();
                MyApp.is_vpn=false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showMultiSelection(final CategoryType categoryType) {
        int i_live=2,i_vod=2,i_series=1;
        switch (categoryType){
            case vod:
                if (MyApp.instance.getPreference().get(Constants.getInvisibleVodCategories())!=null) selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.getInvisibleVodCategories());
                category_names=new String[MyApp.vod_categories.size()-i_vod];
                category_ids=new String[MyApp.vod_categories.size()-i_vod];
                checkedItems=new boolean[category_names.length];
                for (int i=0;i<MyApp.vod_categories.size()-i_vod;i++){
                    CategoryModel categoryModel =MyApp.vod_categories.get(i+i_vod);
                    category_names[i]= categoryModel.getName();
                    category_ids[i]= categoryModel.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModel.getId());
                }
                break;
            case live:
                if (MyApp.instance.getPreference().get(Constants.getInvisibleLiveCategories())!=null) selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.getInvisibleLiveCategories());
                category_names=new String[MyApp.live_categories.size()-i_live];
                category_ids=new String[MyApp.live_categories.size()-i_live];
                checkedItems=new boolean[category_names.length];
                for (int i=0;i<MyApp.live_categories.size()-i_live;i++){
                    CategoryModel categoryModel =MyApp.live_categories.get(i+i_live);
                    category_names[i]= categoryModel.getName();
                    category_ids[i]= categoryModel.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModel.getId());
                }
                break;
            case series:
                if (MyApp.instance.getPreference().get(Constants.getInvisibleSeriesCategories())!=null) selectedIds=(List<String>) MyApp.instance.getPreference().get(Constants.getInvisibleSeriesCategories());
                category_names=new String[MyApp.series_categories.size()-i_series];
                category_ids=new String[MyApp.series_categories.size()-i_series];
                checkedItems=new boolean[category_names.length];
                for (int i=0;i<MyApp.series_categories.size()-i_series;i++){
                    CategoryModel categoryModel =MyApp.series_categories.get(i+i_series);
                    category_names[i]= categoryModel.getName();
                    category_ids[i]= categoryModel.getId();
                    checkedItems[i] = !selectedIds.contains(categoryModel.getId());
                }
                break;
        }
        HideCategoryDlg dlg=new HideCategoryDlg(this, category_names, checkedItems, new HideCategoryDlg.DialogSearchListener() {
            @Override
            public void OnItemClick(Dialog dialog, int position, boolean checked) {
                if (!checked){
                    if (!selectedIds.contains(category_ids[position])){
                        selectedIds.add(category_ids[position]);
                    }
                }else {
                    if (selectedIds.contains(category_ids[position])){
                        selectedIds.removeAll(Arrays.asList(category_ids[position]));
                    }
                }
            }

            @Override
            public void OnOkClick(Dialog dialog) {
                selectedIds=new ArrayList<>();
                for (int m=0;m<checkedItems.length;m++){
                    if (!checkedItems[m]) selectedIds.add(category_ids[m]);
                }
                switch (categoryType){
                    case series:
                        MyApp.instance.getPreference().put(Constants.getInvisibleSeriesCategories(),selectedIds);
                        break;
                    case live:
                        MyApp.instance.getPreference().put(Constants.getInvisibleLiveCategories(),selectedIds);
                        break;
                    case vod:
                        MyApp.instance.getPreference().put(Constants.getInvisibleVodCategories(),selectedIds);
                        break;
                }
            }

            @Override
            public void OnCancelClick(Dialog dialog) {

            }

            @Override
            public void OnSelectAllClick(Dialog dialog) {
                for(int i=0;i<checkedItems.length;i++){
                    checkedItems[i]=true;
                    selectedIds.clear();
                }
            }
        });
        dlg.show();
    }

    private void showInternalPlayers(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Player Option");
        String[] screen_mode_list = {"VLC Player", "IJK Player", "Exo Player"};
        if (MyApp.instance.getPreference().get(Constants.getCurrentPlayer())!=null)
            current_position = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
        else current_position = 0;
        builder.setSingleChoiceItems(screen_mode_list, current_position,
                (dialog, which) -> current_position =which);
        builder.setPositiveButton("OK", (dialog, which) -> {
            MyApp.instance.getPreference().put(Constants.getCurrentPlayer(), current_position);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showSortingDlg(){
        if (MyApp.instance.getPreference().get(Constants.getSORT())!=null)
            current_position = (int) MyApp.instance.getPreference().get(Constants.getSORT());
        else current_position = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Sorting Method");
        String[] screen_mode_list = {"Sort by Number", "Sort by Added", "Sort by Name"};
        builder.setSingleChoiceItems(screen_mode_list, current_position, (dialog, which) -> current_position =which);
        builder.setPositiveButton("OK", (dialog, which) ->
                MyApp.instance.getPreference().put(Constants.getSORT(), current_position));
        builder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void ReloadDlg(Dialog dlg){
        ReloadDlg reloadDlg = new ReloadDlg(WelcomeNewActivity.this, new ReloadDlg.DialogUpdateListener() {
            @Override
            public void OnUpdateNowClick(Dialog dialog) {
                try {
                    dlg.dismiss();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                startActivity(new Intent(WelcomeNewActivity.this, SplashActivity.class));
                finish();
            }

            @Override
            public void OnUpdateSkipClick(Dialog dialog) {
                dialog.dismiss();
            }
        });
        reloadDlg.show();
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

    private void setDescription(String icon, String title, String body, String bg_icon, String contentUri){
        if (icon!=null && !icon.equalsIgnoreCase(""))
            Picasso.with(this).load(icon)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(channel_icon);
        if (bg_icon!=null && !bg_icon.equalsIgnoreCase(""))
            Picasso.with(this).load(bg_icon)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image_ad1);
        channel_title.setText(title);
        channel_body.setText(body);
        this.contentUri = contentUri;
        playChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeListAdapter.notifyDataSetChanged();
        MyApp.is_welcome = true;
        if (!contentUri.equalsIgnoreCase("")){
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
            if (!mMediaPlayer.isPlaying())playChannel();
        }
        if(MyApp.is_vpn){
            findViewById(R.id.ly_vpn).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.ly_vpn).setVisibility(View.GONE);
        }
        mConn = new VPNConnector(this, true) {
            @Override
            public void onUpdate(OpenVpnService service) {
            }
        };
    }

    private void playChannel() {
        Log.e("url",contentUri);
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

    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

    }

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<WelcomeNewActivity> mOwner;

        public MediaPlayerListener(WelcomeNewActivity owner) {
            mOwner = new WeakReference<WelcomeNewActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            WelcomeNewActivity player = mOwner.get();

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
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    if (menu_recyclerview.getAdapter() instanceof CategoryListAdapter){
                        menu_recyclerview.setAdapter(menuListAdapter);
                        findViewById(R.id.btn_back).setVisibility(View.GONE);
                        findViewById(R.id.btn_search).setVisibility(View.GONE);
                        return false;
                    }else {
                        String string, string1, string2;
                        if (num_server==1){
                            string = "DO YOU WISH TO EXIT APP?";
                            string1 = "Yes";
                            string2 = "No";
                        }else {
                            string = "DO YOU WISH TO EXIT APP OR SWITCH SERVER?";
                            string1 = "EXIT";
                            string2 = "SWITCH SERVER";
                        }
                        ConnectionDlg connectionDlg = new ConnectionDlg(WelcomeNewActivity.this, new ConnectionDlg.DialogConnectionListener() {
                            @Override
                            public void OnYesClick(Dialog dialog) {
                                dialog.dismiss();
                                stopVPN();
                                finish();
                            }

                            @Override
                            public void OnNoClick(Dialog dialog) {
                                dialog.dismiss();
                                if (num_server!=1) {
                                    startActivity(new Intent(WelcomeNewActivity.this, MainActivity.class));
                                    stopVPN();
                                    finish();
                                }
                            }
                        }, string,string1, string2);
                        connectionDlg.show();
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_surface:

                break;
            case R.id.btn_back:
                menu_recyclerview.setAdapter(menuListAdapter);
                findViewById(R.id.btn_back).setVisibility(View.GONE);
                break;
            case R.id.btn_search:
                MyApp.instance.getPreference().put(Constants.getCategory_POS(),1);
                SearchDlg searchDlg = new SearchDlg(this, (dialog, sel_Channel) -> {
                    dialog.dismiss();
                    FullScreencall();
                    int sub_pos=-1;
                    List<EPGChannel> channels=Constants.getAllFullModel(MyApp.fullModels_filter).getChannels();
                    for(int i = 0;i<channels.size();i++){
                        if(channels.get(i).getName().equalsIgnoreCase(sel_Channel.getName())){
                            sub_pos = i;
                            break;
                        }
                    }
                    if (sub_pos!=-1){
                        MyApp.instance.getPreference().put(Constants.getChannel_Pos(),sub_pos);
                        switch (current_player){
                            case 0:
                                startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelActivity.class));
                                break;
                            case 1:
                                startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelIJKActivity.class));
                                break;
                            case 2:
                                startActivity(new Intent(WelcomeNewActivity.this, PreviewChannelExoActivity.class));
                                break;
                        }
                    }
                });
                searchDlg.show();
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

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
                new Thread(this::getRespond).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private int selected_item;
    private void showScreenModeList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select One Mode");

        String[] screen_mode_list = {"Four Way Screen", "Three Way Screen", "Dual Screen"};

        builder.setSingleChoiceItems(screen_mode_list, 0,
                (dialog, which) -> selected_item = which);

        builder.setPositiveButton("OK", (dialog, which) -> {
            final Intent intent=new Intent(WelcomeNewActivity.this, MultiScreenActivity.class);
            if (selected_item==0) {
                boolean remember_four=false;
                if (MyApp.instance.getPreference().get("remember_four_screen")!=null) remember_four=(boolean) MyApp.instance.getPreference().get("remember_four_screen");
                if (!remember_four){
                    PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(WelcomeNewActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                        @Override
                        public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                            if(!pin_code.equals(Constants.GetPin4(WelcomeNewActivity.this))) {
                                Toast.makeText(WelcomeNewActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                return;
                            }
                            MyApp.instance.getPreference().put("remember_four_screen",is_remember);
                            intent.putExtra("num_screen",4);
                            startActivity(intent);
                        }

                        @Override
                        public void OnCancelClick(Dialog dialog, String pin_code) {

                        }
                    },remember_four);
                    pinMultiScreenDlg.show();
                }else {
                    intent.putExtra("num_screen",4);
                    startActivity(intent);
                }
            }
            else if (selected_item==1) {
                boolean remember_three=false;
                if (MyApp.instance.getPreference().get("remember_three_screen")!=null) remember_three=(boolean) MyApp.instance.getPreference().get("remember_three_screen");
                if (!remember_three){
                    PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(WelcomeNewActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                        @Override
                        public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                            if(!pin_code.equals(Constants.GetPin3(WelcomeNewActivity.this))) {
                                Toast.makeText(WelcomeNewActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                return;
                            }
                            MyApp.instance.getPreference().put("remember_three_screen",is_remember);
                            intent.putExtra("num_screen",3);
                            startActivity(intent);
                        }

                        @Override
                        public void OnCancelClick(Dialog dialog, String pin_code) {

                        }
                    },remember_three);
                    pinMultiScreenDlg.show();
                }else {
                    intent.putExtra("num_screen",3);
                    startActivity(intent);
                }
            }
            else {
                boolean remember_two=false;
                if (MyApp.instance.getPreference().get("remember_two_screen")!=null) remember_two=(boolean) MyApp.instance.getPreference().get("remember_two_screen");
                if (!remember_two){
                    PinMultiScreenDlg pinMultiScreenDlg=new PinMultiScreenDlg(WelcomeNewActivity.this, new PinMultiScreenDlg.DlgPinListener() {
                        @Override
                        public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember) {
                            if(!pin_code.equals(Constants.GetPin2(WelcomeNewActivity.this))) {
                                Toast.makeText(WelcomeNewActivity.this,"Invalid password!",Toast.LENGTH_LONG).show();
                                return;
                            }
                            MyApp.instance.getPreference().put("remember_two_screen",is_remember);
                            intent.putExtra("num_screen",2);
                            startActivity(intent);
                        }

                        @Override
                        public void OnCancelClick(Dialog dialog, String pin_code) {

                        }
                    },remember_two);
                    pinMultiScreenDlg.show();
                }else {
                    intent.putExtra("num_screen",2);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
