package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.newlegacyxc.R;
import com.newlegacyxc.adapter.SettingListAdapter;

import java.util.List;

public class SettingDlg extends Dialog implements AdapterView.OnItemClickListener{
    DialogSettingListner listner;
    List<String > datas;
    public SettingDlg(@NonNull Context context, List<String> datas, DialogSettingListner listener) {
        super(context);
        this.listner = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_settting);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.datas = datas;
        ListView listview = (ListView) findViewById(R.id.setting_list);
        SettingListAdapter adapter = new SettingListAdapter(context, datas);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        this.listner.OnItemClick(SettingDlg.this, position);
    }
    public interface DialogSettingListner {
        public void OnItemClick(Dialog dialog, int position);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            dismiss();
        }
        return super.dispatchKeyEvent(event);
    }
}
