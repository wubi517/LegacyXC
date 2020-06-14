package com.newlegacyxc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function4;

public class EpgAdapter extends RecyclerView.Adapter<EpgAdapter.HomeListViewHolder> {
    private List<EPGChannel> list ;
    private Context context;
    private Function4<Integer,Integer, EPGChannel, EPGEvent, Unit> onClickListener;
    private Function4<Integer, Integer, EPGChannel, EPGEvent, Unit> onFocusListener;
    private int channelPos=-1;
    private boolean is_header_focused = true;
    public EpgAdapter(List<EPGChannel> list, Context context, Function4<Integer, Integer, EPGChannel, EPGEvent, Unit> onClickListener,
                      Function4<Integer, Integer, EPGChannel, EPGEvent, Unit> onFocusListener) {
        this.list = list;
        this.context = context;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }

    public void setChannelPos(int channelPos){
        this.channelPos=channelPos;
        notifyItemChanged(channelPos);
    }
    public void setList(List<EPGChannel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public boolean getIs_Header_focused(){
        return is_header_focused;
    }
    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.epg_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeListViewHolder holder, final int position) {
        final EPGChannel epgChannel = list.get(position);
        if (epgChannel.getStream_icon()!=null && !epgChannel.getStream_icon().equals("")) {
            Picasso.with(holder.itemView.getContext()).load(epgChannel.getStream_icon())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.ad1)
                    .into(holder.image);
            Log.e("url",epgChannel.getStream_icon());
        }
        holder.programs_recyclerview.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
        Date now = new Date();
        now.setTime(now.getTime());//-Constants.SEVER_OFFSET
        List<EPGEvent> epgEvents = new ArrayList<>();
        int now_i= Constants.findNowEvent(epgChannel.getEvents());
        if (now_i!=-1){
            epgEvents = epgChannel.getEvents().subList(now_i, epgChannel.getEvents().size());
        }
        holder.programs_recyclerview.setAdapter(new EpgProgramsListAdapter(epgEvents, (integer, epgEvent) -> {
            //onClickListener
            onClickListener.invoke(position, integer, epgChannel, epgEvent);
            return null;
        }, (integer, epgEvent) -> {
            //onFocusListener
            onFocusListener.invoke(position, integer, epgChannel, epgEvent);
            is_header_focused = false;
            return null;
        }));
        List<EPGEvent> finalEpgEvents = epgEvents;
        holder.lay.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                holder.lay.setBackgroundColor(Color.parseColor("#2962FF"));
                if (finalEpgEvents.size()!=0) {
                    onFocusListener.invoke(position, -1, epgChannel, finalEpgEvents.get(0));
                }else {
                    onFocusListener.invoke(position, -1, epgChannel, null);
                }
                is_header_focused = true;
            }else{
                holder.lay.setBackgroundColor(Color.parseColor("#20ffffff"));
            }
        });
        holder.tv_channel_name.setText(epgChannel.getName());
        if (finalEpgEvents.size()!=0){
            holder.lay.setOnClickListener(v -> onClickListener.invoke(position, -1, epgChannel, finalEpgEvents.get(0)));
            holder.image.setOnClickListener(v -> onClickListener.invoke(position, -1, epgChannel, finalEpgEvents.get(0)));
        }else {
            holder.lay.setOnClickListener(v -> onClickListener.invoke(position, -1, epgChannel, null));
            holder.image.setOnClickListener(v -> onClickListener.invoke(position, -1, epgChannel, null));
        }
        if (channelPos==position) {
            holder.lay.requestFocus();
            channelPos=-1;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        RecyclerView programs_recyclerview;
        TextView tv_channel_name;
        LinearLayout lay;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            lay = itemView.findViewById(R.id.lay);
            programs_recyclerview= itemView.findViewById(R.id.programs_recyclerview);
            tv_channel_name = itemView.findViewById(R.id.tv_channel_name);
        }
    }
}