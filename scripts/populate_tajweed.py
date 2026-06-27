#!/usr/bin/env python3
"""
Fetch tajweed-annotated Uthmani text from Quran.com API (via curl)
and populate the `text_tajweed` column in qurannew.db.

Usage:
    python3 populate_tajweed.py
"""

import json
import sqlite3
import subprocess
import time
import sys
import os

DB_PATH = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "..",
    "app", "src", "main", "assets", "quran", "qurannew.db"
)

API_BASE = "https://api.quran.com/api/v4/quran/verses/uthmani_tajweed"
TOTAL_SURAHS = 114
REQUEST_DELAY = 1.5  # seconds between requests
MAX_RETRIES = 3
USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"


def fetch_chapter(chapter):
    """Fetch tajweed verses for a single chapter using curl."""
    url = f"{API_BASE}?chapter_number={chapter}"
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            result = subprocess.run(
                ["curl", "-s", "-f", "--max-time", "30",
                 "-H", f"User-Agent: {USER_AGENT}",
                 "-H", "Accept: application/json",
                 url],
                capture_output=True, text=True, timeout=35
            )
            if result.returncode != 0:
                raise RuntimeError(f"curl exit {result.returncode}")
            data = json.loads(result.stdout)
            return data.get("verses", [])
        except Exception as e:
            print(f"  [attempt {attempt}/{MAX_RETRIES}] {e}")
            if attempt < MAX_RETRIES:
                time.sleep(2 ** attempt)
            else:
                print(f"  FAILED surah {chapter}")
                return []


def ensure_column(conn):
    """Add text_tajweed column if it doesn't exist."""
    cursor = conn.execute("PRAGMA table_info(ayas)")
    columns = {row[1] for row in cursor.fetchall()}
    if "text_tajweed" not in columns:
        conn.execute("ALTER TABLE ayas ADD COLUMN text_tajweed TEXT")
        conn.commit()
        print("+ Added column 'text_tajweed' to ayas table")
    else:
        print("= Column 'text_tajweed' already exists")


def populate(conn):
    """Download and insert tajweed text for all 114 surahs."""
    total_updated = 0

    for chapter in range(1, TOTAL_SURAHS + 1):
        print(f"[{chapter:3d}/114] Surah {chapter}...", end=" ", flush=True)
        verses = fetch_chapter(chapter)

        if not verses:
            print("SKIP")
            continue

        updated = 0
        for v in verses:
            verse_key = v.get("verse_key", "")
            tajweed_text = v.get("text_uthmani_tajweed", "")
            if not verse_key or not tajweed_text:
                continue

            parts = verse_key.split(":")
            if len(parts) != 2:
                continue

            sura, aya = int(parts[0]), int(parts[1])
            conn.execute(
                "UPDATE ayas SET text_tajweed = ? WHERE sura = ? AND aya = ?",
                (tajweed_text, sura, aya),
            )
            updated += 1

        conn.commit()
        total_updated += updated
        print(f"{updated} ayat OK")

        if chapter < TOTAL_SURAHS:
            time.sleep(REQUEST_DELAY)

    return total_updated


def verify(conn):
    """Print verification stats."""
    cursor = conn.execute(
        "SELECT COUNT(*) FROM ayas WHERE text_tajweed IS NOT NULL AND text_tajweed != ''"
    )
    filled = cursor.fetchone()[0]

    cursor = conn.execute("SELECT COUNT(*) FROM ayas")
    total = cursor.fetchone()[0]

    cursor = conn.execute(
        "SELECT text_tajweed FROM ayas WHERE sura = 1 AND aya = 1"
    )
    sample = cursor.fetchone()

    print(f"\n{'='*50}")
    print(f"Tajweed populated: {filled}/{total} ayat")
    if sample and sample[0]:
        print(f"Sample (1:1): {sample[0][:120]}...")
    else:
        print("No sample found")
    print(f"{'='*50}")
    return filled


def main():
    db_path = os.path.abspath(DB_PATH)
    print(f"Database: {db_path}")

    if not os.path.exists(db_path):
        print(f"ERROR: Not found: {db_path}")
        sys.exit(1)

    conn = sqlite3.connect(db_path)
    try:
        ensure_column(conn)
        print(f"\nFetching tajweed for {TOTAL_SURAHS} surahs (delay {REQUEST_DELAY}s)...\n")
        updated = populate(conn)
        print(f"\nDone! Total updated: {updated}")
        verify(conn)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
