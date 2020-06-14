package com.newlegacyxc.activity.catchup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newlegacyxc.R;
import com.newlegacyxc.activity.live.LiveExoPlayActivity;
import com.newlegacyxc.activity.live.LiveIjkPlayActivity;
import com.newlegacyxc.activity.live.LivePlayActivity;
import com.newlegacyxc.adapter.DateAdapter;
import com.newlegacyxc.adapter.ProgramsCatchUpAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.CatchupModel;
import com.newlegacyxc.models.EPGEvent;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class GuideNewActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private TextView duration, title, content;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy");
    private ProgramsCatchUpAdapter liveTvProgramsAdapter;
    private DateAdapter dateAdapter;
    private Disposable bookSubscription;

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
        setContentView(R.layout.activity_guide_new);
        FullScreencall();
        RecyclerView dateRecyclerView = findViewById(R.id.dateRecyclerView);
        RecyclerView epg_recyclerView = findViewById(R.id.epg_recyclerview);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        epg_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ImageView current_channel_image = findViewById(R.id.current_channel_image);
        if (!MyApp.epgChannel.getStream_icon().equals(""))
            Picasso.with(this)
                .load(MyApp.epgChannel.getStream_icon())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.icon)
                .into(current_channel_image);
        duration = findViewById(R.id.textView4);
        title = findViewById(R.id.textView7);
        content = findViewById(R.id.textView8);
        liveTvProgramsAdapter = new ProgramsCatchUpAdapter(new ArrayList<EPGEvent>(), (integer, epgEvent) -> {
            //onClickListener
            Calendar now = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.setTime(epgEvent.getEndTime());
            if (now.compareTo(end)<0) {
                Log.e("EpgClick","future event");
                return null;
            }
            String url = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,
                    MyApp.epgChannel.getStream_id()+"", Constants.catchupFormat.format(epgEvent.getStartTime()),
                    (epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/1000/60);
            int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
            Intent intent;
            switch (current_player){
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
            int duration = (int) ((epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/1000);
            long start_mil = epgEvent.getStartTime().getTime();
            long now_mil = System.currentTimeMillis();
            intent.putExtra("title",MyApp.epgChannel.getName());
            intent.putExtra("stream_id",MyApp.epgChannel.getStream_id());
//                        intent.putExtra("img",MyApp.epgChannel.getImageURL());
            intent.putExtra("url",url);
            intent.putExtra("duration",duration);
            intent.putExtra("start_mil",start_mil);
            intent.putExtra("now_mil",now_mil);
            intent.putExtra("is_live",false);
            intent.putExtra("dec",new String(Base64.decode(epgEvent.getDec(),Base64.DEFAULT)));
            intent.putExtra("current_dec",new String(Base64.decode(epgEvent.getTitle(),Base64.DEFAULT)));
            if(epgEvent.getNextEvent()!=null){
                intent.putExtra("next_dec",new String(Base64.decode(epgEvent.getNextEvent().getTitle(),Base64.DEFAULT)));
            }else {
                intent.putExtra("next_dec","No Information");
            }
            startActivity(intent);

            return null;
        }, new Function2<Integer, EPGEvent, Unit>() {
            @Override
            public Unit invoke(Integer integer, EPGEvent epgEvent) {
                //onFocusListener
                setDescription(epgEvent);
                return null;
            }
        });
        dateAdapter = new DateAdapter(new ArrayList<CatchupModel>(), new Function2<CatchupModel, Integer, Unit>() {
            @Override
            public Unit invoke(CatchupModel catchupModel, Integer integer) {
                liveTvProgramsAdapter.setEpgModels(catchupModel.getEpgEvents());
                return null;
            }
        });
        dateRecyclerView.setAdapter(dateAdapter);
        epg_recyclerView.setAdapter(liveTvProgramsAdapter);
        io.reactivex.Observable<List<EPGEvent>> booksObservable =
                Observable.fromCallable(this::getEpg);
        bookSubscription = booksObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(epgModelList -> {
                    MyApp.epgChannel.setEvents(epgModelList);
                    List<CatchupModel> catchupModels = getCatchupModels(MyApp.epgChannel.getEvents());
                    if (catchupModels.size()==0) return;
                    liveTvProgramsAdapter.setEpgModels(catchupModels.get(0).getEpgEvents());
                    dateAdapter.setList(catchupModels);
                    setDescription(MyApp.epgChannel.getEvents().get(0));
                });
    }

    private List<CatchupModel> getCatchupModels(List<EPGEvent> events) {
        if (events==null || events.size()==0) return new ArrayList<>();
        Collections.sort(events, (o1, o2) -> o1.getStart_timestamp().compareTo(o2.getStart_timestamp()));
        List<CatchupModel> catchupModels = new ArrayList<>();
        String date, nowDate=null;
        List<EPGEvent> epgEvents=new ArrayList<>();
        Calendar now = Calendar.getInstance();
        EPGEvent prevEvent = events.get(events.size()-1);
        for (EPGEvent epgEvent:events){
            epgEvent.setPreviousEvent(prevEvent);
            prevEvent = epgEvent;
            Calendar that_date = Calendar.getInstance();
            that_date.setTime(epgEvent.getStartTime());
            date =simpleDateFormat.format(that_date.getTime());
            if (nowDate==null) {
                nowDate = date;
                epgEvents = new ArrayList<>();
                Log.e("FragmentCatchupDetail","initialize");
            }
            if (!date.equals(nowDate)){
                CatchupModel catchupModel = new CatchupModel();
                catchupModel.setName(nowDate);
                catchupModel.setEpgEvents(epgEvents);
                Log.e("FragmentCatchupDetail",nowDate+" "+epgEvents.size());
                catchupModels.add(catchupModel);
                nowDate=date;
                epgEvents = new ArrayList<>();
            }
//            if (now.compareTo(that_date)<0)
//                break;
            epgEvents.add(epgEvent);
        }
        return catchupModels;
    }

    private List<EPGEvent> getEpg() {
        try {
            String map = MyApp.instance.getIptvclient().getAllEPGOfStream(MyApp.user,MyApp.pass,MyApp.epgChannel.getStream_id()+"");
            Log.e(getClass().getSimpleName(),map);
            map=map.replaceAll("[^\\x00-\\x7F]", "");
            if (!map.contains("null_error_response")){
                Log.e("response",map);
                try {
                    JSONObject jsonObject= new JSONObject(map);
                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
                    List<EPGEvent> epgEvents = new ArrayList<>();
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject epgObj = jsonArray.getJSONObject(i);
                        EPGEvent epgEvent = new EPGEvent();
                        epgEvent.setId(epgObj.getString("id"));
                        epgEvent.setEpg_id(epgObj.getString("epg_id"));
                        epgEvent.setTitle(new String(Base64.decode(epgObj.getString("title"),Base64.DEFAULT)));
                        epgEvent.setT_time(epgObj.getString("start"));
                        epgEvent.setT_time_to(epgObj.getString("end"));
                        epgEvent.setDec(new String(Base64.decode(epgObj.getString("description"),Base64.DEFAULT)));
                        epgEvent.setChannel_id(epgObj.getString("channel_id"));
                        epgEvent.setStart_timestamp(epgObj.getString("start_timestamp"));
                        epgEvent.setStop_timestamp(epgObj.getString("stop_timestamp"));
                        epgEvents.add(epgEvent);
                    }
                    return epgEvents;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            } else return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setDescription(EPGEvent epgEvent) {
        Log.e(TAG,"initialize header by changing program");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMM d, HH:mm");
        if (epgEvent!=null){
            Log.e(TAG,epgEvent.getStart_timestamp());
            Calendar that_date = Calendar.getInstance();
            that_date.setTime(epgEvent.getStartTime());
            Calendar end_date = Calendar.getInstance();
            end_date.setTime(epgEvent.getEndTime());
            duration.setText(dateFormat1.format(that_date.getTime())+" - "+dateFormat.format(end_date.getTime()));
            title.setText(epgEvent.getTitle());
            content.setText(epgEvent.getDec());
        }else {
            duration.setText("-");
            title.setText(this.getString(R.string.no_information));
            content.setText("");
        }
    }

}
