package com.newlegacyxc.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.newlegacyxc.R;
import com.newlegacyxc.adapter.CategoryListMultiAdapter;
import com.newlegacyxc.adapter.MainListAdapter;
import com.newlegacyxc.apps.MyApp;
import com.newlegacyxc.models.CategoryModel;
import com.newlegacyxc.models.EPGChannel;
import com.newlegacyxc.models.FullModel;

import java.util.ArrayList;
import java.util.List;

public class SelectChannelDialog extends DialogFragment{
	private String TAG=this.getClass().getSimpleName();
	private List<CategoryModel> categories=new ArrayList<>();

	public SelectChannelDialog() { }
	private SelectChannel selectChannelListener;
	private ListView list_category, list_channel;
	MainListAdapter channelAdapter;
	CategoryListMultiAdapter categoryListAdapter;
	List<FullModel> full_datas;
	private int category_pos=0, channel_pos=0;
	List<EPGChannel> channels;

	public static SelectChannelDialog newInstance(int i, int category_pos, int channel_pos) {
		SelectChannelDialog frag = new SelectChannelDialog();
		Bundle args = new Bundle();
		args.putInt("selected", i);
		args.putInt("category_pos",category_pos);
		args.putInt("channel_pos",channel_pos);
		frag.setArguments(args);
		return frag;
	}

	public void setSelectChannelListener(SelectChannel selectChannelListener) {
		this.selectChannelListener = selectChannelListener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
	}

	public interface SelectChannel{

		void onSelected(int category_pos, int channel_pos, EPGChannel EPGChannel);

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view= inflater.inflate(R.layout.fragment_select_channel, container);
		int selected = getArguments().getInt("selected", -1);
		channel_pos=getArguments().getInt("channel_pos", -1);
		category_pos=getArguments().getInt("category_pos", -1);
		full_datas = MyApp.fullModels_filter;
		list_category=view.findViewById(R.id.list_category);
		list_channel=view.findViewById(R.id.list_channel);
		channels = full_datas.get(category_pos).getChannels();
		Log.e(TAG,""+channels.size());
		channelAdapter = new MainListAdapter(getContext(),channels);
		list_channel.setAdapter(channelAdapter);
		list_channel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				channelAdapter.selectItem(position);
				Log.e(TAG,"channel position set "+position);
				channel_pos=position;
				selectChannelListener.onSelected(category_pos,channel_pos,full_datas.get(category_pos).getChannels().get(channel_pos));
				dismiss();
			}
		});
		list_channel.setSelection(channel_pos);

		categories = new ArrayList<>();
		categories = MyApp.live_categories_filter;
		categoryListAdapter = new CategoryListMultiAdapter(getContext(), categories);
		list_category.setAdapter(categoryListAdapter);
		categoryListAdapter.selectItem(category_pos);
		list_category.setSelection(category_pos);
		list_category.requestFocus();
		list_category.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				categoryListAdapter.selectItem(position);
				Log.e(TAG,"category position set "+position);
				category_pos=position;
				channels = full_datas.get(category_pos).getChannels();
				channelAdapter.setDatas(channels);
			}
		});
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Get field from view
		// Fetch arguments from bundle and set title
		// Show soft keyboard automatically and request focus to field
//		getDialog().getWindow().setSoftInputMode(
//		    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}
}