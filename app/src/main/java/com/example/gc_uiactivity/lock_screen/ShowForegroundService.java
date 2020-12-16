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

import com.example.gc_uiactivity.MainActivity;
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
        if("startForeground".equals(intent.getAction())){
            startForegroundService();
        }
        if("stopForeground".equals(intent.getAction())){
            stopForgroundService();
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
