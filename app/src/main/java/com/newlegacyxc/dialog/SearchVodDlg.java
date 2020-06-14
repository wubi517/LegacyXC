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
import com.newlegacyxc.adapter.VodListAdapter;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.MovieModel;

import java.util.ArrayList;
import java.util.List;

public class SearchVodDlg extends Dialog implements AdapterView.OnItemClickListener {
    Context context;
    DialogSearchListener listener;
    private List<MovieModel> movieModels, search_models;
    private EditText search_txt;
    private ListView search_list;
    private VodListAdapter adapter;
    private int channel_pos,priv_pos = -1;
    public SearchVodDlg(@NonNull Context context, final DialogSearchListener listener) {
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
        adapter = new VodListAdapter(context,search_models);
        search_list.setOnItemClickListener(this);

    }

    private void initChannelData() {
        movieModels = MyApp.movieModels0;
        search_models = new ArrayList<>();
    }

    private void searchChannels(String key) {
        if (key == null || key.isEmpty()) {
            initChannelData();
        } else {
            search_models = new ArrayList<>();
            for (int i = 0; i < movieModels.size(); i++) {
                MovieModel chm = movieModels.get(i);
                if (chm.getName().toLowerCase().contains(key.toLowerCase())) {
                    search_models.add(chm);
                }
            }
        }
        adapter = new VodListAdapter(context, search_models);
        search_list.setAdapter(adapter);
        priv_pos = -1;
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (priv_pos == position)
            listener.OnSearchClick(SearchVodDlg.this, search_models.get(position));
        else {
            priv_pos = position;
            listener.OnSearchClick(SearchVodDlg.this, search_models.get(position));
        }
    }

    public interface DialogSearchListener {
        public void OnSearchClick(Dialog dialog, MovieModel sel_Channel);
    }
}
