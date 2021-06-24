package com.mcs.FaceGestureApp.ui.voice;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.security.BearerTokenAuthenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;
import com.ibm.watson.text_to_speech.v1.util.WaveUtils;
import com.mcs.FaceGestureApp.Api.API;
import com.mcs.FaceGestureApp.Api.APIClient;
import com.mcs.FaceGestureApp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class VoiceFragment extends Fragment {

    private static final String TAG = "TextFragment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int BUFFER_SIZE = 1024;

    private EditText textEditText;
    private Button textToSpeech;
    private API api=getAPIKey();
    private APIClient apiClient;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text, container, false);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textEditText = view.findViewById(R.id.et_text_to_speach);
        textToSpeech = view.findViewById(R.id.activity_main_text);
        progressBar = view.findViewById(R.id.pb_tts);

         textToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String text = textEditText.getText().toString();
                if(TextUtils.isEmpty(text)){
                    textEditText.setError("Enter Some Text!");
                    textEditText.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                else{

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (text.length() > 0) {
                                String voice = "en-GB_JamesV3Voice";
                                try {
                                    createSoundFile(text, voice);
                                    playSoundFile(text + voice);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    thread.start();
                }
            }
        });

    }

    public void createSoundFile(String text, String voice) throws IOException {

        new APIClient(api.getAPI_Key(), api.getUrl());

        HttpConfigOptions configOptions = new HttpConfigOptions.Builder()
                //.disableSslVerification(true)
                .loggingLevel(HttpConfigOptions.LoggingLevel.BODY)
                .build();

        TextToSpeech textToSpeech = new TextToSpeech(APIClient.getClient());
        textToSpeech.configureClient(configOptions);
        textToSpeech.setServiceUrl(api.getUrl());

        SynthesizeOptions synthesizeOptions = new SynthesizeOptions
                .Builder()
                .text(text)
                .accept("audio/mp3")
                .voice(voice)
                .build();

        InputStream inputStream = textToSpeech
                .synthesize(synthesizeOptions)
                .execute()
                .getResult();

        InputStream in = WaveUtils.reWriteWaveHeader(inputStream);

        String fileName = text + voice;
        FileOutputStream fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE);

        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fos.close();

        in.close();
        inputStream.close();
    }

    public void playSoundFile(String fileName) throws IOException {
        File file = new File(requireContext().getFilesDir(), fileName);
        Uri fileUri = Uri.parse(file.getPath());
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build()
            );
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mediaPlayer.setDataSource(requireContext(), fileUri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mediaPlayer.prepareAsync();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "onCompletion: " );
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    private API getAPIKey() {
        DocumentReference docRef = db.collection("API").document("TTS2");
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Log.d(TAG, "onSuccess: API KEY NOT FOUND");
                return;
            }
            Log.e(TAG, "TTS API initialized :"+documentSnapshot.getId());
            api = documentSnapshot.toObject(API.class);
            Log.e(TAG, "getAPIKey: result"+api.getAPI_Key() );
        }).addOnFailureListener(e -> {
            Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show();
        });
        return api;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
