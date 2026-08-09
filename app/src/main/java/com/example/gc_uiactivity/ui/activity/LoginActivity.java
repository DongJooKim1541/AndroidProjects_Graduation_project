package com.example.gc_uiactivity.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.viewmodels.AuthViewModel;
import com.example.gc_uiactivity.viewmodels.ViewModelFactory;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoginActivity with MVVM architecture
 * Handles user login and navigation to MainActivity
 */
public class LoginActivity extends AppCompatActivity {

    private ImageView ivLoginImg;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogIn;
    private Button buttonSignUp;

    // ViewModel
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this, new ViewModelFactory())
                .get(AuthViewModel.class);

        // Initialize UI components
        initializeUI();

        // Setup click listeners
        setupClickListeners();

        // Observe ViewModel data
        observeViewModel();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        ivLoginImg = findViewById(R.id.iv_login_img);
        ivLoginImg.setImageResource(R.drawable.login_lt);

        editTextEmail = findViewById(R.id.edittext_email);
        editTextPassword = findViewById(R.id.edittext_password);

        buttonLogIn = findViewById(R.id.btn_login);
        buttonSignUp = findViewById(R.id.btn_signup);
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        buttonLogIn.setOnClickListener(v -> handleLogin());
        buttonSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    /**
     * Handle login button click
     */
    private void handleLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.login(email, password);
    }

    /**
     * Navigate to SignUp Activity
     */
    private void navigateToSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    /**
     * Observe ViewModel LiveData changes
     */
    private void observeViewModel() {
        // Observe current user changes
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                handleLoginSuccess(user);
            }
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            // Update UI to show/hide loading indicator
            buttonLogIn.setEnabled(!isLoading);
            buttonSignUp.setEnabled(!isLoading);
        });
    }

    /**
     * Handle successful login
     */
    private void handleLoginSuccess(FirebaseUser user) {
        String email = editTextEmail.getText().toString();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("Email", email);
        startActivity(intent);

        finish();
    }
}
