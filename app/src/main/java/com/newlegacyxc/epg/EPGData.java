package com.newlegacyxc.epg;

import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;

import java.util.List;


/**
 * Interface to implement and pass to EPG containing data to be used.
 * Implementation can be a simple as simple as a Map/List or maybe an Adapter.
 * Created by Kristoffer on 15-05-23.
 */
public interface EPGData {

    String getName();

    EPGChannel getChannel(int position);

//    EPGChannel getOrCreateChannel(String ImageUrl, String channelName, String Id, String Number, String  stream);
//
//    EPGChannel addNewChannel(String ImageUrl, String channelName, String Id, String Number, String  stream);

    List<EPGEvent> getEvents(int channelPosition);

    EPGEvent getEvent(int channelPosition, int programPosition);
    EPGEvent getEvent(EPGChannel epgChannel, int programPosition);

    int getChannelCount();

    boolean hasData();

    List<EPGChannel> getChannels();

}
