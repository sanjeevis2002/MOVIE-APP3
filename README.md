# TamilStream IPTV & OTT - Android Application

A high-performance Android streaming application built with **Kotlin** and **Jetpack Compose**, delivering instant HLS live TV broadcast streaming and curated Tamil cinema on-demand.

---

## Key Features

- **Live Tamil TV (IPTV)**:
  - Stream live Tamil channels (News, Music, Entertainment, Religious, Movies) with low latency.
  - Category filtering (All, News, Music, Entertainment, Religious, Movies).
  - Search channels in real-time.
  - Favorites toggle and filtering.
  - Grid and Compact List view modes.
  - Add & edit custom M3U8/HLS stream channels.
- **OTT Movies & Cinema**:
  - Hero banner featuring Tamil blockbusters (*Leo*, *Vikram*, *Jailer*, *Ponniyin Selvan*).
  - Categorized movie rails (Trending, Action, Thriller, Historical).
  - Rich Movie Details Bottom Sheet with cast, synopsis, IMDb rating, and duration.
  - VIP / Premium content gating with subscription prompts.
- **Integrated Video Player**:
  - Live broadcast indicator and title badges.
  - Play/Pause, Mute/Unmute, and Fullscreen toggle.
  - Smooth docked playback while navigating channels.
- **Watchlist & History**:
  - Save favorite films to personal Watchlist.
  - Resume streaming from Watch History with clear history support.
- **VIP Subscription Portal**:
  - Multi-tier membership plans (Basic, Gold Premium, Ultimate VIP 4K).
  - Integrated payment gateway checkout flow (UPI, Card, NetBanking simulation) with instant account upgrade.
- **Admin Control Portal**:
  - Channel hub (add, edit, toggle, delete channels).
  - Direct movie catalog publisher.
  - System performance diagnostics.

---

## Technical Architecture

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Design System**: Obsidian Dark (#09090B) with Golden Amber (#F59E0B) and Live Red accents
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow
- **Media Engine**: AndroidX Media3 (ExoPlayer + HLS)
- **Image Pipeline**: Coil Compose
- **Launcher Icon**: Adaptive icon with custom foreground vector, radial gradient background, and multi-density PNG mipmaps
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`, `libs.versions.toml`)
