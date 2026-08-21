package com.example.gc_uiactivity.lock_screen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

/**
 * 다른 앱 위에 표시 권한(SYSTEM_ALERT_WINDOW) 확인·요청.
 *
 * 잠금화면은 화면이 꺼질 때 백그라운드에서 액티비티를 띄운다. Android 10(API 29)
 * 부터는 백그라운드 액티비티 시작이 막혀 있고, 이 권한을 가진 앱만 예외로 허용된다.
 * 매니페스트에 권한을 선언하지 않았을 때는 targetSdk 가 29 여서 우연히 동작했을
 * 뿐이고, targetSdk 를 올리면 잠금화면이 아예 뜨지 않는다.
 *
 * 이 권한은 위험 권한이 아니라 특별 접근 권한이라 {@code requestPermissions()} 로
 * 받을 수 없다. 설정 화면으로 보내서 사용자가 직접 켜야 한다.
 */
public final class OverlayPermission {

    private OverlayPermission() {
    }

    /** 권한이 있으면 true. API 23 미만은 설치 시 부여되므로 항상 true. */
    public static boolean isGranted(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    /**
     * 권한 설정 화면을 연다. 액티비티 컨텍스트가 아닐 수 있어 NEW_TASK 를 붙인다.
     * 설정 화면이 없는 기기도 있어 실패를 감싼다.
     */
    public static void request(Context context) {
        Toast.makeText(context,
                "잠금화면을 쓰려면 '다른 앱 위에 표시' 권한이 필요합니다.",
                Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            android.util.Log.e("OverlayPermission", "권한 설정 화면을 열 수 없다", e);
            Toast.makeText(context,
                    "설정 > 앱 > 다른 앱 위에 표시 에서 직접 허용해 주세요.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
