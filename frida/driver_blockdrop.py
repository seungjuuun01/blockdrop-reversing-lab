#!/usr/bin/env python3
# BlockDrop 우회 드라이버: spawn -> hook load -> resume -> keep alive
# 사용: python driver_blockdrop.py
import sys, time, frida

PKG = "com.reversinglab.blockdrop"
HOOK = __file__.rsplit("\\", 1)[0] + "\\blockdrop_hook.js"

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
