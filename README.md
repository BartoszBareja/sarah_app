# Sarah – serwer + parowanie QR

## Struktura plików

```
sarah_server/
├── server.py              ← serwer Python (uruchamiasz na laptopie)
├── requirements.txt
│
├── ServerRepository.kt    → data/repository/ServerRepository.kt
├── PairingScreen.kt       → ui/pairing/PairingScreen.kt          (NOWY)
├── PairingViewModel.kt    → ui/pairing/PairingViewModel.kt       (NOWY)
├── ChatViewModel.kt       → ui/chat/ChatViewModel.kt             (NOWY)
└── ChatScreen.kt          → ui/chat/ChatScreen.kt                (zastąp)
```

---

## 1. Uruchomienie serwera

```bash
pip install -r requirements.txt
export ANTHROPIC_API_KEY="sk-ant-..."
python server.py
```

Po starcie w terminalu zobaczysz QR kod, np.:
```
==================================================
  Sarah Server uruchomiony!
  Adres: http://192.168.1.42:8000
==================================================

  Zeskanuj kod QR aplikacją Sarah:

  [QR KOD ASCII]

  QR zapisany jako plik: sarah_qr.png
==================================================
```

---

## 2. Zmiany w projekcie Android

### a) build.gradle.kts (app/app/build.gradle.kts) — dodaj zależności

```kotlin
dependencies {
    // ... istniejące zależności ...

    // Skaner QR (ML Kit)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // CameraX (do skanera)
    val cameraVersion = "1.4.2"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")

    // Accompanist Permissions (prośba o kamerę)
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
}
```

### b) AndroidManifest.xml — dodaj uprawnienia i network config

```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <application
        android:networkSecurityConfig="@xml/network_security_config"
        ... >
```

### c) Utwórz app/src/main/res/xml/network_security_config.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

(Zezwala na HTTP w sieci lokalnej. W produkcji użyj HTTPS i usuń to.)

### d) MainActivity.kt — dodaj routing na PairingScreen

Zastąp `composable("chat") { ChatScreen() }` poniższym fragmentem,
oraz dodaj import `serverRepository` przez Hilt lub odczytaj URL z DataStore:

```kotlin
// Na górze klasy MainActivity, dodaj obsługę stanu parowania.
// Najprostszy sposób — sprawdź w NavHost czy serwer jest zapisany:

composable("chat") {
    val serverUrl by serverRepository.serverUrl
        .collectAsStateWithLifecycle(initialValue = null)   // wymaga lifecycle-runtime-compose

    if (serverUrl == null) {
        PairingScreen(onPaired = { /* nawigacja odświeży się sama przez flow */ })
    } else {
        ChatScreen()
    }
}
```

Alternatywnie (prostsze) — dodaj osobny route "pairing":

```kotlin
composable("chat") {
    val context = LocalContext.current
    // ChatScreen sam sprawdzi połączenie przez ServerRepository (przez Hilt)
    ChatScreen()
}
composable("pairing") {
    PairingScreen(onPaired = { navController.navigate("chat") })
}
```

I w starcie aplikacji sprawdź czy URL jest zapisany:

```kotlin
// W onCreate lub jako startDestination:
val startRoute = if (serverUrlSaved) "home" else "pairing"
```

---

## 3. Jak to działa end-to-end

```
[Python server.py]         [Android app]
  startuje
  wykrywa IP → http://192.168.1.42:8000
  generuje QR ──────────────────────────► użytkownik skanuje QR
                                          PairingScreen zapisuje URL do DataStore
                                          przechodzi do ChatScreen
  
  czeka na requesty ◄──── użytkownik pisze "Boli mnie głowa"
                           ChatViewModel wysyła POST /chat
                               { messages: [{role:"user", content:"Boli mnie głowa"}] }
  
  odbiera request
  wysyła do Claude API ──► Claude API
                    ◄────── "Zjedz Ibuprofen i odpocznij."
  zwraca odpowiedź ────────────────────► ChatScreen wyświetla dymek Sarah
```

---

## 4. Ponowne parowanie

Żeby zresetować URL serwera (np. zmieniło się IP laptopa):
```kotlin
// Wywołaj z dowolnego miejsca przez ServerRepository:
serverRepository.clearServerUrl()
```
Możesz dodać opcję "Połącz ponownie" w Settings lub długie przytrzymanie w ChatScreen.
