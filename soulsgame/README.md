# ASHEN — a souls-like ⚔️🔥

A compact, fully-offline **Souls-like** action game built as an Android **APK**.
Deliberate combat, stamina management, dodge-rolls with i-frames, lock-on, a
bonfire checkpoint, the souls-on-death run-back, and a two-phase boss. Touch
controls, no internet — flight-ready.

## The Souls-like loop

- **Bonfire** — rest to fully heal, refill Estus, and **spend souls to level up**.
  Resting also **respawns all enemies** (just like Dark Souls).
- **Combat** — every action drains **stamina**. Swing, roll, and block cost
  stamina; it regenerates only when you stop acting. Enemies **telegraph** their
  attacks (red arc) — wait, dodge, punish.
- **Dodge roll** — a quick burst with **invincibility frames**. Timing the roll
  through an attack is the core skill.
- **Lock-on** — focus the nearest foe so you strafe and face them automatically.
- **Estus** — limited heals; drinking roots you in place, so make space first.
- **Death** — you drop **all your souls** as a bloodstain where you fell and
  respawn at the bonfire. Run back and touch the stain to reclaim them — but die
  again on the way and they're gone forever.
- **The Ashen Knight** — a boss with an overhead **slam** (AOE telegraph), a
  **lunge**, and at half health a phase-two **spin sweep**. Beat him for souls
  and the **HEIR OF ASH** ending.

## Controls (touch)

| Control | Action |
|---|---|
| Left thumb (drag) | Floating joystick — move |
| **ATK** | Sword swing (up to a 3-hit combo) |
| **ROLL** | Dodge roll with i-frames |
| **BLOCK** | Hold to guard (costs stamina; empty stamina = guard break) |
| **ESTUS** | Heal (limited charges) |
| **LOCK** | Lock-on / release nearest enemy |
| **REST AT BONFIRE** | Appears when you stand on the bonfire |

Keyboard (for desktop testing): WASD/arrows move, **J** attack, **Space/Shift**
roll, **K** block, **R** heal, **L** lock, **E** rest.

## Install on your phone

1. Download **`dist/Ashen.apk`** to the phone.
2. Tap it → allow "install unknown apps" for your browser/Files app → **Install**.
3. Open **Ashen**. Requires Android 8.0+. Debug-signed (hence the prompt) and
   100% offline.

Direct download link (open on phone):
`https://github.com/salemsecure-lgtm/salem/raw/claude/flight-game-apk-o6sd8p/soulsgame/dist/Ashen.apk`

## Rebuild

```bash
cd soulsgame
gradle :app:assembleDebug    # -> app/build/outputs/apk/debug/app-debug.apk
```

## Layout

```
soulsgame/
├── app/src/main/assets/index.html          # the entire game (HTML5 canvas + JS)
├── app/src/main/java/.../MainActivity.java  # fullscreen WebView host
└── dist/Ashen.apk                           # prebuilt, ready to sideload
```
