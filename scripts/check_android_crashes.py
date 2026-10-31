"""Fail release verification for app crashes; retain unrelated emulator evidence."""
import pathlib
import re
import sys

PACKAGE = "com.migraineguard.app"

def classify(text):
    processes = re.findall(r"Process:\s*([A-Za-z0-9_.:]+)", text)
    own = [p for p in processes if p == PACKAGE or p.startswith(PACKAGE + ":") or p.startswith(PACKAGE + ".")]
    blocks = re.split(r"(?m)(?=^.*\bFATAL EXCEPTION:)", text)
    unknown = any("FATAL EXCEPTION:" in block and not re.search(r"Process:\s*[A-Za-z0-9_.:]+", block) for block in blocks)
    return bool(own) or unknown, sorted(set(processes) - set(own))

def main():
    text = pathlib.Path(sys.argv[1]).read_text(encoding="utf-8", errors="replace")
    failed, unrelated = classify(text)
    if failed:
        print("App or unidentified Android fatal exception detected; inspect the retained complete log.")
        return 1
    if unrelated:
        print("::warning::Unrelated Android process crash retained: " + ", ".join(unrelated))
    print("No MigraineGuard fatal exception detected.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
