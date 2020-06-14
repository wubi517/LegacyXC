package com.newlegacyxc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class MainListAdapter extends BaseAdapter {

    Context context;
    private List<EPGChannel> datas;
    private LayoutInflater inflater;
    private int selected_pos;
    private TextView title,num;
    private LinearLayout main_lay,ly_info;
    private ImageView image_play,image_clock,image_star,channel_logo;
    private View vew;
    public MainListAdapter(Context context, List<EPGChannel> datas) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.datas = datas;
    }
    public void setDatas(List<EPGChannel> datas){
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_main_list, parent, false);
        }
        main_lay = (LinearLayout) convertView.findViewById(R.id.main_lay);
        title = (TextView) convertView.findViewById(R.id.main_list_txt);
        num = (TextView)convertView.findViewById(R.id.main_list_num);
        image_play = (ImageView)convertView.findViewById(R.id.image_play);
        image_star = (ImageView)convertView.findViewById(R.id.image_star);
        image_clock = (ImageView)convertView.findViewById(R.id.image_clock);
        channel_logo = (ImageView)convertView.findViewById(R.id.channel_logo);
        EPGChannel epgChannel = datas.get(position);
        if(epgChannel.getStream_icon()!=null && !epgChannel.getStream_icon().isEmpty()){
            Picasso.with(context).load(epgChannel.getStream_icon())
                    .error(R.drawable.icon)
                    .placeholder(R.drawable.icon)
                    .into(channel_logo);
            channel_logo.setVisibility(View.VISIBLE);
        }else {
            channel_logo.setVisibility(View.GONE);
        }
        vew = (View)convertView.findViewById(R.id.view);
        ly_info = (LinearLayout)convertView.findViewById(R.id.ly_info);
        title.setText(epgChannel.getName());
        num.setText(epgChannel.getNum());
        TextView tv_current_epg = convertView.findViewById(R.id.tv_current_epg);
        tv_current_epg.setVisibility(View.GONE);
        if (epgChannel.getEvents()!=null && epgChannel.getEvents().size()>0){
            int i=Constants.findNowEvent(epgChannel.getEvents());
            if (i!=-1){
                tv_current_epg.setVisibility(View.VISIBLE);
                tv_current_epg.setText(epgChannel.getEvents().get(i).getTitle());
            }
        }
        if(epgChannel.is_favorite()){
            image_star.setVisibility(View.VISIBLE);
        }else {
            image_star.setVisibility(View.GONE);
        }
        if(epgChannel.getTv_archive().equalsIgnoreCase("1")){
            image_clock.setVisibility(View.VISIBLE);
        }else {
            image_clock.setVisibility(View.GONE);
        }
        if (selected_pos == position) {
            image_play.setVisibility(View.VISIBLE);
        } else {
            image_play.setVisibility(View.GONE);
//            num.setTextColor(Color.parseColor("#18477f"));
//            num.setBackgroundResource(R.drawable.yelloback);
//            main_lay.setBackgroundResource(R.drawable.list_item_channel_draw);
        }

        vew.setVisibility(View.GONE);
        ly_info.setVisibility(View.GONE);

//        if(MyApp.instance.getPreference().get(Constants.IS_PHONE)!=null){
//            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
//        }else {
//            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
//        }

        return convertView;
    }

    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
