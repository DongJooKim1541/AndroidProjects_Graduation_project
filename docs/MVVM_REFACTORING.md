# MVVM Architecture Refactoring

## Overview
This document describes the MVVM (Model-View-ViewModel) pattern implementation for the 코딩중독 Android application.

## Changes Made

### 1. Gradle Dependencies Update
**File:** `app/build.gradle`

Added MVVM architecture components:
```gradle
// MVVM Architecture Components
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.5.1'
implementation 'androidx.lifecycle:lifecycle-livedata:2.5.1'
implementation 'androidx.lifecycle:lifecycle-runtime:2.5.1'
implementation 'androidx.lifecycle:lifecycle-common:2.5.1'
implementation 'androidx.databinding:databinding-runtime:7.4.0'

// Fragment KTX for ViewModels
implementation 'androidx.fragment:fragment:1.5.5'
```

Also enabled DataBinding in Android configuration:
```gradle
buildFeatures {
    dataBinding true
}
```

### 2. ViewModel Classes Created

#### AuthViewModel.java
**Location:** `app/src/main/java/com/example/gc_uiactivity/viewmodels/`

Handles authentication-related operations:
- User login with email/password
- User sign-up with user data creation
- User logout
- Error message handling
- Loading state management

**LiveData exposed:**
- `currentUserLiveData`: Current authenticated user
- `errorMessageLiveData`: Error messages for login/signup
- `isLoadingLiveData`: Loading state for progress indication

#### MainActivityViewModel.java
**Location:** `app/src/main/java/com/example/gc_uiactivity/viewmodels/`

Manages MainActivity data and navigation:
- Current user email loading
- Fragment navigation state
- User authentication verification
- Menu state management

**LiveData exposed:**
- `currentUserEmailLiveData`: Current user's email
- `currentMenuLiveData`: Currently active menu
- `errorMessageLiveData`: Error messages
- `isLoadingLiveData`: Loading state

#### LockScreenViewModel.java
**Location:** `app/src/main/java/com/example/gc_uiactivity/viewmodels/`

Manages quiz/lock screen functionality:
- Quiz question and image data
- User answer submission and validation
- Points update after correct answers
- Wrong answer recording
- User points management

**LiveData exposed:**
- `quizQuestionLiveData`: Current quiz question
- `quizImageUrlLiveData`: Quiz image URL
- `quizOptionsLiveData`: Multiple choice options
- `correctAnswerLiveData`: Correct answer
- `userAnswerResultLiveData`: Result of user answer
- `userPointsLiveData`: Current user points
- `selectedAnswerLiveData`: User's selected answer
- `isLoadingLiveData`: Loading state
- `errorMessageLiveData`: Error messages

#### AnswerNoteViewModel.java
**Location:** `app/src/main/java/com/example/gc_uiactivity/viewmodels/`

Manages wrong answer notes for review:
- Load wrong answers with filters
- Delete individual wrong answers
- Delete all wrong answers
- Filter by year, round, and problem number
- Refresh data

**LiveData exposed:**
- `wrongAnswersLiveData`: List of wrong answers
- `selectedYearLiveData`: Selected year filter
- `selectedRoundLiveData`: Selected round filter
- `selectedProblemLiveData`: Selected problem filter
- `isLoadingLiveData`: Loading state
- `errorMessageLiveData`: Error messages

#### ViewModelFactory.java
**Location:** `app/src/main/java/com/example/gc_uiactivity/viewmodels/`

Factory class for creating ViewModel instances with dependency injection support:
```java
public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(AuthViewModel.class)) {
        return (T) new AuthViewModel();
    } else if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
        return (T) new MainActivityViewModel();
    } // ... other ViewModels
}
```

### 3. Activity Updates

#### MainActivity.java
**Changes:**
- Replaced direct database manager calls with ViewModel
- Initialize ViewModel using ViewModelFactory
- Observe LiveData for email and error messages
- Fragment navigation now uses ViewModel state
- Separation of business logic from UI

**Key improvements:**
- Cleaner code structure
- Better lifecycle management
- Data persistence on configuration changes
- Testable business logic

#### LoginActivity.java
**Changes:**
- Removed FirebaseAuth listeners
- Use AuthViewModel for authentication
- Observe user state changes through LiveData
- Handle loading state during login
- Separated authentication logic from UI

**Key improvements:**
- Simplified authentication flow
- Better error handling
- Loading state management
- MVVM separation of concerns

