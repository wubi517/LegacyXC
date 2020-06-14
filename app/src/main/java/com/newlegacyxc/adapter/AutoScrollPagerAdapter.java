package com.newlegacyxc.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.newlegacyxc.activity.home.SlideFragment;
import com.newlegacyxc.models.AppInfoModel;

public class AutoScrollPagerAdapter extends FragmentStatePagerAdapter {

  private AppInfoModel appInfoModel;
  private ViewPager viewPager;
  public AutoScrollPagerAdapter(FragmentManager fm, ViewPager viewPager, AppInfoModel appInfoModel) {
    super(fm);
    this.viewPager = viewPager;
    this.appInfoModel = appInfoModel;
    try {
      Log.e("AutoScrollPagerAdapter","set model success "+appInfoModel.getAppInfo().getSlider().get(0).getHeader());
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public void setAppInfoModel(AppInfoModel appInfoModel){
    this.appInfoModel = appInfoModel;
    notifyDataSetChanged();
    try {
      Log.e("AutoScrollPagerAdapter","set model success "+appInfoModel.getAppInfo().getSlider().get(0).getHeader());
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public int getItemPosition(@NonNull Object object) {
//    if (object instanceof SlideFragment){
//      ((SlideFragment)object).update(appInfoModel.getAppInfo().getSlider().get(viewPager.getCurrentItem()));
//    }
    Log.e("AutoScrollPagerAdapter","getItemPosition");
//    return super.getItemPosition(object);
    return POSITION_NONE;
  }

  @Override
  public Fragment getItem(int position) {

    // Return a SlideFragment (defined as a static inner class below).
    Log.e("AutoScrollPagerAdapter","getItem");
    return SlideFragment.newInstance(position+1, appInfoModel.getAppInfo().getSlider().get(position));
  }

  @Override
  public int getCount() {
    // Show 3 total pages.
    return appInfoModel.getAppInfo().getSlider().size();
  }
}