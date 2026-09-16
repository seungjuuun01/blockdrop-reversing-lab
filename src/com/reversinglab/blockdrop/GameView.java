package com.reversinglab.blockdrop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import java.util.Arrays;
import java.util.Random;

/**
 * GameView — 순수 Canvas 테트리스(간단 버전).
 *   10x20 그리드, 7 테트로미노, 중력/회전/줄삭제. 줄을 지우면 Wallet.addGold().
 */
public class GameView extends View {

    private static final int COLS = 10, ROWS = 20;
    private final int[][] grid = new int[ROWS][COLS];       // 0=빈칸, >0=색인덱스(1-based)
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 7 테트로미노(4x4 박스 안의 4칸 좌표)
    private static final int[][][] SHAPES = {
        {{1,0},{1,1},{1,2},{1,3}}, // I
        {{0,0},{1,0},{1,1},{1,2}}, // J
        {{0,2},{1,0},{1,1},{1,2}}, // L
        {{0,1},{0,2},{1,1},{1,2}}, // O
        {{0,1},{0,2},{1,0},{1,1}}, // S
        {{0,1},{1,0},{1,1},{1,2}}, // T
        {{0,0},{0,1},{1,1},{1,2}}  // Z
    };
    private static final int[] COLORS = {
        Color.rgb(0,240,240), Color.rgb(40,80,240), Color.rgb(240,160,0),
        Color.rgb(240,220,0), Color.rgb(0,220,80), Color.rgb(170,0,240),
        Color.rgb(240,40,40)
    };
    private static final int GOLD = Color.rgb(255,205,60);

    private int[][] cur;        // 현재 조각(회전 반영된 4칸 좌표)
    private int curType;        // 색 인덱스(1-based)
    private int curR, curC;     // 조각 기준 오프셋(행, 열)
    private final Random rnd = new Random();
    private int score = 0;
    private int dropMs = 650;
    private boolean goldSkin = false;

    private final Handler h = new Handler(Looper.getMainLooper());
    private Runnable onChange;  // 활동에 점수/골드 변경 알림
    private final Runnable tick = new Runnable() {
        @Override public void run() { step(); h.postDelayed(this, dropMs); }
    };

    public GameView(Context c) { super(c); spawn(); }

    public void setOnChange(Runnable r) { onChange = r; }
    public int getScore() { return score; }
    public void setGoldSkin(boolean g) { goldSkin = g; invalidate(); }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        h.postDelayed(tick, dropMs);
    }
    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        h.removeCallbacks(tick);
    }

    // 4x4 박스 회전: (r,c) -> (c, 3-r)
    private int[][] rotateCells(int[][] cells) {
        int[][] out = new int[4][2];
        for (int i = 0; i < 4; i++) { out[i][0] = cells[i][1]; out[i][1] = 3 - cells[i][0]; }
        return out;
    }

    private void spawn() {
        int t = rnd.nextInt(7);
        curType = t + 1;
        cur = new int[4][2];
        for (int i = 0; i < 4; i++) { cur[i][0] = SHAPES[t][i][0]; cur[i][1] = SHAPES[t][i][1]; }
        curR = 0; curC = 3;
        if (collides(cur, curR, curC)) {         // 스폰 즉시 충돌 = 보드 가득 → 리셋(골드는 유지)
            for (int[] row : grid) Arrays.fill(row, 0);
            score = 0;
            if (onChange != null) onChange.run();
        }
    }

    private boolean collides(int[][] cells, int baseR, int baseC) {
        for (int[] cell : cells) {
            int r = baseR + cell[0], c = baseC + cell[1];
            if (c < 0 || c >= COLS || r >= ROWS) return true;
            if (r >= 0 && grid[r][c] != 0) return true;
        }
        return false;
    }

    private void lock() {
        for (int[] cell : cur) {
            int r = curR + cell[0], c = curC + cell[1];
            if (r >= 0 && r < ROWS && c >= 0 && c < COLS) grid[r][c] = curType;
        }
        int lines = clearLines();
        if (lines > 0) {
            score += lines * 100;
            Wallet.addGold(lines * 100);          // ★ 줄 삭제 = 골드 획득
            if (onChange != null) onChange.run();
        }
        spawn();
    }

    private int clearLines() {
        int cleared = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) if (grid[r][c] == 0) { full = false; break; }
            if (full) {
                cleared++;
                for (int rr = r; rr > 0; rr--) grid[rr] = grid[rr - 1].clone();
                grid[0] = new int[COLS];
                r++;                              // 내려온 행 다시 검사
            }
        }
        return cleared;
    }

    private void step() {
        if (!collides(cur, curR + 1, curC)) curR++;
        else lock();
        invalidate();
    }

    /** 게임 다시 시작: 보드·점수 초기화 후 새 조각 스폰(골드·구매 스킨은 유지). */
    public void restart() {
        for (int[] row : grid) Arrays.fill(row, 0);
        score = 0;
        spawn();
        if (onChange != null) onChange.run();
        invalidate();
    }

    // ---- 컨트롤 ----
    public void moveLeft()  { if (!collides(cur, curR, curC - 1)) { curC--; invalidate(); } }
    public void moveRight() { if (!collides(cur, curR, curC + 1)) { curC++; invalidate(); } }
    public void softDrop()  { step(); }
    public void hardDrop()  { while (!collides(cur, curR + 1, curC)) curR++; lock(); invalidate(); }
    public void rotate() {
        int[][] r = rotateCells(cur);
        if      (!collides(r, curR, curC))     { cur = r; }
        else if (!collides(r, curR, curC - 1)) { curC--; cur = r; }
        else if (!collides(r, curR, curC + 1)) { curC++; cur = r; }
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        int cell = Math.min(getWidth() / COLS, getHeight() / ROWS);
        int ox = (getWidth() - cell * COLS) / 2;
        int oy = (getHeight() - cell * ROWS) / 2;

        p.setColor(Color.parseColor("#12121f"));
        canvas.drawRect(ox, oy, ox + cell * COLS, oy + cell * ROWS, p);

        p.setColor(Color.parseColor("#22223a"));
        p.setStrokeWidth(1);
        for (int c = 0; c <= COLS; c++) canvas.drawLine(ox + c * cell, oy, ox + c * cell, oy + cell * ROWS, p);
        for (int r = 0; r <= ROWS; r++) canvas.drawLine(ox, oy + r * cell, ox + cell * COLS, oy + r * cell, p);

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] != 0) drawCell(canvas, ox, oy, cell, r, c, colorOf(grid[r][c]));

        for (int[] cellc : cur) {
            int r = curR + cellc[0], c = curC + cellc[1];
            if (r >= 0) drawCell(canvas, ox, oy, cell, r, c, colorOf(curType));
        }
    }

    private int colorOf(int typeIdx) { return goldSkin ? GOLD : COLORS[typeIdx - 1]; }

    private void drawCell(Canvas cv, int ox, int oy, int cell, int r, int c, int color) {
        p.setColor(color);
        RectF rf = new RectF(ox + c * cell + 2, oy + r * cell + 2, ox + (c + 1) * cell - 2, oy + (r + 1) * cell - 2);
        cv.drawRoundRect(rf, 5, 5, p);
    }
}
