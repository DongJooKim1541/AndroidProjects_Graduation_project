package com.example.gc_uiactivity.lock_screen;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.gc_uiactivity.ui.activity.MainActivity;
import com.example.gc_uiactivity.R;

public class ShowForegroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startForegroundService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY 로 되살아날 때 시스템은 intent 를 null 로 넘긴다.
        // 그대로 getAction() 을 부르면 앱이 죽는다(앱을 강제 종료한 뒤 다시 켜면 재현).
        String action = intent == null ? null : intent.getAction();

        if ("stopForeground".equals(action)) {
            stopForgroundService();
        } else {
            // action 이 없으면(재시작 포함) 알림을 다시 띄워 포그라운드 상태를 유지한다.
            startForegroundService();
        }
        return START_STICKY;
    }

    void startForegroundService() {
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"default");
        builder.setSmallIcon(R.mipmap.ic_main);
        builder.setContentTitle("이러라고 사준 핸드폰이 아닐텐데");
        builder.setContentText("잠금화면 서비스 실행 중");

        Intent notificationIntent=new Intent(this, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= 1){
            NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default","기본 채널",NotificationManager.IMPORTANCE_DEFAULT));
        }
        startForeground(1,builder.build());
    }

    void stopForgroundService(){
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"default");
        builder.setSmallIcon(R.mipmap.ic_main);
        builder.setContentTitle("이러라고 사준 핸드폰이 아닐텐데");
        builder.setContentText("잠금화면 서비스 미사용");

        Intent notificationIntent=new Intent(this,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= 1){
            NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default","기본 채널",NotificationManager.IMPORTANCE_DEFAULT));
        }
        startForeground(1,builder.build());
    }
}
