package com.example.gc_uiactivity.options;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChoiceProblemFragment extends Fragment {
    ArrayList<String> items;
    ChoiceProblemAdapter Adapter;
    ListView listView;
    HomeFragment homeFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.choice_problem_fragment,container,false);

        homeFragment=new HomeFragment();

        listView=v.findViewById(R.id.lv_choice_problem);
        items=new ArrayList<String>();


        items.add("정보처리 기사");
        items.add("정보처리 산업기사");
        items.add("정보처리 기능사");

        Adapter=new ChoiceProblemAdapter(getActivity(),R.layout.list_form,items,listView);
        listView.setAdapter(Adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item=parent.getAdapter().getItem(position).toString();

                if(item.equals("정보처리 기사")){
                    Toast.makeText(getActivity(),"정보처리 기사 선택됨",Toast.LENGTH_SHORT).show();
                    SetProblem("rb_IPE");
                }
                else if(item.equals("정보처리 산업기사")){
                    Toast.makeText(getActivity(),"정보처리 산업기사 선택됨",Toast.LENGTH_SHORT).show();
                    SetProblem("rb_IPIE");
                }
                else if(item.equals("정보처리 기능사")){
                    Toast.makeText(getActivity(),"정보처리 기능사 선택됨",Toast.LENGTH_SHORT).show();
                    SetProblem("rb_IPT");
                }
            }
        });

        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,homeFragment).commit();

                    return true;
                }
                else{
                    return false;
                }
            }
        });

        return v;
    }

        public void SetProblem(final String problem){

            //파이어베이스 실시간 dB관리 객체 열어오기.
            FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

            //저장시킬 노드 탐조객체 가져오기
            final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

            DatabaseReference curRef=rootRef.child("현재 상태").child("계정 정보");

            curRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String curEmail=dataSnapshot.child("Email").getValue().toString();

                    DatabaseReference memberRef=rootRef.child("계정 정보");

                    DatabaseReference members=memberRef.child(curEmail);

                    DatabaseReference currRef=rootRef.child("현재 상태");
                    DatabaseReference currMembers=currRef.child("계정 정보");

                    DatabaseReference problemRef=rootRef.child("문제 종류");

                    //DatabaseReference problems=problemRef.child(problem);

                    if(problem=="rb_IPE"){
                        currMembers.child("curChoiceProblems").setValue(problem);
                        currMembers.child("problem_to_Korean").setValue("정보처리기사");
                        members.child("ChoiceProblem").setValue(problem);
                        members.child("problem_to_Korean").setValue("정보처리기사");
                    }
                    else if(problem=="rb_IPIE"){
                        currMembers.child("curChoiceProblems").setValue(problem);
                        currMembers.child("problem_to_Korean").setValue("정보처리산업기사");
                        members.child("ChoiceProblem").setValue(problem);
                        members.child("problem_to_Korean").setValue("정보처리산업기사");
                    }
                    else if(problem=="rb_IPT") {
                        currMembers.child("curChoiceProblems").setValue(problem);
                        currMembers.child("problem_to_Korean").setValue("정보처리기능사");
                        members.child("ChoiceProblem").setValue(problem);
                        members.child("problem_to_Korean").setValue("정보처리기능사");
                    }
                    //이미지 세팅(프로젝트 합칠땐 생략해도됨)
                    //setImage(problem);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
};
