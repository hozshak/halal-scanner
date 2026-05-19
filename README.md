# Halal Scanner

Android-App: Barcode-Scan im Supermarkt → prüft Zutatenliste auf haram (Schwein, Alkohol, Karmin, …) und mushbooh (zweifelhaft: Gelatine, Mono-/Diglyceride, …) Bestandteile und zeigt das Ergebnis mit **konkreter Begründung**.

## Features

- **Barcode-Scan** über ML Kit + CameraX (EAN-13/8, UPC, Code128, QR)
- **Produkt-Lookup** über OpenFoodFacts (weltweite Open-Data-Datenbank, kostenlos, kein API-Key)
- **Halal-Analyzer** prüft Zutatenliste gegen Datenbank von:
  - **Haram-Triggern** (eindeutig verboten): Schwein, Alkohol, Karmin/E120, Schellack/E904, Pepsin, L-Cystein/E920, …
  - **Mushbooh-Triggern** (zweifelhaft, Quelle prüfen): Gelatine/E441, Mono-/Diglyceride/E471–473, Knochenphosphat/E542, natürliche Aromen, Glycerin/E422, Molke, Lab/Rennet, …
  - **Halal-Indikatoren**: "vegan", "100% pflanzlich", "halal-zertifiziert"
- **Ergebnis-Screen** mit farbigem Status-Badge (HALAL ✓ / FRAGLICH ? / HARAM ✗), Produkt-Bild, Zutatenliste mit markierten verdächtigen Stellen, und Begründungs-Text
- **Verlauf** der letzten 50 Scans (in SharedPreferences, kein Backend nötig)

## Status-Kategorien

| Status | Bedeutung |
|---|---|
| **HALAL** (grün) | Explizite Halal-Kennzeichnung oder vegan-Label gefunden |
| **vermutlich halal** (hellgrün) | Keine haram- oder zweifelhaften Zutaten in der Liste erkannt |
| **FRAGLICH** (orange) | Mindestens eine mushbooh-Zutat gefunden (z.B. Gelatine ohne Quellenangabe) |
| **HARAM** (rot) | Eindeutig haram-Zutat gefunden (z.B. Schweinegelatine, Alkohol, Karmin) |
| **unbekannt** (grau) | Produkt nicht in DB oder keine Zutatenliste hinterlegt |

## Architektur

```
android/app/src/main/
├── AndroidManifest.xml
├── kotlin/com/halal/scanner/
│   ├── halal/IngredientDatabase.kt    ← Haram + Mushbooh Wortlisten + Analyzer
│   ├── data/OpenFoodFactsClient.kt    ← REST-API zu world.openfoodfacts.org
│   ├── data/Product.kt
│   ├── db/HistoryStore.kt             ← SharedPreferences-Persistenz
│   ├── scanner/BarcodeAnalyzer.kt     ← ML Kit Barcode-Reader
│   └── ui/
│       ├── MainActivity.kt            ← Home (Scan, Verlauf, Info)
│       ├── ScannerActivity.kt         ← Kamera-Preview + Barcode-Erkennung
│       ├── ResultActivity.kt          ← Status + Begründung + Zutaten
│       └── HistoryActivity.kt         ← Liste der letzten Scans
└── res/  …
```

## Lokaler Build

Voraussetzung: JDK 17 + Android SDK.

```bash
cd android
./gradlew assembleRelease
# APK liegt unter app/build/outputs/apk/release/app-release.apk
```

Oder via GitHub Actions: bei jedem push auf `main` wird APK gebaut (siehe `.github/workflows/build-apk.yml`).

## Wichtiger Hinweis

Diese App ist **informativ**. Die Bewertung basiert auf weit verbreitetem Konsens islamischer Speisegesetze – sie ist **keine offizielle Halal-Zertifizierung und keine Fatwa**. Im Zweifel: Hersteller kontaktieren oder zertifiziertes Halal-Produkt wählen.

Die Datenbasis (OpenFoodFacts) ist Crowd-sourced. Produkte ohne Zutatenliste können nicht analysiert werden. Du kannst fehlende Produkte selbst auf https://world.openfoodfacts.org eintragen.
