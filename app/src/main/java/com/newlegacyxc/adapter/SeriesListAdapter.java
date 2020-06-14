package com.newlegacyxc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.SeriesModel;
import com.newlegacyxc.utils.Utils;

import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class SeriesListAdapter extends BaseAdapter{

    private Context context;
    private List<SeriesModel> datas;
    private LayoutInflater inflater;
    private int selected_pos;
    private TextView title,hd;
    private LinearLayout main_lay;
    public SeriesListAdapter(Context context, List<SeriesModel> datas) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.datas = datas;
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
            convertView = inflater.inflate(R.layout.item_vod_list, parent, false);
        }
        main_lay = convertView.findViewById(R.id.main_lay);
        title =  convertView.findViewById(R.id.vod_list_name);
        hd = convertView.findViewById(R.id.vod_list_hd);
        title.setText(datas.get(position).getName());
        hd.setText(datas.get(position).getNum());

        main_lay.setBackgroundResource(R.drawable.list_item_channel_draw);
        if(MyApp.instance.getPreference().get(Constants.IS_PHONE)!=null){
            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
        }else {
            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
        }
        if(selected_pos==position && MyApp.touch){
            main_lay.setBackgroundResource(R.drawable.list_yellow_bg);
            hd.setBackgroundResource(R.drawable.white_btn_border);
            if(!Utils.isTablet(context)){
                title.setTextColor(Color.parseColor("#000000"));
            }
        }else {
            main_lay.setBackgroundResource(R.drawable.list_item_channel_draw);
            hd.setBackgroundResource(R.drawable.yelloback);
            if(!Utils.isTablet(context)){
                title.setTextColor(Color.parseColor("#ffffff"));
            }
        }
        return convertView;
    }

    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
