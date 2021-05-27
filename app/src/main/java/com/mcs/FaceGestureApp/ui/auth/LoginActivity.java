package com.mcs.FaceGestureApp.ui.auth;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

import com.google.firebase.auth.GoogleAuthProvider;
import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.main.HomeActivity;
import com.mcs.FaceGestureApp.ui.main.MainActivity;
import com.mcs.FaceGestureApp.ui.play.WalkThrowActivity;
import com.mcs.FaceGestureApp.utils.MyUtils;
import com.mcs.FaceGestureApp.utils.Tools;

public class LoginActivity extends AppCompatActivity {
    int backpress = 0;
    FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    EditText  email, password;
    Button btn_login,btnforgetpsd;
    //ImageView ani_loading;
    public   static  String TAG="MainActivity";
    private AlertDialog loading_dialog;

    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 101;
    //private  CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        //ani_loading= findViewById(R.id@color/ThemeColorOne.anim_loading_gif);
        Window window = LoginActivity.this.getWindow();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Tools.setSystemBarTransparent(this);
        Tools.setSystemBarLight(this);
        loading_dialog = MyUtils.getLoadingDialog(this);

        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);


        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient= GoogleSignIn.getClient(this, gso);
        //google sign call manager
        // mCallbackManager = CallbackManager.Factory.create();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
        updateUI(user);
    }

    public void reset_psd(View v){
        startActivity(new Intent(LoginActivity.this, com.mcs.FaceGestureApp.ui.auth.ResetPsd.class));
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else
        {
            SharedPreferences user_type = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
            String user_typ = user_type.getString("user_type", "");
            if(user_typ.equals("old")){
                Toasty.info(this, "login to continue", Toast.LENGTH_SHORT).show();
             }else{
                startActivity(new Intent(LoginActivity.this, WalkThrowActivity.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        backpress = (backpress + 1);
        Toasty.info(getApplicationContext(), " Press Back again to Exit ", Toast.LENGTH_SHORT).show();
        if (backpress > 1) {
            this.finish();
        }
    }

    public void onLogin(View view) {
        loading_dialog.show();
       // startActivity(new Intent(MainActivity.this, LoginActivity.class));
        String txt_email = email.getText().toString();
        String txt_password = password.getText().toString();

        if (TextUtils.isEmpty(txt_email) || !MyUtils.isValidEmail(txt_email)) {
            loading_dialog.dismiss();
            email.setError("Enter Email!");
            email.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(txt_password) ) {
            loading_dialog.dismiss();
            password.setError("Enter password!");
            password.requestFocus();
            return;
        }

        MyUtils.hideKeyboard(this);
             //ani_loading.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(txt_email, txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        loading_dialog.dismiss();
                        Toasty.success(LoginActivity.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();

                    } else {
                        loading_dialog.dismiss();
                        Toasty.error(LoginActivity.this, "Authentication Failed "+task.getException(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Error:" + task.getException().getMessage());
                        //ani_loading.setVisibility(View.INVISIBLE);
                    }

                }
            });
    }


    /////////////sign in with google
    public void signInWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //gmail
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toasty.error(getApplicationContext(),"Google sign in failed"+e.getLocalizedMessage()).show();
                // ...
            }
        }
    }



    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toasty.success(getApplicationContext(),"Google signIn success",Toasty.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toasty.error(getApplicationContext(),"Google sign in failed"+task.getException()).show();

                            //  Snackbar.make(vi, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        // ...
                    }
                });
    }


    public void onRegister(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
       //finish();
    }
}