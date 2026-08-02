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
        currentUserLiveData.setValue(firebaseAuth.getCurrentUser());
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

                        // Save user info to database
                        if (user != null) {
                            String userId = user.getUid();
                            String emailFormatted = email.replaceAll("[.]", "");

                            databaseReference.child("users").child(userId)
                                    .child("email").setValue(emailFormatted);
                            databaseReference.child("users").child(userId)
                                    .child("userName").setValue(userName);
                            databaseReference.child("users").child(userId)
                                    .child("points").setValue(0);
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
