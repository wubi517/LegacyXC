package com.newlegacyxc.activity.home;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
 
public class SliderViewModel extends ViewModel {
 
  private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
 
  private LiveData<Integer> mPagerIndex =
      Transformations.map(mIndex, new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer input) {
          return input - 1;
        }
      });
 
  public void setIndex(int index) {
    mIndex.setValue(index);
  }
 
  public LiveData<Integer> getText() {
    return mPagerIndex;
  }
}