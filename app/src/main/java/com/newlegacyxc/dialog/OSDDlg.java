package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newlegacyxc.R;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;

public class OSDDlg extends Dialog implements View.OnClickListener{
    private ImageButton btn_left,btn_right;
    private TextView txt_current;
    private LinearLayout ly_click;
    private long episode_num=0;
    private EpisodeDlgListener listener;
    Context context;
    public OSDDlg(@NonNull final Context context, final long delay_time, final EpisodeDlgListener listener){
        super(context);
        this.context = context;
        this.listener = listener;
        this.episode_num = delay_time;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_osd);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        episode_num = (int) MyApp.instance.getPreference().get(Constants.OSD_TIME);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_left .setOnClickListener(this);
        btn_right.setOnClickListener(this);

        txt_current = findViewById(R.id.txt_current);
        txt_current.setText(String.valueOf(episode_num));

        ly_click = findViewById(R.id.ly_click);
        ly_click.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_left:
                episode_num = episode_num - 50;
                txt_current.setText(String.valueOf(episode_num));
                listener.OnYesClick(OSDDlg.this,episode_num);
                break;
            case R.id.btn_right:
                episode_num = episode_num + 50;
                txt_current.setText(String.valueOf(episode_num));
                listener.OnYesClick(OSDDlg.this,episode_num);
                break;
            case R.id.ly_click:
                listener.OnYesClick(OSDDlg.this,episode_num);
                break;
        }
    }

    public interface EpisodeDlgListener{
        public void OnYesClick(Dialog dialog, long episode_num);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    dismiss();
                    break;
//                case KeyEvent.KEYCODE_DPAD_LEFT:
//                    episode_num=episode_num-50;
//
//                    txt_current.setText(String.valueOf(episode_num));
//                    listener.OnYesClick(OSDDlg.this,episode_num);
//                    break;
//                case KeyEvent.KEYCODE_DPAD_RIGHT:
//                    episode_num = episode_num+50;
//                    txt_current.setText(String.valueOf(episode_num));
//                    listener.OnYesClick(OSDDlg.this,episode_num);
//                    break;
//                case KeyEvent.KEYCODE_DPAD_CENTER:
//                    listener.OnYesClick(OSDDlg.this,episode_num);
//                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}

