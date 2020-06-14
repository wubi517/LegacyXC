package com.newlegacyxc.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.newlegacyxc.R;
import com.newlegacyxc.activity.home.WelcomeNewActivity;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.apps.PlayGifView;
import com.newlegacyxc.dialog.ConnectionDlg;
import com.newlegacyxc.epg.EPGData;
import com.newlegacyxc.epg.misc.EPGDataImpl;
import com.newlegacyxc.models.CategoryModel;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;
import com.newlegacyxc.models.FullModel;
import com.newlegacyxc.models.LoginModel;
import com.newlegacyxc.models.MovieModel;
import com.newlegacyxc.models.SeriesModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.newlegacyxc.apps.MyApp.serverUrl;

public class SplashActivity extends AppCompatActivity{
    String user,password,exp_date,xxxcategory_id;
    List<CategoryModel> categories;
    LoginModel loginModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PlayGifView playGifView = findViewById(R.id.splash_gif);
        playGifView.setImageResource(R.raw.start);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playGifView.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        playGifView.setLayoutParams(params);
//        AppInfoModel appInfoModel = (AppInfoModel) MyApp.instance.getPreference().get(Constants.getAppInfoModel());
////
////        goToLogin(appInfoModel.getResult().get(MyApp.firstServer.getValue()-1));
        LoginModel loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.getLoginInfo());
        user = loginModel.getUser_name();
        password = loginModel.getPassword();

        new Thread(this::callLogin).start();
        FullScreencall();

    }

    private void goToLogin(String playlist) {
        try {
            URL url = new URL(playlist);
            serverUrl = url.getProtocol()+"://"+url.getAuthority();
            Log.e("serverUrl",serverUrl);
            MyApp.instance.getIptvclient().setUrl(serverUrl);
            Log.e("query", url.getQuery());
            String[] queries = url.getQuery().split("&");
            user = queries[0].split("=")[1];
            password = queries[1].split("=")[1];
            new Thread(this::callLogin).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }



    private void callLogin() {
        try {
            long startTime = System.nanoTime();
            String responseBody = MyApp.instance.getIptvclient().authenticate(user,password);
            long endTime = System.nanoTime();
            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),responseBody);
            Log.e("BugCheck","authenticate success "+MethodeDuration);
            try {
                JSONObject map = new JSONObject(responseBody);
                MyApp.user = user;
                MyApp.pass = password;
                JSONObject u_m;
                u_m = map.getJSONObject("user_info");
                if (!u_m.has("username")) {
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                } else {
                    MyApp.created_at = u_m.getString("created_at");
                    MyApp.status = u_m.getString("status");
                    if(!MyApp.status.equalsIgnoreCase("Active")){
                        Intent intent =new Intent(SplashActivity.this,EmptyActivity.class);
                        intent.putExtra("msg","Your account is Expired");
                        startActivity(intent);
                        return;
                    }
                    MyApp.is_trail = u_m.getString("is_trial");
                    MyApp.active_cons = u_m.getString("active_cons");
                    MyApp.max_cons = u_m.getString("max_connections");
                    String exp_date;
                    try{
                        exp_date = u_m.getString("exp_date");
                    }catch (Exception e){
                        exp_date = "unlimited";
                    }
                    LoginModel loginModel = new LoginModel();
                    loginModel.setUser_name(MyApp.user);
                    loginModel.setPassword(MyApp.pass);
                    try{
                        loginModel.setExp_date(exp_date);
                    }catch (Exception e){
                        loginModel.setExp_date("unlimited");
                    }
                    MyApp.loginModel = loginModel;
                    MyApp.instance.getPreference().put(Constants.getLoginInfo(), loginModel);

                    JSONObject serverInfo= map.getJSONObject("server_info");
                    String  my_timestamp= serverInfo.getString("timestamp_now");
                    String server_timestamp= serverInfo.getString("time_now");
                    Constants.setServerTimeOffset(my_timestamp,server_timestamp);
                    callVodCategory();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() ->{
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                } );
            }
        } catch (Exception e0) {
            e0.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLogin()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN UNSUCCESSFUL PLEASE CHECK YOUR LOGIN DETAILS OR CONTACT YOUR PROVIDER",null,null);
                connectionDlg.show();
            });
        }
    }

    private void callVodCategory(){
        try {
            long startTime = System.nanoTime();
            //api call here
            String map = MyApp.instance.getIptvclient().getMovieCategories(user,password);
            long endTime = System.nanoTime();
            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getMovieCategories success "+MethodeDuration);
            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            categories = new ArrayList<>();
            categories.add(getRecentMovies());
            categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
            categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
            categories.add(new CategoryModel(Constants.no_name_id,Constants.No_Name_Category,""));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
            }catch (Exception e){
                e.printStackTrace();
            }
            MyApp.vod_categories = categories;
            for (CategoryModel categoryModel: categories){
                String category_name = categoryModel.getName().toLowerCase();
                if(category_name.contains("adult")||category_name.contains("xxx")){
                    Constants.xxx_vod_category_id = categoryModel.getId();
                    Log.e("LoginActivity","xxx_vod_category_id: "+Constants.xxx_vod_category_id);
                }
            }
            callLiveCategory();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callVodCategory()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA",null,null);
                connectionDlg.show();
            });
        }
    }

    private void callLiveCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveCategories success "+MethodeDuration);
            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<CategoryModel> categories;
            categories = new ArrayList<>();
            categories.add(new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,""));
            categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
            categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
            }catch (Exception e){
                e.printStackTrace();
            }
            MyApp.live_categories = categories;
            for (CategoryModel categoryModel: categories){
                String category_name = categoryModel.getName().toLowerCase();
                if(category_name.contains("adult")||category_name.contains("xxx")){
                    Constants.xxx_category_id = categoryModel.getId();
                    Log.e("LoginActivity","xxx_category_id: "+Constants.xxx_category_id);
                }
            }
            callSeriesCategory();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveCategory()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA",null,null);
                connectionDlg.show();
            });
        }
    }

    private void callSeriesCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeriesCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeriesCategories success "+MethodeDuration);
            Gson gson=new Gson();

            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<CategoryModel> categories;
            categories = new ArrayList<>();
            categories.add(new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,""));
            categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
            categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
            categories.add(new CategoryModel(Constants.no_name_id,Constants.No_Name_Category,""));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
            }catch (Exception e){
                e.printStackTrace();
            }
            MyApp.series_categories = categories;
            callLiveStreams();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeriesCategory()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA",null,null);
                connectionDlg.show();
            });
        }
    }

    private void callLiveStreams(){
        try{
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveStreams(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveStreams success "+MethodeDuration);
            try {
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<EPGChannel> channelModels = new ArrayList<>();
                Gson gson=new Gson();
                try{
                    channelModels = new ArrayList<>(gson.fromJson(map, new TypeToken<List<EPGChannel>>() {}.getType()));
                    for (EPGChannel epgChannel:channelModels){
                        if (epgChannel.getCategory_id().equals(Constants.xxx_category_id))
                            epgChannel.setIs_locked(true);
                        else epgChannel.setIs_locked(false);
                    }
                }catch (Exception e){
                    JSONArray response;
                    try {
                        response=new JSONArray(map);
                        for (int i=0;i<response.length();i++){
                            JSONObject jsonObject=response.getJSONObject(i);
                            EPGChannel epgChannel=new EPGChannel();
                            try{
                                epgChannel.setNum(jsonObject.getString("num"));
                            }catch (JSONException e1){
                                epgChannel.setNum("");
                            }
                            try{
                                epgChannel.setName(jsonObject.getString("name"));
                            }catch (JSONException e2){
                                epgChannel.setName("");
                            }
                            try{
                                epgChannel.setStream_type(jsonObject.getString("stream_type"));
                            }catch (JSONException e3){
                                epgChannel.setStream_type("");
                            }
                            try{
                                epgChannel.setStream_id(jsonObject.getString("stream_id"));
                            }catch (JSONException e4){
                                epgChannel.setStream_id("-1");
                            }
                            try{
                                epgChannel.setStream_icon(jsonObject.getString("stream_icon"));
                            }catch (JSONException e5){
                                epgChannel.setStream_icon("");
                            }
                            try{
                                epgChannel.setChannelID(jsonObject.getInt("epg_channel_id"));
                            }catch (JSONException e1){
                                epgChannel.setChannelID(-1);
                            }
                            try{
                                epgChannel.setAdded(jsonObject.getString("added"));
                            }catch (JSONException e1){
                                epgChannel.setAdded("");
                            }
                            try{
                                epgChannel.setCategory_id(jsonObject.getString("category_id"));
                                if (epgChannel.getCategory_id().equals(Constants.xxx_category_id))
                                    epgChannel.setIs_locked(true);
                                else epgChannel.setIs_locked(false);
                            }catch (JSONException e1){
                                epgChannel.setCategory_id("-1");
                            }
                            try{
                                epgChannel.setCustom_sid(jsonObject.getString("custom_sid"));
                            }catch (JSONException e1){
                                epgChannel.setCustom_sid("");
                            }
                            try{
                                epgChannel.setTv_archive(jsonObject.getString("tv_archive"));
                            }catch (JSONException e1){
                                epgChannel.setTv_archive("0");
                            }
                            try{
                                epgChannel.setDirect_source(jsonObject.getString("direct_source"));
                            }catch (JSONException e1){
                                epgChannel.setDirect_source("");
                            }
                            try{
                                epgChannel.setTv_archive_duration(jsonObject.getString("tv_archive_duration"));
                            }catch (JSONException e1){
                                epgChannel.setTv_archive_duration("0");
                            }
                            channelModels.add(epgChannel);
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                MyApp.channel_size = channelModels.size();
                Log.e("allchannelsize",channelModels.size()+"");
                List<FullModel> fullModels = new ArrayList<>();
                fullModels.add(new FullModel(Constants.recent_id, getRecentChannels(channelModels), Constants.Recently_Viewed,0));
                fullModels.add(new FullModel(Constants.all_id, channelModels,"All Channel",0));
                if(MyApp.instance.getPreference().get(Constants.getFavChannelNames())==null){
                    fullModels.add(new FullModel(Constants.fav_id, new ArrayList<>(),"My Favorites",0));
                }else {
                    List<EPGChannel> epgChannels = new ArrayList<>();
                    for(int i = 0;i<channelModels.size();i++){
                        List<String> fav = (List<String>) MyApp.instance.getPreference().get(Constants.getFavChannelNames());
                        for(int j=0;j< fav.size();j++){
                            if(channelModels.get(i).getName().equals(fav.get(j))){
                                channelModels.get(i).setIs_favorite(true);
                                epgChannels.add(channelModels.get(i));
                            }else {
                                channelModels.get(i).setIs_favorite(false);
                            }
                        }
                    }
                    fullModels.add(new FullModel(Constants.fav_id, epgChannels,"My Favorites",epgChannels.size()));
                }

                List<String> datas = new ArrayList<>();
                datas.add(Constants.Recently_Viewed);
                datas.add("All Channel");
                datas.add("My Favorites");
                for(int i = 3; i< MyApp.live_categories.size(); i++){
                    String category_id = MyApp.live_categories.get(i).getId();
                    String category_name = MyApp.live_categories.get(i).getName();
                    int count =0;
                    List<EPGChannel> chModels = new ArrayList<>();
                    for(int j = 0;j<channelModels.size();j++){
                        EPGChannel chModel = channelModels.get(j);

                        if(category_id.equals(chModel.getCategory_id())){
                            chModels.add(chModel);
                            if (chModel.getTv_archive().equals("1")) count+=1;
                        }
                    }
//                    if(chModels.size()<1){
//                        continue;
//                    }
                    datas.add(MyApp.live_categories.get(i).getName());
                    fullModels.add(new FullModel(MyApp.live_categories.get(i).getName(),chModels,category_name,count));
                    Log.e("catchable_count",count+"");
                }
                MyApp.fullModels = fullModels;
                MyApp.maindatas = datas;
                int count_catchable=0;
                for (int i=Constants.unCount_number;i<MyApp.fullModels.size();i++){
                    count_catchable+=MyApp.fullModels.get(i).getCatchable_count();
                }
                Constants.getAllFullModel(MyApp.fullModels).setCatchable_count(count_catchable);
                Log.e("total catchable_count",count_catchable+"");
            }catch (Exception e){
                e.printStackTrace();
                Log.e("catch","catch");
            }
            callSeries();
        }catch (Exception e){
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveStreams()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA",null,null);
                connectionDlg.show();
            });
        }
    }

    private void callSeries() {
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeries(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
//            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeries success "+MethodeDuration);
            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<SeriesModel> allSeriesModels = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(map);
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    try {
                        SeriesModel seriesModel = gson.fromJson(jsonObject.toString(),SeriesModel.class);
                        allSeriesModels.add(seriesModel);
                    }catch (Exception ignored){}
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            allSeriesModels.addAll((Collection<? extends SeriesModel>) gson.fromJson(map, new TypeToken<List<SeriesModel>>(){}.getType()));
            MyApp.seriesModels = allSeriesModels;
            putRecentSeriesModels(allSeriesModels);
            putFavSeriesModels(allSeriesModels);
            Constants.getRecentCatetory(MyApp.series_categories).setSeriesModels(MyApp.recentSeriesModels);
            Constants.getAllCategory(MyApp.series_categories).setSeriesModels(allSeriesModels);
            Constants.getFavoriteCatetory(MyApp.series_categories).setSeriesModels(MyApp.favSeriesModels);
            Constants.putSeries(allSeriesModels);

            callMovieStreams();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeries()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA", null,null);
                connectionDlg.show();
            });
        }
    }

    private void putRecentSeriesModels(List<SeriesModel> allSeriesModels) {
        List<String> fav_series_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentSeries());
        MyApp.recentSeriesModels=new ArrayList<>();
        if (fav_series_names!=null){
            if (fav_series_names.size()!=0)
                for (SeriesModel seriesModel:allSeriesModels){
                    for (String name:fav_series_names){
                        if (name.equals(seriesModel.getName())) {
                            MyApp.recentSeriesModels.add(seriesModel);
                            break;
                        }
                    }
                }
            Log.e("recent_series_models",MyApp.recentSeriesModels.size()+" seriesRecent "+fav_series_names.size()+" names");
        }
    }

    private void putFavSeriesModels(List<SeriesModel> allSeriesModels) {
        List<String> fav_series_names=(List<String>) MyApp.instance.getPreference().get(Constants.getFAV_SERIES());
        MyApp.favSeriesModels=new ArrayList<>();
        if (fav_series_names!=null){
            if (fav_series_names.size()!=0)
                for (SeriesModel seriesModel:allSeriesModels){
                    for (String name:fav_series_names){
                        if (name.equals(seriesModel.getName())) {
                            MyApp.favSeriesModels.add(seriesModel);
                            seriesModel.setIs_favorite(true);
                            break;
                        }
                    }
                }
            Log.e("fav_series_models",MyApp.favSeriesModels.size()+" seriesFav "+fav_series_names.size()+" names");
        }
    }

    private void putFavoriteMovies(List<MovieModel> movieModels) {
        List<MovieModel> favMovies = (List<MovieModel>) MyApp.instance.getPreference().get(Constants.getFAV_VOD_MOVIES());
        if (favMovies!=null && favMovies.size()>0){
            MyApp.favMovieModels=favMovies;
            for (MovieModel movieModel:movieModels){
                for (MovieModel fav:favMovies){
                    if (movieModel.getStream_id().equals(fav.getStream_id())){
                        movieModel.setIs_favorite(true);
                        break;
                    }
                }
            }
        }
        else {
            MyApp.favMovieModels=new ArrayList<>();
        }
    }

    private void callMovieStreams() {
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getMovies(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
//            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getMovies success "+MethodeDuration);
            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<MovieModel> movieModels = new ArrayList<>();
            try {
                movieModels.addAll(gson.fromJson(map, new TypeToken<List<MovieModel>>() {}.getType()));
            }catch (Exception e){
                e.printStackTrace();
            }
            for (MovieModel movieModel:movieModels){
                if (movieModel.getCategory_id()!=null && movieModel.getCategory_id().equals(Constants.xxx_vod_category_id))
                    movieModel.setIs_locked(true);
                else movieModel.setIs_locked(false);
            }
            MyApp.movieModels = movieModels;
            putFavoriteMovies(movieModels);
            Constants.getAllCategory(MyApp.vod_categories).setMovieModels(movieModels);
            Constants.getFavoriteCatetory(MyApp.vod_categories).setMovieModels(MyApp.favMovieModels);
            Constants.putMovies(movieModels);
            callAllEpg();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callMovieStreams()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA", null,null);
                connectionDlg.show();
            });
        }
    }

    private boolean is_data_loaded = false;

    private void callAllEpg() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String localFileName = getApplicationInfo().dataDir+"/localXml"+MyApp.firstServer.getValue()+".txt";
            File file = new File(localFileName);
            String inputStream;
            InputStream fileStream;
            Log.e("BugCheck","getAllEPG start ");
            if (file.exists() && simpleDateFormat.format(new Date()).equals((String)MyApp.instance.getPreference().get(Constants.getLastXMLDate()))){
                fileStream = new FileInputStream(localFileName);
            }else {
                long startTime = System.nanoTime();
                //api call here
                inputStream = MyApp.instance.getIptvclient().getAllEPG(MyApp.user,MyApp.pass);
                long endTime = System.nanoTime();
                long MethodDuration = (endTime - startTime);
                //            Log.e(getClass().getSimpleName(),inputStream);
                Log.e("BugCheck","getAllEPG success "+MethodDuration);
                if (inputStream.length() == 0) return;
                byte[] bytes = new byte[1024];
                int numRead, numWritten = 0;
                fileStream = new ByteArrayInputStream(inputStream.getBytes(Charset.forName("UTF-8")));
                OutputStream out = null;
                file.delete();
                out = new BufferedOutputStream(new FileOutputStream(localFileName));
                while ((numRead = fileStream.read(bytes)) != -1) {
                    out.write(bytes, 0, numRead);
                    numWritten += numRead;
                }
                out.close();
                fileStream.close();
                fileStream = new FileInputStream(localFileName);
                Log.e("BugCheck","write file success ");
                MyApp.instance.getPreference().put(Constants.getLastXMLDate(),simpleDateFormat.format(new Date()));
            }

