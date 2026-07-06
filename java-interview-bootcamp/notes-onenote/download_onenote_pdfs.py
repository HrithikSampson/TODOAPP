#!/usr/bin/env python3
"""Export OneNote notebooks page-by-page and Zoho notes as PDF."""

from __future__ import annotations

import json
import re
import sys
import time
from io import BytesIO
from pathlib import Path

from PIL import Image
from playwright.sync_api import Frame, Page, sync_playwright
from pypdf import PdfWriter

BASE = Path(__file__).resolve().parent
INDEX = BASE / "onenote-index.json"
MAX_PAGE_HEIGHT = 18000
MAX_PAGE_WIDTH = 1400


def slugify(title: str) -> str:
    s = title.lower()
    s = re.sub(r"[^a-z0-9]+", "-", s)
    return s.strip("-")[:70] or "page"


def normalize_image(img: Image.Image) -> Image.Image:
    img = img.convert("RGB")
    if img.width > MAX_PAGE_WIDTH:
        ratio = MAX_PAGE_WIDTH / img.width
        img = img.resize((MAX_PAGE_WIDTH, int(img.height * ratio)), Image.Resampling.LANCZOS)
    if img.height > MAX_PAGE_HEIGHT:
        ratio = MAX_PAGE_HEIGHT / img.height
        img = img.resize((int(img.width * ratio), MAX_PAGE_HEIGHT), Image.Resampling.LANCZOS)
    return img


def images_to_pdf(images: list[Image.Image], dest: Path) -> None:
    writer = PdfWriter()
    temp_dir = dest.parent / ".tmp_pdf"
    temp_dir.mkdir(exist_ok=True)
    temp_files: list[Path] = []

    for i, img in enumerate(images):
        normalized = normalize_image(img)
        temp_pdf = temp_dir / f"page-{i:03d}.pdf"
        normalized.save(temp_pdf, "PDF", resolution=120.0)
        temp_files.append(temp_pdf)
        writer.append(str(temp_pdf))

    with dest.open("wb") as fh:
        writer.write(fh)

    for temp_pdf in temp_files:
        temp_pdf.unlink(missing_ok=True)
    if temp_dir.exists() and not any(temp_dir.iterdir()):
        temp_dir.rmdir()


def dismiss_dialog(frame: Frame) -> None:
    try:
        close = frame.query_selector('button[aria-label="close"], button[title="close"]')
        if close:
            close.click(timeout=3000)
            time.sleep(1)
    except Exception:
        pass
    try:
        frame.page.keyboard.press("Escape")
        time.sleep(0.5)
    except Exception:
        pass


def get_onenote_frame(page: Page, timeout: int = 120) -> Frame | None:
    deadline = time.time() + timeout
    while time.time() < deadline:
        for frame in page.frames:
            if "onenoteframe.aspx" in frame.url:
                return frame
        time.sleep(1)
    return None


def stitch_panel_screenshot(frame: Frame) -> Image.Image | None:
    panel = frame.query_selector("#WACViewPanel")
    if not panel:
        return None

    frame.evaluate(
        """() => {
        const p = document.querySelector('#WACViewPanel');
        if (!p) return;
        const step = 700;
        for (let y = 0; y < p.scrollHeight; y += step) p.scrollTop = y;
        p.scrollTop = 0;
    }"""
    )
    time.sleep(1.5)

    dims = frame.evaluate(
        """() => {
        const p = document.querySelector('#WACViewPanel');
        return {scrollHeight: p.scrollHeight, clientHeight: p.clientHeight};
    }"""
    )
    scroll_height = int(dims["scrollHeight"])
    client_height = int(dims["clientHeight"])
    if scroll_height <= 0 or client_height <= 0:
        return None

    strips: list[Image.Image] = []
    y = 0
    while y < scroll_height:
        frame.evaluate(f"() => {{ document.querySelector('#WACViewPanel').scrollTop = {y}; }}")
        time.sleep(0.4)
        png = panel.screenshot()
        img = Image.open(BytesIO(png))
        remaining = scroll_height - y
        if remaining < client_height:
            crop_h = max(1, min(img.height, int(img.height * remaining / client_height)))
            img = img.crop((0, 0, img.width, crop_h))
        strips.append(img)
        y += client_height

    if not strips:
        return None

    total_h = sum(s.height for s in strips)
    combined = Image.new("RGB", (strips[0].width, total_h), "white")
    offset = 0
    for strip in strips:
        combined.paste(strip, (0, offset))
        offset += strip.height
    return combined


def collect_sections(frame: Frame) -> list:
    sections = frame.query_selector_all('[role="treeitem"]')
    if not sections:
        return []
    return sections


