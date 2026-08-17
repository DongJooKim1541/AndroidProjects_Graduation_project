package com.example.gc_uiactivity.firebase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.gc_uiactivity.BuildConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 문제·프로필 이미지를 가져오는 단일 창구.
 *
 * 기본은 Firebase Cloud Storage 다. 다만 2024-09 정책 변경으로 무료(Spark) 요금제에서는
 * Storage 접근이 HTTP 402 로 거부된다. 그래서 {@code IMAGE_BASE_URL} 을 지정하면
 * 같은 경로 구조를 가진 임의의 HTTP 호스트에서 대신 내려받는다. Blaze 로 올리지 않고도
 * 이미지를 다른 곳에 올려 앱을 그대로 쓸 수 있다.
 *
 * <p><b>폴백이 아니라 택일이다.</b> {@code IMAGE_BASE_URL} 이 지정되어 있으면 Storage 를
 * 아예 호출하지 않고, 비어 있으면 HTTP 를 시도하지 않는다. 한쪽이 실패했을 때 다른 쪽으로
 * 넘어가지 않는다 — Spark 요금제에서는 Storage 가 항상 실패하므로, 폴백으로 만들면 이미지
 * 한 장마다 402 왕복을 먼저 치르게 되어 문제가 뜰 때마다 지연이 붙는다.
 *
 * 설정 방법 (둘 중 하나):
 *   local.properties 에  IMAGE_BASE_URL=https://example.com/quiz
 *   환경변수로          IMAGE_BASE_URL=https://example.com/quiz
 *
 * 경로 규칙은 Storage 와 동일하다:
 *   {base}/images/{problem}/{year}/{episode}/{number}.jpeg
 *   {base}/images/users/{emailKey}
 */
public final class ImageSource {

    private static final String TAG = "ImageSource";
    private static final long ONE_MEGABYTE = 1024 * 1024;
    private static final ExecutorService IO = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onLoaded(Bitmap bitmap);

        void onFailed(Exception error);
    }

    private ImageSource() {
    }

    /** 문제 이미지 경로를 만든다. */
    public static String problemPath(String problem, String year, String episode, String number) {
        return "images/" + problem + "/" + year + "/" + episode + "/" + number + ".jpeg";
    }

    /** 프로필 이미지 경로를 만든다. */
    public static String profilePath(String emailKey) {
        return "images/users/" + emailKey;
    }

    /** IMAGE_BASE_URL 이 설정되어 있으면 HTTP 로, 아니면 Firebase Storage 로 가져온다. */
    public static void load(final String path, final Callback callback) {
        final String base = BuildConfig.IMAGE_BASE_URL;
        if (base != null && !base.trim().isEmpty()) {
            loadOverHttp(base.trim(), path, callback);
        } else {
            loadFromFirebase(path, callback);
        }
    }

    private static void loadFromFirebase(String path, final Callback callback) {
        StorageReference root = FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://charged-dialect-285301.appspot.com/");
        root.child(path).getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(bytes -> callback.onLoaded(
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Storage 에서 이미지를 가져오지 못했다: " + path, e);
                    callback.onFailed(e);
                });
    }

    private static void loadOverHttp(final String base, final String path, final Callback callback) {
        IO.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(base.endsWith("/") ? base + path : base + "/" + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(15_000);
                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    throw new java.io.IOException("HTTP " + code + " for " + url);
                }
                try (InputStream in = conn.getInputStream()) {
                    final Bitmap bitmap = BitmapFactory.decodeStream(in);
                    if (bitmap == null) {
                        throw new java.io.IOException("이미지를 해석할 수 없다: " + url);
                    }
                    MAIN.post(() -> callback.onLoaded(bitmap));
                }
            } catch (final Exception e) {
                Log.e(TAG, "HTTP 로 이미지를 가져오지 못했다: " + path, e);
                MAIN.post(() -> callback.onFailed(e));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }
}
