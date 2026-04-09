#!/usr/bin/env python3
"""PEM Tracker — Pediatric Emergency Medicine Research Dashboard.

Aggregates the latest PEM research from PubMed and major journal RSS feeds
into a single browsable dashboard with bookmarking and topic filtering.
"""

import hashlib
import json
import re
import sqlite3
import time
from datetime import datetime, timedelta
from pathlib import Path
from xml.etree import ElementTree

import feedparser
import requests
from flask import Flask, g, jsonify, redirect, render_template, request, url_for

app = Flask(__name__)
DB_PATH = Path(__file__).parent / "pemtracker.db"

# ── PEM Topics & Classification ──────────────────────────────────────────────

PEM_TOPICS = {
    "Resuscitation & Shock": [
        "resuscitation", "cardiac arrest", "shock", "septic shock",
        "CPR", "ROSC", "PALS", "fluid resuscitation", "vasopressor",
    ],
    "Trauma": [
        "trauma", "head injury", "TBI", "concussion", "fracture",
        "abdominal trauma", "spleen", "C-spine", "PECARN",
    ],
    "Respiratory": [
        "asthma", "bronchiolitis", "croup", "pneumonia", "respiratory distress",
        "wheezing", "intubation", "high-flow nasal", "HFNC", "RSV",
    ],
    "Infectious Disease": [
        "sepsis", "meningitis", "UTI", "urinary tract infection", "fever",
        "febrile infant", "bacteremia", "cellulitis", "abscess", "COVID",
        "influenza", "streptococcal",
    ],
    "Neurology": [
        "seizure", "status epilepticus", "encephalitis", "headache",
        "altered mental status", "stroke", "shunt malfunction",
    ],
    "Toxicology": [
        "poisoning", "ingestion", "overdose", "toxicology", "acetaminophen",
        "opioid", "naloxone", "toxic", "envenomation",
    ],
    "Procedures & Airway": [
        "procedural sedation", "intubation", "lumbar puncture",
        "intraosseous", "ultrasound", "point-of-care", "POCUS",
        "fracture reduction", "laceration", "rapid sequence",
    ],
    "Neonatal": [
        "neonate", "neonatal", "newborn", "jaundice", "bilirubin",
        "ductus", "congenital heart", "pyloric stenosis",
    ],
    "Cardiology": [
        "myocarditis", "SVT", "supraventricular", "Kawasaki",
        "arrhythmia", "ECG", "troponin", "pericarditis", "murmur",
    ],
    "GI & Surgical": [
        "appendicitis", "intussusception", "volvulus", "obstruction",
        "pancreatitis", "GI bleed", "foreign body", "testicular torsion",
    ],
    "Child Abuse": [
        "child abuse", "non-accidental", "NAT", "abusive head trauma",
        "bruising", "neglect",
    ],
    "Mental Health": [
        "suicidal", "suicide", "psychiatric", "mental health",
        "behavioral", "anxiety", "depression", "agitation",
    ],
    "Quality & Systems": [
        "triage", "overcrowding", "boarding", "quality improvement",
        "patient safety", "diagnostic error", "handoff", "simulation",
        "telemedicine", "health equity", "disparity",
    ],
}

# ── Journal RSS Feeds ────────────────────────────────────────────────────────

JOURNAL_FEEDS = {
    # ── Priority 1: PEM-specific / highest-cited PEM journals ────────────────
    "Pediatric Emergency Care": "https://journals.lww.com/pec-online/pages/issuelist.aspx?desktopMode=true",
    "Annals of Emergency Medicine": "http://rss.sciencedirect.com/publication/science/01960644",
    "Academic Emergency Medicine": "https://onlinelibrary.wiley.com/action/showFeed?jc=15532712&type=etoc&feed=rss",
    "Clinical Pediatric Emergency Medicine": "http://rss.sciencedirect.com/publication/science/15228401",
    # ── Priority 2: Top pediatrics journals with heavy PEM content ───────────
    "Pediatrics (AAP)": "https://feeds.aappublications.org/rss/pediatrics.xml",
    "JAMA Pediatrics": "https://jamanetwork.com/rss/site_12/67.xml",
    "Journal of Pediatrics": "http://rss.sciencedirect.com/publication/science/00223476",
    "Archives of Disease in Childhood": "https://adc.bmj.com/rss/current.xml",
    # ── Priority 3: Canadian EM (fellowship-relevant) ────────────────────────
    "CJEM": "https://onlinelibrary.wiley.com/action/showFeed?jc=14818035&type=etoc&feed=rss",
    # ── Priority 4: Broader EM with PEM review series ────────────────────────
    "American Journal of Emergency Medicine": "http://rss.sciencedirect.com/publication/science/07356757",
    "Emergency Medicine Journal": "https://emj.bmj.com/rss/current.xml",
    # ── Priority 5: Critical care & supporting journals ──────────────────────
    "Pediatric Critical Care Medicine": "http://rss.sciencedirect.com/publication/science/15297535",
    "Lancet Child & Adolescent Health": "http://rss.sciencedirect.com/publication/science/23524642",
    "Resuscitation": "http://rss.sciencedirect.com/publication/science/03009572",
    "Pediatric Research": "https://www.nature.com/pr.rss",
}

