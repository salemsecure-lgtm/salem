#!/usr/bin/env python3
"""PEM Digest — Curates top PEM articles into an HTML email digest.

Focuses on high-impact, career-advancing content:
- Most cited / trending topics
- New guidelines and clinical practice changes
- Landmark trials and meta-analyses
- High-impact journal articles
"""

import sqlite3
from datetime import datetime, timedelta
from pathlib import Path

DB_PATH = Path(__file__).parent / "pemtracker.db"

# Journal scoring — aligned with Salem's priority PEM reading list
JOURNAL_IMPACT = {
    # ── Priority 1: PEM-specific / highest-cited PEM journals (score 12) ─────
    "Pediatric emergency care": 12,
    "Annals of emergency medicine": 12,
    "Academic emergency medicine": 11,
    "Clinical pediatric emergency medicine": 11,
    # ── Priority 2: Top pediatrics with heavy PEM content (score 10) ─────────
    "Pediatrics": 10,
    "JAMA Pediatrics": 10,
    "JAMA pediatrics": 10,
    "The Journal of pediatrics": 10,
    "Journal of Pediatrics": 10,
    "Archives of disease in childhood": 9,
    # ── Priority 3: Canadian EM — fellowship-relevant (score 10) ─────────────
    "CJEM": 10,
    "Canadian journal of emergency medicine": 10,
    # ── Priority 4: EM with PEM review series (score 8) ──────────────────────
    "The American journal of emergency medicine": 8,
    "American Journal of Emergency Medicine": 8,
    "Emergency medicine journal": 8,
    # ── Priority 5: Critical care & supporting (score 7) ─────────────────────
    "Pediatric critical care medicine": 7,
    "Lancet child & adolescent health": 9,
    "Resuscitation": 7,
    "Pediatric research": 7,
    # ── Top-tier general (boost if PEM content) ──────────────────────────────
    "The New England journal of medicine": 10,
    "JAMA": 9,
    "The Lancet": 9,
    "BMJ (Clinical research ed.)": 8,
    "Critical care medicine": 7,
    # ── Other EM/Peds ────────────────────────────────────────────────────────
    "The Journal of emergency medicine": 6,
    "Emergency Medicine Australasia": 6,
    "European journal of pediatrics": 6,
    "World journal of emergency medicine": 5,
    "Scandinavian journal of trauma, resuscitation and emergency medicine": 6,
}

# PEM-focused keywords that signal high-impact / career-relevant content
HIGH_IMPACT_KEYWORDS = [
    # Study quality
    "guideline", "consensus", "recommendation", "practice change",
    "randomized", "randomised", "RCT", "clinical trial", "multicenter",
    "meta-analysis", "systematic review", "Cochrane",
    # Clinical significance
    "mortality", "survival", "outcome", "prognosis",
    "novel", "first-line", "landmark", "paradigm",
    # PEM-specific organizations & tools
    "PECARN", "ACEP", "AAP", "WHO", "APLS", "PALS",
    "algorithm", "protocol", "pathway", "decision rule",
    "point-of-care", "POCUS", "ultrasound", "bedside",
    "quality improvement", "patient safety", "simulation",
    # PEM chief complaints & high-acuity
    "pediatric emergency", "paediatric emergency",
    "emergency department", "resuscitation", "critical",
    "sepsis", "shock", "cardiac arrest", "intubation", "RSI",
    "trauma", "head injury", "TBI", "PECARN",
    "febrile infant", "fever without source",
    "status epilepticus", "seizure",
    "bronchiolitis", "croup", "asthma exacerbation",
    "anaphylaxis", "DKA", "diabetic ketoacidosis",
    "appendicitis", "intussusception", "testicular torsion",
    "procedural sedation", "pain management",
    "child abuse", "non-accidental", "safeguarding",
    "pediatric readiness", "triage",
]

# PEM-relevance keywords — articles must contain these to score well
PEM_RELEVANCE = {
    "pediatric", "paediatric", "child", "children", "infant", "neonatal",
    "neonate", "newborn", "adolescent", "toddler",
    "emergency department", "emergency medicine", "ED ", " ED,",
    "acute care", "urgent care", "critical care",
    "PALS", "PECARN", "PEM",
}

