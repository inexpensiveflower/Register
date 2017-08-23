package com.example.firebaseregister;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SaveUserInfo extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextAddress;
    private EditText editTextName;
    private Button buttonSave;
    private Button buttonBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_user_info);

        editTextName = (EditText) findViewById(R.id.saveNewName);
        editTextName.setVisibility(View.GONE);
        editTextAddress = (EditText) findViewById(R.id.saveNewAddress);
        buttonSave = (Button) findViewById(R.id.renewBtn);
        buttonBack = (Button) findViewById(R.id.backToPre);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        buttonSave.setOnClickListener(this);
        buttonBack.setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference RefName = mRootRef.child(user.getUid()).child("name");
        DatabaseReference RefAddr = mRootRef.child(user.getUid()).child("address");

        RefName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String getName = dataSnapshot.getValue(String.class);
                editTextName.setText(getName);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        RefAddr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String getAddress = dataSnapshot.getValue(String.class);
                editTextAddress.setText(getAddress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //防止按返回鍵APP會壞掉
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(SaveUserInfo.this);
            dialog.setTitle("關閉應用程式");
            dialog.setMessage("確定要關閉應用程式?");
            dialog.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SaveUserInfo.this.finish();
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

        if(v == buttonSave){
            saveInformation();

        }

        if(v == buttonBack){
            startActivity(new Intent(this, MainActivity.class));
        }

    }

    private void saveInformation() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this , "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(address)){
            Toast.makeText(this , "Please enter your address", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseReference.child(user.getUid()).child("name").setValue(name);
        mDatabaseReference.child(user.getUid()).child("address").setValue(address);

        Toast.makeText(this, "Information saved...",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
    }
}
