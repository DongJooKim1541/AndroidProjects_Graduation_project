package com.example.gc_uiactivity.lock_screen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.gc_uiactivity.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class LockScreenActivity extends Activity {

    ImageView iv_problem_image;
    TextView tv_problem;

    RadioGroup rg_answer;
    RadioButton rb_answer1;
    RadioButton rb_answer2;
    RadioButton rb_answer3;
    RadioButton rb_answer4;

    TextView tv_result;
    Random random=new Random();

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        tv_problem=findViewById(R.id.tv_problem);

        iv_problem_image=findViewById(R.id.problem_image);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        rb_answer1=findViewById(R.id.rb_answer1);
        rb_answer2=findViewById(R.id.rb_answer2);
        rb_answer3=findViewById(R.id.rb_answer3);
        rb_answer4=findViewById(R.id.rb_answer4);

        rb_answer1.setOnClickListener(radioButtonClickListener);
        rb_answer2.setOnClickListener(radioButtonClickListener);
        rb_answer3.setOnClickListener(radioButtonClickListener);
        rb_answer4.setOnClickListener(radioButtonClickListener);

        rg_answer=findViewById(R.id.rg_answer);

        tv_result=findViewById(R.id.tv_result);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        CheckTypesTask checkTypesTask=new CheckTypesTask();
        checkTypesTask.execute();
        ShowProblem();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // ScaleGestureDetector에서 factor를 받아 변수로 선언한 factor에 넣고
            mScaleFactor *= mScaleGestureDetector.getScaleFactor();

            // 최대 10배, 최소 10배 줌 한계 설정
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 2.0f));

            // 이미지뷰 스케일에 적용
            iv_problem_image.setScaleX(mScaleFactor);
            iv_problem_image.setScaleY(mScaleFactor*1.1f);
            return true;
        }
    }

    private class CheckTypesTask extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog=new ProgressDialog(LockScreenActivity.this);

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


    RadioButton.OnClickListener radioButtonClickListener=new android.widget.RadioButton.OnClickListener(){
        @Override
        public void onClick(View v) {

        }
    };

    public void ShowProblem(){

        //파이어베이스 실시간 dB관리 객체 열어오기.
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String eDataStr=dataSnapshot.child("Email").getValue().toString();
                final DatabaseReference memberRef=rootRef.child("계정 정보").child(eDataStr);


                memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("ChoiceProblem").getValue()!=null){

                            if(dataSnapshot.child("ChoiceProblem").getValue()!=null && dataSnapshot.child("problem_to_Korean").getValue()!=null){

                                final String problem=dataSnapshot.child("ChoiceProblem").getValue().toString();
                                final String problem_to_korean=dataSnapshot.child("problem_to_Korean").getValue().toString();

                                Log.d("KDJ", "problem:" + problem);
                                final DatabaseReference problemRef=rootRef.child("문제 종류");
                                //아래 pathReference와 child.child가 같아야함
                                final DatabaseReference problems=problemRef.child(problem).child("Year");

                                problems.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getChildrenCount()>0){
                                            //2018년, 2019년
                                            final String randYear=Integer.toString(random.nextInt((int)(dataSnapshot.getChildrenCount()))+2018);

                                            final DatabaseReference problemYear= problems.child(randYear);
                                            Log.d("KDJ", "Episode 개수: " + dataSnapshot.getChildrenCount());
                                            Log.d("KDJ", "randYear: " + randYear);
                                            Log.d("KDJ", "problemYear: " + problemYear);

                                            problemYear.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    //지금 2회차, 3회차만 있어서 +2해놓은상태
                                                    if(dataSnapshot.getChildrenCount()>0){
                                                        //1회, 2회, 3회
                                                        final String randEpisode=Integer.toString(random.nextInt((int)(dataSnapshot.getChildrenCount()))+1);
                                                        //회차 정하기
                                                        DatabaseReference problemEpisode= problemYear.child(randEpisode);

                                                        Log.d("KDJ", "Episode 개수: " + dataSnapshot.getChildrenCount());
                                                        Log.d("KDJ", "randEpisode: " + randEpisode);
                                                        Log.d("KDJ", "problemEpisode: " + problemEpisode);

                                                        problemEpisode.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                                                StorageReference storageRef = storage.getReferenceFromUrl("gs://charged-dialect-285301.appspot.com/");

                                                                //이걸 랜덤으로 돌려야함
                                                                Log.d("KDJ", "randImagePath_dataSnapshot.getChildrenCount(): " + dataSnapshot.getChildrenCount());

                                                                String randImagePath=Integer.toString(random.nextInt((int)(dataSnapshot.getChildrenCount()))+1);

                                                                StorageReference pathReference = storageRef.child("images/"+problem+"/"+randYear+"/"+randEpisode+"/"+randImagePath+".jpeg");

                                                                Log.d("KDJ", "pathReference: " + "images/"+problem+"/"+randYear+"/"+randEpisode+"/"+randImagePath+".jpeg");

                                                                tv_problem.setText(problem_to_korean+" "+randYear+"년 "+randEpisode+"회 "+randImagePath+"번");

                                                                Log.d("KDJ", "pathReference: " + pathReference);
                                                                Log.d("KDJ", "problems: " + problems);

                                                                final long ONE_MEGABYTE = 1024 * 1024;
                                                                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                    @Override
                                                                    public void onSuccess(byte[] bytes) {
                                                                        // Data for "images/island.jpg" is returns, use this as needed
                                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                                        iv_problem_image.setImageBitmap(bitmap);
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception exception) {
                                                                        // Handle any errors
                                                                        Toast.makeText(getApplicationContext(), "다운로드 실패.", Toast.LENGTH_SHORT).show();
                                                                        ShowProblem();
                                                                    }
                                                                });


                                                                Log.d("KDJ", "get_problem_random_dataSnapshot.getChildrenCount(): " + dataSnapshot.getChildrenCount());
                                                                final String get_problem_random=randImagePath;
                                                                if(dataSnapshot.child(get_problem_random).getValue()!=null){

                                                                    final String rightAnswer=dataSnapshot.child(get_problem_random).getValue().toString();

                                                                    final String finalRightAnswer = rightAnswer;
                                                                    android.util.Log.d("KDJ","finalRightAnswer: "+finalRightAnswer);

                                                                    final DatabaseReference memberPointRef=rootRef.child("계정 정보").child(eDataStr).child("Points");
                                                                    rg_answer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                                                        @Override
                                                                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                                                                            //1번
                                                                            if(checkedId==R.id.rb_answer1){
                                                                                String choice="1";
                                                                                if(finalRightAnswer.equals(choice)){
                                                                                    tv_result.setText("정답입니다");
                                                                                    //조건이 참인 경우에만 점수 상승
                                                                                    memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            memberPointRef.setValue(Integer.toString(Integer.parseInt(dataSnapshot.getValue().toString())+1000));
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    finish();
                                                                                }
                                                                                else{
                                                                                    memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            if(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue()==null){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue("0");
                                                                                            }

                                                                                            String inCorrectAnswerNum=dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString();
                                                                                            android.util.Log.d("KDJ","inCorrectAnswerNum: "+inCorrectAnswerNum);
                                                                                            boolean isExist=false;
                                                                                            for(int i=0;i<Integer.parseInt(inCorrectAnswerNum);i++){
                                                                                                if(dataSnapshot.child("오답 목록").child(problem).child(Integer.toString(i)).getValue().toString().equals(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer)){
                                                                                                    isExist=true;
                                                                                                }
                                                                                            }
                                                                                            if(isExist==false){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue(Integer.toString(Integer.parseInt(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString())+1));
                                                                                                memberRef.child("오답 목록").child(problem).child(inCorrectAnswerNum).setValue(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer);
                                                                                            }
                                                                                            else{

                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    Toast.makeText(getApplicationContext(), "오답입니다.", Toast.LENGTH_SHORT).show();
                                                                                    finish();
                                                                                }
                                                                            }
                                                                            //2번
                                                                            else if(checkedId==R.id.rb_answer2){
                                                                                String choice="2";
                                                                                if(finalRightAnswer.equals(choice)){
                                                                                    tv_result.setText("정답입니다");
                                                                                    //조건이 참인 경우에만 점수 상승
                                                                                    memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            memberPointRef.setValue(Integer.toString(Integer.parseInt(dataSnapshot.getValue().toString())+1000));
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    finish();
                                                                                }
                                                                                else{
                                                                                    memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            if(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue()==null) {
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue("0");
                                                                                            }

                                                                                            String inCorrectAnswerNum=dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString();
                                                                                            android.util.Log.d("KDJ","inCorrectAnswerNum: "+inCorrectAnswerNum);
                                                                                            boolean isExist=false;
                                                                                            for(int i=0;i<Integer.parseInt(inCorrectAnswerNum);i++){
                                                                                                if(dataSnapshot.child("오답 목록").child(problem).child(Integer.toString(i)).getValue().toString().equals(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer)){
                                                                                                    isExist=true;
                                                                                                }
                                                                                            }
                                                                                            if(isExist==false){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue(Integer.toString(Integer.parseInt(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString())+1));
                                                                                                memberRef.child("오답 목록").child(problem).child(inCorrectAnswerNum).setValue(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer);
                                                                                            }
                                                                                            else{

                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    Toast.makeText(getApplicationContext(), "오답입니다.", Toast.LENGTH_SHORT).show();
                                                                                    finish();
                                                                                }
                                                                            }
                                                                            //3번
                                                                            else if(checkedId==R.id.rb_answer3){
                                                                                String choice="3";
                                                                                if(finalRightAnswer.equals(choice)){
                                                                                    tv_result.setText("정답입니다");
                                                                                    //조건이 참인 경우에만 점수 상승
                                                                                    memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            memberPointRef.setValue(Integer.toString(Integer.parseInt(dataSnapshot.getValue().toString())+1000));
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    finish();
                                                                                }
                                                                                else{
                                                                                    memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            if(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue()==null){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue("0");
                                                                                            }

                                                                                            String inCorrectAnswerNum=dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString();
                                                                                            android.util.Log.d("KDJ","inCorrectAnswerNum: "+inCorrectAnswerNum);
                                                                                            boolean isExist=false;
                                                                                            for(int i=0;i<Integer.parseInt(inCorrectAnswerNum);i++){
                                                                                                if(dataSnapshot.child("오답 목록").child(problem).child(Integer.toString(i)).getValue().toString().equals(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer)){
                                                                                                    isExist=true;
                                                                                                }
                                                                                            }
                                                                                            if(isExist==false){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue(Integer.toString(Integer.parseInt(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString())+1));
                                                                                                memberRef.child("오답 목록").child(problem).child(inCorrectAnswerNum).setValue(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer);
                                                                                            }
                                                                                            else{

                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    Toast.makeText(getApplicationContext(), "오답입니다.", Toast.LENGTH_SHORT).show();
                                                                                    finish();
                                                                                }
                                                                            }
                                                                            //4번
                                                                            else if(checkedId==R.id.rb_answer4){
                                                                                String choice="4";
                                                                                if(finalRightAnswer.equals(choice)){
                                                                                    tv_result.setText("정답입니다");
                                                                                    //조건이 참인 경우에만 점수 상승
                                                                                    memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            memberPointRef.setValue(Integer.toString(Integer.parseInt(dataSnapshot.getValue().toString())+1000));
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    finish();
                                                                                }
                                                                                else{
                                                                                    memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            if(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue()==null){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue("0");
                                                                                            }

                                                                                            String inCorrectAnswerNum=dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString();
                                                                                            android.util.Log.d("KDJ","inCorrectAnswerNum: "+inCorrectAnswerNum);
                                                                                            boolean isExist=false;
                                                                                            for(int i=0;i<Integer.parseInt(inCorrectAnswerNum);i++){
                                                                                                if(dataSnapshot.child("오답 목록").child(problem).child(Integer.toString(i)).getValue().toString().equals(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer)){
                                                                                                    isExist=true;
                                                                                                }
                                                                                            }
                                                                                            if(isExist==false){
                                                                                                memberRef.child("오답 목록").child(problem).child("오답 개수").setValue(Integer.toString(Integer.parseInt(dataSnapshot.child("오답 목록").child(problem).child("오답 개수").getValue().toString())+1));
                                                                                                memberRef.child("오답 목록").child(problem).child(inCorrectAnswerNum).setValue(randYear+"_"+randEpisode+"_"+get_problem_random+"_"+rightAnswer);
                                                                                            }
                                                                                            else{

                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                    Toast.makeText(getApplicationContext(), "오답입니다.", Toast.LENGTH_SHORT).show();
                                                                                    finish();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                }
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
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                //DB 구조 만들기
                        /*for(int i=1;i<4;i++){
                            for(int j=1;j<16;j++) {
                                //문제 form만 만들기
                                problems.child(Integer.toString(i)).child(Integer.toString(j)).setValue("");
                            }
                        }*/

                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "문제 설정을 하지 않음.", Toast.LENGTH_SHORT).show();
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

    }//showProblem

}