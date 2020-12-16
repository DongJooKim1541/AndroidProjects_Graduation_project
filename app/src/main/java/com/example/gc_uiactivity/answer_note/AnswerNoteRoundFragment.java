package com.example.gc_uiactivity.answer_note;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

public class AnswerNoteRoundFragment extends Fragment {

    ArrayList<String> items;
    RoundAdapter Adapter;
    ListView listView;
    HomeFragment homeFragment;
    AnswerNoteNumFragment answerNoteNumFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.answer_note_round_fragment,container,false);

        homeFragment=new HomeFragment();
        answerNoteNumFragment=new AnswerNoteNumFragment();

        showAnswerList(v);

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


    public void showAnswerList(final View v){
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

        //저장시킬 노드 탐조객체 가져오기
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

        DatabaseReference curRef=rootRef.child("현재 상태").child("계정 정보");

        curRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String curEmail=dataSnapshot.child("Email").getValue().toString();
                String curChoiceProblems=dataSnapshot.child("curChoiceProblems").getValue().toString();
                final String curYear=dataSnapshot.child("curYear").getValue().toString();

                //파이어베이스 실시간 dB관리 객체 열어오기.
                final FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

                //저장시킬 노드 탐조객체 가져오기
                DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

                DatabaseReference memberRef=rootRef.child("계정 정보");

                DatabaseReference members=memberRef.child(curEmail).child("오답 목록").child(curChoiceProblems);

                members.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        items=new ArrayList<String>();

                        if(dataSnapshot.getChildrenCount()>0){
                            for(int idx=0;idx<4;idx++){
                                items.add(idx+1+"회");
                            }

                            Adapter=new RoundAdapter(getActivity(),R.layout.list_form,items,listView);
                            listView=v.findViewById(R.id.lv_choice_round);
                            listView.setAdapter(Adapter);
                            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                            Adapter.notifyDataSetChanged();

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String item=parent.getAdapter().getItem(position).toString();
                                    final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

                                    DatabaseReference curRef=rootRef.child("현재 상태").child("계정 정보");
                                    curRef.child("curRound").setValue(item);
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,answerNoteNumFragment).commit();
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
