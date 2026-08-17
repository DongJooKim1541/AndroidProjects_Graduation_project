package com.example.gc_uiactivity.ui.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.example.gc_uiactivity.ui.fragment.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class CashFragment extends Fragment {
    HomeFragment homeFragment;
    CashFragment cashFragment;
    TextView tv_currentpoint;
    ImageView iv_1000_point,iv_3000_point,iv_5000_point,iv_10000_point,iv_50000_point;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        homeFragment=new HomeFragment();
        cashFragment=new CashFragment();
        View v=inflater.inflate(R.layout.cashfragment,container,false);

        tv_currentpoint=v.findViewById(R.id.tv_currentpoint);
        iv_1000_point=v.findViewById(R.id.iv_1000_point);
        iv_3000_point=v.findViewById(R.id.iv_3000_point);
        iv_5000_point=v.findViewById(R.id.iv_5000_point);
        iv_10000_point=v.findViewById(R.id.iv_10000_point);
        iv_50000_point=v.findViewById(R.id.iv_50000_point);

        final CheckTypesTask checkTypesTask=new CheckTypesTask();

        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference rootRef=firebaseDatabase.getReference();
        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        final DatabaseReference stateInfoRef=rootRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String email = DatabaseManager.currentUserEmailKey();
                if (email == null) {
                    // 로그아웃 상태에서는 child(null) 이 되어 앱이 죽었다.
                    tv_currentpoint.setText("로그인이 필요합니다");
                    return;
                }
                stateInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String points=(String)dataSnapshot.child(email).child("Points").getValue();
                        if(points!=null){
                            tv_currentpoint.setText("현재 포인트: "+points+"점");
                        }
                        else{
                            tv_currentpoint.setText("포인트가 존재하지 않습니다");
                        }
                        ImageView.OnClickListener onClickListener=new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (v.getId()){
                                    case R.id.iv_1000_point:
                                        if(Integer.parseInt(points)>=1000){
                                            checkTypesTask.execute();
                                            UsePoints(1000);
                                            Toast.makeText(getActivity(),"1000 Point 차감되었습니다.",Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,cashFragment).commit();
                                        }
                                        else{
                                            Toast.makeText(getActivity(),"잔액이 부족합니다.",Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case R.id.iv_3000_point:
                                        if(Integer.parseInt(points)>=3000){
                                            checkTypesTask.execute();
                                            UsePoints(3000);
                                            Toast.makeText(getActivity(),"3000 Point 차감되었습니다.",Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,cashFragment).commit();
                                        }
                                        else{
                                            Toast.makeText(getActivity(),"잔액이 부족합니다.",Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case R.id.iv_5000_point:
                                        if(Integer.parseInt(points)>=5000){
                                            checkTypesTask.execute();
                                            UsePoints(5000);
                                            Toast.makeText(getActivity(),"5000 Point 차감되었습니다.",Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,cashFragment).commit();
                                        }
                                        else{
                                            Toast.makeText(getActivity(),"잔액이 부족합니다.",Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case R.id.iv_10000_point:
                                        if(Integer.parseInt(points)>=10000){
                                            checkTypesTask.execute();
                                            UsePoints(10000);
                                            Toast.makeText(getActivity(),"10000 Point 차감되었습니다.",Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,cashFragment).commit();
                                        }
                                        else{
                                            Toast.makeText(getActivity(),"잔액이 부족합니다.",Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case R.id.iv_50000_point:
                                        if(Integer.parseInt(points)>=50000){
                                            checkTypesTask.execute();
                                            UsePoints(50000);
                                            Toast.makeText(getActivity(),"50000 Point 차감되었습니다.",Toast.LENGTH_SHORT).show();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,cashFragment).commit();
                                        }
                                        else{
                                            Toast.makeText(getActivity(),"잔액이 부족합니다.",Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                }
                            }
                        };
                        iv_1000_point.setOnClickListener(onClickListener);
                        iv_3000_point.setOnClickListener(onClickListener);
                        iv_5000_point.setOnClickListener(onClickListener);
                        iv_10000_point.setOnClickListener(onClickListener);
                        iv_50000_point.setOnClickListener(onClickListener);

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
                    Thread.sleep(200);
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

    public void UsePoints(final int points){
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        final DatabaseReference rootRef=firebaseDatabase.getReference();

        DatabaseReference currRef=rootRef.child("현재 상태");
        DatabaseReference currMembers=currRef.child("계정 정보");

        final DatabaseReference stateInfoRef=rootRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = DatabaseManager.currentUserEmailKey();
                android.util.Log.d("KDJ","cash_email: "+email);
                if (email == null) {
                    return;
                }
                final DatabaseReference memberPointRef=stateInfoRef.child(email).child("Points");
                // 차감도 트랜잭션으로 한다. 읽고 나서 쓰면 동시에 두 번 교환할 때
                // 한 번만 차감되어 포인트를 공짜로 쓸 수 있다. 잔액도 여기서 다시 본다.
                memberPointRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Object raw = currentData.getValue();
                        int current;
                        try {
                            current = raw == null ? 0 : Integer.parseInt(raw.toString().trim());
                        } catch (NumberFormatException e) {
                            current = 0;
                        }
                        if (current < points) {
                            // 잔액이 모자라면 아무것도 바꾸지 않는다.
                            return Transaction.abort();
                        }
                        currentData.setValue(Integer.toString(current - points));
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            android.util.Log.e("KDJ", "포인트 차감 실패", error.toException());
                        } else if (!committed) {
                            Toast.makeText(getActivity(), "잔액이 부족합니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}


