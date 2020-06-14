package com.newlegacyxc.activity.vod;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.newlegacyxc.R;
import com.newlegacyxc.activity.TrailerActivity;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.MovieInfoModel;
import com.newlegacyxc.models.MovieModel;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.newlegacyxc.activity.vod.PreviewVodNewActivity.checkAddedRecent;

public class VodInfo extends AppCompatActivity implements View.OnClickListener {

    private ImageView image;
    private TextView title, subTitle, body;
    private MovieModel showMovieModel;
    private Button addFav;
    private ImageView fav_icon;

    public void FullScreencall() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_info);
        FullScreencall();
        title = findViewById(R.id.textView9);
        subTitle = findViewById(R.id.textView10);
        body = findViewById(R.id.textView11);
        Button watchTrailer = findViewById(R.id.button2);
        Button watchMovie = findViewById(R.id.button3);
        image = findViewById(R.id.image);
        watchMovie.setOnClickListener(this);
        watchTrailer.requestFocus();
        watchTrailer.setOnClickListener(this);
        if (MyApp.vod_model==null) {
            finish();
            return;
        }
        showMovieModel= MyApp.vod_model;
        addFav = (Button)findViewById(R.id.button4);
        fav_icon = (ImageView)findViewById(R.id.fav_icon);
        addFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFav();
            }
        });
        setAddFavText();
        new Thread(new Runnable() {
            public void run() {
                getMovieInfo();
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button3:
                //add recent movie
                checkAddedRecent(showMovieModel);
                Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels().add(0,showMovieModel);
                //get recent channel names list
                List<MovieModel> recent_channel_models = new ArrayList<>(Constants.getRecentCatetory(MyApp.vod_categories).getMovieModels());
                //set
                MyApp.instance.getPreference().put(Constants.getRecentMovies(), recent_channel_models);
                Log.e(getClass().getSimpleName(),"added");

                //watchMovie
                String vod_url = MyApp.instance.getIptvclient().buildMovieStreamURL(MyApp.user,MyApp.pass,showMovieModel.getStream_id(),showMovieModel.getExtension());
                Log.e(getClass().getSimpleName(),vod_url);

                int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
                Intent intent;
                switch (current_player){
                    case 1:
                        intent = new Intent(this, VideoIjkPlayActivity.class);
                        break;
                    case 2:
                        intent = new Intent(this, VideoExoPlayActivity.class);
                        break;
                    default:
                        intent = new Intent(this, VideoPlayActivity.class);
                        break;
                }
                MyApp.vod_model = showMovieModel;
                intent.putExtra("title",showMovieModel.getName());
                intent.putExtra("img",showMovieModel.getStream_icon());
                intent.putExtra("url",vod_url);
                startActivity(intent);
                break;
            case R.id.button2:
                //watchTrailer
                String content_Uri = showMovieModel.getMovieInfoModel().getYoutube();
                if(content_Uri.isEmpty()){
                    Toast.makeText(this,"This movie do not have trailer",Toast.LENGTH_LONG).show();
                }else {
                    String newstr = content_Uri;
                    if(content_Uri.contains("=")){
                        int endIndex = content_Uri.lastIndexOf("=");
                        if (endIndex != -1)
                        {
                            newstr = content_Uri.substring(endIndex+1); // not forgot to put check if(endIndex != -1)
                        }
                    }else {
                        int endIndex = content_Uri.lastIndexOf("/");
                        if (endIndex != -1)
                        {
                            newstr = content_Uri.substring(endIndex+1); // not forgot to put check if(endIndex != -1)
                        }
                    }
                    Intent intent1 = new Intent(this, TrailerActivity.class);
                    intent1.putExtra("content_Uri",newstr);
                    startActivity(intent1);
                }
                break;
            case R.id.button4:
                break;
        }
    }

    private void setAddFavText(){
        if (showMovieModel.isIs_favorite()) {
            addFav.setText(getResources().getString(R.string.remove_favorites));
            fav_icon.setImageResource(R.drawable.star_white);
        }
        else {
            addFav.setText(getResources().getString(R.string.add_to_favorite));
            fav_icon.setImageResource(R.drawable.star_outline);
        }
    }

    private void addToFav() {
        if (showMovieModel.isIs_favorite()) {
            showMovieModel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i < Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().size(); i++) {
                if (Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().get(i).getName().equals(showMovieModel.getName())) {
                    is_exist = true;
                    pp = i;
                }
            }
            if (is_exist)
                Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().remove(pp);
            //get favorite channel names list
            List<MovieModel> fav_movie_names=new ArrayList<>();
            for (MovieModel movieModel:Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels()){
                fav_movie_names.add(movieModel);
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_VOD_MOVIES(), fav_movie_names);
            Log.e("ADD_FAV","removed");
        } else {
            showMovieModel.setIs_favorite(true);
            Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels().add(showMovieModel);
            //get favorite channel names list
            List<MovieModel> fav_channel_names=new ArrayList<>();
            for (MovieModel movieModel:Constants.getFavoriteCatetory(MyApp.vod_categories).getMovieModels()){
                fav_channel_names.add(movieModel);
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_VOD_MOVIES(), fav_channel_names);
            Log.e("LIVE_RATIO","added");
        }
        setAddFavText();
    }

    private void setModelInfo() {
        if (showMovieModel.getMovieInfoModel().getMovie_img()!=null &&
                !showMovieModel.getMovieInfoModel().getMovie_img().equals(""))
        Picasso.with(this).load(showMovieModel.getMovieInfoModel().getMovie_img())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(image);
        title.setText(showMovieModel.getName());
        subTitle.setText(showMovieModel.getMovieInfoModel().getCast());
        body.setText(showMovieModel.getMovieInfoModel().getPlot());
    }

    private void getMovieInfo(){
        try {
            String response = MyApp.instance.getIptvclient().getVodInfo(MyApp.user,MyApp.pass, showMovieModel.getStream_id());
            Log.e(getClass().getSimpleName(),response);
            JSONObject map = new JSONObject(response);
            MovieInfoModel movieInfoModel = new MovieInfoModel();
            try{
                JSONObject info_obj = map.getJSONObject("info");
                movieInfoModel.setMovie_img(info_obj.getString("movie_image"));
                movieInfoModel.setGenre(info_obj.getString("genre"));
                movieInfoModel.setPlot(info_obj.getString("plot"));
                movieInfoModel.setCast(info_obj.getString("cast"));
                try {
                    movieInfoModel.setRating(info_obj.getDouble("rating"));
                }catch (Exception e){
                    e.printStackTrace();
                    movieInfoModel.setRating(0.0);
                }
                try {
                    movieInfoModel.setYoutube(info_obj.getString("youtube_trailer"));
                }catch (Exception e){
                    e.printStackTrace();
                }
                movieInfoModel.setDirector(info_obj.getString("director"));
                movieInfoModel.setDuration(Integer.valueOf(info_obj.getString("duration")));
                try {
                    movieInfoModel.setActors(info_obj.getString("actors"));
                    movieInfoModel.setDescription(info_obj.getString("description"));
                    movieInfoModel.setAge(info_obj.getString("age"));
                    movieInfoModel.setCountry(info_obj.getString("country"));
                }catch (Exception ignored){
                }
                JSONObject movie_data = map.getJSONObject("movie_data");
                movieInfoModel.setStream_id(Integer.parseInt(movie_data.getString("stream_id")));
                movieInfoModel.setExtension(movie_data.getString("container_extension"));
                movieInfoModel.setName(movie_data.getString("name"));
            }catch (Exception e){
                Log.e("error","info_parse_error");
            }
            showMovieModel.setMovieInfoModel(movieInfoModel);
            runOnUiThread(this::setModelInfo);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                try {
                    Picasso.with(this).load(showMovieModel.getStream_icon())
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .error(R.drawable.icon)
                            .into(image);
                }catch (Exception i){
                    Picasso.with(this).load(R.drawable.icon_default)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .error(R.drawable.icon)
                            .into(image);
                }
            });
        }
    }

}
