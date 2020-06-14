package com.newlegacyxc.epg.misc;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.newlegacyxc.epg.EPGData;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;

/**
 * TODO: Add null check when fetching at position etc.
 * Created by Kristoffer on 15-05-23.
 */
public class EPGDataImpl implements EPGData {

    private String name;
    private List<EPGChannel> channels = new ArrayList<>();
//    private List<List<EPGEvent>> events = new ArrayList<>();
//    private Map<String, EPGChannel> channelsByName = new HashMap<>();

//    public EPGDataImpl(String name, Map<EPGChannel, List<EPGEvent>> data) {
//        channels = new ArrayList<>(data.keySet());
////        events = new ArrayList<>(data.values());
//        this.name = name;
//        indexChannels();
//    }

    public EPGDataImpl(String name, List<EPGChannel> channels){
        this.channels=channels;
        this.name = name;
//        indexChannels();
    }

    @Override
    public String getName() {
        return name;
    }

    public EPGChannel getChannel(int position) {
        return channels.get(position);
    }

    public List<EPGEvent> getEvents(int channelPosition) {
//        return events.get(channelPosition);
        return channels.get(channelPosition).getEvents();
    }

    public EPGEvent getEvent(int channelPosition, int programPosition) {
//        return events.get(channelPosition).get(programPosition);
        Log.e("position","channel: "+channelPosition+" program: "+programPosition);
        return channels.get(channelPosition).getEvents().get(programPosition);
    }

    @Override
    public EPGEvent getEvent(EPGChannel epgChannel, int programPosition) {
        Log.e("position","channel: "+epgChannel.getName()+" program: "+programPosition);
        return epgChannel.getEvents().get(programPosition);
    }

    public int getChannelCount() {
        return channels.size();
    }

    @Override
    public boolean hasData() {
        return !channels.isEmpty();
    }

    @Override
    public List<EPGChannel> getChannels() {
        return channels;
    }

//    @Override
//    public EPGChannel getOrCreateChannel(String ImageUrl, String channelName, String Id, String Number, String  stream) {
//        EPGChannel channel = channelsByName.get(channelName);
//        if (channel != null) {
//            return channel;
//        }
//        return addNewChannel(ImageUrl,channelName,Id,Number,stream);
//    }
//
//    @Override
//    public EPGChannel addNewChannel(String ImageUrl, String channelName, String Id, String Number, String stream) {
//        int newChannelID = channels.size();
//        EPGChannel newChannel = new EPGChannel(ImageUrl,channelName,newChannelID,Id,Number,stream);
//        if(newChannelID>0) {
//            EPGChannel previousChannel = channels.get(newChannelID-1);
//            previousChannel.setNextChannel(newChannel);
//            newChannel.setPreviousChannel(previousChannel);
//        }
//        channels.add(newChannel);
//        channelsByName.put(String.valueOf(newChannel.getName()), newChannel);
//        return newChannel;
//    }

//    private void indexChannels() {
//        channelsByName = new HashMap<>();
//        for (int j = 0; j < channels.size(); j++) {
//            EPGChannel channel = channels.get(j);
//            channelsByName.put(channel.getName(), channel);
//        }
//        Log.e("size", String.valueOf(channelsByName.size()));
//    }
}
