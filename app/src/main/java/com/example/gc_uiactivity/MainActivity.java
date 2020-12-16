package com.example.gc_uiactivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gc_uiactivity.answer_note.AnswerNoteYearFragment;
import com.example.gc_uiactivity.cash.CashFragment;
import com.example.gc_uiactivity.home.HomeFragment;
import com.example.gc_uiactivity.options.OptionFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{

    HomeFragment homeFragment;
    //LockFragment lockFragment;
    OptionFragment optionFragment;
    AnswerNoteYearFragment answerNoteYearFragment;
    CashFragment cashFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //액션바컨트롤
        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.drawable.logo_image);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        homeFragment=new HomeFragment();
        //lockFragment=new LockFragment();
        optionFragment=new OptionFragment();
        answerNoteYearFragment=new AnswerNoteYearFragment();
        cashFragment=new CashFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,homeFragment).commit();
    }




    //액션바 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    //액션바 리스너
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

        //저장시킬 노드 탐조객체 가져오기
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        switch (item.getItemId()) {
            case R.id.action_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,homeFragment).commit();
                return true;
            case R.id.action_option:
                currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email=(String)dataSnapshot.child("Email").getValue();
                        if(email!=null){
                            String eDataStr=email.replaceAll("[.]","");

                            DatabaseReference memberRef=rootRef.child("계정 정보");

                            DatabaseReference members=memberRef.child(eDataStr);

                            members.child("ChoiceProblem");

                            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,optionFragment).commit();
                        }
                        else{
                            Toast.makeText(MainActivity.this,"로그인을 해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            case R.id.action_study:
                currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email=(String)dataSnapshot.child("Email").getValue();
                        if(email!=null){
                            String eDataStr=email.replaceAll("[.]","");

                            DatabaseReference memberRef=rootRef.child("계정 정보");

                            DatabaseReference members=memberRef.child(eDataStr);

                            members.child("ChoiceProblem");

                            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,answerNoteYearFragment).commit();
                        }
                        else{
                            Toast.makeText(MainActivity.this,"로그인을 해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            case R.id.action_cash:
                currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email=(String)dataSnapshot.child("Email").getValue();
                        if(email!=null){
                            String eDataStr=email.replaceAll("[.]","");

                            DatabaseReference memberRef=rootRef.child("계정 정보");

                            DatabaseReference members=memberRef.child(eDataStr);

                            members.child("ChoiceProblem");

                            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,cashFragment).commit();
                        }
                        else{
                            Toast.makeText(MainActivity.this,"로그인을 해주시기 바랍니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
