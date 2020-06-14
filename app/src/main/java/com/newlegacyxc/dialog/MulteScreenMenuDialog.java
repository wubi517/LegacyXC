package com.newlegacyxc.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.newlegacyxc.R;


public class MulteScreenMenuDialog extends DialogFragment{

	public MulteScreenMenuDialog() { }
	private MultiScreenMenuListener multiScreenMenuListenerListener;
	private boolean is_playing;
	private boolean is_mute;
	public static MulteScreenMenuDialog newInstance(boolean is_playing, boolean is_mute) {
		MulteScreenMenuDialog frag = new MulteScreenMenuDialog();
		Bundle args = new Bundle();
		args.putBoolean("is_playing",is_playing);
		args.putBoolean("is_mute",is_mute);
		frag.setArguments(args);
		return frag;
	}

	public void setMultiScreenMenuListenerListener(MultiScreenMenuListener multiScreenMenuListenerListener) {
		this.multiScreenMenuListenerListener = multiScreenMenuListenerListener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
	}

	public interface MultiScreenMenuListener {

		void onPlus();

		void onFullScreen();

		void onPlayPause(boolean is_playing);

		void onSoundMute(boolean is_mute);

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View view= inflater.inflate(R.layout.fragment_multiscreen_menu, container);
		is_mute=getArguments().getBoolean("is_mute");
		is_playing=getArguments().getBoolean("is_playing");
		ImageView icon_full = view.findViewById(R.id.icon_full);
		ImageView icon_play = view.findViewById(R.id.icon_play);
		ImageView icon_plus = view.findViewById(R.id.icon_plus);
		ImageView icon_sound = view.findViewById(R.id.icon_sound);
		icon_sound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeButtons(3);
			}
		});
		icon_plus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeButtons(0);
			}
		});
		icon_play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeButtons(2);
			}
		});
		icon_full.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeButtons(1);
			}
		});
		if (is_playing) icon_play.setImageResource(R.drawable.icons_pause);
		else  icon_play.setImageResource(R.drawable.icons_play);
		if (!is_mute)  icon_sound.setImageResource(R.drawable.sound);
		else icon_sound.setImageResource(R.drawable.sound_mute);
		icon_plus.requestFocus();
		return view;
	}

	private void changeButtons(int i){
		switch (i){
			case 0:
				dismiss();
				multiScreenMenuListenerListener.onPlus();
				break;
			case 1:
				dismiss();
				multiScreenMenuListenerListener.onFullScreen();
				break;
			case 2:
				dismiss();
				multiScreenMenuListenerListener.onPlayPause(is_playing);
				break;
			case 3:
				dismiss();
				multiScreenMenuListenerListener.onSoundMute(is_mute);
				break;
		}
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