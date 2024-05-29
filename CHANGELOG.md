# Changelog

## unreleased
- added support for Google Drive
- added percentage display while uploading
- fixed login for new Internet Archive profiles
- fixed UI glitch when changing day-/night-theme
- fixed upload dialog text overflow

## 0.3.0
- Updated translations, fixed typos.
- Added notifications for background uploads, so app stays alive on newer Android versions.

## 0.3.0 beta8
- Fixed ANR on Android 14
- Fixed excessive database reads/writes during file upload
- Added file picker support on more devices

## 0.3.0 beta7
- Show circular upload progress.
- Fixed display when no folder created, yet.
- Removed licensing options after browsing folders again.
- Updated translations.
- Reworked WebDAV and Dropbox upload for better stability and correctness. Fixes flag bug.
- Added tag "Significant Content" to metadata, when item is flagged.

## 0.3.0 beta6
- Improved background upload stability.
- Fixed file errors showing when pausing the upload while upload is in progress.
- Added "undo" when deleting files from the upload manager scene.
- Show option to restart a failed upload in upload manager scene.
- Fixed ordering and layout of main scene.
- Fixed problem with changed upload priority getting lost again.
- Fixed some crashes.

## 0.3.0 beta5
- Added Internet Archive tutorial.
- UI/UX brushing of main scene and upload management scene.
- Show errors of media items in main scene.
- Delete items, when they get removed from the upload queue.
- Better styled add-media dialog, which shows on long-tapping the "+" (add) button.
- Updated dependencies to latest versions. Cleaned out old, unused dependencies.
- Improved health check consent form.
- Allow CC editing right when folder is added. (As long as there is no overriding license set up for the server.)
- Fixed bug when adding media, where sometimes the first one wouldn't be in the correct collection.
- Fixed bug where media of same collection would have been written to different folders.
- Improved background upload.

## 0.3.0 beta4
- Translation and wording updates.
- Fixed lots of UI bugs.
- Added long-tap on + (add) button showing menu to import documents. (Only with supported devices!)
- Removed Google Play Service dependency.

## 0.3.0 beta3
- New review and batch review scene.
- Moved all review from main scene one level deeper into preview scene.
- Fixed issue in upload manager scene.

## 0.3.0 beta2
- Introduce new preview scene.
- Fixed minor wording issues.
- Fixed superfluous creation of new collection, when there already is an open collection.
- Updated translations.

## 0.3.0 beta1

- Introduced UI/UX overhaul finished about 2/3rds.
- Fixed tons of minor and not-so-minor bugs.
- Nextcloud upload chunking now configurable per WebDAV-server, instead of globally.
- Added licensing configurable per-server (aka. "space") instead of just per folder (aka. "project").
- Completely new onboarding.
- Screenshot prevention now configurable.
- Fixed web links.
- Support for biometry/device passcode to secure the ProofMode signing key.

## 0.2.6

- Fixed Dropbox login via browser, when Dropbox app is not installed.
- Also improved Internet Archive and WebDAV login.
- Improved Tor/Orbot support.
- Updated localization.
- Dependency updates.
- General code cleanup.