#### LockScreenActivity.java
**Changes:**
- Initialize LockScreenViewModel
- Submit answers through ViewModel
- Observe answer results from LiveData
- Points and error handling through ViewModel
- Separation of quiz logic from UI rendering

**Key improvements:**
- Quiz logic separated from UI
- Better answer handling
- Points management through ViewModel
- Error handling improvements

### 4. Fragment Updates

#### HomeFragment.java
**Changes:**
- Initialize MainActivityViewModel
- Load user profile through ViewModel
- Observe user data changes
- Handle logout through ViewModel
- Better state management

**Key improvements:**
- Cleaner fragment code
- Better lifecycle awareness
- Data persistence on configuration changes
- Improved user state handling

## Architecture Pattern

### LiveData Pattern
All ViewModels use LiveData for observable data streams:
```java
// In ViewModel
private MutableLiveData<String> userEmailLiveData = new MutableLiveData<>();

public LiveData<String> getUserEmail() {
    return userEmailLiveData;
}

// In Activity/Fragment
viewModel.getUserEmail().observe(this, email -> {
    // Update UI with email
});
```

### ViewModel Lifecycle
- ViewModels survive configuration changes (rotation, etc.)
- ViewModels are tied to Activity/Fragment lifecycle
- Data persists across screen rotations
- Automatic cleanup on Activity/Fragment destruction

### Dependency Injection
ViewModelFactory provides dependency injection:
```java
viewModel = new ViewModelProvider(this, new ViewModelFactory())
    .get(MainActivityViewModel.class);
```

## Benefits of MVVM Architecture

1. **Separation of Concerns**
   - UI layer (Activity/Fragment) - Only handles UI display
   - ViewModel layer - Manages state and business logic
   - Model layer - Handles data operations

2. **Testability**
   - ViewModels can be tested independently
   - LiveData can be tested without UI
   - Business logic is separated from UI

3. **Lifecycle Awareness**
   - Automatic lifecycle management
   - Data survives configuration changes
   - Memory efficient

4. **Maintainability**
   - Cleaner, more readable code
   - Easier to debug
   - Better code organization

5. **Reusability**
   - ViewModels can be shared between components
   - Business logic is independent of UI

## Migration Checklist

- [x] Add MVVM dependencies to build.gradle
- [x] Create AuthViewModel
- [x] Create MainActivityViewModel
- [x] Create LockScreenViewModel
- [x] Create AnswerNoteViewModel
- [x] Create ViewModelFactory
- [x] Update MainActivity with MVVM
- [x] Update LoginActivity with MVVM
- [x] Update LockScreenActivity with MVVM
- [x] Update HomeFragment with MVVM
- [x] Update other Fragments (pending)

## Next Steps

1. Update remaining Fragments:
   - AnswerNoteYearFragment
   - AnswerNoteNumFragment
   - AnswerNoteRoundFragment
   - CashFragment
   - OptionFragment
   - etc.

2. Create Repository pattern for data access:
   - Separate data access logic
   - Improve testability
   - Centralize data operations

3. Implement dependency injection:
   - Add Dagger/Hilt for automatic injection
   - Remove manual ViewModelFactory instantiation
   - Improve code modularity

4. Add unit tests:
   - Test ViewModels independently
   - Test LiveData observers
   - Test error handling

## Files Modified

- `app/build.gradle` - Added MVVM dependencies
- `app/src/main/java/com/example/gc_uiactivity/ui/activity/MainActivity.java` - MVVM refactoring
- `app/src/main/java/com/example/gc_uiactivity/ui/activity/LoginActivity.java` - MVVM refactoring
- `app/src/main/java/com/example/gc_uiactivity/ui/activity/LockScreenActivity.java` - MVVM refactoring
- `app/src/main/java/com/example/gc_uiactivity/ui/fragment/HomeFragment.java` - MVVM refactoring

## Files Created

- `app/src/main/java/com/example/gc_uiactivity/viewmodels/AuthViewModel.java`
- `app/src/main/java/com/example/gc_uiactivity/viewmodels/MainActivityViewModel.java`
- `app/src/main/java/com/example/gc_uiactivity/viewmodels/LockScreenViewModel.java`
- `app/src/main/java/com/example/gc_uiactivity/viewmodels/AnswerNoteViewModel.java`
- `app/src/main/java/com/example/gc_uiactivity/viewmodels/ViewModelFactory.java`

## References

- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [ViewModel Documentation](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData Documentation](https://developer.android.com/topic/libraries/architecture/livedata)
- [MVVM Pattern in Android](https://developer.android.com/jetpack/guide)
