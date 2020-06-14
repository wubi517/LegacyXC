package com.newlegacyxc.activity.series;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.google.android.material.textfield.TextInputEditText;
import com.newlegacyxc.R;
import com.newlegacyxc.adapter.GridSeriesAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.CategoryModel;
import com.newlegacyxc.models.SeriesModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreviewSeriesNewActivity extends AppCompatActivity {

    //    RecyclerView list_vod_movies;
    GridView list_vod_movies;
//    SeriesAdapter movieAdapter;
    GridSeriesAdapter gridSeriesAdapter;
    List<SeriesModel> seriesModels;
    int category_pos,sub_pos=0,sort=0,current_player=0;
    String category_id;
    List<CategoryModel> categoryModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_series_new);
        list_vod_movies =findViewById(R.id.category_recyclerview);
        category_pos = (int) MyApp.instance.getPreference().get(Constants.getSeriesPos());
        categoryModels = MyApp.series_categories_filter;
        category_id = categoryModels.get(category_pos).getId();
        seriesModels = MyApp.series_categories_filter.get(category_pos).getSeriesModels();
        MyApp.seriesModels = seriesModels;
        gridSeriesAdapter = new GridSeriesAdapter(this, seriesModels);
        TextInputEditText editText = findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("Text ["+s+"]");
                gridSeriesAdapter.getFilter().filter(s.toString());
            }
        });
        Button button = findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridSeriesAdapter.getFilter().filter(editText.getText().toString());
            }
        });
        list_vod_movies.requestFocus();
        list_vod_movies.setAdapter(gridSeriesAdapter);
        list_vod_movies.setOnItemClickListener((adapterView, view, i, l) -> {
            sub_pos = i;
            SeriesModel showmodel = (SeriesModel)gridSeriesAdapter.getItem(i);
            checkAddedRecent(showmodel);
            Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels().add(0,showmodel);
            //get recent channel names list
            List<String> recent_series_names = new ArrayList<>(Constants.getStrListFromSeries(Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels()));
            //set
            MyApp.instance.getPreference().put(Constants.getRecentSeries(), recent_series_names);
            Intent intent = new Intent(PreviewSeriesNewActivity.this, SeasonActivity.class);
            intent.putExtra("series",showmodel);
            startActivity(intent);
        });
        list_vod_movies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//        movieAdapter=new SeriesAdapter(this, seriesModels, new SeriesAdapter.IMyViewHolderClicks() {
//            @Override
//            public void onClick(int i, SeriesModel showmodel) {
//                sub_pos = i;
//                checkAddedRecent(showmodel);
//                Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels().add(0,showmodel);
//                //get recent channel names list
//                List<SeriesModel> recent_channel_models = new ArrayList<>(Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels());
//                //set
//                MyApp.instance.getPreference().put(Constants.getRecentSeries(), recent_channel_models);
//
//                String vod_title = seriesModels.get(i).getName();
//                String vod_image = seriesModels.get(i).getStream_icon();
//                String vod_star = seriesModels.get(i).getRating();
//                String vod_cast = seriesModels.get(i).getCast();
//                String vod_genre = seriesModels.get(i).getGenre();
//                String vod_plot = seriesModels.get(i).getPlot();
//                String vod_cat_id = category_id;
//                Intent intent = new Intent(PreviewSeriesNewActivity.this,SeriesCatActivity.class);
//                intent.putExtra("title",vod_title);
//                intent.putExtra("star",vod_star);
//                intent.putExtra("cast",vod_cast);
//                intent.putExtra("genre",vod_genre);
//                intent.putExtra("plot",vod_plot);
//                intent.putExtra("cat_id",vod_cat_id);
//                intent.putExtra("img_url",vod_image);
//                intent.putExtra("series_id",seriesModels.get(i).getSeries_id());
//                startActivity(intent);
//            }
//
//            @Override
//            public void onFocus(int i, SeriesModel movieModel) {
//
//            }
//        });
//        list_vod_movies.setHasFixedSize(true);
//        list_vod_movies.setItemViewCacheSize(50);
//        list_vod_movies.setDrawingCacheEnabled(true);
//        list_vod_movies.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//        list_vod_movies.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.span_count_1)));
//        list_vod_movies.addItemDecoration(new SpacesItemDecoration(getResources().getInteger(R.integer.span_count_1), Constants.SPACE_ITEM_DECORATION, true));
//        list_vod_movies.setAdapter(movieAdapter);
        FullScreencall();
    }

    private void checkAddedRecent(SeriesModel showMovieModel) {
        Iterator<SeriesModel> iter = Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels().iterator();
        while(iter.hasNext()){
            SeriesModel movieModel = iter.next();
            if (movieModel.getName().equals(showMovieModel.getName()))
                iter.remove();
        }
    }

    public void FullScreencall() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
