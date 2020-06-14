package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.newlegacyxc.R;

public class ConnectionDlg extends Dialog {

    public ConnectionDlg(@NonNull final Context context, final DialogConnectionListener listener, String body, String btn_yes, String btn_no) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_connection);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ((TextView)findViewById(R.id.body)).setText(body);
        if (btn_yes==null) btn_yes = getContext().getString(R.string.retry);
        if (btn_no==null) btn_no = getContext().getString(R.string.more_help);
        ((Button)findViewById(R.id.dlg_con_retry_btn)).setText(btn_yes);
        ((Button)findViewById(R.id.dlg_con_help_btn)).setText(btn_no);
        findViewById(R.id.dlg_con_retry_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnYesClick(ConnectionDlg.this);
            }
        });
        findViewById(R.id.dlg_con_help_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnNoClick(ConnectionDlg.this);
            }
        });
    }

    public interface DialogConnectionListener {
        public void OnYesClick(Dialog dialog);
        public void OnNoClick(Dialog dialog);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            dismiss();
        }
        return super.dispatchKeyEvent(event);
    }
}
