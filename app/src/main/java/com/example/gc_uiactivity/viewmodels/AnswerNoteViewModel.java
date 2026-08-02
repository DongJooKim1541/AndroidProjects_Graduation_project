package com.example.gc_uiactivity.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gc_uiactivity.firebase.WrongAnswerManager;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for AnswerNote functionality
 * Manages wrong answers and provides data for review
 */
public class AnswerNoteViewModel extends ViewModel {

    private final WrongAnswerManager wrongAnswerManager;

    private final MutableLiveData<List<DataSnapshot>> wrongAnswersLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedYearLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedRoundLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> selectedProblemLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public AnswerNoteViewModel() {
        wrongAnswerManager = new WrongAnswerManager();
    }

    /**
     * Load wrong answers filtered by year, round, and problem number
     */
    public void loadWrongAnswers(String year, String round, String problemNum) {
        isLoadingLiveData.setValue(true);
        wrongAnswerManager.getWrongAnswers(new WrongAnswerManager.WrongAnswerListCallback() {
            @Override
            public void onSuccess(List<DataSnapshot> answers) {
                isLoadingLiveData.setValue(false);
                wrongAnswersLiveData.setValue(answers);
            }

            @Override
            public void onError(String error) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(error);
            }
        });
    }

    /**
     * Refresh wrong answers
     */
    public void refreshWrongAnswers() {
        String year = selectedYearLiveData.getValue();
        String round = selectedRoundLiveData.getValue();
        String problem = selectedProblemLiveData.getValue();

        if (year != null && round != null) {
            loadWrongAnswers(year, round, problem != null ? problem : "");
        }
    }

    /**
     * Delete a wrong answer
     */
    public void deleteWrongAnswer(String answerId) {
        isLoadingLiveData.setValue(true);
        wrongAnswerManager.deleteWrongAnswer(answerId, new WrongAnswerManager.WrongAnswerCallback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                refreshWrongAnswers();
            }

            @Override
            public void onError(String error) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(error);
            }
        });
    }

    /**
     * Delete all wrong answers
     */
    public void deleteAllWrongAnswers() {
        isLoadingLiveData.setValue(true);
        wrongAnswerManager.deleteAllWrongAnswers(new WrongAnswerManager.WrongAnswerCallback() {
            @Override
            public void onSuccess() {
                isLoadingLiveData.setValue(false);
                wrongAnswersLiveData.setValue(new ArrayList<>());
            }

            @Override
            public void onError(String error) {
                isLoadingLiveData.setValue(false);
                errorMessageLiveData.setValue(error);
            }
        });
    }

    /**
     * Set selected year filter
     */
    public void setSelectedYear(String year) {
        selectedYearLiveData.setValue(year);
    }

    /**
     * Set selected round filter
     */
    public void setSelectedRound(String round) {
        selectedRoundLiveData.setValue(round);
    }

    /**
     * Set selected problem number filter
     */
    public void setSelectedProblem(String problemNum) {
        selectedProblemLiveData.setValue(problemNum);
    }

    // Getters for LiveData
    public LiveData<List<DataSnapshot>> getWrongAnswers() {
        return wrongAnswersLiveData;
    }

    public LiveData<String> getSelectedYear() {
        return selectedYearLiveData;
    }

    public LiveData<String> getSelectedRound() {
        return selectedRoundLiveData;
    }

    public LiveData<String> getSelectedProblem() {
        return selectedProblemLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    /**
     * Get wrong answer count
     */
    public int getWrongAnswerCount() {
        List<DataSnapshot> answers = wrongAnswersLiveData.getValue();
        return answers != null ? answers.size() : 0;
    }

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        errorMessageLiveData.setValue(null);
    }
}