STUDY_TYPE_KEYWORDS = {
    "Guideline / Consensus": ["guideline", "consensus", "recommendation", "position statement", "clinical practice"],
    "RCT / Clinical Trial": ["randomized", "randomised", "clinical trial", "RCT", "multicenter trial"],
    "Meta-Analysis / Review": ["meta-analysis", "systematic review", "Cochrane", "scoping review"],
    "Landmark / Novel": ["novel", "first-line", "landmark", "paradigm", "breakthrough"],
    "Quality / Safety": ["quality improvement", "patient safety", "diagnostic error", "simulation"],
}


def score_article(article: dict) -> float:
    """Score an article by PEM relevance and impact. Higher = more important."""
    score = 0.0
    title = (article["title"] or "").lower()
    abstract = (article["abstract"] or "").lower()
    text = f"{title} {abstract}"

    # PEM relevance gate — must mention pediatric/child + emergency/acute context
    pem_relevance_hits = sum(1 for kw in PEM_RELEVANCE if kw.lower() in text)
    if pem_relevance_hits == 0:
        return 0.0  # not PEM-relevant, exclude entirely
    score += pem_relevance_hits * 1.5  # reward strong PEM signal

    # Bonus for explicitly mentioning "pediatric emergency"
    if "pediatric emergency" in text or "paediatric emergency" in text:
        score += 8
    if "emergency department" in text and any(w in text for w in ["pediatric", "paediatric", "child", "infant"]):
        score += 5

    # Journal impact
    journal = article["journal"] or ""
    for j_name, j_score in JOURNAL_IMPACT.items():
        if j_name.lower() in journal.lower() or journal.lower() in j_name.lower():
            score += j_score
            break
    else:
        score += 2  # lower baseline for unknown journals

    # High-impact keyword matches
    for kw in HIGH_IMPACT_KEYWORDS:
        if kw.lower() in text:
            score += 2

    # Recency bonus (newer = better)
    if article["pub_date"]:
        try:
            pub = datetime.strptime(article["pub_date"], "%Y-%m-%d")
            days_old = (datetime.now() - pub).days
            if days_old <= 7:
                score += 5
            elif days_old <= 14:
                score += 3
            elif days_old <= 30:
                score += 1
        except ValueError:
            pass

    # Title-level keyword bonus (title matches weigh more)
    for kw in HIGH_IMPACT_KEYWORDS:
        if kw.lower() in title:
            score += 1.5

    return score


def classify_study_type(title: str, abstract: str) -> str:
    text = f"{title} {abstract}".lower()
    for stype, keywords in STUDY_TYPE_KEYWORDS.items():
        if any(kw.lower() in text for kw in keywords):
            return stype
    return "Research"


def get_top_articles(days_back: int = 7, limit: int = 15) -> list[dict]:
    """Get the top scored articles from the past N days."""
    db = sqlite3.connect(str(DB_PATH))
    db.row_factory = sqlite3.Row

    cutoff = (datetime.now() - timedelta(days=days_back)).strftime("%Y-%m-%d")
    rows = db.execute(
        "SELECT * FROM articles WHERE pub_date >= ? ORDER BY pub_date DESC",
        (cutoff,)
    ).fetchall()

    if not rows:
        # Fallback: get most recent articles regardless of date
        rows = db.execute(
            "SELECT * FROM articles ORDER BY pub_date DESC LIMIT 100"
        ).fetchall()

    articles = []
    for r in rows:
        a = dict(r)
        a["score"] = score_article(a)
        a["study_type"] = classify_study_type(a["title"] or "", a["abstract"] or "")
        articles.append(a)

    db.close()

    articles.sort(key=lambda x: x["score"], reverse=True)

    # Ensure topic diversity: max 3 per topic in top results
    selected = []
    topic_counts = {}
    for a in articles:
        topic = a["topic"]
        if topic_counts.get(topic, 0) >= 3:
            continue
        selected.append(a)
        topic_counts[topic] = topic_counts.get(topic, 0) + 1
        if len(selected) >= limit:
            break

    return selected


