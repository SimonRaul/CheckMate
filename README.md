# CheckMate 🧾✅

Smart receipt splitting for students and friend groups — Kotlin, Jetpack Compose, Room (SQLite).
Architecture, DB schema and the Lab 3 checklist are in [PLAN.md](PLAN.md).

## Requirements

- **Android Studio** (a recent stable version that bundles AGP 9.4 support)
- Android SDK **Platform 37** (Studio installs it on sync if missing)
- An emulator or device running **Android 15 (API 35) or later** — e.g. a Pixel emulator with an API 37 image

Build config: `minSdk 35`, `compileSdk 37`, `targetSdk 37`, AGP 9.4.1, Gradle 9.6.0, Kotlin 2.4.21.

## Run it

1. Android Studio → **File › Open** → select this folder (`lab mobile`).
2. Let Gradle sync finish. Studio downloads Gradle 9.6.0 from `gradle/wrapper/gradle-wrapper.properties`.
   If Studio offers to install SDK Platform 37 or update AGP, accept.
3. **Device Manager** → create a Pixel device with an **API 37** system image (or API 35+).
4. Press **Run ▶** (`app` configuration).

> The Gradle wrapper scripts (`gradlew`) aren't checked in. Android Studio doesn't need them.
> To build from a terminal, run `gradle wrapper` once (or use Studio's Terminal), then `./gradlew assembleDebug`.

## Demo walkthrough (for the lab presentation)

1. **Dashboard** — greeting, stats and two seeded receipts loaded from the database.
2. Tap **Scan New Receipt** → **Scan** screen with a glowing crop frame. Tap the shutter (or the gallery icon).
3. **Edit Receipt** — "We found 5 items". Rename an item, change a price, delete one, tap **+ Add missing item**. **Confirm Items** is disabled while a row is invalid.
4. **Who had what?** — pick a mate at the top, then tap the empty slots next to items. Tap an avatar on an item to remove it. **Split** shares an item between everyone. **Add mate** saves a new person to the DB.
5. **Settle Up** — switch the tip (No tip / 10 / 15 / 20 %) and watch each share update proportionally. The chat icon opens WhatsApp (or the share sheet), the bank icon marks someone as settled.
6. **Save & Finish** → back on the Dashboard the new receipt is at the top. Close the app completely and reopen it: it's still there (persistence).

## Inspecting the database

Run the app, then **View › Tool Windows › App Inspection › Database Inspector**, pick `checkmate.db` and
browse `receipts`, `items`, `mates`, `item_assignments`, `receipt_mates`. Room also exports the schema
as JSON to `app/schemas/` on every build.

## Tests

`app/src/test/.../SplitCalculatorTest.kt` covers the split maths. Run it from Studio (right-click › Run) or `./gradlew test`.

## Project layout

```
app/src/main/java/com/checkmate/app/
├── MainActivity.kt, CheckMateApp.kt      entry point, DB/repository singleton
├── data/
│   ├── db/AppDatabase.kt                 Room database
│   ├── db/entities/                      tables + ReceiptWithDetails relation
│   ├── db/dao/                           ReceiptDao, MateDao
│   ├── CheckMateRepository.kt            save/load, first-launch seed data
│   └── MockData.kt                       what the simulated scanner "reads"
├── domain/SplitCalculator.kt             split + proportional tip, in cents
└── ui/
    ├── navigation/CheckMateNavHost.kt    routes between the 5 screens
    ├── screens/                          Dashboard, Scan, EditReceipt, AssignMates, SettleUp
    ├── viewmodel/                        DashboardViewModel, SplitViewModel
    ├── components/                       avatars, sticky button, top bar, dashed borders
    └── theme/                            colours, type, shapes
```
