# Sarah — Dokumentacja projektu

Sarah to mobilna aplikacja Android dla seniorów, zapewniająca wsparcie emocjonalne przez czat z AI, ćwiczenia oddechowe i przyjaznego towarzysza — czerwoną pandę.

---

## Spis treści

1. [Stack technologiczny](#1-stack-technologiczny)
2. [Architektura](#2-architektura)
3. [Struktura projektu](#3-struktura-projektu)
4. [Backend — serwer Python](#4-backend--serwer-python)
5. [Ekrany aplikacji](#5-ekrany-aplikacji)
6. [Baza danych Room](#6-baza-danych-room)
7. [Przechowywanie ustawień — DataStore](#7-przechowywanie-ustawień--datastore)
8. [Nawigacja](#8-nawigacja)
9. [Komunikacja app ↔ serwer](#9-komunikacja-app--serwer)
10. [Uprawnienia i bezpieczeństwo sieciowe](#10-uprawnienia-i-bezpieczeństwo-sieciowe)
11. [Zasoby multimedialne](#11-zasoby-multimedialne)
12. [Uruchomienie serwera](#12-uruchomienie-serwera)

---

## 1. Stack technologiczny

### Android

| Warstwa | Technologia | Wersja |
|---|---|---|
| Język | Kotlin | 2.2.10 |
| UI | Jetpack Compose + Material 3 | BOM 2026.03.00 |
| Nawigacja | Navigation Compose | 2.8.0 |
| Dependency Injection | Hilt (Dagger) | 2.59.2 |
| Baza danych lokalna | Room + KSP | 2.7.1 |
| Preferencje | DataStore Preferences | 1.1.2 |
| Ładowanie obrazów | Coil Compose | 2.7.0 |
| Skaner QR | ML Kit Barcode Scanning | 17.3.0 |
| Podgląd kamery | CameraX | 1.4.2 |
| Uprawnienia runtime | Accompanist Permissions | 0.36.0 |
| Async | Kotlin Coroutines + Flow | — |
| Build system | Gradle (KTS) + KSP | AGP 9.1.1 |

### Backend

| Warstwa | Technologia |
|---|---|
| Framework | FastAPI (Python) |
| Serwer ASGI | Uvicorn |
| Klient LLM | OpenAI SDK (endpoint NVIDIA) |
| Model AI | `meta/llama-3.2-3b-instruct` |
| QR kod | qrcode + Pillow |
| Walidacja danych | Pydantic |

---

## 2. Architektura

Aplikacja stosuje wzorzec **MVVM (Model-View-ViewModel)**:

```
View (Composable)
    │  obserwuje StateFlow/collectAsState
    ▼
ViewModel (HiltViewModel)
    │  wywołuje metody repozytorium
    ▼
Repository
    │  odczytuje/zapisuje dane
    ▼
Data Source (Room DB / DataStore / HTTP)
```

- **View** — ekrany Compose (`*Screen.kt`), nie zawierają logiki biznesowej
- **ViewModel** — przechowuje stan UI jako `StateFlow`, zarządza cyklem życia operacji
- **Repository** — abstrakcja nad źródłami danych; wstrzykiwana przez Hilt
- **Hilt** — dostarcza zależności przez cały graf obiektów; `@HiltAndroidApp` na `SarahApplication`, `@AndroidEntryPoint` na `MainActivity`

Wyjątek: `BreathingScreen` przechowuje stan animacji lokalnie (`remember`), bo jest to czysto wizualny stan bez persystencji — ViewModel dostarcza jedynie głośność z ustawień.

---

## 3. Struktura projektu

```
sarah_app/
├── app/
│   └── app/src/main/
│       ├── java/com/example/sarah_app/
│       │   ├── SarahApplication.kt          ← @HiltAndroidApp
│       │   ├── MainActivity.kt              ← @AndroidEntryPoint, NavHost, BottomBar
│       │   │
│       │   ├── data/
│       │   │   ├── db/
│       │   │   │   ├── AppDatabase.kt       ← Room database (v2)
│       │   │   │   ├── UserProfileEntity.kt
│       │   │   │   ├── UserProfileDao.kt
│       │   │   │   ├── ChatSessionEntity.kt
│       │   │   │   ├── ChatSessionDao.kt
│       │   │   │   ├── ChatMessageEntity.kt
│       │   │   │   └── ChatMessageDao.kt
│       │   │   └── repository/
│       │   │       ├── UserProfileRepository.kt
│       │   │       ├── SettingsRepository.kt
│       │   │       ├── ServerRepository.kt
│       │   │       └── ChatRepository.kt
│       │   │
│       │   ├── di/
│       │   │   └── DatabaseModule.kt        ← Hilt: dostarcza DB, DAO-sy
│       │   │
│       │   └── ui/
│       │       ├── theme/                   ← kolory, typografia, motyw
│       │       ├── components/
│       │       │   └── SharedTopBar.kt
│       │       ├── home/        HomeScreen
│       │       ├── chat/        ChatScreen, ChatViewModel
│       │       │                ChatHistoryScreen, ChatHistoryViewModel
│       │       ├── breathing/   BreathingScreen, BreathingViewModel
│       │       ├── profile/     ProfileScreen, ProfileViewModel
│       │       ├── settings/    SettingsScreen, SettingsViewModel
│       │       ├── pairing/     PairingScreen, PairingViewModel
│       │       ├── gallery/     GalleryScreen
│       │       ├── rateus/      RateUsScreen, RateUsViewModel
│       │       └── about/       AboutSarahScreen
│       │
│       ├── assets/
│       │   └── panda/           ← panda1.jpg … panda15.jpg
│       └── res/
│           ├── raw/
│           │   ├── about_video.mp4
│           │   └── breathing_music.mp3
│           └── xml/
│               └── network_security_config.xml
│
└── serwer/
    ├── server.py
    └── requirements.txt
```

---

## 4. Backend — serwer Python

### Opis

Serwer FastAPI uruchamiany lokalnie na laptopie/PC. Przy starcie wykrywa lokalne IP, generuje kod QR (w terminalu ASCII i jako plik `sarah_qr.png`), który użytkownik skanuje aplikacją.

### Plik: `serwer/server.py`

```
FastAPI app
├── GET  /health   → {"status":"ok","model":"meta/llama-3.2-3b-instruct"}
└── POST /chat     → przyjmuje historię wiadomości, zwraca odpowiedź AI
```

**Żądanie POST /chat:**
```json
{
  "session_id": "abc-123",
  "messages": [
    {"role": "user", "content": "Boli mnie głowa"}
  ],
  "user_name": "Jan"
}
```

**Odpowiedź:**
```json
{
  "session_id": "abc-123",
  "reply": "Przykro mi to słyszeć. Czy boli od dawna?",
  "timestamp": "2026-04-19T18:00:00"
}
```

### Model AI

Używany model: **`meta/llama-3.2-3b-instruct`** przez NVIDIA API (endpoint OpenAI-compatible: `https://integrate.api.nvidia.com/v1`). Klucz API w zmiennej środowiskowej `NVIDIA_API_KEY`.

Parametry wywołania: `temperature=0.2`, `top_p=0.7`, `max_tokens=1024`, `stream=True`.

**System prompt Sarah:**
> You are Sarah, a warm and empathetic AI companion designed to support emotional wellbeing. You speak in a calm, friendly, and non-judgmental tone. You listen actively, ask thoughtful follow-up questions, and help users explore their feelings. You never diagnose or replace professional therapy. Keep responses concise — 2 to 4 sentences — unless the user clearly wants more depth. Always respond in the same language the user writes in.

### Uruchomienie

```bash
cd serwer
pip install -r requirements.txt
export NVIDIA_API_KEY="nvapi-..."
python server.py
```

---

## 5. Ekrany aplikacji

### 5.1 Home (`HomeScreen`)

Ekran startowy z siatką kart nawigacyjnych 2×2 + 1 pełna szerokość:

| Karta | Kolor | Nawiguje do |
|---|---|---|
| Breathing Exercises | PrimaryOrange | breathing |
| Profile | pomarańczowy | profile |
| Chat with Sarah | kremowy + panda | chat |
| Rate Us | AccentOrange | rate_us |
| About Sarah | CardBrown | about_sarah |

### 5.2 Chat (`ChatScreen` + `ChatViewModel`)

Ekran czatu z AI Sarah.

- **Wiadomości** wyświetlane jako dymki: użytkownik po prawej (PrimaryOrange), Sarah po lewej (kremowy)
- **„Sarah pisze..."** — spinner `CircularProgressIndicator` podczas oczekiwania na odpowiedź
- **Dialog błędu** przy problemach z połączeniem
- **`ChatViewModel`** wysyła `POST /chat` z pełną historią wiadomości; używa `withContext(Dispatchers.IO)` do operacji sieciowych; `readTimeout = 90s` (LLM może wolno odpowiadać)
- Każda wiadomość i odpowiedź jest automatycznie zapisywana do bazy Room (`ChatRepository`)
- `sessionId` generowany raz przy otwarciu ekranu (UUID), wysyłany z każdym requestem

### 5.3 Chat History (`ChatHistoryScreen` + `ChatHistoryViewModel`)

Lista poprzednich sesji czatu z bazy Room.

- Dostępna przez hamburger menu (≡) na ekranie Chat
- Każda sesja: podgląd pierwszej wiadomości + data/godzina
- `ChatHistoryViewModel` ładuje sesje jako `StateFlow<List<ChatSessionEntity>>`

### 5.4 Breathing Exercises (`BreathingScreen` + `BreathingViewModel`)

Ćwiczenie oddechowe **Box Breathing** — 4 fazy po 4 sekundy:

| Faza | Czas | Kolor pierścienia | Wskazówka |
|---|---|---|---|
| Inhale | 4 s | PrimaryOrange | Breathe in slowly through your nose |
| Hold your breath | 4 s | CardBrown | Keep the air in, stay still |
| Exhale | 4 s | AccentOrange | Release slowly through your mouth |
| Hold your breath | 4 s | CardBrown | Stay empty, stay calm |

**Elementy wizualne:**
- Animowany pierścień postępu (`drawArc` via `drawBehind`, `StrokeCap.Round`, `animateColorAsState`)
- Pulsujące kółko — skala wyliczana z `ringProgress.value` (synchronizacja z animacją)
- Licznik sekund wewnątrz kółka (64sp, serif, kolor fazy)
- W stanie spoczynku: emoji 🐼

**Audio:**
- Podczas ćwiczenia odtwarzany jest `res/raw/breathing_music.mp3` przez `MediaPlayer`
- Głośność sterowana suwakiem Sound z ekranu Settings (w czasie rzeczywistym przez `BreathingViewModel` → `SettingsRepository` → `DataStore`)
- `DisposableEffect(isRunning)` tworzy/zwalnia `MediaPlayer`; `LaunchedEffect(soundVolume)` aktualizuje głośność na bieżąco

### 5.5 Profile (`ProfileScreen` + `ProfileViewModel`)

Profil użytkownika zapisywany w Room (`UserProfileEntity`):

- Zdjęcie profilowe (okrągłe, 112dp) — picker z galerii
- Imię (edytowalne inline)
- Nastrój — 3 emotikony (😞 😐 😊), zaznaczona podświetlona AccentOrange
- Wiek (dialog z polem liczbowym)
- Płeć (dropdown: Male / Female / Other)
- Zaimki (dropdown lub TextField)

### 5.6 Settings (`SettingsScreen` + `SettingsViewModel`)

Ustawienia przechowywane w DataStore:

| Ustawienie | Komponent | Klucz DataStore |
|---|---|---|
| 🔊 Sound | Slider 0–1 | `sound_volume` (Float) |
| 🔔 Notifications | Switch | `notifications` (Boolean) |
| 🌐 Language | Dropdown (EN/PL/DE) | `language` (String) |
| 🔒 Privacy | Expandable card | — |
| 📶 Server connection | Kafelek → PairingScreen | — |

### 5.7 Server Connection (`PairingScreen` + `PairingViewModel`)

Ekran dostępny z Settings → „Server connection".

**Stan niesparowany:**
- Wyświetla podgląd kamery (CameraX) w kwadracie 260dp
- ML Kit `BarcodeScanning` wykrywa QR kod w czasie rzeczywistym
- Po wykryciu URL serwera zapisuje go do DataStore przez `ServerRepository`
- Prosi o uprawnienie `CAMERA` jeśli nie przyznane

**Stan sparowany:**
- Wyświetla aktualny URL serwera
- Przycisk „Rozłącz i sparuj ponownie" czyści URL z DataStore

**Startowanie aplikacji:** przy każdym uruchomieniu `MainActivity` odczytuje `serverUrl` z DataStore — jeśli `null`, start z `"pairing"`, jeśli zapisany — start z `"home"`.

### 5.8 Gallery (`GalleryScreen`)

Galeria zdjęć czerwonej pandy, dostępna jako zakładka w BottomBar.

- `LazyVerticalGrid` — 2 kolumny, zdjęcia kwadratowe 1:1
- 15 zdjęć ładowanych z `assets/panda/panda1.jpg … panda15.jpg`
- Coil `AsyncImage` z `crossfade` i `ContentScale.Crop`

### 5.9 Rate Us (`RateUsScreen` + `RateUsViewModel`)

- 5 interaktywnych gwiazdek — tapnięcie zaznacza N gwiazdek
- Ocena zapisywana w DataStore (`user_rating`, Int)
- Klikalny adres e-mail `support@sarah.com` (Intent do klienta pocztowego)

### 5.10 About Sarah (`AboutSarahScreen`)

- Wideo `res/raw/about_video.mp4` na górze ekranu — autoplay, pętla, z kontrolkami `MediaController`
- Tekst opisowy projektu
- Klikalny link do strony projektu: `https://konar.pwr.edu.pl/project/sarah`

---

## 6. Baza danych Room

Baza danych: `sarah_db`, wersja 2, `fallbackToDestructiveMigration`.

### Encje

**`UserProfileEntity`** (tabela `user_profile`)

| Pole | Typ | Opis |
|---|---|---|
| id | Int (PK=1) | singleton — jeden rekord |
| name | String | imię |
| age | Int | wiek |
| sex | String | płeć |
| pronouns | String | zaimki |
| mood | Int | 0=smutny, 1=neutralny, 2=szczęśliwy |
| photoUri | String? | URI zdjęcia |

**`ChatSessionEntity`** (tabela `chat_sessions`)

| Pole | Typ | Opis |
|---|---|---|
| id | String (PK) | UUID sesji |
| createdAt | Long | timestamp (ms) |
| preview | String | pierwsze 60 znaków pierwszej wiadomości |

**`ChatMessageEntity`** (tabela `chat_messages`)

| Pole | Typ | Opis |
|---|---|---|
| id | Long (autogenerate) | PK |
| sessionId | String (FK) | referencja do sesji (CASCADE DELETE) |
| role | String | `"user"` lub `"assistant"` |
| content | String | treść wiadomości |
| timestamp | Long | timestamp (ms) |

### DAO-sy

- `UserProfileDao` — `upsert`, `getProfile(): Flow<UserProfileEntity?>`
- `ChatSessionDao` — `insert`, `getAllSessions(): Flow<List<...>>`, `getById`
- `ChatMessageDao` — `insert`, `getMessagesForSession(): Flow<List<...>>`

---

## 7. Przechowywanie ustawień — DataStore

Plik `settings` (DataStore Preferences), klucze:

| Klucz | Typ | Domyślna | Opis |
|---|---|---|---|
| `sound_volume` | Float | 0.5 | głośność 0.0–1.0 |
| `notifications` | Boolean | true | powiadomienia włączone |
| `language` | String | "en" | język (en/pl/de) |
| `user_rating` | Int | 0 | ocena 1–5 gwiazdek |

Plik `server_prefs` (DataStore Preferences), klucze:

| Klucz | Typ | Opis |
|---|---|---|
| `server_url` | String? | URL serwera; null = niesparowany |

---

## 8. Nawigacja

`NavHost` z `rememberNavController()` w `MainActivity`. Bottom Navigation Bar zawiera 4 zakładki.

### Bottom Navigation Bar

| Ikona | Label | Route |
|---|---|---|
| 🏠 Home | Home | `home` |
| 💬 Chat | Chat | `chat` |
| 🖼 PhotoLibrary | Gallery | `gallery` |
| ⚙️ Settings | Settings | `settings` |

### Wszystkie routes

| Route | Ekran | Dostęp |
|---|---|---|
| `home` | HomeScreen | BottomBar |
| `chat` | ChatScreen | BottomBar |
| `chat_history` | ChatHistoryScreen | hamburger ≡ z Chat |
| `gallery` | GalleryScreen | BottomBar |
| `breathing` | BreathingScreen | karta na Home |
| `profile` | ProfileScreen | avatar w TopBar |
| `settings` | SettingsScreen | BottomBar |
| `pairing` | PairingScreen | Settings → Server connection |
| `rate_us` | RateUsScreen | karta na Home |
| `about_sarah` | AboutSarahScreen | karta na Home |

### Shared TopAppBar

- Widoczny na wszystkich ekranach
- **Lewo:** hamburger ≡ (tylko na `chat` i `chat_history`)
- **Środek:** tytuł ekranu (serif, BrownText)
- **Prawo:** okrągły avatar użytkownika (40dp) → nawiguje do `profile`

---

## 9. Komunikacja app ↔ serwer

```
[Android app]                        [Python server.py]
  ChatViewModel
    └─ sendMessage()
         └─ withContext(Dispatchers.IO)
              └─ HttpURLConnection POST /chat ──────────────────►
                 Content-Type: application/json; charset=utf-8
                 connectTimeout = 10s
                 readTimeout    = 90s
                                                 call_llm()
                                                   └─ openai client
                                                        └─ NVIDIA API
                                                             └─ Llama 3.2
                                          ◄──────────────────
              └─ parse JSON {"reply": "..."}
         └─ update UI state
         └─ save to Room (ChatRepository)
```

**Diagram parowania:**
```
server.py startuje
  └─ wykrywa lokalne IP → http://192.168.x.x:8000
  └─ generuje QR kod (terminal + sarah_qr.png)

użytkownik skanuje QR w PairingScreen
  └─ CameraX + ML Kit odczytuje URL
  └─ ServerRepository.saveServerUrl(url) → DataStore
  └─ app przechodzi do HomeScreen
```

---

## 10. Uprawnienia i bezpieczeństwo sieciowe

**`AndroidManifest.xml`:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

**`res/xml/network_security_config.xml`:**
```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

Zezwolenie na HTTP (cleartext) wymagane do komunikacji z serwerem lokalnym przez HTTP. W środowisku produkcyjnym należy zastąpić HTTPS i ograniczyć domenę.

Uprawnienie `CAMERA` pyta użytkownika runtime przez Accompanist `rememberPermissionState`.

---

## 11. Zasoby multimedialne

| Zasób | Ścieżka | Użycie |
|---|---|---|
| panda1–15.jpg | `assets/panda/` | galeria zdjęć (`GalleryScreen`) |
| about_video.mp4 | `res/raw/` | wideo na ekranie About |
| breathing_music.mp3 | `res/raw/` | muzyka podczas ćwiczeń oddechowych |

Obrazy ładowane przez **Coil** z URI `file:///android_asset/panda/pandaX.jpg`.  
Wideo odtwarzane przez `VideoView` (Android SDK) via `AndroidView` w Compose.  
Audio odtwarzane przez `MediaPlayer` (Android SDK); głośność zsynchronizowana z suwakiem Sound w Settings.

---

## 12. Uruchomienie serwera

```bash
cd serwer
pip install -r requirements.txt
# Ustaw klucz API NVIDIA:
export NVIDIA_API_KEY="nvapi-..."      # Linux/macOS
set NVIDIA_API_KEY=nvapi-...           # Windows CMD

python server.py
```

Serwer wypisze w terminalu adres IP i kod QR. Zeskanuj kod aplikacją Sarah (Settings → Server connection).

### Wymagania serwera

```
fastapi==0.115.12
uvicorn[standard]==0.34.0
openai>=1.30.0
qrcode==8.1
pillow==11.2.1
```

### Wymagania systemu Android

- Minimalne SDK: 26 (Android 8.0)
- Target SDK: 35
- Orientacja: tylko pionowa (portrait)
- Telefon i serwer muszą być w **tej samej sieci lokalnej (Wi-Fi)**
