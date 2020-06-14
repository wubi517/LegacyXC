package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.newlegacyxc.R;


public class VPNPinDlg extends Dialog implements View.OnClickListener{
    private Context context;
    private DlgPinListener listener;
    private Button btn_ok,btn_cancel;
    private EditText txt_pin;
    private String pin_code;
    public VPNPinDlg(@NonNull Context context, final DlgPinListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.vpn_pin);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_cancel = (Button)findViewById(R.id.btn_cancel);
        txt_pin = (EditText)findViewById(R.id.txt_pin);
        txt_pin.requestFocus();
        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok:
                pin_code = txt_pin.getText().toString();
                listener.OnYesClick(VPNPinDlg.this,pin_code);
                break;
            case R.id.btn_cancel:
                listener.OnCancelClick(VPNPinDlg.this,"");
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_BACK:
                dismiss();
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    public interface DlgPinListener {
        public void OnYesClick(Dialog dialog, String pin_code);
        public void OnCancelClick(Dialog dialog, String pin_code);
    }
}
