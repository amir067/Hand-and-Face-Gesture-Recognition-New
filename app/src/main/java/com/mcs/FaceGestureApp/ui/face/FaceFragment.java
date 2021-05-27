package com.mcs.FaceGestureApp.ui.face;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.face.facer.FaceDetectionUtil.ScannerActivity;
import com.mcs.FaceGestureApp.ui.face.facer.FaceEngineActivity;

import static com.mcs.FaceGestureApp.ui.face.facer.Utils.Imageutils.SCANNER_REQUEST_CODE;

public class FaceFragment extends Fragment {

    private static final String TAG = "FaceFragment";


    Button faceButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_face, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        faceButton = view.findViewById(R.id.button2);

        faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startActivity(new Intent(requireContext(), FaceEngineActivity.class));
                launchScanner();
            }
        });

    }


    public void launchScanner() {
        Intent scannerIntent = new Intent(requireContext(), ScannerActivity.class);
        startActivityForResult( scannerIntent, SCANNER_REQUEST_CODE);

    }
}
