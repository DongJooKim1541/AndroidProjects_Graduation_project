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
 * 포인트 관리 클래스
 * 반복되는 포인트 계산 및 업데이트 로직 통합
 */
public class PointManager {

    private static final String TAG = "PointManager";
    private static final int CORRECT_ANSWER_POINTS = 1000;

    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference rootRef;

    public PointManager() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.rootRef = firebaseDatabase.getReference();
    }

    /**
     * 포인트 추가
     * @param email 사용자 이메일
     * @param points 추가할 포인트
     */
    public void addPoints(String email, int points) {
        if (email == null) {
            Log.e(TAG, "addPoints: email 이 null 이다 (로그아웃 상태)");
            return;
        }
        final DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");

        // 트랜잭션으로 더한다. 읽고 나서 쓰는 방식은 두 곳에서 동시에 정답을 맞히면
        // 나중에 쓴 값이 앞의 증가를 덮어써 점수가 사라진다. (동일 패턴으로 동시에
        // +1000 을 10번 넣었을 때 10000 이 아니라 1000 만 남는 것을 실측 확인했다.)
        memberPointRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                int currentPoints = parsePoints(currentData.getValue());
                currentData.setValue(Integer.toString(currentPoints + points));
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null) {
                    Log.e(TAG, "Error adding points", error.toException());
                } else if (!committed) {
                    Log.w(TAG, "포인트 증가가 커밋되지 않았다: " + email);
                } else {
                    Log.d(TAG, "Points updated: +" + points
                            + " -> " + (snapshot != null ? snapshot.getValue() : "?"));
                }
            }
        });
    }

    /**
     * 정답에 대한 포인트 추가 (기본값: 1000점)
     * @param email 사용자 이메일
     */
    public void addPointsForCorrectAnswer(String email) {
        addPoints(email, CORRECT_ANSWER_POINTS);
    }

    /**
     * 포인트 조회
     * @param email 사용자 이메일
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getPoints(String email, final PointsCallback callback) {
        if (email == null) {
            Log.e(TAG, "getPoints: email 이 null 이다 (로그아웃 상태)");
            return;
        }
        final DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");

        memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onPointsReceived(parsePoints(dataSnapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting points", databaseError.toException());
                callback.onPointsReceived(0);
            }
        });
    }

    /**
     * 포인트 직접 설정
     * @param email 사용자 이메일
     * @param points 설정할 포인트
     */
    public void setPoints(String email, int points) {
        if (email == null) {
            Log.e(TAG, "setPoints: email 이 null 이다 (로그아웃 상태)");
            return;
        }
        DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");
        memberPointRef.setValue(Integer.toString(points));
        Log.d(TAG, "Points set to: " + points);
    }

    // Callback 인터페이스
    public interface PointsCallback {
        void onPointsReceived(int points);
    }

    /**
     * 스냅샷에서 포인트를 읽는다. 값이 없거나 숫자가 아니면 0 으로 본다.
     */
    private static int parsePoints(DataSnapshot snapshot) {
        return parsePoints(snapshot == null ? null : snapshot.getValue());
    }

    /**
     * 값에서 포인트를 읽는다. 없거나 숫자가 아니면 0 으로 본다.
     */
    private static int parsePoints(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "포인트를 숫자로 읽을 수 없다: " + value, e);
            return 0;
        }
    }
}