# ── Database ─────────────────────────────────────────────────────────────────


def get_db() -> sqlite3.Connection:
    if "db" not in g:
        g.db = sqlite3.connect(str(DB_PATH))
        g.db.row_factory = sqlite3.Row
    return g.db


@app.teardown_appcontext
def close_db(exc):
    db = g.pop("db", None)
    if db is not None:
        db.close()


def init_db():
    db = sqlite3.connect(str(DB_PATH))
    db.executescript("""
        CREATE TABLE IF NOT EXISTS articles (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            authors TEXT,
            abstract TEXT,
            journal TEXT,
            pub_date TEXT,
            url TEXT,
            source TEXT,
            topic TEXT,
            fetched_at TEXT NOT NULL,
            pmid TEXT
        );
        CREATE TABLE IF NOT EXISTS bookmarks (
            article_id TEXT PRIMARY KEY,
            created_at TEXT NOT NULL,
            FOREIGN KEY (article_id) REFERENCES articles(id)
        );
        CREATE TABLE IF NOT EXISTS read_status (
            article_id TEXT PRIMARY KEY,
            read_at TEXT NOT NULL,
            FOREIGN KEY (article_id) REFERENCES articles(id)
        );
        CREATE TABLE IF NOT EXISTS fetch_log (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            source TEXT,
            fetched_at TEXT,
            article_count INTEGER
        );
        CREATE INDEX IF NOT EXISTS idx_articles_topic ON articles(topic);
        CREATE INDEX IF NOT EXISTS idx_articles_pub_date ON articles(pub_date);
        CREATE INDEX IF NOT EXISTS idx_articles_source ON articles(source);
    """)
    db.close()


# ── Article classification ───────────────────────────────────────────────────


def classify_article(title: str, abstract: str = "") -> str:
    text = f"{title} {abstract}".lower()
    scores = {}
    for topic, keywords in PEM_TOPICS.items():
        score = sum(1 for kw in keywords if kw.lower() in text)
        if score > 0:
            scores[topic] = score
    if scores:
        return max(scores, key=scores.get)
    return "General PEM"


def make_article_id(title: str, journal: str) -> str:
    raw = f"{title.lower().strip()}|{journal.lower().strip()}"
    return hashlib.md5(raw.encode()).hexdigest()


# ── PubMed fetching ──────────────────────────────────────────────────────────

PUBMED_SEARCH = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi"
PUBMED_FETCH = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"

PEM_QUERIES = [
    # Core PEM
    '"pediatric emergency medicine"',
    '"pediatric emergency department"',
    '"paediatric emergency department"',
    # PEM chief complaints & presentations
    '"febrile infant" OR "febrile neonate" OR "fever without source"',
    '"pediatric sepsis" OR "septic shock" AND (child OR pediatric OR infant)',
    '"pediatric trauma" OR "pediatric head injury" OR "PECARN" OR "pediatric TBI"',
    '"bronchiolitis" OR "croup" OR "pediatric asthma" OR "pediatric respiratory distress"',
    '"status epilepticus" AND (pediatric OR child) OR "pediatric seizure"',
    '"pediatric resuscitation" OR "PALS" OR "pediatric cardiac arrest"',
    # PEM procedures & diagnostics
    '"pediatric procedural sedation" OR "pediatric intubation" OR "pediatric RSI"',
    '"pediatric POCUS" OR "pediatric point-of-care ultrasound"',
    '"pediatric intraosseous" OR "pediatric lumbar puncture"',
    # PEM specific conditions
    '"pediatric appendicitis" OR "intussusception" OR "pyloric stenosis"',
    '"pediatric ingestion" OR "pediatric poisoning" OR "pediatric toxicology"',
    '"child abuse" OR "non-accidental trauma" OR "abusive head trauma"',
    '"pediatric anaphylaxis" OR "pediatric allergic reaction"',
    '"DKA" AND (pediatric OR child) OR "pediatric diabetic ketoacidosis"',
    '"Kawasaki" OR "pediatric myocarditis" OR "pediatric SVT"',
    '"pediatric suicidal" OR "pediatric psychiatric emergency"',
    # PEM systems & quality
    '"pediatric triage" OR "pediatric readiness" OR "pediatric emergency" AND "quality improvement"',
    '"pediatric emergency" AND (guideline OR consensus OR protocol OR pathway)',
]


