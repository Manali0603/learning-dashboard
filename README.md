# Learning Dashboard (Android · Kotlin · Jetpack Compose)

Login → course dashboard → course details with lesson completion, working offline after the first load.
**Demo login:** `learner@example.com` / `password123` · **Build:** `./gradlew assembleDebug` · **Tests:** `./gradlew testDebugUnitTest`

## 1. Architecture
MVVM with an offline-first repository, split into `ui` → `domain` → `data`:

```
Compose screen ─ observes StateFlow ─► ViewModel ─► Repository interface (domain)
                                                       └─ impl (data): Room (source of truth) ◄─ Mock API
```
- **Unidirectional data flow:** each screen renders one immutable `UiState` (sealed Loading / Success / Empty / Error); user events go to the ViewModel.
- **Room is the single source of truth.** The UI only ever observes the database; the network only writes into it. Marking a lesson done updates the lesson and the course's progress in one transaction, and both screens update automatically because they observe the same tables.
- **Domain logic is plain Kotlin** (`ProgressCalculator`, `LoginValidator`, `CourseSyncMerger`), so it's unit-tested on the JVM.
- **Manual DI** (`AppContainer`) instead of Hilt: less ceremony at this size, and the same seams (interfaces + constructor injection) make the Hilt migration mechanical.

Why: this is the structure Google recommends for Android, it scales to more features without rewrites, and every layer can be tested in isolation.

## 2. Offline support
- Courses and lessons are cached in **Room**. Every launch shows the cache immediately and refreshes in the background; a failed refresh **never clears the cache**. The dashboard shows an "offline, showing saved courses" banner instead of an error.
- Lesson completion is a local write, so it works offline. When a server snapshot arrives, `CourseSyncMerger` keeps lessons completed locally (`server OR device`) and recomputes progress, so a refresh can't erase offline progress.
- The session token is persisted, so the app opens straight to the cached dashboard without a network.
- The API is mocked (assets JSON + delay), but it **checks real connectivity** first, so airplane mode genuinely exercises the offline path.

## 3. Security (tokens)
The token is stored in DataStore, **encrypted with AES-256-GCM using a key in the Android Keystore** (hardware-backed where available). The session file is excluded from cloud backup and device transfer. In production I'd also use a short-lived access token plus a refresh token handled by an OkHttp `Authenticator`, never log tokens, and use HTTPS with certificate pinning. Logout clears the token and the cached user data.

## 4. Scale (1M users, hundreds of courses)
1. **Paging 3 + RemoteMediator** for course/lesson lists, with incremental sync (ETags / `updatedSince`) instead of full snapshots.
2. **Server sync for completions:** an outbox table flushed by **WorkManager** (retries, backoff, network constraint), with idempotent APIs so retries are safe.
3. **Real networking stack:** Retrofit/OkHttp with HTTP caching, token-refresh interceptor, timeouts; CDN for static course content.
4. **Modularisation + Hilt** (`:core:data`, `:feature:dashboard`, …) for build speed and team ownership; Baseline Profiles for startup.
5. **Observability:** Crashlytics, performance monitoring, analytics and feature flags / staged rollouts.

## 5. Second platform (iOS)
Same layers in Swift: **SwiftUI** views observing `@Observable` ViewModels; a `CourseRepository` protocol using `async/await` + `URLSession`; **SwiftData / Core Data** as the offline source of truth; the token in the **Keychain**; `NWPathMonitor` for connectivity; `BackgroundTasks` for sync; **XCTest** for the same ViewModel and merge-rule tests. Shared business logic could alternatively live in **Kotlin Multiplatform**.

---
**Trade-offs:** mock API instead of a real backend; completions aren't sent to a server (clearly marked in `OfflineFirstCourseRepository`); UI strings aren't in `strings.xml` yet (needed for localisation). To see the empty state, point `MockCourseApi` at `courses_empty.json` in `AppContainer`.
