package com.example.gc_uiactivity.options;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileImageUploadFragment extends Fragment {
    HomeFragment homeFragment;
    Uri filePath;
    /*ImageButton btn_choose;
    ImageButton btn_upload;*/
    CardView cv_choose;
    CardView cv_upload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.profile_upload_image_fragment,container,false);

        homeFragment=new HomeFragment();
        cv_choose=v.findViewById(R.id.cv_choose);
        cv_upload=v.findViewById(R.id.cv_upload);
        SetUserImg();

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

    public void SetUserImg(){
        //파이어베이스 실시간 dB관리 객체 열어오기.
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

        //저장시킬 노드 탐조객체 가져오기
        final DatabaseReference rootRef=firebaseDatabase.getReference();//()안에 아무것도 안쓰면 최상위 노드

        DatabaseReference curRef=rootRef.child("현재 상태").child("계정 정보");

        curRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String curMail=(String)dataSnapshot.child("Email").getValue();
                SetImage(curMail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void SetImage(final String user){
        cv_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"이미지를 선택하세요."),0);
            }
        });
        cv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage(user);
            }
        });
    }

    public void UploadImage(String user){
        if(filePath!=null){
            /*CheckTypesTask checkTypesTask=new CheckTypesTask();
            checkTypesTask.execute();*/
            String[] userInfo=user.split("@");

            //가장 먼저, FirebaseStorage 인스턴스를 생성한다
            // getInstance() 파라미터에 들어가는 값은 firebase console에서 storage를 추가하면 상단에 gs:// 로 시작하는 스킴을 확인할 수 있다
            FirebaseStorage storage=FirebaseStorage.getInstance();
            //storage 주소와 폴더 파일명 지정
            StorageReference storageRef = storage.getReferenceFromUrl("gs://charged-dialect-285301.appspot.com").child("images/users/"+userInfo[0]);
            //업로드
            storageRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,homeFragment).commit();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")
                    double progresss=(100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                }
            });
        }
        else{
            Toast.makeText(getActivity(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        //resultCode -1은 액티비티에서 RESULT_OK에 해당됨
        if(requestCode == 0 && resultCode == -1){
            filePath = data.getData();
            Log.d("KDJ", "filePath:" + filePath);
            Toast.makeText(getActivity(), "이미지를 선택하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
