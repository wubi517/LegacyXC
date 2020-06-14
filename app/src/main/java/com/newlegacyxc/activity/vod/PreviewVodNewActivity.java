package com.newlegacyxc.activity.vod;

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
import com.newlegacyxc.adapter.GridMovieAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.MovieModel;

import java.util.Iterator;
import java.util.List;

public class PreviewVodNewActivity extends AppCompatActivity {
//    RecyclerView list_vod_movies;
    GridView list_vod_movies;
//    VodMovieAdapter movieAdapter;
    GridMovieAdapter gridMovieAdapter;
    List<MovieModel> movieModels;
    int category_pos,sub_pos=0,sort=0,current_player=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_vod_new);
        list_vod_movies =findViewById(R.id.category_recyclerview);
        category_pos = (int) MyApp.instance.getPreference().get(Constants.getVodPos());
        if (MyApp.vod_categories_filter.size()==0) Constants.getVodFilter();
        if (MyApp.vod_categories_filter.size()-1<category_pos) {
            finish();
            return;
        }
        movieModels = MyApp.vod_categories_filter.get(category_pos).getMovieModels();
        MyApp.movieModels0 = movieModels;
        gridMovieAdapter = new GridMovieAdapter(this, movieModels);
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
                gridMovieAdapter.getFilter().filter(s.toString());
            }
        });
        Button button = findViewById(R.id.submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridMovieAdapter.getFilter().filter(editText.getText().toString());
            }
        });
        list_vod_movies.requestFocus();
        list_vod_movies.setAdapter(gridMovieAdapter);
        list_vod_movies.setOnItemClickListener((adapterView, view, i, l) -> {
            sub_pos = i;
            MovieModel showmodel = (MovieModel)gridMovieAdapter.getItem(i);
            MyApp.vod_model = showmodel;
            Intent intent = new Intent(PreviewVodNewActivity.this,VodInfo.class);
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
//        movieAdapter=new VodMovieAdapter(this, movieModels, new VodMovieAdapter.IMyViewHolderClicks() {
//            @Override
//            public void onClick(int i, MovieModel showmodel) {
//                sub_pos = i;
//                checkAddedRecent(showmodel);
//                Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels().add(0,showmodel);
//                //get recent channel names list
//                List<MovieModel> recent_channel_models = new ArrayList<>(Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels());
//                //set
//                MyApp.instance.getPreference().put(Constants.getRecentMovies(), recent_channel_models);
//                String vod_title = showmodel.getName();
//                String vod_image = showmodel.getStream_icon();
//                String type = showmodel.getExtension();
//                MyApp.vod_model = movieModels.get(sub_pos);
//                String vod_url = MyApp.instance.getIptvclient().buildMovieStreamURL(MyApp.user,MyApp.pass,showmodel.getStream_id(),type);
//                Intent intent = new Intent();
//                switch (current_player){
//                    case 0:
//                        intent = new Intent(PreviewVodNewActivity.this,VideoPlayActivity.class);
//                        break;
//                    case 1:
//                        intent = new Intent(PreviewVodNewActivity.this,VideoIjkPlayActivity.class);
//                        break;
//                    case 2:
//                        intent = new Intent(PreviewVodNewActivity.this,VideoExoPlayActivity.class);
//                        break;
//                }
//                intent.putExtra("title",vod_title);
//                intent.putExtra("img",vod_image);
//                intent.putExtra("url",vod_url);
//                startActivity(intent);
//            }
//
//            @Override
//            public void onFocus(int i, MovieModel movieModel) {
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

    public static void checkAddedRecent(MovieModel showMovieModel) {
        Iterator<MovieModel> iter = Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels().iterator();
        while(iter.hasNext()){
            MovieModel movieModel = iter.next();
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
