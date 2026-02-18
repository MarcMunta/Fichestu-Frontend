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
3. Ejecuta el módulo `app` en un emulador o dispositivo.

También puedes compilar por consola:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Configuración del backend

La URL por defecto del backend es:

- `http://10.0.2.2:8080/`

Esta URL funciona desde el emulador Android cuando el backend corre en tu PC local.

La app se conecta automáticamente a esta URL y no permite cambiarla desde el frontend.

## Endpoints usados por la app

La app está simplificada a autenticación básica:

- `POST /api/auth/login`
- `POST /api/auth/register`

Si el contrato cambia, ajusta:

- `app/src/main/java/com/fichestu/frontend/data/remote/AuthApi.kt`
