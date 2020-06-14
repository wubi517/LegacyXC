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
import com.newlegacyxc.adapter.PackageListAdapter;

import java.util.List;

public class PackageDlg extends Dialog implements AdapterView.OnItemClickListener{

    DialogPackageListener listener;
    List<String> datas;

    public PackageDlg(@NonNull Context context, List<String> datas, DialogPackageListener listener) {
        super(context);
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_package);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.datas = datas;
        ListView listview = (ListView) findViewById(R.id.package_list);
        PackageListAdapter adapter = new PackageListAdapter(context, datas);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.listener.OnItemClick(PackageDlg.this, position);
    }

    public interface DialogPackageListener {
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
