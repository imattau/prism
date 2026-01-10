# Repository Guidelines

## Project Structure & Module Organization
- `amethyst/`: Android app (Kotlin + Jetpack Compose).
- `quartz/`: Nostr protocol KMP library shared across platforms.
- `commons/`: Shared UI components and utilities (KMP).
- `desktopApp/`: Compose Multiplatform desktop app.
- `benchmark/`: Performance benchmarks.
- `docs/`: Design docs, screenshots, and technical notes.
- `spotless/`: Formatting configuration (license headers).
- `git-hooks/`: Pre-commit and pre-push hooks.

## Build, Test, and Development Commands
- `./gradlew assembleDebug`: Build the Android debug APK.
- `./gradlew :desktopApp:run`: Build and run the desktop app (Java 21+).
- `./gradlew test`: Run JVM/KMP unit tests.
- `./gradlew connectedAndroidTest`: Run Android instrumented tests.
- `./gradlew spotlessCheck`: Verify formatting/linting.
- `./gradlew spotlessApply`: Auto-format Kotlin/Gradle files.
- `./gradlew installFdroidDebug` / `./gradlew installPlayDebug`: Install device builds.

## Coding Style & Naming Conventions
- Kotlin formatting is enforced with Spotless + ktlint; run `spotlessApply` before committing.
- License headers are required and managed via `spotless/copyright.kt`.
- Use standard Kotlin naming: `UpperCamelCase` for classes, `lowerCamelCase` for functions/vars.
- Test classes follow `*Test.kt` naming and live under `src/commonTest`, `src/test`, or `src/androidTest`.

## Testing Guidelines
- Unit tests run via `./gradlew test` and are located in `src/commonTest` and `src/test`.
- Instrumented tests run via `./gradlew connectedAndroidTest` and live in `src/androidTest`.
- No explicit coverage target is documented; focus on behavioral coverage for protocol and UI logic.

## Commit & Pull Request Guidelines
- Commits are typically short and descriptive (e.g., “Fixes: …”, “remove unused imports”).
- Reference issues when applicable (e.g., `Fixes #1621`).
- PRs should include: a clear summary, testing performed, and screenshots for UI changes.

## Local Tooling Tips
- Git hooks are provided in `git-hooks/`. Running any Gradle `preBuild` task installs them.

## Plan Tracking
- Check plan files (for example, `PLAN.md`) before starting work.
- Cross out completed plan items using Markdown strikethrough (e.g., `~~done~~`).
