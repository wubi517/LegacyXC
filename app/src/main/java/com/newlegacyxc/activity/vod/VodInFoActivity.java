package com.newlegacyxc.activity.vod;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VodInFoActivity extends AppCompatActivity {
    ImageView btn_back,image_movie;
    TextView txt_time,txt_title,txt_year,txt_genre,txt_length,txt_age,txt_director,txt_cast,txt_dec;
    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_in_fo);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(view -> finish());

        findViewById(R.id.ly_back).setOnClickListener(view -> finish());
        image_movie = findViewById(R.id.image_movie);
        txt_time = findViewById(R.id.txt_time);
        txt_title = findViewById(R.id.txt_title);
        txt_year = findViewById(R.id.txt_year);
        txt_genre = findViewById(R.id.txt_genre);
        txt_length = findViewById(R.id.txt_length);
        txt_age = findViewById(R.id.txt_age);
        txt_director = findViewById(R.id.txt_director);
        txt_cast = findViewById(R.id.txt_cast);
        txt_dec = findViewById(R.id.txt_dec);



        txt_title.setText("  "+getIntent().getStringExtra("title"));
        txt_year.setText(getIntent().getStringExtra("year"));
        txt_genre.setText(getIntent().getStringExtra("genre"));
        int second = getIntent().getIntExtra("length",0);
        int hour = second/3600;
        int min = (second%3600)/60;
        int sec = (second%3600)%60;
        txt_length.setText(hour +":"+min+":"+sec);
        txt_director.setText(getIntent().getStringExtra("director"));
        txt_cast.setText(getIntent().getStringExtra("cast"));
        txt_age.setText(getIntent().getStringExtra("age"));
        txt_dec.setText(getIntent().getStringExtra("dec"));

        try {
            Picasso.with(this).load(getIntent().getStringExtra("img"))
                    .into(image_movie);
        }catch (Exception e){
            Picasso.with(this).load(R.drawable.icon_default)
                    .into(image_movie);
        }

        Thread myThread = null;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();

        FullScreencall();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    finish();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
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
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