def fetch_pubmed_articles(days_back: int = 30, max_results: int = 50) -> list[dict]:
    articles = []
    seen_pmids = set()

    for query in PEM_QUERIES:
        try:
            search_resp = requests.get(PUBMED_SEARCH, params={
                "db": "pubmed",
                "term": query,
                "retmax": max_results,
                "sort": "date",
                "datetype": "pdat",
                "reldate": days_back,
                "retmode": "json",
            }, timeout=15)
            search_data = search_resp.json()
            id_list = search_data.get("esearchresult", {}).get("idlist", [])

            new_ids = [pid for pid in id_list if pid not in seen_pmids]
            if not new_ids:
                continue
            seen_pmids.update(new_ids)

            fetch_resp = requests.get(PUBMED_FETCH, params={
                "db": "pubmed",
                "id": ",".join(new_ids),
                "retmode": "xml",
            }, timeout=20)

            root = ElementTree.fromstring(fetch_resp.content)
            for article_el in root.findall(".//PubmedArticle"):
                try:
                    a = _parse_pubmed_article(article_el)
                    if a:
                        articles.append(a)
                except Exception:
                    continue

            time.sleep(0.4)  # respect NCBI rate limits
        except Exception:
            continue

    return articles


def _parse_pubmed_article(el) -> dict | None:
    medline = el.find(".//MedlineCitation")
    if medline is None:
        return None

    pmid_el = medline.find("PMID")
    pmid = pmid_el.text if pmid_el is not None else ""

    art = medline.find("Article")
    if art is None:
        return None

    title_el = art.find("ArticleTitle")
    title = title_el.text if title_el is not None else ""
    if not title:
        return None

    abstract_parts = []
    for abs_text in art.findall(".//AbstractText"):
        label = abs_text.get("Label", "")
        text = abs_text.text or ""
        if label:
            abstract_parts.append(f"**{label}**: {text}")
        else:
            abstract_parts.append(text)
    abstract = " ".join(abstract_parts)

    authors = []
    for author in art.findall(".//Author"):
        last = author.findtext("LastName", "")
        fore = author.findtext("ForeName", "")
        if last:
            authors.append(f"{last} {fore}".strip())

    journal_el = art.find(".//Journal/Title")
    journal = journal_el.text if journal_el is not None else "Unknown"

    pub_date = ""
    date_el = art.find(".//ArticleDate")
    if date_el is not None:
        y = date_el.findtext("Year", "")
        m = date_el.findtext("Month", "01")
        d = date_el.findtext("Day", "01")
        pub_date = f"{y}-{m.zfill(2)}-{d.zfill(2)}"
    else:
        pd_el = art.find(".//Journal/JournalIssue/PubDate")
        if pd_el is not None:
            y = pd_el.findtext("Year", "")
            m = pd_el.findtext("Month", "01")
            if not m.isdigit():
                month_map = {"jan": "01", "feb": "02", "mar": "03", "apr": "04",
                             "may": "05", "jun": "06", "jul": "07", "aug": "08",
                             "sep": "09", "oct": "10", "nov": "11", "dec": "12"}
                m = month_map.get(m.lower()[:3], "01")
            d = pd_el.findtext("Day", "01")
            pub_date = f"{y}-{m.zfill(2)}-{d.zfill(2)}"

    topic = classify_article(title, abstract)
    article_id = make_article_id(title, journal)
    url = f"https://pubmed.ncbi.nlm.nih.gov/{pmid}/" if pmid else ""

    return {
        "id": article_id,
        "title": title,
        "authors": ", ".join(authors[:5]) + (" et al." if len(authors) > 5 else ""),
        "abstract": abstract,
        "journal": journal,
        "pub_date": pub_date,
        "url": url,
        "source": "pubmed",
        "topic": topic,
        "pmid": pmid,
    }


# ── RSS feed fetching ────────────────────────────────────────────────────────


