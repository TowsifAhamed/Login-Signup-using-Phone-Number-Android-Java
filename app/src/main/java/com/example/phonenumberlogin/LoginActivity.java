package com.example.phonenumberlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mphonenumber, mcode;

    private Button mverify;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallbacks;

    String mverificationid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        userisloggedin();
        mphonenumber = findViewById(R.id.phonenumber);
        mcode = findViewById(R.id.code);
        mverify = findViewById(R.id.send);
        mverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mverificationid != null){
                    verifyphonenumberwithcode();
                }
                else
                    startphonenumberverificastion();
            }
        });
        mcallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signinwithphoneauthcredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String verificationid, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationid, forceResendingToken);
                mverificationid = verificationid;
                mverify.setText("Verify Code");
            }
        };
    }

    private void verifyphonenumberwithcode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mverificationid, mcode.getText().toString());
        signinwithphoneauthcredential(credential);
    }

    private void signinwithphoneauthcredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user!=null){
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    mUserDB.updateChildren(userMap);
                                }
                                userisloggedin();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
    }

    private void userisloggedin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            startActivity(new Intent(getApplicationContext(),MainPageActivity.class)); finish();
            return;
        }
    }

    private void startphonenumberverificastion() {
        mverify.setText("sending");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mphonenumber.getText().toString(),
                60 ,
                TimeUnit.SECONDS,
                this ,
                mcallbacks
        );

    }

}

