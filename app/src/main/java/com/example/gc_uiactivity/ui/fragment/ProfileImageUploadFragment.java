package com.example.gc_uiactivity.ui.fragment;

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

import com.example.gc_uiactivity.BuildConfig;
import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.ui.fragment.HomeFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
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
                String curMail = DatabaseManager.currentUserEmailKey();
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
        // 업로드는 Cloud Storage 전용이다. IMAGE_BASE_URL 이 지정되어 있으면 읽기는
        // 그 HTTP 호스트에서만 하므로(택일), 올려도 화면에는 반영되지 않는다.
        // 올린 뒤에 안 보이는 것보다 먼저 알리는 편이 낫다.
        String base = BuildConfig.IMAGE_BASE_URL;
        if (base != null && !base.trim().isEmpty()) {
            Toast.makeText(getActivity(),
                    "IMAGE_BASE_URL 이 설정되어 있어 업로드할 수 없습니다. "
                            + "사진은 그 주소에 직접 올려 주세요.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        // 로그아웃 상태에서는 user 가 null 이고, 아래 split 에서 죽는다.
        if (user == null) {
            Toast.makeText(getActivity(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
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
                    // "업로드 실패!" 만 띄우면 요금제 때문인지 네트워크 때문인지 알 수 없다.
                    String message = uploadFailureMessage(e);
                    Log.e("KDJ", "프로필 사진 업로드 실패: " + message, e);
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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

    /**
     * 업로드 실패 원인을 사용자가 읽을 수 있는 문장으로 바꾼다.
     *
     * 무료(Spark) 요금제에서는 Cloud Storage 가 HTTP 402 로 거부된다. 코드로 고칠 수
     * 없는 문제라 무엇을 해야 하는지까지 알려 준다.
     */
    private String uploadFailureMessage(Exception e) {
        if (e instanceof StorageException) {
            StorageException se = (StorageException) e;
            if (se.getHttpResultCode() == 402) {
                return "업로드 실패: 무료(Spark) 요금제에서는 Cloud Storage 를 쓸 수 없습니다. "
                        + "Blaze 로 업그레이드하거나 사진을 다른 곳에 올려 주세요.";
            }
            if (se.getErrorCode() == StorageException.ERROR_NOT_AUTHENTICATED
                    || se.getErrorCode() == StorageException.ERROR_NOT_AUTHORIZED) {
                return "업로드 실패: 저장소 접근 권한이 없습니다. 다시 로그인해 주세요.";
            }
            return "업로드 실패: 저장소 오류 (" + se.getHttpResultCode() + ")";
        }
        return "업로드 실패: " + (e.getMessage() == null ? "원인을 알 수 없습니다." : e.getMessage());
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


