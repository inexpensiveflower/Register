package com.example.firebaseregister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.firebaseregister.R.id.button_facebook_signout;

public class FbLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView welcomeText;

    private Button prePage ;
    private Button signOut;
    private static final String TAG = "FacebookLogin";

    private EditText holdUid;
    private EditText holdName;
    private EditText holdAddress;
    private Button saveInfo;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_fb_login);

        welcomeText = (TextView) findViewById(R.id.welcomeTextView);

        holdAddress = (EditText) findViewById(R.id.holdAddress);
        holdName =(EditText) findViewById(R.id.holdName);
        holdUid = (EditText) findViewById(R.id.holdUid);
        saveInfo = (Button) findViewById(R.id.saveInfo);
        findViewById(R.id.holdName).setVisibility(View.GONE);
        findViewById(R.id.holdUid).setVisibility(View.GONE);
        findViewById(R.id.holdAddress).setVisibility(View.GONE);
        findViewById(R.id.saveInfo).setVisibility(View.GONE);


        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        signOut = (Button) findViewById(R.id.button_facebook_signout);
        prePage = (Button) findViewById(R.id.prePage);
        prePage.setVisibility(View.GONE);
        signOut.setVisibility(View.GONE);



        //initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //initialize fb login button
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton fbLoginBtn = (LoginButton) findViewById(R.id.fblogin_button);
        fbLoginBtn.setVisibility(View.GONE);
        fbLoginBtn.performClick();
        fbLoginBtn.setReadPermissions("email");
        fbLoginBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                startActivity(new Intent(FbLoginActivity.this , MainActivity.class));
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                //start exclude
                updateUI(null);
                //end exclude
                prePage.performClick();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError" , error);
                //start exclude
                updateUI(null);
                //end exclude
            }
        });
        //end initialize fb login

        progressDialog = new ProgressDialog(this);
        saveInfo.setOnClickListener(this);
        signOut.setOnClickListener(this);
        prePage.setOnClickListener(this);

    }

    //start onStart check user
    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }

    //start auth with facebook
    private void handleFacebookAccessToken(AccessToken token){
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        progressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //sign in success, update UI with sign in user's information
                            Log.d(TAG, "signInWithCrediential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else{
                            //if sign in fails, display a message to user
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(FbLoginActivity.this, "Authentication fialed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        progressDialog.dismiss();

                    }
                });
    }
    //end auth with fb


    //更改頁面上物件的visibility
    private void updateUI(FirebaseUser user){
        progressDialog.dismiss();

        if(user != null){

            holdName.setText(user.getDisplayName());
            holdUid.setText(user.getUid());
            welcomeText.setText("Welcome");


        }else{

            holdUid.setText(null);
            holdName.setText(null);
            holdAddress.setText(null);
            welcomeText.setText("Welcome");

            //findViewById(R.id.fbBtn).setVisibility(View.VISIBLE);
        }
    }

    private void saveInformation(){

        String name = holdName.getText().toString().trim();
        String address = holdAddress.getText().toString().trim();
        String uid = holdUid.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(address)){
            Toast.makeText(this , "Please enter address", Toast.LENGTH_SHORT).show();
            return;
        }


        databaseReference.child(uid).child("name").setValue(name);
        databaseReference.child(uid).child("address").setValue(address);
        Toast.makeText(this, "Information saved...",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(FbLoginActivity.this, MainActivity.class));

    }

    @Override
    public void onClick(View view) {

        if(view == prePage){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        if(view == saveInfo){
            saveInformation();
            startActivity(new Intent(this,MainActivity.class));
        }

        if(view == signOut){
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            updateUI(null);
            startActivity(new Intent(FbLoginActivity.this, MainActivity.class));
        }
    }
}