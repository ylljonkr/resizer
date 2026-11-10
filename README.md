# Resizer

Resize images on your phone or PC. Works fully offline. Nothing is uploaded anywhere.

One web app (`www/index.html`) is packaged two ways:

| Platform | Tool | Output |
|---|---|---|
| Android | [Capacitor](https://capacitorjs.com) | `Resizer-x.y.z-android.apk` |
| Windows | [Electron](https://www.electronjs.org) + electron-builder | `Resizer-x.y.z-win-x64.zip` |

Download both from the [Releases page](https://github.com/ylljonkr/resizer/releases).

## Install on Android

1. On your phone, open the Releases page and download `Resizer-x.y.z-android.apk`.
2. Tap the downloaded file (from the notification or the **Files / Downloads** app).
3. Android will say it can't install apps from this source. Tap **Settings**, turn on
   **Allow from this source** for the app you used (Chrome or Files), then go back.
   You can find it later under **Settings > Apps > Special app access > Install unknown apps**.
4. Tap **Install**. If **Play Protect** warns about an unknown app, tap **More details > Install anyway**.
   (It warns because the app isn't from the Play Store, not because something is wrong.)
5. Open **Resizer**.

**Saving images:** tap Save. The image goes straight into your gallery, in an album called
**Resizer** (folder `Pictures/Resizer`). It stays there even if you uninstall the app.
On Android 9 or older, the first save asks for storage permission.

**Updating:** install the new APK over the old one. Every build is signed with the same debug key
(`android/app/resizer-debug.keystore`), so Android accepts it as an update.

## Run on Windows

1. Download `Resizer-x.y.z-win-x64.zip` from the Releases page.
2. Right-click the zip > **Extract All...** and pick a folder (for example `Documents\Resizer`).
   Don't run it from inside the zip; it won't work properly.
3. Open the extracted folder and double-click **Resizer.exe**.
4. Windows SmartScreen may show "Windows protected your PC" because the app isn't code-signed.
   Click **More info > Run anyway**.
5. Optional: right-click `Resizer.exe` > **Show more options > Send to > Desktop (create shortcut)**.

There's no installer. To uninstall, delete the folder.

## Make a release

Releases are built by GitHub Actions (`.github/workflows/release.yml`) when you push a version tag:

```bash
git tag v1.0.1
```

```bash
git push origin v1.0.1
```

After ~10 minutes the APK and the zip appear on the Releases page. Use a higher number each time.

**Test build without a release:** go to the **Actions** tab > **Release** > **Run workflow**.
When it finishes, the APK and zip are under **Artifacts** at the bottom of that run's page.

## Develop locally

Needs [Node.js](https://nodejs.org) 22+.

```bash
npm install
```

Run the desktop app:

```bash
npm start
```

Android: after changing anything in `www/`, copy it into the Android project, then open it in
Android Studio (needs Android Studio + JDK 21):

```bash
npx cap sync android
```

```bash
npx cap open android
```

Regenerate app icons after editing `resources/icon.svg` (re-export the PNGs first):

```bash
npm run android:icons
```

## Project layout

```
www/                 the app itself (shared by Android and Windows)
  index.html
  native-save.js     Android saving (calls GallerySaverPlugin.java)
  vendor/capacitor.js  Capacitor core, lets plain <script> code use plugins
  fonts/             Bricolage Grotesque (SIL Open Font License, see OFL.txt)
electron/main.js     Windows/desktop window
android/             Android Studio project, made by Capacitor
  app/src/main/java/com/ylljonkr/resizer/GallerySaverPlugin.java   saves to the gallery
resources/           app icon source files
capacitor.config.json
```
