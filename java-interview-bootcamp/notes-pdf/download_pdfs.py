#!/usr/bin/env python3
"""Download and organize PDFs from the study index RTF."""

import json
import re
import subprocess
import sys
import time
from pathlib import Path

BASE = Path(__file__).resolve().parent
INDEX_JSON = BASE / "pdf-index.json"


def slugify(title: str) -> str:
    s = title.lower()
    s = re.sub(r"[^a-z0-9]+", "-", s)
    return s.strip("-")[:80] or "untitled"


def download_file(file_id: str, dest: Path) -> bool:
    if dest.exists() and dest.stat().st_size > 1000:
        return True
    dest.parent.mkdir(parents=True, exist_ok=True)
    url = f"https://drive.google.com/uc?export=download&id={file_id}"
    try:
        import gdown
        gdown.download(url, str(dest), quiet=True)
        if dest.exists() and dest.stat().st_size > 1000:
            return True
    except Exception as exc:
        print(f"  FAIL gdown: {exc}")

    # fallback: curl (works for most public Drive files)
    import subprocess
    result = subprocess.run(
        ["curl", "-L", "-s", "--fail", url, "-o", str(dest)],
        capture_output=True,
    )
    if result.returncode == 0 and dest.exists() and dest.stat().st_size > 1000:
        # verify PDF magic bytes
        if dest.read_bytes()[:4] == b"%PDF":
            return True
    if dest.exists():
        dest.unlink(missing_ok=True)
    return False


def write_index_md(entries: list[dict], results: dict) -> None:
  for category in sorted({e["category"] for e in entries}):
    cat_dir = BASE / category
    cat_entries = [e for e in entries if e["category"] == category]
    lines = [f"# {category.replace('-', ' ').title()} Notes\n"]
    for e in sorted(cat_entries, key=lambda x: x["num"]):
      fname = results.get(e["file_id"], "(not downloaded)")
      if fname != "(not downloaded)":
        lines.append(f"- [{e['num']}. {e['title']}]({fname})")
      else:
        lines.append(f"- {e['num']}. {e['title']} — [Drive link]({e['url']}) *(download failed)*")
    (cat_dir / "INDEX.md").write_text("\n".join(lines) + "\n")

  master = ["# Study PDF Index\n", "Extracted from the original Google Drive notes collection.\n"]
  master.append("| Category | Count | Folder |")
  master.append("|----------|-------|--------|")
  for category in sorted({e["category"] for e in entries}):
    cat_entries = [e for e in entries if e["category"] == category]
    ok = sum(1 for e in cat_entries if results.get(e["file_id"], "").endswith(".pdf"))
    master.append(f"| {category} | {ok}/{len(cat_entries)} | [{category}/]({category}/) |")
  master.append("\n## Recommended for this bootcamp\n")
  master.append("- **java/** — Topics 1–6\n- **spring-boot/** — Topics 7–11\n- **junit/** — Topic 12\n")
  master.append("\n> microservices/, lld/, hld/, event-driven/ are included for reference but skipped in the README curriculum.\n")
  (BASE / "INDEX.md").write_text("\n".join(master) + "\n")


def main() -> int:
    if not INDEX_JSON.exists():
        print("Run build_index.py first")
        return 1

    entries = json.loads(INDEX_JSON.read_text())
    results: dict[str, str] = {}
    ok = fail = 0

    for i, e in enumerate(entries, 1):
        category = e["category"]
        fname = f"{e['num']:02d}-{slugify(e['title'])}.pdf"
        dest = BASE / category / fname
        print(f"[{i}/{len(entries)}] {category}/{fname}")
        if download_file(e["file_id"], dest):
            results[e["file_id"]] = fname
            ok += 1
        else:
            fail += 1
        time.sleep(0.3)

    write_index_md(entries, results)
    print(f"\nDone: {ok} downloaded, {fail} failed")
    return 0 if fail == 0 else 0


if __name__ == "__main__":
    sys.exit(main())
