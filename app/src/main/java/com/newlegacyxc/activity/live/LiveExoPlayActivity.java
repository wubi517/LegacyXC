package com.newlegacyxc.activity.live;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.dialog.PackageDlg;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.ParsingLoadable;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.newlegacyxc.apps.Constants.catchupFormat;


public class LiveExoPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener,View.OnClickListener, PlaybackPreparer, PlayerControlView.VisibilityListener,
        View.OnFocusChangeListener {
    private static final String TAG = "JavaActivity";


    public static final String DRM_SCHEME_EXTRA = "drm_scheme";
    public static final String DRM_LICENSE_URL_EXTRA = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES_EXTRA = "drm_key_request_properties";
    public static final String DRM_MULTI_SESSION_EXTRA = "drm_multi_session";
    public static final String PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";

    public static final String AD_TAG_URI_EXTRA = "ad_tag_uri";

    public static final String ABR_ALGORITHM_EXTRA = "abr_algorithm";
    private static final String ABR_ALGORITHM_DEFAULT = "default";
    private static final String ABR_ALGORITHM_RANDOM = "random";

    private static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";

    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private PlayerView playerView;
    private FrameLayout frameLayout;
    private TextView debugTextView;
    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private FrameworkMediaDrm mediaDrm;
    private MediaSource mediaSource;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private DebugTextViewHelper debugViewHelper;
    private TrackGroupArray lastSeenTrackGroupArray;

    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private AdsLoader adsLoader;
    private Uri loadedAdTagUri;
    private ViewGroup adUiViewGroup;


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
    int duration_time = 0,msg_time = 0,duration = 0,mStream_id = 0;
    long start_mil;
    long now_mil;
    long current_mil;

    boolean is_create = true,is_long=false;
    boolean is_rss = false,is_msg = false, is_live = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private RelativeLayout lay_header;
    SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd HH:mm");
    EPGChannel epgModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_exo_player);
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

        mediaDataSourceFactory = buildDataSourceFactory(true);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        lay_header = findViewById(R.id.lay_header);

        frameLayout = findViewById(R.id.rootVideoPlayerMovieActivity);
        playerView = findViewById(R.id.player_view);
        playerView.setControllerVisibilityListener(this);
        playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
        playerView.requestFocus();

        debugTextView = findViewById(R.id.debug_text_view);

        changeVideoViewSize();
        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        } else {
            trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
            clearStartPosition();
        }

        def_lay =  findViewById(R.id.def_lay);
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

        cont_url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
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

        txt_title.setText(title);

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

    private void addFav(){
        if (epgModel.is_favorite()) {
            pkg_datas.set(0, "Add to Fav");
            epgModel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i <Constants.getFavFullModel(MyApp.fullModels_filter).getChannels().size(); i++) {
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

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            playVideo(cont_url);
        }
    }

