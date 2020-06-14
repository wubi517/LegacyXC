package com.newlegacyxc.apps;

/**
 * Created by RST on 1/17/2018.
 */
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class PlayGifView extends View{

    private static final int DEFAULT_MOVIEW_DURATION = 1000;

    private int mMovieResourceId;
    private Movie mMovie;

    private long mMovieStart = 0;
    private int mCurrentAnimationTime = 0;

    @SuppressLint("NewApi")
    public PlayGifView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * Starting from HONEYCOMB have to turn off HardWare acceleration to draw
         * Movie on Canvas.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void setImageResource(int mvId){
        this.mMovieResourceId = mvId;
        mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mMovie != null){
            if(MyApp.SCREEN_WIDTH<MyApp.SCREEN_HEIGHT){
                setMeasuredDimension(MyApp.SCREEN_HEIGHT, MyApp.SCREEN_WIDTH);
            }else {
                setMeasuredDimension(MyApp.SCREEN_WIDTH, MyApp.SCREEN_HEIGHT);

            }
        }else{
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null){
            updateAnimtionTime();
            if(MyApp.SCREEN_WIDTH<MyApp.SCREEN_HEIGHT){
                canvas.scale(MyApp.SCREEN_HEIGHT/ (float)mMovie.width(),MyApp.SCREEN_WIDTH/(float)mMovie.height());
            }else {
                canvas.scale(MyApp.SCREEN_WIDTH / (float)mMovie.width(),MyApp.SCREEN_HEIGHT/(float)mMovie.height());
            }            drawGif(canvas);
            invalidate();
        }else{
            if(MyApp.SCREEN_WIDTH<MyApp.SCREEN_HEIGHT){
                canvas.scale(MyApp.SCREEN_HEIGHT/ (float)mMovie.width(),MyApp.SCREEN_WIDTH/(float)mMovie.height());
            }else {
                canvas.scale(MyApp.SCREEN_WIDTH / (float)mMovie.width(),MyApp.SCREEN_HEIGHT/(float)mMovie.height());
            }           drawGif(canvas);
        }
    }

    private void updateAnimtionTime() {
        long now = android.os.SystemClock.uptimeMillis();

        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int dur = mMovie.duration();
        if (dur == 0) {
            dur = DEFAULT_MOVIEW_DURATION;
        }
        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    private void drawGif(Canvas canvas) {
        mMovie.setTime(mCurrentAnimationTime);
        mMovie.draw(canvas, 0, 0);
        if(MyApp.SCREEN_WIDTH<MyApp.SCREEN_HEIGHT){
            canvas.scale(MyApp.SCREEN_HEIGHT/ (float)mMovie.width(),MyApp.SCREEN_WIDTH/(float)mMovie.height());
        }else {
            canvas.scale(MyApp.SCREEN_WIDTH / (float)mMovie.width(),MyApp.SCREEN_HEIGHT/(float)mMovie.height());
        }
        canvas.restore();
    }
}