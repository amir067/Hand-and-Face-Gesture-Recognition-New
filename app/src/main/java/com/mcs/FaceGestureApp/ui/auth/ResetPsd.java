package com.mcs.FaceGestureApp.ui.auth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.zeptosystem.R;
//import com.example.zeptosystem.ui.activities.SplashActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.utils.MyUtils;
import com.mcs.FaceGestureApp.utils.Tools;

import es.dmoral.toasty.Toasty;

public class ResetPsd extends AppCompatActivity {
    private EditText inputEmail1;
    private Button btnReset1, btnBack1;
    private FirebaseAuth auth1;
    private ProgressBar progressBar1;
    EditText et_email1;
    private AlertDialog loading_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_rstpsd);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Tools.setSystemBarTransparent(this);
        Tools.setSystemBarLight(this);
        inputEmail1 = (EditText) findViewById(R.id.editTextRsdpsd);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar2);
        loading_dialog = MyUtils.getLoadingDialog(this);
        btnReset1 = (Button) findViewById(R.id.editTextRsdpsdbtn);

        auth1 = FirebaseAuth.getInstance();

        btnReset1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail1.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toasty.info(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                }
                else if(!MyUtils.isValidEmail(email)){

                    Toasty.error(getApplication(), "Enter a valid email id", Toast.LENGTH_SHORT).show();

                } else if(!isOnline()){

                    Toasty.info(getApplication(), "Internet not Available!", Toast.LENGTH_SHORT).show();

                }else
                {
                    loading_dialog.show();
                    auth1.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loading_dialog.dismiss();
                                        Toasty.success(ResetPsd.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                        //Intent loginIntent = new Intent(ResetPsd.this, LoginActivity.class);
                                        //startActivity(loginIntent);
                                        finish();
                                    } else {
                                        loading_dialog.dismiss();
                                        Toasty.error(ResetPsd.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }

            }
        });

    }


    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }




}
