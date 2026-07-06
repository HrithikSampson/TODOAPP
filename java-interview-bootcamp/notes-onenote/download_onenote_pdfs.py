#!/usr/bin/env python3
"""Export OneNote (OneDrive) and Zoho Notebook links as PDF files."""

import json
import re
import sys
import time
from pathlib import Path

from PIL import Image
from playwright.sync_api import sync_playwright

BASE = Path(__file__).resolve().parent
INDEX = BASE / "onenote-index.json"


def slugify(title: str) -> str:
    s = title.lower()
    s = re.sub(r"[^a-z0-9]+", "-", s)
    return s.strip("-")[:80] or "note"


def screenshot_to_pdf(png_path: Path, pdf_path: Path) -> None:
    img = Image.open(png_path)
    if img.mode in ("RGBA", "P"):
        img = img.convert("RGB")
    img.save(pdf_path, "PDF", resolution=150.0)


def export_zoho(page, url: str, dest: Path) -> bool:
    page.goto(url, wait_until="networkidle", timeout=90000)
    time.sleep(4)
    page.pdf(path=str(dest), format="A4", print_background=True)
    return dest.exists() and dest.stat().st_size > 5000


def export_onenote(page, url: str, dest: Path, png_tmp: Path) -> bool:
    page.goto(url, wait_until="domcontentloaded", timeout=90000)
    time.sleep(18)
    page.screenshot(path=str(png_tmp), full_page=True)
    if png_tmp.stat().st_size < 10000:
        return False
    screenshot_to_pdf(png_tmp, dest)
    png_tmp.unlink(missing_ok=True)
    return dest.exists() and dest.stat().st_size > 5000


def write_index(entries: list[dict], results: dict) -> None:
    lines = [
        "# OneNote & Zoho Notebook PDFs\n",
        "Exported from OneDrive OneNote and Zoho Notebook public links in the original study index.\n",
        "| Category | File | Source |",
        "|----------|------|--------|",
    ]
    for e in entries:
        key = e["url"]
        fname = results.get(key, "FAILED")
        src = "OneNote/OneDrive" if e["type"] == "onedrive" else "Zoho Notebook"
        if fname != "FAILED":
            lines.append(f"| {e['category']} | [{e['title']}]({fname}) | {src} |")
        else:
            lines.append(f"| {e['category']} | {e['title']} *(failed)* | [{src}]({e['url']}) |")

    lines.append("\n## Notes\n")
    lines.append("- **Zoho** pages export as full text PDFs.\n")
    lines.append("- **OneNote** notebooks are interactive; export uses a full-page screenshot converted to PDF. ")
    lines.append("Open the original link for full navigation between pages.\n")
    lines.append("- Re-run: `python3 download_onenote_pdfs.py`\n")
    (BASE / "INDEX.md").write_text("\n".join(lines) + "\n")


def main() -> int:
    entries = json.loads(INDEX.read_text())
    results: dict[str, str] = {}
    ok = fail = 0

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": 1400, "height": 900})

        for i, e in enumerate(entries, 1):
            num = e.get("num", i)
            fname = f"{num:02d}-{slugify(e['title'])}.pdf"
            dest = BASE / e["category"] / fname
            dest.parent.mkdir(parents=True, exist_ok=True)
            print(f"[{i}/{len(entries)}] {e['type']} -> {dest.relative_to(BASE)}")

            try:
                if e["type"] == "zoho":
                    success = export_zoho(page, e["url"], dest)
                else:
                    png_tmp = dest.with_suffix(".png")
                    success = export_onenote(page, e["url"], dest, png_tmp)

                if success:
                    results[e["url"]] = f"{e['category']}/{fname}"
                    ok += 1
                    print("  OK")
                else:
                    fail += 1
                    print("  FAILED (empty or too small)")
            except Exception as exc:
                fail += 1
                print(f"  ERROR: {exc}")

        browser.close()

    write_index(entries, results)
    print(f"\nDone: {ok} ok, {fail} failed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
