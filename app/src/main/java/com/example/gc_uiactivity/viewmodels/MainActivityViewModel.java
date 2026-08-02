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
                if (email != null) {
                    currentUserEmailLiveData.setValue(email);
                } else {
                    errorMessageLiveData.setValue("Failed to load user email");
                }
            }
        });
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
