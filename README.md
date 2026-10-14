# Escritura Métrica PRO — Aplicación Android

Esta carpeta contiene la **versión 4** del editor de escritura, adaptada a APK Android.
No necesita conexión a Internet para funcionar.

## Desde el teléfono, usando GitHub y Codespaces

1. Abre https://github.com/new e inicia sesión.
2. Repositorio: `escritura-metrica-pro-android`. Selecciona **Private** y **Create repository**. Si te ofrece un README inicial, puedes aceptarlo.
3. En el repositorio, selecciona **Add file → Upload files**, sube `Escritura_Metrica_PRO_GitHub.zip`, y confirma **Commit changes**.
4. Entra en **Code → Codespaces → Create codespace on main**.
5. En el terminal de Codespaces, copia y ejecuta exactamente esta línea:

```bash
unzip -oq Escritura_Metrica_PRO_GitHub.zip && rm Escritura_Metrica_PRO_GitHub.zip && git add -A && git commit -m "App Android Escritura Metrica PRO" && git push
```

6. Abre el enlace **https://github.com/ellmeer/escritura-metrica-pro-android/actions**
   (o abre la pestaña **Actions** en tu repositorio si usas otro nombre).
7. Abre **Crear APK Android** → la ejecución verde (correcta) → baja a **Artifacts** → descarga `EscrituraMetricaPRO-Android-APK`.
8. Extrae el ZIP descargado de GitHub y ejecuta `app-debug.apk` en Android. Es posible que debas permitir instalaciones desde el navegador o gestor de archivos.

**Nota:** el primer commit que solo contiene el ZIP no ejecuta la compilación, porque aún no existe la carpeta `.github/workflows/`. El `git push` del paso 5 la agrega y dispara la compilación.

## Funcionamiento

- El HTML está en `app/src/main/assets/index.html`.
- Funciona como app nativa sencilla que usa Android WebView y WebViewAssetLoader.
- El guardado local automático usa `localStorage` e IndexedDB.
- En Android 10 o superior, los respaldos/exportaciones se escriben en `Descargas/EscrituraMetrica/` (la copia automática JSON usa el intervalo original de la V4).
- El selector de archivos permite restaurar copias JSON e importar documentos.
- Los datos del navegador anterior **no se migran solos** a la app Android. Importa una copia JSON una vez, si ya escribiste en la versión HTML.
- Desinstalar la aplicación puede eliminar los datos internos; conserva los JSON en Descargas.
- La versión `debug` usa firma de desarrollo generada por GitHub; builds posteriores pueden usar otra firma. Para actualizaciones fluidas y permanentes se necesita una firma release estable.

## Información técnica

- Paquete: `com.escriturametrica.pro`
- Android: 8.0 o posterior (minSdk 26)
- No pide permiso de Internet ni acceso general a archivos.
- Android Gradle Plugin: 8.9.2 / Gradle 8.11.1 / Java 17.
- Compila en GitHub Actions, sin instalar Android Studio.

## Privacidad

El texto se procesa localmente dentro de la app. No se conecta a servidores para enviar documentos.
