package com.mcs.FaceGestureApp.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.face.FaceFragment;
import com.mcs.FaceGestureApp.ui.hand.HandFragment;
import com.mcs.FaceGestureApp.ui.hand.MlKit.Camera2BasicFragment;
import com.mcs.FaceGestureApp.ui.voice.VoiceFragment;
//import com.mcs.FaceGestureApp.ui.voice.VoiceFragment;

public class Pager extends FragmentStatePagerAdapter {

    //integer to count number of tabs
  //  int tabCount;
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2,R.string.tab_text_3};

    private final Context mContext;
    //Constructor to the class
    public Pager(FragmentManager fm,Context context) {
        super(fm);
        //Initializing tab count
        //this.tabCount= tabCount;
        mContext = context;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                FaceFragment faceFragment = new FaceFragment();
                return faceFragment;
            case 1:
                VoiceFragment textFragment = new VoiceFragment();
                return textFragment;
            case 2:
                //HandFragment handFragment = new HandFragment();
                return Camera2BasicFragment.newInstance();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }




    @Override
    public int getCount() {
        // Show 2 total pages.
        return TAB_TITLES.length;
    }


}
