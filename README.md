# SmartSpend AI — Android App

**Status: All milestones complete (0–10) + 2 bonus features.** Java/XML, MVVM, offline-first Room database syncing with the FastAPI backend, MPAndroidChart visualizations, and a UPI auto-detect feature.

## Folder structure

```
SmartSpendAI-android/
├── build.gradle                     ← project-level
├── settings.gradle                  ← includes JitPack (for MPAndroidChart)
├── keystore.properties.example      ← copy to keystore.properties for signed builds
├── .gitignore                       ← keeps signing credentials out of Git
├── DEPLOYMENT.md                    ← signed APK build guide
└── app/
    ├── build.gradle                  ← all dependencies, signing config, BASE_URL switching
    └── src/
        ├── debug/AndroidManifest.xml  ← cleartext HTTP allowed ONLY in debug (for local backend)
        └── main/
            ├── AndroidManifest.xml
            └── java/com/example/smartspendai/
                ├── autodetect/              ← UPI notification auto-detect (bonus feature)
                │   ├── UpiNotificationListenerService.java
                │   ├── UpiNotificationParser.java
                │   ├── CategoryGuesser.java
                │   └── ParsedTransaction.java
                ├── data/
                │   ├── local/                ← Room: entities, DAOs, POJOs, AppDatabase
                │   ├── model/                 ← Retrofit request/response DTOs
                │   ├── remote/                ← Retrofit API interfaces + RetrofitClient
                │   └── repository/             ← Repository layer (MVVM) — Auth, Expense, Category, Goal, Insights
                └── ui/
                    ├── auth/           (Milestone 1) Splash, Login, Register
                    ├── dashboard/      (Milestone 4) real balance/trend dashboard
                    ├── analytics/      (Milestone 4) category pie, weekly bar, stats
                    ├── expense/        (Milestone 2/3/5) Add/Edit, List, Search
                    ├── category/       (Milestone 5) Manage Categories
                    ├── report/         (Milestone 5) PDF report export
                    ├── insights/       (Milestone 6/7) AI Insights — forecast, budget, suggestions, health score
                    ├── goal/           (Milestone 9) Goal Planner + What-If Simulator
                    └── settings/       (bonus) UPI auto-detect toggle + test simulator
```

## One-time setup

1. Extract this folder, then open it in **Android Studio** (or copy the files into a fresh **Empty Views Activity** project — Java, min SDK 24, package name `com.example.smartspendai`).
2. Make sure `app/src/main/java/com/example/smartspendai/` matches the structure above exactly — package name mismatches are the #1 source of build errors (see the troubleshooting notes below if you hit `ClassNotFoundException`).
3. Sync Gradle (**Sync Now** banner, or File → Sync Project with Gradle Files).
4. Run the **backend first** (see `SmartSpendAI-backend/README.md`) — the app needs it for login/register/sync/ML features. Local expense tracking (Room) works without it.
5. Run the app. Flow: **Splash → Login/Register → Dashboard** (bottom nav: Home / Analytics / Expenses / AI Insights).

## What's built, by milestone

| Milestone | What it added |
|---|---|
| 0 | Project setup, Material 3 dark theme, logo |
| 1 | Splash/Login/Register, JWT stored in EncryptedSharedPreferences |
| 2 | Room DB — offline Add/Edit/Delete expenses |
| 3 | Backend sync (push new/edited, pull remote, duplicate-safe) |
| 4 | Real Dashboard (balance, trend chart) + Analytics (pie/bar charts, stats) |
| 5 | Category management, live search, PDF report export |
| 6 + 7 | AI Insights screen — forecast, recommended budget, saving suggestions, health score |
| 8 | Anomaly detection *(backend built; Android UI available on request — currently not wired in, by choice)* |
| 9 | Goal Planner (progress tracking, estimated completion) + What-If Simulator |
| 10 | Signed release build, `BuildConfig.BASE_URL` switching, HTTPS-only release |
| Bonus | UPI auto-detect via NotificationListenerService (GPay/PhonePe/Paytm), with a debug-only test simulator |

## Database (Room) — 4 tables

- `expenses` — offline-first, syncs with backend, tracks `is_synced`/`remote_id`/`is_auto_detected`
- `categories` — 9 defaults pre-seeded, custom ones addable
- `goals` + `goal_contributions` — progress computed as SUM of contributions, never a stored mutable field

## Running the app for real (signed APK, no laptop needed)

See **`DEPLOYMENT.md`** — covers generating a signing key, setting up `keystore.properties`, pointing the release build at your deployed backend URL, and building an installable `.apk`.

## Common setup issues (and fixes)

- **`ClassNotFoundException` on launch** — your `namespace`/`applicationId` in `build.gradle` doesn't match your actual Java package folder structure. Both must say `com.example.smartspendai` exactly.
- **`Cannot resolve symbol 'R'`** — usually a resource (XML) error elsewhere breaking the whole build. Check the Build/Problems panel, fix any red errors there first, then Build → Clean Project → Rebuild.
- **MPAndroidChart dependency not found** — make sure `settings.gradle` includes the JitPack repository (`maven { url 'https://jitpack.io' }`).
- **After adding new Room columns/tables, app crashes on launch** — Room's schema changed; uninstall the app from your emulator/device before reinstalling (this project uses `fallbackToDestructiveMigration()` during development, which wipes local data on schema changes — expected while building, not for a real release).

## What's intentionally out of scope (documented simplifications, not bugs)

- **No automatic background sync** — sync is a manual button tap, not scheduled (WorkManager would be the production next-step).
- **UPI auto-detect reliability depends on the source app** — GPay/PhonePe/Paytm only post a system notification for *some* transactions (mainly background ones); an in-app "Payment Successful" screen for a foreground payment often posts nothing at all. This is a platform limitation, not a bug — see the in-app Settings screen for the privacy/reliability notes, and use the debug-only test simulator to verify the parsing pipeline independent of real notification timing.
- **Anomaly detection has no dedicated Android screen** — the backend endpoint (`/ml/anomalies`) is fully built and tested; wiring it into AI Insights is a quick follow-up if wanted.
