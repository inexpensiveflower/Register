package com.example.firebaseregister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GoogleLoginActivity extends AppCompatActivity implements View.OnClickListener{

    private SignInButton googleBtn;
    private Button fakeGoogle;
    private Button saveInfo;
    private Button editInfo;
    private Button signout;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "MAIN_ACTIVITY";
    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    private TextView welcome;
    private EditText name;
    private EditText uid;
    private EditText address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        name = (EditText) findViewById(R.id.holdNameGoogle);
        name.setVisibility(View.GONE);
        uid = (EditText) findViewById(R.id.holdUidGoogle);
        uid.setVisibility(View.GONE);
        address = (EditText) findViewById(R.id.saveAddrGoogle);
        address.setVisibility(View.GONE);
        welcome = (TextView) findViewById(R.id.welcomeGoogle);

        googleBtn = (SignInButton) findViewById(R.id.googleButton);
        findViewById(R.id.googleButton).setVisibility(View.GONE);

        fakeGoogle = (Button) findViewById(R.id.fakeGoogle);
        findViewById(R.id.fakeGoogle).setVisibility(View.GONE);
        saveInfo = (Button) findViewById(R.id.saveInfoGoogle);
        saveInfo.setVisibility(View.GONE);
        editInfo = (Button) findViewById(R.id.editInfoGoogle);
        editInfo.setVisibility(View.GONE);
        signout = (Button) findViewById(R.id.signoutGoogle);
        findViewById(R.id.signoutGoogle).setVisibility(View.GONE);


        databaseReference= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();


        // Configure Google Sign In
        //google 登入
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(GoogleLoginActivity.this, "You got error" , Toast.LENGTH_SHORT).show();

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        fakeGoogle.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        signout.setOnClickListener(this);
        saveInfo.setOnClickListener(this);
        editInfo.setOnClickListener(this);


    }

    @Override
    public void onStart(){
        super.onStart();
        fakeGoogle.performClick();
    }

    public void signOut(){

    }

    //google登入method
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

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

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        FirebaseUser user = mAuth.getCurrentUser();
                        String googleUserName = account.getDisplayName();
                        name.setText(googleUserName);
                        updateUIGoogle(user);


                        if(!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GoogleLoginActivity.this, "Authentication failed. ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUIGoogle(FirebaseUser user){
        progressDialog.dismiss();

        if(user != null){

            final String googleUserName = name.getText().toString().trim();
            welcome.setText("Welcome " + googleUserName);
            saveInfo.setVisibility(View.GONE);

            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference RefAddress = mRootRef.child(user.getUid()).child("address");

            RefAddress.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userAddress = dataSnapshot.getValue(String.class);
                    address.setText(userAddress);


                    if(userAddress == null){
                        name.setText(googleUserName);
                        address.setVisibility(View.VISIBLE);
                        saveInfo.setVisibility(View.VISIBLE);
                        editInfo.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            findViewById(R.id.fakeGoogle).setVisibility(View.GONE);
            signout.setVisibility(View.VISIBLE);
            editInfo.setVisibility(View.VISIBLE);


        }else{

            name.setText(null);
            welcome.setText("歡迎註冊/登入");
            findViewById(R.id.fakeGoogle).setVisibility(View.VISIBLE);
            signout.setVisibility(View.GONE);
            editInfo.setVisibility(View.GONE);

        }
    }

    private void saveInformationGoogle() {
        String getName = name.getText().toString().trim();
        String getAddress = address.getText().toString().trim();

        if(TextUtils.isEmpty(getName)){
            Toast.makeText(this , "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(getAddress)){
            Toast.makeText(this , "Please enter your address", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        databaseReference.child(user.getUid()).child("name").setValue(name);
        databaseReference.child(user.getUid()).child("address").setValue(address);

        Toast.makeText(this, "Information saved...",Toast.LENGTH_SHORT).show();
        welcome.setText("Welcome " + name);
        startActivity(new Intent(GoogleLoginActivity.this , MainActivity.class));
    }

    @Override
    public void onClick(View v) {


        if(v == saveInfo){
            saveInformationGoogle();
            startActivity(new Intent(GoogleLoginActivity.this,MainActivity.class));
        }

        if(v == editInfo){
            startActivity(new Intent(GoogleLoginActivity.this, SaveUserInfo.class));
        }
        if(v == signout){

            mAuth.signOut();
            LoginManager.getInstance().logOut();
            startActivity(new Intent(GoogleLoginActivity.this, MainActivity.class));
        }

        if(v == fakeGoogle){
            googleSignIn();
        }
    }
}
