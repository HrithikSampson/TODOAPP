#!/usr/bin/env python3
"""Build pdf-index.json from the original RTF index file."""

import json
import re
import subprocess
from pathlib import Path

RTF_URL = "https://drive.google.com/uc?export=download&id=1lbM6DksojZ41szS7o4azXpub5Nxm7Rd6"
RTF_PATH = Path(__file__).resolve().parent / "SOURCE_INDEX.rtf"
OUT = Path(__file__).resolve().parent / "pdf-index.json"


def ensure_rtf() -> str:
    if not RTF_PATH.exists():
        import urllib.request
        print("Downloading source index RTF...")
        urllib.request.urlretrieve(RTF_URL, RTF_PATH)
    return subprocess.check_output(["strings", str(RTF_PATH)]).decode()


def build_index(text: str) -> list[dict]:
    current = "other"
    entries = []

    for line in text.split("\n"):
        line = line.strip().rstrip("\\")
        if not line:
            continue
        lower = line.lower()
        if "distributed microservices pdf" in lower:
            current = "microservices"
            continue
        if "event driven architecture" in lower:
            current = "event-driven"
            continue
        if "junit5 pdf" in lower:
            current = "junit"
            continue
        if "lld pdf" in lower:
            current = "lld"
            continue
        if "spring boot pdf" in lower:
            current = "spring-boot"
            continue
        if "java pdf" in lower:
            current = "java"
            continue
        if "hld pdf" in lower:
            current = "hld"
            continue

        m = re.search(
            r"(?:^|\s)(\d+)\.\s*(.+?)\s*:?\s*(https://drive\.google\.com/file/d/([A-Za-z0-9_-]+))",
            line,
        )
        if not m:
            m = re.search(
                r"(?:^|\s)(\d+)\.\s*(.+?)\s+(https://drive\.google\.com/file/d/([A-Za-z0-9_-]+))",
                line,
            )
        if m:
            num, title, url, fid = m.group(1), m.group(2).strip(), m.group(3), m.group(4)
            title = re.sub(r"\s+", " ", title).strip(" :")
            if title.lower() in ("wip", "not applicable"):
                continue
            entries.append(
                {
                    "category": current,
                    "num": int(num),
                    "title": title,
                    "file_id": fid,
                    "url": url,
                }
            )

    seen = set()
    unique = []
    for e in entries:
        if e["file_id"] not in seen:
            seen.add(e["file_id"])
            unique.append(e)
    return unique


if __name__ == "__main__":
    text = ensure_rtf()
    index = build_index(text)
    OUT.write_text(json.dumps(index, indent=2))
    print(f"Wrote {len(index)} entries to {OUT}")
