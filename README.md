# SmartArrange — Android launcher organization assistant

A **native Android APK project** for GitHub and GitHub Codespaces. Keeps the original home launcher. Current v0.1.0 is a **read-only proof of concept**: detects the active home app, discovers launchable applications, groups them using local rules, and shares a suggested organization plan. **It does not move icons or create folders.**

## Fastest way: build an APK using GitHub Actions (no Android Studio needed)

1. Create a **new empty GitHub repository** (do not initialize with README).
2. Upload **the contents** of this ZIP to the repository root (not the ZIP itself). Include `.github` and `.devcontainer` hidden folders. Or use `git push`.
3. Open **Actions → Build Android APK → Run workflow** (or push to `main`).
4. When the workflow finishes successfully, open its run → **Artifacts** → `SmartArrange-debug-apk`.
5. Unzip the artifact and transfer `app-debug.apk` to your Android phone. Android may ask you to permit installation from that source. Only install builds you trust.

A debug APK is for personal testing, **not a signed Play Store release**. Do not commit signing keys to GitHub.

## Git push

```bash
git init
git add .
git commit -m "Initial SmartArrange Android MVP"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/SmartArrange.git
git push -u origin main
```

## GitHub Codespaces

Open the repository → **Code → Codespaces → Create codespace on main**. The project uses the checked-in Gradle Wrapper (Gradle 8.9) and Android SDK 35. Use a compatible JDK (17 or 21); if the terminal defaults to an incompatible Java version, select an installed compatible JDK before building. Then run:

```bash
./gradlew --no-daemon clean assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Download via Codespaces file explorer or GitHub Actions artifact.

**Important:** Codespaces builds the APK but generally cannot interact with your physical phone's launcher remotely. Test the APK on a phone.

## Privacy and compatibility

- Android 8.0+ minimum; compiles against Android 15 SDK.
- No internet, accessibility, root, device administrator, or broad package-list permission.
- Only apps with launchable activities visible through the Android package visibility query are inventoried; some apps/work profiles may be absent.
- Category rules are heuristic and may misclassify apps; categories are suggestions, not actual folders.
- The current home app may report a chooser or an ambiguous result on some devices.
- No universal Android API permits modifying arbitrary third-party launcher layouts. Future gesture-based research requires explicit consent, compatibility testing, and Google Play policy review.

## Project structure

- `app/src/main/java/com/smartarrange/app/MainActivity.java` — native UI, discovery and categorization.
- `.github/workflows/build-apk.yml` — cloud APK build.
- `gradle/wrapper/` — checked-in Gradle Wrapper used by local and CI builds.

## Next development steps

Add manual category editing, persistent user rules, layout preview, per-launcher capability diagnostics, and an opt-in **single-icon** interaction experiment on explicitly supported launchers. Keep automation disabled until verified.
