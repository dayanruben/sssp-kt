# Repository Guidelines

Kotlin Multiplatform library for single-source shortest paths. Use this guide to navigate the layout and contribute changes that stay consistent with the existing build, style, and review flow.

## Project Structure & Modules
- Root Gradle project declares plugins; the main module is `sssp`.
- Core code lives in `sssp/src/commonMain/kotlin` (algorithms, heaps, transforms). Platform-specific folders (`jvmMain`, `androidMain`, `iosMain`, `linuxX64Main`) stay thin and currently contain only target scaffolding.
- Cross-platform tests sit in `sssp/src/commonTest/kotlin`; target-specific tests can live in `*/src/<target>Test/kotlin` if needed.
- Dependency and version pins are centralized in `gradle/libs.versions.toml`. Build outputs land in `build/` (root and module).

## Build, Test, and Development Commands
- `./gradlew :sssp:build` — assemble the KMP library and run the full verification lifecycle.
- `./gradlew :sssp:check` — run all unit tests across configured targets.
- `./gradlew :sssp:jvmTest` — fastest test loop while iterating on algorithm changes.
- `./gradlew :sssp:publishToMavenLocal` — install the library locally for downstream testing.
- `./gradlew :sssp:androidSourcesJar` — verify Android-specific packaging artifacts when touching the Android target.

## Coding Style & Naming Conventions
- Follow Kotlin official style; indent with 4 spaces, prefer `val` over `var`, avoid wildcard imports, and favor expression-bodied functions for short helpers.
- Keep algorithms side-effect free where possible; pass data explicitly instead of relying on globals.
- Types/classes use `PascalCase`; functions, properties, and parameters use `camelCase`; constants use `UPPER_SNAKE_CASE`.
- Keep public API minimal and documented; consider inline `require` guards for argument validation.

## Testing Guidelines
- Use `kotlin.test` (already available) and place shared tests in `sssp/src/commonTest/kotlin/*Test.kt`. Mirror target-specific edge cases under the respective `<target>Test` directory only when platform behavior differs.
- Include negative and boundary cases (empty graphs, disconnected nodes, large weights). Prefer deterministic adjacency lists and assert approximate equality for doubles (see `SSSPTest.kt` helpers).
- Run `./gradlew :sssp:check` before opening a PR; add new tests with meaningful names matching the scenario (e.g., `denseGraphPrefersBmssp`).

## Commit & Pull Request Guidelines
- Follow the existing Conventional Commit style seen in history (`feat:`, `fix:`, `chore:`, `docs:`, scopes optional). Write imperative, concise subjects.
- PRs should include a clear problem statement, a short summary of the approach, test evidence (`./gradlew :sssp:check` output), and any performance notes if algorithm complexity or allocations change.
- Update docs when public APIs or behavior change (README, KDoc, or tests as living examples). Keep changesets small and focused; prefer separate PRs for dependency bumps vs. algorithm changes.
