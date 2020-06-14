package com.newlegacyxc.epg.misc;

import android.content.Context;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by Kristoffer.
 */
public class EPGUtil {
    private static final String TAG = "EPGUtil";
    private static final DateTimeFormatter dtfShortTime = DateTimeFormat.forPattern("h:mm a");
    private static final DateTimeFormatter indicatorFormat = DateTimeFormat.forPattern("MM-dd-yyyy");

    public static String getShortTime(long timeMillis) {
        return dtfShortTime.print(timeMillis);
    }

    public static String getWeekdayName(long dateMillis) {
        return indicatorFormat.print(dateMillis);
//        LocalDate date = new LocalDate(dateMillis);
//        return date.dayOfWeek().getAsText();
    }

    public static void loadImageInto(Context context, String url, int width, int height, Target target) {
        try {
            Picasso.with(context).load(url)
                    .resize(width, height)
                    .centerInside()
                    .into(target);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
