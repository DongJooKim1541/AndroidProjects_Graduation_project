package com.example.gc_uiactivity.lock_screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.gc_uiactivity.firebase.DatabaseManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * 재부팅 후 잠금화면 설정을 '미사용' 으로 되돌린다.
 *
 * 화면 꺼짐 감지는 {@link ScreenService} 가 살아 있는 동안만 동작한다. 재부팅하면
 * 서비스가 죽으므로 잠금화면은 실제로 꺼진다. 그런데 DB 의 {@code lockState} 는
 * "true" 로 남아 있어서 홈 화면과 설정 스위치는 "사용" 으로 보였다. 표시와 실제가
 * 어긋나 사용자가 잠금화면이 켜져 있다고 오해하게 된다.
 *
 * 부팅 시 서비스를 자동으로 되살리지 않고 상태를 끄는 쪽을 택했다. 잠금화면은
 * '다른 앱 위에 표시' 권한이 필요하고 사용자가 그 사이 권한을 회수했을 수도 있어,
 * 동의 없이 되살리는 것보다 꺼진 상태를 정확히 알리는 편이 안전하다.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        Log.i(TAG, "부팅 완료를 받았다");

        final String email = DatabaseManager.currentUserEmailKey();
        if (email == null) {
            Log.i(TAG, "로그인 상태가 아니라 되돌릴 lockState 가 없다");
            // 로그인 상태가 아니면 되돌릴 대상이 없다.
            return;
        }

        // 브로드캐스트는 onReceive 가 끝나면 프로세스가 정리될 수 있다. 쓰기가 끝날
        // 때까지 살려 두지 않으면 lockState 가 그대로 남는다.
        final PendingResult pendingResult = goAsync();

        DatabaseReference lockStateRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("계정 정보").child(email).child("lockState");

        lockStateRef.setValue("false", (error, ref) -> {
            if (error != null) {
                Log.e(TAG, "재부팅 후 lockState 초기화 실패", error.toException());
            } else {
                Log.i(TAG, "재부팅 감지: 잠금화면 설정을 미사용으로 되돌렸다");
            }
            pendingResult.finish();
        });
    }
}
