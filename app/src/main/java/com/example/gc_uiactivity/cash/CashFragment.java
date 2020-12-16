package com.example.gc_uiactivity.cash;

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
import com.example.gc_uiactivity.home.HomeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
                final String email=(String)dataSnapshot.child("Email").getValue();
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
                String email=(String)dataSnapshot.child("Email").getValue();
                android.util.Log.d("KDJ","cash_email: "+email);
                final DatabaseReference memberPointRef=stateInfoRef.child(email).child("Points");
                memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        memberPointRef.setValue(Integer.toString(Integer.parseInt(dataSnapshot.getValue().toString())-points));
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