//            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            String str = null;
//            while ((str = bufferedReader.readLine()) != null) {
//                buffer.append(str);
//            }
//            bufferedReader.close();
//            inputStreamReader.close();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = null;
            //        Log.e("xml result","received");
            try {
                parser = parserFactory.newSAXParser();
                DefaultHandler handler = new DefaultHandler(){
                    String currentValue = "";
                    boolean currentElement = false;
                    EPGEvent prevEvent=null;
                    EPGEvent currentEvent=null;
                    String channel="";
                    List<EPGChannel> currentChannelList;
                    List<EPGEvent> epgModels=new ArrayList<>();
                    public void startElement(String uri, String localName,String qName, Attributes attributes) {
                        currentElement = true;
                        currentValue = "";
//                    Log.e("response","received");
                        if(localName.equals("programme")){
//                        Log.e("response","started programs list");
                            currentEvent = new EPGEvent();
                            String start=attributes.getValue(0);
                            String end=attributes.getValue(1);
                            currentEvent.setStart_timestamp(start);//.split(" ")[0]
                            currentEvent.setStop_timestamp(end);//.split(" ")[0]
                            if (!channel.equals(attributes.getValue(2))) {
                                if (currentChannelList !=null && !currentChannelList.isEmpty()) {
                                    Collections.sort(epgModels, new Comparator<EPGEvent>(){
                                        public int compare(EPGEvent o1, EPGEvent o2){
                                            return o1.getStart_timestamp().compareTo(o2.getStart_timestamp());
                                        }
                                    });
                                    List<EPGEvent> epgEvents;
                                    EPGEvent firstEPGEvent=null,prevEPGEvent=null, currentEPGEvent=null;
                                    for (EPGChannel epgChannel:currentChannelList) {
                                        epgEvents = new ArrayList<>();
                                        for (EPGEvent epgEvent:epgModels){
                                            EPGEvent event = new EPGEvent(epgEvent);
                                            if (firstEPGEvent==null) firstEPGEvent=event;
                                            currentEPGEvent = event;
                                            if (prevEPGEvent!=null) {
                                                currentEPGEvent.setPreviousEvent(prevEPGEvent);
                                                prevEPGEvent.setNextEvent(currentEPGEvent);
                                            }
                                            currentEPGEvent.setChannel(epgChannel);
                                            epgEvents.add(currentEPGEvent);
                                            prevEPGEvent = event;
                                        }
                                        if (firstEPGEvent!=null){
                                            firstEPGEvent.setPreviousEvent(currentEPGEvent);
                                            currentEPGEvent.setNextEvent(firstEPGEvent);
                                        }
                                        epgChannel.setEvents(epgEvents);
                                    }
                                }
                                epgModels=new ArrayList<>();
                                channel=attributes.getValue(2);
                                currentChannelList =findChannelByid(channel);
                                if (currentChannelList.size()>0 &&
                                        currentChannelList.get(0).getEvents()!=null &&
                                        currentChannelList.get(0).getEvents().size()>0)
                                    epgModels = currentChannelList.get(0).getEvents();
                            }
                        }
                    }
                    public void endElement(String uri, String localName, String qName) {
                        currentElement = false;
                        if (localName.equalsIgnoreCase("title"))
                            currentEvent.setTitle(currentValue);
                        else if (localName.equalsIgnoreCase("desc"))
                            currentEvent.setDec(currentValue);
                        else if (localName.equalsIgnoreCase("programme")) {
                            if (prevEvent!=null){
                                currentEvent.setPreviousEvent(prevEvent);
                                prevEvent.setNextEvent(currentEvent);
                            }
                            prevEvent=currentEvent;
                            for (EPGEvent epgEvent:epgModels){
                                if (epgEvent.getTitle().equals(currentEvent.getTitle()) &&
                                        epgEvent.getDec().equals(currentEvent.getDec()) &&
                                        epgEvent.getStart_timestamp().equals(currentEvent.getStart_timestamp()) &&
                                        epgEvent.getStop_timestamp().equals(currentEvent.getStop_timestamp()))
                                    return;
                            }
                            epgModels.add(currentEvent);
                        }
                        else if (localName.equalsIgnoreCase("tv")){
                            //
                            is_data_loaded=true;
                            Constants.getLiveFilter();
                            getCatchModels();
                        }
                    }
                    @Override
                    public void characters(char[] ch, int start, int length) {
                        if (currentElement) {
                            currentValue = currentValue +  new String(ch, start, length);
                        }
                    }
                };

                parser.parse(fileStream,handler);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
                is_data_loaded=true;
                Constants.getLiveFilter();
                getCatchModels();
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(SplashActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callAllEpg()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(SplashActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA",null,null);
                connectionDlg.show();
            });
        }
    }

    private List<EPGChannel> findChannelByid(String channel_id){
        List<EPGChannel> channelList = new ArrayList<>();
//        Log.e("allfullmodel",MyApp.fullModels.size()+"");
        List<EPGChannel> entireChannels =Constants.getAllFullModel(MyApp.fullModels).getChannels();
        for (EPGChannel epgChannel : entireChannels) {
            if (epgChannel.getId().equals(channel_id)) {
                channelList.add(epgChannel);
            }
        }
        return channelList;
    }

    private List<EPGChannel> getRecentChannels(List<EPGChannel> epgChannels){
        List<EPGChannel> recentChannels=new ArrayList<>();
        if(MyApp.instance.getPreference().get(Constants.getRecentChannels())!=null){
            List<String> recent_channel_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentChannels());
            for(int j=0;j< recent_channel_names.size();j++){
                for(int i = 0;i<epgChannels.size();i++){
                    if(epgChannels.get(i).getName().equals(recent_channel_names.get(j))){
                        recentChannels.add(epgChannels.get(i));
                    }
                }
            }
        }
        return recentChannels;
    }

    private CategoryModel getRecentMovies() {
        CategoryModel recentCategory = new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,"");
        List<MovieModel> recentMovies=(List<MovieModel>) MyApp.instance.getPreference().get(Constants.getRecentMovies());
        if (recentMovies!=null){
            MyApp.recentMovieModels=recentMovies;
            recentCategory.setMovieModels(recentMovies);
        }
        else {
            MyApp.recentMovieModels=new ArrayList<>();
            recentCategory.setMovieModels(new ArrayList<>());
        }
        return recentCategory;
    }

    private void getCatchModels() {
        MyApp.epgDatas = new ArrayList<>();
        if (MyApp.fullModels==null || MyApp.fullModels.size()==0) return;
        for (int j=0;j<MyApp.fullModels.size();j++){
            FullModel fullModel=MyApp.fullModels.get(j);
            EPGData epgData= new EPGDataImpl(fullModel.getCategory_id(),fullModel.getChannels());
            MyApp.epgDatas.add(epgData);
        }
        if (MyApp.epgDatas!=null && MyApp.epgDatas.size()>1) MyApp.epgData = Constants.getAllEPGData(MyApp.epgDatas);
        else  MyApp.epgData = new EPGDataImpl(Constants.all_id,new ArrayList<>());
        getAuthorization();
    }

    private void getAuthorization(){
        startActivity(new Intent(SplashActivity.this, WelcomeNewActivity.class));
        finish();
    }
    public void FullScreencall() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
