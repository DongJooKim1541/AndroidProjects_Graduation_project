package com.example.gc_uiactivity.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.example.gc_uiactivity.firebase.ImageSource;
import com.example.gc_uiactivity.viewmodels.LockScreenViewModel;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * LockScreenActivity with MVVM architecture
 * Displays quiz problems and handles user answers
 */
public class LockScreenActivity extends AppCompatActivity {

    private ImageView ivProblemImage;
    private TextView tvProblem;
    private RadioGroup rgAnswer;
    private RadioButton rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4;
    private TextView tvResult;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private Random random = new Random();
    private DatabaseManager databaseManager;

    /**
     * 문제 이미지 내려받기에 실패했을 때 다른 문제로 다시 시도할 수 있는 횟수.
     * 이전에는 실패할 때마다 무조건 showProblem() 을 다시 불러서, 스토리지가 계속
     * 실패하면 끝없이 재귀했다(20초에 재로딩 11회, StorageException 504회 관측).
     */
    private static final int MAX_IMAGE_RETRIES = 3;
    private int imageRetryCount = 0;

    // ViewModel
    private LockScreenViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new ViewModelFactory())
                .get(LockScreenViewModel.class);

        // Initialize database manager
        databaseManager = new DatabaseManager();

        // Initialize UI components
        initializeUI();

        // Setup window flags
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Observe ViewModel data
        observeViewModel();

        // Load initial data
        loadInitialData();
    }

    /**
     * Initialize UI components
     */
    private void initializeUI() {
        tvProblem = findViewById(R.id.tv_problem);
        ivProblemImage = findViewById(R.id.problem_image);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        rbAnswer1 = findViewById(R.id.rb_answer1);
        rbAnswer2 = findViewById(R.id.rb_answer2);
        rbAnswer3 = findViewById(R.id.rb_answer3);
        rbAnswer4 = findViewById(R.id.rb_answer4);

        rgAnswer = findViewById(R.id.rg_answer);
        tvResult = findViewById(R.id.tv_result);

        // Setup answer selection listener
        rgAnswer.setOnCheckedChangeListener((group, checkedId) -> handleAnswerSelection(checkedId));
    }

    /**
     * Handle scale gesture for image zoom
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * Scale gesture listener for image zooming
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= mScaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 2.0f));

            ivProblemImage.setScaleX(mScaleFactor);
            ivProblemImage.setScaleY(mScaleFactor * 1.1f);
            return true;
        }
    }

    /**
     * Load initial quiz data with progress dialog
     */
    private void loadInitialData() {
        new LoadDataTask().execute();
    }

    /**
     * AsyncTask for loading initial data
     */
    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(LockScreenActivity.this);

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
            showProblem();
            super.onPostExecute(result);
        }
    }

    /**
     * Load and display problem
     */
    private void showProblem() {
        databaseManager.getCurrentUserEmail(new DatabaseManager.EmailCallback() {
            @Override
            public void onEmailReceived(String email) {
                if (email != null) {
                    loadProblemData(email);
                } else {
                    Toast.makeText(LockScreenActivity.this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Load problem data from Firebase
     */
    private void loadProblemData(final String email) {
        databaseManager.getUserInfo(email, new DatabaseManager.UserInfoCallback() {
            @Override
            public void onUserInfoReceived(DataSnapshot dataSnapshot) {
                final String problem = DatabaseManager.stringOf(dataSnapshot, "ChoiceProblem");
                final String problemToKorean = DatabaseManager.stringOf(dataSnapshot, "problem_to_Korean");

                // 이전에는 ChoiceProblem 은 있는데 problem_to_Korean 이 없으면 안내도 없이
                // 아무 일도 일어나지 않았다. 둘 중 하나라도 없으면 설정하라고 알린다.
                if (problem == null || problemToKorean == null) {
                    Toast.makeText(LockScreenActivity.this, "문제 설정을 하지 않음.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("KDJ", "problem:" + problem);
                loadProblemYears(email, problem, problemToKorean);
            }
        });
    }

    /**
     * 스냅샷의 자식 키 중 하나를 무작위로 고른다. 자식이 없으면 null.
     *
     * 실제 키를 보고 고른다. 이전에는 연도를 {@code nextInt(개수) + 2018},
     * 회차를 {@code nextInt(개수) + 1} 로 만들었는데, 이는 키가 2018 부터 1 씩
     * 이어진다는 가정이다. 2019 년만 등록된 종목(산업기사·기능사)에서는 개수가 1 이라
     * 항상 "2018" 이 나왔고, 그런 노드는 없으므로 잠금화면에 문제가 아예 뜨지 않았다.
     */
    private String randomKeyOf(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.hasChildren()) {
            return null;
        }
        List<String> keys = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            keys.add(child.getKey());
        }
        return keys.get(random.nextInt(keys.size()));
    }

    /**
     * Load problem years
     */
    private void loadProblemYears(final String email, final String problem, final String problemToKorean) {
        databaseManager.getProblemYears(problem, new DatabaseManager.QuizCallback() {
            @Override
            public void onQuizzesReceived(DataSnapshot dataSnapshot) {
                final String randYear = randomKeyOf(dataSnapshot);
                if (randYear == null) {
                    Toast.makeText(LockScreenActivity.this,
                            "등록된 문제가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("KDJ", "randYear: " + randYear);
                loadProblemEpisode(email, problem, problemToKorean, randYear);
            }
        });
    }

    /**
     * Load problem episode
     */
    private void loadProblemEpisode(final String email, final String problem,
                                    final String problemToKorean, final String year) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference problemRef = rootRef.child("문제 종류");
        final DatabaseReference problemYear = problemRef.child(problem).child("Year").child(year);

        problemYear.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String randEpisode = randomKeyOf(dataSnapshot);
                if (randEpisode == null) {
                    Toast.makeText(LockScreenActivity.this,
                            "등록된 문제가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("KDJ", "randEpisode: " + randEpisode);
                loadProblemImage(email, problem, problemToKorean, year, randEpisode);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("KDJ", "Error loading episodes", databaseError.toException());
            }
        });
    }

    /**
     * Load problem image and setup answer listener
     */
    private void loadProblemImage(final String email, final String problem, final String problemToKorean,
                                  final String year, final String episode) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference problemRef = rootRef.child("문제 종류");
        final DatabaseReference problemEpisode = problemRef.child(problem)
                .child("Year").child(year).child(episode);

        problemEpisode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String randImagePath = randomKeyOf(dataSnapshot);
                if (randImagePath != null) {
                    Log.d("KDJ", "randImagePath: " + randImagePath);

                    // Get correct answer
                    final String correctAnswer = DatabaseManager.stringOf(dataSnapshot, randImagePath);
                    if (correctAnswer == null) {
                        Log.e("KDJ", "정답이 비어 있는 문제: " + randImagePath);
                        return;
                    }

                    // Load image from storage
                    loadImageFromStorage(problem, year, episode, randImagePath);

                    setupAnswerListener(email, problem, year, episode, randImagePath, correctAnswer, problemToKorean);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("KDJ", "Error loading problem image", databaseError.toException());
            }
        });
    }

    /**
     * Load image from Firebase Storage
     */
    private void loadImageFromStorage(String problem, String year, String episode, String number) {
        ImageSource.load(ImageSource.problemPath(problem, year, episode, number), new ImageSource.Callback() {
            @Override
            public void onLoaded(Bitmap bitmap) {
                imageRetryCount = 0;
                ivProblemImage.setImageBitmap(bitmap);
            }

            @Override
            public void onFailed(Exception error) {
                // 다른 문제로 몇 번만 다시 시도한다. 무제한으로 다시 부르면
                // 이미지 서버가 계속 실패할 때 재귀가 끝나지 않는다.
                if (imageRetryCount < MAX_IMAGE_RETRIES) {
                    imageRetryCount++;
                    Toast.makeText(LockScreenActivity.this, "다운로드 실패. 다른 문제를 불러옵니다.",
                            Toast.LENGTH_SHORT).show();
                    showProblem();
                } else {
                    imageRetryCount = 0;
                    Toast.makeText(LockScreenActivity.this,
                            "문제 이미지를 불러올 수 없습니다. 네트워크와 이미지 저장소 설정을 확인하세요.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Setup answer selection listener
     */
    private void setupAnswerListener(final String email, final String problem, final String year,
                                     final String episode, final String problemNumber,
                                     final String correctAnswer, final String problemToKorean) {
        tvProblem.setText(problemToKorean + " " + year + "년 " + episode + "회 " + problemNumber + "번");

        // 정답과 오답 기록용 정보를 ViewModel 로 넘긴다.
        // 이 호출이 없으면 submitAnswer 가 정답을 알 수 없어 항상 오답으로 채점된다.
        String problemInfo = year + "_" + episode + "_" + problemNumber + "_" + correctAnswer;
        viewModel.setQuiz(problem, problemInfo, correctAnswer);
    }

    /**
     * Handle answer selection
     */
    private void handleAnswerSelection(int checkedId) {
        String selectedChoice = null;

        if (checkedId == R.id.rb_answer1) {
            selectedChoice = "1";
        } else if (checkedId == R.id.rb_answer2) {
            selectedChoice = "2";
        } else if (checkedId == R.id.rb_answer3) {
            selectedChoice = "3";
        } else if (checkedId == R.id.rb_answer4) {
            selectedChoice = "4";
        }

        if (selectedChoice != null) {
            viewModel.submitAnswer(Integer.parseInt(selectedChoice));
        }
    }

    /**
     * Observe ViewModel LiveData changes
     */
    private void observeViewModel() {
        viewModel.getUserAnswerResult().observe(this, result -> {
            if (result != null) {
                if ("CORRECT".equals(result)) {
                    tvResult.setText("정답입니다");
                    Toast.makeText(LockScreenActivity.this, "정답입니다!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LockScreenActivity.this, "오답입니다.", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        viewModel.getUserPoints().observe(this, points -> {
            if (points != null) {
                // Update UI with new points if needed
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(LockScreenActivity.this, error, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // Handle loading state
        });
    }
}
