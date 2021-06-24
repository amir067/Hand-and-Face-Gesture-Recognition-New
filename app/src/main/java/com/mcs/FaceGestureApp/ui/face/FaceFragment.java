package com.mcs.FaceGestureApp.ui.face;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.face.facer.FaceDetectionUtil.ScannerActivity;
import com.mcs.FaceGestureApp.ui.face.facer.FaceEngineActivity;
import com.mcs.FaceGestureApp.ui.hand.MlKit.Camera2Activity;
import com.mcs.FaceGestureApp.ui.main.HomeActivity;
import com.mcs.FaceGestureApp.ui.main.MainActivity;
import com.mcs.FaceGestureApp.ui.voice.STT;

import static com.mcs.FaceGestureApp.ui.face.facer.Utils.Imageutils.SCANNER_REQUEST_CODE;

public class FaceFragment extends Fragment {

    private static final String TAG = "FaceFragment";


    CardView faceGestureCardView;
    CardView handGestureCardView;
    CardView TTSCardView;
    CardView STTCardView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_face, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        faceGestureCardView = view.findViewById(R.id.faceGestureCardView);
        handGestureCardView = view.findViewById(R.id.handGestureCardView);
        TTSCardView = view.findViewById(R.id.TTSCardView);
        STTCardView = view.findViewById(R.id.STTCardView);


        faceGestureCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startActivity(new Intent(requireContext(), FaceEngineActivity.class));
                launchFAceScanner();//faceGesture
            }
        });

        handGestureCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startActivity(new Intent(requireContext(), FaceEngineActivity.class));
                launchHandGesture();
            }
        });

        TTSCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startActivity(new Intent(requireContext(), FaceEngineActivity.class));
                ((HomeActivity)getActivity()).viewPager.setCurrentItem(1,true);
            }
        });
        STTCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // startActivity(new Intent(requireContext(), STT.class));
                ((HomeActivity)getActivity()).viewPager.setCurrentItem(2,true);
            }
        });

    }


    public void launchFAceScanner() {
        Intent scannerIntent = new Intent(requireContext(), ScannerActivity.class);
        startActivityForResult( scannerIntent, SCANNER_REQUEST_CODE);
    }

    public void launchHandGesture() {
        Intent scannerIntent = new Intent(requireContext(), Camera2Activity.class);
        startActivity( scannerIntent);
    }
}
