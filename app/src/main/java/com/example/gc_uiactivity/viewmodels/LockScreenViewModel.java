package com.example.gc_uiactivity.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.example.gc_uiactivity.firebase.PointManager;
import com.example.gc_uiactivity.firebase.WrongAnswerManager;

/**
 * ViewModel for LockScreenActivity
 * Manages quiz questions, user answers, and points updates
 */
public class LockScreenViewModel extends ViewModel {

    private final DatabaseManager databaseManager;
    private final PointManager pointManager;
    private final WrongAnswerManager wrongAnswerManager;

    // Quiz data
    private final MutableLiveData<String> quizQuestionLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> quizImageUrlLiveData = new MutableLiveData<>();
    private final MutableLiveData<String[]> quizOptionsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> correctAnswerLiveData = new MutableLiveData<>();

    // User interaction data
    private final MutableLiveData<String> userAnswerResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> userPointsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedAnswerLiveData = new MutableLiveData<>();

    // State data
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public LockScreenViewModel() {
        databaseManager = new DatabaseManager();
        pointManager = new PointManager();
        wrongAnswerManager = new WrongAnswerManager();
    }

    /**
     * Load next quiz question
     */
    public void loadNextQuiz() {
        isLoadingLiveData.setValue(true);
        // Implementation will fetch quiz data from Firebase
        // This is a placeholder for the actual implementation
        isLoadingLiveData.setValue(false);
    }

    /**
     * Submit user answer and update result
     */
    public void submitAnswer(int selectedOption) {
        selectedAnswerLiveData.setValue(selectedOption);
        Integer correctAnswer = correctAnswerLiveData.getValue();

        if (correctAnswer != null && selectedOption == correctAnswer) {
            userAnswerResultLiveData.setValue("CORRECT");
            updatePoints(10); // Award points for correct answer
        } else {
            userAnswerResultLiveData.setValue("INCORRECT");
            // Save wrong answer
            if (correctAnswer != null) {
                saveWrongAnswer(selectedOption, correctAnswer);
            }
        }
    }

    /**
     * Update user points
     */
    private void updatePoints(int points) {
        pointManager.addPoints(points, new PointManager.PointCallback() {
            @Override
            public void onPointsUpdated(Integer newPoints) {
                userPointsLiveData.setValue(newPoints);
            }

            @Override
            public void onError(String error) {
                errorMessageLiveData.setValue(error);
            }
        });
    }

    /**
     * Save wrong answer for later review
     */
    private void saveWrongAnswer(int userAnswer, int correctAnswer) {
        String question = quizQuestionLiveData.getValue();
        String[] options = quizOptionsLiveData.getValue();

        if (question != null && options != null) {
            wrongAnswerManager.addWrongAnswer(
                    question,
                    options[userAnswer],
                    options[correctAnswer],
                    new WrongAnswerManager.WrongAnswerCallback() {
                        @Override
                        public void onSuccess() {
                            // Wrong answer saved successfully
                        }

                        @Override
                        public void onError(String error) {
                            errorMessageLiveData.setValue(error);
                        }
                    }
            );
        }
    }

    /**
     * Load user current points
     */
    public void loadUserPoints() {
        pointManager.getCurrentPoints(new PointManager.PointCallback() {
            @Override
            public void onPointsUpdated(Integer newPoints) {
                userPointsLiveData.setValue(newPoints);
            }

            @Override
            public void onError(String error) {
                errorMessageLiveData.setValue(error);
            }
        });
    }

    // Getters for LiveData
    public LiveData<String> getQuizQuestion() {
        return quizQuestionLiveData;
    }

    public LiveData<String> getQuizImageUrl() {
        return quizImageUrlLiveData;
    }

    public LiveData<String[]> getQuizOptions() {
        return quizOptionsLiveData;
    }

    public LiveData<Integer> getCorrectAnswer() {
        return correctAnswerLiveData;
    }

    public LiveData<String> getUserAnswerResult() {
        return userAnswerResultLiveData;
    }

    public LiveData<Integer> getUserPoints() {
        return userPointsLiveData;
    }

    public LiveData<Integer> getSelectedAnswer() {
        return selectedAnswerLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }
}
