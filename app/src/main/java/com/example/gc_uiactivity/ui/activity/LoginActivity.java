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
import com.example.gc_uiactivity.firebase.DatabaseManager;
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
        // 입력창이 아니라 인증된 사용자에서 이메일을 가져온다. 입력창을 쓰면 이 콜백이
        // 사용자의 타이핑 없이 불릴 때 빈 값이 저장된다.
        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            email = editTextEmail.getText().toString();
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 앱 전체가 로그인 여부를 "현재 상태/계정 정보/Email" 로 판단한다.
        // 이 기록이 없으면 인증에 성공해도 계속 비로그인으로 보인다.
        new DatabaseManager().setCurrentUserEmail(email);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("Email", email);
        startActivity(intent);

        finish();
    }
}