//    private void getRespond(){
//        if (!MyApp.is_announce_enabled) return;
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
//        Log.e("url",path);
        toggleFullscreen(true);
        try {
            if (player == null) {
                Intent intent = this.getIntent();

                Uri[] uris = new Uri[1];
                String[] extensions = new String[1];

                uris[0] = Uri.parse(path);

                DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
                if (intent.hasExtra(DRM_SCHEME_EXTRA) || intent.hasExtra(DRM_SCHEME_UUID_EXTRA)) {
                    String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL_EXTRA);
                    String[] keyRequestPropertiesArray =
                            intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES_EXTRA);
                    boolean multiSession = intent.getBooleanExtra(DRM_MULTI_SESSION_EXTRA, false);
                    int errorStringId = R.string.error_drm_unknown;
                    if (Util.SDK_INT < 18) {
                        errorStringId = R.string.error_drm_not_supported;
                    } else {
                        try {
                            String drmSchemeExtra = intent.hasExtra(DRM_SCHEME_EXTRA) ? DRM_SCHEME_EXTRA
                                    : DRM_SCHEME_UUID_EXTRA;
                            UUID drmSchemeUuid = Util.getDrmUuid(intent.getStringExtra(drmSchemeExtra));
                            if (drmSchemeUuid == null) {
                                errorStringId = R.string.error_drm_unsupported_scheme;
                            } else {
                                drmSessionManager =
                                        buildDrmSessionManagerV18(
                                                drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray, multiSession);
                            }
                        } catch (UnsupportedDrmException e) {
                            errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                        }
                    }
                    if (drmSessionManager == null) {
                        showToast(errorStringId);
                        releaseMediaPlayer();
                        return;
                    }
                }

                TrackSelection.Factory trackSelectionFactory;
                String abrAlgorithm = intent.getStringExtra(ABR_ALGORITHM_DEFAULT);
                if (abrAlgorithm == null || ABR_ALGORITHM_DEFAULT.equals(abrAlgorithm)) {
                    trackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
                } else if (ABR_ALGORITHM_RANDOM.equals(abrAlgorithm)) {
                    trackSelectionFactory = new RandomTrackSelection.Factory();
                } else {
                    showToast(R.string.error_unrecognized_abr_algorithm);
                    releaseMediaPlayer();
                    return;
                }

                boolean preferExtensionDecoders =
                        intent.getBooleanExtra(PREFER_EXTENSION_DECODERS_EXTRA, false);
                @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                        MyApp.instance.useExtensionRenderers()
                                ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                DefaultRenderersFactory renderersFactory =
                        new DefaultRenderersFactory(this, extensionRendererMode);

                trackSelector = new DefaultTrackSelector(trackSelectionFactory);
                trackSelector.setParameters(trackSelectorParameters);
                lastSeenTrackGroupArray = null;

                player = ExoPlayerFactory.newSimpleInstance(this,renderersFactory, trackSelector, drmSessionManager);
                player.addListener(new PlayerEventListener());
                player.setPlayWhenReady(startAutoPlay);
                player.addAnalyticsListener(new EventLogger(trackSelector));
                playerView.setPlayer(player);
                playerView.setPlaybackPreparer(this);

                debugViewHelper = new DebugTextViewHelper(player, debugTextView);
                debugViewHelper.start();

                MediaSource[] mediaSources = new MediaSource[uris.length];
                for (int i = 0; i < uris.length; i++) {
                    mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
                }
                mediaSource =
                        mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
                String adTagUriString = intent.getStringExtra(AD_TAG_URI_EXTRA);
                if (adTagUriString != null) {
                    Uri adTagUri = Uri.parse(adTagUriString);
                    if (!adTagUri.equals(loadedAdTagUri)) {
                        releaseAdsLoader();
                        loadedAdTagUri = adTagUri;
                    }
                    MediaSource adsMediaSource = createAdsMediaSource(mediaSource, Uri.parse(adTagUriString));
                    if (adsMediaSource != null) {
                        mediaSource = adsMediaSource;
                    } else {
                        showToast(R.string.ima_not_loaded);
                    }
                } else {
                    releaseAdsLoader();
                }
            }
            boolean haveStartPosition = startWindow != C.INDEX_UNSET;
            if (haveStartPosition) {
                player.seekTo(startWindow, startPosition);
            }
            player.prepare(mediaSource, !haveStartPosition, false);
        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }

        if(is_live){
            updateProgressBarLive();
        }else {
            updateProgressBar();
        }
        updateTimer();

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
            if (player != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm a EEE MM/dd");
                txt_date.setText(dateFormat.format(new Date()));
                current_mil = System.currentTimeMillis();
                int pass_min = (int) ((current_mil - now_mil)/(1000*60));
                int remain_min = duration- pass_min;
                int progress;
                if (duration!=0) progress = pass_min * 100/(pass_min+remain_min);
                else progress = 0;
                txt_last_time.setText(getFromDate1(now_mil + duration*60*1000));
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
            if (player != null) {
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
            if (Util.SDK_INT <= 23 || player == null) {
                playVideo(cont_url);
            }
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
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releaseMediaPlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releaseMediaPlayer();
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
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            debugViewHelper.stop();
            debugViewHelper = null;
            player.release();
            player = null;
            mediaSource = null;
            trackSelector = null;
        }
        releaseMediaDrm();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                showAudioOptions();
                break;
            case R.id.ly_subtitle:
                showSubtitlesOptions();
                break;
            case R.id.ly_resolution:

                break;

            case R.id.btn_play:
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                } else {
                    player.setPlayWhenReady(true);
                }
                break;
            case R.id.ly_fav:
                break;
            case R.id.btn_rewind:
                if(playerView.getVisibility()==View.VISIBLE && !is_live){
                    mHandler.removeCallbacks(mTicker);
                    Date date1 = new Date();
                    if (date1.getTime() > now_mil + 60 * 1000) {
                        now_mil += 60 * 1000;
                        releaseMediaPlayer();
                        start_mil = start_mil-60*1000;
                        start_time = getFromCatchDate(start_mil);
                        duration = duration+1;
                        cont_url = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass, mStream_id+"",start_time, duration);
                        playVideo(cont_url);
                        ly_bottom.setVisibility(View.VISIBLE);
                        updateProgressBar();
                        startTimer();
                    }
                }
                break;
            case R.id.btn_forward:
                if(playerView.getVisibility()==View.VISIBLE && !is_live){
                    mHandler.removeCallbacks(mTicker);
                    now_mil -= 60*1000;
                     releaseMediaPlayer();
                    start_mil = start_mil+60*1000;
                    start_time = getFromCatchDate(start_mil);
                    duration = duration-1;
                    cont_url = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass, mStream_id+"",start_time, duration);
                    playVideo(cont_url);
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
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
                            is_long = false;
                            switch (position) {
                                case 0:
                                    addFav();
                                    break;
                                case 1:
                                    showSubtitlesOptions();
                                    break;
                                case 2:
                                    showAudioOptions();
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


    //ExoPlayer

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return MyApp.instance.buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    @Override
    public void preparePlayback() {

    }

    @Override
    public void onVisibilityChange(int visibility) {

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

//    private void getEpg(){
//        try {
//            String map = MyApp.instance.getIptvclient().getShortEPG(MyApp.user,MyApp.pass,
//                    mStream_id+"",4);
//            Log.e(getClass().getSimpleName(),map);
//            Gson gson=new Gson();
//            map=map.replaceAll("[^\\x00-\\x7F]", "");
//            if (!map.contains("null_error_response")){
//                Log.e("response",map);
//                try {
//                    JSONObject jsonObject= new JSONObject(map);
//                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
//                    epgModelList = new ArrayList<>();
//                    epgModelList.addAll((Collection<? extends EPGEvent>) gson.fromJson(jsonArray.toString(), new TypeToken<List<EPGEvent>>(){}.getType()));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString =
                                    getString(
                                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                        } else {
                            errorString =
                                    getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString =
                                getString(
                                        R.string.error_instantiating_decoder,
                                        decoderInitializationException.decoderName);
                    }
                }
            }
            return Pair.create(0, errorString);
        }
    }

    private void changeVideoViewSize(){
        WindowManager wm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            ViewGroup.LayoutParams paramsPlayerView=frameLayout.getLayoutParams();
            paramsPlayerView.height = MyApp.SCREEN_HEIGHT;
            paramsPlayerView.width = MyApp.SCREEN_WIDTH;
            frameLayout.setLayoutParams(paramsPlayerView);
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private void releaseAdsLoader() {
        if (adsLoader != null) {
            adsLoader.release();
            adsLoader = null;
            loadedAdTagUri = null;
            playerView.getOverlayFrameLayout().removeAllViews();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            releaseMediaPlayer();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);

        super.onSaveInstanceState(outState);
    }

    private void updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector.getParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void releaseMediaDrm() {
        if (mediaDrm != null) {
            mediaDrm.release();
            mediaDrm = null;
        }
    }

    private List<?> getOfflineStreamKeys(Uri uri) {
        return MyApp.instance.getDownloadTracker().getOfflineStreamKeys(uri);
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
            UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession)
            throws UnsupportedDrmException {
        HttpDataSource.Factory licenseDataSourceFactory =
                MyApp.instance.buildHttpDataSourceFactory(/* listener= */ null);
        HttpMediaDrmCallback drmCallback =
                new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        releaseMediaDrm();
        mediaDrm = FrameworkMediaDrm.newInstance(uuid);
        return new DefaultDrmSessionManager<>(uuid, mediaDrm, drmCallback, null, multiSession);
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        private  final String LOG_TAG =  PlayerEventListener.class.getSimpleName();

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            super.onPlaybackParametersChanged(playbackParameters);
            Log.d(LOG_TAG,"onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            super.onSeekProcessed();
            Log.d(LOG_TAG,"onSeekProcessed");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                showControls();
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (player.getPlaybackError() != null) {
                updateStartPosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                playVideo(cont_url);
            } else {
                updateStartPosition();
                showControls();
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
    }

    private void showControls() {
        ly_bottom.setVisibility(View.VISIBLE);
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }
    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParserFactory(new HlsPlaylistParserFactory() {
                            @Override
                            public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser() {
                                return null;
                            }

                            @Override
                            public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser(HlsMasterPlaylist masterPlaylist) {
                                return null;
                            }
                        })
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private @Nullable
    MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) {
        try {
            Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");
            if (adsLoader == null) {
                Constructor<? extends AdsLoader> loaderConstructor =
                        loaderClass
                                .asSubclass(AdsLoader.class)
                                .getConstructor(android.content.Context.class, Uri.class);
                adsLoader = loaderConstructor.newInstance(this, adTagUri);
                adUiViewGroup = new FrameLayout(this);
                playerView.getOverlayFrameLayout().addView(adUiViewGroup);
            }
            AdsMediaSource.MediaSourceFactory adMediaSourceFactory =
                    new AdsMediaSource.MediaSourceFactory() {
                        @Override
                        public MediaSource createMediaSource(Uri uri) {
                            return buildMediaSource(uri);
                        }

                        @Override
                        public int[] getSupportedTypes() {
                            return new int[] {C.TYPE_DASH, C.TYPE_SS, C.TYPE_HLS, C.TYPE_OTHER};
                        }
                    };
            return new AdsMediaSource(mediaSource, adMediaSourceFactory, adsLoader, (AdsLoader.AdViewProvider) adUiViewGroup);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showAudioOptions(){
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = "Audio";
            int rendererIndex = 1;
            /*int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
            boolean allowAdaptiveSelections =
                    rendererType == C.TRACK_TYPE_VIDEO
                            || (rendererType == C.TRACK_TYPE_AUDIO
                            && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);*/
            Pair<AlertDialog, TrackSelectionView> dialogPair =
                    TrackSelectionView.getDialog(this, title, trackSelector, rendererIndex);
            dialogPair.second.setShowDisableOption(false);
            dialogPair.second.setAllowAdaptiveSelections(false);
            dialogPair.first.show();
        }
    }

    private void showSubtitlesOptions(){
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = "Subtitles";
            int rendererIndex = 2;
            /*int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
            boolean allowAdaptiveSelections =
                    rendererType == C.TRACK_TYPE_VIDEO
                            || (rendererType == C.TRACK_TYPE_AUDIO
                            && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);*/
            Pair<AlertDialog, TrackSelectionView> dialogPair =
                    TrackSelectionView.getDialog(this, title, trackSelector, rendererIndex);
            dialogPair.second.setShowDisableOption(true);
            dialogPair.second.setAllowAdaptiveSelections(false);
            dialogPair.second.removeViewAt(4);
            dialogPair.first.show();
        }
    }

}
