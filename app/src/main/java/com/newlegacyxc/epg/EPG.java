package com.newlegacyxc.epg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import com.google.common.collect.Maps;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.EPGEvent;
import com.newlegacyxc.models.EPGState;
import com.newlegacyxc.utils.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.joda.time.LocalDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.newlegacyxc.R;
import com.newlegacyxc.epg.misc.EPGUtil;

/**
 * Classic EPG, electronic program guide, that scrolls both horizontal, vertical and diagonal.
 * It utilize onDraw() to draw the graphic on screen. So there are some private helper methods calculating positions etc.
 * Listed on Y-axis are channels and X-axis are programs/events. Data is added to EPG by using GetMergeEPGData()
 * and pass in an EPGData implementation. A click listener can be added using setEPGClickListener().
 * Created by Kristoffer, http://kmdev.se
 */
public class EPG extends ViewGroup {

    public final String TAG = getClass().getSimpleName();
    public static final int DAYS_BACK_MILLIS = 3 * 24 * 60 * 60 * 1000;        // 3 days
    public static final int DAYS_FORWARD_MILLIS = 3 * 24 * 60 * 60 * 1000;     // 3 days
    public static final int HOURS_IN_VIEWPORT_MILLIS = 2 * 60 * 60 * 1000;     // 2 hours
    public static final int TIME_LABEL_SPACING_MILLIS = 30 * 60 * 1000;        // 30 minutes

    private final Rect mClipRect;
    private final Rect mDrawingRect;
    private final Rect mMeasuringRect;
    private final Paint mPaint;
    private final Scroller mScroller;
    private final GestureDetector mGestureDetector;

    private final int mChannelLayoutMargin;
    private int mTimeBarBackground;
    private final int mChannelLayoutPadding;
    private final int mChannelLayoutHeight;
    private final int mChannelLayoutWidth;
    private int mChannelLayoutBackground;
    private int mChannelLayoutForeground,mChannelLayoutForegroundSelected;
    private int mChannelLayoutTextColor;
    private int mEventLayoutBackground;
    private int mEventLayoutBackgroundCurrent;
    private int mEventLayoutTextColor;
    private int mEventLayoutTextColorOveray;
    private final int mEventLayoutTextSize;
    private final int mChannelLayoutTextSize;
    private final int mTimeBarLineWidth;
    private final int mTimeBarLineColor;
    private final int mTimeBarHeight;
    private final int mTimeBarTextSize;

    private final int mResetButtonSize;
    private final int mResetButtonMargin;
    private final Bitmap mResetButtonIcon;

    private final int mEPGBackground;
    private final Map<String, Bitmap> mChannelImageCache;
    private final Map<String, Target> mChannelImageTargetCache;

    private EPGClickListener mClickListener;
    private int mMaxHorizontalScroll;
    private int mMaxVerticalScroll;
    private long mMillisPerPixel;
    private long mTimeOffset;
    private long mTimeLowerBoundary;
    private long mTimeUpperBoundary;

    private EPGData epgData = null;

    //My Addition
    private int mEventLayoutBackgroundSelected;
    private long mMargin = 200000;
    public String contentUri;
    public long start_time;
    public String catch_Time;
    public long duration;
    public String title;
    public String next_title;
    private boolean is_epg=true;

    private EPGEvent selectedEvent = null;
    private EPGChannel selectedChannel= null;

    private TextView currentEventTextView, currentEventTimeTextView, currentEventContentTextView, channel_name;
    private ImageView current_channel_image;
    private final int mButtonRadius;
    private boolean is_first = true;

    private boolean is_overlay=false;
    SimpleDateFormat catchTimeFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm");
    private SimpleDateFormat programTimeFormatLong = new SimpleDateFormat("dd MMM, EEEE  h:mm a");
    private SimpleDateFormat programTimeFormat = new SimpleDateFormat("h:mm a");
    public static final String NO_INFO = "No Information";
    private int pos_channel=0, pos_program;

    public void setPos_channel(int i){
        pos_channel = i;
    }

    public EPG(Context context) {
        this(context, null);
    }

