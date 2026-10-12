# AI Superstore Manager (سپر اسٹور مینیجر)

An offline-first Android application designed for superstore shopkeepers in Pakistan. Built with Kotlin, Jetpack Compose, Material 3, Room, and optional AI vision and cloud backup integrations.

---

## Key Features

- **Offline-First Core:** Full inventory management, manual sales recording, stock receiving, physical adjustments, and report generation work without an internet connection.
- **Bilingual Support (English & Urdu):** Proper RTL (Right-To-Left) layout support, Urdu and English product names, nicknames, and localized interface strings.
- **Precise Decimal Arithmetic:** Uses `BigDecimal` for exact quantity and financial calculations (pieces, packets, kg, grams, litres, ml).
- **Home Dashboard:** Offline summary metrics including active products, today's sales revenue, completed sales count, low-stock/out-of-stock items, and estimated gross profit.
- **Product Management:** Product catalogue, photo attachment, unit selection, cost/selling price tracking, stock history ledger, and product aliases.
- **Manual Sales & Cart:** Fast sales interface with search (English/Urdu/alias), decimal quantities, cart editing, discount support, and atomic stock deductions.
- **Handwritten Bill Photo Import:** Photo import flow for handwritten bills, vision-based structured AI parsing abstraction (`AIBillParser`), product matching algorithm (`ProductMatcher`), and review/confirmation with idempotency protection against duplicate deductions.
- **Financial & Inventory Reports:** Detailed reports on Sales Revenue, Cost of Goods Sold (COGS), Gross Profit, Stock Valuations, and CSV export for product catalogue.
- **Cloud Backup & Configuration:** Configurable AI API Key and optional Cloud REST endpoint via `CloudSyncService` and background `WorkManager`.

---

## Technical Architecture

- **Language:** Kotlin 1.9.22
- **UI Framework:** Jetpack Compose with Material 3 Design System
- **Database:** Room (SQLite) with transactional DAOs & Flow streams
- **Architecture Pattern:** Clean Architecture (Data, Domain/Repository, UI)
- **Background Tasks:** Android WorkManager
- **Async Streams:** Kotlin Coroutines & Flow
- **Image Loading:** Coil Compose
- **Min SDK:** 26 (Android 8.0) | **Target SDK:** 34 (Android 14)

---

## How to Build & Test

### Requirements
- JDK 17
- Android SDK 34
- Gradle 8.8 (configured via Gradle Wrapper / local Gradle)

### Run Unit Tests
```bash
./gradlew test
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK file will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## AI & Cloud Configuration

1. **AI Service Integration:**
   - Go to **Settings > AI Service Configuration**.
   - Input your Gemini / AI API Key.
   - If no API key is provided, the app gracefully operates in manual review/offline draft mode.

2. **Cloud Sync Integration:**
   - Go to **Settings > Cloud Backup Setup**.
   - Input optional cloud endpoint URL (e.g. Supabase REST API).
   - Local JSON backups can be generated and restored via local file storage.

---

## Project Structure

```
app/src/main/java/com/superstore/manager/
├── data/
│   ├── ai/            # AI Product Identifier & Handwritten Bill Parser
│   ├── local/         # Room Entities, DAOs, and AppDatabase
│   ├── repository/    # Dashboard, Product, Sales, Stock, Bill & Reports Repositories
│   └── sync/          # CloudSyncService, Backup Payload, & SyncWorker
├── ui/
│   ├── bills/         # Handwritten Bill Import & Review Screens
│   ├── dashboard/     # Home Dashboard & Quick Actions
│   ├── navigation/    # Navigation Screens & Routes
│   ├── product/       # Catalogue, Add/Edit, and Product Detail Screens
│   ├── reports/       # Financial Reports & CSV Export Screen
│   ├── sales/         # Manual Sales Cart & Checkout Screen
│   ├── settings/      # App Settings, Language Selector & AI Setup
│   ├── stock/         # Low Stock List & Stock Receiving Screens
│   └── theme/         # Material 3 Theme & Typography
└── util/              # LanguageHelper, UnitConverter, ProductMatcher, CsvExporter
```

---

## License & Privacy Notice
All transaction data and product records are saved locally on device storage. AI vision requests send selected images over secure TLS connections directly to the configured provider API.
