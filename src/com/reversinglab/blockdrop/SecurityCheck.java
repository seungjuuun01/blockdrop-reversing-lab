package com.reversinglab.blockdrop;

import android.os.Build;
import java.io.File;

/**
 * SecurityCheck — 교육용 루팅 탐지 (클라이언트 사이드 판정).
 *
 * ★ 리버싱 포인트:
 *   루팅 여부를 '기기 위(온-디바이스)'에서만 판정한다 = 근본적으로 우회 가능.
 *   isDeviceRooted() 가 돌려주는 boolean 하나를 Frida 로 false 로 만들면 탐지 무력화.
 *   진짜 방어는 서버 측 원격검증(Play Integrity 등)이라는 점을 보이기 위한 랩.
 */
public class SecurityCheck {

    // 흔한 su / busybox 설치 경로
    private static final String[] SU_PATHS = {
        "/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su",
        "/system/bin/failsafe/su", "/data/local/su", "/data/local/bin/su",
        "/data/local/xbin/su", "/magisk/.core/bin/su", "/cache/su", "/dev/su",
        "/system/bin/busybox", "/system/xbin/busybox", "/sbin/busybox"
    };

    /** ★ Frida 후킹 대상: 이 반환값만 false 로 바꾸면 통과. */
    public static boolean isDeviceRooted() {
        // 1) su/busybox 바이너리 존재
        for (String p : SU_PATHS) {
            if (new File(p).exists()) return true;
        }
        // 2) test-keys 빌드 태그(커스텀/비공식 ROM 흔적)
        String tags = Build.TAGS;
        if (tags != null && tags.contains("test-keys")) return true;

        return false;
    }

    /** 어떤 신호가 걸렸는지(차단 화면 표시용). */
    public static String firstSignal() {
        for (String p : SU_PATHS) {
            if (new File(p).exists()) return "su_binary(" + p + ")";
        }
        String tags = Build.TAGS;
        if (tags != null && tags.contains("test-keys")) return "test-keys";
        return "-";
    }
}
