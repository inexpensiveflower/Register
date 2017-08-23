package com.example.firebaseregister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Ref;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button registerBtn;
    private Button memberLogin;
    private Button logout;
    private Button editInfo;
    private TextView title;
    private FirebaseAuth mAuth;

    //給google登入後要儲存使用者基本資訊用的
    private EditText putAddress;
    private EditText putName;
    private Button saveInfoForGoogle;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = (TextView) findViewById(R.id.title);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        memberLogin = (Button) findViewById(R.id.memberLogin);
        editInfo = (Button) findViewById(R.id.editInfo);
        logout = (Button) findViewById(R.id.logoutttt);

        putAddress = (EditText) findViewById(R.id.putAddress);
        putName = (EditText) findViewById(R.id.putName);
        saveInfoForGoogle = (Button) findViewById(R.id.saveInfoForGoogle);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.registerBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.memberLogin).setVisibility(View.VISIBLE);
        findViewById(R.id.editInfo).setVisibility(View.VISIBLE);
        findViewById(R.id.logoutttt).setVisibility(View.VISIBLE);
        putName.setVisibility(View.GONE);
        saveInfoForGoogle.setVisibility(View.GONE);

        checkStatus();

        registerBtn.setOnClickListener(this);
        memberLogin.setOnClickListener(this);
        logout.setOnClickListener(this);
        editInfo.setOnClickListener(this);
        saveInfoForGoogle.setOnClickListener(this);
    }

    private void checkStatus() {

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){

            findViewById(R.id.registerBtn).setVisibility(View.GONE);
            findViewById(R.id.memberLogin).setVisibility(View.GONE);
            findViewById(R.id.logoutttt).setVisibility(View.VISIBLE);
            findViewById(R.id.editInfo).setVisibility(View.VISIBLE);
            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference RefName = mRootRef.child(user.getUid()).child("name");

            RefName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userName = dataSnapshot.getValue(String.class);
                    title.setText("Welcome " + userName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            DatabaseReference RefAddress = mRootRef.child(user.getUid()).child("address");
            RefAddress.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userAddress = dataSnapshot.getValue(String.class);

                    if(userAddress == null){
                        title.setText("Welcome " + getIntent().getExtras().getString("googleUserName"));
                        putName.setText(getIntent().getExtras().getString("googleUserName"));
                        putAddress.setVisibility(View.VISIBLE);
                        saveInfoForGoogle.setVisibility(View.VISIBLE);
                        editInfo.setVisibility(View.GONE);
                        logout.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            findViewById(R.id.registerBtn).setVisibility(View.VISIBLE);
            findViewById(R.id.memberLogin).setVisibility(View.VISIBLE);
            findViewById(R.id.logoutttt).setVisibility(View.GONE);
            findViewById(R.id.editInfo).setVisibility(View.GONE);
        }
    }

    //防止按返回鍵APP會壞掉
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("關閉應用程式");
            dialog.setMessage("確定要關閉應用程式?");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();

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
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        if(v == registerBtn){
            startActivity(new Intent(MainActivity.this, Register.class));
        }

        if(v == memberLogin){
            startActivity(new Intent(MainActivity.this , LoginActivity.class));
        }

        if(v == logout){
            mAuth.signOut();
            title.setText(" ");
            logout.setVisibility(View.GONE);
            editInfo.setVisibility(View.GONE);
            registerBtn.setVisibility(View.VISIBLE);
            memberLogin.setVisibility(View.VISIBLE);
        }

        if(v == editInfo){
            startActivity(new Intent(MainActivity.this, SaveUserInfo.class));
        }

        if(v == saveInfoForGoogle){
            saveInformation();
            saveInfoForGoogle.setVisibility(View.GONE);
            putName.setVisibility(View.GONE);
            putAddress.setVisibility(View.GONE);
            editInfo.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
        }

    }

    private void saveInformation() {
        String name = putName.getText().toString().trim();
        String address = putAddress.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this , "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(address)){
            Toast.makeText(this , "Please enter your address", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        databaseReference.child(user.getUid()).child("name").setValue(name);
        databaseReference.child(user.getUid()).child("address").setValue(address);

        Toast.makeText(this, "Information saved...",Toast.LENGTH_SHORT).show();
        title.setText("Welcome " + name);
        startActivity(new Intent(MainActivity.this , MainActivity.class));
    }

}
