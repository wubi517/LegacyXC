package com.newlegacyxc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.newlegacyxc.R;
import com.newlegacyxc.adapter.MainListAdapter;
import com.newlegacyxc.apps.Constants;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.FullModel;

import java.util.ArrayList;
import java.util.List;

public class SearchDlg extends Dialog implements AdapterView.OnItemClickListener {
    Context context;
    DialogSearchListener listener;
    private List<EPGChannel> EPGChannels, search_models;
    private List<FullModel> full_datas;
    private EditText search_txt;
    private ListView search_list;
    private MainListAdapter adapter;
    private int channel_pos,priv_pos = -1;
    public SearchDlg(@NonNull Context context,final DialogSearchListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_search);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        initChannelData();
        search_txt = (EditText) findViewById(R.id.search_txt);
        search_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchChannels(search_txt.getText().toString());
            }
        });
        search_list = (ListView) findViewById(R.id.search_list);
        adapter = new MainListAdapter(context,search_models);
        search_list.setOnItemClickListener(this);

    }

    private void initChannelData() {
        channel_pos = (int) MyApp.instance.getPreference().get(Constants.getCategory_POS());
        full_datas = MyApp.fullModels_filter;
        EPGChannels = full_datas.get(channel_pos).getChannels();
        search_models = new ArrayList<>();
    }

    private void searchChannels(String key) {
        if (key == null || key.isEmpty()) {
            initChannelData();
        } else {
            search_models = new ArrayList<>();
            for (int i = 0; i < EPGChannels.size(); i++) {
                EPGChannel chm = EPGChannels.get(i);
                if (chm.getName().toLowerCase().contains(key.toLowerCase())) {
                    search_models.add(chm);
                }
            }
        }
        adapter = new MainListAdapter(context, search_models);
        search_list.setAdapter(adapter);
        priv_pos = -1;
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (priv_pos == position)
            listener.OnSearchClick(SearchDlg.this, search_models.get(position));
        else {
            priv_pos = position;
            listener.OnSearchClick(SearchDlg.this, search_models.get(position));
        }
    }

    public interface DialogSearchListener {
        public void OnSearchClick(Dialog dialog, EPGChannel sel_Channel);
    }
}
