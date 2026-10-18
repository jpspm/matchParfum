# MatchParfum

Compose Multiplatform app (Android + iOS) for [perfume-api](../../perfume-api/perfume-api):
search the catalog, open a perfume's accords / notes pyramid, and list the most similar perfumes
(cosine similarity), optionally filtered by gender.

## Screens

| Screen | Endpoint |
|---|---|
| Search (debounced, min. 3 chars) | `GET /v1/perfumes/search?q=&limit=` |
| Perfume detail | `GET /v1/perfumes/{id}` |
| Similar perfumes (+ gender filter) | `GET /v1/perfumes/{id}/recommendations?limit=&gender=` |

Errors are parsed from the API's `application/problem+json` body and mapped by their stable `code`
(`shared/.../ui/common/LoadState.kt`); the `request_id` is shown so it can be found in the server log.

## Code layout (`shared/src/commonMain/kotlin/com/matchparfum`)

- `data/model` – serializable DTOs mirroring `app/schemas.py`
- `data/remote` – Ktor client (`PerfumeApi`), `ApiException`, platform engine + base URL (`expect`/`actual`)
- `ui/search`, `ui/detail`, `ui/similar` – screen + ViewModel each
- `ui/common`, `ui/theme` – shared components, theme, inlined icons, accord colors
- `App.kt` – type-safe Navigation Compose graph; `AppContainer.kt` – manual DI

## Pointing the app at the API

Start the backend (`docker compose up -d api` in perfume-api, port 8000). Defaults:

- Android emulator: `http://10.0.2.2:8000` (`androidMain/.../Platform.android.kt`)
- Android physical device (USB): `http://localhost:8000` – run `adb reverse tcp:8000 tcp:8000`
  after plugging the phone in (the forward is lost when it disconnects)
- iOS simulator: `http://localhost:8000` (`iosMain/.../Platform.ios.kt`)

Cleartext HTTP is allowed only for those dev hosts (`androidApp/src/main/res/xml/network_security_config.xml`,
`NSAllowsLocalNetworking` in `iosApp/iosApp/Info.plist`). For a physical device or a deployed API,
change `defaultBaseUrl` (or pass `AppContainer(baseUrl = ...)` to `App`) and use HTTPS.

---

This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…