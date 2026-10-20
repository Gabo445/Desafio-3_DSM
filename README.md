# Atlas Mundial – Desafío Práctico #03 (Desarrollo de Software para Móviles, UDB)

App Android (Kotlin, Views + ViewBinding) que consume **REST Countries v5**, **GeoNames** y **WeatherAPI**.

## Flujo
1. **Regiones** → 2. **Países de la región** (nombre, bandera, capital) → 3. **Detalle del país**: datos completos,
   hasta 10 ciudades principales (capital destacada), clima actual por ciudad y mapa interactivo (osmdroid).
   Al tocar una ciudad el mapa se centra en ella y muestra su globo con nombre y clima.

## Arquitectura MVC
| Capa | Paquete | Contenido |
|---|---|---|
| Modelo | `models`, `models.api`, `models.mappers`, `network` | Data classes, DTOs Gson, mappers, interfaces Retrofit, manejo de errores |
| Vista | `views`, `views.adapters` + `res/layout` | Activities, adaptadores RecyclerView, layouts XML |
| Controlador | `controllers` | `RegionController`, `CountryController`, `CityController` (lógica, corrutinas) |

Las Vistas solo hablan con los Controladores; los Controladores devuelven `ApiResult<T>` (Success/Failure con `AppError`).

## Abrir el proyecto
Abre esta carpeta completa con File > Open en Android Studio (Panda 4). Gradle 9.4.1 se descarga solo.
Antes de compilar, crea `local.properties` (ver abajo).

## Credenciales (no se suben al repo)
1. Abre `local.properties` (raíz del proyecto, ya está en `.gitignore`) y agrega las variables de `local.properties.example`:
```
RESTCOUNTRIES_API_TOKEN=...
GEONAMES_USERNAME=...
WEATHERAPI_KEY=...
```
2. Sincroniza Gradle y compila (se generan como campos de `BuildConfig`).
- REST Countries: token en https://restcountries.com/sign-up (se envía como `Authorization: Bearer`).
- GeoNames: crea cuenta y **activa "Free Web Services"** en https://www.geonames.org/manageaccount.
- WeatherAPI: API key gratuita en https://www.weatherapi.com/.

## APK
Enlace de descarga: _(pegar aquí)_
[Descargar APK (app-debug.zip)](https://drive.google.com/file/d/1oenqH_3f5whiq6j4Hs19dZoKLpbCWh6X/view?usp=sharing)