package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.newlegacyxc.R;
import com.newlegacyxc.adapter.HideCategoryListAdapter;

public class HideCategoryDlg extends Dialog implements AdapterView.OnItemClickListener,View.OnClickListener {
    Context context;
    DialogSearchListener listener;
    private HideCategoryListAdapter adapter;
    public HideCategoryDlg(@NonNull Context context, String[]  data, final boolean[] checks, final DialogSearchListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_hide_category);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ListView category_list=findViewById(R.id.category_list);
        category_list.requestFocus();
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_select_all).setOnClickListener(this);
        adapter = new HideCategoryListAdapter(context, data, checks, new HideCategoryListAdapter.OnClick() {
            @Override
            public boolean onClick(int position, boolean checked) {
                listener.OnItemClick(HideCategoryDlg.this, position, checked);
                return false;
            }
        });
        category_list.setAdapter(adapter);
        category_list.setOnItemClickListener(this);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (getCurrentFocus().getId()==R.id.category_list) findViewById(R.id.btn_ok).requestFocus();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (getCurrentFocus().getId()==R.id.category_list) findViewById(R.id.btn_cancel).requestFocus();
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok:
                listener.OnOkClick(this);
                dismiss();
                break;
            case R.id.btn_cancel:
                listener.OnCancelClick(this);
                dismiss();
                break;
            case R.id.btn_select_all:
                listener.OnSelectAllClick(this);
                dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        adapter.toggleChecked(i);
        Log.e("selected_id",i+"");
    }

    public interface DialogSearchListener {
        void OnItemClick(Dialog dialog, int position, boolean checked);
        void OnOkClick(Dialog dialog);
        void OnCancelClick(Dialog dialog);
        void OnSelectAllClick(Dialog dialog);
    }
}
