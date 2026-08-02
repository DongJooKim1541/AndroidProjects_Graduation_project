package com.example.gc_uiactivity.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gc_uiactivity.ui.fragment.AnswerNoteYearFragment;
import com.example.gc_uiactivity.ui.fragment.CashFragment;
import com.example.gc_uiactivity.ui.fragment.HomeFragment;
import com.example.gc_uiactivity.ui.fragment.OptionFragment;
import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.google.firebase.database.DataSnapshot;

public class MainActivity extends AppCompatActivity {

    HomeFragment homeFragment;
    OptionFragment optionFragment;
    AnswerNoteYearFragment answerNoteYearFragment;
    CashFragment cashFragment;

    // Firebase 매니저 인스턴스
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 매니저 초기화
        databaseManager = new DatabaseManager();

        // 액션바 컨트롤
        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.drawable.logo_image);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        homeFragment = new HomeFragment();
        optionFragment = new OptionFragment();
        answerNoteYearFragment = new AnswerNoteYearFragment();
        cashFragment = new CashFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, homeFragment).commit();
    }

    // 액션바 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    // 액션바 리스너
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, homeFragment).commit();
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
     * 프래그먼트 네비게이션 처리 (중복 코드 제거)
     */
    private void handleFragmentNavigation(final android.app.Fragment targetFragment, String menuName) {
        databaseManager.getCurrentUserEmail(new DatabaseManager.EmailCallback() {
            @Override
            public void onEmailReceived(String email) {
                if (email != null) {
                    String eDataStr = email.replaceAll("[.]", "");
                    // 사용자 정보 확인 (선택적)
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, (androidx.fragment.app.Fragment) targetFragment).commit();
                } else {
                    Toast.makeText(MainActivity.this, "로그인을 해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
