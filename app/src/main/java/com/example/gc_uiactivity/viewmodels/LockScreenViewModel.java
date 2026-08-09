package com.example.gc_uiactivity.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.example.gc_uiactivity.firebase.PointManager;
import com.example.gc_uiactivity.firebase.WrongAnswerManager;

/**
 * 잠금화면 퀴즈 ViewModel.
 *
 * 화면에서 받은 정답/문제 정보를 보관하고, 채점 결과에 따라 포인트를 지급하거나
 * 오답을 기록한다. Firebase 접근은 firebase 패키지의 매니저들에 위임한다.
 */
public class LockScreenViewModel extends ViewModel {

    private static final String TAG = "LockScreenViewModel";

    public static final String RESULT_CORRECT = "CORRECT";
    public static final String RESULT_INCORRECT = "INCORRECT";

    private final DatabaseManager databaseManager;
    private final PointManager pointManager;
    private final WrongAnswerManager wrongAnswerManager;

    /** 현재 출제된 문제의 종류. 오답 기록 시 사용한다. */
    private String problemType;

    /** 오답 기록 형식: year_episode_number_answer */
    private String problemInfo;

    private final MutableLiveData<Integer> correctAnswerLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedAnswerLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userAnswerResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> userPointsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public LockScreenViewModel() {
        this(new DatabaseManager(), new PointManager(), new WrongAnswerManager());
    }

    LockScreenViewModel(DatabaseManager databaseManager,
                        PointManager pointManager,
                        WrongAnswerManager wrongAnswerManager) {
        this.databaseManager = databaseManager;
        this.pointManager = pointManager;
        this.wrongAnswerManager = wrongAnswerManager;
    }

    /**
     * 출제된 문제 정보를 전달한다. 이걸 호출하지 않으면 채점이 항상 오답으로 처리된다.
     *
     * @param problemType 문제 종류
     * @param problemInfo 오답 기록용 정보 (year_episode_number_answer)
     * @param correctAnswer 정답 번호
     */
    public void setQuiz(String problemType, String problemInfo, String correctAnswer) {
        this.problemType = problemType;
        this.problemInfo = problemInfo;
        try {
            correctAnswerLiveData.setValue(Integer.parseInt(correctAnswer.trim()));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid correct answer: " + correctAnswer, e);
            correctAnswerLiveData.setValue(null);
        }
    }

    /** 사용자가 고른 답을 채점한다. */
    public void submitAnswer(int selectedOption) {
        selectedAnswerLiveData.setValue(selectedOption);

        Integer correctAnswer = correctAnswerLiveData.getValue();
        if (correctAnswer != null && selectedOption == correctAnswer) {
            userAnswerResultLiveData.setValue(RESULT_CORRECT);
            awardPointsForCorrectAnswer();
        } else {
            userAnswerResultLiveData.setValue(RESULT_INCORRECT);
            recordWrongAnswer();
        }
    }

    /** 현재 사용자의 포인트를 다시 읽어온다. */
    public void loadUserPoints() {
        isLoadingLiveData.setValue(true);
        withCurrentEmail(new EmailAction() {
            @Override
            public void run(String email) {
                refreshPoints(email);
            }
        });
    }

    private void awardPointsForCorrectAnswer() {
        withCurrentEmail(new EmailAction() {
            @Override
            public void run(String email) {
                // 지급 포인트는 PointManager 가 정한다(정답 1문항당 1,000점).
                pointManager.addPointsForCorrectAnswer(email);
                refreshPoints(email);
            }
        });
    }

    private void recordWrongAnswer() {
        if (problemType == null || problemInfo == null) {
            Log.d(TAG, "No quiz context; skipping wrong answer record");
            return;
        }
        withCurrentEmail(new EmailAction() {
            @Override
            public void run(String email) {
                wrongAnswerManager.recordWrongAnswer(email, problemType, problemInfo);
            }
        });
    }

    private void refreshPoints(String email) {
        pointManager.getPoints(email, new PointManager.PointsCallback() {
            @Override
            public void onPointsReceived(int points) {
                userPointsLiveData.postValue(points);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    private void withCurrentEmail(final EmailAction action) {
        databaseManager.getCurrentUserEmail(new DatabaseManager.EmailCallback() {
            @Override
            public void onEmailReceived(String email) {
                if (email == null) {
                    errorMessageLiveData.postValue("로그인 정보를 확인할 수 없습니다.");
                    isLoadingLiveData.postValue(false);
                    return;
                }
                action.run(email);
            }
        });
    }

    /** 이메일을 얻은 뒤 이어서 할 작업. 콜백 중첩을 한 겹으로 줄이기 위한 내부 타입. */
    private interface EmailAction {
        void run(String email);
    }

    public LiveData<Integer> getCorrectAnswer() {
        return correctAnswerLiveData;
    }

    public LiveData<Integer> getSelectedAnswer() {
        return selectedAnswerLiveData;
    }

    public LiveData<String> getUserAnswerResult() {
        return userAnswerResultLiveData;
    }

    public LiveData<Integer> getUserPoints() {
        return userPointsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }
}
