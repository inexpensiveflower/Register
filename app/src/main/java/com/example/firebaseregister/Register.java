package com.example.firebaseregister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Process;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Register extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonNextStep;
    private Button buttonBack;

    private EditText typeName;
    private EditText typeAddress;
    private Button buttonRegiser;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonNextStep = (Button) findViewById(R.id.buttonRegister);
        buttonBack = (Button) findViewById(R.id.back);

        typeName = (EditText) findViewById(R.id.saveName);
        typeName.setVisibility(View.GONE);
        typeAddress = (EditText) findViewById(R.id.saveAddress);
        typeAddress.setVisibility(View.GONE);
        buttonRegiser = (Button) findViewById(R.id.register);
        buttonRegiser.setVisibility(View.GONE);

        buttonNextStep.setOnClickListener(this);
        buttonRegiser.setOnClickListener(this);
        buttonBack.setOnClickListener(this);


    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        Integer countPassword = password.length();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "請輸入信箱", Toast.LENGTH_SHORT).show();
            //Stopping the function
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this , "請輸入密碼", Toast.LENGTH_SHORT).show();
            return;
        }

        if(countPassword < 6){
            Toast.makeText(this, "密碼不能少於6個字", Toast.LENGTH_SHORT).show();
            return;
        }
        //if validation is ok
        //we will first show a progressDialog
        progressDialog.setMessage("請稍後...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            editTextEmail.setVisibility(View.GONE);
                            editTextPassword.setVisibility(View.GONE);
                            buttonNextStep.setVisibility(View.GONE);
                            buttonBack.setVisibility(View.GONE);
                            userlogin();

                        }else{
                            Toast.makeText(Register.this, "You got a error! please check again",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void userlogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            //Stopping the function
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this , "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            typeName.setVisibility(View.VISIBLE);
                            typeAddress.setVisibility(View.VISIBLE);
                            buttonRegiser.setVisibility(View.VISIBLE);

                        }else{
                            Toast.makeText(Register.this, "Your email/password is wrong.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //防止按返回鍵APP會壞掉
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(Register.this);
            dialog.setTitle("關閉應用程式");
            dialog.setMessage("確定要關閉應用程式?");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Register.this.finish();
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


    private void saveInformation() {
        String name = typeName.getText().toString().trim();
        String address = typeAddress.getText().toString().trim();

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

        progressDialog.setMessage("註冊中...");
        progressDialog.show();
        startActivity(new Intent(Register.this , MainActivity.class));
    }


    @Override
    public void onClick(View view) {

        if(view == buttonNextStep){
            registerUser();
        }


        if(view == buttonBack){
            startActivity(new Intent(Register.this, MainActivity.class));
        }

        if(view == buttonRegiser){
            saveInformation();
        }

    }
}
