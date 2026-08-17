package com.example.gc_uiactivity.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;

/**
 * ViewModel for MainActivity
 * Handles fragment navigation and user data updates
 */
public class MainActivityViewModel extends ViewModel {

    private final DatabaseManager databaseManager;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<String> currentUserEmailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> currentMenuLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();

    public MainActivityViewModel() {
        databaseManager = new DatabaseManager();
        firebaseAuth = FirebaseAuth.getInstance();
        loadCurrentUserEmail();
    }

    /**
     * Load current user email from Firebase
     */
    private void loadCurrentUserEmail() {
        isLoadingLiveData.setValue(true);
        databaseManager.getCurrentUserEmail(new DatabaseManager.EmailCallback() {
            @Override
            public void onEmailReceived(String email) {
                isLoadingLiveData.setValue(false);
                // 로그인하지 않은 것은 오류가 아니다. 예전에는 여기서 에러를 세팅했고,
                // MainActivity 의 에러 옵저버가 다시 조회를 부르면서 무한 루프가 돌았다.
                // (조회가 DB 왕복이라 느렸을 때는 눈에 띄지 않았지만, 로컬 조회로
                //  바뀌자 초당 수십 번 Toast 를 띄워 앱이 ANR 로 멈췄다.)
                currentUserEmailLiveData.setValue(email);
            }
        });
    }

    /**
     * 에러 메시지를 한 번 표시한 뒤 지운다.
     */
    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }

    /**
     * Refresh user email data
     */
    public void refreshUserEmail() {
        loadCurrentUserEmail();
    }

    /**
     * Update current menu
     */
    public void updateCurrentMenu(String menuName) {
        currentMenuLiveData.setValue(menuName);
    }

    /**
     * Get current user email LiveData
     */
    public LiveData<String> getCurrentUserEmail() {
        return currentUserEmailLiveData;
    }

    /**
     * Get current menu LiveData
     */
    public LiveData<String> getCurrentMenu() {
        return currentMenuLiveData;
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
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Logout user
     */
    public void logout() {
        firebaseAuth.signOut();
        currentUserEmailLiveData.setValue(null);
    }
}