def build_html_digest(articles: list[dict], period: str = "This Week") -> str:
    """Build a polished HTML email digest."""
    today = datetime.now().strftime("%B %d, %Y")

    # Group by study type for the "highlights" section
    highlights = [a for a in articles[:5]]
    rest = articles[5:]

    # Topic summary
    topic_counts = {}
    for a in articles:
        topic_counts[a["topic"]] = topic_counts.get(a["topic"], 0) + 1

    topic_pills_html = ""
    for topic, count in sorted(topic_counts.items(), key=lambda x: -x[1]):
        topic_pills_html += f'<span style="display:inline-block;background:#1e3a5f;color:#38bdf8;padding:4px 12px;border-radius:20px;font-size:12px;margin:2px 4px;">{topic} ({count})</span>'

    # Build highlight cards
    highlight_html = ""
    for i, a in enumerate(highlights):
        badge_color = "#38bdf8" if a["study_type"] == "Research" else "#fbbf24" if "Guideline" in a["study_type"] else "#4ade80" if "RCT" in a["study_type"] else "#818cf8"
        pubmed_link = a.get("url", "")
        link_html = f'<a href="{pubmed_link}" style="color:#38bdf8;text-decoration:none;font-size:13px;">View on PubMed &rarr;</a>' if pubmed_link else ""

        abstract_snippet = (a["abstract"] or "")[:250]
        if len(a["abstract"] or "") > 250:
            abstract_snippet += "..."

        highlight_html += f'''
        <div style="background:#1e293b;border:1px solid #334155;border-radius:10px;padding:20px;margin-bottom:12px;">
            <div style="margin-bottom:8px;">
                <span style="display:inline-block;background:rgba(56,189,248,0.15);color:#38bdf8;padding:2px 10px;border-radius:20px;font-size:11px;font-weight:600;text-transform:uppercase;">{a["topic"]}</span>
                <span style="display:inline-block;background:rgba(129,140,248,0.15);color:{badge_color};padding:2px 10px;border-radius:20px;font-size:11px;font-weight:600;text-transform:uppercase;margin-left:4px;">{a["study_type"]}</span>
            </div>
            <h3 style="color:#e2e8f0;font-size:16px;margin:0 0 6px 0;line-height:1.4;">{a["title"]}</h3>
            <p style="color:#94a3b8;font-size:12px;margin:0 0 8px 0;">{a.get("authors","") or ""}  &middot;  <em>{a.get("journal","")}</em>  &middot;  {a.get("pub_date","")}</p>
            <p style="color:#cbd5e1;font-size:13px;line-height:1.6;margin:0 0 10px 0;">{abstract_snippet}</p>
            {link_html}
        </div>'''

    # Build "more articles" list
    more_html = ""
    for a in rest:
        pubmed_link = a.get("url", "")
        title_html = f'<a href="{pubmed_link}" style="color:#e2e8f0;text-decoration:none;">{a["title"]}</a>' if pubmed_link else a["title"]
        more_html += f'''
        <tr>
            <td style="padding:10px 12px;border-bottom:1px solid #334155;">
                <span style="display:inline-block;background:rgba(56,189,248,0.1);color:#38bdf8;padding:1px 8px;border-radius:10px;font-size:10px;margin-bottom:4px;">{a["topic"]}</span><br>
                <span style="color:#e2e8f0;font-size:14px;">{title_html}</span><br>
                <span style="color:#64748b;font-size:11px;">{a.get("journal","")} &middot; {a.get("pub_date","")}</span>
            </td>
        </tr>'''

    html = f'''<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"></head>
<body style="margin:0;padding:0;background:#0f172a;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',system-ui,sans-serif;">
<div style="max-width:680px;margin:0 auto;padding:20px;">

    <!-- Header -->
    <div style="text-align:center;padding:30px 20px;background:linear-gradient(135deg,#1e3a5f 0%,#0f172a 100%);border-radius:12px 12px 0 0;border:1px solid #334155;border-bottom:none;">
        <div style="display:inline-block;background:#38bdf8;width:48px;height:48px;border-radius:12px;line-height:48px;font-size:24px;font-weight:700;color:#0f172a;margin-bottom:12px;">P</div>
        <h1 style="color:#e2e8f0;font-size:24px;margin:0;">PEM Weekly Digest</h1>
        <p style="color:#94a3b8;font-size:14px;margin:8px 0 0 0;">{period} &middot; {today}</p>
    </div>

    <!-- Summary -->
    <div style="background:#1e293b;padding:20px;border:1px solid #334155;border-top:none;">
        <p style="color:#94a3b8;font-size:13px;margin:0 0 12px 0;">This week's top {len(articles)} articles curated for you, ranked by journal impact, study type, and clinical relevance.</p>
        <div style="margin:0;">{topic_pills_html}</div>
    </div>

    <!-- Top Picks -->
    <div style="background:#0f172a;padding:24px 0;">
        <h2 style="color:#fbbf24;font-size:14px;text-transform:uppercase;letter-spacing:1px;margin:0 0 16px 0;">Top Picks</h2>
        {highlight_html}
    </div>

    <!-- More Articles -->
    {f"""
    <div style="background:#0f172a;padding:0 0 24px 0;">
        <h2 style="color:#38bdf8;font-size:14px;text-transform:uppercase;letter-spacing:1px;margin:0 0 12px 0;">More Worth Reading</h2>
        <table style="width:100%;border-collapse:collapse;background:#1e293b;border-radius:10px;overflow:hidden;border:1px solid #334155;">
            {more_html}
        </table>
    </div>
    """ if rest else ""}

    <!-- Footer -->
    <div style="text-align:center;padding:24px;color:#64748b;font-size:12px;">
        <p>Curated by PEM Tracker &middot; Sources: PubMed, AAP Pediatrics, JAMA Pediatrics, Ann Emerg Med, BMJ, and more</p>
        <p style="margin-top:4px;">Powered by your local PEM Tracker dashboard</p>
    </div>

</div>
</body>
</html>'''

    return html


