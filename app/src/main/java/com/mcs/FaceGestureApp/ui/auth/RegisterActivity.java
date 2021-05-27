package com.mcs.FaceGestureApp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.main.HomeActivity;
import com.mcs.FaceGestureApp.ui.main.MainActivity;
import com.mcs.FaceGestureApp.utils.MyUtils;
import com.mcs.FaceGestureApp.utils.Tools;

import static com.mcs.FaceGestureApp.utils.MyUtils.isValidEmail;


//import static my.personal.psychiatrist.ui.auth.LoginActivity.isValidEmail;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth auth;
    private  DatabaseReference reference;

    @BindView(R.id.r_username)
    EditText rUsername;

    @BindView(R.id.r_email)
    EditText rEmail;
    @BindView(R.id.r_password)
    EditText rPassword;

    @BindView(R.id.iv_back)
    ImageView backIcon;




    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference blogsRef = db.collection("users");

    private AlertDialog loading_dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Tools.setSystemBarTransparent(this);
        Tools.setSystemBarLight(this);

        loading_dialog = MyUtils.getLoadingDialog(this);



        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
            }
        });
    }




    public void onRegisterClick(View view) {
        loading_dialog.show();

        String username = rUsername.getText().toString();
        String email = rEmail.getText().toString();

        String password = rPassword.getText().toString();



       // loading.setVisibility(View.INVISIBLE);

        if (TextUtils.isEmpty(username)) {
            loading_dialog.dismiss();
            rUsername.setError("Enter you Name");
            rUsername.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(email) || !isValidEmail(email)) {
            loading_dialog.dismiss();
            rEmail.setError("Please Enter a Valid Email");
            rEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            loading_dialog.dismiss();
            rPassword.setError("Enter you Password");
            rPassword.requestFocus();
            return;
        }

        loading_dialog.show();

       // loading.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                loading_dialog.dismiss();

                /*
               // loading.setVisibility(View.GONE);
                FirebaseUser firebaseUser = auth.getCurrentUser();
                String userid = firebaseUser.getUid();
                DocumentReference blogRef = blogsRef.document(userid);
                Map<String, Object> register_user = new HashMap<>();
                register_user.put("id", userid);
                register_user.put("email", email);
                register_user.put("password", password);
                register_user.put("username", username);
                register_user.put("phone", phone);
                register_user.put("created_at", new Date());
                register_user.put("updated_at", new Date());
                register_user.put("imageURL", "default");

                blogRef.set(register_user).addOnSuccessListener(aVoid -> {
                    loading_dialog.dismiss();
                    Toasty.success(this,"Register Success",Toasty.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    loading_dialog.dismiss();
                   // loading.setVisibility(View.INVISIBLE);
                    Log.e(TAG, "onFailure: " + e.getLocalizedMessage());

                });*/

                Toasty.success(this,"Register Success",Toasty.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                RegisterActivity.this.finish();


            } else {
                loading_dialog.dismiss();
                Log.d(TAG, "onComplete: " + task.getException().toString());
                Toasty.error(RegisterActivity.this, "You Can't register with this email /E-mail already register", Toast.LENGTH_SHORT).show();
               // loading.setVisibility(View.INVISIBLE);
            }

        });

    }

    public void onBackPressed(View view) {
        //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
    @Override
    public void onBackPressed() {
        //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

}



