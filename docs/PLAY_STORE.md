# Google Play: Wear OS Client for Traccar

Paquete `com.sanabria.traccarwearosclient` · solo Wear OS · ficha en [`fastlane/metadata/android/`](../fastlane/metadata/android)
(formato estándar, ver §8 de `HERRAMIENTAS/GUIA_PROYECTOS.md`). Idiomas: los 9 de la app; `en-US` es el predeterminado.

- **Precio**: gratuita. También se descarga gratis desde GitHub (Releases).

## Qué subir y dónde (Play Console › Crecimiento › Presencia en la tienda › Ficha principal)

1. **Idiomas**: Gestionar idiomas → añade `es-ES`, `fr-FR`, `de-DE`, `it-IT`, `pt-PT`, `ca`, `eu-ES`, `gl-ES`. Pega en cada uno
   `title.txt`, `short_description.txt` y `full_description.txt` de su carpeta.
2. **Icono**: `en-US/images/icon.png` (vale para todos los idiomas).
3. **Gráfico de funciones** (obligatorio): `<idioma>/images/featureGraphic.png`, con el lema traducido.
4. **Capturas de Wear OS**: las de `en-US/images/wearScreenshots/` (3; 1:1, entre 384 y 3840 px). Valen para todos los idiomas.
   Si quieres capturas con la interfaz traducida, sácalas del emulador con el reloj en ese idioma.
5. **Novedades** de cada versión: `<idioma>/changelogs/<versionCode>.txt`.
6. Sin capturas de móvil ni tableta: la app no tiene versión de teléfono.

## Antes de publicar (lo que Play pedirá)

- **Política de privacidad (URL)**: obligatoria. El README ya describe la privacidad; publica ese texto en una URL.
- **Seguridad de los datos**: recoge **ubicación precisa y aproximada** (se envía al servidor que indica el usuario) e
  **ID de dispositivo** (el identificador que genera la app). Como se permite `http://`, no marques «datos cifrados en
  tránsito». No se comparte con terceros ni hay analíticas.
- **Ubicación en segundo plano**: formulario de declaración y **vídeo corto**: pedir «Permitir siempre», iniciar el
  seguimiento y la notificación que aparece. Justificación: enviar la posición del reloj al servidor Traccar mientras la
  pantalla está apagada y el usuario va sin el móvil.
- **Servicio en primer plano de tipo ubicación**: su propia declaración, con la misma justificación y el mismo vídeo
  (guárdalo en `docs/play-videos/`, que git ignora).
- **Exención de optimización de batería**: Play la restringe. Caso válido: app de seguimiento que debe arrancar en segundo
  plano; la descripción ya la presenta como opcional.
- **Clasificación de contenido**, **público objetivo** (solo adultos), **anuncios** (ninguno) y factor de forma **Wear OS** al subir el AAB.
- **Marca**: la descripción dice que no está afiliada a Traccar. Mantén ese texto para evitar un rechazo por uso de marca.
- **Enlace al repositorio**: la descripción apunta a `github.com/Eduzyrtin/TraccarWearClient`. Si el repo sigue privado, quita
  la sección «FREE AND OPEN SOURCE» (y su traducción) de los 9 idiomas, o haz el repo público antes de publicar.
