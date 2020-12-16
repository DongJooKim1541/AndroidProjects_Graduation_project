package com.example.gc_uiactivity.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gc_uiactivity.user_state.LoginActivity;
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

public class HomeFragment extends Fragment implements View.OnClickListener{
    ImageView iv_user_state;
    ImageView iv_user_image;
    ImageView iv_introduce;
    ImageView iv_event;
    ImageView iv_service1;
    ImageView iv_service2;
    ImageView iv_service3;
    ImageView iv_advertizement;

    TextView tv_username;
    TextView tv_lock_state;
    TextView tv_cash;
    TextView tv_type;

    IntroduceFragment introduceFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.homefragment,container,false);

        iv_user_state=v.findViewById(R.id.iv_user_state);
        iv_user_image=v.findViewById(R.id.iv_user_image);

        iv_introduce=v.findViewById(R.id.iv_introduce);
        iv_event=v.findViewById(R.id.iv_event);
        iv_service1=v.findViewById(R.id.iv_service1);
        iv_service2=v.findViewById(R.id.iv_service2);
        iv_service3=v.findViewById(R.id.iv_service3);
        iv_advertizement=v.findViewById(R.id.iv_advertizement);

        iv_introduce.setOnClickListener(this);
        iv_event.setOnClickListener(this);
        iv_service1.setOnClickListener(this);
        iv_service2.setOnClickListener(this);
        iv_service3.setOnClickListener(this);
        iv_advertizement.setOnClickListener(this);

        tv_username=v.findViewById(R.id.tv_username);
        tv_lock_state=v.findViewById(R.id.tv_lock_state);
        tv_cash=v.findViewById(R.id.tv_cash);
        tv_type=v.findViewById(R.id.tv_type);

        introduceFragment=new IntroduceFragment();

        CheckTypesTask checkTypesTask=new CheckTypesTask();
        checkTypesTask.execute();

        return v;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_introduce:
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,introduceFragment).commit();
                break;
            case R.id.iv_event:
                Toast.makeText(getActivity(), "이벤트 준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_service1:
                Toast.makeText(getActivity(), "서비스 준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_service2:
                Toast.makeText(getActivity(), "서비스 준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_service3:
                Toast.makeText(getActivity(), "서비스 준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_advertizement:
                Toast.makeText(getActivity(), "광고 준비중입니다..", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private class CheckTypesTask extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog=new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("로딩중...");
            //show dialog
            progressDialog.show();
            UserProfile();
            UserImageDownload();
            UserState();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
                for(int i=0;i<5;i++){
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

    public void UserProfile(){
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference rootRef=firebaseDatabase.getReference();
        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        final DatabaseReference stateInfoRef=rootRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String email=(String)dataSnapshot.child("Email").getValue();
                stateInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(email!=null){
                            String name=(String)dataSnapshot.child(email).child("name").getValue();
                            Log.d("KDJ", "name:" + name);
                            tv_username.setText(name);
                            String lockstate=(String) dataSnapshot.child(email).child("lockState").getValue();
                            if(lockstate.equals("true")){
                                tv_lock_state.setText("사용");
                            }
                            else{
                                tv_lock_state.setText("미사용");
                            }
                            String points=(String)dataSnapshot.child(email).child("Points").getValue();
                            tv_cash.setText(points);
                            String type=(String)dataSnapshot.child(email).child("problem_to_Korean").getValue();
                            tv_type.setText(type);
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

    public void UserImageDownload(){
        //파이어베이스 실시간 dB관리 객체 열어오기.
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

        //저장시킬 노드 탐조객체 가져오기
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email=(String)dataSnapshot.child("Email").getValue();
                Log.d("KDJ", "email:" + email);
                if(email!=null){
                    String[] userInfo=email.split("@");
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://charged-dialect-285301.appspot.com/");
                    StorageReference pathReference = storageRef.child("images/users/"+userInfo[0]);
                    Log.d("KDJ", "pathReference:" + pathReference);
                    //메모리에 다운로드
                    final long ONE_MEGABYTE = 1024 * 1024;
                    pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for "images/island.jpg" is returns, use this as needed
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            iv_user_image.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(), "프로필 사진이 지정되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void UserState(){

        final int imageResources[]={R.drawable.login_icon,R.drawable.logout_icon};

        //파이어베이스 실시간 dB관리 객체 열어오기.
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

        //저장시킬 노드 탐조객체 가져오기
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email= (String) dataSnapshot.child("Email").getValue();
                if(email!=null){
                    iv_user_state.setImageResource(imageResources[1]);
                }
                else{
                    iv_user_state.setImageResource(imageResources[0]);
                }
                Log.d("KDJ", "HomeEmail:" + email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        iv_user_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //파이어베이스 실시간 dB관리 객체 열어오기.
                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

                //저장시킬 노드 탐조객체 가져오기
                final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
                DatabaseReference currRef=rootRef.child("현재 상태");
                DatabaseReference currMembers=currRef.child("계정 정보");

                currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email=(String)dataSnapshot.child("Email").getValue();
                        if(email!=null){
                            tv_username.setText("");
                            tv_lock_state.setText("");
                            tv_cash.setText("");
                            tv_type.setText("");
                            iv_user_image.setImageResource(R.drawable.user_icon);
                            iv_user_state.setImageResource(imageResources[0]);
                            ChangeEmail();

                        }
                        else{
                            Intent intent= new Intent(getActivity(), LoginActivity.class);
                            //액티비티 상위 스택 지우기
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
    public void ChangeEmail(){
        //파이어베이스 실시간 dB관리 객체 열어오기.
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

        //저장시킬 노드 탐조객체 가져오기
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드
        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");
        currMembers.child("Email").setValue(null);
    }
}
