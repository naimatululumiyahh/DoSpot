# Firebase & Google Maps setup (DoSpot)

Follow these steps to fully connect this project (`com.example.dospot`) to Firebase and Google Maps.

## 1) Register Android app in Firebase

1. Open Firebase Console: https://console.firebase.google.com/
2. Select your project or create one.
3. In Project Overview, click Add App > Android.
4. Enter package name: `com.example.dospot` and register app.
5. Download `google-services.json` and place it in the module folder:

   `c:\mobile\DoSpot\app\google-services.json`

   Note: do NOT commit the real `google-services.json` to a public repo. Use `.gitignore` if needed.

## 2) Add Google Maps API key

1. Open Google Cloud Console: https://console.cloud.google.com/
2. Select the same project as Firebase.
3. APIs & Services → Library → enable "Maps SDK for Android".
4. APIs & Services → Credentials → Create Credentials → API key.
5. (Recommended) Restrict key: Application restrictions → Android apps → add `com.example.dospot` and SHA-1.
6. Copy the API key and paste into:

   - `app/src/main/res/values/strings.xml` replace `REPLACE_WITH_YOUR_GOOGLE_MAPS_API_KEY` in `google_maps_key`.

   The AndroidManifest already references `@string/google_maps_key`.

## 3) SHA-1 fingerprint (for Google Sign-In or API restrictions)

- From Android Studio: Gradle panel → :app → Tasks → android → signingReport.
- Or from PowerShell (requires `keytool` from JDK):

```powershell
# default debug keystore
keytool -list -v -keystore $env:USERPROFILE\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copy the SHA-1 and add it in Firebase Console: Project settings → Your apps → Add fingerprint (SHA-1).

## 4) Firestore rules

1. In Firebase Console → Firestore Database → Rules, replace with `firebase.rules` contents (recommended):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /reminders/{reminderId} {
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
      allow read, update, delete: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

Publish rules.

## 5) Build & run locally

1. Make sure Java (JDK 17) is installed and `JAVA_HOME` is set.

PowerShell example to set for current session (adjust path to your JDK):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:PATH = "$env:JAVA_HOME\\bin;" + $env:PATH
```

2. In project root:

```powershell
cd c:\mobile\DoSpot
.\gradlew clean assembleDebug
```

If you use Android Studio, open the project and run from there.

## 6) Quick verifications

- If Firebase not configured correctly you'll see runtime errors (logcat) mentioning `FirebaseApp` or authentication failures.
- If Google Maps key invalid you will see a runtime message on map view or `ApiKey` errors in logcat.

## Files added to repo for guidance

- `firebase.rules` — recommended Firestore rules
- `app/google-services.json.example` — example template (do not use in production)
- `README-FIREBASE-SETUP.md` — this file

If you want, I can also:

- Paste your API key into `strings.xml` if you provide it
- Help generate the SHA-1 command tailored to your environment
- Add `.gitignore` entry to ignore `app/google-services.json`

## Quick helper script

I added `scripts/setup.ps1` which:

- Sets `JAVA_HOME` for the current PowerShell session (edit the script to point to your JDK if needed)
- Runs `./gradlew signingReport` to print SHA-1 fingerprints
- Runs `./gradlew clean assembleDebug` to build the debug APK

Run it from PowerShell (from repo/scripts):

```powershell
cd c:\mobile\DoSpot\scripts
.\setup.ps1
```

This script helps you get SHA-1 and attempt a build; it won't add your `google-services.json` or API key automatically.

### Next actions I can take for you

1. If you paste the full contents of `google-services.json` here I will write it to `app/google-services.json` (NOTE: do NOT paste sensitive credentials in public chat if this is public). After that I can run the build and report results.
2. If you paste your Maps API key here I will insert it into `app/src/main/res/values/strings.xml` for you.
3. If you prefer not to paste secrets here, run `scripts/setup.ps1` locally and share the SHA-1 output; I will help you add it to Firebase Console instructions.
