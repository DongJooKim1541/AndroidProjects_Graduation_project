package com.example.gc_uiactivity.lock_screen;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.gc_uiactivity.R;
import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.example.gc_uiactivity.firebase.PointManager;
import com.example.gc_uiactivity.firebase.WrongAnswerManager;
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

public class LockScreenActivity extends Activity {

    ImageView iv_problem_image;
    TextView tv_problem;

    RadioGroup rg_answer;
    RadioButton rb_answer1;
    RadioButton rb_answer2;
    RadioButton rb_answer3;
    RadioButton rb_answer4;

    TextView tv_result;
    Random random = new Random();

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;

    // Firebase 매니저 인스턴스
    private DatabaseManager databaseManager;
    private PointManager pointManager;
    private WrongAnswerManager wrongAnswerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        // 매니저 초기화
        databaseManager = new DatabaseManager();
        pointManager = new PointManager();
        wrongAnswerManager = new WrongAnswerManager();

        tv_problem = findViewById(R.id.tv_problem);

        iv_problem_image = findViewById(R.id.problem_image);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        rb_answer1 = findViewById(R.id.rb_answer1);
        rb_answer2 = findViewById(R.id.rb_answer2);
        rb_answer3 = findViewById(R.id.rb_answer3);
        rb_answer4 = findViewById(R.id.rb_answer4);

        rb_answer1.setOnClickListener(radioButtonClickListener);
        rb_answer2.setOnClickListener(radioButtonClickListener);
        rb_answer3.setOnClickListener(radioButtonClickListener);
        rb_answer4.setOnClickListener(radioButtonClickListener);

        rg_answer = findViewById(R.id.rg_answer);

        tv_result = findViewById(R.id.tv_result);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        CheckTypesTask checkTypesTask = new CheckTypesTask();
        checkTypesTask.execute();
        ShowProblem();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= mScaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 2.0f));

            iv_problem_image.setScaleX(mScaleFactor);
            iv_problem_image.setScaleY(mScaleFactor * 1.1f);
            return true;
        }
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

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
            super.onPostExecute(result);
        }
    }

    RadioButton.OnClickListener radioButtonClickListener = new android.widget.RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public void ShowProblem() {
        // 현재 사용자의 이메일 조회
        databaseManager.getCurrentUserEmail(new DatabaseManager.EmailCallback() {
            @Override
            public void onEmailReceived(String email) {
                if (email != null) {
                    final String eDataStr = email;
                    loadProblemData(eDataStr);
                } else {
                    Toast.makeText(LockScreenActivity.this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 문제 데이터 로드
     */
    private void loadProblemData(final String email) {
        databaseManager.getUserInfo(email, new DatabaseManager.UserInfoCallback() {
            @Override
            public void onUserInfoReceived(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.child("ChoiceProblem").getValue() != null) {
                    if (dataSnapshot.child("problem_to_Korean").getValue() != null) {
                        final String problem = dataSnapshot.child("ChoiceProblem").getValue().toString();
                        final String problem_to_korean = dataSnapshot.child("problem_to_Korean").getValue().toString();

                        Log.d("KDJ", "problem:" + problem);
                        loadProblemYears(email, problem, problem_to_korean);
                    }
                } else {
                    Toast.makeText(LockScreenActivity.this, "문제 설정을 하지 않음.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 문제 연도 로드
     */
    private void loadProblemYears(final String email, final String problem, final String problem_to_korean) {
        databaseManager.getProblemYears(problem, new DatabaseManager.QuizCallback() {
            @Override
            public void onQuizzesReceived(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                    final String randYear = Integer.toString(random.nextInt((int) (dataSnapshot.getChildrenCount())) + 2018);
                    Log.d("KDJ", "randYear: " + randYear);
                    loadProblemEpisode(email, problem, problem_to_korean, randYear);
                }
            }
        });
    }

    /**
     * 문제 회차 로드
     */
    private void loadProblemEpisode(final String email, final String problem, final String problem_to_korean, final String year) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference problemRef = rootRef.child("문제 종류");
        final DatabaseReference problemYear = problemRef.child(problem).child("Year").child(year);

        problemYear.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    final String randEpisode = Integer.toString(random.nextInt((int) (dataSnapshot.getChildrenCount())) + 1);
                    Log.d("KDJ", "randEpisode: " + randEpisode);
                    loadProblemImage(email, problem, problem_to_korean, year, randEpisode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * 문제 이미지 및 정답 로드
     */
    private void loadProblemImage(final String email, final String problem, final String problem_to_korean, final String year, final String episode) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        DatabaseReference problemRef = rootRef.child("문제 종류");
        final DatabaseReference problemEpisode = problemRef.child(problem).child("Year").child(year).child(episode);

        problemEpisode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    final String randImagePath = Integer.toString(random.nextInt((int) (dataSnapshot.getChildrenCount())) + 1);
                    Log.d("KDJ", "randImagePath: " + randImagePath);

                    // 파이어베이스 스토리지에서 이미지 로드
                    loadImageFromStorage(problem, year, episode, randImagePath);

                    // 정답 로드
                    final String rightAnswer = dataSnapshot.child(randImagePath).getValue().toString();
                    setupAnswerListener(email, problem, year, episode, randImagePath, rightAnswer, problem_to_korean);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * 스토리지에서 이미지 로드
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
                iv_problem_image.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(LockScreenActivity.this, "다운로드 실패.", Toast.LENGTH_SHORT).show();
                ShowProblem();
            }
        });
    }

    /**
     * 정답 선택 리스너 설정
     */
    private void setupAnswerListener(final String email, final String problem, final String year,
                                     final String episode, final String problemNumber, final String correctAnswer,
                                     final String problem_to_korean) {
        tv_problem.setText(problem_to_korean + " " + year + "년 " + episode + "회 " + problemNumber + "번");

        rg_answer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleAnswerSelection(email, problem, year, episode, problemNumber, correctAnswer, checkedId);
            }
        });
    }

    /**
     * 정답 선택 처리 (코드 중복 제거)
     */
    private void handleAnswerSelection(final String email, final String problem, final String year,
                                       final String episode, final String problemNumber, final String correctAnswer, int checkedId) {
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

        if (selectedChoice != null && correctAnswer.equals(selectedChoice)) {
            // 정답 처리
            tv_result.setText("정답입니다");
            pointManager.addPointsForCorrectAnswer(email);
            finish();
        } else {
            // 오답 처리
            String problemInfo = year + "_" + episode + "_" + problemNumber + "_" + correctAnswer;
            wrongAnswerManager.recordWrongAnswer(email, problem, problemInfo);
            Toast.makeText(LockScreenActivity.this, "오답입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