def send_digest_email(html: str, subject: str, to_email: str) -> bool:
    """Send the digest email via Gmail SMTP."""
    import smtplib
    from email.mime.multipart import MIMEMultipart
    from email.mime.text import MIMEText

    env_path = Path(__file__).parent / ".env"
    env_vars = {}
    for line in env_path.read_text().strip().splitlines():
        if "=" in line and not line.startswith("#"):
            k, v = line.split("=", 1)
            env_vars[k.strip()] = v.strip()

    gmail_user = env_vars.get("GMAIL_USER", "")
    gmail_pass = env_vars.get("GMAIL_APP_PASSWORD", "")

    if not gmail_user or not gmail_pass:
        print("ERROR: GMAIL_USER or GMAIL_APP_PASSWORD not set in .env")
        return False

    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"] = f"PEM Tracker <{gmail_user}>"
    msg["To"] = to_email
    msg.attach(MIMEText(html, "html"))

    try:
        with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
            server.login(gmail_user, gmail_pass)
            server.sendmail(gmail_user, to_email, msg.as_string())
        print(f"Email sent to {to_email}")
        return True
    except Exception as e:
        print(f"ERROR sending email: {e}")
        return False


if __name__ == "__main__":
    from app import fetch_pubmed_articles, fetch_rss_articles, store_articles, init_db

    print("Refreshing article database...")
    init_db()
    pubmed = fetch_pubmed_articles(days_back=14, max_results=80)
    rss = fetch_rss_articles()
    store_articles(pubmed + rss)
    print(f"  Fetched {len(pubmed)} PubMed + {len(rss)} RSS articles")

    print("Curating top articles...")
    top = get_top_articles(days_back=14, limit=15)
    print(f"  Selected {len(top)} top articles")
    for i, a in enumerate(top, 1):
        print(f"  {i}. [{a['score']:.0f}] [{a['study_type']}] {a['title'][:80]}")

    html = build_html_digest(top)
    out = Path(__file__).parent / "latest_digest.html"
    out.write_text(html, encoding="utf-8")
    print(f"\nDigest saved to {out}")

    today = datetime.now().strftime("%B %d, %Y")
    subject = f"PEM Weekly Digest — {today} | Top {len(top)} Articles This Week"
    send_digest_email(html, subject, "salemkhalafs@gmail.com")
