package com.example.gc_uiactivity.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * ViewModel for authentication related operations (Login/SignUp)
 * Handles Firebase Auth state and user data management
 */
public class AuthViewModel extends ViewModel {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;

    private final MutableLiveData<FirebaseUser> currentUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // 여기서 기존 세션을 넣으면 안 된다. LoginActivity 는 이 LiveData 가 채워지는 것을
        // "방금 로그인에 성공했다"로 해석하므로, 앱을 다시 켰을 때 로그인 화면에 들어서자마자
        // 입력값이 빈 채로 성공 처리가 돌아 화면이 튕겨 나간다.
        // 기존 세션이 필요하면 isUserLoggedIn() 을 쓴다.
    }

    /**
     * Login with email and password
     */
    public void login(String email, String password) {
        isLoadingLiveData.setValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        currentUserLiveData.setValue(firebaseAuth.getCurrentUser());
                        errorMessageLiveData.setValue(null);
                    } else {
                        errorMessageLiveData.setValue(task.getException() != null ?
                            task.getException().getMessage() : "Login failed");
                    }
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorMessageLiveData.setValue(e.getMessage());
                });
    }

    /**
     * Sign up with email and password
     */
    public void signUp(String email, String password, String userName) {
        isLoadingLiveData.setValue(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        currentUserLiveData.setValue(user);

                        // 계정 레코드는 앱의 다른 화면들이 읽는 것과 같은 위치·같은 필드명으로
                        // 저장해야 한다. 이전에는 users/<uid>/{email,userName,points} 라는
                        // 별도 스키마로 써서 어느 화면도 읽지 못했다.
                        if (user != null) {
                            String emailKey = email.replaceAll("[.]", "");
                            DatabaseReference member = databaseReference
                                    .child("계정 정보").child(emailKey);
                            member.child("Email").setValue(email);
                            member.child("name").setValue(userName);
                            member.child("Points").setValue("0");
                            member.child("lockState").setValue("false");
                        }
                        errorMessageLiveData.setValue(null);
                    } else {
                        errorMessageLiveData.setValue(task.getException() != null ?
                            task.getException().getMessage() : "Sign up failed");
                    }
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorMessageLiveData.setValue(e.getMessage());
                });
    }

    /**
     * Logout current user
     */
    public void logout() {
        firebaseAuth.signOut();
        currentUserLiveData.setValue(null);
        errorMessageLiveData.setValue(null);
    }

    /**
     * Get current user LiveData
     */
    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUserLiveData;
    }

    /**
     * Get error message LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    /**
     * Get loading state LiveData
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}
