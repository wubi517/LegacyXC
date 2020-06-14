package com.newlegacyxc.activity.series;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newlegacyxc.R;
import com.newlegacyxc.adapter.SeasonAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.EpisodeInfoModel;
import com.newlegacyxc.models.EpisodeModel;
import com.newlegacyxc.models.SeasonModel;
import com.newlegacyxc.models.SeriesModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SeasonActivity extends AppCompatActivity implements View.OnClickListener {

    private SeriesModel seriesModel;
    private Disposable bookSubscription;
    private ImageView imageView, img_fav;
    private List<String> slideModels;
    private SeasonAdapter seasonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        View rip_back = findViewById(R.id.rip_back);
        View rip_fav = findViewById(R.id.rip_fav);
        rip_fav.setOnClickListener(this);
        rip_back.setOnClickListener(this);
        TextView title = findViewById(R.id.title);
        imageView = findViewById(R.id.background);
        img_fav = findViewById(R.id.img_fav);
        Intent intent = getIntent();
        seriesModel = (SeriesModel) intent.getSerializableExtra("series");
        if (seriesModel==null){
            finish();
            return;
        }
        setFav();
        title.setText(seriesModel.getName()+" ("+seriesModel.getReleaseDate()+")");
        slideModels = new ArrayList<>();
        if (seriesModel.getBackdrop_path()!=null && seriesModel.getBackdrop_path().size()>0)
            slideModels = seriesModel.getBackdrop_path();
        else slideModels.add(seriesModel.getStream_icon());
        Thread myThread;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("tab_pos",tab.getPosition()+"");
//                getSupportFragmentManager().beginTransaction().replace(R.id.frame,fragments.get(tab.getPosition()),null).commit();
                if (tab.getPosition()==1){
                    findViewById(R.id.lay_overview).setVisibility(View.GONE);
                    findViewById(R.id.lay_season).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.lay_overview).setVisibility(View.VISIBLE);
                    findViewById(R.id.lay_season).setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
//        getSupportFragmentManager().beginTransaction().replace(R.id.frame,fragments.get(0),null).commit();
        findViewById(R.id.lay_overview).setVisibility(View.VISIBLE);
        findViewById(R.id.lay_season).setVisibility(View.GONE);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#90CAF9"));
        tabLayout.setSelectedTabIndicatorHeight((int) (2 * getResources().getDisplayMetrics().density));
        tabLayout.setTabTextColors(Color.parseColor("#CCCCCC"), Color.parseColor("#FFFFFF"));
        if (seriesModel.getSeasonModels()!=null && seriesModel.getSeasonModels().size()>0){
            seasonAdapter.setSeasonModels(seriesModel.getSeasonModels());
        }else {
            Observable<List<SeasonModel>> booksObservable =
                    Observable.fromCallable(this::startGetSeries);
            bookSubscription = booksObservable.
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe(seasonModels -> {
                        seriesModel.setSeasonModels(seasonModels);
                        seasonAdapter.setSeasonModels(seriesModel.getSeasonModels());
                    });
        }
        setSeasons();
        setOverView();
        FullScreencall();
    }

    private void setFav() {
        if (seriesModel.isIs_favorite()) img_fav.setImageResource(R.drawable.star_white);
        else img_fav.setImageResource(R.drawable.star_outline);
    }

    private void setSeasons() {
        RecyclerView season_list = findViewById(R.id.season_list);
        seasonAdapter = new SeasonAdapter(this, new ArrayList<>(), new SeasonAdapter.IMyViewHolderClicks() {
            @Override
            public void onClick(int i, SeasonModel movieModel) {
                //goto episode activity
                Intent intent = new Intent(SeasonActivity.this, EpisodeActivity.class);
                MyApp.seriesModel = seriesModel;
                intent.putExtra("season_i",i);
                startActivity(intent);
            }

            @Override
            public void onFocus(int i, SeasonModel movieModel) {

            }
        });
        LinearLayoutManager layoutManager= new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        season_list.setLayoutManager(layoutManager);
        season_list.setAdapter(seasonAdapter);
    }

    private void setOverView() {
        ImageView imageView3 = findViewById(R.id.imageView3);
        TextView title = findViewById(R.id.textView10);
        TextView date = findViewById(R.id.textView11);
        TextView cast = findViewById(R.id.textView12);
        TextView description = findViewById(R.id.textView13);
        Picasso.with(this)
                .load(seriesModel.getStream_icon())
                .placeholder(R.drawable.icon) // Equivalent of what ends up in onPrepareLoad
                .error(R.drawable.icon) // Equivalent of what ends up in onBitmapFailed
                .centerCrop()
                .fit()
                .into(imageView3);
        title.setText(seriesModel.getName());
        date.setText(seriesModel.getReleaseDate()+"   "+seriesModel.getGenre()+ "   "+ seriesModel.getDirector());
        cast.setText(seriesModel.getCast());
        description.setText(seriesModel.getPlot());
    }

    class CountDownRunner implements Runnable {
        // @Override
        int i=0;
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork(i);
                    i = (i+1)%slideModels.size();
                    Thread.sleep(Constants.GetSlideTime(SeasonActivity.this) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void doWork(int i) {
        runOnUiThread(() -> {
            try {
                Picasso.with(SeasonActivity.this).load(slideModels.get(i))
                        .error(R.drawable.icon_default)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
    public void onStop() {
        super.onStop();
        if (bookSubscription!=null && !bookSubscription.isDisposed()) {
            bookSubscription.dispose();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bookSubscription != null && !bookSubscription.isDisposed()) {
            bookSubscription.dispose();
        }
    }

    private List<SeasonModel> startGetSeries(){
        try {
            String requestBody = MyApp.instance.getIptvclient().getSeriesInfo(MyApp.user,MyApp.pass,seriesModel.getSeries_id());
            Log.e(getClass().getSimpleName(),seriesModel.getSeries_id() + " "+ requestBody);
            JSONObject map = new JSONObject(requestBody);
            Gson gson=new Gson();
            try {
                JSONArray seasons=map.getJSONArray("seasons");
                List<SeasonModel> seasonModelList = new ArrayList<>();
                List<SeasonModel> seasonModels = new ArrayList<>(gson.fromJson(seasons.toString(), new TypeToken<List<SeasonModel>>() {}.getType()));
//                JSONObject info= map.getJSONObject("info");
//                SeriesModel seriesModel = gson.fromJson(info.toString(),SeriesModel.class);
//                seriesModel.setBackdrop_path(seriesModel.getBackdrop_path());
                try {
                    JSONObject episodes=map.getJSONObject("episodes");
                    Log.e("FragmentSeasons",episodes.toString());
                    Iterator<?> keys = episodes.keys();
                    while (keys.hasNext()){
                        String key = (String) keys.next();
                        SeasonModel seasonModel = getSeasonByKey(seasonModels,key);
                        try {
                            JSONArray i_episodes = episodes.getJSONArray(key);
                            List<EpisodeModel> episodeModels=new ArrayList<>();
                            for (int i=0;i<i_episodes.length();i++){
                                try {
                                    JSONObject object_episode = i_episodes.getJSONObject(i);
                                    EpisodeModel episodeModel = gson.fromJson(object_episode.toString(),EpisodeModel.class);
                                    try {
                                        JSONObject info_object= object_episode.getJSONObject("info");
                                        EpisodeInfoModel episodeInfoModel = gson.fromJson(info_object.toString(),EpisodeInfoModel.class);
                                        episodeModel.setEpisodeInfoModel(episodeInfoModel);
                                        episodeModels.add(episodeModel);
                                    }catch (JSONException ignored){
                                        Log.e("FragmentSeasons","There is an error in getting info model " + seasonModel.getSeason_number());
                                    }
                                }catch (JSONException ignored){
                                    Log.e("FragmentSeasons","There is an error in getting episode model " + seasonModel.getSeason_number());
                                }
                            }
                            seasonModel.setEpisodeModels(episodeModels);
                            seasonModel.setTotal(episodeModels.size());
                        }catch (JSONException ignored){
                            Log.e("FragmentSeasons","There is no episodes in " + seasonModel.getSeason_number());
                        }
                        seasonModelList.add(seasonModel);
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        JSONArray episodes=map.getJSONArray("episodes");
                        for (int k=0;k<episodes.length();k++){
                            SeasonModel seasonModel = getSeasonByKey(seasonModels,k+"");
                            try {
                                JSONArray i_episodes = episodes.getJSONArray(k);
                                List<EpisodeModel> episodeModels=new ArrayList<>();
                                for (int i=0;i<i_episodes.length();i++){
                                    try {
                                        JSONObject object_episode = i_episodes.getJSONObject(i);
                                        EpisodeModel episodeModel = gson.fromJson(object_episode.toString(),EpisodeModel.class);
                                        try {
                                            JSONObject info_object= object_episode.getJSONObject("info");
                                            EpisodeInfoModel episodeInfoModel = gson.fromJson(info_object.toString(),EpisodeInfoModel.class);
                                            episodeModel.setEpisodeInfoModel(episodeInfoModel);
                                            episodeModels.add(episodeModel);
                                        }catch (JSONException ignored){
                                            Log.e("FragmentSeasons","There is an error in getting info model " + seasonModel.getSeason_number());
                                        }
                                    }catch (JSONException ignored){
                                        Log.e("FragmentSeasons","There is an error in getting episode model " + seasonModel.getSeason_number());
                                    }
                                }
                                seasonModel.setEpisodeModels(episodeModels);
                                seasonModel.setTotal(episodeModels.size());
                            }catch (JSONException e1) {
                                e1.printStackTrace();
                                Log.e("FragmentSeasons", "There is no episodes "+k);
                            }
                            seasonModelList.add(seasonModel);
                        }
                    }catch (JSONException ignored){
                        Log.e("FragmentSeasons", "There is no episodes at all");
                    }
                }
                Log.e(getClass().getSimpleName(),seasonModelList.size()+"");
                return seasonModelList;
            } catch (JSONException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private SeasonModel getSeasonByKey(List<SeasonModel> seasonModels, String key) {
        for (SeasonModel seasonModel:seasonModels){
            if (key.equals(String.valueOf(seasonModel.getSeason_number())))
                return seasonModel;
        }
        return new SeasonModel(key);
    }
    private void addToFav() {
        Log.e("OnAddFavClick","received");
        if (seriesModel.isIs_favorite()) {
            seriesModel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i < MyApp.favSeriesModels.size(); i++) {
                if (MyApp.favSeriesModels.get(i).getName().equals(seriesModel.getName())) {
                    is_exist = true;
                    pp = i;
                }
            }
            if (is_exist)
                MyApp.favSeriesModels.remove(pp);
            //get favorite channel names list
            List<String> fav_series_names=new ArrayList<>();
            for (SeriesModel seriesModel:MyApp.favSeriesModels){
                fav_series_names.add(seriesModel.getName());
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_SERIES(), fav_series_names);
            Log.e("SERIES_FAV","removed");
        } else {
            seriesModel.setIs_favorite(true);
            //get favorite channel names list
            MyApp.favSeriesModels.add(seriesModel);
            List<String> fav_series_names=new ArrayList<>();
            for (SeriesModel seriesModel:MyApp.favSeriesModels){
                fav_series_names.add(seriesModel.getName());
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_SERIES(), fav_series_names);
            Log.e("SERIES_FAV","added");
        }
        Constants.getFavoriteCatetory(MyApp.series_categories).setSeriesModels(MyApp.favSeriesModels);
        setFav();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rip_back:
                onBackPressed();
                break;
            case R.id.rip_fav:
                //add_remove_fav
                addToFav();
                break;
        }
    }
}
