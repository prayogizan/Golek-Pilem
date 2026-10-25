# 🎬 Golek Pilem

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.4.1-green.svg?logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02.01-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Enabled-795548.svg?logo=materialdesign)](https://m3.material.io)
[![Koin DI](https://img.shields.io/badge/Koin-4.0.2-purple.svg?logo=koin)](https://insert-koin.io/)
[![Paging 3](https://img.shields.io/badge/Paging%203-3.3.6-orange.svg)](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
[![Coil](https://img.shields.io/badge/Coil%203-3.1.0-teal.svg)](https://coil-kt.github.io/coil/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-informational.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-informational.svg)](https://developer.android.com)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20UDF-brightgreen.svg)](https://developer.android.com/topic/architecture)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Golek Pilem** (*Javanese for "Search Movies" / "Find Movies"*) is a showcase Android application designed to discover popular movies and TV series. Built entirely with **Jetpack Compose**, **Material 3**, and **Clean Architecture**, it demonstrates modern Android engineering practices including Unidirectional Data Flow (UDF), type-safe navigation, reactive pagination, in-app video playback, and comprehensive automated testing.

Powered by [The Movie Database (TMDB) API](https://www.themoviedb.org/).

---

## 📑 Table of Contents

- [✨ Features](#-features)
- [🏛 Architecture & Design Principles](#-architecture--design-principles)
- [🛠 Tech Stack & Libraries](#-tech-stack--libraries)
- [📁 Project Directory Structure](#-project-directory-structure)
- [🚀 Getting Started & Setup](#-getting-started--setup)
- [🧪 Testing & Quality Assurance](#-testing--quality-assurance)
- [📜 TMDB Attribution & License](#-tmdb-attribution--license)

---

## ✨ Features

### 🎞️ Dual-Tab Discovery Feed
- **Movies & TV Series Tabs**: Fast switching between trending/popular Movies and TV series with responsive tab indicators.
- **Collapsible Top App Bar**: Uses `enterAlwaysScrollBehavior` to maximize screen real estate when scrolling through catalog items.
- **Scroll Position Preservation**: Retains independent scroll positions (`LazyStaggeredGridState`) when navigating between tabs.
- **Adaptive Staggered Grid**: Balanced multi-column grid presentation for rich media poster cards.
- **Expressive Empty & Error States**: Contextual empty state illustrations (`EmptyFeedView`) with one-tap refresh and inline retry capabilities.

### ⚡ Reactive Infinite Pagination (Paging 3)
- Fully reactive pagination powered by AndroidX **Paging 3** and Kotlin Coroutines `Flow`.
- **Skeleton Shimmer Loading**: Custom `Modifier.shimmerEffect()` displaying smooth 1200ms linear animated placeholders during initial load.
- Seamless append error recovery displaying inline retry prompts directly in the feed.

### 🔍 Comprehensive Media Detail Screen
- **Cinematic Hero Header**: High-definition backdrop artwork overlay with elevated poster card and subtle gradient vignettes.
- **Dynamic Rating Meter**: Circular progress indicator color-coded according to TMDB score thresholds (green $\ge 7.0$, amber $\ge 5.0$, red $< 5.0$).
- **Metadata & Genre Chips**: Formatted runtimes (`Xh Ym`), season/episode counts, release status, total vote counts, and horizontal scrolling genre chips.
- **Overview & Synopsis**: Formatted plot overview with fallback for unreleased or unsummarized titles.
- **Production & Network Badges**: Production companies and broadcast networks with high-contrast logo rendering and fallbacks.

### 🍿 In-App Trailer Player & YouTube Integration
- Real-time video resolution via TMDB Videos API (`/movie/{id}/videos` & `/tv/{id}/videos`).
- Priority selection for official trailers and teasers.
- **In-App YouTube Player**: Embedded Android WebView player with interactive thumbnail launch and fullscreen mode.
- **External Intent Fallback**: Direct launch to the native YouTube application or browser with one tap.

### 💬 Community Reviews
- Paginated user reviews from TMDB API with author avatars, ratings, and submission dates.
- Expandable review body cards with "Read more" / "Show less" toggling for lengthy reviews.

---

## 🏛 Architecture & Design Principles

Golek Pilem strictly enforces **Clean Architecture** and **Unidirectional Data Flow (UDF)**, ensuring complete separation of concerns, testability, and scalability.

```
┌────────────────────────────────────────────────────────┐
│               Presentation Layer (UI)                  │
│    Jetpack Compose UI, ViewModels, UiState, UiEvent    │
└───────────────────────────┬────────────────────────────┘
                            │ (depends on)
                            ▼
┌────────────────────────────────────────────────────────┐
│                Domain Layer (Core)                     │
│    Domain Models, Repository Interfaces, Use Cases     │
│    (Pure Kotlin — ZERO Android / Network / DB imports) │
└───────────────────────────▲────────────────────────────┘
                            │ (implements)
┌───────────────────────────┴────────────────────────────┐
│                 Data Layer (Infra)                     │
│    Retrofit ApiService, DTOs, Mappers, Paging Sources, │
│    Repository Implementations, OkHttp Client           │
└────────────────────────────────────────────────────────┘
```

### 1. Architectural Layers

- **Presentation Layer (`com.prayogizan.golekpilem.ui`)**:
  - Implements the **Route vs Screen Separation Pattern**:
    - `*Route.kt`: Stateful composable responsible for dependency injection (`koinViewModel()`), lifecycle-aware state collection (`collectAsStateWithLifecycle()`), and navigation event dispatching.
    - `*Screen.kt`: Pure, stateless composable driven solely by immutable `UiState` and event lambdas `(UiEvent) -> Unit`, with full `@Preview` support for both Light and Dark themes.
  - Type-safe Compose navigation using Kotlinx Serialization (`Screen.Main`, `Screen.Detail(id, mediaType)`).

- **Domain Layer (`com.prayogizan.golekpilem.domain`)**:
  - **Pure Kotlin**: Free of any Android framework, network, or third-party database imports.
  - **Use Cases / Interactors**: Single-responsibility interactors exposing an `operator fun invoke(...)` (e.g., `GetMoviesUseCase`, `GetMovieDetailUseCase`).
  - **Contracts**: Abstract repository interfaces (`MovieRepository`, `TvSeriesRepository`).

- **Data Layer (`com.prayogizan.golekpilem.data`)**:
  - **DTOs (`data/remote/dto`)**: Pure wire representations for TMDB JSON responses.
  - **Mappers (`data/mapper`)**: Defensive transformation functions mapping DTOs to Domain models with fallback values, null safety, and image URL formatting.
  - **Paging Sources (`data/paging`)**: Paging 3 `PagingSource` implementations handling TMDB page-based pagination.
  - **Repository Implementations (`data/repository`)**: Concrete implementations coordinating remote API calls and mapping data streams.

- **Core Layer (`com.prayogizan.golekpilem.core`)**:
  - Cross-cutting network configurations, HTTP Bearer authentication interceptor (`AuthInterceptor`), constants, and resource wrappers (`Resource`).

### 2. SOLID Principles in Practice

- **S — Single Responsibility**: DTOs only parse JSON; Mappers only transform data; Domain models only store business state; ViewModels only process UI intents into state; Composables only render UI.
- **O — Open/Closed**: Repositories and data sources are abstracted behind interfaces, making it seamless to add offline caching (e.g., Room) without altering domain logic.
- **L — Liskov Substitution**: All repository implementations can be substituted with test fakes (`FakeMovieRepository`) in unit tests without affecting domain use cases.
- **I — Interface Segregation**: Distinct, focused repository interfaces (`MovieRepository`, `TvSeriesRepository`) prevent bloated monolithic contracts.
- **D — Dependency Inversion**: Presentation and Domain layers depend on abstractions; Koin wires implementations into consumers.

### 3. Unidirectional Data Flow (UDF)

```
        ┌──────────────────┐
        │    User Input    │
        └────────┬─────────┘
                 │ (Triggers UiEvent)
                 ▼
        ┌──────────────────┐
        │    ViewModel     │ ◄─── (Executes UseCase)
        └────────┬─────────┘
                 │ (Emits StateFlow<UiState>)
                 ▼
        ┌──────────────────┐
        │ Composable Screen│
        └──────────────────┘
```

---

## 🛠 Tech Stack & Libraries

| Category | Technology / Library | Version | Description |
| :--- | :--- | :--- | :--- |
| **Language** | Kotlin | `2.2.10` | First-class language features, Coroutines & Flow |
| **Build Tool** | Android Gradle Plugin | `9.4.1` | Gradle build automation with version catalogs |
| **UI Framework** | Jetpack Compose | `BOM 2026.02.01` | Declarative UI toolkit |
| **Design System** | Material Design 3 | Latest | Modern Material components, shapes, and theming |
| **Dependency Injection** | Koin | `4.0.2` | Lightweight pragmatic DI for Kotlin & Compose |
| **Networking** | Retrofit & OkHttp | `2.11.0` / `4.12.0` | Type-safe HTTP client with logging & auth interceptors |
| **Serialization** | Kotlinx Serialization | `1.7.3` | High-performance JSON parser & route serializer |
| **Image Loading** | Coil 3 | `3.1.0` | Kotlin-first image loader with crossfade & OkHttp engine |
| **Pagination** | AndroidX Paging 3 | `3.3.6` | Infinite scrolling and reactive load state management |
| **Navigation** | Navigation Compose | `2.8.9` | Type-safe declarative destination navigation |
| **Testing** | JUnit4, MockK, Turbine | `4.13.2` / `1.13.13` / `1.2.0` | Unit testing, mocking, and coroutine flow testing |
| **Mock Web Server** | OkHttp MockWebServer | `4.12.0` | Testing HTTP serialization and network responses |

---

## 📁 Project Directory Structure

```
com.prayogizan.golekpilem
├── GolekPilemApp.kt                  # Application class & Koin bootstrap
├── MainActivity.kt                   # Single Activity host with Edge-to-Edge support
│
├── core/                             # Cross-cutting concerns
│   ├── constants/                    # Global constants (ApiConstants)
│   ├── di/                           # Network DI modules (NetworkModule, CoreModule)
│   └── util/                         # Resource wrappers & utility extensions
│
├── domain/                           # Pure Kotlin Domain Layer
│   ├── model/                        # Business models (Movie, TvSeries, Details, Review, Video)
│   ├── repository/                   # Abstract repository interfaces (MovieRepository, TvSeriesRepository)
│   ├── usecase/                      # Single-responsibility interactors (GetMoviesUseCase, etc.)
│   └── di/                           # Domain Koin module
│
├── data/                             # Data & Infrastructure Layer
│   ├── remote/
│   │   ├── api/                      # Retrofit interfaces (TmdbApiService)
│   │   └── dto/                      # Serializable network DTOs (*Dto)
│   ├── mapper/                       # Defensive DTO-to-Domain mappers (*Mapper)
│   ├── paging/                       # Paging 3 PagingSources (*PagingSource)
│   ├── repository/                   # Repository implementations (*RepositoryImpl)
│   └── di/                           # Data Koin modules (DataModule, RepositoryModule)
│
└── ui/                               # Presentation Layer
    ├── common/                       # Reusable UI components (Shimmer, ErrorView, RatingIndicator)
    ├── navigation/                   # Type-safe Screen routes & GolekPilemNavGraph
    ├── main/                         # Discovery catalog feed (MainRoute, MainScreen, MainViewModel)
    │   └── components/               # MediaGridCard, EmptyFeedView, PaginationStatusItems
    ├── detail/                       # Media detail screen (DetailRoute, DetailScreen, DetailViewModel)
    │   └── components/               # DetailHeader, TrailersSection, ReviewsSection, YouTubePlayer
    ├── theme/                        # Color, Typography, Shapes, Spacing
    └── di/                           # Presentation Koin module (PresentationModule)
```

---

## 🚀 Getting Started & Setup

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 11 or higher
- **Android SDK**: `compileSdk 37`, `minSdk 24`, `targetSdk 37`
- **TMDB API Read Access Token (v4)**: Create a free account on [The Movie Database (TMDB)](https://www.themoviedb.org/) and generate an API Read Access Token under your account settings.

### 1. Clone Repository

```bash
git clone https://github.com/prayogizan/Golek-Pilem.git
cd Golek-Pilem
```

### 2. Configure TMDB API Token

Provide your TMDB Access Token using either of the following methods:

#### Option A: `local.properties` (Recommended for local development)
Add the following line to `local.properties` in the root directory:

```properties
TMDB_ACCESS_TOKEN=your_tmdb_v4_read_access_token_here
```

#### Option B: Environment Variable (Recommended for CI/CD)
Set the environment variable:

```bash
export TMDB_ACCESS_TOKEN="your_tmdb_v4_read_access_token_here"
```

### 3. Build & Run

- Open the project in Android Studio.
- Sync Gradle projects with build files.
- Select your target emulator or physical device (Android 7.0 / API 24+).
- Press **Run 'app'** (`Shift + F10`) or execute via terminal:

```bash
# Assemble Debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 🧪 Testing & Quality Assurance

The project enforces strict automated testing across all architectural layers.

### Test Coverage Highlights

- **Domain Layer**: Unit tests validating business rules and use cases with `MockK` (`GetMoviesUseCaseTest`, `GetMovieDetailUseCaseTest`, `GetTvSeriesUseCaseTest`, `GetTvSeriesDetailUseCaseTest`).
- **Data Layer**:
  - DTO serialization and deserialization tests using `kotlinx.serialization` (`TmdbDtoSerializationTest`).
  - Robust defensive mapper verification (`MovieMapperTest`, `TvMapperTest`).
  - Network contract verification using OkHttp `MockWebServer` (`TmdbApiServiceTest`).
  - Paging 3 pagination mechanics and refresh/append load states (`MoviePagingSourceTest`, `TvPagingSourceTest`).
  - Repository coordination tests (`MovieRepositoryImplTest`, `TvRepositoryImplTest`).
- **Presentation Layer**:
  - State machine testing for `MainViewModel` and `DetailViewModel` using CashApp `Turbine`.
  - Type-safe navigation argument serialization verification (`ScreenSerializationTest`).
  - Custom UI rating scale conversion tests (`RatingIndicatorTest`).
- **Dependency Injection**:
  - Complete Koin dependency graph verification (`AppModuleTest`, `NetworkModuleTest`).

### Running Tests

Execute the full unit test suite from the terminal:

```bash
./gradlew testDebugUnitTest
```

---

## 📜 TMDB Attribution & License

- This product uses the [TMDB API](https://www.themoviedb.org/) but is not endorsed or certified by TMDB.
- All movie and TV series poster artwork, backdrops, and metadata belong to their respective copyright holders.

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.