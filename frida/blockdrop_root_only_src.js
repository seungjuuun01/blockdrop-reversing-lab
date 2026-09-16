// ================================================================
//  BlockDrop 훅 (루팅 우회만) — 골드는 건드리지 않음
//  → 메시지함 보상 수령/돈 초기화 같은 '진짜 골드 변화'를 화면에서 확인할 때 사용.
//  (무한 골드 데모는 blockdrop_hook.js 사용)
//
//  컴파일:  npx frida-compile blockdrop_root_only_src.js -o blockdrop_root_only.js
//  실행  :  frida -H 127.0.0.1:27043 -f com.reversinglab.blockdrop -l blockdrop_root_only.js
// ================================================================
import Java from "frida-java-bridge";
globalThis.Java = Java;

Java.perform(function () {
    Java.use("com.reversinglab.blockdrop.SecurityCheck").isDeviceRooted.implementation = function () {
        console.log("[root] isDeviceRooted() -> false");
        return false;
    };
    console.log("[*] BlockDrop: 루팅 우회만 (골드 후킹 안 함)");
});
