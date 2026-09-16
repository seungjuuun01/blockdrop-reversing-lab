package com.reversinglab.blockdrop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * MainActivity — 루팅 게이트.
 *   루팅 탐지되면 차단 화면(우회 없이는 못 지나감), 아니면 게임으로 진입.
 *   ※ '무시하고 계속' 같은 탈출 버튼 없음 → 실제 우회가 있어야만 통과(하드 게이트).
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SecurityCheck.isDeviceRooted()) {
            setContentView(buildBlockScreen());
        } else {
            startActivity(new Intent(this, BlockDropActivity.class));
            finish();
        }
    }

    private LinearLayout buildBlockScreen() {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setBackgroundColor(Color.parseColor("#1b1b2f"));
        ll.setPadding(56, 56, 56, 56);

        TextView t1 = new TextView(this);
        t1.setText("⛔  보안 경고");   // ⛔ 보안 경고
        t1.setTextColor(Color.parseColor("#ff5252"));
        t1.setTextSize(30);
        t1.setGravity(Gravity.CENTER);

        TextView t2 = new TextView(this);
        t2.setText("무결성 검증에 실패했습니다.\n루팅·변조된 기기에서는 실행할 수 없습니다.");
        t2.setTextColor(Color.parseColor("#e0e0e0"));
        t2.setTextSize(16);
        t2.setGravity(Gravity.CENTER);
        t2.setPadding(0, 36, 0, 0);

        // 실제 앱처럼 담백하게: 우회법/함수명 노출 X, 오류코드만.
        TextView t3 = new TextView(this);
        t3.setText("오류 코드: SEC-INTEGRITY-0x1A");
        t3.setTextColor(Color.parseColor("#6b7186"));
        t3.setTextSize(12);
        t3.setGravity(Gravity.CENTER);
        t3.setPadding(0, 40, 0, 0);

        ll.addView(t1);
        ll.addView(t2);
        ll.addView(t3);
        return ll;
    }
}
