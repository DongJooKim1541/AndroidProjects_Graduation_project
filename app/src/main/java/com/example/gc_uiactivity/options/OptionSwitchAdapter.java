package com.example.gc_uiactivity.options;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.lock_screen.ScreenService;
import com.example.gc_uiactivity.lock_screen.ShowForegroundService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OptionSwitchAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> arr;
    private ListView myList;
    LayoutInflater layoutInflater;
    //생성자
    public OptionSwitchAdapter(Context context, int resource, ArrayList<String> arr, ListView myList){
        super(context,resource,arr);

        this.context=context;
        this.arr=arr;
        this.myList=myList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_switch_form, null);

        //인플레이션 작업 준비
        LayoutInflater linf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //시스템상에 있는 인플레이터를 가져다가 객체로 만드는 작업.

        //인플레이션 작업
        convertView = linf.inflate(R.layout.list_switch_form, null);

        String str = arr.get(position);

        //객체 받기
        TextView txt_form = (TextView) convertView.findViewById(R.id.list_switch_text);

        txt_form.setText("잠금화면 사용 여부");

        final Switch switchView = view.findViewById(R.id.switch_lock);

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
                        boolean finalisChecked=Boolean.parseBoolean((String)dataSnapshot.child(email).child("lockState").getValue());
                        switchView.setChecked(finalisChecked);

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
        //
        switchView.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){
            FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
            DatabaseReference rootRef=firebaseDatabase.getReference();
            DatabaseReference currRef=rootRef.child("현재 상태");
            DatabaseReference currMembers=currRef.child("계정 정보");

            final DatabaseReference stateInfoRef=rootRef.child("계정 정보");
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String email=(String)dataSnapshot.child("Email").getValue();
                        //stateInfoRef.child(email).child("lockState").setValue("");

                        stateInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean finalisChecked=Boolean.parseBoolean((String)dataSnapshot.child(email).child("lockState").getValue());
                                //lockSwitch.setChecked(finalisChecked);
                                //Log.d("KDJ", "finalisChecked:" + finalisChecked);
                                if(isChecked){
                                    Log.i("KDJ", "잠금화면 설정");
                                    Toast.makeText(context,"잠금화면을 설정하였습니다",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, ScreenService.class);
                                    context.startService(intent);
                                    onStartForegroundService();
                                    IsChecked("true");
                                }
                                else{
                                    Toast.makeText(context,"잠금화면 설정을 해제하였습니다",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(context, ScreenService.class);
                                    context.stopService(intent);
                                    onStopForegroundService();
                                    IsChecked("false");
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
        });
        return view;
    }



    public void onStartForegroundService(){
        Intent intent=new Intent(context, ShowForegroundService.class);
        intent.setAction("startForeground");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
            context.startForegroundService(intent);
        }
        else{
            context.startService(intent);
        }
    }
    public void onStopForegroundService(){
        Intent intent=new Intent(context,ShowForegroundService.class);
        intent.setAction("stopForeground");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
            context.startForegroundService(intent);
        }
        else{
            context.startService(intent);
        }
    }

/*    class SwitchListener implements CompoundButton.OnCheckedChangeListener{

    }*/
    public void IsChecked(final String ischecked) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");

        final DatabaseReference stateInfoRef = rootRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String email = (String) dataSnapshot.child("Email").getValue();
                stateInfoRef.child(email).child("lockState").setValue(ischecked);
                Log.d("KDJ", "stateInfoRef.child(email).child(lockState):" + ischecked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
