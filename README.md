# Poultry Farm Tracker (पोल्ट्री फार्म ट्रैकर)

A native, offline-first Android application designed for smallholder poultry farmers in India to track flock inventory, daily egg production, bird mortality, and generate insights.

---

## Key Features

1. **Flock Inventory Tracking & Sequential Balance Chaining**
   - Tracks **Adult Chickens** and **Chicks** separately.
   - Calculates closing inventory strictly using the formula:
     $$\text{Closing Adults} = \text{Opening Adults} + \text{Added} - \text{Deaths} - \text{Sold/Removed}$$
     $$\text{Closing Chicks} = \text{Opening Chicks} + \text{Added} - \text{Deaths} - \text{Sold/Removed}$$
     $$\text{Total Live Birds} = \text{Closing Adults} + \text{Closing Chicks}$$
     $$\text{Total Deaths} = \text{Adult Deaths} + \text{Chick Deaths}$$
   - Chaining rule: Today's closing inventory automatically becomes tomorrow's opening balance.
   - Editing or deleting any historical record triggers automatic chronological recalculation of all subsequent records inside a database transaction.
   - Prevents saving if calculated closing inventory would be negative.

2. **Daily Egg Production & Health Monitoring**
   - Daily egg collection logs.
   - Mortality rate calculation: $(\text{Total Deaths} / \text{Opening Live Birds}) \times 100$. Handled safely when opening stock is 0 (displays N/A rather than dividing by zero).
   - Health observations, notes, and vaccination/treatment logs.

3. **Bilingual Support (English & हिंदी)**
   - Instant in-app language switching between English and Hindi (हिंदी).
   - Full string resource localization (`res/values/strings.xml` and `res/values-hi/strings.xml`).

4. **100% Offline & Private (Room SQLite)**
   - Operates completely offline with zero account, cloud server, or internet requirements.
   - Data stored locally in an encrypted/private SQLite database managed by Android Jetpack Room.

5. **Data Backup, Export & Storage Access Framework (SAF)**
   - CSV export for Daily Records and Farm Reports.
   - Full JSON backup and restore with structure and value validation prior to database replacement.
   - Integration with Android Share Sheet via FileProvider.

6. **Interactive Visual Analytics & Charts**
   - Egg Production trend chart.
   - Daily Mortality trend chart.
   - Total Flock Population over time chart.
   - Period filters: Today, Yesterday, Last 7 Days, Last 30 Days, All Time, Custom Range.

---

## Build & Installation Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 or JDK 21
- Android SDK with API 36 / 34 (minimum supported device: Android 8.0, API 26)

### Running Locally
```bash
# Build Debug APK
gradle assembleDebug

# Run Unit & Robolectric Tests
gradle :app:testDebugUnitTest
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Architecture & Package Structure

```
com.example
├── MainActivity.kt
├── data
│   ├── database
│   │   └── AppDatabase.kt
│   ├── dao
│   │   ├── FarmDao.kt
│   │   ├── DailyRecordDao.kt
│   │   └── InventoryAdjustmentDao.kt
│   ├── entity
│   │   ├── Farm.kt
│   │   ├── DailyRecord.kt
│   │   └── InventoryAdjustment.kt
│   ├── preferences
│   │   └── FarmPreferences.kt
│   └── repository
│       └── FarmRepository.kt
├── domain
│   └── model
│       ├── DashboardStats.kt
│       └── InventoryCalculator.kt
├── ui
│   ├── components
│   │   ├── DatePickerDialogs.kt
│   │   ├── FarmChart.kt
│   │   └── StatCard.kt
│   ├── navigation
│   │   └── AppNavigation.kt
│   ├── screens
│   │   ├── HomeScreen.kt
│   │   ├── RecordFormScreen.kt
│   │   ├── RecordsHistoryScreen.kt
│   │   ├── ReportsScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── SetupScreen.kt
│   ├── theme
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel
│       ├── DailyRecordFormViewModel.kt
│       ├── FarmViewModel.kt
│       └── ReportsViewModel.kt
└── util
    ├── BackupManager.kt
    └── DateUtils.kt
```

---

## Known Limitations

1. **Single Farm Scope**: This version is optimized for managing one active small poultry flock/farm per device installation.
2. **One Closing Snapshot Per Calendar Date**: Enforces one authoritative closing inventory record per date (modifying an existing date updates that day's log and recalculates downstream inventory).
3. **Local Storage Only**: Because the app is designed strictly offline-first for rural connectivity, backups must be manually exported via SAF or shared to Google Drive/WhatsApp.
