// ================================================================
//  BlockDrop 리버싱 훅 (초보용 · 제일 쉬운 패턴)
//  ① 루팅 우회: SecurityCheck.isDeviceRooted() -> false
//  ② 골드 조작: Wallet.getGold() -> 999999
//
//  ★ Frida 17 함정: 전역 Java 가 코어에서 제거됨.
//    그냥 Java.perform 쓰면 "ReferenceError: 'Java' is not defined".
//    아래처럼 frida-java-bridge 를 import 하고 frida-compile 로 번들링해야 함.
//
//  컴파일:  npx frida-compile blockdrop_hook_src.js -o blockdrop_hook.js
//  실행  :  frida -H 127.0.0.1:27043 -f com.reversinglab.blockdrop -l blockdrop_hook.js
//          (반드시 -f spawn: 루팅체크가 onCreate 초반에 돌기 때문)
// ================================================================
import Java from "frida-java-bridge";
globalThis.Java = Java;

Java.perform(function () {
    var PKG = "com.reversinglab.blockdrop";

    // ① 루팅 우회 — boolean 하나 뒤집기(헬로월드 패턴)
    var SecurityCheck = Java.use(PKG + ".SecurityCheck");
    SecurityCheck.isDeviceRooted.implementation = function () {
        console.log("[root] SecurityCheck.isDeviceRooted() -> false");
        return false;
    };

    // ② 골드 조작 — getGold() 를 큰 값으로
    var Wallet = Java.use(PKG + ".Wallet");
    Wallet.getGold.implementation = function () {
        return 999999;    // 상점 잔액 확인도 이걸 보므로 무한 구매
    };

    console.log("[*] BlockDrop hooks installed: root bypass + gold=999999");
});
