package com.mcs.FaceGestureApp.ui.face.facer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.face.facer.FaceCenterCircleView.FaceCenterCrop;
import com.mcs.FaceGestureApp.ui.face.facer.Utils.Imageutils;
import com.mcs.FaceGestureApp.ui.face.facer.Utils.ProgressBarUtil.ProgressBarData;
import com.mcs.FaceGestureApp.ui.face.facer.Utils.ProgressBarUtil.ProgressUtils;


//import com.droidmentor.mlkitfacedetection.Utils.Imageutils;
//import com.droidmentor.mlkitfacedetection.Utils.Imageutils.ImageAttachmentListener;
//import com.droidmentor.mlkitfacedetection.Utils.ProgressBarUtil.ProgressBarData;
//import com.droidmentor.mlkitfacedetection.Utils.ProgressBarUtil.ProgressUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import de.hdodenhof.circleimageview.CircleImageView;



import pl.droidsonroids.gif.GifImageView;

import static com.mcs.FaceGestureApp.ui.face.facer.Utils.Imageutils.GALEERY_REQUEST_CODE;
import static com.mcs.FaceGestureApp.ui.face.facer.Utils.Imageutils.SCANNER_REQUEST_CODE;


//import static com.droidmentor.mlkitfacedetection.Utils.Imageutils.GALEERY_REQUEST_CODE;
//import static com.droidmentor.mlkitfacedetection.Utils.Imageutils.SCANNER_REQUEST_CODE;

public class FaceEngineActivity extends AppCompatActivity {

    String TAG = "ProfileActivity";



    Imageutils imageutils;
    Imageutils.ImageAttachmentListener imageAttachmentListener;

    FaceCenterCrop faceCenterCrop;
    FaceCenterCrop.FaceCenterCropListener faceCenterCropListener;


    @BindView(R.id.ivProfile)
    CircleImageView ivProfile;
    //ImageView ivProfile;

    @BindView(R.id.gifImageView3)
    GifImageView scanimage;

    @BindView(R.id.btnscan)
    Button btnscan;

    @BindView(R.id.waitfoter)
    TextView waitfoter;

    ProgressUtils progressUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilefr);

        ButterKnife.bind(this);
        ivProfile.setVisibility(View.INVISIBLE);
        scanimage.setVisibility(View.INVISIBLE);
        //scanimage.setFreezesAnimation(false);

       // btntxt.findViewById(R.id.textView6);

        imageutils = new Imageutils(this);
        imageutils.setImageAttachment_callBack(getImageAttachmentCallback());

        progressUtils=new ProgressUtils(this);

        faceCenterCrop = new FaceCenterCrop(this, 100, 100, 1);

    }

    private Imageutils.ImageAttachmentListener getImageAttachmentCallback() {
        if (imageAttachmentListener == null)
            imageAttachmentListener = (from, filename, file, uri) -> {
                Log.d(TAG, "getImageAttachmentCallback: " + from);

                if (from == SCANNER_REQUEST_CODE) {
                  ivProfile.setImageBitmap(file);
                    //btntxt.setText("completed"+);
                    ivProfile.setVisibility(View.VISIBLE);
                    scanimage.setVisibility(View.VISIBLE);
                    btnscan.setVisibility(View.INVISIBLE);
                    waitfoter.setVisibility(View.VISIBLE);

                    ///////////////////////////
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //go to next activity


                           // startActivity(new Intent(FaceEngineActivity.this, ResultActivity.class));
                           // finish();

                        }
                    }, 5000); // for 3 second

                }
                else if(from == GALEERY_REQUEST_CODE)
                {
                    Log.d("Time log", "IA callback triggered");

                    ProgressBarData progressBarData= new ProgressBarData.ProgressBarBuilder()
                            .setCancelable(true)
                            .setProgressMessage("Processing")
                            .setProgressMessageColor(Color.parseColor("#4A4A4A"))
                            .setBackgroundViewColor(Color.parseColor("#FFFFFF"))
                            .setProgressbarTintColor(Color.parseColor("#FAC42A")).build();

                   // ivProfile.setImageBitmap(file);

                    progressUtils.showDialog(progressBarData);

                    faceCenterCrop.detectFace(file, getFaceCropResult());
                }
            };

        return imageAttachmentListener;
    }

    private FaceCenterCrop.FaceCenterCropListener getFaceCropResult() {
        if (faceCenterCropListener == null)
            faceCenterCropListener = new FaceCenterCrop.FaceCenterCropListener() {
                @Override
                public void onTransform(Bitmap updatedBitmap) {
                    Log.d("Time log", "Output is set");
                    ivProfile.setImageBitmap(updatedBitmap);
                    Toast.makeText(FaceEngineActivity.this, "We detected a face", Toast.LENGTH_SHORT).show();
                    progressUtils.dismissDialog();
                    /////////////////////////////////////////////////////
                    ivProfile.setVisibility(View.VISIBLE);
                    scanimage.setVisibility(View.VISIBLE);
                    btnscan.setVisibility(View.INVISIBLE);
                    waitfoter.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //go to next activity


                           // startActivity(new Intent(FaceEngineActivity.this, ResultActivity.class));
                           // finish();

                        }
                    }, 5000); // for 3 second

                }

                @Override
                public void onFailure() {
                    Toast.makeText(FaceEngineActivity.this, "No face was detected", Toast.LENGTH_SHORT).show();
                    progressUtils.dismissDialog();

                }
            };

        return faceCenterCropListener;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        try {
            super.onActivityResult(requestCode, resultCode, data);
            imageutils.onActivityResult(requestCode, resultCode, data);

            if (requestCode == SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: " + SCANNER_REQUEST_CODE);
            } else if (requestCode == GALEERY_REQUEST_CODE && resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: " + GALEERY_REQUEST_CODE);

            }
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imageutils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick(R.id.ivProfile)
    public void onViewClicked() {
        imageutils.imagepicker(1);
    }

    @OnClick(R.id.btnscan)
    public void onbtnViewClicked() {

        imageutils.imagepicker(1);
    }


}
