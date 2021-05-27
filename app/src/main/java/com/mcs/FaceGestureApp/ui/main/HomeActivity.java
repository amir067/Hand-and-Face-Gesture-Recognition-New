package com.mcs.FaceGestureApp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import com.ibm.watson.text_to_speech.v1.util.WaveUtils;
import com.mcs.FaceGestureApp.Api.API;
import com.mcs.FaceGestureApp.Api.APIClient;
import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.model.UserModel;

import com.mcs.FaceGestureApp.ui.auth.LoginActivity;
import com.mcs.FaceGestureApp.utils.PreferenceHelperDemo;
import com.mcs.FaceGestureApp.utils.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import es.dmoral.toasty.Toasty;

public class HomeActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener , NavigationView.OnNavigationItemSelectedListener{

    private int backpress = 0;
    private PreferenceHelperDemo preferenceHelperDemo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "HomeActivity";
    private CardView card1,card2,card3,card4,card5,card6,card7,card8;
    private TextView user_name;
    private String usr_name,imageURL;
    private ImageView user_dp;
    private Button logOutBtn;

    private static final int BUFFER_SIZE = 1024;

    private EditText textEditText;
    private Button textToSpeech;
   // private API api=getAPIKey();
    private APIClient apiClient;
    private ProgressBar progressBar;


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        preferenceHelperDemo = new PreferenceHelperDemo(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Tools.setSystemBarTransparent(this);
        Tools.setSystemBarLight(this);

        AlphaAnimation dp_alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        dp_alphaAnimation.setDuration(2500);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_icon_navigation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hand and face Gesture Recognition");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Tools.setSystemBarColor(this,R.color.GradientB1);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        //Creating our pager adapter
        Pager adapter = new Pager(getSupportFragmentManager(), this);
        //Adding adapter to pager
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));




        //profile();
    }
    @Override
    public void onBackPressed() {
        backpress = (backpress + 1);
        Toast.makeText(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();
        if (backpress > 1) {
            this.finish();
        }
    }




    @Override
    public void onStart() {
        super.onStart();

    }


    private void profile() {
        DocumentReference docRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Log.d(TAG, "onSuccess: LIST EMPTY");
                return;
            }
            UserModel types = documentSnapshot.toObject(UserModel.class);
            String gender = types.getGender();
            usr_name= types.getUsername();
            user_name.setText(usr_name);
            imageURL = types.getImageURL();
            Glide.with(getApplicationContext())
                    .load(imageURL)
                    .placeholder(R.drawable.picture_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .error(R.drawable.app_icon_ic_doctor_with_nobg)
                    .into(user_dp);
            preferenceHelperDemo.setKey("gender", gender);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            Toast.makeText(HomeActivity.this, "error", Toast.LENGTH_SHORT).show();
        });
    }



    private void intent() {
        //startActivity(new Intent(this,StatementActivity.class));
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.profile) {
           // Intent profile = new Intent(HomeActivity.this,ProfileActivity.class);
            //startActivity(profile);
            //startActivity(new Intent(this, PaymentActivity.class));
            // Handle the camera action
        }
        else if (id == R.id.sharelink) {
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND,
                    Uri.parse("http://www.google.com")),"Invite Friend .."));// share url is on your own
            //Intent profile = new Intent(HomeActivity.this,ProfileActivity.class);
            //startActivity(profile);
            //startActivity(new Intent(this, PaymentActivity.class));
            // Handle the camera action
        }
        else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            //FirebaseAuth.getInstance().signOut();
            Toasty.success(getApplicationContext(), "Logout Success", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            ///Intent about = new Intent(Home.this,About.class);
            //startActivity(about);
        } else if (id == R.id.about) {
            //FirebaseAuth.getInstance().signOut();
            //startActivity(new Intent(this, AboutAppSimple.class));
            ///Intent about = new Intent(Home.this,About.class);
            //startActivity(about);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }
}
