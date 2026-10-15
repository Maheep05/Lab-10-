# Experiment 10 — Contacts via Android Content Provider

**Name:** Maheep Singh Saluja
**USN:** 25MCAR0105

An Android app that reads the device's contacts through the system **Content Provider** (`ContactsContract`) and hands off a selected contact to the system **Contacts** app via an `Intent`. Built with Kotlin, `AppCompat`, and Material 3.

---

## 1. About the Experiment

### Aim
Demonstrate inter-application data sharing in Android using a **Content Provider** as the source of structured data.

### Concept / Technology

**Content Providers** are one of Android's four core application components. They expose an app's data to other apps through a uniform CRUD interface backed by a `content://` URI, letting consumers query data without knowing the storage format (SQLite, file, network, etc.).

The system itself ships several providers; this experiment consumes the **Contacts Provider**:

| Element | Purpose |
|---|---|
| `ContactsContract.CommonDataKinds.Phone.CONTENT_URI` | The `content://` endpoint for phone-number rows |
| `contentResolver.query(...)` | Issues the SELECT against the provider |
| `READ_CONTACTS` runtime permission | Gate required since Android 6.0 (API 23) |
| `Intent(ACTION_VIEW, contactUri)` | Hands the selected contact to the system Contacts app |

### Scenario
A **"Contacts Viewer"** screen that:

1. Shows a **Load contacts** button on launch.
2. On tap, requests `READ_CONTACTS` at runtime (if not already granted).
3. Queries the Contacts Content Provider and renders each contact as a card with an avatar initial, display name, and phone number.
4. Shows a live **count pill** (e.g. `12 contacts`).
5. On tapping any contact, fires an implicit `Intent` so the system Contacts app opens that contact's detail page — demonstrating cross-app data sharing via the `content://contacts/...` URI.

---

## 2. Project Structure

```
Experiment10/
├── build.gradle.kts                 # Root Gradle config
├── settings.gradle.kts              # Module declarations
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml           # Version catalog (AGP, Kotlin, Material)
│   └── wrapper/                     # Gradle wrapper
│
└── app/
    ├── build.gradle.kts             # App module config (SDK, deps, Material 3)
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml  # Declares READ_CONTACTS / WRITE_CONTACTS
        │   │
        │   ├── java/com/example/experiment10/
        │   │   └── MainActivity.kt  # All logic: permission, query, adapter, intent
        │   │
        │   └── res/
        │       ├── layout/
        │       │   ├── activity_main.xml   # Header + button + count pill + ListView
        │       │   └── item_contact.xml    # Row: avatar + name + number + chevron
        │       │
        │       ├── drawable/
        │       │   ├── card_background.xml      # Rounded card for each row
        │       │   ├── avatar_background.xml    # Circular avatar bubble
        │       │   ├── count_pill_background.xml
        │       │   └── ic_launcher_*.xml
        │       │
        │       ├── values/
        │       │   ├── colors.xml       # Surface, text, 5-colour avatar palette
        │       │   ├── strings.xml      # app_name = "Experiment10"
        │       │   └── themes.xml       # Material 3 theme
        │       │
        │       ├── values-night/themes.xml
        │       ├── mipmap-*/            # Launcher icons
        │       └── xml/
        │           ├── backup_rules.xml
        │           └── data_extraction_rules.xml
        │
        ├── test/                    # JVM unit test stub
        └── androidTest/             # Instrumented test stub
```

### Key files

- **`MainActivity.kt`** — holds the entire flow:
  - `checkPermissionAndLoadContacts()` — runtime permission gate
  - `loadContacts()` — the `contentResolver.query(...)` against `ContactsContract`
  - `openContactInSystemApp(...)` — `ACTION_VIEW` on the contact URI
  - `ContactsAdapter` — custom `BaseAdapter` with a 5-colour avatar palette
- **`AndroidManifest.xml`** — declares `READ_CONTACTS` and `WRITE_CONTACTS` so Android will prompt the user.
- **`activity_main.xml`** / **`item_contact.xml`** — the two layouts the activity inflates.

---

## 3. Output

Default screen on first launch (before contacts are loaded):

<img width="1080" height="2400" alt="test3_intent_handoff" src="https://github.com/user-attachments/assets/b5e42fa9-7af2-44f1-975d-f7d43c0398b7" />

<img width="1080" height="2400" alt="output" src="https://github.com/user-attachments/assets/df78de63-c4b0-4e89-be55-598921c98a3d" />



---

## 4. Test Cases

> Place the referenced images in a `screenshots/` folder at the project root.

### Test Case 1 — Permission Prompt on First Load
**Steps:** Fresh install → tap **Load contacts**.
**Expected:** System dialog "Allow Experiment10 to access your contacts?" appears.
**Result:** Dialog shown; selecting *Allow* proceeds to load, *Deny* shows a toast.
<img width="1080" height="2400" alt="test3_intent_handoff" src="https://github.com/user-attachments/assets/b5e42fa9-7af2-44f1-975d-f7d43c0398b7" />


---

### Test Case 2 — Contacts Loaded and Rendered (shows USN & Name)
**Steps:** Grant permission. A test contact had been pre-saved on the device:
- **Name:** `Maheep Singh Saluja`
- **Phone (stored as the USN field):** `25MCAR0105`

**Expected:** The ListView renders every device contact as a card. The pill at the top shows the live count (e.g. `13 contacts`). The row for *Maheep Singh Saluja* shows the avatar initial **M**, the full name, and the USN `25MCAR0105` beneath it.
**Result:** All contacts load alphabetically; the `Maheep Singh Saluja / 25MCAR0105` row is visible in the list.
<img width="1080" height="2400" alt="test2_usn_name" src="https://github.com/user-attachments/assets/b808ce39-6ca7-4e34-95d8-a69d811d62e2" />


---

### Test Case 3 — Tap-to-Open Hand-off via Intent
**Steps:** From the loaded list, tap any contact row.
**Expected:** An implicit `Intent(ACTION_VIEW, content://com.android.contacts/contacts/<id>)` is fired; the system **Contacts** app opens on that contact's detail page.
**Result:** System Contacts app opens showing the selected contact — confirming the Content Provider URI was successfully shared across apps.
<img width="1080" height="2400" alt="test1_permission" src="https://github.com/user-attachments/assets/421ae836-e45e-4541-b9c9-1da52557df68" />


---

## 5. How to Run

1. Open the project in **Android Studio** (Giraffe or newer).
2. Let Gradle sync; the version catalog in `gradle/libs.versions.toml` pins all deps.
3. Run on an emulator or physical device with at least a few saved contacts (`Settings → Contacts` to add some first).
4. On launch, tap **Load contacts** and accept the permission.

Min SDK and target SDK are set in `app/build.gradle.kts`.

---

**Submitted by:** Maheep Singh Saluja — **USN:** 25MCAR0105

