# Fichestu Frontend (Android nativo)

Este repositorio contiene solo el frontend Android nativo en Kotlin (Jetpack Compose).

El backend Spring Boot vive en otro repositorio:

- <https://github.com/MarcMunta/Fichestu-Backend>

## Requisitos

- Android Studio (Hedgehog o superior)
- Android SDK instalado
- JDK 17

## Ejecutar

1. Abre este proyecto en Android Studio.
2. Sincroniza Gradle.
3. Ejecuta el modulo `app` en un emulador o dispositivo.

Tambien puedes compilar por consola:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Configuracion del backend

La URL por defecto del backend es:

- `http://26.226.245.83:8080/`

Se define en:

- `app/build.gradle.kts` -> `BuildConfig.BASE_URL`

Para backend local desde emulador Android usa:

- `http://10.0.2.2:8080/`

Para dispositivo fisico usa la IP LAN/VPN del host Docker.

## Endpoints usados por la app

La app usa JWT Bearer guardado en `SharedPreferences`.

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`
- `GET /api/game/market`
- `POST /api/game/market/buy`
- `POST /api/game/market/sell`
- `POST /api/game/rewarded/claim`
- `POST /api/game/ball-room/enter`
- `GET /api/game/match/state`
- `POST /api/game/matches/{matchId}/pick-ball`
- `POST /api/game/matches/{matchId}/reveal`
- `POST /api/game/matches/{matchId}/battle/round`

Estas rutas son aliases legacy compatibles con el backend Docker actual. El backend nuevo tambien expone el contrato canonico `/api/me`, `/api/market`, `/api/rewards/*`, `/api/games/*`.

## Validacion en tablet emulator

AVD probado:

- `tablet_proyecto`
- `emulator-5554`
- resolucion `2560x1800`

Comandos:

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --console=plain --quiet
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s emulator-5554 shell am start -n com.fichestu.frontend/.MainActivity
```

Si el contrato cambia, ajusta:

- `app/src/main/java/com/fichestu/frontend/data/remote/AuthApi.kt`
- `app/src/main/java/com/fichestu/frontend/data/remote/UserApi.kt`
- `app/src/main/java/com/fichestu/frontend/data/remote/MarketApi.kt`
- `app/src/main/java/com/fichestu/frontend/data/remote/GameApi.kt`
