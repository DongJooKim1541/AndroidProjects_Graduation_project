package com.example.gc_uiactivity.answer_note;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class AnswerNoteNumFragment extends Fragment {

    ArrayList<String> items;
    NumAdapter Adapter;
    ListView listView;
    HomeFragment homeFragment;
    AnswerNoteRoundFragment answerNoteRoundFragment;
    AnswerNoteEndFragment answerNoteEndFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.answer_note_num_fragment,container,false);

        homeFragment=new HomeFragment();
        answerNoteRoundFragment=new AnswerNoteRoundFragment();
        answerNoteEndFragment=new AnswerNoteEndFragment();

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

        showAnswerNote(v);

        return v;
    }

    public void showAnswerNote(final View v){

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
                final String curRound=dataSnapshot.child("curRound").getValue().toString();

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
                            String[] problems=new String[(int)(dataSnapshot.getChildrenCount())];
                            for(int i=0;i<dataSnapshot.getChildrenCount();i++){
                                if(dataSnapshot.child(Integer.toString(i)).getValue()!=null){
                                    problems[i]=dataSnapshot.child(Integer.toString(i)).getValue().toString();
                                    android.util.Log.d("KDJ","problems["+i+"]: "+problems[i]);
                                    String problem=problems[i];
                                    String[] splitStr=problem.split("_");
                                    android.util.Log.d("KDJ","split: "+splitStr[0]+"년 "+splitStr[1]+"회 "+splitStr[2]+"번");
                                    if((splitStr[0]+"년").equals(curYear) && (splitStr[1]+"회").equals(curRound)){
                                        items.add(splitStr[2]+"번");
                                        android.util.Log.d("KDJ","split: "+splitStr[1]+"회 "+splitStr[2]+"번");
                                    }
                                }
                            }

                            Adapter=new NumAdapter(getActivity(),R.layout.list_form,items,listView);
                            listView=v.findViewById(R.id.lv_choice_num);
                            listView.setAdapter(Adapter);
                            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                            Adapter.notifyDataSetChanged();

                            if(Adapter.getCount()==0){
                                CheckTypesTask checkTypesTask=new CheckTypesTask();
                                checkTypesTask.execute();
                                Toast.makeText(getActivity(), "해당 회차에 오답이 없습니다.", Toast.LENGTH_SHORT).show();
                                android.util.Log.d("KDJ","numOfProblem: "+dataSnapshot.getChildrenCount());
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,answerNoteRoundFragment).commit();
                            }

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String item=parent.getAdapter().getItem(position).toString();
                                    final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

                                    DatabaseReference curRef=rootRef.child("현재 상태").child("계정 정보");
                                    curRef.child("curNum").setValue(item);
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,answerNoteEndFragment).commit();
                                }
                            });
                        }
                        else{

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

    private class CheckTypesTask extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog=new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("로딩중...");
            //show dialog
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
                for(int i=0;i<5;i++){
                    //progressDialog.setProgress(i*30);
                    Thread.sleep(100);
                }
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            super.onPostExecute(result);
        }
    }
}
