package com.example.firebaseregister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Process;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView welcomeTextView;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button back;
    private TextView forgotPassword;
    private TextView enterApp;

    private SignInButton googleBtn;
    private Button loginWithGoogle;
    private EditText holdName;
    private EditText holdAddress;

    private CallbackManager mCallbackManager;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private static final String TAG = "FBLOGIN";
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        welcomeTextView =(TextView) findViewById(R.id.textView);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.emailLoginBtn);
        back = (Button) findViewById(R.id.back);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        loginWithGoogle = (Button) findViewById(R.id.loginWithGoogle);
        enterApp = (TextView) findViewById(R.id.enterApp);

        //fb、google登入後儲存使用者資訊用的
        holdName =(EditText) findViewById(R.id.holdName);
        holdAddress = (EditText) findViewById(R.id.holdAddress);
        googleBtn = (SignInButton) findViewById(R.id.googleBtn);

        findViewById(R.id.googleBtn).setVisibility(View.GONE);
        findViewById(R.id.holdAddress).setVisibility(View.GONE);
        findViewById(R.id.holdName).setVisibility(View.GONE);

        mCallbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();

        //google 登入
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(LoginActivity.this, "You got error" , Toast.LENGTH_SHORT).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //googleBtn.setOnClickListener(this);

        //google 登入結束

        buttonLogin.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        loginWithGoogle.setOnClickListener(this);
        enterApp.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        back.setOnClickListener(this);


    }

    private void userlogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this , "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("登入中...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        }else{
                            Toast.makeText(LoginActivity.this, "Your email/password is wrong.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void resetPassword(){
        String email = editTextEmail.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email first!", Toast.LENGTH_SHORT).show();
            //Stopping the function
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Log.d(TAG, "Email sent.");
                            Toast.makeText(LoginActivity.this, "請至信箱重設密碼",Toast.LENGTH_LONG).show();
                            findViewById(R.id.forgotPassword).setVisibility(View.GONE);
                        }
                    }
                });
    }

    //防止按返回鍵APP會壞掉
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
            dialog.setTitle("關閉應用程式");
            dialog.setMessage("確定要關閉應用程式?");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LoginActivity.this.finish();
                }
            });

            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.show();

        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                updateUIGoogle(null);
            }
        }
    }

    //google登入method
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        String googleUserName = account.getDisplayName();
                        holdName.setText(googleUserName);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("googleUserName" , googleUserName);
                        startActivity(intent);


                        if(!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed. ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //google登入method結束

    //GOOGLE登入時，更改頁面上物件的visibility
    private void updateUIGoogle(FirebaseUser user){
        progressDialog.dismiss();

        if(user != null){

        }
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.signOut();
        findViewById(R.id.forgotPassword).setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View view) {

        if(view == buttonLogin){
            userlogin();
        }

        if(view == back){
            startActivity(new Intent(this, MainActivity.class));
        }

        if(view == forgotPassword){
            resetPassword();
        }

        if(view == loginWithGoogle){
            googleSignIn();
        }
        if(view == enterApp){
            startActivity(new Intent(LoginActivity.this , MainActivity.class));
        }
    }
}
