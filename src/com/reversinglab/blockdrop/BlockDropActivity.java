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
 * BlockDropActivity — 게임 화면 + 상점.
 *   줄 삭제로 골드를 모으고, 상점에서 500 골드로 '골드 스킨'을 산다.
 *   ★ 상점 구매는 Wallet.getGold() 잔액을 보고 통과 → getGold() 후킹이면 무한 구매.
 */
public class BlockDropActivity extends Activity {

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

        LinearLayout ctl = new LinearLayout(this);
        ctl.setOrientation(LinearLayout.HORIZONTAL);
        ctl.addView(btn("◀", new View.OnClickListener() { public void onClick(View v) { game.moveLeft(); } }));
        ctl.addView(btn("⟳", new View.OnClickListener() { public void onClick(View v) { game.rotate(); } }));
        ctl.addView(btn("▶", new View.OnClickListener() { public void onClick(View v) { game.moveRight(); } }));
        ctl.addView(btn("▼", new View.OnClickListener() { public void onClick(View v) { game.softDrop(); } }));
        ctl.addView(btn("⤓", new View.OnClickListener() { public void onClick(View v) { game.hardDrop(); } }));
        root.addView(ctl);

        Button shop = new Button(this);
        shop.setText("🛍 상점 (골드 스킨 500)");   // 🛍 상점 (골드 스킨 500)
        shop.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { openShop(); } });
        root.addView(shop);

        setContentView(root);
        refresh();
    }

    private Button btn(String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(20);
        b.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        b.setOnClickListener(l);
        return b;
    }

    private void refresh() {
        status.setText("점수 " + game.getScore()
                + "        골드 " + Wallet.getGold()
                + (skinOwned ? "     ✨골드스킨" : ""));
    }

    private void openShop() {
        final int PRICE = 500;
        String msg = "골드 스킨 (모든 블록 금색)\n가격: " + PRICE
                + " 골드\n\n보유 골드: " + Wallet.getGold();
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle("상점")
                .setMessage(msg)
                .setPositiveButton("구매", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (Wallet.spend(PRICE)) {
                            skinOwned = true;
                            game.setGoldSkin(true);
                            toast("구매 완료! 골드 스킨 적용");
                        } else {
                            toast("골드가 부족합니다. (줄을 지워 모으거나… 후킹?)");
                        }
                        refresh();
                    }
                })
                .setNegativeButton("닫기", null)
                .show();
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
