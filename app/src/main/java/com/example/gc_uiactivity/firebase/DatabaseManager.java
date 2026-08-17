package com.example.gc_uiactivity.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        // 로그인한 사용자는 이 기기의 FirebaseAuth 세션에서 판단한다.
        // 예전에는 "현재 상태/계정 정보/Email" 이라는 DB 전역 노드 하나를 읽었는데,
        // 그 노드는 모든 기기가 공유한다. 그래서 다른 기기에서 누가 로그인하면
        // 이 기기 화면에도 그 사람의 이름·포인트·오답이 그대로 보였다.
        callback.onEmailReceived(currentUserEmailKey());
    }

    /**
     * 이 기기에 로그인한 사용자의 이메일 키. 로그인하지 않았으면 null.
     *
     * FirebaseAuth 세션은 기기마다 독립이므로, 여러 기기에서 서로 다른 계정으로
     * 동시에 접속해도 서로 간섭하지 않는다.
     */
    public static String currentUserEmailKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        String email = user.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return toEmailKey(email.trim());
    }

    /**
     * 사용자 정보 조회
     * @param email 사용자 이메일 (점 제거됨)
     * @param callback 조회 완료 시 호출되는 콜백
     */
    public void getUserInfo(final String email, final UserInfoCallback callback) {
        if (email == null) {
            Log.e(TAG, "getUserInfo: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "getUserPoints: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
     * 로그인한 사용자를 "현재 상태" 노드에 기록한다.
     *
     * 앱 전체가 로그인 여부를 FirebaseAuth 가 아니라 이 노드로 판단한다
     * (HomeFragment, OptionSwitchAdapter, AnswerNote* 등). 따라서 로그인
     * 직후 반드시 호출해야 하며, 빠지면 로그인해도 계속 비로그인으로 보인다.
     *
     * @param email 사용자 이메일 (원본. 점은 이 메서드가 제거한다)
     */
    public void setCurrentUserEmail(String email) {
        // Firebase 키에는 '.' 을 쓸 수 없다. 저장 형식은 회원가입 때와 동일해야 한다.
        String emailKey = toEmailKey(email);
        rootRef.child("현재 상태").child("계정 정보").child("Email").setValue(emailKey);
    }

    /**
     * 로그아웃 — "현재 상태" 의 이메일을 지운다.
     */
    public void clearCurrentUserEmail() {
        rootRef.child("현재 상태").child("계정 정보").child("Email").setValue(null);
    }

    /**
     * 이메일을 Firebase 키로 쓸 수 있는 형태로 바꾼다.
     */
    public static String toEmailKey(String email) {
        return email == null ? null : email.replaceAll("[.]", "");
    }

    /**
     * 스냅샷의 자식 값을 문자열로 읽는다. 값이 없으면 null 을 돌려준다.
     *
     * 곳곳에서 쓰던 {@code snapshot.child(key).getValue().toString()} 은 노드가 아직
     * 없을 때 NullPointerException 으로 앱을 죽인다.
     */
    public static String stringOf(DataSnapshot snapshot, String key) {
        if (snapshot == null) {
            return null;
        }
        Object value = snapshot.child(key).getValue();
        return value == null ? null : value.toString();
    }

    /**
     * "현재 상태/계정 정보" 스냅샷에서 로그인한 사용자 이메일을 꺼낸다.
     *
     * 로그인하지 않았으면 null 을 돌려준다. 값이 없을 때뿐 아니라 빈 문자열일 때도
     * null 로 본다 — 예전 버전이 로그아웃 시 ""을 기록한 적이 있어서, 이를 구분하지
     * 않으면 로그아웃 상태가 로그인으로 보인다.
     *
     * @param currentStateSnapshot "현재 상태/계정 정보" 노드의 스냅샷
     */
    public static String currentEmailOf(DataSnapshot currentStateSnapshot) {
        if (currentStateSnapshot == null) {
            return null;
        }
        Object value = currentStateSnapshot.child("Email").getValue();
        if (value == null) {
            return null;
        }
        String email = value.toString().trim();
        return email.isEmpty() ? null : email;
    }

    /**
     * 포인트 업데이트
     * @param email 사용자 이메일
     * @param newPoints 새로운 포인트 값
     */
    public void updateUserPoints(String email, int newPoints) {
        if (email == null) {
            Log.e(TAG, "updateUserPoints: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "getWrongAnswers: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "getWrongAnswerCount: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "updateWrongAnswer: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
        if (email == null) {
            Log.e(TAG, "updateWrongAnswerCount: email 이 null 이다 (로그아웃 상태)");
            return;
        }
        DatabaseReference memberRef = rootRef.child("계정 정보").child(email).child("오답 목록").child(problemType).child("오답 개수");
        memberRef.setValue(Integer.toString(count));
    }

    /**
     * 오답 목록 초기화 (개수가 0일 때)
     * @param email 사용자 이메일
     * @param problemType 문제 종류
     */
    public void initializeWrongAnswerCount(String email, String problemType) {
        if (email == null) {
            Log.e(TAG, "initializeWrongAnswerCount: email 이 null 이다 (로그아웃 상태)");
            return;
        }
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
