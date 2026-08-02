package com.example.gc_uiactivity.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory class for creating ViewModel instances
 * This allows for dependency injection into ViewModels
 */
public class ViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel();
        } else if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
            return (T) new MainActivityViewModel();
        } else if (modelClass.isAssignableFrom(LockScreenViewModel.class)) {
            return (T) new LockScreenViewModel();
        } else if (modelClass.isAssignableFrom(AnswerNoteViewModel.class)) {
            return (T) new AnswerNoteViewModel();
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
