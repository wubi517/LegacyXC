package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.newlegacyxc.R;


public class PinMultiScreenDlg extends Dialog implements View.OnClickListener{
    private Context context;
    private DlgPinListener listener;
    private EditText txt_pin;
    private CheckBox checkBox;
    public PinMultiScreenDlg(@NonNull Context context, final DlgPinListener listener, boolean is_remember) {
        super(context);
        this.context = context;
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pin_multi_screen);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btn_ok = (Button) findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        txt_pin = (EditText)findViewById(R.id.txt_pin);
        txt_pin.requestFocus();
        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnClickListener(this);
        checkBox.setChecked(is_remember);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok:
                String pin_code = txt_pin.getText().toString();
                listener.OnYesClick(PinMultiScreenDlg.this, pin_code, checkBox.isChecked());
                dismiss();
                break;
            case R.id.btn_cancel:
                listener.OnCancelClick(PinMultiScreenDlg.this,"");
                dismiss();
                break;
            case R.id.checkbox:
                break;
        }
    }

    public interface DlgPinListener {
        public void OnYesClick(Dialog dialog, String pin_code, boolean is_remember);
        public void OnCancelClick(Dialog dialog, String pin_code);
    }
}
