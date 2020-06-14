package com.newlegacyxc.activity.series;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.EpisodeModel;
import com.newlegacyxc.models.SeasonModel;
import com.newlegacyxc.models.SeriesModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EpisodeActivity extends AppCompatActivity implements View.OnClickListener {

    private SeriesModel seriesModel;
    private int season_i;
    private int episode_i;
    private Spinner spinner;
    private TextView title, releasedate, body;
    private ImageView background, ep_icon;
    private TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        findViewById(R.id.rip_drop).setOnClickListener(this);
        findViewById(R.id.rip_back).setOnClickListener(this);
        findViewById(R.id.rip_play).setOnClickListener(this);
        seriesModel = MyApp.seriesModel;
        season_i = (int) getIntent().getIntExtra("season_i",0);
        spinner = findViewById(R.id.spinner);
        title = findViewById(R.id.title);
        releasedate = findViewById(R.id.releasedate);
        body = findViewById(R.id.body);
        background = findViewById(R.id.background);
        ep_icon = findViewById(R.id.ep_icon);
        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#90CAF9"));
        tabLayout.setSelectedTabIndicatorHeight((int) (2 * getResources().getDisplayMetrics().density));
        tabLayout.setTabTextColors(Color.parseColor("#CCCCCC"), Color.parseColor("#FFFFFF"));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                season_i = i;
                Log.e("spinner","onItemSelected "+season_i);
                setSeason();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        String[] DayOfWeek = new String[seriesModel.getSeasonModels().size()];
        for (int i=0;i<DayOfWeek.length;i++){
            SeasonModel seasonModel = seriesModel.getSeasonModels().get(i);
            DayOfWeek[i] = seasonModel.getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.item_season_spinner, R.id.categry_list_txt, DayOfWeek);
//        adapter.setDropDownViewResource(R.layout.drop_down_bg); // The drop down view

        spinner.setAdapter(adapter);
        spinner.setSelection(season_i);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                episode_i = tab.getPosition();
                setEpisode();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setSeason();
        FullScreencall();
    }

    @SuppressLint("SetTextI18n")
    private void setEpisode() {
        EpisodeModel episodeModel = seriesModel.getSeasonModels().get(season_i).getEpisodeModels().get(episode_i);
        title.setText(episodeModel.getTitle());
        body.setText(episodeModel.getEpisodeInfoModel().getPlot());
        releasedate.setText(episodeModel.getEpisodeInfoModel().getReleasedate()+" | "+
                episodeModel.getEpisodeInfoModel().getRating()+" | "+
                episodeModel.getEpisodeInfoModel().getDuration());
        Picasso.with(this).load(episodeModel.getEpisodeInfoModel().getMovie_image())
                .error(R.drawable.icon_default)
                .into(ep_icon);
    }

    private void setSeason() {
        List<EpisodeModel> episodeModels = seriesModel.getSeasonModels().get(season_i).getEpisodeModels();
        if (episodeModels==null || episodeModels.size()==0) return;
        tabLayout.removeAllTabs();
        for (EpisodeModel episodeModel:episodeModels)
            tabLayout.addTab(tabLayout.newTab().setText(episodeModel.getTitle()));
        tabLayout.getTabAt(0).select();
        Picasso.with(this).load(seriesModel.getSeasonModels().get(season_i).getIcon_big())
                .error(R.drawable.icon_default)
                .into(background);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rip_back:
                onBackPressed();
                break;
            case R.id.rip_drop:
                spinner.performClick();
                break;
            case R.id.rip_play:
                MyApp.episodeModels = seriesModel.getSeasonModels().get(season_i).getEpisodeModels();
                MyApp.episode_pos = episode_i;
                int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());

                Intent intent = new Intent();
                switch (current_player){
                    case 0:
                        intent = new Intent(this, SeriesPlayActivity.class);
                        break;
                    case 1:
                        intent = new Intent(this, SeriesIjkPlayActivity.class);
                        break;
                    case 2:
                        intent = new Intent(this, SeriesExoPlayActivity.class);
                        break;
                }
                startActivity(intent);
                break;
        }
    }
}
