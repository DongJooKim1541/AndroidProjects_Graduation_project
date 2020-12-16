package com.example.gc_uiactivity.answer_note;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.home.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AnswerNoteEndFragment extends Fragment {

    TextView tv_problem;
    ImageView imv_problemImg;
    TextView tv_answer;
    HomeFragment homeFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.answer_note_end_fragment,container,false);

        homeFragment=new HomeFragment();

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
        CheckTypesTask checkTypesTask=new CheckTypesTask();
        checkTypesTask.execute();
        showAnswerNote(v);

        return v;
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
                    Thread.sleep(500);
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

    public void showAnswerNote(View v){
        tv_problem=v.findViewById(R.id.tv_problem);
        imv_problemImg=v.findViewById(R.id.imv_problemImg);
        tv_answer=v.findViewById(R.id.tv_answer);

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
                final String curNum=dataSnapshot.child("curNum").getValue().toString();
                showNote(curEmail,curChoiceProblems,curYear,curRound,curNum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void showNote(String curEmail, final String curProblem, final String year, final String round,final String num){
        final String eDataStr=curEmail.replaceAll("[.]","");

        //파이어베이스 실시간 dB관리 객체 열어오기.
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
        final DatabaseReference memberRef=rootRef.child("계정 정보").child(eDataStr);

        DatabaseReference problemRef=rootRef.child("문제 종류");
        final DatabaseReference problems=problemRef.child(curProblem).child("Year");

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                tv_problem.setText(year+" "+round+" "+num);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://charged-dialect-285301.appspot.com/");
                final String[] itemYear=year.split("년");
                final String[] itemEpisode=round.split("회");
                final String[] itemNumber=num.split("번");
                StorageReference pathReference = storageRef.child("images/"+curProblem+"/"+itemYear[0]+"/"+itemEpisode[0]+"/"+itemNumber[0]+".jpeg");
                Log.d("KDJ", "imagepath: " + "images/"+curProblem+"/"+itemYear[0]+"/"+itemEpisode[0]+"/"+itemNumber[0]+".jpeg");
                Log.d("KDJ", "pathReference: " + pathReference);

                final long ONE_MEGABYTE = 1024 * 1024;
                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imv_problemImg.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Toast.makeText(getActivity(), "다운로드 실패.", Toast.LENGTH_SHORT).show();
                    }
                });


                problems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount()>0){
                            DatabaseReference problem=problems.child(itemYear[0]).child(itemEpisode[0]).child(itemNumber[0]);
                            android.util.Log.d("KDJ","problem: "+problem);

                            problem.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String answer=dataSnapshot.getValue().toString();
                                    android.util.Log.d("KDJ","answer: "+answer);
                                    tv_answer.setText("정답은 "+answer+"번 입니다.");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

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
