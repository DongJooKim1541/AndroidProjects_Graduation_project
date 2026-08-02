package com.example.gc_uiactivity.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * 오답 관리 클래스
 * 반복되는 오답 기록 로직 통합
 */
public class WrongAnswerManager {

    private static final String TAG = "WrongAnswerManager";

    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference rootRef;

    public WrongAnswerManager() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.rootRef = firebaseDatabase.getReference();
    }

    /**
     * 오답 기록 (중복 확인 포함)
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param problemInfo 문제 정보 (형식: year_episode_number_answer)
     */
    public void recordWrongAnswer(final String email, final String problemType, final String problemInfo) {
        final DatabaseReference memberRef = rootRef.child("계정 정보").child(email);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // 오답 개수 초기화 확인
                    if (dataSnapshot.child("오답 목록").child(problemType).child("오답 개수").getValue() == null) {
                        memberRef.child("오답 목록").child(problemType).child("오답 개수").setValue("0");
                    }

                    String inCorrectAnswerNum = dataSnapshot.child("오답 목록").child(problemType).child("오답 개수").getValue().toString();
                    int count = Integer.parseInt(inCorrectAnswerNum);

                    // 중복 확인
                    if (!isWrongAnswerExists(dataSnapshot, problemType, count, problemInfo)) {
                        // 새로운 오답 추가
                        memberRef.child("오답 목록").child(problemType).child("오답 개수").setValue(Integer.toString(count + 1));
                        memberRef.child("오답 목록").child(problemType).child(inCorrectAnswerNum).setValue(problemInfo);
                        Log.d(TAG, "Wrong answer recorded: " + problemInfo);
                    } else {
                        Log.d(TAG, "Wrong answer already exists: " + problemInfo);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error recording wrong answer", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error recording wrong answer", databaseError.toException());
            }
        });
    }

    /**
     * 오답이 이미 존재하는지 확인
     * @param dataSnapshot 사용자 데이터
     * @param problemType 문제 종류
     * @param count 현재 오답 개수
     * @param problemInfo 확인할 오답 정보
     * @return 오답이 존재하면 true
     */
    private boolean isWrongAnswerExists(DataSnapshot dataSnapshot, String problemType, int count, String problemInfo) {
        for (int i = 0; i < count; i++) {
            try {
                Object value = dataSnapshot.child("오답 목록").child(problemType).child(Integer.toString(i)).getValue();
                if (value != null && value.toString().equals(problemInfo)) {
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking wrong answer existence", e);
            }
        }
        return false;
    }

    /**
     * 오답 목록 조회
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getWrongAnswers(final String email, final String problemType, final WrongAnswerListCallback callback) {
        final DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType);

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
     * 특정 문제의 오답 개수 조회
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getWrongAnswerCount(final String email, final String problemType, final CountCallback callback) {
        final DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType).child("오답 개수");

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
     * 오답 목록 초기화
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     */
    public void clearWrongAnswers(String email, String problemType) {
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType);
        memberRef.removeValue();
        Log.d(TAG, "Wrong answers cleared for problem type: " + problemType);
    }

    // Callback 인터페이스들
    public interface WrongAnswerListCallback {
        void onWrongAnswersReceived(DataSnapshot dataSnapshot);
    }

    public interface CountCallback {
        void onCountReceived(int count);
    }
}
