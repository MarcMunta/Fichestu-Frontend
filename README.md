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

- `http://10.0.2.2:8081/`

Esta URL funciona desde el emulador Android cuando el backend corre en Docker.
Si arrancas backend en local con `spring-boot:run` (puerto `8080`), la app intenta fallback automatico entre `8081` y `8080`.

## Endpoints usados por la app

La app usa autenticacion JSON contra el backend Spring:

- `POST /api/auth/login`
- `POST /api/auth/register`

Si el contrato cambia, ajusta:

- `app/src/main/java/com/fichestu/frontend/data/remote/AuthApi.kt`
