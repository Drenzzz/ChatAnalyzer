# ChatAnalyzer Plugin

Kotlin collector for Spigot, Paper, and Purpur. It saves local JSON Lines data with only
`message` and `occurred_at_utc`.

## Build

```powershell
.\gradlew.bat build
```

Upload `build/libs/ChatAnalyzer-plugin-0.1.0-all.jar` to the server's `plugins/`
directory and restart the server.

## Output

The collector writes files under `plugins/ChatAnalyzer/data/`, one file per UTC day:

```json
{"message":"halo semuanya","occurred_at_utc":"2026-08-01T12:05:43Z"}
```

## Commands

- `/chatanalyzer start`
- `/chatanalyzer stop`
- `/chatanalyzer status`
- `/chatanalyzer reload`
