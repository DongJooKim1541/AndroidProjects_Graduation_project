package com.example.gc_uiactivity.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Firebase 데이터베이스 관리 클래스
 * 반복되는 Firebase 쿼리 로직을 통합
 */
public class DatabaseManager {

    private static final String TAG = "DatabaseManager";
    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference rootRef;

    public DatabaseManager() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.rootRef = firebaseDatabase.getReference();
    }

    /**
     * 현재 로그인한 사용자의 이메일 정보 조회
     * @param callback 이메일 조회 완료 시 호출되는 콜백
     */
    public void getCurrentUserEmail(final EmailCallback callback) {
        DatabaseReference currRef = rootRef.child("현재 상태");
        DatabaseReference currMembers = currRef.child("계정 정보");

        currMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Object emailObj = dataSnapshot.child("Email").getValue();
                    if (emailObj != null) {
                        String email = emailObj.toString();
                        callback.onEmailReceived(email);
                    } else {
                        callback.onEmailReceived(null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting user email", e);
                    callback.onEmailReceived(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                callback.onEmailReceived(null);
            }
        });
    }

    /**
     * 사용자 정보 조회
     * @param email 사용자 이메일 (점 제거됨)
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getUserInfo(final String email, final UserInfoCallback callback) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    callback.onUserInfoReceived(dataSnapshot);
                } else {
                    callback.onUserInfoReceived(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting user info", databaseError.toException());
                callback.onUserInfoReceived(null);
            }
        });
    }

    /**
     * 특정 문제 종류의 연도 정보 조회
     * @param problemType 문제 종류 (예: "ChoiceProblem")
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getProblemYears(final String problemType, final QuizCallback callback) {
        DatabaseReference problemRef = rootRef.child("문제 종류");
        DatabaseReference problems = problemRef.child(problemType).child("Year");

        problems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    callback.onQuizzesReceived(dataSnapshot);
                } else {
                    callback.onQuizzesReceived(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting problem years", databaseError.toException());
                callback.onQuizzesReceived(null);
            }
        });
    }

    /**
     * 포인트 조회
     * @param email 사용자 이메일
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getUserPoints(final String email, final PointsCallback callback) {
        DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");

        memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        int points = Integer.parseInt(dataSnapshot.getValue().toString());
                        callback.onPointsReceived(points);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing points", e);
                        callback.onPointsReceived(0);
                    }
                } else {
                    callback.onPointsReceived(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting user points", databaseError.toException());
                callback.onPointsReceived(0);
            }
        });
    }

    /**
     * 포인트 업데이트
     * @param email 사용자 이메일
     * @param newPoints 새로운 포인트 값
     */
    public void updateUserPoints(String email, int newPoints) {
        DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");
        memberPointRef.setValue(Integer.toString(newPoints));
    }

    /**
     * 오답 목록 조회
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getWrongAnswers(final String email, final String problemType, final WrongAnswerCallback callback) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    callback.onWrongAnswersReceived(dataSnapshot);
                } else {
                    callback.onWrongAnswersReceived(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting wrong answers", databaseError.toException());
                callback.onWrongAnswersReceived(null);
            }
        });
    }

    /**
     * 오답 개수 조회
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getWrongAnswerCount(final String email, final String problemType, final CountCallback callback) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType).child("오답 개수");

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        int count = Integer.parseInt(dataSnapshot.getValue().toString());
                        callback.onCountReceived(count);
                    } else {
                        callback.onCountReceived(0);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing count", e);
                    callback.onCountReceived(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting count", databaseError.toException());
                callback.onCountReceived(0);
            }
        });
    }

    /**
     * 오답 정보 업데이트
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param index 인덱스
     * @param value 저장할 값
     */
    public void updateWrongAnswer(String email, String problemType, int index, String value) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType);
        memberRef.child(Integer.toString(index)).setValue(value);
    }

    /**
     * 오답 개수 업데이트
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param count 새로운 개수
     */
    public void updateWrongAnswerCount(String email, String problemType, int count) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType).child("오답 개수");
        memberRef.setValue(Integer.toString(count));
    }

    /**
     * 오답 목록 초기화 (개수가 0일 때)
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     */
    public void initializeWrongAnswerCount(String email, String problemType) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType).child("오답 개수");
        memberRef.setValue("0");
    }

    // Callback 인터페이스들
    public interface EmailCallback {
        void onEmailReceived(String email);
    }

    public interface UserInfoCallback {
        void onUserInfoReceived(DataSnapshot dataSnapshot);
    }

    public interface QuizCallback {
        void onQuizzesReceived(DataSnapshot dataSnapshot);
    }

    public interface PointsCallback {
        void onPointsReceived(int points);
    }

    public interface WrongAnswerCallback {
        void onWrongAnswersReceived(DataSnapshot dataSnapshot);
    }

    public interface CountCallback {
        void onCountReceived(int count);
    }
}
