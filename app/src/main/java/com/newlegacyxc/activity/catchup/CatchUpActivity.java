package com.newlegacyxc.activity.catchup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.leanback.widget.VerticalGridView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;

import com.newlegacyxc.R;
import com.newlegacyxc.adapter.CategoryAdapter;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.FullModel;

import java.util.ArrayList;
import java.util.List;

public class CatchUpActivity extends AppCompatActivity {

    private GridView channel_recycler_view;
    private List<FullModel> catchUpFullModels = new ArrayList<>();
    private ChannelAdapter channelAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch_up);
        FullScreencall();
        channel_recycler_view = findViewById(R.id.channel_recyclerview);
        List<EPGChannel> epgChannels;
        for (FullModel fullModel: MyApp.fullModels_filter){
            epgChannels = new ArrayList<>();
            for (EPGChannel epgChannel: fullModel.getChannels()){
                if (epgChannel.getTv_archive().equals("1"))
                    epgChannels.add(epgChannel);
            }
            catchUpFullModels.add(new FullModel(fullModel.getCategory_id(),epgChannels,fullModel.getCategory_name(),fullModel.getCatchable_count()));
        }
        if (catchUpFullModels.size()>0){
            channelAdapter = new ChannelAdapter(CatchUpActivity.this,catchUpFullModels.get(0).getChannels());
            channel_recycler_view.setAdapter(channelAdapter);
            channel_recycler_view.setOnItemClickListener((parent, view, position, id) -> goToDetailPage(position, (EPGChannel) channelAdapter.getItem(position)));
            VerticalGridView category_recycler_view = findViewById(R.id.category_recyclerview);
            category_recycler_view.setAdapter(new CategoryAdapter(MyApp.live_categories_filter, (categoryModel, position, is_clicked) -> {
                if (!is_clicked) return null;
                channelAdapter.setData(catchUpFullModels.get(position).getChannels());
                return null;
            }));
        }
    }

    private void goToDetailPage(Integer integer, EPGChannel epgChannel) {
        if (epgChannel.getTv_archive().equals("0")) return;
        MyApp.epgChannel = epgChannel;
        Intent intent = new Intent(this, GuideNewActivity.class);
        startActivity(intent);
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
