package com.mcs.FaceGestureApp.ui.play;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.adapter.Adapter_walkthrough;
import com.mcs.FaceGestureApp.utils.Tools;

import me.relex.circleindicator.CircleIndicator;

public class WalkThrowActivity extends AppCompatActivity {

    public ViewPager viewpager;

    Adapter_walkthrough adapter_walkthrough;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_throw);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Tools.setSystemBarTransparent(this);
        Tools.setSystemBarLight(this);



        viewpager = findViewById(R.id.viewpager);
        CircleIndicator indicator = findViewById(R.id.indicator);
        adapter_walkthrough = new Adapter_walkthrough(getSupportFragmentManager());
        viewpager.setAdapter(adapter_walkthrough);
        indicator.setViewPager(viewpager);
        adapter_walkthrough.registerDataSetObserver(indicator.getDataSetObserver());
    }

    public  void brn_get_start(View v){
        SharedPreferences.Editor editor = getSharedPreferences("MySharedPref", MODE_PRIVATE).edit();
        editor.putString("user_type", "old");
        editor.apply();
        finish();

    }
}