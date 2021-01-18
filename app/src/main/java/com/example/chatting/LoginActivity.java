package com.example.chatting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatting.model.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText id;
    private EditText pw;
    public UserModel userModel;
    private Button login;
    private Button signup;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleLogin;
    private GoogleSignInClient  mGoogleSignInClient;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //구글 로그인 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        // Initialize Firebase Auth




        //구글 로그인 설정
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
       firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
       //mGoogleSignInClient.signOut();
        String splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }
        id = (EditText)findViewById(R.id.loginActivity_edittext_id);
        pw = (EditText)findViewById(R.id.loginActivity_edittext_pw);

        login = (Button) findViewById(R.id.loginActivity_button_login);
        signup = (Button)findViewById(R.id.loginActivity_buttn_signup);
        googleLogin = (SignInButton) findViewById(R.id.loginActivity_button_loginGoogle);
        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();
            }
        });
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("구글 로그인버튼","구글로그인버튼");
                Toast.makeText(getApplicationContext(),"로그인버튼 클릭",Toast.LENGTH_LONG);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });

        //로그인 인터페이스 리스너

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user !=null){
                //로그인
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                //로그아웃
            }
        };
    }
    void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(),pw.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){


                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()){
                            //로그인 실패
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("로그인","로그인연습");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.d("a","a");

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.d("b","b");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                Log.d("c","c");
                firebaseAuthWithGoogle(account.getIdToken());
                Log.d("d","d");
            } catch (ApiException e) {

            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        // [START_EXCLUDE silent]

        // [END_EXCLUDE]
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("로그인", "signInWithCredential:success");
                            FirebaseDatabase.getInstance().getReference().child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    DataSnapshot datasnapshot = task.getResult();
                                    for(DataSnapshot snapshot:datasnapshot.getChildren()){
                                        if(!snapshot.getKey().contains(firebaseAuth.getCurrentUser().getUid())) {
                                           userModel.userName= firebaseAuth.getCurrentUser().getDisplayName();
                                           userModel.profileImageUrl=String.valueOf(firebaseAuth.getCurrentUser().getPhotoUrl());
                                           userModel.uid=firebaseAuth.getCurrentUser().getUid();
                                           FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getCurrentUser().getUid()).setValue(userModel);
                                        }
                                    }
                                }
                            });



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("로그인", "signInWithCredential:fail");

                        }


                    }
                });
    }
    // [END auth_with_google]

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

}