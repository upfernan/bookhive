# BookHive Web

Aplicación web de BookHive, construida con Angular 22 y Angular Material.

## La clave de Identity Platform

`src/app/session/identity-provider.settings.ts` se sube con el marcador `PEGA_AQUI_LA_API_KEY`, nunca con la clave. La clave vive solo en Secret Manager, en el secreto `identity-api-key` (ADR-043). Al publicar, el workflow `frontend.yml` la lee de ahí y reemplaza el marcador antes de compilar.

Para probar el inicio de sesión en local, pega la clave en ese archivo solo mientras pruebas y no subas ese cambio: el workflow rechaza el archivo si no tiene el marcador.

La clave debe estar restringida en **APIs y servicios → Credenciales** a los dominios de BookHive.

## Desarrollo

```bash
npm install
npm start
```

La aplicación queda en `http://localhost:4200/`. Las rutas `/api` y `/actuator` se envían al balanceador de cargas (`https://136.81.95.29.nip.io`), según `proxy.conf.json`. El balanceador las pasa al API Gateway y este al backend; ni el gateway ni el backend se pueden llamar directamente.

## Compilación

```bash
npm run build
```

Los archivos quedan en `dist/bookhive-web/browser`, que es lo que se sube al depósito de Cloud Storage (ADR-033).

## Despliegue

Cada cambio en `Frontend/` que llega a `main` se publica solo en el depósito `bookhive-web-511618` con el workflow `frontend.yml`. Primero se suben los archivos nuevos y al final `index.html`, sin caché, para que nadie cargue una página que apunte a archivos que todavía no existen. En los pull request el workflow solo compila.

El depósito tiene `index.html` como página de error, así que rutas como `/prestamos` cargan la aplicación y Angular muestra la pantalla que corresponde.
