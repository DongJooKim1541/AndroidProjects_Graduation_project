package com.example.gc_uiactivity.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
        final DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");

        memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    int currentPoints = Integer.parseInt(dataSnapshot.getValue().toString());
                    int newPoints = currentPoints + points;
                    memberPointRef.setValue(Integer.toString(newPoints));
                    Log.d(TAG, "Points updated: " + currentPoints + " -> " + newPoints);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing points", e);
                    memberPointRef.setValue(Integer.toString(points));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error adding points", databaseError.toException());
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
        final DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");

        memberPointRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    int points = Integer.parseInt(dataSnapshot.getValue().toString());
                    callback.onPointsReceived(points);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing points", e);
                    callback.onPointsReceived(0);
                }
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
        DatabaseReference memberPointRef = rootRef.child("계정 정보").child(email).child("Points");
        memberPointRef.setValue(Integer.toString(points));
        Log.d(TAG, "Points set to: " + points);
    }

    // Callback 인터페이스
    public interface PointsCallback {
        void onPointsReceived(int points);
    }
}
