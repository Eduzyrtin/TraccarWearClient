# Tracker for Traccar (Wear OS)

App de Wear OS que envía la ubicación del reloj a un servidor [Traccar](https://www.traccar.org).
Es independiente: no necesita app en el teléfono, y el móvil puede seguir con el Traccar Client oficial.

El tracking lo hace el SDK oficial, [`org.traccar:traccar-client-sdk`](https://github.com/traccar/traccar-client-sdk),
el mismo motor que usa Traccar Client. Para incorporar las mejoras del oficial basta con subir
`traccarSdk` en `gradle/libs.versions.toml`.

## Pausa con el móvil cerca

Con *Pausa con el móvil cerca* activado (por defecto), el reloj solo transmite cuando el teléfono
**no** está conectado por Bluetooth:

- Se considera que el móvil está cerca cuando Wear OS lo marca como `isNearby`, es decir, conectado
  directamente por Bluetooth. Si el reloj tiene LTE o Wi-Fi, el móvil puede seguir apareciendo como
  conectado a través de la nube, y eso **no** cuenta como cerca.
- Disparadores: los avisos de Bluetooth del sistema, que necesitan el permiso *Dispositivos cercanos*,
  el arranque del reloj y una comprobación cada 15 minutos.
- Al alejarse, empieza a transmitir en cuanto Wear OS confirma que el móvil no está. Al volver,
  para tras 15 segundos.
- Cuando está en pausa, el GPS queda **apagado**, no solo el envío.

Código: `PhoneProximity`, `TrackingGate`, `GateReceiver` y `GateWorker`.

## Configuración en Traccar

1. En el servidor, da de alta un dispositivo **nuevo** para el reloj, distinto del móvil, con el
   identificador que muestra la app (*Ajustes › Identificador*).
2. En el reloj, *Ajustes › URL del servidor* es la del puerto de la app cliente, normalmente
   `http://servidor:5055`.
3. Pulsa *Iniciar* y concede los permisos de ubicación, también *Permitir siempre*, y el de
   dispositivos cercanos.

## Compilar

Android Studio con AGP 9.4, JDK 17 o superior y el SDK de Android 37.

```sh
./gradlew assembleDebug
```

## Pendiente

- Probarlo en un reloj real (pausa, consumo, arranque en segundo plano en Wear OS 5 y 6).
- Tile o complicación con el estado.
- Elegir licencia.
