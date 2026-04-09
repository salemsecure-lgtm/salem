#!/usr/bin/env python3
"""FileBot — Smart Desktop File Automation Bot.

Scans directories, auto-organizes files by type, finds duplicates,
cleans up clutter, watches for new files in real-time, and generates reports.
"""

import hashlib
import os
import shutil
import sys
import time
from collections import defaultdict
from datetime import datetime
from pathlib import Path

import click
from rich.console import Console
from rich.panel import Panel
from rich.progress import Progress, TextColumn
from rich.table import Table
from rich.tree import Tree
from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer

console = Console(force_terminal=True)

import io, sys
if sys.platform == "win32":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8", errors="replace")

# ── File categories ──────────────────────────────────────────────────────────

CATEGORIES = {
    "Images": {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp", ".ico", ".tiff", ".heic"},
    "Documents": {".pdf", ".doc", ".docx", ".txt", ".rtf", ".odt", ".xls", ".xlsx", ".csv", ".pptx", ".ppt", ".md"},
    "Videos": {".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm", ".m4v"},
    "Audio": {".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a"},
    "Archives": {".zip", ".rar", ".7z", ".tar", ".gz", ".bz2", ".xz"},
    "Code": {".py", ".js", ".ts", ".jsx", ".tsx", ".html", ".css", ".java", ".c", ".cpp", ".go", ".rs", ".rb", ".php", ".sh", ".bat", ".ps1"},
    "Data": {".json", ".xml", ".yaml", ".yml", ".toml", ".sql", ".db", ".sqlite"},
    "Executables": {".exe", ".msi", ".dmg", ".app", ".deb", ".rpm", ".appimage"},
    "Fonts": {".ttf", ".otf", ".woff", ".woff2", ".eot"},
    "3D/Design": {".psd", ".ai", ".sketch", ".fig", ".blend", ".obj", ".stl", ".fbx"},
}


def categorize_file(path: Path) -> str:
    ext = path.suffix.lower()
    for category, extensions in CATEGORIES.items():
        if ext in extensions:
            return category
    return "Other"


# ── Scanner ──────────────────────────────────────────────────────────────────


def scan_directory(target: Path, recursive: bool = True) -> list[dict]:
    files = []
    iterator = target.rglob("*") if recursive else target.iterdir()
    for item in iterator:
        if item.is_file():
            try:
                stat = item.stat()
                files.append({
                    "path": item,
                    "name": item.name,
                    "ext": item.suffix.lower(),
                    "size": stat.st_size,
                    "modified": datetime.fromtimestamp(stat.st_mtime),
                    "category": categorize_file(item),
                })
            except (PermissionError, OSError):
                continue
    return files


# ── Duplicate finder ─────────────────────────────────────────────────────────


def hash_file(path: Path, chunk_size: int = 8192) -> str:
    h = hashlib.md5()
    try:
        with open(path, "rb") as f:
            while chunk := f.read(chunk_size):
                h.update(chunk)
    except (PermissionError, OSError):
        return ""
    return h.hexdigest()


def find_duplicates(files: list[dict]) -> dict[str, list[Path]]:
    size_groups = defaultdict(list)
    for f in files:
        if f["size"] > 0:
            size_groups[f["size"]].append(f["path"])

    duplicates = {}
    for paths in size_groups.values():
        if len(paths) < 2:
            continue
        hash_groups = defaultdict(list)
        for p in paths:
            h = hash_file(p)
            if h:
                hash_groups[h].append(p)
        for h, group in hash_groups.items():
            if len(group) >= 2:
                duplicates[h] = group
    return duplicates


# ── Organizer ────────────────────────────────────────────────────────────────


def organize_files(files: list[dict], target: Path, dry_run: bool = False) -> list[dict]:
    moves = []
    for f in files:
        category = f["category"]
        dest_dir = target / category
        dest_path = dest_dir / f["name"]

        if dest_path == f["path"]:
            continue

        # Handle name collisions
        counter = 1
        while dest_path.exists():
            stem = f["path"].stem
            dest_path = dest_dir / f"{stem}_{counter}{f['ext']}"
            counter += 1

        moves.append({"src": f["path"], "dst": dest_path, "category": category})

        if not dry_run:
            dest_dir.mkdir(parents=True, exist_ok=True)
            shutil.move(str(f["path"]), str(dest_path))

    return moves


# ── Watcher ──────────────────────────────────────────────────────────────────


class FileBotHandler(FileSystemEventHandler):
    def __init__(self, target: Path):
        self.target = target

    def on_created(self, event):
        if event.is_directory:
            return
        path = Path(event.src_path)
        # Skip files already in category folders
        if path.parent != self.target:
            return
        category = categorize_file(path)
        dest_dir = self.target / category
        dest_dir.mkdir(parents=True, exist_ok=True)
        dest = dest_dir / path.name
        counter = 1
        while dest.exists():
            dest = dest_dir / f"{path.stem}_{counter}{path.suffix}"
            counter += 1
        try:
            time.sleep(0.5)  # wait for file write to finish
            shutil.move(str(path), str(dest))
            console.print(f"  [green]→[/green] {path.name} [dim]moved to[/dim] [cyan]{category}/[/cyan]")
        except (PermissionError, OSError) as e:
            console.print(f"  [red]✗[/red] {path.name}: {e}")


# ── Formatters ───────────────────────────────────────────────────────────────


def format_size(size: int) -> str:
    for unit in ("B", "KB", "MB", "GB", "TB"):
        if size < 1024:
            return f"{size:.1f} {unit}"
        size /= 1024
    return f"{size:.1f} PB"


# ── CLI ──────────────────────────────────────────────────────────────────────


@click.group()
def cli():
    """FileBot — Smart Desktop File Automation Bot."""
    pass


@cli.command()
@click.argument("directory", type=click.Path(exists=True), default=".")
@click.option("--recursive/--no-recursive", default=True, help="Scan subdirectories.")
def scan(directory, recursive):
    """Scan a directory and show a categorized report."""
    target = Path(directory).resolve()
    console.print(Panel(f"[bold]Scanning[/bold] {target}", style="blue"))

    with Progress(TextColumn("[progress.description]{task.description}"), console=console, transient=True) as progress:
        progress.add_task("Scanning files...", total=None)
        files = scan_directory(target, recursive)

    if not files:
        console.print("[yellow]No files found.[/yellow]")
        return

    # Category breakdown
    cat_stats = defaultdict(lambda: {"count": 0, "size": 0})
    for f in files:
        cat_stats[f["category"]]["count"] += 1
        cat_stats[f["category"]]["size"] += f["size"]

    table = Table(title=f"File Report — {len(files)} files", show_lines=True)
    table.add_column("Category", style="cyan", min_width=12)
    table.add_column("Count", justify="right", style="green")
    table.add_column("Total Size", justify="right", style="yellow")

    total_size = 0
    for cat in sorted(cat_stats, key=lambda c: cat_stats[c]["size"], reverse=True):
        s = cat_stats[cat]
        table.add_row(cat, str(s["count"]), format_size(s["size"]))
        total_size += s["size"]

    table.add_section()
    table.add_row("[bold]Total[/bold]", f"[bold]{len(files)}[/bold]", f"[bold]{format_size(total_size)}[/bold]")
    console.print(table)

    # Top 10 largest files
    largest = sorted(files, key=lambda f: f["size"], reverse=True)[:10]
    lg_table = Table(title="Top 10 Largest Files")
    lg_table.add_column("File", style="white", max_width=50)
    lg_table.add_column("Size", justify="right", style="yellow")
    lg_table.add_column("Category", style="cyan")
    for f in largest:
        lg_table.add_row(f["name"], format_size(f["size"]), f["category"])
    console.print(lg_table)


@cli.command()
@click.argument("directory", type=click.Path(exists=True), default=".")
def dupes(directory):
    """Find duplicate files in a directory."""
    target = Path(directory).resolve()
    console.print(Panel(f"[bold]Finding duplicates in[/bold] {target}", style="blue"))

    with Progress(TextColumn("[progress.description]{task.description}"), console=console, transient=True) as progress:
        task = progress.add_task("Scanning...", total=None)
        files = scan_directory(target)
        progress.update(task, description="Hashing files...")
        duplicates = find_duplicates(files)

    if not duplicates:
        console.print("[green]No duplicates found![/green]")
        return

    total_wasted = 0
    tree = Tree("[bold red]Duplicate Groups[/bold red]")
    for h, paths in duplicates.items():
        size = paths[0].stat().st_size
        wasted = size * (len(paths) - 1)
        total_wasted += wasted
        group = tree.add(f"[yellow]{format_size(size)}[/yellow] x{len(paths)} — wasting [red]{format_size(wasted)}[/red]")
        for p in paths:
            group.add(f"[dim]{p}[/dim]")

    console.print(tree)
    console.print(f"\n[bold red]Total wasted space: {format_size(total_wasted)}[/bold red]")


@cli.command()
@click.argument("directory", type=click.Path(exists=True), default=".")
@click.option("--dry-run", is_flag=True, help="Preview moves without executing.")
def organize(directory, dry_run):
    """Auto-organize files into categorized folders."""
    target = Path(directory).resolve()
    mode = "[yellow]DRY RUN[/yellow]" if dry_run else "[green]LIVE[/green]"
    console.print(Panel(f"[bold]Organizing[/bold] {target}  ({mode})", style="blue"))

    files = scan_directory(target, recursive=False)
    if not files:
        console.print("[yellow]No files to organize.[/yellow]")
        return

    moves = organize_files(files, target, dry_run=dry_run)

    if not moves:
        console.print("[green]Everything is already organized![/green]")
        return

    table = Table(title=f"{'Planned' if dry_run else 'Completed'} Moves — {len(moves)} files")
    table.add_column("File", style="white", max_width=40)
    table.add_column("→", justify="center")
    table.add_column("Destination", style="cyan", max_width=50)

    for m in moves:
        table.add_row(m["src"].name, "→", f"{m['category']}/{m['dst'].name}")

    console.print(table)

    if dry_run:
        console.print("\n[yellow]Run without --dry-run to execute.[/yellow]")
    else:
        console.print(f"\n[green]Organized {len(moves)} files into category folders.[/green]")


@cli.command()
@click.argument("directory", type=click.Path(exists=True), default=".")
def watch(directory):
    """Watch a directory and auto-organize new files in real-time."""
    target = Path(directory).resolve()
    console.print(Panel(f"[bold]Watching[/bold] {target}\n[dim]New files will be auto-sorted. Press Ctrl+C to stop.[/dim]", style="green"))

    handler = FileBotHandler(target)
    observer = Observer()
    observer.schedule(handler, str(target), recursive=False)
    observer.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
        console.print("\n[yellow]Watcher stopped.[/yellow]")
    observer.join()


@cli.command()
@click.argument("directory", type=click.Path(exists=True), default=".")
@click.option("--days", default=90, help="Files older than this are considered stale.")
def stale(directory, days):
    """Find stale files that haven't been modified recently."""
    target = Path(directory).resolve()
    console.print(Panel(f"[bold]Finding stale files[/bold] (>{days} days) in {target}", style="blue"))

    files = scan_directory(target)
    cutoff = datetime.now().timestamp() - (days * 86400)
    stale_files = [f for f in files if f["modified"].timestamp() < cutoff]

    if not stale_files:
        console.print(f"[green]No files older than {days} days![/green]")
        return

    stale_files.sort(key=lambda f: f["modified"])
    total_size = sum(f["size"] for f in stale_files)

    table = Table(title=f"Stale Files — {len(stale_files)} files ({format_size(total_size)})")
    table.add_column("File", style="white", max_width=45)
    table.add_column("Last Modified", style="yellow")
    table.add_column("Size", justify="right", style="cyan")
    table.add_column("Category", style="dim")

    for f in stale_files[:30]:
        table.add_row(f["name"], f["modified"].strftime("%Y-%m-%d"), format_size(f["size"]), f["category"])

    if len(stale_files) > 30:
        table.add_row(f"[dim]... and {len(stale_files) - 30} more[/dim]", "", "", "")

    console.print(table)


@cli.command()
@click.argument("directory", type=click.Path(exists=True), default=".")
def summary(directory):
    """Show a full directory health summary."""
    target = Path(directory).resolve()
    console.print(Panel("[bold]Directory Health Summary[/bold]", style="blue"))

    files = scan_directory(target)
    if not files:
        console.print("[yellow]Empty directory.[/yellow]")
        return

    total_size = sum(f["size"] for f in files)
    duplicates = find_duplicates(files)
    dup_count = sum(len(v) - 1 for v in duplicates.values())
    wasted = sum(v[0].stat().st_size * (len(v) - 1) for v in duplicates.values())

    cutoff = datetime.now().timestamp() - (90 * 86400)
    stale_count = sum(1 for f in files if f["modified"].timestamp() < cutoff)

    cat_counts = defaultdict(int)
    for f in files:
        cat_counts[f["category"]] += 1

    # Build summary panel
    lines = [
        f"[bold]Path:[/bold]          {target}",
        f"[bold]Total files:[/bold]   {len(files)}",
        f"[bold]Total size:[/bold]    {format_size(total_size)}",
        f"[bold]Categories:[/bold]    {len(cat_counts)}",
        "",
        f"[bold]Duplicates:[/bold]    [{'red' if dup_count else 'green'}]{dup_count} redundant copies[/{'red' if dup_count else 'green'}]",
        f"[bold]Wasted space:[/bold]  [{'red' if wasted else 'green'}]{format_size(wasted)}[/{'red' if wasted else 'green'}]",
        f"[bold]Stale (>90d):[/bold]  [{'yellow' if stale_count else 'green'}]{stale_count} files[/{'yellow' if stale_count else 'green'}]",
        "",
        "[bold]Breakdown:[/bold]",
    ]
    for cat in sorted(cat_counts, key=cat_counts.get, reverse=True):
        bar_len = int((cat_counts[cat] / len(files)) * 30)
        bar = "█" * bar_len + "░" * (30 - bar_len)
        lines.append(f"  [cyan]{cat:<14}[/cyan] {bar} {cat_counts[cat]}")

    console.print(Panel("\n".join(lines), title="Health Report", border_style="green"))


if __name__ == "__main__":
    cli()
