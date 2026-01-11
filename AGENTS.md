# Repository Guidelines

## Project Structure & Module Organization

This repo is a multi-module Kotlin project:
- `amethyst/` holds the Android app (Compose UI and Android-specific code). Main sources are in `amethyst/src/main`, with unit tests in `amethyst/src/test` and instrumentation tests in `amethyst/src/androidTest`.
- `quartz/` is a Kotlin Multiplatform (KMP) Nostr protocol library. Shared code lives in `quartz/src/commonMain`, with platform code under `quartz/src/{androidMain,jvmMain,iosMain}` and tests under `quartz/src/commonTest` plus platform test folders.
- `commons/` provides shared UI/resources for Android/Desktop. Shared code in `commons/src/commonMain`.
- `desktopApp/` is the Compose Desktop client, with sources in `desktopApp/src/jvmMain` and tests in `desktopApp/src/jvmTest`.

## Build, Test, and Development Commands

```bash
./gradlew assembleDebug           # Build Android debug APK
./gradlew :desktopApp:run         # Run the desktop app
./gradlew test                    # Run JVM/unit tests
./gradlew connectedAndroidTest    # Run Android instrumentation tests
./gradlew spotlessCheck           # Verify formatting and lint rules
./gradlew spotlessApply           # Auto-format Kotlin and Gradle files
```

## Coding Style & Naming Conventions

- Kotlin + Jetpack Compose across modules; follow `ktlint` defaults enforced via Spotless.
- 4-space indentation and standard Kotlin naming (`PascalCase` classes, `camelCase` functions/properties).
- Android resources and file names use `lower_snake_case`.
- Spotless applies a license header to Kotlin sources; keep headers intact.

## Testing Guidelines

- Unit tests live in `src/test` or `src/commonTest`; instrumented Android tests in `src/androidTest` or `src/androidInstrumentedTest`.
- Prefer naming test classes `*Test` to align with Gradle defaults.
- Run `./gradlew test` for fast checks and `./gradlew connectedAndroidTest` before Android UI changes.

## Commit & Pull Request Guidelines

- Commit messages are short, descriptive sentences (often present-tense). Avoid noisy prefixes unless the change truly needs one.
- PRs should include: a concise summary, test plan (commands run), and screenshots for UI/UX changes.
- Link relevant issues when available; translation work should go through Crowdin.

## Security & Configuration Tips

- Do not commit signing keys or credentials. Release signing uses environment/CI secrets (see `README.md` deployment notes).
- Keep local Android SDK paths in `local.properties` only.

## Agent-Specific Instructions

- When maintaining a plan file, cross out completed tasks using Markdown strikethrough (e.g., `~~Task text~~`).
- Include a Scratchpad section in the plan for notes, assumptions, and open questions.
