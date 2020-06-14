package com.newlegacyxc.activity.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.newlegacyxc.R;
import com.newlegacyxc.models.AppInfoModel;

public class SlideFragment extends Fragment {

    private AppInfoModel.SliderEntity sliderEntity;
    private static final String ARG_SECTION_NUMBER = "section_number";
//    @StringRes
//    private static final int[] PAGE_TITLES =
//            new int[] { R.string.step_1, R.string.step_2, R.string.step_3 };
//    @StringRes
//    private static final int[] PAGE_IMAGE =
//            new int[] {
//                    R.drawable.ad1, R.drawable.ad2, R.drawable.background1
//            };
    private SliderViewModel sliderViewModel;
    private TextView textView,tv_body;
    private ImageView imageView;
    public static SlideFragment newInstance(int index, AppInfoModel.SliderEntity appInfoEntity) {
        SlideFragment fragment = new SlideFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setSliderEntity(appInfoEntity);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void setSliderEntity(AppInfoModel.SliderEntity sliderEntity){
        this.sliderEntity = sliderEntity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sliderViewModel = ViewModelProviders.of(this).get(SliderViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        sliderViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slide, container, false);
        textView = root.findViewById(R.id.section_label);
        tv_body = root.findViewById(R.id.tv_body);
        imageView = root.findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        sliderViewModel.getText().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override public void onChanged(Integer index) {
                if (sliderEntity!=null){
                    if (sliderEntity.getHeader()!=null) textView.setText(sliderEntity.getHeader());
                    if (sliderEntity.getBody()!=null) tv_body.setText(sliderEntity.getBody());
                }
//                Log.e("SlideFragment", sliderEntity.getHeader()+" "+ sliderEntity.getBody());
//                Picasso.with(requireContext())
//                        .load(sliderEntity.getSlider().get(index).getImageurl())
//                        .memoryPolicy(MemoryPolicy.NO_CACHE)
//                        .networkPolicy(NetworkPolicy.NO_CACHE)
//                        .error(R.drawable.icon)
//                        .into(imageView);
            }
        });
        return root;
    }

    public void update(AppInfoModel.SliderEntity sliderEntity){
        textView.setText(sliderEntity.getHeader());
        tv_body.setText(sliderEntity.getBody());
        Log.e("SlideFragment", sliderEntity.getHeader()+" "+ sliderEntity.getBody());
    }
}
