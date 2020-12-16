package com.example.gc_uiactivity.user_state;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.gc_uiactivity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends Activity {
    FirebaseAuth firebaseAuth;
    EditText editTextEmail;
    EditText editTextPassword;
    EditText editTextName;
    Button buttonJoin;
    ImageView iv_sign_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.editText_email);
        editTextPassword = (EditText) findViewById(R.id.editText_passWord);
        editTextName = (EditText) findViewById(R.id.editText_name);

        buttonJoin = (Button) findViewById(R.id.btn_join);
        iv_sign_up=(ImageView)findViewById(R.id.iv_sign_up);
        iv_sign_up.setImageResource(R.drawable.sign_up);

        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editTextEmail.getText().toString().trim();
                final String pwd = editTextPassword.getText().toString().trim();
                final String name=editTextName.getText().toString().trim();
                Log.d("KDJ", "email: " + email);
                Log.d("KDJ", "pwd: " + pwd);
                Log.d("KDJ", "name: " + name);

                firebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    //회원가입 정보를 DB에 저장
                                    final String eData=editTextEmail.getText().toString();
                                    android.util.Log.d("KDJ","이메일: "+eData);
                                    //파이어베이스 실시간 dB관리 객체 열어오기.
                                    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

                                    //저장시킬 노드 탐조객체 가져오기
                                    DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
                                    //전체 지우기
                                    //rootRef.removeValue();
                                    //이메일에서 . 문자 제거
                                    String eDataStr=eData.replaceAll("[.]","");
                                    android.util.Log.d("KDJ","eDataStr: "+eDataStr);
                                    DatabaseReference memberRef=rootRef.child("계정 정보");

                                    DatabaseReference members=memberRef.child(eDataStr);

                                    members.child("lockState").setValue("false");
                                    members.child("Email").setValue(email);
                                    members.child("name").setValue(name);
                                    members.child("password").setValue(pwd);
                                    members.child("Points").setValue("0");

                                    finish();
                                }
                                else if (!task.isSuccessful()) {
                                    Log.e("KDJ", "onComplete: Failed=" + task.getException().getMessage());
                                }
                                else {
                                    Toast.makeText(SignUpActivity.this, "등록 에러", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                });
            }
        });
    }
}