def fetch_rss_articles() -> list[dict]:
    articles = []
    # Must match at least one ED/emergency keyword AND one pediatric keyword
    ed_keywords = {
        "emergency", "emergencies", "acute", "critical", "triage", "resuscitation",
        "trauma", "shock", "sepsis", "arrest", "intubation", "sedation",
        "POCUS", "ultrasound", "intraosseous", "anaphylaxis", "status epilepticus",
        "ingestion", "poisoning", "overdose", "DKA", "diabetic ketoacidosis",
        "appendicitis", "intussusception", "croup", "bronchiolitis", "asthma",
        "febrile", "fever", "meningitis", "seizure", "fracture", "laceration",
        "abuse", "non-accidental", "suicidal", "psychiatric",
        "Kawasaki", "SVT", "myocarditis", "foreign body", "burns",
    }
    peds_keywords = {
        "pediatric", "paediatric", "child", "children", "infant", "infants",
        "neonatal", "neonate", "neonates", "newborn", "adolescent", "toddler",
        "PALS", "PECARN", "PEM", "pediatric emergency",
    }
    # Also allow direct PEM topic keyword matches
    pem_topic_keywords = set()
    for kws in PEM_TOPICS.values():
        pem_topic_keywords.update(kw.lower() for kw in kws)

    for journal_name, feed_url in JOURNAL_FEEDS.items():
        try:
            feed = feedparser.parse(feed_url)
            for entry in feed.entries[:30]:
                title = entry.get("title", "")
                summary = entry.get("summary", "")
                text = f"{title} {summary}".lower()

                # Filter: must be relevant to pediatric emergency medicine
                has_ed = any(kw in text for kw in ed_keywords)
                has_peds = any(kw in text for kw in peds_keywords)
                has_pem_topic = any(kw in text for kw in pem_topic_keywords)
                # Require BOTH emergency + pediatric, OR a direct PEM topic match with pediatric
                if not ((has_ed and has_peds) or (has_pem_topic and has_peds)):
                    continue

                pub_date = ""
                if hasattr(entry, "published_parsed") and entry.published_parsed:
                    pub_date = time.strftime("%Y-%m-%d", entry.published_parsed)
                elif hasattr(entry, "updated_parsed") and entry.updated_parsed:
                    pub_date = time.strftime("%Y-%m-%d", entry.updated_parsed)

                authors = entry.get("author", "")
                link = entry.get("link", "")
                topic = classify_article(title, summary)
                article_id = make_article_id(title, journal_name)

                # Strip HTML tags from summary
                clean_summary = re.sub(r"<[^>]+>", "", summary)

                articles.append({
                    "id": article_id,
                    "title": title,
                    "authors": authors,
                    "abstract": clean_summary[:2000],
                    "journal": journal_name,
                    "pub_date": pub_date,
                    "url": link,
                    "source": "rss",
                    "topic": topic,
                    "pmid": "",
                })
        except Exception:
            continue

    return articles


# ── Store articles ───────────────────────────────────────────────────────────


