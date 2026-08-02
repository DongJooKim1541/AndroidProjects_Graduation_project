package com.example.gc_uiactivity.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gc_uiactivity.ui.fragment.AnswerNoteYearFragment;
import com.example.gc_uiactivity.ui.fragment.CashFragment;
import com.example.gc_uiactivity.ui.fragment.HomeFragment;
import com.example.gc_uiactivity.ui.fragment.OptionFragment;
import com.example.gc_uiactivity.viewmodels.MainActivityViewModel;
import com.example.gc_uiactivity.viewmodels.ViewModelFactory;
import com.google.firebase.database.DataSnapshot;

/**
 * MainActivity with MVVM architecture
 * Handles fragment navigation using ViewModel
 */
public class MainActivity extends AppCompatActivity {

    // Fragments
    private HomeFragment homeFragment;
    private OptionFragment optionFragment;
    private AnswerNoteYearFragment answerNoteYearFragment;
    private CashFragment cashFragment;

    // ViewModel
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new ViewModelFactory())
                .get(MainActivityViewModel.class);

        // Setup ActionBar
        setupActionBar();

        // Initialize Fragments
        initializeFragments();

        // Observe ViewModel data
        observeViewModel();
    }

    /**
     * Setup ActionBar
     */
    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setIcon(R.drawable.logo_image);
            ab.setDisplayUseLogoEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * Initialize Fragments
     */
    private void initializeFragments() {
        homeFragment = new HomeFragment();
        optionFragment = new OptionFragment();
        answerNoteYearFragment = new AnswerNoteYearFragment();
        cashFragment = new CashFragment();

        // Show home fragment by default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, homeFragment)
                .commit();
    }

    /**
     * Observe ViewModel LiveData
     */
    private void observeViewModel() {
        viewModel.getCurrentUserEmail().observe(this, email -> {
            if (email != null) {
                // User email loaded successfully
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                viewModel.refreshUserEmail();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Handle loading state if needed
        });
    }

    /**
     * Create ActionBar menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    /**
     * Handle ActionBar menu selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                viewModel.updateCurrentMenu("홈");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, homeFragment)
                        .commit();
                return true;
            case R.id.action_option:
                handleFragmentNavigation(optionFragment, "옵션");
                return true;
            case R.id.action_study:
                handleFragmentNavigation(answerNoteYearFragment, "학습");
                return true;
            case R.id.action_cash:
                handleFragmentNavigation(cashFragment, "캐시");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handle fragment navigation with authentication check
     */
    private void handleFragmentNavigation(androidx.fragment.app.Fragment targetFragment, String menuName) {
        if (viewModel.isUserAuthenticated()) {
            viewModel.updateCurrentMenu(menuName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, targetFragment)
                    .commit();
        } else {
            Toast.makeText(MainActivity.this, "로그인을 해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
            viewModel.refreshUserEmail();
        }
    }
}
