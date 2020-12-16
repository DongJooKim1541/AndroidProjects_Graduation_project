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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AnswerNoteYearFragment extends Fragment {
    ArrayList<String> items;
    YearAdapter Adapter;
    ListView listView;
    AnswerNoteRoundFragment answerNoteRoundFragment;
    HomeFragment homeFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.answer_note_year_fragment,container,false);

        answerNoteRoundFragment=new AnswerNoteRoundFragment();
        homeFragment=new HomeFragment();

        listView=v.findViewById(R.id.lv_choice_year);
        items=new ArrayList<String>();
        items.add(2018+"년");
        items.add(2019+"년");
        Adapter=new YearAdapter(getActivity(),R.layout.list_form,items,listView);
        listView.setAdapter(Adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Adapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item=parent.getAdapter().getItem(position).toString();

                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

                //저장시킬 노드 탐조객체 가져오기
                final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

                DatabaseReference curRef=rootRef.child("현재 상태").child("계정 정보");
                curRef.child("curYear").setValue(item);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,answerNoteRoundFragment).commit();
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
}