    public EPG(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EPG(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);

        resetBoundaries();

        mDrawingRect = new Rect();
        mClipRect = new Rect();
        mMeasuringRect = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGestureDetector = new GestureDetector(context, new OnGestureListener());
        mChannelImageCache = Maps.newHashMap();
        mChannelImageTargetCache = Maps.newHashMap();

        // Adding some friction that makes the epg less flappy.
        mScroller = new Scroller(context);
        mScroller.setFriction(0.2f);

        mEPGBackground = getResources().getColor(R.color.epg_background);

        mChannelLayoutMargin = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_margin);
        mChannelLayoutPadding = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_padding);
        mChannelLayoutHeight = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_height);
        mChannelLayoutWidth = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_width);
        setResources();
        mButtonRadius = getResources().getDimensionPixelSize(R.dimen.epg_button_radius);
        mEventLayoutTextSize = getResources().getDimensionPixelSize(R.dimen.epg_event_layout_text);
        mChannelLayoutTextSize = getResources().getDimensionPixelSize(R.dimen.epg_channel_layout_text);
        mTimeBarHeight = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_height);
        mTimeBarTextSize = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_text);
        mTimeBarLineWidth = getResources().getDimensionPixelSize(R.dimen.epg_time_bar_line_width);
        mTimeBarLineColor = getResources().getColor(R.color.epg_time_bar);

        mResetButtonSize = getResources().getDimensionPixelSize(R.dimen.epg_reset_button_size);
        mResetButtonMargin = getResources().getDimensionPixelSize(R.dimen.epg_reset_button_margin);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = mResetButtonSize;
        options.outHeight = mResetButtonSize;
        mResetButtonIcon = BitmapFactory.decodeResource(getResources(), R.drawable.reset, options);
    }

    public void setIs_epg(boolean is_epg){
        this.is_epg = is_epg;
    }

    public boolean getIs_epg(){
        return is_epg;
    }
    public void setIs_overlay(boolean is_overlay) {
        this.is_overlay = is_overlay;
        setResources();
    }

    public EPGData getEpgData(){
        return epgData;
    }

    public void setIs_first(boolean is_first){
        this.is_first=is_first;
    }

    @Override
    //save state to recover state after restart or screen rotation
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        EPGState epgState = new EPGState(superState);
        epgState.setCurrentEvent(this.selectedEvent);
        return epgState;
    }

    @Override
    //recover the state
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof EPGState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        EPGState epgState = (EPGState)state;
        super.onRestoreInstanceState(epgState.getSuperState());
        this.selectedEvent = epgState.getCurrentEvent();
        this.selectedChannel = epgState.getCurrentEvent().getChannel();
    }

    private void setResources() {
        if (is_overlay){
            mChannelLayoutBackground = getResources().getColor(R.color.epg_channel_layout_background_overlay);
            mChannelLayoutForeground = getResources().getColor(R.color.epg_channel_layout_foreground);
            mChannelLayoutForegroundSelected = getResources().getColor(R.color.epg_channel_layout_foreground_selected);
            mTimeBarBackground = getResources().getColor(R.color.epg_timebar_background);
            mChannelLayoutTextColor = getResources().getColor(R.color.epg_channel_layout_text_overlay);
            mEventLayoutBackground = getResources().getColor(R.color.epg_event_layout_background_overlay);
            mEventLayoutBackgroundCurrent = getResources().getColor(R.color.epg_event_layout_background_current_overlay);
            mEventLayoutBackgroundSelected = getResources().getColor(R.color.epg_event_layout_background_selected_overlay);
            mEventLayoutTextColor = getResources().getColor(R.color.epg_event_layout_text_overlay);
            mEventLayoutTextColorOveray = getResources().getColor(R.color.black);
        }else {
            mChannelLayoutBackground = getResources().getColor(R.color.epg_channel_layout_background);
            mChannelLayoutForeground = getResources().getColor(R.color.epg_channel_layout_foreground);
            mChannelLayoutForegroundSelected = getResources().getColor(R.color.epg_channel_layout_foreground_selected);
            mTimeBarBackground = getResources().getColor(R.color.epg_timebar_background);
            mChannelLayoutTextColor = getResources().getColor(R.color.epg_channel_layout_text);
            mEventLayoutBackground = getResources().getColor(R.color.epg_event_layout_background);
            mEventLayoutBackgroundCurrent = getResources().getColor(R.color.epg_event_layout_background_current);
            mEventLayoutBackgroundSelected = getResources().getColor(R.color.epg_event_layout_background_selected);
            mEventLayoutTextColor = getResources().getColor(R.color.epg_event_layout_text);
            mEventLayoutTextColorOveray = getResources().getColor(R.color.black);
        }
    }

    public void selectEvent(EPGEvent epgEvent, boolean withAnimation) {
        if (this.selectedEvent != null) {
            this.selectedEvent.setSelected(false);
            if (this.selectedEvent.getChannel()!=null)this.selectedEvent.getChannel().selected = false;
        }
        epgEvent.getChannel().selected=true;
        epgEvent.setSelected(true);

        this.selectedEvent = epgEvent;
        selectedChannel = selectedEvent.getChannel();
        optimizeVisibility(epgEvent, withAnimation);
        loadProgramDetails(epgEvent);

        is_first = false;
        //redraw to get the coloring of the selected event
        redraw();
    }

    public void setCurrentEventTextView(TextView currentEventTextView) {
        this.currentEventTextView = currentEventTextView;
    }

    public void setCurrentEventTimeTextView(TextView currentEventTimeTextView) {
        this.currentEventTimeTextView = currentEventTimeTextView;
    }

    public void selectChannel(EPGChannel epgChannel, boolean getChannelPos){
        if(this.selectedChannel != null){
            this.selectedChannel.selected = false;
        }
        epgChannel.selected = true;
        this.selectedChannel = epgChannel;
        if (getChannelPos)pos_channel=getChannelPosition(selectedChannel);
        redraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (epgData != null && epgData.hasData()) {
            mTimeLowerBoundary = getTimeFrom(getScrollX());
            mTimeUpperBoundary = getTimeFrom(getScrollX() + getWidth());

            Rect drawingRect = mDrawingRect;
            drawingRect.left = getScrollX();
            drawingRect.top = getScrollY();
            drawingRect.right = drawingRect.left + getWidth();
            drawingRect.bottom = drawingRect.top + getHeight();

            drawChannelListItems(canvas, drawingRect);
            drawEvents(canvas, drawingRect);
            drawTimebar(canvas, drawingRect);
            drawTimeLine(canvas, drawingRect);
            drawResetButton(canvas, drawingRect);

            // If scroller is scrolling/animating do scroll. This applies when doing a fling.
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            if(is_first){
                EPGChannel epgChannelSelect;
                if (selectedChannel==null){
                    epgChannelSelect = epgData.getChannel(0);
                }else epgChannelSelect = selectedChannel;
                int program_pos=0;
                program_pos=getProgramPosition(epgChannelSelect, getTimeFrom(getXPositionStart() + (getWidth() / 2)));

                try {
                    selectEvent(epgData.getEvent(epgChannelSelect, program_pos), true);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                selectChannel(epgChannelSelect,true);
                long dT = 0;
                int dX = 0;
                int dY = 0;

                // calculate optimal Y position

                int minYVisible = getScrollY(); // is 0 when scrolled completely to top (first channel fully visible)
                int maxYVisible = minYVisible + getHeight();
                int currentChannelPosition = pos_channel;

                for (int i=0;i<epgData.getChannels().size();i++){
                    EPGChannel epgChannel=epgData.getChannels().get(i);
                    if (epgChannel.getStream_id()==epgData.getChannel(pos_channel).getStream_id())
                        currentChannelPosition = i;
                }
                int currentChannelTop = mTimeBarHeight + (currentChannelPosition * (mChannelLayoutHeight + mChannelLayoutMargin));
                int currentChannelBottom = currentChannelTop + mChannelLayoutHeight;
                if (currentChannelTop < minYVisible) {
                    dY = currentChannelTop - minYVisible - mTimeBarHeight;
                } else if (currentChannelBottom > maxYVisible) {
                    dY = currentChannelBottom - maxYVisible;
                }
                Log.e("position info:", "epgheight: "+getHeight()+"maxy: "+maxYVisible+" miny: " +minYVisible+ " currentChannelPosition:"+
                        currentChannelPosition+" currentChannelTop:"+currentChannelTop+" currentChannelBottom:"+currentChannelBottom +" dy:"+dY);

                // calculate optimal X position

                mTimeLowerBoundary = getTimeFrom(getScrollX());
                mTimeUpperBoundary = getTimeFrom(getScrollX() + getProgramAreaWidth());
                if (epgChannelSelect.getEvents().size()>program_pos && epgData.getEvent(epgChannelSelect, program_pos).getEndTime().getTime() > mTimeUpperBoundary) {
                    //we need to scroll the grid to the left
                    dT = (mTimeUpperBoundary - epgData.getEvent(epgChannelSelect, program_pos).getEndTime().getTime() - mMargin) * -1;
                    dX = Math.round(dT / mMillisPerPixel);
                }
                mTimeLowerBoundary = getTimeFrom(getScrollX());
                mTimeUpperBoundary = getTimeFrom(getScrollX() + getWidth());
                if (epgChannelSelect.getEvents().size()>program_pos && epgData.getEvent(epgChannelSelect, program_pos).getStartTime().getTime() < mTimeLowerBoundary) {
                    //we need to scroll the grid to the right
                    dT = (epgData.getEvent(epgChannelSelect, program_pos).getStartTime().getTime() - mTimeLowerBoundary - mMargin);
                    dX = Math.round(dT / mMillisPerPixel);
                }

                if (dX != 0 || dY != 0) {
                    mScroller.startScroll(getScrollX(), getScrollY(), dX, dY, true ? 600 : 0);
                }
                Log.e("onDraw_dx:dy", dX+" : "+dY);

                is_first = false;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        recalculateAndRedraw(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    }

    private void drawResetButton(Canvas canvas, Rect drawingRect) {
        // Show button when scrolled 1/3 of screen width from current time
        final long threshold = getWidth() / 3;
        if (Math.abs(getXPositionStart() - getScrollX()) > threshold) {
            drawingRect = calculateResetButtonHitArea();
            mPaint.setColor(mTimeBarLineColor);
            canvas.drawCircle(drawingRect.right - (mResetButtonSize / 2),
                    drawingRect.bottom - (mResetButtonSize / 2),
                    Math.min(drawingRect.width(), drawingRect.height()) / 2,
                    mPaint);

            drawingRect.left += mResetButtonMargin;
            drawingRect.right -= mResetButtonMargin;
            drawingRect.top += mResetButtonMargin;
            drawingRect.bottom -= mResetButtonMargin;
            canvas.drawBitmap(mResetButtonIcon, null, drawingRect, mPaint);
        }
    }

    private void drawTimebarBottomStroke(Canvas canvas, Rect drawingRect) {
        drawingRect.left = getScrollX();
        drawingRect.top = getScrollY() + mTimeBarHeight;
        drawingRect.right = drawingRect.left + getWidth();
        drawingRect.bottom = drawingRect.top + mChannelLayoutMargin;

        // Bottom stroke
        mPaint.setColor(mEPGBackground);
        canvas.drawRect(drawingRect, mPaint);
    }

    private void drawTimebar(Canvas canvas, Rect drawingRect) {
        drawingRect.left = getScrollX() + mChannelLayoutWidth + mChannelLayoutMargin;
        drawingRect.top = getScrollY();
        drawingRect.right = drawingRect.left + getWidth();
        drawingRect.bottom = drawingRect.top + mTimeBarHeight;

        mClipRect.left = getScrollX() + mChannelLayoutWidth + mChannelLayoutMargin;
        mClipRect.top = getScrollY();
        mClipRect.right = getScrollX() + getWidth();
        mClipRect.bottom = mClipRect.top + mTimeBarHeight;

        canvas.save();
        canvas.clipRect(mClipRect);

        // Background
        mPaint.setColor(mTimeBarBackground);
        canvas.drawRect(drawingRect, mPaint);

        // Time stamps
        mPaint.setColor(mEventLayoutTextColor);
        mPaint.setTextSize(mTimeBarTextSize);

        for (int i = 0; i < HOURS_IN_VIEWPORT_MILLIS / TIME_LABEL_SPACING_MILLIS; i++) {
            // Get time and round to nearest half hour
            final long time = TIME_LABEL_SPACING_MILLIS *
                    (((mTimeLowerBoundary + (TIME_LABEL_SPACING_MILLIS * i)) +
                            (TIME_LABEL_SPACING_MILLIS / 2)) / TIME_LABEL_SPACING_MILLIS);

            canvas.drawText(EPGUtil.getShortTime(time),
                    getXFrom(time),
                    drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTimeBarTextSize / 2)), mPaint);
        }

        canvas.restore();

        drawTimebarDayIndicator(canvas, drawingRect);
        drawTimebarBottomStroke(canvas, drawingRect);
    }

    private void drawTimebarDayIndicator(Canvas canvas, Rect drawingRect) {
        drawingRect.left = getScrollX();
        drawingRect.top = getScrollY();
        drawingRect.right = drawingRect.left + mChannelLayoutWidth;
        drawingRect.bottom = drawingRect.top + mTimeBarHeight;

        // Background
        mPaint.setColor(mTimeBarBackground);
        canvas.drawRect(drawingRect, mPaint);

        // Text
        mPaint.setColor(mEventLayoutTextColor);
        mPaint.setTextSize(mTimeBarTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(EPGUtil.getWeekdayName(mTimeLowerBoundary),
                drawingRect.left + ((drawingRect.right - drawingRect.left) / 2),
                drawingRect.top + (((drawingRect.bottom - drawingRect.top) / 2) + (mTimeBarTextSize / 2)), mPaint);

        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawTimeLine(Canvas canvas, Rect drawingRect) {
        long now = System.currentTimeMillis();

        if (shouldDrawTimeLine(now)) {
            drawingRect.left = getXFrom(now);
            drawingRect.top = getScrollY();
            drawingRect.right = drawingRect.left + mTimeBarLineWidth;
            drawingRect.bottom = drawingRect.top + getHeight();

            mPaint.setColor(mTimeBarLineColor);
            canvas.drawRect(drawingRect, mPaint);
        }

    }

    private void drawEvents(Canvas canvas, Rect drawingRect) {
        final int firstPos = getFirstVisibleChannelPosition();
        final int lastPos = getLastVisibleChannelPosition();

        for (int pos = firstPos; pos <= lastPos; pos++) {

            // Set clip rectangle
            mClipRect.left = getScrollX() + mChannelLayoutWidth + mChannelLayoutMargin;
            mClipRect.top = getTopFrom(pos);
            mClipRect.right = getScrollX() + getWidth();
            mClipRect.bottom = mClipRect.top + mChannelLayoutHeight;

            canvas.save();
            canvas.clipRect(mClipRect);

            // Draw each event
            boolean foundFirst = false;

            List<EPGEvent> epgEvents = epgData.getEvents(pos);
            if (epgEvents==null || epgEvents.size()==0) {
                epgEvents = new ArrayList<>();
                epgEvents.add(new EPGEvent(epgData.getChannel(pos),NO_INFO,NO_INFO));
            }
            for (EPGEvent event : epgEvents) {
                if (isEventVisible(event.getStartTime().getTime(), event.getEndTime().getTime())) {
                    drawEvent(canvas, pos, event, drawingRect);
                    foundFirst = true;
                } else if (foundFirst) {
                    break;
                }
            }

            canvas.restore();
        }

    }

    private void drawEvent(final Canvas canvas, final int channelPosition, final EPGEvent event, final Rect drawingRect) {

        if (event.getTitle().equals(NO_INFO)){
            drawingRect.top = getTopFrom(channelPosition);
            drawingRect.bottom = drawingRect.top + mChannelLayoutHeight;
            Path path = Utils.RoundedRect(drawingRect.left,drawingRect.top,drawingRect.right,drawingRect.bottom,mButtonRadius,mButtonRadius,false);
            if (event.getChannel().selected) {
                mPaint.setColor(mEventLayoutBackgroundSelected);
                Paint mStrokePaint = new Paint();
                mStrokePaint.setColor(mChannelLayoutForegroundSelected);
                mStrokePaint.setAntiAlias(true);
                mStrokePaint.setStrokeWidth(2);
                mStrokePaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path,mStrokePaint);
            }
            else mPaint.setColor(mEventLayoutBackground);
            canvas.drawPath(path,mPaint);
        }
        else {
            setEventDrawingRectangle(channelPosition, event.getStartTime().getTime(), event.getEndTime().getTime(), drawingRect);

            Path path = Utils.RoundedRect(drawingRect.left,drawingRect.top,drawingRect.right,drawingRect.bottom,mButtonRadius,mButtonRadius,false);
            // Background
            if (event.isSelected()) {
                mPaint.setColor(mEventLayoutBackgroundSelected);
            } else if (event.isCurrent()||event.getTitle().equals(NO_INFO)) {
                mPaint.setColor(mEventLayoutBackgroundCurrent);
            } else {
                mPaint.setColor(mEventLayoutBackground);
            }
//        canvas.drawRect(drawingRect, mPaint);
            if (event.isSelected()){
                Paint mStrokePaint = new Paint();
                mStrokePaint.setColor(mChannelLayoutForegroundSelected);
                mStrokePaint.setAntiAlias(true);
                mStrokePaint.setStrokeWidth(2);
                mStrokePaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path,mStrokePaint);
            }

            canvas.drawPath(path,mPaint);
        }

        // Add left and right inner padding
        drawingRect.left += mChannelLayoutPadding;
        drawingRect.right -= mChannelLayoutPadding;

        // Text
        mPaint.setColor(mEventLayoutTextColor);
        mPaint.setTextSize(mEventLayoutTextSize);

        // Move drawing.top so text will be centered (text is drawn bottom>up)
        mPaint.getTextBounds(event.getTitle(), 0, event.getTitle().length(), mMeasuringRect);
        drawingRect.top += (((drawingRect.bottom - drawingRect.top) / 2) + (mMeasuringRect.height()/2));

        String title = event.getTitle();
        title = title.substring(0,
                mPaint.breakText(title, true, drawingRect.right - drawingRect.left, null));
        canvas.drawText(title, drawingRect.left, drawingRect.top, mPaint);

    }

    private void setEventDrawingRectangle(final int channelPosition, final long start, final long end, final Rect drawingRect) {
        drawingRect.left = getXFrom(start);
        drawingRect.top = getTopFrom(channelPosition);
        drawingRect.right = getXFrom(end) - mChannelLayoutMargin;
        drawingRect.bottom = drawingRect.top + mChannelLayoutHeight;
    }

    private void drawChannelListItems(Canvas canvas, Rect drawingRect) {
        // Background
        mMeasuringRect.left = getScrollX();
        mMeasuringRect.top = getScrollY();
        mMeasuringRect.right = drawingRect.left + mChannelLayoutWidth;
        mMeasuringRect.bottom = mMeasuringRect.top + getHeight();

        mPaint.setColor(mChannelLayoutBackground);
        canvas.drawRect(mMeasuringRect, mPaint);

        final int firstPos = getFirstVisibleChannelPosition();
        final int lastPos = getLastVisibleChannelPosition();

        for (int pos = firstPos; pos <= lastPos; pos++) {
            drawChannelItem(canvas, pos, drawingRect);
        }
    }

    private void drawChannelItem(final Canvas canvas, int position, Rect drawingRect) {
        drawingRect.left = getScrollX();
        drawingRect.top = getTopFrom(position);
        drawingRect.right = drawingRect.left + mChannelLayoutWidth;
        drawingRect.bottom = drawingRect.top + mChannelLayoutHeight;

        // Loading channel image into target for
        final String imageURL = epgData.getChannel(position).getStream_icon();

        if (epgData.getChannel(position).isSelected()) {
            mPaint.setColor(mChannelLayoutForegroundSelected);
        } else {
            mPaint.setColor(mChannelLayoutForeground);
        }
        Path path = Utils.RoundedRect(drawingRect.left,drawingRect.top,drawingRect.right,drawingRect.bottom,mButtonRadius,mButtonRadius,false);
        canvas.drawPath(path,mPaint);
//        canvas.drawRect(drawingRect, mPaint);

        // Add left and right inner padding
        drawingRect.left += mChannelLayoutPadding;
        drawingRect.right -= mChannelLayoutPadding;
        Rect rect_num, rect_icon, rect_name;
        rect_num = new Rect(drawingRect);
        rect_icon = new Rect(drawingRect);
        rect_name = new Rect(drawingRect);

        rect_num.left+=mChannelLayoutPadding;
        rect_num.right = rect_num.left+mChannelLayoutHeight;

        rect_icon.left = rect_num.right;
        rect_icon.right = rect_icon.left+mChannelLayoutHeight;

        rect_name.left = rect_icon.right;
        rect_name.right = drawingRect.right;

        String text = String.valueOf(position+1);
        if(epgData.getChannel(position).isSelected()){
            mPaint.setColor(mEventLayoutTextColorOveray);
        }else {
            mPaint.setColor(mEventLayoutTextColor);
        }
        mPaint.setTextSize(mChannelLayoutTextSize);
        drawChannelText(canvas,rect_num,text);
        if (mChannelImageCache.containsKey(imageURL)) {
            Bitmap image = mChannelImageCache.get(imageURL);
            if (image!=null)rect_icon = getDrawingRectForChannelImage(rect_icon, image);
            if (image!=null)canvas.drawBitmap(image, null, rect_icon, null);
        } else {
            final int smallestSide = Math.min(mChannelLayoutHeight, mChannelLayoutWidth);

            if (!mChannelImageTargetCache.containsKey(imageURL)) {
                mChannelImageTargetCache.put(imageURL, new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mChannelImageCache.put(imageURL, bitmap);
                        redraw();
                        mChannelImageTargetCache.remove(imageURL);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                Log.e("EPG",imageURL+" "+smallestSide);
                EPGUtil.loadImageInto(getContext(), imageURL, smallestSide, smallestSide, mChannelImageTargetCache.get(imageURL));
            }

        }
        EPGChannel epgChannel = epgData.getChannel(position);
        if(epgData.getChannel(position).isSelected()){
            mPaint.setColor(mEventLayoutTextColorOveray);
        }else {
            mPaint.setColor(mChannelLayoutTextColor);
        }
        mPaint.setTextSize(mChannelLayoutTextSize);
        drawChannelText(canvas,rect_name,epgChannel.getName());
    }

    private void drawChannelText(Canvas canvas, Rect drawingRect, String text) {
        // Text
        // Move drawing.top so text will be centered (text is drawn bottom>up)
        mPaint.getTextBounds(text, 0, text.length(), mMeasuringRect);
        drawingRect.top += (((drawingRect.bottom - drawingRect.top) / 2) + (mMeasuringRect.height()/2));
        String title = text;
        title = title.substring(0,
                mPaint.breakText(title, true, drawingRect.right - drawingRect.left, null));
        canvas.drawText(title, drawingRect.left, drawingRect.top, mPaint);
    }

    private Rect getDrawingRectForChannelImage(Rect drawingRect, Bitmap image) {
        drawingRect.left += mChannelLayoutPadding;
        drawingRect.top += mChannelLayoutPadding;
        drawingRect.right -= mChannelLayoutPadding;
        drawingRect.bottom -= mChannelLayoutPadding;

        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        final float imageRatio = imageHeight / (float) imageWidth;

//        final int rectWidth = drawingRect.right - drawingRect.left;
        final int rectHeight = drawingRect.bottom - drawingRect.top;

        // Keep aspect ratio.
        if (imageWidth > imageHeight) {
            final int padding = (int) (rectHeight - (rectHeight * imageRatio)) / 2;
            drawingRect.top += padding;
            drawingRect.bottom -= padding;
        } else {
            final int padding = (int) (rectHeight - (rectHeight / imageRatio)) / 2;
            drawingRect.left += padding;
            drawingRect.right -= padding;
        }

        return drawingRect;
    }

    private boolean shouldDrawTimeLine(long now) {
        return now >= mTimeLowerBoundary && now < mTimeUpperBoundary;
    }

    private boolean isEventVisible(final long start, final long end) {
        return (start >= mTimeLowerBoundary && start <= mTimeUpperBoundary)
                || (end >= mTimeLowerBoundary && end <= mTimeUpperBoundary)
                || (start <= mTimeLowerBoundary && end >= mTimeUpperBoundary);
    }

    private long calculatedBaseLine() {
        return LocalDateTime.now().toDateTime().minusMillis(DAYS_BACK_MILLIS).getMillis();
    }

    private int getFirstVisibleChannelPosition() {
        final int y = getScrollY();

        int position = (y - mChannelLayoutMargin - mTimeBarHeight)
                / (mChannelLayoutHeight + mChannelLayoutMargin);

        if (position < 0) {
            position = 0;
        }
        return position;
    }

    private int getLastVisibleChannelPosition() {
        final int y = getScrollY();
        final int totalChannelCount = epgData.getChannelCount();
        final int screenHeight = getHeight();
        int position = (y + screenHeight + mTimeBarHeight - mChannelLayoutMargin)
                / (mChannelLayoutHeight + mChannelLayoutMargin);

        if (position > totalChannelCount - 1) {
            position = totalChannelCount - 1;
        }

        // Add one extra row if we don't fill screen with current..
        return (y + screenHeight) > (position * mChannelLayoutHeight) && position < totalChannelCount - 1 ? position + 1 : position;
    }

    private void calculateMaxHorizontalScroll() {
        mMaxHorizontalScroll = (int) ((DAYS_BACK_MILLIS + DAYS_FORWARD_MILLIS - HOURS_IN_VIEWPORT_MILLIS) / mMillisPerPixel);
    }

    private void calculateMaxVerticalScroll() {
        final int maxVerticalScroll = getTopFrom(epgData.getChannelCount() - 2) + mChannelLayoutHeight;
        mMaxVerticalScroll = maxVerticalScroll < getHeight() ? 0 : maxVerticalScroll - getHeight();
    }

    private int getXFrom(long time) {
        return (int) ((time - mTimeOffset) / mMillisPerPixel) + mChannelLayoutMargin
                + mChannelLayoutWidth + mChannelLayoutMargin;
    }

    private int getTopFrom(int position) {
        int y = position * (mChannelLayoutHeight + mChannelLayoutMargin)
                + mChannelLayoutMargin + mTimeBarHeight;
        return y;
    }

    private long getTimeFrom(int x) {
        return (x * mMillisPerPixel) + mTimeOffset;
    }

    private long calculateMillisPerPixel() {
        return HOURS_IN_VIEWPORT_MILLIS / (getResources().getDisplayMetrics().widthPixels - mChannelLayoutWidth - mChannelLayoutMargin);
    }

    private int getXPositionStart() {
        return getXFrom(System.currentTimeMillis() - (HOURS_IN_VIEWPORT_MILLIS / 2));
    }

    private void resetBoundaries() {
        mMillisPerPixel = calculateMillisPerPixel();
        mTimeOffset = calculatedBaseLine();
        mTimeLowerBoundary = getTimeFrom(0);
        mTimeUpperBoundary = getTimeFrom(getWidth());
    }

    private Rect calculateChannelsHitArea() {
        mMeasuringRect.top = mTimeBarHeight;
        int visibleChannelsHeight = epgData.getChannelCount() * (mChannelLayoutHeight + mChannelLayoutMargin);
        mMeasuringRect.bottom = visibleChannelsHeight < getHeight() ? visibleChannelsHeight : getHeight();
        mMeasuringRect.left = 0;
        mMeasuringRect.right = mChannelLayoutWidth;
        return mMeasuringRect;
    }

    private Rect calculateProgramsHitArea() {
        mMeasuringRect.top = mTimeBarHeight;
        int visibleChannelsHeight = epgData.getChannelCount() * (mChannelLayoutHeight + mChannelLayoutMargin);
        mMeasuringRect.bottom = visibleChannelsHeight < getHeight() ? visibleChannelsHeight : getHeight();
        mMeasuringRect.left = mChannelLayoutWidth;
        mMeasuringRect.right = getWidth();
        return mMeasuringRect;
    }

    private Rect calculateResetButtonHitArea() {
        mMeasuringRect.left = getScrollX() + getWidth() - mResetButtonSize - mResetButtonMargin;
        mMeasuringRect.top = getScrollY() + getHeight() - mResetButtonSize - mResetButtonMargin;
        mMeasuringRect.right = mMeasuringRect.left + mResetButtonSize;
        mMeasuringRect.bottom = mMeasuringRect.top + mResetButtonSize;
        return mMeasuringRect;
    }

    private int getChannelPosition(int y) {
        y -= mTimeBarHeight;
        int channelPosition = (y + mChannelLayoutMargin)
                / (mChannelLayoutHeight + mChannelLayoutMargin);

        return epgData.getChannelCount() == 0 ? -1 : channelPosition;
    }

    private int getChannelPosition(EPGChannel epgChannel){
        for (int i=0;i<epgData.getChannelCount();i++){
            if (epgData.getChannel(i).getStream_id().equals(epgChannel.getStream_id()))
                return i;
        }
        return 0;
    }

    private int getProgramPosition(int channelPosition, long time) {
        List<EPGEvent> events = epgData.getEvents(channelPosition);

        if (events != null) {

            for (int eventPos = 0; eventPos < events.size(); eventPos++) {
                EPGEvent event = events.get(eventPos);

                if (event.getStartTime().getTime() <= time && event.getEndTime().getTime() >= time) {
                    return eventPos;
                }
            }
        }
        return 0;
    }

    private int getProgramPosition(EPGChannel epgChannel, long time) {
        List<EPGEvent> events = epgChannel.getEvents();

        if (events != null) {

            for (int eventPos = 0; eventPos < events.size(); eventPos++) {
                EPGEvent event = events.get(eventPos);

                if (event.getStartTime().getTime() <= time && event.getEndTime().getTime() >= time) {
                    return eventPos;
                }
            }
        }
        return 0;
    }

    private EPGEvent getProgramAtTime(EPGChannel epgChannel, long time) {
        List<EPGEvent> events = epgChannel.getEvents();

        if (events != null && events.size()>0) {

            for (int eventPos = 0; eventPos < events.size(); eventPos++) {
                EPGEvent event = events.get(eventPos);
                if (events.size()==1 && event.getTitle().equals(NO_INFO)) return event;
                if (event.getStartTime().getTime() <= time && event.getEndTime().getTime() >= time) {
                    return event;
                }
            }
        }
        events = new ArrayList<>();
        events.add(new EPGEvent(epgChannel,NO_INFO,NO_INFO));
        return events.get(0);
    }

    /**
     * Add click listener to the EPG.
     * @param epgClickListener to add.
     */
    public void setEPGClickListener(EPGClickListener epgClickListener) {
        mClickListener = epgClickListener;
    }

    /**
     * Add data to EPG. This must be set for EPG to able to draw something.
     * @param epgData pass in any implementation of EPGData.
     */
//    public void MergeEPGData(EPGData epgData) {
//        this.epgData = GetMergeEPGData(this.epgData, epgData);
//    }
    public void SetEPGData(EPGData epgData) {
        this.epgData = epgData;
        pos_channel = 0;
        for (EPGEvent epgEvent:epgData.getEvents(pos_channel)){
            if (epgEvent.isCurrent())
                selectEvent(epgEvent,true);
        }
    }

//    private EPGData GetMergeEPGData(EPGData oldData, EPGData newData) {
//        try {
//            if (oldData == null) {
//                Map<EPGChannel, List<EPGEvent>> map = Maps.newLinkedHashMap();
//                oldData = new EPGDataImpl(newData.getName(),map);
//            }
//            if (newData != null) {
//                for (int i = 0; i < newData.getChannelCount(); i++) {
//                    EPGChannel newChannel = newData.getChannel(i);
//                    EPGChannel oldChannel = oldData.getOrCreateChannel(newChannel.getStream_icon(),newChannel.getName(),newChannel.getId(),newChannel.getNum(),newChannel.getStream_id());
//                    for (int j = 0; j < newChannel.getEvents().size(); j++) {
//                        EPGEvent newEvent = newChannel.getEvents().get(j);
//                        oldChannel.addEvent(newEvent);
//                    }
//                }
//            }
//            return oldData;
//        } catch (Throwable e) {
//            throw new RuntimeException("Could not merge EPG data: " + e.getClass().getSimpleName() + " " + e.getMessage(), e);
//        }
//    }

    /**
     * This will recalculate boundaries, maximal scroll and scroll to start position which is current time.
     * To be used on device rotation etc since the device height and width will change.
     * @param withAnimation true if scroll to current position should be animated.
     */
    public void recalculateAndRedraw(boolean withAnimation) {
        if (epgData != null && epgData.hasData()) {
            resetBoundaries();

            calculateMaxVerticalScroll();
            calculateMaxHorizontalScroll();
            //Select initial event
            if(selectedEvent!=null){
                selectEvent(selectedEvent, withAnimation);
                selectChannel(selectedEvent.getChannel(),withAnimation);
            }else {
                pos_channel=0;
                try {
                    selectEvent(epgData.getEvent(pos_channel, getProgramPosition(pos_channel, getTimeFrom(getXPositionStart() + (getWidth() / 2)))), withAnimation);
                }catch (Exception e){
                    e.printStackTrace();
                }
                selectChannel(epgData.getChannel(pos_channel),false);
            }
//            mScroller.startScroll(getScrollX(), getScrollY(),
//                    getXPositionStart() - getScrollX(),
//                    0, withAnimation ? 600 : 0);

            redraw();
        }
    }

    /**
     * Does a invalidate() and requestLayout() which causes a redraw of screen.
     */
    public void redraw() {
        invalidate();
        requestLayout();
    }

    /**
     * Clears the local image cache for channel images. Can be used when leaving epg and you want to
     * free some memory. Images will be fetched again when loading EPG next time.
     */
    public void clearEPGImageCache() {
        mChannelImageCache.clear();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.e(TAG,is_epg+ " "+event.toString()+" dispatchKeyEvent");
        mTimeLowerBoundary = getTimeFrom(getScrollX());
        mTimeUpperBoundary = getTimeFrom(getScrollX() + getWidth());

        if (event.getAction()==KeyEvent.ACTION_DOWN && is_epg){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    is_epg=!is_epg;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (selectedEvent != null) {
                        if (selectedEvent.getNextEvent() != null) {
                            selectedEvent.setSelected(false);
                            selectedEvent = selectedEvent.getNextEvent();
                            selectedEvent.setSelected(true);
                            contentUri = "ok";
                            start_time = selectedEvent.getStartTime().getTime();
                            catchTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                            String catchTime = catchTimeFormat.format(selectedEvent.getStartTime().getTime());
                            duration = (selectedEvent.getEndTime().getTime()-selectedEvent.getStartTime().getTime())/1000/60;
                            Log.e("EPG",catchTime);
                            catch_Time = catchTime;
                            title = selectedEvent.getTitle();
                            if (selectedEvent.getNextEvent()!=null)
                                next_title = selectedEvent.getNextEvent().getTitle();
                            optimizeVisibility(selectedEvent, true);
                        }else {
                            Log.e("next event","null");
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (selectedEvent != null) {
                        if (selectedEvent.getPreviousEvent() != null) {
                            selectedEvent.setSelected(false);
                            selectedEvent = selectedEvent.getPreviousEvent();
                            selectedEvent.setSelected(true);
                            catchTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                            contentUri = "ok";
                            start_time = selectedEvent.getStartTime().getTime();
                            String catchTime = catchTimeFormat.format(selectedEvent.getStartTime().getTime());
                            duration = (selectedEvent.getEndTime().getTime()-selectedEvent.getStartTime().getTime())/1000/60;
                            Log.e("EPG",catchTime);
                            catch_Time = catchTime;
                            title = selectedEvent.getTitle();
                            if (selectedEvent.getNextEvent()!=null)
                                next_title = selectedEvent.getNextEvent().getTitle();
                            optimizeVisibility(selectedEvent, true);
                        }else {
                            Log.e("next event","null");
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    int next_i;
                    if (pos_channel>0) {
                        next_i = pos_channel-1;
                    }else {
                        next_i = epgData.getChannelCount()-1;
                    }
                    try {
                        EPGChannel channel = epgData.getChannel(next_i);
                        long lowerBoundary,upperBoundary,eventMiddleTime;
                        if (selectedEvent!=null) {
                            lowerBoundary= Math.max(mTimeLowerBoundary, selectedEvent.getStartTime().getTime());
                            upperBoundary = Math.min(mTimeUpperBoundary, selectedEvent.getEndTime().getTime());
                            eventMiddleTime = (lowerBoundary + upperBoundary) / 2;
                        }else {
                            eventMiddleTime = Calendar.getInstance().getTimeInMillis();
                        }
                        long nowMils = System.currentTimeMillis();
                        for(int i = 0;i<channel.getEvents().size();i++){
                            if(nowMils>channel.getEvents().get(i).getStartTime().getTime() &&
                                    nowMils<=channel.getEvents().get(i).getEndTime().getTime()){
                                eventMiddleTime = (channel.getEvents().get(i).getStartTime().getTime() +
                                        channel.getEvents().get(i).getEndTime().getTime()) / 2;
                            }
                        }
                        EPGEvent previousChannelEvent = getProgramAtTime(channel, eventMiddleTime);
                        if (previousChannelEvent != null) {
                            Log.e("KEY_DPAD_UP",channel.getName()+" "+previousChannelEvent.getTitle());
                            if (selectedEvent!=null) selectedEvent.setSelected(false);
                            selectedEvent = previousChannelEvent;
                            selectedEvent.setSelected(true);
                            selectedChannel.selected = false;
                            selectedChannel = channel;
                            pos_channel = next_i;
                            selectedChannel.selected = true;
                            contentUri = "ok";
                            start_time = selectedEvent.getStartTime().getTime();
                            catchTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                            String catchTime = catchTimeFormat.format(selectedEvent.getStartTime().getTime());
                            duration = (selectedEvent.getEndTime().getTime()-selectedEvent.getStartTime().getTime())/1000/60;
                            catch_Time = catchTime;
                            title = selectedEvent.getTitle();
                            if (selectedEvent.getNextEvent()!=null)
                                next_title = selectedEvent.getNextEvent().getTitle();
                            optimizeVisibility(selectedEvent, true);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (pos_channel<epgData.getChannelCount()-1) {
                        next_i = pos_channel+1;
                    }else next_i = 0;
                    try {
                        EPGChannel channel = epgData.getChannel(next_i);
                        long lowerBoundary,upperBoundary,eventMiddleTime;
                        if (selectedEvent!=null) {
                            lowerBoundary= Math.max(mTimeLowerBoundary, selectedEvent.getStartTime().getTime());
                            upperBoundary = Math.min(mTimeUpperBoundary, selectedEvent.getEndTime().getTime());
                            eventMiddleTime = (lowerBoundary + upperBoundary) / 2;
                        }else {
                            eventMiddleTime = Calendar.getInstance().getTimeInMillis();
                        }
                        long nowMils = System.currentTimeMillis();
                        for(int i = 0;i<channel.getEvents().size();i++){
                            if(nowMils>channel.getEvents().get(i).getStartTime().getTime() &&
                                    nowMils<=channel.getEvents().get(i).getEndTime().getTime()){
                                eventMiddleTime = (channel.getEvents().get(i).getStartTime().getTime() +
                                        channel.getEvents().get(i).getEndTime().getTime()) / 2;
                            }
                        }
                        EPGEvent nextChannelEvent = getProgramAtTime(channel, eventMiddleTime);
                        if (nextChannelEvent != null) {
                            Log.e("KEY_DPAD_DOWN",channel.getName()+" "+nextChannelEvent.getTitle());
                            if (selectedEvent!=null) selectedEvent.setSelected(false);
                            selectedEvent = nextChannelEvent;
                            selectedEvent.setSelected(true);
                            selectedChannel.selected = false;
                            selectedChannel = channel;
                            pos_channel = next_i;
                            selectedChannel.selected = true;
                            contentUri = "ok";
                            start_time = selectedEvent.getStartTime().getTime();
                            catchTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                            String catchTime = catchTimeFormat.format(selectedEvent.getStartTime().getTime());
                            duration = (selectedEvent.getEndTime().getTime()-selectedEvent.getStartTime().getTime())/1000/60;
                            catch_Time = catchTime;
                            title = selectedEvent.getTitle();
                            if (selectedEvent.getNextEvent()!=null)
                                next_title = selectedEvent.getNextEvent().getTitle();
                            optimizeVisibility(selectedEvent, true);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case KeyEvent.KEYCODE_BUTTON_R1:
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    gotoNextDay(selectedEvent);
                    break;
                case KeyEvent.KEYCODE_BUTTON_L1:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    gotoPreviousDay(selectedEvent);
                    break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    mClickListener.onEventClicked(pos_channel,pos_program,selectedEvent);
                    break;
            }
            if (selectedEvent != null) {
                loadProgramDetails(selectedEvent);
            }
            redraw();
//          Log.e("selected_event",selectedEvent.getTitle()+":"+selectedEvent.getStart()+":"+selectedEvent.getEnd()+" start:"+selectedEvent.getStart_timestamp()+" end:"+selectedEvent.getStop_timestamp());
        }
        return super.dispatchKeyEvent(event);
    }

    private void gotoPreviousDay(EPGEvent currentEvent) {
        //TODO
    }

    private void gotoNextDay(EPGEvent currentEvent) {
        //TODO
    }

    private void loadProgramDetails(EPGEvent epgEvent) {
        currentEventTextView.setText(epgEvent.getTitle());
        currentEventTimeTextView.setText(programTimeFormatLong.format(epgEvent.getStartTime().getTime()) + " - " + programTimeFormat.format(epgEvent.getEndTime().getTime()));
        currentEventContentTextView.setText(epgEvent.getDec());
        try{
            channel_name.setText(epgEvent.getChannel().getName());
        }catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Picasso.with(getContext()).load(epgEvent.getChannel().getStream_icon())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .error(R.drawable.icon)
                    .into(current_channel_image);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void optimizeVisibility(EPGEvent epgEvent, boolean withAnimation) {

        long dT = 0;
        int dX = 0;
        int dY = 0;

        // calculate optimal Y position

        int minYVisible = getScrollY(); // is 0 when scrolled completely to top (first channel fully visible)
        int maxYVisible = minYVisible + getHeight();
//        int program_pos=0;
//        program_pos=getProgramPosition(selectedChannel, getTimeFrom(getXPositionStart() + (getWidth() / 2)));

        int currentChannelPosition = epgEvent.getChannel().getChannelID();
        for (int i=0;i<epgData.getChannels().size();i++){
            EPGChannel epgChannel=epgData.getChannels().get(i);
            if (epgChannel.getStream_id()==selectedChannel.getStream_id()) {
                currentChannelPosition = i;
                break;
            }
        }
        int currentChannelTop = mTimeBarHeight + (currentChannelPosition * (mChannelLayoutHeight + mChannelLayoutMargin));
        int currentChannelBottom = currentChannelTop + mChannelLayoutHeight;

        if (currentChannelTop < minYVisible) {
            dY = currentChannelTop - minYVisible - mTimeBarHeight;
        } else if (currentChannelBottom > maxYVisible) {
            dY = currentChannelBottom - maxYVisible;
        }
        Log.e("measure","minYVisible:"+minYVisible+"maxYVisible:"+maxYVisible+"currentChannelTop:"+currentChannelTop);
        // calculate optimal X position

        mTimeLowerBoundary = getTimeFrom(getScrollX());
        mTimeUpperBoundary = getTimeFrom(getScrollX() + getProgramAreaWidth());
        if (epgEvent.getEndTime().getTime() > mTimeUpperBoundary) {
            //we need to scroll the grid to the left
            dT = (mTimeUpperBoundary - epgEvent.getEndTime().getTime() - mMargin) * -1;
            dX = Math.round(dT / mMillisPerPixel);
        }
        mTimeLowerBoundary = getTimeFrom(getScrollX());
        mTimeUpperBoundary = getTimeFrom(getScrollX() + getWidth());
        if (epgEvent.getStartTime().getTime() < mTimeLowerBoundary) {
            //we need to scroll the grid to the right
            dT = (this.selectedEvent.getStartTime().getTime() - mTimeLowerBoundary - mMargin);
            dX = Math.round(dT / mMillisPerPixel);
        }

        if (dX != 0 || dY != 0) {
            mScroller.startScroll(getScrollX(), getScrollY(), dX, dY, withAnimation ? 600 : 0);
        }
        Log.e("optimizeVisibile_dx:dy", dX+" : "+dY);
    }

    private int getChannelAreaWidth() {
        return mChannelLayoutWidth + mChannelLayoutPadding + mChannelLayoutMargin;
    }

    private int getProgramAreaWidth() {
        return getWidth() - getChannelAreaWidth();
    }

    public TextView getCurrentEventContentTextView() {
        return currentEventContentTextView;
    }

    public void setCurrentEventContentTextView(TextView currentEventContentTextView) {
        this.currentEventContentTextView = currentEventContentTextView;
    }

    public void setChannel_name(TextView channel_name) {
        this.channel_name = channel_name;
    }

    public void setCurrent_channel_image(ImageView current_channel_image) {
        this.current_channel_image = current_channel_image;
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            // This is absolute coordinate on screen not taking scroll into account.
            int x = (int) e.getX();
            int y = (int) e.getY();

            // Adding scroll to clicked coordinate
            int scrollX = getScrollX() + x;
            int scrollY = getScrollY() + y;

            int channelPosition = getChannelPosition(scrollY);
            if (channelPosition != -1 && mClickListener != null) {
                if (calculateResetButtonHitArea().contains(scrollX,scrollY)) {
                    // Reset button clicked
                    mClickListener.onResetButtonClicked();
                } else if (calculateChannelsHitArea().contains(x, y)) {
                    // Channel area is clicked
                    mClickListener.onChannelClicked(channelPosition, epgData.getChannel(channelPosition));
                } else if (calculateProgramsHitArea().contains(x, y)) {
                    // Event area is clicked
                    int programPosition = getProgramPosition(channelPosition, getTimeFrom(getScrollX() + x - calculateProgramsHitArea().left));
                    if (programPosition != 0) {
                        mClickListener.onEventClicked(channelPosition, programPosition, epgData.getEvent(channelPosition, programPosition));
                    }
                }
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            int dx = (int) distanceX;
            int dy = (int) distanceY;
            int x = getScrollX();
            int y = getScrollY();


            // Avoid over scrolling
            if (x + dx < 0) {
                dx = 0 - x;
            }
            if (y + dy < 0) {
                dy = 0 - y;
            }
            if (x + dx > mMaxHorizontalScroll) {
                dx = mMaxHorizontalScroll - x;
            }
            if (y + dy > mMaxVerticalScroll) {
                dy = mMaxVerticalScroll - y;
            }

            scrollBy(dx, dy);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float vX, float vY) {

            mScroller.fling(getScrollX(), getScrollY(), -(int) vX,
                    -(int) vY, 0, mMaxHorizontalScroll, 0, mMaxVerticalScroll);

            redraw();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
                return true;
            }
            return true;
        }
    }
}
