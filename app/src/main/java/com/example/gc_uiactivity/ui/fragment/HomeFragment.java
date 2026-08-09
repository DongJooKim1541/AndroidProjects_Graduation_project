package com.example.gc_uiactivity.ui.fragment;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.ui.activity.LoginActivity;
import com.example.gc_uiactivity.viewmodels.MainActivityViewModel;
import com.example.gc_uiactivity.viewmodels.ViewModelFactory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * HomeFragment with MVVM architecture
 * Displays user profile and main menu options
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "HomeFragment";

    private ImageView ivUserState;
    private ImageView ivUserImage;
    private ImageView ivIntroduce;
    private ImageView ivEvent;
    private ImageView ivService1;
    private ImageView ivService2;
    private ImageView ivService3;
    private ImageView ivAdvertisement;

    private TextView tvUsername;
    private TextView tvLockState;
    private TextView tvCash;
    private TextView tvType;

    private IntroduceFragment introduceFragment;

    // ViewModel
    private MainActivityViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.homefragment, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory())
                .get(MainActivityViewModel.class);

        // Initialize UI components
        initializeUI(v);

        // Setup click listeners
        setupClickListeners();

        // Load user data
        new LoadUserDataTask().execute();

        return v;
    }

    /**
     * Initialize UI components
     */
    private void initializeUI(View v) {
        ivUserState = v.findViewById(R.id.iv_user_state);
        ivUserImage = v.findViewById(R.id.iv_user_image);

        ivIntroduce = v.findViewById(R.id.iv_introduce);
        ivEvent = v.findViewById(R.id.iv_event);
        ivService1 = v.findViewById(R.id.iv_service1);
        ivService2 = v.findViewById(R.id.iv_service2);
        ivService3 = v.findViewById(R.id.iv_service3);
        ivAdvertisement = v.findViewById(R.id.iv_advertizement);

        tvUsername = v.findViewById(R.id.tv_username);
        tvLockState = v.findViewById(R.id.tv_lock_state);
        tvCash = v.findViewById(R.id.tv_cash);
        tvType = v.findViewById(R.id.tv_type);

        introduceFragment = new IntroduceFragment();
    }

    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        ivIntroduce.setOnClickListener(this);
        ivEvent.setOnClickListener(this);
        ivService1.setOnClickListener(this);
        ivService2.setOnClickListener(this);
        ivService3.setOnClickListener(this);
        ivAdvertisement.setOnClickListener(this);
        ivUserState.setOnClickListener(this);
    }

    /**
     * Handle UI component click events
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_introduce:
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_frame, introduceFragment)
                        .commit();
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
                Toast.makeText(getActivity(), "광고 준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_user_state:
                handleUserStateClick();
                break;
        }
    }

    /**
     * AsyncTask for loading user data with progress dialog
     */
    private class LoadUserDataTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("로딩중...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            loadUserProfile();
            loadUserImage();
            loadUserState();
            super.onPostExecute(result);
        }
    }

    /**
     * Load user profile information
     */
    private void loadUserProfile() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");
        final DatabaseReference stateInfoRef = rootRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String email = (String) dataSnapshot.child("Email").getValue();
                stateInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (email != null) {
                            String name = (String) dataSnapshot.child(email).child("name").getValue();
                            String lockstate = (String) dataSnapshot.child(email).child("lockState").getValue();
                            String points = (String) dataSnapshot.child(email).child("Points").getValue();
                            String type = (String) dataSnapshot.child(email).child("problem_to_Korean").getValue();

                            tvUsername.setText(name != null ? name : "");
                            tvLockState.setText("true".equals(lockstate) ? "사용" : "미사용");
                            tvCash.setText(points != null ? points : "0");
                            tvType.setText(type != null ? type : "");

                            Log.d("KDJ", "name:" + name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("KDJ", "Error loading user profile", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("KDJ", "Error loading email", databaseError.toException());
            }
        });
    }

    /**
     * Load user profile image from Firebase Storage
     */
    private void loadUserImage() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = (String) dataSnapshot.child("Email").getValue();
                Log.d("KDJ", "email:" + email);
                if (email != null) {
                    String[] userInfo = email.split("@");
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://charged-dialect-285301.appspot.com/");
                    StorageReference pathReference = storageRef.child("images/users/" + userInfo[0]);

                    final long ONE_MEGABYTE = 1024 * 1024;
                    pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            ivUserImage.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("KDJ", "Failed to download user image", exception);
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "프로필 사진이 지정되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("KDJ", "Error loading user image", databaseError.toException());
            }
        });
    }

    /**
     * Load and display user state (login/logout)
     */
    private void loadUserState() {
        // 로그인 여부를 확인하기 전에는 로그아웃이 아니라 로그인 아이콘이 기본이다.
        // Firebase 조회가 실패하거나 응답이 늦으면 이 값이 그대로 남는다.
        ivUserState.setImageResource(R.drawable.login_icon);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = (String) dataSnapshot.child("Email").getValue();
                boolean loggedIn = email != null;
                ivUserState.setImageResource(
                        loggedIn ? R.drawable.logout_icon : R.drawable.login_icon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 조회 실패는 로그인 상태를 확인하지 못한 것이므로 로그인 아이콘을 유지한다.
                Log.e(TAG, "Error loading user state", databaseError.toException());
                ivUserState.setImageResource(R.drawable.login_icon);
            }
        });
    }

    /**
     * Handle user state button click (login/logout)
     */
    private void handleUserStateClick() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = (String) dataSnapshot.child("Email").getValue();
                if (email != null) {
                    // Logout
                    clearUserDisplay();
                    changeEmail();
                } else {
                    // Navigate to login
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("KDJ", "Error handling user state click", databaseError.toException());
            }
        });
    }

    /**
     * Clear user display information
     */
    private void clearUserDisplay() {
        tvUsername.setText("");
        tvLockState.setText("");
        tvCash.setText("");
        tvType.setText("");
        ivUserImage.setImageResource(R.drawable.user_icon);
        ivUserState.setImageResource(R.drawable.login_icon);
    }

    /**
     * Clear email from Firebase (logout)
     */
    private void changeEmail() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");
        currMembers.child("Email").setValue(null);
    }
}
