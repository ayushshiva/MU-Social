# MU Social - Production CI/CD & Deployment Guide

This document outlines the CI/CD pipeline, security protocols, and release procedures for the MU Social Android application.

## 1. CI/CD Architecture (GitHub Actions)

### Quality & Build Workflow (`.github/workflows/android.yml`)
- **Triggers**: Push and Pull Requests to `main` and `develop`.
- **Quality Gates**:
  - **Detekt**: Static code analysis for Kotlin.
  - **Lint**: Android Lint for resource and code issues.
  - **Unit Tests**: Executes all JVM tests.
- **Security**:
  - **Snyk Scan**: Scans for vulnerable dependencies (High severity threshold).
- **Internal Distribution**:
  - Automatically builds a Debug APK on success.
  - Deploys to Firebase App Distribution group: `internal-testers`.

### Production Release Workflow (`.github/workflows/release.yml`)
- **Trigger**: New tags matching `v*` (e.g., `v1.0.2`).
- **Artifacts**:
  - Generates signed **AAB** (Android App Bundle) for Play Store.
  - Generates signed **APK** for direct distribution.
  - Extracts **ProGuard Mapping** files.
- **Distribution**:
  - Deploys AAB to Firebase App Distribution group: `beta-testers`.
  - Creates a GitHub Release with build artifacts and auto-generated changelogs.

---

## 2. Required GitHub Secrets

To enable these workflows, the following secrets must be configured in the GitHub Repository settings:

| Secret Name | Description |
|-------------|-------------|
| `GOOGLE_SERVICES_JSON` | Base64 encoded content of `app/google-services.json`. |
| `FIREBASE_TOKEN` | CI Token generated via `firebase login:ci`. |
| `FIREBASE_APP_ID_DEBUG` | App ID for the Debug Firebase project. |
| `FIREBASE_APP_ID_PROD` | App ID for the Production Firebase project. |
| `KEYSTORE_FILE` | Base64 encoded `.jks` or `.keystore` file. |
| `KEYSTORE_PASSWORD` | Password for the keystore. |
| `KEY_ALIAS` | Alias for the signing key. |
| `KEY_PASSWORD` | Password for the signing key. |
| `SNYK_TOKEN` | API Token from Snyk for security scanning. |
| `GEMINI_API_KEY_PROD` | Production AI API Key. |
| `AGORA_APP_ID_PROD` | Production Agora App ID. |

---

## 3. Firebase App Distribution Setup

1. **Service Account/Token**:
   - Run `firebase login:ci` locally to get your `FIREBASE_TOKEN`.
2. **Tester Groups**:
   - Create a group named `internal-testers` in the Firebase Console (App Distribution tab).
   - Create a group named `beta-testers` for external QA.
3. **App ID**:
   - Found in Project Settings > General > Your Apps.

---

## 4. Release Checklist

- [ ] All feature branches merged into `develop`.
- [ ] `develop` merged into `main` via PR (passing CI).
- [ ] Update `versionName` prefix in `app/build.gradle.kts` if moving to a new minor/major version.
- [ ] Verify Firestore/Storage rules are in "Production" mode (already secured in `firestore.rules`).
- [ ] Ensure `ProGuard` is enabled in `build.gradle.kts` (checked).
- [ ] Tag the commit: `git tag -a v1.0.0 -m "Release version 1.0.0"`.
- [ ] Push tags: `git push origin --tags`.

---

## 5. Deployment Checklist

- [ ] Check Firebase Crashlytics for any unresolved "Fatal" crashes.
- [ ] Verify Play Integrity settings in Google Play Console match the production certificate.
- [ ] Confirm Agora project is set to "Live" mode (not testing).
- [ ] Monitor Gemini AI quota usage.
- [ ] Review latest Detekt reports for technical debt.

---

## 6. Environment Variables (Local Development)

The app uses `BuildConfig` to inject secrets. For local development, these can be set in `local.properties`:
```properties
GEMINI_API_KEY=your_key
AGORA_APP_ID=your_id
```
*Note: The CI pipeline injects these via GitHub Secrets and Gradle Environment variables.*