def collect_pages(frame: Frame) -> list[tuple[str, object]]:
    pages = []
    for item in frame.query_selector_all(".pageItem"):
        name = item.inner_text().strip().split("\n")[0].strip()
        if name and name not in ("Add page",):
            pages.append((name, item))
    return pages


def export_onenote_notebook(page: Page, url: str, dest: Path) -> int:
    dest.parent.mkdir(parents=True, exist_ok=True)
    page.goto(url, wait_until="networkidle", timeout=180000)
    time.sleep(18)

    frame = get_onenote_frame(page)
    if not frame:
        print("  ERROR: OneNote frame not found")
        return 0

    dismiss_dialog(frame)
    time.sleep(2)

    images: list[Image.Image] = []
    captured: list[str] = []

    sections = collect_sections(frame)
    if not sections:
        sections = [None]

    for section in sections:
        if section is not None:
            try:
                section.click(timeout=8000)
                time.sleep(2)
            except Exception:
                pass

        page_entries = collect_pages(frame)
        print(f"  section pages: {len(page_entries)}")

        for page_name, page_el in page_entries:
            print(f"    -> {page_name}")
            try:
                page_el.click(timeout=10000)
                time.sleep(5)
                img = stitch_panel_screenshot(frame)
                if img and img.height > 100:
                    images.append(img)
                    captured.append(page_name)
                else:
                    print("       (empty capture, skipped)")
            except Exception as exc:
                print(f"       ERROR: {exc}")

    if not images:
        return 0

    images_to_pdf(images, dest)

    # Save per-page PNGs for reference
    pages_dir = dest.parent / (dest.stem + "_pages")
    pages_dir.mkdir(exist_ok=True)
    for i, (name, img) in enumerate(zip(captured, images), 1):
        fname = f"{i:02d}-{slugify(name)}.png"
        normalize_image(img).save(pages_dir / fname, optimize=True)

    manifest = pages_dir / "pages.json"
    manifest.write_text(json.dumps(captured, indent=2))
    return len(captured)


def export_zoho(page: Page, url: str, dest: Path) -> bool:
    dest.parent.mkdir(parents=True, exist_ok=True)
    page.goto(url, wait_until="networkidle", timeout=90000)
    time.sleep(4)
    page.pdf(path=str(dest), format="A4", print_background=True)
    return dest.exists() and dest.stat().st_size > 5000


def write_index(entries: list[dict], results: dict) -> None:
    lines = [
        "# OneNote & Zoho Notebook PDFs",
        "",
        "OneNote notebooks are exported **page by page** (all tabs) into a single combined PDF per notebook.",
        "Individual page screenshots are in `*_pages/` subfolders.",
        "",
        "| Category | Notebook / Note | Pages | File |",
        "|----------|-----------------|-------|------|",
    ]
    for e in entries:
        key = e["url"]
        r = results.get(key, {})
        if isinstance(r, dict) and r.get("file"):
            lines.append(
                f"| {e['category']} | {e['title']} | {r.get('pages', '?')} | [{r['file']}]({r['file']}) |"
            )
        elif r == "ok":
            lines.append(f"| {e['category']} | {e['title']} | 1 | PDF |")
        else:
            lines.append(f"| {e['category']} | {e['title']} | - | [link]({e['url']}) *(failed)* |")

    lines += [
        "",
        "## Re-export",
        "",
        "```bash",
        "python3 download_onenote_pdfs.py",
        "```",
        "",
    ]
    (BASE / "INDEX.md").write_text("\n".join(lines) + "\n")


def main() -> int:
    entries = json.loads(INDEX.read_text())
    results: dict = {}

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": 1800, "height": 1100})

        for i, e in enumerate(entries, 1):
            num = e.get("num", i)
            fname = f"{num:02d}-{slugify(e['title'])}.pdf"
            dest = BASE / e["category"] / fname
            print(f"[{i}/{len(entries)}] {e['type']}: {e['title']}")

            if e["type"] == "zoho":
                if export_zoho(page, e["url"], dest):
                    results[e["url"]] = "ok"
                    print("  OK (zoho)")
                else:
                    print("  FAILED")
            else:
                count = export_onenote_notebook(page, e["url"], dest)
                if count > 0:
                    rel = f"{e['category']}/{fname}"
                    results[e["url"]] = {"file": rel, "pages": count}
                    print(f"  OK — {count} pages -> {rel}")
                else:
                    print("  FAILED")

        browser.close()

    write_index(entries, results)
    return 0


if __name__ == "__main__":
    sys.exit(main())
