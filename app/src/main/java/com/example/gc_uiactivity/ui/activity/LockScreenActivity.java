package com.example.gc_uiactivity.ui.activity;

import android.app.Activity;
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

import java.util.Random;

/**
 * LockScreenActivity with MVVM architecture
 * Displays quiz problems and handles user answers
 */
public class LockScreenActivity extends Activity {

    private ImageView ivProblemImage;
    private TextView tvProblem;
    private RadioGroup rgAnswer;
    private RadioButton rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4;
    private TextView tvResult;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    private Random random = new Random();
    private DatabaseManager databaseManager;

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
                if (dataSnapshot != null && dataSnapshot.child("ChoiceProblem").getValue() != null) {
                    if (dataSnapshot.child("problem_to_Korean").getValue() != null) {
                        final String problem = dataSnapshot.child("ChoiceProblem").getValue().toString();
                        final String problemToKorean = dataSnapshot.child("problem_to_Korean").getValue().toString();

                        Log.d("KDJ", "problem:" + problem);
                        loadProblemYears(email, problem, problemToKorean);
                    }
                } else {
                    Toast.makeText(LockScreenActivity.this, "문제 설정을 하지 않음.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Load problem years
     */
    private void loadProblemYears(final String email, final String problem, final String problemToKorean) {
        databaseManager.getProblemYears(problem, new DatabaseManager.QuizCallback() {
            @Override
            public void onQuizzesReceived(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                    final String randYear = Integer.toString(
                        random.nextInt((int) dataSnapshot.getChildrenCount()) + 2018);
                    Log.d("KDJ", "randYear: " + randYear);
                    loadProblemEpisode(email, problem, problemToKorean, randYear);
                }
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
                if (dataSnapshot.getChildrenCount() > 0) {
                    final String randEpisode = Integer.toString(
                        random.nextInt((int) dataSnapshot.getChildrenCount()) + 1);
                    Log.d("KDJ", "randEpisode: " + randEpisode);
                    loadProblemImage(email, problem, problemToKorean, year, randEpisode);
                }
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
                if (dataSnapshot.getChildrenCount() > 0) {
                    final String randImagePath = Integer.toString(
                        random.nextInt((int) dataSnapshot.getChildrenCount()) + 1);
                    Log.d("KDJ", "randImagePath: " + randImagePath);

                    // Load image from storage
                    loadImageFromStorage(problem, year, episode, randImagePath);

                    // Get correct answer
                    final String correctAnswer = dataSnapshot.child(randImagePath).getValue().toString();
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
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://charged-dialect-285301.appspot.com/");
        StorageReference pathReference = storageRef.child("images/" + problem + "/" + year + "/" + episode + "/" + number + ".jpeg");

        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ivProblemImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(LockScreenActivity.this, "다운로드 실패.", Toast.LENGTH_SHORT).show();
                showProblem();
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
