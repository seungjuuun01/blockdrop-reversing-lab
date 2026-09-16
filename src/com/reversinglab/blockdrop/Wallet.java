package com.reversinglab.blockdrop;

/**
 * Wallet — 게임 내 골드(재화) 지갑.
 *
 * ★ 리버싱 포인트:
 *   골드는 전적으로 '클라이언트 메모리 상태'다(서버 검증 없음).
 *   getGold() 를 후킹해 큰 값을 돌려주면 상점 구매가 무한히 통과된다.
 *   spend() 가 getGold() 를 통해 잔액을 확인하므로, getGold() 하나만 후킹하면 끝.
 */
public class Wallet {

    private static int gold = 0;

    /** ★ Frida 후킹 대상: 이 값을 999999 로 돌려주면 무한 골드. */
    public static int getGold() {
        return gold;
    }

    /** 줄을 지우면 골드 획득. */
    public static void addGold(int amount) {
        gold += amount;
    }

    /** 상점 구매: 잔액 확인은 getGold() 를 통해서 한다(후킹 지점 일원화). */
    public static boolean spend(int amount) {
        if (getGold() >= amount) {
            gold -= amount;
            return true;
        }
        return false;
    }

    public static void reset() {
        gold = 0;
    }
}