def store_articles(articles: list[dict]) -> int:
    db = sqlite3.connect(str(DB_PATH))
    stored = 0
    for a in articles:
        try:
            db.execute("""
                INSERT OR IGNORE INTO articles
                (id, title, authors, abstract, journal, pub_date, url, source, topic, fetched_at, pmid)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                a["id"], a["title"], a["authors"], a["abstract"],
                a["journal"], a["pub_date"], a["url"], a["source"],
                a["topic"], datetime.now().isoformat(), a.get("pmid", ""),
            ))
            stored += db.total_changes
        except Exception:
            continue
    db.commit()
    db.close()
    return stored


# ── Routes ───────────────────────────────────────────────────────────────────


@app.route("/")
def index():
    db = get_db()
    topic = request.args.get("topic", "")
    search = request.args.get("q", "")
    show = request.args.get("show", "all")  # all, bookmarked, unread
    page = int(request.args.get("page", 1))
    per_page = 20

    query = "SELECT a.*, b.created_at as bookmarked, r.read_at as read_date FROM articles a"
    query += " LEFT JOIN bookmarks b ON a.id = b.article_id"
    query += " LEFT JOIN read_status r ON a.id = r.article_id"
    conditions = []
    params = []

    if topic:
        conditions.append("a.topic = ?")
        params.append(topic)
    if search:
        conditions.append("(a.title LIKE ? OR a.abstract LIKE ?)")
        params.extend([f"%{search}%", f"%{search}%"])
    if show == "bookmarked":
        conditions.append("b.article_id IS NOT NULL")
    elif show == "unread":
        conditions.append("r.article_id IS NULL")

    if conditions:
        query += " WHERE " + " AND ".join(conditions)

    # Count total
    count_q = query.replace(
        "SELECT a.*, b.created_at as bookmarked, r.read_at as read_date FROM",
        "SELECT COUNT(*) FROM"
    ).replace(
        " LEFT JOIN bookmarks b ON a.id = b.article_id"
        " LEFT JOIN read_status r ON a.id = r.article_id", ""
    )
    # Simpler count approach
    count_row = db.execute(f"SELECT COUNT(*) FROM ({query})", params).fetchone()
    total = count_row[0]

    query += " ORDER BY a.pub_date DESC LIMIT ? OFFSET ?"
    params.extend([per_page, (page - 1) * per_page])

    articles = db.execute(query, params).fetchall()

    # Topic counts
    topic_counts = db.execute(
        "SELECT topic, COUNT(*) as cnt FROM articles GROUP BY topic ORDER BY cnt DESC"
    ).fetchall()

    # Stats
    total_articles = db.execute("SELECT COUNT(*) FROM articles").fetchone()[0]
    total_bookmarked = db.execute("SELECT COUNT(*) FROM bookmarks").fetchone()[0]
    total_unread = db.execute(
        "SELECT COUNT(*) FROM articles a LEFT JOIN read_status r ON a.id = r.article_id WHERE r.article_id IS NULL"
    ).fetchone()[0]
    last_fetch = db.execute(
        "SELECT fetched_at FROM fetch_log ORDER BY id DESC LIMIT 1"
    ).fetchone()

    total_pages = max(1, (total + per_page - 1) // per_page)

    return render_template("index.html",
        articles=articles,
        topics=PEM_TOPICS.keys(),
        topic_counts=topic_counts,
        current_topic=topic,
        search=search,
        show=show,
        page=page,
        total_pages=total_pages,
        total=total,
        stats={
            "total": total_articles,
            "bookmarked": total_bookmarked,
            "unread": total_unread,
            "last_fetch": last_fetch["fetched_at"][:16] if last_fetch else "Never",
        },
    )


@app.route("/article/<article_id>")
def article_detail(article_id):
    db = get_db()
    article = db.execute(
        "SELECT a.*, b.created_at as bookmarked, r.read_at as read_date FROM articles a"
        " LEFT JOIN bookmarks b ON a.id = b.article_id"
        " LEFT JOIN read_status r ON a.id = r.article_id"
        " WHERE a.id = ?", (article_id,)
    ).fetchone()
    if not article:
        return "Not found", 404

    # Mark as read
    db.execute(
        "INSERT OR IGNORE INTO read_status (article_id, read_at) VALUES (?, ?)",
        (article_id, datetime.now().isoformat())
    )
    db.commit()
    return render_template("article.html", article=article)


@app.route("/bookmark/<article_id>", methods=["POST"])
def toggle_bookmark(article_id):
    db = get_db()
    existing = db.execute("SELECT 1 FROM bookmarks WHERE article_id = ?", (article_id,)).fetchone()
    if existing:
        db.execute("DELETE FROM bookmarks WHERE article_id = ?", (article_id,))
    else:
        db.execute("INSERT INTO bookmarks (article_id, created_at) VALUES (?, ?)",
                   (article_id, datetime.now().isoformat()))
    db.commit()
    return redirect(request.referrer or url_for("index"))


@app.route("/refresh", methods=["POST"])
def refresh_articles():
    days = int(request.form.get("days", 30))
    pubmed_articles = fetch_pubmed_articles(days_back=days)
    rss_articles = fetch_rss_articles()

    all_articles = pubmed_articles + rss_articles
    stored = store_articles(all_articles)

    db = get_db()
    db.execute("INSERT INTO fetch_log (source, fetched_at, article_count) VALUES (?, ?, ?)",
               ("all", datetime.now().isoformat(), len(all_articles)))
    db.commit()

    return redirect(url_for("index"))


@app.route("/api/stats")
def api_stats():
    db = get_db()
    topics = db.execute(
        "SELECT topic, COUNT(*) as cnt FROM articles GROUP BY topic ORDER BY cnt DESC"
    ).fetchall()
    journals = db.execute(
        "SELECT journal, COUNT(*) as cnt FROM articles GROUP BY journal ORDER BY cnt DESC"
    ).fetchall()
    return jsonify({
        "topics": {r["topic"]: r["cnt"] for r in topics},
        "journals": {r["journal"]: r["cnt"] for r in journals},
    })


# ── Main ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    init_db()
    print("PEM Tracker starting on http://127.0.0.1:5000")
    app.run(debug=True, port=5000)
