package com.example.gc_uiactivity.lock_screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.gc_uiactivity.ui.activity.LockScreenActivity;

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // action 이 없는 브로드캐스트가 올 수 있으므로 상수 쪽에서 비교한다.
        if (intent != null && Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            Intent i = new Intent(context, LockScreenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}