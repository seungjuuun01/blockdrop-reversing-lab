package com.reversinglab.blockdrop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * BlockDropActivity — 게임 화면 + 상점 + 메시지함 + 설정.
 *   줄 삭제로 골드를 모으고, 상점에서 500 골드로 '골드 스킨' 구매.
 *   메시지함: 신규가입 100원 / 오늘 출석 50원 보상 수령. 설정: 돈 초기화.
 *   ★ 상점 구매·잔액·보상 전부 Wallet(클라이언트 상태)이라 후킹으로 조작 가능.
 */
public class BlockDropActivity extends Activity {

    private static final int DIALOG_THEME = android.R.style.Theme_Material_Light_Dialog_Alert;

    private TextView status;
    private GameView game;
    private boolean skinOwned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1b1b2f"));

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setPadding(28, 28, 28, 12);
        root.addView(status);

        game = new GameView(this);
        game.setOnChange(new Runnable() { public void run() { refresh(); } });
        root.addView(game, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // 방향/회전 컨트롤
        LinearLayout ctl = new LinearLayout(this);
        ctl.setOrientation(LinearLayout.HORIZONTAL);
        ctl.addView(btn("◀", new View.OnClickListener() { public void onClick(View v) { game.moveLeft(); } }));
        ctl.addView(btn("⟳", new View.OnClickListener() { public void onClick(View v) { game.rotate(); } }));
        ctl.addView(btn("▶", new View.OnClickListener() { public void onClick(View v) { game.moveRight(); } }));
        ctl.addView(btn("▼", new View.OnClickListener() { public void onClick(View v) { game.softDrop(); } }));
        ctl.addView(btn("⤓", new View.OnClickListener() { public void onClick(View v) { game.hardDrop(); } }));
        root.addView(ctl);

        // 하단 메뉴: 상점 | 메시지함 | 설정
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.addView(btn("🛍 상점", new View.OnClickListener() { public void onClick(View v) { openShop(); } }));
        menu.addView(btn("✉ 메시지함", new View.OnClickListener() { public void onClick(View v) { openMailbox(); } }));
        menu.addView(btn("⚙ 설정", new View.OnClickListener() { public void onClick(View v) { openSettings(); } }));
        root.addView(menu);

        setContentView(root);
        refresh();
    }

    private Button btn(String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        b.setOnClickListener(l);
        return b;
    }

    private void refresh() {
        status.setText("점수 " + game.getScore()
                + "        골드 " + Wallet.getGold()
                + (skinOwned ? "     ✨골드스킨" : ""));
    }

    // ---------- 상점 ----------
    private void openShop() {
        final int PRICE = 500;
        String msg = "골드 스킨 (모든 블록 금색)\n가격: " + PRICE
                + " 골드\n\n보유 골드: " + Wallet.getGold();
        new AlertDialog.Builder(this, DIALOG_THEME)
                .setTitle("상점")
                .setMessage(msg)
                .setPositiveButton("구매", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (Wallet.spend(PRICE)) {
                            skinOwned = true;
                            game.setGoldSkin(true);
                            toast("구매 완료! 골드 스킨 적용");
                        } else {
                            toast("골드가 부족합니다.");
                        }
                        refresh();
                    }
                })
                .setNegativeButton("닫기", null)
                .show();
    }

    // ---------- 메시지함 ----------
    private void openMailbox() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(56, 20, 56, 8);

        // 🎁 신규가입 100원 / 📅 오늘 출석 50원
        box.addView(mailRow("🎁 신규가입 축하 보상", 100, true));
        box.addView(divider());
        box.addView(mailRow("📅 오늘 출석 보상", 50, false));

        new AlertDialog.Builder(this, DIALOG_THEME)
                .setTitle("메시지함")
                .setView(box)
                .setNegativeButton("닫기", null)
                .show();
    }

    /** 메시지함 한 줄: 설명 + 받기/받음 버튼. isSignup=true 면 신규가입, false 면 출석. */
    private View mailRow(String label, final int amount, final boolean isSignup) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 18, 0, 18);

        TextView tv = new TextView(this);
        tv.setText(label + "\n" + amount + "원");   // ...원
        tv.setTextColor(Color.parseColor("#20242e"));
        tv.setTextSize(15);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final boolean claimed = isSignup ? Wallet.signupClaimed : Wallet.attendanceClaimed;
        final Button b = new Button(this);
        b.setText(claimed ? "받음" : "받기");   // 받음 / 받기
        b.setEnabled(!claimed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Wallet.addGold(amount);                 // ★ 클라이언트에서 그냥 골드 지급
                if (isSignup) Wallet.signupClaimed = true; else Wallet.attendanceClaimed = true;
                b.setText("받음");
                b.setEnabled(false);
                toast(amount + "원 받았습니다!");   // N원 받았습니다!
                refresh();
            }
        });

        row.addView(tv);
        row.addView(b);
        return row;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(Color.parseColor("#d0d3da"));
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        return v;
    }

    // ---------- 설정 ----------
    private void openSettings() {
        final String[] items = { "돈 초기화" };   // 돈 초기화
        new AlertDialog.Builder(this, DIALOG_THEME)
                .setTitle("설정")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            Wallet.reset();                        // 골드 0 으로
                            toast("돈을 0원으로 초기화했습니다.");
                            refresh();
                        }
                    }
                })
                .setNegativeButton("닫기", null)
                .show();
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
