package com.example.gc_uiactivity.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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
        if (email == null) {
            Log.e(TAG, "recordWrongAnswer: email 이 null 이다 (로그아웃 상태)");
            return;
        }
        final DatabaseReference memberRef = rootRef.child("계정 정보").child(email);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    // 오답 개수를 읽는다. setValue 는 비동기여서 방금 쓴 값이 이 스냅샷에
                    // 반영되지 않는다. 이전에는 없을 때 "0" 을 쓴 뒤 같은 스냅샷을 다시
                    // 읽어 여전히 null 이었고, NPE 가 catch 에 먹혀 문제 유형별 첫 오답이
                    // 조용히 유실됐다. 없으면 로컬에서 0 으로 두고 진행한다.
                    Object storedCount = dataSnapshot
                            .child("오답 목록").child(problemType).child("오답 개수").getValue();

                    int parsed;
                    try {
                        parsed = storedCount == null ? 0 : Integer.parseInt(storedCount.toString().trim());
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "오답 개수를 숫자로 읽을 수 없어 0 으로 시작한다: " + storedCount, e);
                        parsed = 0;
                    }
                    final int count = parsed;
                    String inCorrectAnswerNum = Integer.toString(count);

                    // 중복 확인
                    if (!isWrongAnswerExists(dataSnapshot, problemType, count, problemInfo)) {
                        // 새로운 오답 추가
                        // 개수는 트랜잭션으로 올린다. 동시에 두 문제를 틀리면 읽고-쓰기
                        // 방식에서는 한 건이 같은 인덱스에 덮어써져 사라진다.
                        memberRef.child("오답 목록").child(problemType).child("오답 개수")
                                .runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Object raw = currentData.getValue();
                                int now;
                                try {
                                    now = raw == null ? 0 : Integer.parseInt(raw.toString().trim());
                                } catch (NumberFormatException e) {
                                    now = 0;
                                }
                                currentData.setValue(Integer.toString(now + 1));
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                                if (error != null || !committed || snapshot == null) {
                                    Log.e(TAG, "오답 개수 증가 실패", error != null ? error.toException() : null);
                                    return;
                                }
                                // 확정된 인덱스에 기록한다. 트랜잭션이 정한 값보다 하나 앞이 이번 자리다.
                                int assigned;
                                try {
                                    assigned = Integer.parseInt(snapshot.getValue().toString().trim()) - 1;
                                } catch (Exception e) {
                                    assigned = count;
                                }
                                memberRef.child("오답 목록").child(problemType)
                                        .child(Integer.toString(assigned)).setValue(problemInfo);
                            }
                        });
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
        if (email == null) {
            Log.e(TAG, "getWrongAnswers: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "getWrongAnswerCount: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "clearWrongAnswers: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
