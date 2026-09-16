#!/usr/bin/env python3
# BlockDrop 우회 드라이버: spawn -> hook load -> resume -> keep alive
# 사용: python driver_blockdrop.py
import sys, time, frida

PKG = "com.reversinglab.blockdrop"
# 기본 훅 = 무한 골드(blockdrop_hook.js). 인자로 다른 훅(예: blockdrop_root_only.js) 지정 가능.
_HOOK_NAME = sys.argv[1] if len(sys.argv) > 1 else "blockdrop_hook.js"
HOOK = __file__.rsplit("\\", 1)[0] + "\\" + _HOOK_NAME

def on_message(msg, data):
    if msg.get("type") == "send":
        print("[send]", msg.get("payload"))
    elif msg.get("type") == "error":
        print("[error]", msg.get("stack") or msg.get("description"))

def main():
    dev = frida.get_device_manager().add_remote_device("127.0.0.1:27043")
    print("[*] spawning", PKG)
    pid = dev.spawn([PKG])
    session = dev.attach(pid)
    with open(HOOK, "r", encoding="utf-8") as f:
        src = f.read()
    script = session.create_script(src)
    script.on("message", on_message)
    script.load()
    print("[*] hook loaded, resuming pid", pid)
    dev.resume(pid)
    print("[*] running. Ctrl+C to detach.")
    while True:
        time.sleep(1)

if __name__ == "__main__":
    main()
