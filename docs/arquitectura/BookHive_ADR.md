# Registro de decisiones de arquitectura — BookHive

| | |
|---|---|
| **Proyecto** | BookHive — plataforma de gestión de bibliotecas institucionales |
| **Equipo** | Johan Camilo Bedoya · Fernando Zuluaga Botero |
| **Decisiones registradas** | 62 |
| **Período** | 04/09/2026 – 29/09/2026 |

## Índice

| ID | Decisión | Fecha | Estado |
|---|---|---|---|
| [ADR-001](#adr-001) | Selección de la plataforma de nube para el ambiente de despliegue | 16/09/2026 | Aceptado |
| [ADR-002](#adr-002) | Entorno de ejecución del backend | 18/09/2026 | Propuesto |
| [ADR-003](#adr-003) | Formato de empaquetado del backend | 18/09/2026 | Propuesto |
| [ADR-004](#adr-004) | Patrón arquitectónico interno del backend | 18/09/2026 | Propuesto |
| [ADR-005](#adr-005) | Modelo de programación del sistema: Reactivo, no reactivo o mixto | 17/09/2026 | Propuesto |
| [ADR-006](#adr-006) | Selección de la tecnología del backend | 20/09/2026 | Propuesto |
| [ADR-007](#adr-007) | Estilo de comunicación de la API de BookHive | 17/09/2026 | Propuesto |
| [ADR-008](#adr-008) | Selección del estilo arquitectónico de la aplicación web | 19/09/2026 | Propuesto |
| [ADR-009](#adr-009) | Selección de la tecnología de la aplicación web | 19/09/2026 | Propuesto |
| [ADR-010](#adr-010) | Elección del tipo de motor de base de datos: relacional vs no relacional | 04/09/2026 | Aceptado |
| [ADR-011](#adr-011) | Estrategia de aislamiento de datos por institución a nivel de base de datos | 06/09/2026 | Propuesto |
| [ADR-012](#adr-012) | Elección del motor de base de datos relacional | 04/09/2026 | Aceptado |
| [ADR-013](#adr-013) | Disponibilidad de la base de datos ante la falla del servidor | 07/09/2026 | Aceptado |
| [ADR-014](#adr-014) | Copias de seguridad de la información | 07/09/2026 | Aceptado |
| [ADR-015](#adr-015) | Selección de la herramienta de migraciones de base de datos | 12/09/2026 | Propuesto |
| [ADR-016](#adr-016) | Manejo de zona horaria en los registros del sistema | 05/09/2026 | Propuesto |
| [ADR-017](#adr-017) | Catálogo de parámetros de negocio (Parameter catalog) | 18/09/2026 | Aceptado |
| [ADR-018](#adr-018) | Catálogo de mensajes de la aplicación (Message Catalog) | 18/09/2026 | Aceptado |
| [ADR-019](#adr-019) | Mecanismo de concurrencia para la cola de reservas | 07/09/2026 | Propuesto |
| [ADR-020](#adr-020) | Elección del tipo de motor para la búsqueda del catálogo: Relacional vs no relacional | 07/09/2026 | Propuesto |
| [ADR-021](#adr-021) | Elección del motor de búsqueda no relacional | 07/09/2026 | Aceptado |
| [ADR-022](#adr-022) | Mantener actualizada la búsqueda del catálogo (sincronización con el motor de búsqueda) | 12/09/2026 | Propuesto |
| [ADR-023](#adr-023) | Selección de la tecnología de la caché | 12/09/2026 | Propuesto |
| [ADR-024](#adr-024) | Máquina donde se ejecutan el motor de búsqueda y la caché | 20/09/2026 | Propuesto |
| [ADR-025](#adr-025) | Autenticación y control de acceso: propio o externo | 05/09/2026 | Propuesto |
| [ADR-026](#adr-026) | Elección de proveedor de autenticación (IDP) | 07/09/2026 | Propuesto |
| [ADR-027](#adr-027) | Estrategia de autorización y control de acceso basado en roles | 07/09/2026 | Propuesto |
| [ADR-028](#adr-028) | Registro del rol y la institución de cada usuario | 07/09/2026 | Propuesto |
| [ADR-029](#adr-029) | Manejo de la sesión del usuario en el navegador | 19/09/2026 | Propuesto |
| [ADR-030](#adr-030) | Selección de la puerta de entrada de las solicitudes (API Gateway) | 09/09/2026 | Propuesto |
| [ADR-031](#adr-031) | Política de limitación de intentos | 17/09/2026 | Propuesto |
| [ADR-032](#adr-032) | Selección del filtro de protección del tráfico de entrada (WAF) | 22/09/2026 | Propuesto |
| [ADR-033](#adr-033) | Entrega de la aplicación web al navegador | 20/09/2026 | Propuesto |
| [ADR-034](#adr-034) | Selección de la tecnología de cola de mensajes (message broker) | 06/09/2026 | Propuesto |
| [ADR-035](#adr-035) | Selección de la tecnología de tareas programadas (Worker) | 12/09/2026 | Propuesto |
| [ADR-036](#adr-036) | Componente de notificaciones (Notification gateway) | 05/09/2026 | Aceptado |
| [ADR-037](#adr-037) | Selección del servicio de envío de correos electrónicos | 04/09/2026 | Propuesto |
| [ADR-038](#adr-038) | Selección de la pasarela de pagos | 05/09/2026 | Propuesto |
| [ADR-039](#adr-039) | Evitar pagos y avisos duplicados (Idempotencia) | 12/09/2026 | Aceptado |
| [ADR-040](#adr-040) | Comportamiento del sistema ante fallas de los servicios externos (Circuit breaker) | 19/09/2026 | Aceptado |
| [ADR-041](#adr-041) | Entrega de los archivos que el sistema genera | 20/09/2026 | Propuesto |
| [ADR-042](#adr-042) | Selección de la herramienta de integración y despliegue continuo | 12/09/2026 | Aceptado |
| [ADR-043](#adr-043) | Almacenamiento de las claves de acceso a servicios (Key Vault) | 20/09/2026 | Propuesto |
| [ADR-044](#adr-044) | Selección de la herramienta de monitoreo del sistema (Monitoreo / instrumentación) | 18/09/2026 | Propuesto |
| [ADR-045](#adr-045) | Inmutabilidad del historial transaccional y de los registros de auditoría de seguridad | 06/09/2026 | Propuesto |
| [ADR-046](#adr-046) | Registro y trazabilidad de las operaciones de negocio | 20/09/2026 | Propuesto |
| [ADR-047](#adr-047) | Registro de auditoría y trazabilidad de eventos, elección entre tecnología propia o servicio externo | 05/09/2026 | Propuesto |
| [ADR-048](#adr-048) | Selección de tecnología de auditoría y logs de eventos | 12/09/2026 | Propuesto |
| [ADR-049](#adr-049) | Herramienta de pruebas unitarias del backend | 06/09/2026 | Propuesto |
| [ADR-050](#adr-050) | Entrega de los datos de una institución que cancela su membresía | 20/09/2026 | Propuesto |
| [ADR-051](#adr-051) | Selección del estilo arquitectónico del backend | 25/09/2026 | Propuesto |
| [ADR-052](#adr-052) | Estilo de comunicación usado | 25/09/2026 | Rechazado |
| [ADR-053](#adr-053) | Manejo del crecimiento de las tablas de historial | 27/09/2026 | Propuesto |
| [ADR-054](#adr-054) | Capacidad de la base de datos cuando un servidor no alcance | 27/09/2026 | Propuesto |
| [ADR-055](#adr-055) | Dónde se guardan las imágenes del sistema | 27/09/2026 | Aceptado |
| [ADR-056](#adr-056) | Seguimiento de una solicitud entre las piezas del sistema | 27/09/2026 | Propuesto |
| [ADR-057](#adr-057) | Eliminación de los datos personales de un usuario | 27/09/2026 | Aceptado |
| [ADR-058](#adr-058) | Organización de las reglas del préstamo, la devolución y la multa | 27/09/2026 | Propuesto |
| [ADR-059](#adr-059) | Atención de registros masivos en el proveedor de seguridad | 29/09/2026 | Propuesto |
| [ADR-060](#adr-060) | Orden de envío de los avisos cuando se alcanza el límite diario de correos | 29/09/2026 | Propuesto |
| [ADR-061](#adr-061) | Crecimiento de la máquina del motor de busqueda del caché | 29/09/2026 | Propuesto |
| [ADR-062](#adr-062) | Ejecución de las tareas programadas cuando crece el numero de instituciones | 29/09/2026 | Propuesto |
---

<a id="adr-001"></a>
## ADR-001 · Selección de la plataforma de nube para el ambiente de despliegue

**Fecha:** 16/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-15, US-16, US-26, US-31, RT-17, RN-07, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Es necesario definir sobre qué infraestructura en la nube va a correr el sistema, porque esa solución condiciona muchas otras decisiones de arquitectura. El ambiente de despliegue debe satisfacer simultáneamente algunas exigencias del sistema: disponibilidad con recuperación automática, rendimiento bajo concurrencia alta de usuarios, restricciones de arquitectura y despliegue sin interrupción del servicio.

### Alternativas consideradas

**1.** Google Cloud Platform (Cloud Run + Cloud SQL). Google mantiene los equipos y la plataforma crece sola cuando entran más personas. Las mejoras se entregan poco a poco, sin sacar a nadie del sistema. La información se guarda duplicada en dos lugares y cambia sola si uno falla; el correo y el control del tráfico se resuelven con proveedores externos. Desde USD 11 al mes, con USD 300 de regalo por 90 días.

**2.** Microsoft Azure (Container Apps + PostgreSQL Flexible Server). Microsoft mantiene los equipos, la plataforma crece sola y las mejoras se entregan poco a poco, sin sacar a nadie del sistema. La información se guarda duplicada en dos lugares y cambia sola si uno falla. Incluye la función que organiza las conexiones cuando hay mucha gente conectada, disponible solo en los planes de mayor capacidad. Desde USD 15 al mes, con USD 100 de regalo por un año para estudiantes.

**3.** Amazon Web Services (ECS Fargate + RDS). Amazon mantiene los equipos y la plataforma crece sola cuando entran más personas. Entregar las mejoras poco a poco se arma combinando varios servicios, en lugar de venir listo en uno solo. La información se guarda duplicada en dos lugares y cambia sola si uno falla. Ofrece aparte el organizador de conexiones, el envío de correos y el control del tráfico. Desde USD 12 al mes, con USD 200 de regalo por 6 meses.

### Decisión

Se elige Google Cloud Platform por cuatro razones:

1. Entregar mejoras sin sacar a nadie del sistema viene incluido y no hay que construir nada para lograrlo. En las otras dos opciones hay que armarlo combinando varios servicios, trabajo que recae sobre el equipo.

2. Si la base de datos falla, el sistema cambia solo a la copia que está en otro lugar, en menos de un minuto.

3. Cuesta cerca de la mitad: unos USD 60 al mes, contra unos USD 100 de la opción de Amazon. La diferencia está en dos piezas que Google incluye y Amazon cobra por separado: la que recibe y reparte las solicitudes de los usuarios, y la que evita que la base de datos se sature cuando hay mucha gente conectada al tiempo. Al año son cerca de $2.700.000 COP, menos de la mitad del presupuesto definido en el negocio.

4. El regalo inicial de USD 300 es el más alto de los tres y tiene un buen alcance funcional que cubre lo que se necesita.

No se eligió Amazon, aunque trae de fábrica el envío de correos y las dos piezas mencionadas, porque las cobra aparte y el total sube al doble. El equipo puede resolverlas por fuera sin incumplir ningún compromiso del sistema. No se eligió Azure porque la pieza que evita que la base de datos se sature solo viene en sus planes grandes, y con ese plan el costo del año supera el presupuesto definido en el negocio.

### Consecuencias

**Positivas**

1. Se puede publicar una versión nueva del sistema sin sacarlo de servicio, y viene incluido: el equipo no tiene que construir nada.
2. Si la base de datos principal falla, el sistema cambia solo a la copia que está en otro lugar.
3. Cuesta cerca de la mitad de la alternativa más cercana y, en un año, consume menos de la mitad del presupuesto definido en el negocio.

**Negativas**

1. Mantener una copia siempre encendida cuesta unos 320.000 pesos al año, se use o no el sistema, lo cual es un 4% del presupuesto de infraestructura.
2. Los USD 300 de regalo vencen a los 90 días, así que hay que dejar configuradas alertas de gasto antes de que empiece el cobro real.

---

<a id="adr-002"></a>
## ADR-002 · Entorno de ejecución del backend

**Fecha:** 18/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-15, US-26, US-31, RT-18, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La plataforma de nube ya está definida. Falta decidir en cuál de sus servicios queda funcionando el backend, es decir, dónde se ejecuta y atiende las solicitudes todos los días. El servicio elegido debe permitir publicar versiones nuevas sin interrumpir a los usuarios y aumentar o reducir su capacidad de atención según cuánta gente esté usando el sistema.

### Alternativas consideradas

**A.** Cloud Run. Servicio de Google que recibe el backend ya empaquetado y lo ejecuta. Ajusta su capacidad por sí solo según las solicitudes que van llegando, y deja de consumir recursos cuando nadie lo está usando. Se paga por el tiempo en que está atendiendo.

**B.** App Engine. Servicio de Google al que se le entrega el código: Google lo prepara, lo ejecuta y ajusta su capacidad. Funciona con los lenguajes y las versiones que la plataforma tiene disponibles.

**C.** GKE Autopilot. Servicio de Google basado en Kubernetes, una herramienta para administrar sistemas repartidos en varios servidores. Google se encarga de los servidores, y el equipo define en archivos de configuración cómo debe ejecutarse el backend.

### Decisión

Se elige la opción A, Cloud Run. Las tres son administradas por Google y equivalentes en seguridad y cumplimiento; la diferencia está en el costo y en el trabajo que le queda al equipo.

Cloud Run recibe el mismo paquete definido en la decisión de empaquetado, y lo ejecuta sin atarlo a los lenguajes ni a las versiones del proveedor, así que el backend podría llevarse a otro. Ajusta su capacidad por sí solo según la demanda, manteniendo siempre una copia encendida para responder sin demora, lo que ayuda a no exceder el presupuesto fijo del proyecto. Además publica cada versión nueva trasladándole el tráfico sin cortar el servicio, y permite volver a la anterior de inmediato si algo sale mal.

Se descarta B porque solo ejecuta los lenguajes y versiones que la plataforma tiene disponibles, y su versión flexible mantiene el sistema encendido aunque nadie lo esté usando, lo que genera un costo permanente.

Se descarta C porque, aunque da más control sobre cómo se ejecuta el backend, obliga a aprender y mantener la configuración de Kubernetes, un trabajo que con un equipo reducido no se justifica.

### Consecuencias

**Positivas**

1. El equipo no instala, actualiza ni vigila servidores.
2. La capacidad se ajusta sola a la demanda, y solo se paga fija la copia que se mantiene encendida.
3. Cada versión nueva se publica sin cortar el servicio, y se puede volver a la anterior de inmediato.
4. El backend se reparte entre varias zonas de la región, así que la falla de una no detiene el sistema.

**Negativas**

1. Cuando no llegan solicitudes, el backend se apaga, así que las tareas programadas no pueden vivir dentro de él: deben dispararse desde afuera.
2. Cada instalación nueva abre sus propias conexiones a la base de datos, y con varias funcionando al tiempo, la base podría saturarse.
3. Cada solicitud tiene un tiempo máximo de ejecución, así que una tarea muy larga debe partirse en pedazos o ejecutarse aparte.

---

<a id="adr-003"></a>
## ADR-003 · Formato de empaquetado del backend

**Fecha:** 18/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-13, RT-18  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Ya está decidido dónde va a funcionar el backend. Falta decidir qué se le entrega a ese servicio para que lo ejecute: si el código tal cual, o un paquete que ya trae adentro todo lo que el backend necesita para funcionar. Lo que se elija debe lograr que el backend se comporte igual en el computador de cada desarrollador, en las pruebas y en producción, y que se pueda mover a otro proveedor sin tener que rehacerlo. La interfaz y el API Gateway no entran en esta decisión: la interfaz son archivos que el navegador descarga, y el API Gateway ya viene listo en la plataforma.

### Alternativas consideradas

**A.** Entregar el código directamente. Se le entrega a la plataforma el código del backend, y ella se encarga de prepararlo y ejecutarlo con las herramientas y versiones que tiene disponibles.

**B.** Contenedores. El backend se entrega dentro de un paquete que ya trae adentro todo lo que necesita: su código, sus librerías y la versión del lenguaje. Ese paquete se ejecuta igual en cualquier lugar que acepte este formato.

**C.** Funciones independientes. El backend se parte en funciones pequeñas y se entrega cada una por separado; la plataforma ejecuta solo la que se llame en cada momento.

### Decisión

Se elige la opción B, contenedores. El paquete lleva dentro el código, las librerías y la versión del lenguaje, así que el backend se comporta igual en el computador de cada desarrollador, en las pruebas y en producción, y el mismo paquete se puede mover a otro proveedor sin rehacerlo. Se descarta A porque la plataforma prepara el código con sus propias herramientas y versiones, así que cambiar de proveedor obligaría a rehacer toda la entrega. Finalmente, se descarta C porque, al partir el backend en funciones sueltas, una operación que debe ocurrir completa, como en nuestro caso prestar un ejemplar y descontarlo del inventario, puede quedar a medias.

### Consecuencias

**Positivas**

1. Se evitan los errores que aparecen en un computador y en otro no, porque todos ejecutan el mismo paquete.
2. El sistema puede cambiar de proveedor sin rehacer la entrega.
3. El backend se publica sin depender de la interfaz.
4. El paquete no guarda nada adentro, así que se puede borrar y volver a crear en cualquier momento sin perder información.

**Negativas**

1. Un paquete mal armado queda pesado y demora el arranque de cada copia nueva.
2. Las claves y contraseñas no pueden ir dentro del paquete: se leen aparte cuando el backend se ejecuta.

---

<a id="adr-004"></a>
## ADR-004 · Patrón arquitectónico interno del backend

**Fecha:** 18/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-22, RF-15, RT-11, RT-19  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El corazón de BookHive son sus reglas de negocio: son las que más se revisan con el cliente, las que hay que explicar y probar, y las que cada institución puede ajustar. Falta decidir cómo se organiza el backend por dentro para que esas reglas queden en un lugar claro.

### Alternativas consideradas

**A.** En capas. El código se divide en tres capas: la que recibe las solicitudes, la de las reglas del negocio y la de acceso a los datos. Las reglas del negocio trabajan directamente contra cada herramienta.

**B.** Hexagonal. Las reglas del negocio quedan en el centro, separadas de lo técnico: la base de datos, el proveedor de identidad, la pasarela de pagos y el buscador. Lo técnico se conecta a las reglas por un enchufe definido, y no al revés.

### Decisión

Se elige la opción B, hexagonal. Las reglas del negocio, como cuántos días dura un préstamo o cómo se calcula una multa, no dependen de ninguna herramienta. Así se pueden cambiar las herramientas sin tocar las reglas, y probar las reglas sin encender las herramientas.

Se descarta A porque las reglas quedan mezcladas con las herramientas, y cambiar una obliga a meterse en la otra.

### Consecuencias

**Positivas**

1. Cada regla está en un solo lugar, fácil de encontrar y de explicar.
2. Todas las partes del backend se ordenan igual, así que quien entiende una entiende las demás.
3. Si algún día una parte se separa del backend, sus reglas se llevan tal cual.

**Negativas**

1. Al comienzo cuesta más ubicar dónde va cada cosa, porque las reglas y las herramientas viven en lugares distintos.
2. Si el equipo no respeta la separación, las herramientas terminan dentro de las reglas y se pierde la ventaja.

---

<a id="adr-005"></a>
## ADR-005 · Modelo de programación del sistema: Reactivo, no reactivo o mixto

**Fecha:** 17/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-15, US-24, US-26, RT-16  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Falta definir con qué modelo de programación se va a construir BookHive. La decisión está tensionada por dos requisitos ya definidos: por un lado el sistema debe responder rápido aunque muchas personas lo usen al tiempo, seguir funcionando aunque una de sus partes falle, soportar el crecimiento a medida que entran más instituciones, y permitir que sus componentes se avisen entre sí sin quedarse esperando respuesta. Además, toda acción debe quedar registrada al instante para efectos de trazabilidad.

### Alternativas consideradas

**A.** Reactivo. El sistema nunca se queda detenido esperando una respuesta: mientras una solicitud está en curso, sigue recibiendo otras. Todo el sistema, consultas, préstamos, reservas, multas y pagos se construye de esta forma.

**B.** No reactivo. El sistema atiende cada solicitud completa, de principio a fin, antes de pasar a la siguiente. Cuando entran más usuarios, se ponen a funcionar más servidores del sistema en paralelo. Todo el sistema se construye de esta forma

**C.** Mixto. Las consultas del catálogo, los avisos y las notificaciones se construyen de forma reactiva; los préstamos, reservas, multas y pagos se construyen de forma no reactiva.

### Decisión

Se elige la opción C, mixto.

Las consultas del catálogo y las notificaciones se construyen de forma reactiva, porque ahí el sistema pasa la mayor parte del tiempo esperando una respuesta de la base de datos, del buscador o del correo, y mientras espera puede ir atendiendo a otros. Los préstamos, las reservas, las multas y los pagos se construyen de forma no reactiva, porque un ejemplar solo se lo puede llevar una persona y una multa solo se paga una vez, así que el sistema debe confirmar cada operación antes de seguir con la siguiente.

### Consecuencias

**Positivas**

1. El sistema responde rápido en las consultas y mantiene seguras las operaciones de préstamo.
2. El corazón del negocio queda simple de entender, probar y corregir.
3. Se atiende a más usuarios sin prender más servidores.

**Negativas**

1. El equipo debe manejar dos formas de programar en un mismo sistema.
2. Si una operación de préstamo queda del lado reactivo por descuido, frena todo el sistema.

---

<a id="adr-006"></a>
## ADR-006 · Selección de la tecnología del backend

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-01, RT-11, RT-16, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Se necesita elegir el lenguaje y la herramienta para el backend, teniendo en cuenta que el sistema debe soportar operaciones que no pueden quedar a medias; que una parte se construye de forma reactiva, otra no y que el presupuesto es fijo.

### Alternativas consideradas


**A.** Java con Spring Boot. Herramienta muy usada para construir el lado del servidor. Trae incluidos el manejo de las solicitudes, el acceso a la base de datos con control de operaciones completas, la revisión del pase de entrada y la conexión con servicios externos. Permite escribir una parte del sistema de forma reactiva y otra no, dentro del mismo proyecto. Sin costo de licencia.

**B.** C# con .NET. Herramienta equivalente, de Microsoft, también sin costo de licencia. Trae incluido lo mismo, permite las dos formas de programar y se empaqueta igual en un contenedor.

**C.** TypeScript con NestJS, sobre Node. Usa el mismo lenguaje que la aplicación web, así que el equipo trabajaría con uno solo. Todo lo que hace es no bloqueante por naturaleza: el servidor nunca se queda detenido esperando una respuesta.

**D.** Python con Django o FastAPI. Herramientas muy usadas y rápidas de escribir. Django trae mucho incluido; FastAPI es más liviano y deja que cada equipo escoja el resto.

### Decisión

Se elige la opción A, Java con Spring Boot.

Las cuatro sirven para construir el sistema y ninguna cobra licencia, así que el precio no decide. Lo que decide son tres condiciones que el proyecto ya tiene puestas: que se pueda escribir una parte del sistema de forma reactiva y otra no dentro del mismo proyecto; que cada dato y cada pieza lleven escrito qué son, de modo que la herramienta avise del error antes de que el programa se ejecute y no cuando una operación ya va por la mitad; y que el equipo ya sepa usarla, porque son pocas personas y el plazo es corto. La opción A es la única que reúne las tres.

Se descarta B porque cumple las dos primeras condiciones igual que A, pero el equipo nunca la ha usado, y lo más delicado del sistema se construiría aprendiendo sobre la marcha.

Se descarta C porque ahí todo funciona de una sola forma, la no bloqueante. Las operaciones que conviene escribir de manera sencilla y directa tendrían que escribirse igual que las demás, y eso va en contra de la mezcla que ya se decidió.

Se descarta D porque escribir qué es cada dato es opcional y la herramienta no lo revisa antes de ejecutar, así que los patrones de diseño que el proyecto exige quedarían sostenidos por la convención del equipo y no por la herramienta.

### Consecuencias

**Positivas**

1. El equipo ya conoce la herramienta, así que no se invierte tiempo aprendiéndola.
2. Los errores de tipo aparecen antes de ejecutar y no cuando el usuario ya está usando el sistema.
3. La parte reactiva y la que no lo es conviven en un mismo proyecto, sin partir el sistema en dos.
4. Es una herramienta muy usada, así que cuando algo falle es fácil encontrar cómo resolverlo debido a la lata documentación que se tiene.

**Negativas**

1. El programa ocupa más memoria y tarda más en arrancar que las otras opciones, lo que encarece tenerlo encendido.
2. El equipo trabaja con dos lenguajes: uno en la parte visual y otro en el servidor.
3. Dentro del mismo proyecto conviven dos formas de escribir, y hay que cuidar que no se mezclen sin darse cuenta.

---

<a id="adr-007"></a>
## ADR-007 · Estilo de comunicación de la API de BookHive

**Fecha:** 17/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RF-14, RF-19, RT-05, RT-09  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El frontend se comunica con el backend a través de la API, y la pasarela de pagos le avisa al sistema por esa misma vía cuando confirma un pago. Falta definir el estilo de esa comunicación: cómo el frontend pide y envía la información, y si el sistema necesita avisarle algo al usuario en el momento justo en que ocurre.

### Alternativas consideradas

**A.** REST. Cada elemento del negocio, préstamos, reservas, multas, tiene su propia dirección y el frontend la consulta o la modifica. Cada solicitud recibe una respuesta completa.

**B.** GraphQL. Existe una única dirección de entrada y, en cada consulta, el frontend describe exactamente qué datos necesita. El servidor responde solo con esos datos.

**C.** WebSockets. Una conexión que permanece abierta, por la que el frontend y el servidor pueden enviarse información en cualquier momento.

**D.** Consulta periódica. No se mantiene ninguna conexión abierta: el frontend le pregunta al servidor cada cierto tiempo si hubo alguna novedad.

### Decisión

Se elige REST para las solicitudes y la consulta periódica para los avisos. Con REST cada operación tiene su propia dirección, así que se puede controlar por separado quién puede usarla y cuántas veces. Es además la forma en que la pasarela de pagos le avisa al sistema. GraphQL se descarta porque todo entra por una sola dirección y ese control se vuelve más difícil. WebSockets se descarta porque mantener una conexión abierta solo vale la pena si hay avisos constantes. En BookHive los demás avisos llegan por correo o se muestran cuando el usuario entra a la aplicación; el único que no puede esperar es la confirmación del pago, y se resuelve preguntando unas pocas veces si ya se confirmó.

### Consecuencias

**Positivas**

1. Se controla por separado quién puede usar cada operación y cuántas veces.
2. Todo lo que entra al sistema usa el mismo estilo, incluidos los avisos de la pasarela de pagos.
3. Es el estilo más conocido, lo que facilita construirlo y mantenerlo.

**Negativas**

1. Cada pantalla que reúne datos de varias partes necesita una dirección propia en el backend, que hay que construir.
2. Los avisos no llegan al instante: la pantalla del pago debe preguntar varias veces si ya se confirmó.
3. Si más adelante el sistema necesita avisar en el momento, habrá que agregar otro mecanismo.

---

<a id="adr-008"></a>
## ADR-008 · Selección del estilo arquitectónico de la aplicación web

**Fecha:** 19/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-10, RT-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La aplicación web es lo que usan el estudiante, el docente, el bibliotecario y el administrador, y reúne pantallas de partes muy distintas del negocio. Con el tiempo entran pantallas nuevas y cambian las existentes. Falta decidir cómo se organiza por dentro para que agregar o cambiar una parte no obligue a recorrer toda la aplicación.

### Alternativas consideradas

**A.** Por tipo de archivo. Todo se agrupa según lo que es: las pantallas por un lado, las llamadas al backend por otro, los modelos por otro. Una misma funcionalidad queda repartida entre esos grupos.

**B.** Por funcionalidades del negocio. Cada parte del negocio agrupa lo suyo en un solo lugar: sus pantallas, sus llamadas al backend y sus reglas de presentación. Se publica como una sola aplicación.

**C.** Micro frontends. Cada parte del negocio es una aplicación independiente, que se construye y se publica por su cuenta, y se integran todas en el navegador.

### Decisión

Se elige la opción B, organizar por funcionalidades del negocio.

Cada parte del negocio: préstamos, reservas, multas, catálogo, guarda en un solo lugar sus pantallas, sus llamadas al backend y sus reglas de presentación, con los mismos nombres que usa el backend. Así, cambiar algo de préstamos no obliga a buscar en tres carpetas distintas. Y como se publica una sola aplicación, todos los usuarios quedan siempre en la misma versión.

Se descarta A porque cada cambio del negocio obligaría a tocar varios grupos separados, y con el tiempo se vuelve difícil saber qué pertenece a qué.

Se descarta C porque está pensado para varios equipos trabajando en paralelo sobre la misma interfaz: sumaría publicar y coordinar varias aplicaciones y mantener el mismo aspecto entre todas, sin resolver ningún problema que el proyecto tenga hoy

### Consecuencias

**Positivas**

1. Agregar o cambiar una parte del negocio se hace en un solo lugar de la aplicación.
2. Las pantallas usan los mismos nombres que el backend, así que es fácil ubicar qué corresponde a qué.
3. Se publica una sola aplicación, así que todos los usuarios quedan siempre en la misma versión.

**Negativas**

1. Al publicarse todo junto, un cambio pequeño obliga a volver a publicar la aplicación completa.
2. Hay que mantener la disciplina de que cada pantalla llame al backend solo desde el lugar previsto de su funcionalidad.

---

<a id="adr-009"></a>
## ADR-009 · Selección de la tecnología de la aplicación web

**Fecha:** 19/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-02, US-03, US-28, US-29, US-30, RT-10  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La aplicación web es lo que usan las personas, y está compuesta sobre todo por formularios y tablas, donde cada pantalla depende del rol de quien entra. Falta decidir con qué tecnología se construye.

### Alternativas consideradas

**A.** Angular. Herramienta de Google para construir aplicaciones web. Trae incluidos la navegación entre pantallas, los formularios con validación, las llamadas al backend y el control de acceso por rol, define la estructura del proyecto y tiene su propia biblioteca de componentes visuales.

**B.** React. Biblioteca de Meta para construir pantallas. Trae el dibujo de la pantalla; la navegación, los formularios, las llamadas al backend y los componentes visuales se escogen entre piezas externas, y la estructura del proyecto la define cada equipo. Es la de comunidad más grande.

**C.** Next.js. Herramienta construida sobre React, que además arma las páginas en un servidor antes de enviarlas al navegador. Trae la navegación entre pantallas y su propia forma de escribir operaciones de servidor.

### Decisión

Se elige la opción A, Angular.

Trae incluido lo que el sistema necesita: navegación entre pantallas, formularios con validación, llamadas al backend, control de acceso por pantalla y componentes visuales, así que no hay que escoger ni mantener piezas externas. Además, impone la estructura del proyecto, lo que sostiene la organización por funcionalidades que ya se decidió. Y la aplicación se entrega como archivos que el navegador descarga, sin ningún programa encendido que la atienda.

Se descarta B porque cada pieza se escoge y se mantiene por separado, cada una con su propio ritmo de cambios.

Se descarta C porque su principal aporte es armar las páginas en el servidor, algo que BookHive no necesita al estar casi toda la aplicación detrás del inicio de sesión. A cambio, sumaría un programa encendido y su forma de escribir operaciones de servidor abriría la puerta a que parte de las reglas del negocio terminaran fuera del backend.

### Consecuencias

**Positivas**

1. Lo que el sistema necesita viene incluido: no hay piezas externas que escoger ni mantener.
2. La herramienta impone la estructura, así que la organización por funcionalidades no depende de la disciplina del equipo.
3. Los componentes visuales vienen hechos y el equipo no los construye.
4. El control de acceso por pantalla usa el mismo rol que comprueba el IDP.

**Negativas**

1. Es la que más cuesta aprender, así que al comienzo se avanza más lento.
2. La primera descarga pesa más que las otras dos, así que hay que dividirla por pantallas.
3. Publica versiones nuevas dos veces al año, y mantenerse al día exige seguirle el ritmo.
4. Ocultar una pantalla según el rol no reemplaza la comprobación del backend: siguen siendo dos revisiones distintas.

---

<a id="adr-010"></a>
## ADR-010 · Elección del tipo de motor de base de datos: Relacional vs no relacional

**Fecha:** 04/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** RF-15, RT-01, RT-03, RT-06  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La información de BookHive está fuertemente relacionada entre sí: cada préstamo pertenece a un usuario y a un ejemplar, cada multa nace de un préstamo y cada pago salda una multa. Esas relaciones no se pueden romper nunca, y los datos guardados deben tener siempre el formato correcto. Falta decidir qué tipo de base de datos se usará, teniendo en cuenta cuánto de ese control puede hacer la base por sí misma y cuánto quedaría en manos del diseño.

### Alternativas consideradas

**A.** Base de datos relacional. La información se guarda en tablas con una estructura fija: cada dato tiene un tipo definido y las relaciones entre registros se declaran dentro de la misma base. Es la base la que aplica esas definiciones cada vez que se guarda, se modifica o se borra algo. Una operación que toca varias tablas a la vez se aplica completa o no se aplica.

**B.** Base de datos no relacional. Cada registro se guarda con la forma que necesite, sin una estructura común. El formato de los datos y las relaciones entre registros se definen y se aplican en la aplicación. El crecimiento se maneja sumando servidores.

### Decisión

Se elige la opción A, base de datos relacional.

Las reglas que no pueden fallar, que una multa siempre tenga su préstamo, que un dato guardado tenga el formato correcto, quedan escritas dentro de la base, y es ella la que las hace cumplir cada vez que se guarda, se modifica o se borra algo. Si el código tiene un error, la base rechaza la operación en lugar de dejar el dato dañado; de esta forma, se tiene una capa de orden y seguridad en la base de datos, y no recae todo este trabajo sobre el diseño únicamente.

Se descarta B porque dejaría esas mismas reglas en manos de la aplicación únicamente: un error de programación bastaría para guardar una multa sin su préstamo, y el sistema no tendría cómo detectarlo. Su ventaja de crecer sumando servidores tampoco compensa, porque BookHive maneja el volumen de instituciones por esquema, de forma que el volumen de datos no se salga de control.

### Consecuencias

**Positivas**

1. Los datos inválidos no entran: la base rechaza lo que no cumpla el formato definido.
2. Las relaciones entre registros las garantiza la base como capa adicional, además de la capa del diseño.
3. Cuando una operación toca varias tablas, se aplican todos los cambios o ninguno.
4. Cada dato vive en un solo lugar: si se corrige, queda corregido en todas partes al tiempo.

**Negativas**

1. Definir la estructura y las relaciones sigue siendo trabajo del diseño; la base solo garantiza que se respete lo que se haya definido.
2. Agregar algo que no se planeó desde el inicio exige un cambio de estructura pensado con cuidado, para no afectar lo que ya existe.
3. Las tablas están conectadas entre sí, así que modificar una obliga a revisar el efecto en las demás.

---

<a id="adr-011"></a>
## ADR-011 · Estrategia de aislamiento de datos por institución a nivel de base de datos

**Fecha:** 06/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-04, RF-05, RT-02, RN-01, RN-03  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive es una sola aplicación compartida por varias universidades, cada una con una o varias sedes. La información de una nunca puede quedar al alcance de otra, y esa garantía no puede depender solo de que el diseño esté bien escrito: tiene que sostenerse también en la base de datos. Falta decidir cómo se separan esos datos.

### Alternativas consideradas

**A.** Base de datos independiente por institución. Cada institución tiene su propia base de datos completa, separada de las demás.

**B.** Esquema independiente por institución. Todas las instituciones comparten una misma base de datos, y cada una tiene su propio espacio separado dentro de ella.

**C.** Tablas compartidas con identificador de institución. Todas las instituciones comparten las mismas tablas, y cada registro indica a cuál pertenece, cada consulta debe filtrar por ese identificador.

### Decisión

Se elige la opción B, un espacio separado por institución dentro de una misma base de datos.

Además, cada institución tiene su propio usuario de base de datos, con permisos únicamente sobre su espacio. El espacio por sí solo no separa nada si la aplicación se conecta con un usuario dueño de toda la base: ahí lo único que separaría a las instituciones volvería a ser el código. Con un usuario por institución, una consulta mal escrita no alcanza los datos de otra, porque la conexión no tiene permiso para llegar ahí.

Se descarta A porque cada institución nueva sería una base de datos más que instalar, respaldar y vigilar, y ese trabajo crece con cada universidad que entra.

Se descarta C porque la separación quedaría escrita en cada consulta: basta que a una se le olvide el filtro para que los datos de una universidad aparezcan en la pantalla de otra. Además, al no estar separados, entregar o eliminar los datos de una institución que se va obliga a extraerlos de entre los de las demás.

### Consecuencias

**Positivas**

1. Una consulta mal escrita no alcanza los datos de otra institución: lo impide el motor, ademas del diseño, cada parte aporta un grado de seguridad.
2. Sumar una institución nueva es crear su espacio y su usuario, no montar infraestructura.
3. Al irse una institución, sus datos se entregan y se eliminan como una unidad.
4. Todas las instituciones comparten una sola base de datos, así que el costo no crece con cada una que entra.

**Negativas**

1. Medir cuántos datos ocupa cada institución obliga a recorrer los espacios uno por uno, y ese recorrido se alarga a medida que entran más.
2. Un usuario de base de datos por institución significa más conexiones abiertas al mismo tiempo.
3. Esto solo protege la base de datos: la caché y el registro de eventos guardan información de todas las instituciones y necesitan su propio mecanismo de separación.

---

<a id="adr-012"></a>
## ADR-012 · Elección del motor de base de datos relacional

**Fecha:** 04/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** RF-02, RF-05, RT-01, RT-02, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Se debe decidir con cuál motor. La elección debe cumplir tres condiciones: que la información de cada universidad no se mezcle con la de otra, que el motor ayude a sostener reglas que no pueden fallar, como que un mismo ejemplar no quede prestado dos veces al tiempo y que el costo no crezca a medida que se suman universidades o se acumula información.

### Alternativas consideradas

**A.** PostgreSQL. De código abierto: sin costo de licencia ni límite de tamaño. Permite guardar la información de cada universidad en espacios separados (esquemas) dentro de una misma base y puede rechazar por sí mismo un préstamo que se cruce con otro sobre el mismo ejemplar.

**B.** MySQL. De código abierto: sin costo de licencia ni límite de tamaño. No maneja espacios separados dentro de una base, así que aislar cada universidad exige crear una base completa para cada una. La verificación de préstamos cruzados queda a cargo de la aplicación.

**C.** SQL Server. Motor comercial de Microsoft. Permite espacios separados dentro de una misma base. Su versión gratuita limita cada base a 10 GB, y superar ese tope obliga a comprar licencia.

**D.** Oracle Database. Motor comercial. Permite espacios separados dentro de una misma base. Su versión gratuita limita el almacenamiento a 12 GB y no incluye todas las capacidades de la versión paga, que tiene costo de licencia.

### Decisión

Se elige PostgreSQL, ejecutado en Cloud SQL, el servicio de bases de datos administrado de la plataforma ya elegida. Es la única opción sin costo de licencia que guarda la información de cada universidad en espacios separados dentro de una misma base y que además puede impedir por sí misma que un ejemplar quede prestado dos veces en fechas que se cruzan.

Se descarta MySQL porque obliga a crear una base completa por universidad, y administrarlas sería un trabajo que crece con cada institución que entra.

Se descarta SQL Server porque su versión gratuita limita cada base a 10 GB, insuficiente para un sistema que nunca borra su historial, y sus versiones pagas exceden el presupuesto.

Se descarta Oracle porque cobra licencia y no está disponible como servicio administrado en la plataforma elegida.

### Consecuencias

**Positivas**

1. No tiene costo de licencia: sumar universidades solo aumenta el espacio usado.
2. Google se encarga de instalarlo, actualizarlo y respaldarlo.
3. Si la base se queda corta, se puede agrandar o agregarle copias de solo lectura para repartir las consultas, sin cambiar de motor ni tocar la aplicación.
4. Tiene documentación y comunidad amplias, fáciles de consultar.

**Negativas**

1. Con muchas conexiones abiertas al tiempo, PostgreSQL consume más memoria que otros motores, porque reserva un espacio propio para cada una.
2. Como cada universidad usa su propio usuario, habrá más conexiones abiertas simultáneamente, lo que agrava el punto anterior.

---

<a id="adr-013"></a>
## ADR-013 · Disponibilidad de la base de datos ante la falla del servidor

**Fecha:** 07/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-16, RT-22  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Si la base de datos deja de funcionar, BookHive queda fuera de servicio: no se pueden registrar préstamos ni devoluciones. Y aunque un estudiante alcance a pagar una multa, el cobro lo procesa la pasarela, que sigue funcionando por su cuenta; el sistema no puede registrar ese pago ni levantarle el bloqueo: la plata ya salió de su cuenta y para él nada cambió. Falta decidir cómo vuelve el servicio tras esa falla y en cuánto tiempo.

### Alternativas consideradas

**A.** Restaurar en una base de datos nueva. Cuando la base principal falla, se crea una nueva y se le carga la última copia de seguridad. El servicio vuelve cuando esa carga termina, y ese tiempo crece a medida que crece la información guardada.

**B.** Segunda base de datos en espera, con relevo manual. Una segunda base de datos, en otra máquina, se mantiene al día con todo lo que ocurre en la principal. Cuando la principal falla, alguien del equipo pone a la segunda a tomar su lugar.

**C.** Segunda base de datos con relevo automático. Igual que la anterior, pero es la propia plataforma la que detecta la falla y hace el cambio, sin que nadie intervenga.

### Decisión

Se elige la opción C: BookHive opera con dos bases de datos, una principal y una de respaldo que toma su lugar automáticamente si la principal falla. El servicio debe quedar restablecido en menos de quince minutos, sin perder operaciones ya confirmadas.

Los quince minutos salen del pago: la plata del estudiante ya salió de su cuenta, y mientras el sistema no vuelva, él sigue bloqueado. Para no perder operaciones, la base de respaldo confirma cada escritura junto con la principal.

Se descarta A porque el tiempo de carga crece con la información guardada, así que la meta se cumpliría hoy y dejaría de cumplirse con los años.

Se descarta B porque depende de que alguien del equipo esté disponible en el momento de la falla, y una caída de madrugada dejaría el sistema abajo hasta que alguien despierte.

### Consecuencias

**Positivas**

1. Si la base principal falla, el servicio vuelve sin que nadie intervenga, a cualquier hora.
2. No se pierde ninguna operación que ya se le haya confirmado al usuario.
3. Se pueden hacer mantenimientos de la base sin apagar el sistema.

**Negativas**

1. La base de respaldo es un espejo: si alguien borra o daña datos, el daño se copia de inmediato, y de eso solo protegen las copias de seguridad.
2. Cada escritura espera a que la base de respaldo la confirme, así que guardar tarda un poco más.

---

<a id="adr-014"></a>
## ADR-014 · Copias de seguridad de la información

**Fecha:** 07/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-16, RT-22  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La información de BookHive también se puede dañar sin que falle ningún servidor: alguien borra registros por equivocación, o un error del sistema deja datos inconsistentes. La base de respaldo no sirve para eso, porque copia el daño al instante; la única forma de volver atrás es tener guardada la información como estaba antes. Falta decidir cada cuánto se guarda esa copia, hasta qué momento se puede volver y cuánto tiempo se conservan las copias.

### Alternativas consideradas

**A.** Sin copias, solo con la base de respaldo. No se guarda ninguna copia aparte. La base de respaldo contiene en todo momento exactamente lo mismo que la principal.

**B.** Una copia completa cada día. Una vez al día se guarda un archivo con toda la información tal como está en ese momento. Se puede volver a cualquiera de esas copias diarias.

**C.** La copia diaria más el registro de cambios. Además de la copia diaria, se conserva el registro de cambios que la base de datos ya escribe por su cuenta para funcionar. Permite volver a cualquier minuto dentro del periodo conservado.

### Decisión


Se elige la opción C, la copia diaria más el registro de cambios. Permite volver al minuto anterior al error en lugar de perder el día completo, y conservar ese registro casi no ocupa espacio, porque la base de datos ya lo escribe para poder funcionar. En el servicio administrado, las dos cosas se activan con una casilla: no hay nada que construir.

Queda definido que la copia se genera automáticamente una vez al día, que se conservan los últimos 30 días , plazo que cubre el ciclo mensual de facturación, que las copias se guardan fuera de la región donde corre el sistema, y que periódicamente se verifica que una copia se pueda restaurar.

Se descarta A porque dejaría al sistema sin defensa ante un error humano, que es el riesgo más frecuente. Se descarta B porque un error a media tarde obligaría a volver a la copia de la madrugada, perdiendo el día completo de todas las universidades y no solo lo que se dañó.

### Consecuencias

**Positivas**

1. Ante un borrado o un daño por error, se puede volver al minuto anterior al problema, en lugar de perder el trabajo del día.
2. Conservar el registro de cambios casi no ocupa espacio, porque la base de datos ya lo escribe para funcionar.
3. Al guardarse fuera de la región, las copias siguen sirviendo aunque falle la región entera, lo cual es el caso que la base de respaldo no cubre.

**Negativas**

1. Volver atrás afecta a todas las universidades a la vez: si una borra algo por error, hay que restaurar en una base aparte y de ahí recuperar solo lo dañado, lo que toma tiempo y trabajo manual.
2. Los datos personales que se eliminen por solicitud de un usuario siguen existiendo dentro de las copias hasta que estas caduquen.
3. Las copias se guardan aparte y ese almacenamiento se paga, y crece con el tamaño de la información.

---

<a id="adr-015"></a>
## ADR-015 · Selección de la herramienta de migraciones de base de datos

**Fecha:** 12/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-26, US-31, RT-02, RT-13  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cada vez que cambia la estructura de la base de datos: agregar una columna, crear una tabla nueva, ese cambio hay que aplicarlo en el espacio de cada institución por separado, y hay que saber con certeza cuáles ya lo recibieron y cuáles no. Además, debe poder aplicarse mientras la versión anterior del sistema sigue atendiendo a usuarios, sin interrumpir el servicio. Falta decidir con qué herramienta se hace.

### Alternativas consideradas

**A.** Flyway. Herramienta que aplica los cambios de estructura escritos como archivos SQL numerados por versión. Lleva su propio registro, dentro del espacio de datos de cada institución, de qué cambios ya se aplicaron ahí. Gratis en su versión de código abierto.

**B.** Liquibase. Herramienta equivalente. Los cambios se pueden escribir en SQL o describirse en un formato propio (XML o YAML) que permite aplicar el mismo cambio sobre motores de base de datos distintos. También lleva registro de lo aplicado, y puede generar automáticamente la instrucción para deshacer un cambio. Gratis en su versión de código abierto.

**C.** Scripts SQL ejecutados a mano. El equipo escribe los cambios como archivos SQL y los ejecuta uno por uno. No requiere ninguna herramienta adicional.

### Decisión

Se elige la opción A, Flyway.

Los cambios se escriben en SQL, que el equipo ya maneja, y la herramienta anota dentro del espacio de cada institución cuáles ya se aplicaron ahí. Así nadie tiene que llevar esa cuenta a mano.

Se descarta Liquibase porque su ventaja principal: deshacer un cambio solo, sirve de poco: en la práctica, los errores se corrigen con un cambio nuevo.

Se descarta hacerlo a mano porque con varias instituciones es cuestión de tiempo que un cambio se repita en una o se olvide en otra.

### Consecuencias

**Positivas**

1. Queda registrado, dentro del espacio de cada institución, qué cambios ya se aplicaron ahí: nadie lleva esa cuenta a mano.
2. Los cambios se aplican antes de que la versión nueva empiece a atender, así que ninguna copia arranca sobre una estructura a medio actualizar.
3. Incorporar una institución nueva usa el mismo mecanismo, sin reiniciar nada.

**Negativas**

1. Si un cambio de estructura sale mal escrito, se aplica igual y hay que corregirlo con otro cambio encima: no se puede deshacer.
2. El cambio se aplica una vez por institución, así que entre más universidades haya, más tarda cada actualización.
3. Si ese recorrido falla a mitad de camino, unas instituciones quedan con la estructura nueva y otras con la vieja.

---

<a id="adr-016"></a>
## ADR-016 · Manejo de zona horaria en los registros del sistema

**Fecha:** 05/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RF-10, RT-23  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive puede tener instituciones ubicadas en distintos países, cada una con su propia zona horaria (por ejemplo, una institución en Colombia y otra en Brasil), e instituciones con sedes en diferentes localizaciones. Se necesita definir en qué zona horaria se guardan y calculan las fechas y horas del sistema, de modo que cada institución vea sus plazos y multas calculados en su propia hora local, sin que el resultado dependa de dónde esté alojado el servidor ni de la hora del dispositivo del usuario.

### Alternativas consideradas

**A.** Todo en la hora local del servidor. Las fechas se guardan y se muestran tal como las ve el servidor, sin conversiones.

**B.** Todo en una hora de referencia única (UTC), convertida a la hora de cada institución al mostrarla y al calcular. El sistema guarda siempre en esa misma escala, y cada institución tiene configurada su propia zona.

**C.** Cada institución guarda sus fechas ya convertidas a su propia zona, sin una escala común entre ellas.

### Decisión

Se elige la opción B, guardar todo en una hora de referencia única y convertirla a la hora de cada institución al mostrarla y al calcular.

Así, el significado de una fecha no depende de dónde esté alojado el servidor ni de la hora del dispositivo de quien entra, y comparar dos fechas nunca es ambiguo porque todas están en la misma escala.

Se descarta A porque un cambio de proveedor o de región desplazaría el significado de las fechas ya guardadas, sin forma de corregirlo después.

Se descarta C porque, sin una escala común, comparar registros entre instituciones deja de ser directo, y un error de configuración podría generar cobros equivocados sin que nadie lo note.

### Consecuencias

**Positivas**

1. El cálculo de la multa no depende de dónde esté alojado el servidor: si se cambia de proveedor o de región, las fechas ya guardadas siguen significando exactamente lo mismo.
2. Comparar dos fechas nunca es ambiguo, porque todas están en la misma escala interna; no hay que averiguar en qué zona se guardó cada registro.
3. Si se suma una institución en un país con otra zona horaria, ya está resuelto sin rediseñar nada: solo se configura su zona y las conversiones funcionan igual.

**Negativas**

1. La conversión a la hora local la hace el código. Si en algún punto se omite, el usuario ve una hora equivocada aunque el dato guardado sea correcto: el error queda en la presentación y no en la información, lo que lo hace fácil de pasar por alto.
2. La zona horaria de cada institución hay que configurarla bien al incorporarla. Si queda mal, todos los plazos y multas de esa institución se calculan desplazados, y el error solo se nota cuando alguien reclama.

---

<a id="adr-017"></a>
## ADR-017 · Catálogo de parámetros de negocio (Parameter catalog)

**Fecha:** 18/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-22, RF-09, RF-21, RN-10  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cada institución opera con sus propias reglas: cuántos días dura máximo un préstamo, cuánto cuesta cada día de retraso, cuántas veces se puede renovar. Esas reglas cambian con el tiempo, y el administrador de cada institución debe poder ajustarlas sin esperar una versión nueva del sistema. Falta decidir dónde se guardan esos valores y cómo se cambian de forma segura

### Alternativas consideradas

**A.** En la configuración del sistema. Los valores se escriben junto con el código a la hora de llevar a cabo el diseño o en la configuración del servicio donde se ejecuta, y los cambia el equipo de desarrollo al publicar una versión.

**B.** En la base de datos. Una tabla guarda los valores generales de la plataforma, y cada institución guarda los suyos en su propio espacio. Los cambia el administrador de cada institución desde una pantalla del sistema.

**C.** En un servicio de configuración aparte. Un servicio dedicado a guardar configuración almacena los valores y el sistema se los pide cuando los necesita. Los cambia quien administre ese servicio.

### Decisión

Se elige la opción B, en la base de datos.

Cada administrador institucional ajusta las reglas de su institución desde una pantalla, sin esperar una versión nueva, y una institución que entra arranca con los valores generales sin configurar nada. Esos valores viven en el mismo espacio que el resto de sus datos, así que quedan protegidos por la misma separación que aísla a una institución de otra, y solo los cambia quien tenga ese permiso.

Cuando se genera una multa, queda guardada con la tarifa que regía ese día. Así, cambiar la tarifa afecta de ahí en adelante y nunca recalcula lo que ya ocurrió.

Se descarta A porque cualquier ajuste de una institución obligaría al equipo a publicar una versión nueva, y todas las instituciones tendrían que compartir el mismo valor.

Se descarta C porque sería una pieza más que instalar y mantener, y guardaría los valores fuera del espacio de cada institución, de modo que la separación entre ellas habría que construirla otra vez allí.

### Consecuencias

**Positivas**

1. El administrador de cada institución ajusta sus reglas desde una pantalla, sin esperar una versión nueva.
2. Una institución que entra arranca con los valores generales, sin configurar nada.
3. Cada cambio queda registrado: quién lo hizo, cuándo, el valor anterior y el nuevo, y cada multa se puede explicar con la tarifa que regía ese día.
4. Los parámetros de cada institución quedan protegidos por la misma separación que aísla sus datos.

**Negativas**

1. El cálculo de multas debe usar la tarifa que regía en la fecha correspondiente y no la actual, lo que obliga a guardar ese dato junto a cada multa.
2. Los valores se guardan en la caché para no consultar la base en cada solicitud; si falla el borrado de esa copia al cambiar un parámetro, algunas solicitudes usarían el valor anterior durante un rato.

---

<a id="adr-018"></a>
## ADR-018 · Catálogo de mensajes de la aplicación (Message Catalog)

**Fecha:** 18/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** RF-04, RF-09, RF-19, RT-12  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive muestra mensajes de confirmación, de reglas del negocio, de validación y de errores, y envía correos con sus propios textos. Si cada mensaje se escribe dentro del código, la misma situación termina mostrando textos distintos, corregir una palabra exige publicar una versión nueva, y un error técnico puede mostrarle al usuario detalles internos del sistema. Falta decidir dónde se guardan esos mensajes y cómo los usa el sistema.

### Alternativas consideradas

**A.** Textos escritos dentro del código. Cada mensaje se escribe directamente en la parte del sistema que lo muestra, y los cambia el equipo de desarrollo.

**B.** Archivos de mensajes dentro de la aplicación. Los mensajes se guardan en archivos aparte del código, identificados por un código y con una versión por idioma. Los cambia el equipo de desarrollo.

**C.** Tabla de mensajes en la base de datos. Los mensajes se guardan en una sola tabla, común a todas las instituciones, identificados por un código y con la posibilidad de incluir datos variables como el nombre de la institución o una fecha. Los cambia el equipo de la plataforma desde una pantalla del sistema.

### Decisión

Se elige la opción C, tabla de mensajes en la base de datos.

Todos los textos quedan en un solo lugar, así que la misma situación muestra siempre el mismo mensaje en pantallas y correos, y corregir una palabra no obliga a publicar una versión nueva. La tabla es única para todas las instituciones: lo que cambia entre ellas entra como dato variable dentro del texto, y solo el equipo de la plataforma puede editarlo.

Ante un error técnico, el usuario ve un mensaje general con un código, y el detalle queda en los registros técnicos bajo ese mismo código: nadie ve información interna y el equipo ubica la falla exacta.

Se descarta A porque los textos quedarían repartidos por todo el código y cualquier corrección exigiría publicar una versión nueva.

Se descarta B porque, aunque reúne los textos en un solo lugar, cambiarlos seguiría exigiendo publicar una versión nueva.

### Consecuencias

**Positivas**

1. La misma situación muestra siempre el mismo texto, en pantallas y correos, para todas las instituciones.
2. Corregir un texto no exige publicar una versión nueva, y la corrección llega a todos al tiempo.
3. Ante un error técnico el usuario ve un mensaje general con un código, y ese mismo código permite ubicar la falla exacta en los registros técnicos.
4. Solo el equipo de la plataforma edita los textos, así que la redacción mantiene un nivel parejo.

**Negativas**

1. Un texto mal escrito se ve de inmediato en todas las instituciones, así que cada cambio debe revisarse antes de guardarse.
2. Ninguna universidad puede adaptar la redacción a su manera; si alguna lo necesitara más adelante, habría que agregar textos propios por institución, algo que la estructura permite pero que hoy no se construye.
3. Cada mensaje nuevo debe quedar registrado en la tabla, y si falta, el sistema muestra un texto por defecto.

---

<a id="adr-019"></a>
## ADR-019 · Mecanismo de concurrencia para la cola de reservas

**Fecha:** 07/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-05, RF-03, RF-13, RF-25, RT-07  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cuando un ejemplar no está disponible, varios usuarios pueden reservarlo al mismo tiempo, y BookHive debe atenderlos en el orden exacto en que llegaron. Si dos solicitudes llegan casi simultáneamente, el sistema no puede darle el turno a la persona equivocada ni asignar el mismo ejemplar a dos personas a la vez. Hay que definir cómo se garantiza ese orden bajo concurrencia.

### Alternativas consideradas

**A.** Bloquear la fila del ejemplar mientras se procesa cada solicitud. Cuando alguien pide reservar, el sistema bloquea esa fila hasta terminar, evitando que dos solicitudes se procesen a la vez sobre el mismo ejemplar. Sin embargo, no queda registrado quién llegó primero, así que si varias solicitudes llegan casi al mismo tiempo, no hay garantía de que se atiendan en el orden real de llegada.

**B.** Guardar cada solicitud con la hora exacta de llegada y atenderlas en ese orden. Cada solicitud queda registrada con marca de tiempo, y el sistema revisa esa lista y siempre asigna el ejemplar a la solicitud más antigua sin atender.

**C.** Usar una cola de mensajes externa que procese las solicitudes una por una. Las solicitudes de reserva se envían a un sistema aparte especializado en colas, que garantiza que se procesen exactamente en el orden en que llegaron.

### Decisión

Se elige la opción B, guardar cada solicitud con la hora exacta de llegada y atenderlas en ese orden.

Esa hora se toma en el instante exacto en que se registra cada solicitud, no al comenzar la operación que la guarda: si se tomara al comienzo, dos solicitudes guardadas dentro de una misma operación quedarían con la misma hora y el desempate sería arbitrario. Al asignar el ejemplar a la solicitud más antigua, esa operación se protege con un bloqueo, para que si dos ejemplares se liberan casi al mismo tiempo, ninguna solicitud reciba el mismo.

Se descarta A porque evita que dos solicitudes choquen, pero no registra quién llegó primero: con solicitudes casi simultáneas, el turno podría quedar en la persona equivocada.

Se descarta C porque una cola de mensajes conserva el orden solo mientras la atienda un único consumidor y ningún mensaje vuelva a la fila. Por eso, aunque el proyecto use una cola para otras cosas, la fila de reservas no se ordena con ella.

### Consecuencias

**Positivas**

1. Quien reservó primero recibe el ejemplar primero, que es el orden que el proyecto exige.
2. El bloqueo al asignar evita que dos solicitudes reciban el mismo ejemplar, aunque se liberen varios casi al mismo tiempo.
3. El turno de cada persona queda registrado, así que ante un reclamo se puede demostrar quién iba primero.

**Negativas**

1. Si muchas solicitudes llegan a la vez sobre el mismo ejemplar, las últimas deben esperar a que se resuelvan todas las anteriores, una por una.
2. El orden depende por completo de que la hora de llegada se registre bien; si esa marca falla, la fila se desordena sin que nadie lo note de inmediato.

---

<a id="adr-020"></a>
## ADR-020 · Elección del tipo de motor para la búsqueda del catálogo: Relacional vs no relacional

**Fecha:** 07/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-15, US-27, RF-12  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El catálogo crece con cada universidad que entra, y las búsquedas sobre él compiten por recursos con las operaciones que no se pueden volver lentas: préstamos, multas y pagos. Falta decidir con qué tipo de motor se resuelven esas búsquedas.

### Alternativas consideradas

**A.** Motor relacional. El catálogo se guarda en tablas y las búsquedas se hacen con consultas sobre esas mismas tablas, apoyadas en índices. Ocurren dentro de la misma base donde se registran préstamos, multas y pagos.

**B.** Motor no relacional especializado en búsqueda. Guarda aparte un índice que registra, para cada palabra, en qué títulos aparece, y responde consultando ese índice en vez de recorrer el catálogo. Tolera errores de escritura y ordena los resultados por relevancia. Funciona como una pieza separada de la base principal

### Decisión

Se elige la opción B, un motor no relacional especializado en búsqueda.

Las búsquedas del catálogo dejan de compartir máquina con las operaciones críticas: préstamos, multas y pagos. A medida que entran más universidades, el catálogo y la cantidad de búsquedas crecen, y ese crecimiento ya no le quita procesador ni memoria a la base donde se registran esas operaciones. Además, el motor de búsqueda tolera errores de escritura y ordena los resultados por relevancia.

Se descarta A porque las búsquedas competirían por recursos con las operaciones que no se pueden volver lentas, justo en los momentos de mayor uso.

### Consecuencias

**Positivas**

1. Las búsquedas no le quitan recursos a la base donde se registran préstamos, multas y pagos.
2. Los resultados salen ordenados por relevancia y toleran errores de escritura.
3. El catálogo puede crecer con cada universidad nueva sin afectar al resto del sistema.

**Negativas**

1. Es otra pieza encendida todo el tiempo, con su propio costo mensual y su propio mantenimiento.
2. El catálogo queda en dos lugares, así que cada cambio debe reflejarse también en el motor de búsqueda.

---

<a id="adr-021"></a>
## ADR-021 · Elección del motor de búsqueda no relacional

**Fecha:** 07/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-27, RF-12, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive necesita buscar libros en un catálogo compartido por varias instituciones, de forma rápida, tolerante a errores de escritura, y filtrable por autor, género o sede. La búsqueda sirve para encontrar el título y saber en qué sede está; la cantidad de ejemplares disponibles en ese momento no sale de aquí.

### Alternativas consideradas

**A.** Elastic Cloud. Servicio administrado de Elasticsearch, un motor muy usado y con muchas funciones de análisis. Cobro mensual fijo.

**B.** Meilisearch Cloud. Servicio administrado de Meilisearch, un motor liviano con tolerancia a errores de escritura. Plan gratuito con límites de uso.

**C.** Typesense Cloud. Servicio administrado de Typesense. Plan gratuito bajo, y por encima cobra según las búsquedas realizadas.

**D.** Typesense instalado por el equipo. El mismo motor, de código abierto, instalado en una máquina propia dentro de la plataforma. Mantiene el índice en memoria.

### Decisión

Se elige la opción D, Typesense instalado por el equipo.

Las tres opciones administradas cobran mensualidad o cobran por búsqueda, y ninguna cabe en el presupuesto. Instalarlo por cuenta propia no cuesta licencia, y aquí el riesgo de hacerlo es bajo: lo que este motor guarda es una copia del catálogo que se puede volver a armar desde la base de datos en cualquier momento, así que si se cae no se pierde nada ni se detienen los préstamos.

Además, tolera errores de escritura, permite filtrar por autor, categoría, sede e institución y le entrega a cada universidad una clave que solo alcanza su propio catálogo.

Se descartan A, B y C porque las tres cobran: Elastic cobra una mensualidad fija, Meilisearch limita su plan gratuito por debajo de lo que el sistema necesita, y Typesense Cloud cobra según las búsquedas realizadas.

### Consecuencias

**Positivas**

1. No se paga licencia ni se depende de un proveedor externo, lo cual encaja con el presupuesto fijo del proyecto.
2. Es fácil de instalar y de mantener funcionando, así que el propio equipo de desarrollo puede encargarse sin necesitar a alguien especializado en infraestructura.
3. Permite buscar libros de forma rápida y tolerar errores de escritura (por ejemplo, si el estudiante escribe mal el título o el autor) y género o autor.
4. El aislamiento entre instituciones lo impone el propio motor a través de la clave de búsqueda, no la consulta, así que un error de código no expone el catálogo de otra universidad.

**Negativas**

1. Es una tecnología menos conocida y con una comunidad más pequeña.
2. Al ser un proyecto más nuevo, tiene menos funciones avanzadas de análisis.
3. Se agrega un componente más al sistema, lo que implica mantener sincronizada la información del catálogo entre la base de datos principal y el motor de búsqueda.
4. Guarda todo su índice en memoria. Entre más crezca el catálogo sumado de todas las instituciones, más memoria exige el servidor.

---

<a id="adr-022"></a>
## ADR-022 · Mantener actualizada la búsqueda del catálogo (sincronización con el motor de búsqueda)

**Fecha:** 12/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-27, RF-11, RF-12  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Los títulos se guardan en la base de datos principal y se buscan en un motor de búsqueda aparte. Cada vez que se registra, edita o da de baja un título, la búsqueda debe reflejarlo. Hay que decidir cómo se mantiene actualizado el motor de búsqueda seleccionado.

### Alternativas consideradas

**A.** Actualizar los dos al mismo tiempo. Al guardar un título, el sistema lo guarda en la base de datos principal y enseguida lo envía al motor de búsqueda.

**B.** Anotar el cambio como pendiente y enviarlo después. Al guardar un título, en el mismo paso se anota el cambio como pendiente. Una tarea aparte toma esos cambios y los envía al motor de búsqueda.

**C.** Leer los cambios directamente de la base de datos. Una herramienta aparte revisa los cambios que registra la base de datos y los envía al motor de búsqueda, sin que el sistema intervenga.

**D.** Recargar todo el catálogo cada cierto tiempo. Una tarea programada vuelve a cargar todos los títulos en el motor de búsqueda de forma periódica.

### Decisión

Se elige la opción B, anotar el cambio como pendiente, con una revisión periódica de respaldo. La anotación se hace en el mismo paso en que se guarda el título: o quedan los dos, o no queda ninguno, así que nunca hay un título guardado sin que la búsqueda se entere. Ese cambio llega a la búsqueda por dos caminos. El normal: apenas se guarda el título, el sistema publica un aviso, el backend lo recibe y actualiza la búsqueda en pocos segundos. El de respaldo: una tarea programada revisa cada cierto tiempo qué cambios quedaron sin aplicar y los reenvía.

Se descarta A porque si el sistema falla entre las dos escrituras, el título queda guardado sin que la búsqueda se entere. Se descarta C porque exige sumar un servicio más, con su propio costo, para algo que ya se puede hacer con lo que hay. Se descarta D porque entre una recarga y la siguiente la búsqueda queda desactualizada, y cada recarga pesa más a medida que crece el catálogo.

### Consecuencias

**Positivas**

1. Ningún cambio del catálogo se pierde: si el título quedó guardado, tarde o temprano aparece en la búsqueda.
2. Si el motor de búsqueda falla, los cambios esperan y se aplican cuando vuelve, sin impedir que se sigan registrando títulos.
3. No se agrega nada nuevo porque se usa el sistema de Message Broker y las tareas programadas ya decididas.

**Negativas**

1. La búsqueda no se actualiza al instante: un título nuevo puede tardar unos segundos en aparecer. Por eso la disponibilidad de ejemplares no se toma de la búsqueda.
2. Un mismo cambio puede enviarse dos veces, así que la búsqueda debe aplicarlo una sola vez.
3. Si la tarea que envía los cambios se detiene, la búsqueda deja de actualizarse sin mostrar ningún error hasta la siguiente revisión periódica.

---

<a id="adr-023"></a>
## ADR-023 · Selección de la tecnología de la caché

**Fecha:** 12/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-01, US-04, US-14, US-18, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive necesita tener a mano cierta información para no consultarla en la base de datos cada vez: los permisos de cada rol, los reportes más pedidos y el conteo de intentos repetidos sobre una misma acción. Ese conteo tiene que ser compartido: el backend funciona en varias copias al tiempo, y si cada una lleva su propia cuenta, cinco intentos se convierten en quince. Falta definir con qué tecnología se guarda esa información, teniendo en cuenta el presupuesto fijo del proyecto.

### Alternativas consideradas

**A.** Redis instalado por el equipo. Guarda en memoria contadores, listas y textos con vencimiento automático, y permite aumentar un contador y fijarle vencimiento en un solo paso. Es de código abierto: no cobra licencia, pero necesita una máquina encendida y una conexión de red propia para que el backend la alcance.

**B.** Memcached instalado por el equipo. Guarda en memoria parejas de nombre y valor con vencimiento. También de código abierto y con los mismos requisitos de máquina y red.

**C.** Memorystore. El mismo Redis, instalado y mantenido por Google. Se contrata por capacidad, con un mínimo cercano a 35 dólares al mes.

**D.** Sin caché dedicada. Los contadores viven en una tabla de la base de datos y los permisos en la memoria de cada copia del backend. No agrega ninguna pieza ni ningún costo.

### Decisión

Se elige la opción A, Redis instalado por el equipo.

Es el único que aumenta un contador y le pone vencimiento en un solo paso, sin que dos solicitudes que llegan al mismo tiempo se pisen, y ese contador lo comparten todas las copias del backend. Todo lo que se guarde lleva el nombre de la institución dentro, para que un dato guardado para una universidad nunca se le entregue a otra.

Se descarta B porque ese conteo habría que construirlo aparte en el código, con más riesgo de errores.

Se descarta C porque cobra por capacidad contratada, con un mínimo de unos 35 dólares al mes aunque el sistema esté inactivo: más del doble de lo que cuesta mantenerlo por cuenta propia.

Se descarta D porque los permisos guardados en la memoria de cada copia se desactualizan de forma distinta en cada una, y llevar los contadores a la base de datos le suma una escritura por solicitud a la pieza que menos conviene recargar.

### Consecuencias

**Positivas**

1. Una sola herramienta cubre las tres necesidades, sin sumar otra pieza al sistema.
2. Los contadores no se pisan y comparten todas las copias del backend.
3. No tiene costo de licencia ni un costo que crezca con el uso.

**Negativas**

1. Guarda todo en memoria: si se reinicia, se pierden los contadores y lo guardado, y quien estuviera bloqueado vuelve a empezar de cero.
2. Solo provee el almacenamiento; decidir cuándo bloquear o cuándo reutilizar lo guardado sigue siendo trabajo del código.

---

<a id="adr-024"></a>
## ADR-024 · Máquina donde se ejecutan el motor de búsqueda y la caché

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-27, RF-12, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El motor de búsqueda y la caché son dos programas que el equipo instala y mantiene. Falta decidir en qué máquina corren, porque los dos deben estar encendidos siempre y el presupuesto del proyecto está definido.

### Alternativas consideradas

**A.** Una máquina de 4 GB con los dos programas adentro. La crea y la mantiene el equipo. 77.100 COP al mes.

**B.** Dos máquinas de 2 GB, una para cada programa. Las crea y las mantiene el equipo. 77.100 COP al mes entre las dos.

**C.** Un clúster administrado de Google. Google pone y mantiene las máquinas; el equipo solo dice cuánta memoria pide cada programa. 147.500 COP al mes.

### Decisión

Se elige la opción A, una máquina con los dos programas adentro.

Si esa máquina se cae, no se pierde nada: el catálogo se vuelve a copiar desde la base de datos y los contadores empiezan de cero. Por eso no vale la pena pagar de más por separarlos ni porque alguien más los administre.Cuesta 925.000 COP al año, menos que los 1.324.000 COP anuales de la caché alquilada que se descartó en ADR-023.

Se descarta B porque cuesta lo mismo y parte la memoria en dos mitades fijas.

Se descarta C porque cuesta el doble.

### Consecuencias

**Positivas**

1. Los dos programas comparten la memoria, así que el que más la necesite la aprovecha.
2. Si el catálogo crece, se cambia la máquina por una más grande.
3. Si la máquina se cae, no se pierde información.

**Negativas**

1. Es una máquina más que el equipo debe mantener.
2. Si un programa consume toda la memoria, afecta al otro.
3. Mientras esté caída, no hay búsquedas ni freno a los intentos fallidos.

---

<a id="adr-025"></a>
## ADR-025 · Autenticación y control de acceso: propio o externo

**Fecha:** 05/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-07, US-23, RF-01, RN-03, RN-04  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive necesita autenticación y control de acceso para administradores institucionales, bibliotecarios y usuarios de varias instituciones. El manejo de contraseñas es una de las partes más sensibles de cualquier sistema: un error de diseño puede exponer a todos los usuarios. Además, incorporar una institución ya exige validar su dominio de correo institucional, lo cual se relaciona directamente con que el usuario inicie sesión con su cuenta institucional. Hay que decidir si BookHive construye esto por su cuenta o se apoya en un proveedor externo.

### Alternativas consideradas

**A.** Sistema de autenticación propio. El equipo de BookHive construye y mantiene por su cuenta todo el manejo de cuentas y contraseñas.

**B.** Herramienta de autenticación ya construida. Se usa un software especializado que ya resuelve el manejo de cuentas y contraseñas, ya sea instalado y administrado por el propio equipo o contratado como servicio en la nube, y le confirma a BookHive quién es cada usuario.

**C.** Inicio de sesión con la cuenta institucional. Cada universidad conecta su propio sistema de cuentas, y el usuario entra con el correo institucional que ya tiene.

### Decisión

Se elige la opción B, una herramienta de autenticación ya construida.

El manejo de contraseñas es de lo más delicado de cualquier sistema: guardarlas bien, bloquear tras varios intentos fallidos, ofrecer segundo factor, y una herramienta especializada lo trae resuelto y probado desde el primer día.

Se descarta A porque construir eso desde cero tomaría meses y, mientras tanto, expondría a todos los usuarios.

Se descarta C porque conectarse al sistema de cuentas de cada universidad obligaría a una integración distinta con cada una y a hacerse cargo de información confidencial suya. Validar que el correo pertenezca al dominio de una institución activa basta para confirmar que la persona pertenece a ella. Esa conexión no queda descartada para siempre: podrá habilitarse más adelante con las universidades que la pidan.

### Consecuencias

**Positivas**

1. El equipo no construye ni mantiene el manejo de contraseñas: guardarlas bien, bloquear tras varios intentos y el segundo factor vienen resueltos.
2. El equipo no tiene que seguirle el ritmo a las prácticas de seguridad, que cambian todo el tiempo.
3. Una institución nueva entra validando su dominio de correo, sin una integración técnica propia para cada una.

**Negativas**

1. Si la herramienta falla, nadie nuevo puede entrar; quienes ya tienen la sesión abierta siguen trabajando hasta que se venza.
2. Las reglas de las contraseñas y del segundo factor las fija la herramienta: el sistema solo ajusta lo que ella permita.
3. Si la herramienta cambia de precio o de condiciones, cambiarla implica mover las cuentas de todos los usuarios.

---

<a id="adr-026"></a>
## ADR-026 · Elección de proveedor de autenticación (IDP)

**Fecha:** 07/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-07, US-19, US-23, RF-01, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive necesita verificar quién es cada persona antes de dejarla entrar y guardar las cuentas de todas las instituciones. Se apoyará en un proveedor de autenticación ya construido, en vez de desarrollarlo desde cero. Falta elegir cuál, considerando que el número de instituciones y usuarios crecerá con el tiempo, y que el proyecto tiene un presupuesto fijo.

### Alternativas consideradas

**A.** Identity Platform. Servicio de identidad administrado por Google, dentro de la misma nube donde corre BookHive. Incluye 50.000 usuarios activos al mes sin costo.

**B.** Auth0. Servicio de identidad administrado por una empresa especializada, independiente de la nube. Incluye 25.000 usuarios activos al mes sin costo.

**C.** Microsoft Entra External ID. Servicio de identidad administrado por Microsoft, dentro de su propia nube. Incluye 50.000 usuarios activos al mes sin costo.

### Decisión

Se elige la opción A, Identity Platform.

Cubre a 50.000 usuarios activos al mes sin costo, y vive en la misma nube donde ya corre el sistema: una sola facturación, un solo control de accesos y un solo tercero con acceso a datos personales.

Se descarta B porque su plan gratuito cubre la mitad de los usuarios que A y porque su historial de aumentos de precio representa un riesgo para un presupuesto fijo.

Se descarta C porque, aun cubriendo los mismos 50.000 usuarios, obligaría a operar con dos nubes distintas: dos facturaciones, dos controles de acceso y dos proveedores con datos personales.

### Consecuencias

**Positivas**

1. El equipo no instala, actualiza ni vigila un servidor de identidad.
2. Con el volumen previsto, la autenticación no genera ningún costo.
3. Despliegue e identidad quedan en un mismo proveedor: una sola facturación y un solo tercero con acceso a datos personales.
4. La disponibilidad del inicio de sesión queda respaldada por el acuerdo de servicio de Google.

**Negativas**

1. Las cuentas quedan guardadas fuera del país, lo que constituye una transferencia internacional de datos personales y debe informarse en la política de tratamiento de datos.
2. Aumenta la dependencia de Google: cambiar de proveedor obligaría a trasladar todas las cuentas.
3. La pantalla de inicio de sesión no viene incluida: la construye el frontend.

---

<a id="adr-027"></a>
## ADR-027 · Estrategia de autorización y control de acceso basado en roles

**Fecha:** 07/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-01, US-04, RF-05, RF-16, RT-05  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

En BookHive una misma persona puede tener varios papeles a la vez, así que la comprobación no puede preguntar cuál es su papel sino si tiene permiso para hacer esa operación. Cada intento debe verificar dos cosas: que su papel se lo permita y que sea sobre su propia universidad; y como la acción se puede intentar sin pasar por la pantalla, esa verificación tiene que ir por dentro del sistema. Falta decidir dónde vive esa comprobación y dónde se guardan las reglas de qué puede hacer cada papel.

### Alternativas consideradas

**A.** Comprobar los permisos en cada parte del sistema. Cada acción lleva escrito en su propio código qué papeles pueden ejecutarla. Las reglas quedan repartidas por todo el sistema.

**B.** Comprobar los permisos en un solo lugar. Todo intento pasa por una misma pieza del sistema, que decide si el papel de la persona lo permite antes de dejarla continuar. Las reglas siguen dentro del código, concentradas en un solo sitio.

**C.** Comprobar los permisos en un solo lugar y guardar las reglas como datos. La comprobación funciona igual que en B, pero las reglas viven en una tabla que relaciona cada papel con las acciones que puede hacer, y se ajustan desde una pantalla del sistema.

### Decisión

Se elige la opción C, comprobar en un solo lugar con las reglas guardadas como datos.

Cada universidad define qué hace cada papel dentro de su institución, y el administrador puede conceder o quitar un permiso desde la pantalla sin esperar una versión nueva del sistema.Las acciones se dan de alta solas en la tabla cuando se publica una versión nueva, y nacen sin ningún papel asignado hasta que un administrador las conceda.

Hay un grupo de permisos fijos que ningún administrador puede modificar: ver datos de otra institución, alterar el historial de préstamos, multas y pagos, y cambiar el valor de una multa ya generada. La tabla de permisos se guarda en la caché para no consultarla en cada solicitud.

Se descartan A y B porque dejan las reglas dentro del código: cualquier ajuste exigiría publicar una versión nueva del sistema.

### Consecuencias

**Positivas**

1. El administrador de cada institución ajusta los permisos de sus papeles desde el sistema, sin depender de un cambio de código.
2. Los permisos se revisan en un solo lugar, así que ninguna función queda sin control por olvido.
3. Lo más delicado (ver datos de otra institución, alterar el historial, cambiar una multa ya generada) no lo puede habilitar ningún administrador.
4. Como los permisos se guardan en la caché, revisarlos no le suma trabajo a la base de datos en cada solicitud.

**Negativas**

1. Un error al configurar, como conceder un permiso de más, abre acceso indebido dentro de esa institución sin que el código lo impida.
2. Una función recién agregada no la puede usar nadie hasta que un administrador le asigne los papeles que correspondan.

---

<a id="adr-028"></a>
## ADR-028 · Registro del rol y la institución de cada usuario

**Fecha:** 07/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RF-01, RF-16, RT-05, RN-04  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Las reglas sobre qué puede hacer cada rol ya están definidas. Falta decidir dónde queda registrado el rol de cada persona y la institución a la que pertenece: en el proveedor de identidad o en BookHive. De esa decisión depende qué datos debe entregar el proveedor de identidad y desde dónde se administran los roles.

### Alternativas consideradas

**A.** Roles registrados en el proveedor de identidad. El proveedor guarda el rol y la institución de cada persona y los incluye en el token al iniciar sesión. BookHive lee esos datos del token y aplica sus reglas de permisos.

**B.** Roles registrados en BookHive. El proveedor de identidad solo confirma quién es la persona. BookHive consulta en su propia base de datos el rol y la institución de esa persona y aplica sus reglas de permisos.

### Decisión

Se elige la opción B, roles registrados en BookHive.

El administrador de cada institución ya gestiona los permisos desde el sistema, así que asignar el rol desde esa misma pantalla es suficiente y no obliga a modificar datos en otro lugar. Además, al proveedor de identidad solo se le pide autenticar a la persona y entregar un identificador, algo que cualquiera cumple: cambiarlo mañana sería reemplazar solo esa conexión.

Se descarta A porque obligaría a repetir en el proveedor cada cambio de rol hecho desde BookHive, y un rol retirado seguiría vigente hasta que venza el pase de la persona.

### Consecuencias

**Positivas**

1. El rol, los permisos y la institución se administran en un solo lugar.
2. Un cambio de rol se aplica en la siguiente solicitud, porque al guardarlo se borra la copia que estaba en la caché.
3. Cambiar de proveedor de identidad no obliga a trasladar los roles ni las instituciones.

**Negativas**

1. Cada solicitud necesita saber el rol y la institución de quien la hace; se resuelve leyéndolos de la caché, pero es un dato más que mantener al día.
2. BookHive asume la responsabilidad de proteger las asignaciones de roles.

---

<a id="adr-029"></a>
## ADR-029 · Manejo de la sesión del usuario en el navegador

**Fecha:** 19/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-05, RT-21, RN-03  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cuando alguien entra, el proveedor de identidad entrega un pase que dice quién es, y ese pase viaja con cada solicitud que el front le hace al backend. Falta decidir dónde queda guardado ese pase mientras la persona usa el sistema, porque de ahí depende de que nadie más pueda tomarlo y usarlo en su nombre, y qué pasa cuando la persona recarga la pantalla o cierra el navegador.

### Alternativas consideradas

**A.** En el almacenamiento del navegador. El pase queda guardado en el espacio que el navegador reserva para la aplicación. Sobrevive a las recargas y al cierre del navegador, y cualquier código que se ejecute dentro de la página lo puede leer.

**B.** En la memoria de la aplicación. El pase existe solo mientras la pantalla está abierta. Al recargar se pierde y hay que volver a pedirlo. Al no quedar guardado en ninguna parte, no hay de dónde tomarlo.

**C.** En una cookie que el navegador no deja leer desde la página. El navegador la guarda y la envía sola con cada solicitud. El código de la página no la puede leer ni copiar.

### Decisión

Se elige la opción C, una cookie que el navegador no deja leer desde la página.

El pase queda fuera del alcance de cualquier código que se ejecute dentro de la página, así que un script indebido no puede tomarlo y usarlo en nombre del usuario. Y la persona sigue dentro del sistema aunque recargue la pantalla, sin tener que entrar de nuevo.

La sesión no dura para siempre: se cierra sola cuando la persona pasa 20 minutos sin hacer nada, y también cuando cierra sesión. En cualquiera de los dos casos, el pase deja de servir de inmediato y hay que volver a entrar.

Se descarta A porque cualquier código que se ejecute dentro de la página puede leer el pase y usarlo en nombre del usuario.

Se descarta B porque obligaría a entrar de nuevo cada vez que se recargue la pantalla.

### Consecuencias

**Positivas**

1. El pase no se puede leer desde la página, así que no se puede robar desde ahí.
2. La contraseña nunca llega al backend de BookHive: viaja directo al proveedor de autenticación.
3. La sesión se cierra sola tras un rato sin uso, así que un computador de sala no queda abierto con la cuenta de alguien más.
4. Recargar la pantalla no saca a nadie del sistema.

**Negativas**

1. El backend debe encargarse de crear y eliminar la sesión: dos operaciones más que mantener.
2. Si el navegador de la persona bloquea las cookies, el sistema no funciona.

---

<a id="adr-030"></a>
## ADR-030 · Selección de la puerta de entrada de las solicitudes (Api Gateway)

**Fecha:** 09/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-01, US-04, US-14, RT-05, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Todo lo que un usuario hace en BookHive llega al sistema como una solicitud. Conviene que, antes de llegar al backend, todas pasen por una puerta que funcione como una capa más de protección: ahí se revisa el pase de quien envía cada solicitud y se frena a quien insista con intentos fallidos. Falta decidir qué pieza cumple ese papel. Hay que tener en cuenta que el presupuesto es fijo y que revisar el pase en la entrada no reemplaza la comprobación de rol e institución dentro del sistema.

### Alternativas consideradas

**A.** Kong. Programa gratuito que el equipo instala y mantiene: encendido todo el tiempo y con su propia base de datos, entre 25 y 35 dólares al mes. Revisa el pase con su propio mecanismo, distinto al del resto del sistema.

**B.** El control de tráfico de Google sobre el repartidor de tráfico. Lo administra Google, sin nada que instalar. Revisa el tráfico antes de que llegue al sistema, cuando todavía no se sabe de quién viene, así que cuenta por dirección de internet. Se cobra el repartidor de tráfico más cada solicitud atendida.

**C.** Spring Cloud Gateway. Pieza construida con la misma tecnología del backend, sin licencia ni cobro por uso. Revisa el mismo pase que el resto del sistema y cuenta los intentos sobre la caché que ya existe.

### Decisión


Se elige la opción C, Spring Cloud Gateway, desplegada como un contenedor propio delante del backend. No tiene licencia ni cobro por uso; revisa el mismo pase que el resto del sistema , sin montar una segunda revisión, y cuenta los intentos sobre la caché que ya existe, así que no suma ninguna herramienta nueva.

Se descarta A porque obliga a mantener una pieza encendida todo el tiempo con su propia base de datos y un gasto fijo mensual, y aun así el pase quedaría revisándose en dos lugares distintos.

Se descarta B porque revisa el tráfico antes de que se sepa de quién viene, así que solo puede contar por dirección de internet y nunca por cuenta, que es justamente lo que este punto de entrada necesita hacer.

### Consecuencias

**Positivas**

1. No cobra licencia ni por uso, sin importar cuántas instituciones o usuarios se conecten.
2. Usa la misma tecnología del resto del sistema y la caché que ya existe: nada nuevo que aprender.
3. Una solicitud sin pase válido o con intentos de más se frena antes de llegar al backend.

**Negativas**

1. Revisar el pase en la entrada no reemplaza la comprobación de rol e institución dentro del sistema: siguen siendo dos revisiones distintas.
2. Es un contenedor más que construir, desplegar y vigilar, y agrega un salto en cada solicitud.
3. Al ser el único punto de entrada, si esta pieza se detiene, el sistema completo deja de atender; por eso su capacidad se ajusta sola, igual que la del backend.

---

<a id="adr-031"></a>
## ADR-031 · Política de limitación de intentos

**Fecha:** 17/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-01, US-04, US-14, US-19  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cuatro escenarios de seguridad exigen lo mismo: frenar a quien insiste con intentos fallidos sobre una acción sensible y dejar registro de cada intento. Falta decidir sobre qué se cuentan esos intentos: si se cuentan por el lugar desde donde llega la solicitud, quien cambie de red esquivaría el bloqueo; si se cuentan por cuenta, queda sin contar el tráfico de quien todavía no ha iniciado sesión. Falta definir también cuántos intentos y en qué lapso disparan el bloqueo, cuánto dura, y qué hace el sistema si la caché donde viven los contadores deja de responder.

### Alternativas consideradas

**A.** Contar por el lugar desde donde llega la solicitud. El contador se lleva por dirección de internet, sin importar a qué cuenta se intenta entrar.

**B.** Contar por cuenta. El contador se lleva por la cuenta contra la que se intenta la acción, sin importar desde dónde llegue.

**C.** Contar las dos cosas al tiempo, cada una con su propio número: uno bajo por cuenta y otro mucho más alto por origen.

### Decisión

Se elige la opción C, los dos contadores al tiempo.

Por cuenta: cinco intentos fallidos en diez minutos y esa acción queda frenada otros diez. Es lo que protege a una persona de quien prueba contraseñas cambiando de red, porque la red cambia pero la cuenta no. Por origen: un número mucho más alto, que solo alcanza una máquina disparando sola, así se frenan las avalanchas sin bloquear a un campus entero, que sale a internet por una sola conexión.

El bloqueo cae sobre la acción que venía fallando, no sobre la cuenta entera, y se levanta solo a los diez minutos. Si la caché donde viven los contadores no responde, el tráfico normal pasa y las operaciones administrativas quedan bloqueadas: entre dejar el sistema caído y dejar sin protección lo más sensible, se protege lo sensible.

Se descarta A porque no detiene a quien ataca una cuenta desde varias redes, y B porque no ve la avalancha que va contra muchas cuentas a la vez.

### Consecuencias

**Positivas**

1. Un solo mecanismo cubre los cuatro escenarios de seguridad, en vez de tres implementaciones distintas que mantener iguales.
2. Cada contador tapa el hueco del otro: el de cuenta frena a quien ataca a una persona repartiendo los intentos entre varias IP, y el de IP frena la avalancha automatizada aunque vaya contra cuentas distintas.
3. No castiga a una red compartida: el intento de una persona en un hogar universitario no deja por fuera a los demás.

**Negativas**

1. El número del contador por IP hay que medirlo antes de salir a producción: muy bajo estorba a las redes compartidas, muy alto no frena nada.
2. Cuando lo que falla son los intentos de iniciar sesión, el dueño legítimo queda sin entrar esos diez minutos. Es el costo de proteger la cuenta y se acepta así.

---

<a id="adr-032"></a>
## ADR-032 · Selección del filtro de protección del tráfico de entrada (WAF)

**Fecha:** 22/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-01, US-04, US-14, US-16, RN-03, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive está en internet y puede recibir avalanchas de solicitudes o solicitudes con ataques escondidos. La puerta de entrada ya los frena, pero cuando ya llegaron al sistema. Falta decidir si se pone un filtro antes, sin salirse del presupuesto.

### Alternativas consideradas


**A.** Cloud Armor. Filtro de Google que revisa cada solicitud antes de que llegue al sistema y bloquea las que parecen ataques. Lo administra Google. Cuesta cerca de 96.000COP al mes.

**B.** Cloudflare. Filtro de otra empresa por el que pasa todo el tráfico antes de llegar al sistema. Lo administra Cloudflare. Cuesta cerca de 64.000 COP al mes.

**C.** ModSecurity. Filtro gratuito que se instala delante del sistema. Lo instala y mantiene el equipo. Cuesta cerca de 27.000 COP al mes.

**D.** Sin filtro. La puerta de entrada, Spring y Angular se encargan de todo. No cuesta nada.

### Decisión

Se elige la opción A, Cloud Armor. Frena los ataques antes de que lleguen al sistema, así que no le quitan capacidad para atender a los usuarios. Google mantiene sus reglas al día, y cada bloqueo queda registrado en la misma herramienta de Google con la que el equipo ya vigila el sistema, sin tener que revisar otra aparte. Su costo cabe en el presupuesto del proyecto.

Se descarta B porque todo el tráfico, con datos personales, pasaría por una empresa externa.

Se descarta C porque el equipo tendría que instalarlo y mantener sus reglas al día.

Se descarta D porque los ataques llegan hasta el sistema antes de ser frenados.

### Consecuencias

**Positivas**

1. Los ataques se frenan antes de llegar al sistema.
2. Google mantiene las reglas al día.
3. Los bloqueos se ven en el mismo monitoreo del sistema.
4. No se suma otro proveedor.

**Negativas**

1. Cuesta cerca de 96.000 COP al mes.
2. Puede bloquear por error textos normales, como títulos con comillas.

---

<a id="adr-033"></a>
## ADR-033 · Entrega de la aplicación web al navegador

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-02, US-03, RT-10, RT-21, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Ya se decidió que la parte visual es un proyecto separado del servidor, con qué tecnología se construye, y que todo el sistema vive en Google Cloud. Falta decidir quién le entrega al navegador los archivos de esa parte visual cada vez que alguien abre BookHive. Esa entrega debe verse en la dirección propia de BookHive y con conexión segura, y el presupuesto del proyecto establecido, el cual es de 8.000.000.

### Alternativas consideradas


**A.** Un paquete propio en Google Cloud Run, con un programa pequeño adentro que entrega los archivos. El equipo lo arma y lo mantiene; Google lo mantiene encendido. Cuesta unos 320.000 COP al año.

**B.** Un depósito de archivos de Google (Cloud Storage), donde se suben los archivos y Google se los entrega al navegador. El equipo solo sube los archivos. La dirección propia y la conexión segura se consiguen con un repartidor de tráfico que cuesta 50.000COP al mes.

**C.** Firebase Hosting, el servicio de Google hecho para publicar este tipo de aplicaciones. El equipo sube la carpeta y Google hace el resto, con la dirección propia y la conexión segura incluidas. Sin costo hasta 360 MB descargados por día; pasado eso, 473 COP por GB.

### Decisión

Se elige la opción B, Cloud Storage. El repartidor de tráfico ya se paga por el filtro de protección, así que entregar la aplicación por ahí no suma costo. El mismo repartidor entrega la aplicación y le pasa las solicitudes al servidor, así que el navegador ve un solo sitio.

Se descarta A porque cobra por mantener encendido un programa que solo entrega archivos que no cambian.

Se descarta C porque le pasa las solicitudes al servidor por un camino que no atraviesa el filtro de protección.

### Consecuencias


**Positivas**

1. No suma costo fijo: el repartidor ya se paga por el filtro de protección.
2. No hay ningún programa encendido que se pueda caer ni que haya que actualizar.
3. El navegador ve un solo sitio, así que la sesión guardada en la cookie funciona.
4. Todo el tráfico pasa por el filtro de protección.

**Negativas**

1. Si algún día se quita el filtro, el repartidor quedaría pagándose solo para entregar la aplicación.
4. La entrega queda atada a Google, así que cambiar de proveedor obliga a rehacer la publicación.

---

<a id="adr-034"></a>
## ADR-034 · Selección de la tecnología de cola de mensajes (message broker)

**Fecha:** 06/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-08, US-10, RF-19, RT-16  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cuando alguien cancela una reserva, hay que avisarle en menos de tres segundos a la siguiente persona de la fila, pero ese correo lo entrega un proveedor externo que puede tardar o estar caído. Por eso el sistema anota el aviso, confirma la cancelación de inmediato y deja que el correo se entregue aparte. Lo mismo pasa con otros trabajos que pueden hacerse después, como actualizar el buscador o las multas de cada universidad: deben quedar guardados hasta cumplirse, aunque el backend se apague en medio. Falta elegir con qué herramienta se organizan esos avisos, sin un costo que crezca sin control al entrar más instituciones.

### Alternativas consideradas

**A.** Google Cloud Pub/Sub. Servicio de avisos de la misma plataforma donde corre BookHive. Google lo administra. Reintenta por su cuenta los avisos que no logra entregar, aparta los que siguen fallando y puede respetar el orden de llegada. Se cobra por cantidad de avisos, con una cuota mensual gratuita; funciona únicamente dentro de esa nube.

**B.** RabbitMQ instalado por el equipo. Programa gratuito y muy usado, que el equipo instala, mantiene y tiene encendido todo el tiempo en una máquina propia: entre 25 y 45 dólares al mes, más el trabajo de administrarlo. Funciona con cualquier proveedor.

**C.** RabbitMQ administrado por otra empresa. El mismo RabbitMQ, instalado y mantenido por un proveedor externo. Se paga una tarifa mensual fija. Funciona con cualquier proveedor.

**D.** Apache Kafka. Programa gratuito diseñado para volúmenes de avisos muy grandes, con más piezas que configurar y mantener que las anteriores. El equipo lo instala y lo administra.

### Decisión

Se elige la opción A, Google Cloud Pub/Sub.

Es la única que no agrega nada que instalar ni mantener: ya viene con la plataforma donde el sistema funciona, y Google se encarga de tenerla andando. Trae hecho lo que BookHive necesita: reintentar los avisos que fallan, apartar los que no se logran entregar y respetar el orden cuando importa, así que no hay que construirlo. En el volumen previsto no tiene costo, y si algún día lo tiene, se cobra por cantidad de avisos y no por una máquina encendida haya o no movimiento.

Se descartan B y D porque exigen tener una máquina encendida todo el tiempo, que el equipo debe instalar, actualizar y vigilar: un gasto fijo y una pieza más que, si se cae, detiene todos los avisos del sistema. Kafka, además, está hecho para volúmenes muy superiores a los de BookHive.

Se descarta C porque quita el trabajo de mantenerlo pero cobra una mensualidad fija, y no ofrece nada que BookHive necesite y Pub/Sub no tenga.

### Consecuencias

**Positivas**

1. No hay nada que instalar, actualizar, respaldar ni vigilar: ese trabajo queda del lado de Google.
2. No tiene costo en el volumen previsto, y no hay una máquina cobrando aunque no haya avisos.
3. Un aviso que no se puede entregar en el momento no se pierde: espera en la fila y se reintenta solo, sin frenar la operación que lo originó.

**Negativas**

1. Ata esta parte del sistema a esa nube: mudarse a otro proveedor obligaría a reemplazarla.
2. Si el servicio falla, el equipo no puede reiniciarlo ni corregirlo: solo esperar a que Google lo resuelva.

---

<a id="adr-035"></a>
## ADR-035 · Selección de la tecnología de tareas programadas (Worker)

**Fecha:** 12/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RF-06, RF-07, RF-18, RF-19, RT-08  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El sistema ejecuta varias tareas por su cuenta, sin que nadie las dispare: avisar de un préstamo próximo a vencerse, cancelar las reservas que nadie recogió, actualizar las multas que crecen cada día. Falta decidir con qué herramienta se programan. Cada tarea debe ocurrir, y ocurrir una sola vez.

### Alternativas consideradas

**A.** @Scheduled. Viene incluido con la herramienta con la que está hecho el backend. Cada copia del backend dispara las tareas por su cuenta, a la hora programada en el código.

**B.** Quartz. Se agrega dentro del backend. Las tareas también las dispara el backend, pero las copias se coordinan entre ellas a través de unas tablas propias en la base de datos para que solo una las ejecute.

**C.** pg_cron. Es un agregado de la base de datos. Las tareas las dispara la propia base a la hora programada, y se escriben como instrucciones dentro de ella.

**D.** Cloud Scheduler. Es un servicio de la plataforma. Ese servicio dispara las tareas a la hora programada haciéndole una llamada al backend, que es quien las ejecuta. Las tres primeras tareas no tienen costo y cada tarea adicional cuesta cerca de diez centavos de dólar al mes.

### Decisión

Se elige la opción D, Cloud Scheduler.

Es la única que funciona con el entorno ya elegido. Como el backend se apaga cuando no hay solicitudes, nadie adentro puede disparar una tarea a las dos de la mañana: el aviso tiene que venir de afuera.

Y resuelve de paso el problema de las copias repetidas: aunque el backend esté funcionando en varias copias, la llamada del servicio llega a una sola, y es esa la que ejecuta la tarea. No hace falta que las copias se pongan de acuerdo entre ellas. Las reglas del negocio siguen escritas en el backend; el servicio solo dice cuándo. Con las cinco tareas del sistema, el costo ronda los 600 pesos al mes.

Se descartan A y B porque dependen del backend, que se apaga cuando no llega tráfico: la tarea de la madrugada simplemente no ocurriría, salvo dejando una copia encendida todo el tiempo, que es justo el costo que se evitó al elegir el entorno de ejecución. Quartz, además, exigiría tablas nuevas y que todas las copias tengan la misma hora.

Se descarta C porque obligaría a escribir dentro de la base de datos reglas que ya están en el backend: el cálculo de la multa, el avance de la fila de reservas, y habría que mantenerlas iguales en dos lugares.

### Consecuencias

**Positivas**

1. Cada tarea ocurre una sola vez, sin que las copias del backend tengan que ponerse de acuerdo entre ellas.
2. No hay nada nuevo que instalar ni tablas que crear: las reglas del negocio siguen donde ya estaban.
3. Las tareas ocurren aunque el sistema lleve horas apagado por falta de uso.
4. Si una ejecución falla, el servicio la vuelve a intentar por su cuenta.

**Negativas**

1. Es otro servicio que configurar y vigilar, y el backend debe exponer una entrada por cada tarea, protegida para que solo ese servicio pueda llamarla.
2. Cada tarea debe poder repetirse sin duplicar su efecto, porque un reintento puede ejecutarla dos veces.
3. Ata las tareas programadas a esa nube: mudarse a otro proveedor obligaría a rehacer esta parte.

---

<a id="adr-036"></a>
## ADR-036 · Componente de notificaciones (Notification gateway)

**Fecha:** 05/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-08, US-10, US-25, RF-19  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Varias partes del sistema necesitan avisarle algo a una persona: que su préstamo está por vencerse, que quedó de primero en la fila, que se generó una multa o que se recibió un pago. Hoy cada una tendría que resolver por su cuenta a quién le avisa, por qué medio y qué hacer si el aviso no sale. Falta decidir cómo se envían esos avisos y quién lleva la cuenta de lo que se envió.

### Alternativas consideradas

**A.** Firebase Cloud Messaging. Servicio de Google que entrega avisos al navegador usando el mecanismo propio de cada uno. No tiene costo ni límite de mensajes. El envío se programa desde el backend con su librería; la consola incluye además un compositor para enviar avisos sin programar.

**B.** OneSignal. Servicio especializado en notificaciones, con pantallas propias para redactar, programar y medir los envíos. Tiene un plan gratuito con límite de suscriptores y cobro mensual al superarlo.

**C.** Envío directo con el mecanismo del navegador. El backend guarda por su cuenta la autorización de cada navegador y le entrega el aviso al servicio de avisos del navegador correspondiente, sin ningún intermediario.

### Decisión

Se elige A, Firebase Cloud Messaging. Las tres son equivalentes en resiliencia y en seguridad; se diferencian en costo, operación, mantenimiento y cumplimiento.

En eficiencia de costos, no tiene costo ni límite de mensajes ni de usuarios, así que el gasto no crece al sumar instituciones.

En operabilidad, resuelve por su cuenta las diferencias entre los navegadores y los reintentos de entrega.

En cumplimiento, es de la misma nube donde ya corre el sistema, así que no se suma un tercero más con acceso a los datos de las personas.

El envío se programa desde el backend, dentro del componente de notificaciones, y el aviso en el navegador siempre acompaña al correo, nunca lo reemplaza.

Se descarta B porque su plan gratuito limita el número de personas suscritas y cobra al superarlo, y suma un proveedor más con acceso a los datos de contacto, a cambio de pantallas de redacción y medición que el sistema no necesita.

Se descarta C porque obligaría al equipo a manejar por su cuenta las diferencias entre navegadores, los reintentos y las autorizaciones vencidas, que es justamente lo que las otras dos ya traen resuelto.

### Consecuencias

**Positivas**

1. No tiene costo ni límite, sin importar cuántas instituciones entren.
2. Las diferencias entre navegadores y los reintentos los resuelve el servicio.
3. No se suma otro tercero con acceso a los datos de las personas.

**Negativas**

1. El aviso exige que la persona lo autorice.
2. El sistema debe guardar y mantener al día qué navegadores se le avisa a cada persona, y descartar los que dejan de servir.

---

<a id="adr-037"></a>
## ADR-037 · Selección del servicio de envío de correos electrónicos

**Fecha:** 04/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-10, RF-19, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive necesita enviar los correos automáticos del sistema (avisos de vencimiento, multas y reservas disponibles) sin generar un costo demasiado alto que el presupuesto fijo del proyecto no contempla, y sin depender de un plan de prueba con fecha de vencimiento.

### Alternativas consideradas

**A.** SendGrid. Proveedor especializado en envío de correos. Su plan gratuito es una prueba de 60 días con 100 correos diarios; después cuesta desde 19,95 dólares al mes.

**B.** Brevo. Proveedor especializado con plan gratuito permanente de 300 correos diarios (9.000 al mes), sin fecha de vencimiento y sin pedir tarjeta. Desde la misma cuenta se pueden enviar mensajes de texto.

**C.** Mailgun. Proveedor especializado con plan gratuito de 100 correos diarios. Los planes pagos parten en 15 dólares al mes.

**D.** Amazon SES. Servicio de envío de Amazon. No tiene plan gratuito de correos: da 200 dólares en créditos por seis meses, y después cobra desde 10 centavos por cada mil correos. Funciona dentro de la infraestructura de Amazon, que hay que configurar aparte.

### Decisión

Se elige la opción B, Brevo.

Su plan gratuito es permanente: 300 correos diarios, sin fecha de vencimiento, y con eso el proyecto no genera costo de envío. Desde la misma cuenta se pueden mandar mensajes de texto, por si más adelante se agrega ese canal.

Se descarta SendGrid porque su plan gratuito es una prueba de 60 días, y BookHive puede seguir en uso después del curso: depender de ese plazo es un riesgo que no hace falta correr.

Se descarta Mailgun porque su plan gratuito cubre un tercio de lo que cubre Brevo.

Se descarta Amazon SES porque exige montar infraestructura de Amazon que el proyecto no usa para nada más; sería la opción más barata si algún día el volumen crece lo suficiente para justificarla.

### Consecuencias

**Positivas**

1. El plan gratuito es permanente: 300 correos al día, unos 9.000 al mes, sin fecha de vencimiento, así que enviar los avisos del sistema no genera ningún costo.
2. Desde la misma cuenta se pueden mandar mensajes de texto, por si más adelante se agrega ese canal, sin contratar otro proveedor.
3. Cada correo enviado queda registrado del lado del proveedor con su propio identificador, así que ante un reclamo se puede demostrar qué salió y cuándo.

**Negativas**

1. Los 300 correos diarios son un tope: si algún día el sistema necesita enviar más, los que sobren quedan para el día siguiente.
2. Para que los correos salgan a nombre de BookHive y no lleguen como sospechosos, el proyecto necesita un dominio propio configurado con el proveedor. Con una cuenta gratuita de correo el envío funciona, pero sale sin la firma que respalda al remitente.
3. Las condiciones del plan gratuito las fija el proveedor y puede cambiarlas; si eso ocurre, habría que pasar a otro.

---

<a id="adr-038"></a>
## ADR-038 · Selección de la pasarela de pagos

**Fecha:** 05/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RF-08, RF-14, RT-09, RN-02, RN-09, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Se debe elegir qué pasarela de pagos implementar. La pasarela debe cubrir los medios que realmente usan los estudiantes y profesores, como tarjeta, cuenta bancaria, billeteras como Nequi y recibo para pagar en efectivo, y no puede tener costos excesivos mensuales, porque el presupuesto del proyecto no los contempla.

### Alternativas consideradas

**A.** Wompi. Empresa colombiana de pagos del grupo Bancolombia, el mismo grupo de Nequi. Publica sus tarifas y permite registrarse sin negociar. Cubre tarjeta, cuenta bancaria (PSE), Nequi y recibo para pagar en efectivo.

**B.** Mercado Pago. Empresa de pagos de Mercado Libre, presente en varios países de la región y con billetera propia. Cubre tarjeta, cuenta bancaria, efectivo y su billetera; recibir el dinero de inmediato tiene una tarifa mayor que recibirlo a los catorce días.

**C.** PayU. Empresa internacional presente en Colombia desde hace más de una década. Cubre tarjeta, cuenta bancaria y efectivo, y negocia sus tarifas según el volumen de transacciones del negocio.

**D.** ePayco. Empresa colombiana orientada a pequeñas y medianas empresas. Cubre tarjeta, cuenta bancaria y efectivo, con tarifas similares a las de Wompi.

**E.** PSE. No es una empresa de pagos sino el sistema con el que los bancos colombianos permiten pagar desde la cuenta; lo administra ACH Colombia. Vincularse directamente exige registrarse como entidad recaudadora y no cubre pagos con tarjeta.

### Decisión

Se elige la opción A, Wompi.

Pertenece al mismo grupo que Nequi, así que esa billetera queda disponible de forma directa, y es la manera en que paga buena parte de los estudiantes que no tienen cuenta bancaria. Publica sus tarifas y permite registrarse sin negociar un contrato, lo que se ajusta a un proyecto que todavía no tiene volumen de transacciones. No cobra costos fijos mensuales y ofrece un entorno de pruebas donde el equipo puede simular pagos sin dinero real antes de salir a producción.

Se descarta PayU porque negocia sus tarifas según el volumen de transacciones, un modelo pensado para negocios ya establecidos, y su tarifa de tarjeta es la más alta del grupo.

Se descarta Mercado Pago porque está orientada a su propio ecosistema de comercio electrónico y cobra más por entregar el dinero de inmediato en lugar de a los catorce días.

Se descarta ePayco porque, a tarifas equivalentes, Wompi ofrece el respaldo del banco más grande del país y la relación directa con Nequi. Es la alternativa más cercana y queda como reemplazo si esas condiciones cambian. Finalmente se descarta PSE porque vincularse directamente exige registrarse como identidad recaudadora y cumplir requisitos propios de una entidad financiera y aun así no cubriría los pagos con tarjeta

### Consecuencias

**Positivas**

1. Cubre tarjeta, cuenta bancaria, Nequi y recibo en efectivo, así que un estudiante puede pagar su multa aunque no tenga cuenta bancaria.
2. No hay mensualidad: si en un mes nadie paga una multa, el sistema no genera ningún costo.
3. El pago ocurre en la página del proveedor, así que BookHive nunca recibe el número de la tarjeta.
4. Cada transacción queda registrada también del lado del proveedor, lo que sirve de respaldo si un usuario reclama haber pagado.

**Negativas**

1. Las tarifas las fija el proveedor y pueden cambiar, así que conviene revisarlas de vez en cuando.
2. Cambiar de proveedor más adelante obliga a rehacer la integración, aunque el reemplazo más cercano ya quedó identificado.

---

<a id="adr-039"></a>
## ADR-039 · Evitar pagos y avisos duplicados (Idempotencia)

**Fecha:** 12/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-10, US-11, RF-08, RF-14, RT-09, RN-02  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Una misma operación puede llegarle al sistema más de una vez: la pasarela confirma un pago y vuelve a confirmarlo, el usuario hace doble clic, o una tarea programada se reintenta después de fallar. Falta decidir cómo se garantiza que, aunque llegue repetida, la operación se aplique una sola vez: que un pago no quede registrado dos veces ni un aviso se envíe repetido.

### Alternativas consideradas

**A.** No aplicar ningún control. Cada pago, aviso o tarea se procesa tal como llega.

**B.** Guardar un identificador único en la base de datos. Cada operación llega con un identificador propio, que se guarda en el mismo paso en que se aplica la operación. Si ese identificador ya existe, la operación no se vuelve a aplicar.

**C.** Guardar el identificador único en la caché. Cada operación llega con un identificador propio, que se guarda en la caché durante un tiempo. Si ya está ahí, la operación no se vuelve a aplicar.

**D.** Delegar el control al sistema de mensajería. Se configura ese sistema para que entregue cada mensaje una sola vez. El control queda a su cargo y aplica a los mensajes que pasan por él.

### Decisión

Se elige la opción B, guardar un identificador único en la base de datos.

Es la única en la que el identificador y la operación se guardan en el mismo paso: o quedan los dos, o no queda ninguno. Si el sistema falla en la mitad, no puede pasar que un pago quede registrado sin su identificador y termine aplicándose otra vez. Además sirve igual para pagos, avisos y tareas, con un solo mecanismo.

Se descarta A porque los pagos, los avisos y las tareas sí llegan repetidos, y nada lo impediría.

Se descarta C porque la caché guarda los datos de forma temporal y puede perderlos al reiniciarse: desaparecería el identificador de un pago ya registrado y ese pago podría volver a registrarse.

Se descarta D porque solo alcanzaría a los mensajes que pasan por el sistema de mensajería, y quedarían por fuera las confirmaciones de la pasarela de pagos y el doble clic del usuario.

### Consecuencias

**Positivas**

1. Un pago confirmado dos veces queda registrado una sola vez.
2. Un mismo mecanismo cubre pagos, avisos y tareas, sin una solución distinta para cada caso.
3. Queda constancia de qué operaciones llegaron repetidas, lo que sirve ante un reclamo.

**Negativas**

1. Cada operación necesita un identificador bien definido: si es muy general, bloquea operaciones que en realidad son distintas; si es muy específico, deja pasar repetidas.
2. Los identificadores se acumulan con cada operación, así que hay que definir cuánto tiempo se guardan.
3. Cuando llega una operación repetida, el sistema debe responder como si hubiera funcionado y no como error; si responde error, quien la envió seguirá reintentando.

---

<a id="adr-040"></a>
## ADR-040 · Comportamiento del sistema ante fallas de los servicios externos (Circuit breaker)

**Fecha:** 19/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-10, US-11, US-24, RT-16  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive depende de servicios que no controla. Esos servicios no siempre fallan de inmediato: a veces se demoran en responder. Mientras tanto, las solicitudes se quedan esperando y ocupan recursos. Falta decidir cómo responde BookHive cuando un servicio externo no contesta a tiempo.

### Alternativas consideradas

**A.** Esperar la respuesta. Cada llamada espera hasta que el servicio externo conteste.

**B.** Tiempo máximo de espera con reintentos. Cada llamada tiene un límite de espera; si se pasa, se da por fallida y se vuelve a intentar un número definido de veces.

**C.** Tiempo máximo de espera con corte temporal. Además del límite, el sistema cuenta los fallos seguidos de cada servicio. Si pasan de cierto número, deja de llamarlo durante un rato y responde de inmediato con lo que haya definido para ese caso. Pasado ese rato prueba con una llamada suelta y, si responde, vuelve a la normalidad.

### Decisión

Se elige la opción C, tiempo máximo de espera con corte temporal.

Las tres son equivalentes en seguridad y cumplimiento; se diferencian en resiliencia, operación y mantenimiento.

En resiliencia, la falla de un servicio externo queda contenida en la función que lo usa y no arrastra al resto del sistema.

En operabilidad, la persona recibe una respuesta clara en el momento, en lugar de una pantalla que se queda esperando.

Queda definido que cada servicio externo tiene un tiempo máximo de espera y una respuesta propia para cuando no esté disponible, que los reintentos solo se aplican a operaciones que se pueden repetir sin duplicar su efecto, y que cada corte queda registrado y genera una alerta al equipo.

Se descarta A porque la demora de un tercero se convierte en demora de todo el sistema.

Se descarta B porque reintentar contra un servicio caído multiplica las llamadas y alarga la espera en lugar de acortarla.

### Consecuencias

**Positivas**

1. La falla de un servicio externo no detiene las operaciones que no dependen de él.
2. La persona recibe una respuesta clara en vez de quedarse esperando.
3. No se paga tiempo de ejecución esperando respuestas que no llegan.
4. Cada corte queda registrado y el equipo se entera.

**Negativas**

1. El tiempo de espera, el número de fallos que activan el corte y su duración hay que ajustarlos con el uso real.
2. Cada llamada a un servicio externo obliga a definir qué se responde cuando no está disponible.

---

<a id="adr-041"></a>
## ADR-041 · Entrega de los archivos que el sistema genera

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-18, US-32, RN-02  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive entrega archivos a sus usuarios: el comprobante de un pago, los reportes que descarga el bibliotecario y el recibo con el que se paga una multa en efectivo. Falta decidir si esos archivos se guardan en algún lado o se generan cada vez que alguien los pide.

### Alternativas consideradas

**A.** Generarlos en el momento. Cada vez que alguien pide un comprobante o un reporte, el sistema lo arma a partir de los datos y lo entrega para descarga, sin guardarlo. El recibo para pagar en efectivo lo genera la pasarela, y el sistema guarda solo el código para volver a pedirlo.

**B.** Guardarlos en un servicio de almacenamiento de archivos. Cada archivo se guarda cuando se genera, y en la base queda la dirección donde encontrarlo. Cuesta cerca de dos centavos de dólar por gigabyte al mes, con los primeros cinco gigabytes sin costo.

**C.** Mixto. Se genera al momento lo que se puede reconstruir y se guarda solo lo que no.

### Decisión

Se elige la opción A, generar los archivos en el momento.

El comprobante de pago y los reportes se arman con los datos guardados cada vez que alguien los pide, y se entregan para descarga sin quedar almacenados. Como el historial no se puede alterar, volver a generar un comprobante da exactamente el mismo documento que la primera vez. El recibo para pagar en efectivo lo genera la pasarela, así que el sistema guarda solo el código con el que se vuelve a pedir. La única excepción es la entrega de datos de una institución que cancela el servicio, que por su tamaño se arma aparte y se guarda por un tiempo corto.

Se descarta B porque guardar esos archivos sería tener dos veces la misma información: el documento y los datos de los que salió. Además, un comprobante guardado conserva el nombre de la persona, y quedaría por fuera del proceso de anonimización.

Se descarta C porque, al no haber ningún archivo que no se pueda reconstruir, no queda nada que justifique el almacenamiento.

### Consecuencias

**Positivas**

1. No se contrata ningún servicio de almacenamiento para los comprobantes ni los reportes.
2. El documento siempre coincide con los datos, porque sale de ellos en el momento.
3. No quedan archivos sueltos con datos personales después de anonimizar una cuenta.
4. No hay que decidir cuánto tiempo se conservan los archivos ni limpiarlos después.

**Negativas**

1. Cada descarga vuelve a armar el documento, así que un reporte pesado consume tiempo de procesador cada vez que alguien lo pide.
2. Si el formato del comprobante cambia, los comprobantes viejos se descargarán con el formato nuevo, no con el que tenían cuando se emitieron.

---

<a id="adr-042"></a>
## ADR-042 · Selección de la herramienta de integración y despliegue continuo

**Fecha:** 12/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** US-31, RT-13, RT-14  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cada cambio en el código puede afectar reglas críticas del sistema, como el bloqueo de préstamos o el cálculo de multas, y revisarlas a mano antes de cada publicación es lento y deja pasar errores. Falta elegir una herramienta que ejecute las pruebas por sí sola antes de publicar cada versión, sin sumar un servidor más que instalar y mantener y sin costos que el presupuesto fijo no contempla.

### Alternativas consideradas

**A.** GitHub Actions. Herramienta incluida dentro de GitHub. Ejecuta las pruebas y el despliegue automáticamente cada vez que alguien sube código. Su plan gratuito da 2.000 minutos de ejecución al mes en repositorios privados, y minutos ilimitados si el repositorio es público.

**B.** GitLab CI/CD. Herramienta incluida dentro de GitLab. Hace lo mismo, pero exige que el código esté alojado en GitLab. Su plan gratuito da 400 minutos de ejecución al mes.

**C.** Jenkins. Programa de código abierto que el equipo instala en su propio servidor. No tiene límite de minutos ni costo de licencia, y funciona con el código alojado en cualquier lado.

### Decisión

Se elige la opción A, GitHub Actions. El código del proyecto ya está en GitHub, así que la herramienta viene incluida ahí: no hay que instalar nada, montar otro servidor ni crear cuentas aparte. Ejecuta las pruebas por sí sola cada vez que alguien sube un cambio y, al ser el repositorio público, no tiene límite de uso ni costo.

Se descarta GitLab CI/CD porque obligaría a mover todo el código a GitLab sin dar nada a cambio que GitHub Actions no haga ya, y su plan gratuito da menos minutos al mes.

Se descarta Jenkins porque hay que instalarlo y mantenerlo en un servidor propio: más memoria ocupada y más horas del equipo, un costo que no se justifica cuando la herramienta que ya viene con el repositorio hace lo mismo.

### Consecuencias

**Positivas**

1. No hay nada nuevo que instalar ni mantener: la herramienta ya viene donde está el código.
2. Las pruebas corren solas con cada cambio, y ninguna versión se publica si alguna falla.
3. Las pruebas se ejecutan contra una base de datos PostgreSQL que se crea solo para esa prueba, lo que permite probar de verdad los bloqueos del préstamo y de la fila de reservas.
4. Al ser el repositorio público, no hay límite de uso ni costo.

**Negativas**

1. Mantener el uso sin límite obliga a que el repositorio siga siendo público, así que cualquier clave que se suba por error queda expuesta de inmediato, y toda la configuración sensible debe vivir fuera del código.
2. Ata el proyecto a GitHub: mover el código a otro lado obligaría a rehacer toda la configuración.
3. Es el plan gratuito de una empresa: GitHub puede cambiar qué incluye ese plan cuando quiera.

---

<a id="adr-043"></a>
## ADR-043 · Almacenamiento de las claves de acceso a servicios (Key Vault)

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-09, RT-18, RT-21, RN-03  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive maneja claves que dan acceso a cosas serias: la contraseña de la base de datos, las claves de la pasarela de pagos y del servicio de correo, y la cuenta que aplica los cambios de estructura. Ya está decidido que esas claves no van dentro del paquete del sistema, y que el código vive en un repositorio público. Falta decidir dónde se guardan y cómo las obtiene el sistema cuando las necesita.

### Alternativas consideradas

**A.** En el código o en archivos del proyecto. Las claves se escriben junto al código y viajan con él a donde vaya.

**B.** En la configuración del servicio donde corre el backend. Las claves se escriben como valores de configuración de ese servicio, y el sistema las lee al arrancar.

**C.** En el servicio de secretos de la plataforma. Un servicio dedicado las guarda cifradas. Cada pieza del sistema pide solo las que le corresponden, y queda registro de quién pidió cuál. Las seis primeras claves no tienen costo; cada una adicional cuesta seis centavos de dólar al mes.

**D.** Un baúl instalado por el equipo. Programa especializado que el equipo instala y mantiene encendido en una máquina propia.

### Decisión

Se elige la opción C, el servicio de secretos de la plataforma.

Las claves quedan cifradas y fuera del código, y cada pieza recibe solo las suyas: el backend no puede leer la clave que se usa para cambiar la estructura de la base, ni al revés. Cambiar una clave no obliga a publicar una versión nueva del sistema, y queda registro de quién la pidió y cuándo. Con las claves que BookHive necesita, el costo ronda los doce centavos de dólar al mes.

Se descarta A porque el repositorio es público: una clave escrita ahí queda expuesta desde que se sube, y borrarla después no sirve, porque permanece en el historial.

Se descarta B porque cualquiera con acceso a la consola del proyecto las ve escritas tal cual, y se copian solas cada vez que alguien exporta o duplica la configuración del servicio.

Se descarta D porque exigiría una máquina encendida todo el tiempo y el trabajo de mantenerla, para hacer lo que la plataforma ya ofrece.

### Consecuencias

**Positivas**

1. Ninguna clave vive en el código ni viaja dentro del paquete.
2. Cada pieza del sistema recibe solo las claves que le corresponden.
3. Cambiar una clave no obliga a publicar una versión nueva.
4. Queda registro de quién pidió cada clave y cuándo.

**Negativas**

1. Si el servicio de secretos no responde cuando una pieza arranca, esa pieza no arranca.
2. Hay que definir quién puede leer cada clave y revisarlo cada vez que cambia el equipo.
3. Cambiar las claves periódicamente sigue siendo trabajo manual: el servicio guarda las versiones, pero alguien tiene que generarlas y actualizarlas en cada proveedor.

---

<a id="adr-044"></a>
## ADR-044 · Selección de la herramienta de monitoreo del sistema (Monitoreo / instrumentación)

**Fecha:** 18/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-16, US-25, RT-15  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El sistema tiene fallas que ocurren en silencio: un cambio que no llega al buscador, un aviso que no se entrega, una tarea que no se ejecuta. Además, debe quedar registro de quién entra y de qué hace cada cuenta. Falta elegir con qué herramienta se vigila todo eso.

### Alternativas consideradas

**A.** Cloud Monitoring y Cloud Logging. Servicios de Google que reúnen las mediciones, los registros y las alertas, y reciben por sí solos lo que reportan los demás servicios de la plataforma. Tienen un tramo gratuito mensual y cobran por volumen por encima de él.

**B.** Grafana Cloud. Servicio administrado que reúne lo mismo en tableros propios y se conecta a la plataforma para obtener los datos. Tiene un plan gratuito con límites de volumen y de conservación.

**C.** Datadog. Servicio administrado especializado, que se conecta a la plataforma para obtener los datos. Cobra por cada servicio vigilado.

### Decisión

Se elige la opción A, Cloud Monitoring y Cloud Logging.

Reciben por sí solos lo que reportan los demás servicios de la plataforma, sin instalar nada; solo la máquina del buscador necesita un agente. Las mediciones de esos servicios no se cobran y los registros son gratuitos hasta un volumen mensual que el sistema no alcanza.

Todo queda guardado dentro de la misma plataforma, sin sumar un tercero con acceso a los registros, que incluyen quién entra y qué solicita cada cuenta. Y como el sistema reporta sus mediciones en un formato abierto, cambiar de herramienta más adelante no obligaría a tocar el código.

Se descarta B porque suma otro proveedor para hacer lo que la plataforma ya ofrece. Se descarta C porque cobra por cada servicio vigilado, lo que excede el presupuesto.

### Consecuencias

**Positivas**

1. Las fallas que antes ocurrían en silencio ahora avisan: avisos sin entregar, cambios que no llegan al buscador, tareas que no se ejecutan.
2. Queda registro de quién entra, de qué solicita cada cuenta y de cada cambio de configuración hecho por el equipo.
3. Con el volumen inicial no genera ningún cobro.
4. Cambiar de herramienta más adelante no obliga a tocar el código.

**Negativas**

1. Por encima del tramo gratuito se paga por volumen de registros.
2. Guardar registros por más de 30 días se cobra aparte, y los plazos de cada tipo ya se encuentran definidos en el ADR-048.

---

<a id="adr-045"></a>
## ADR-045 · Inmutabilidad del historial transaccional y de los registros de auditoría de seguridad

**Fecha:** 06/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-20, RF-17, RT-03, RN-02, RN-05  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive guarda dos tipos de registros que no se pueden alterar después de creados: el historial de préstamos, devoluciones, multas y pagos, y los eventos de seguridad, como los accesos denegados o los cambios de configuración. Permitir que se edite abriría la puerta a manipular deudas. El segundo pierde todo su valor como prueba si alguien puede modificarlo. Falta decidir cómo se garantiza que ninguno de los dos se pueda alterar.

### Alternativas consideradas

**A.** Por acuerdo del equipo. No hay nada en el sistema que impida modificar o borrar esos registros: depende de que nadie lo haga.

**B.** Restringido desde la aplicación. El programa no ofrece ninguna forma de modificar ni borrar, pero quien entre directamente a la base de datos sí puede.

**C.** Restringido en la base de datos y en el servicio de registros. A la aplicación se le quita la posibilidad de modificar y borrar: solo puede agregar.

**D.** Permitir cambios guardando cada versión. El valor actual se puede editar, y cada cambio queda copiado aparte..

### Decisión

Se elige la opción C, restringida en el propio almacén.

El código no ofrece ninguna forma de modificar ni borrar esos registros, y además el lugar donde viven tampoco lo permite, sin importar lo que haga el código: quedan dos barreras, y la definitiva es la segunda.

En la base de datos, donde vive el historial, se aplica quitándole a la aplicación el permiso de modificar y borrar esas tablas. En el servicio de registros, donde viven los eventos de seguridad, no hay que configurar nada: ese servicio no permite alterar un evento ya escrito, solo agregar nuevos y fijar cuánto tiempo se conservan.

Se descartan A y B porque dejan la garantía en manos del código o de la disciplina del equipo: un error de programación o un acceso directo a la base bastan para romperla.

Se descarta D porque el valor actual seguiría siendo editable, y si el mecanismo que guarda las versiones se desactiva, nadie lo nota.

### Consecuencias

**Positivas**

1. Si el código llegara a intentar modificar un registro, el almacén lo rechaza igual: la protección no depende de una sola capa.
2. El historial y los eventos de seguridad sirven como prueba ante un reclamo, precisamente porque nadie pudo alterarlos.
3. Cumple la Ley 527 en integridad de los registros de pago y la Ley 1273 en protección de la información.

**Negativas**

1. La restricción se aplica distinto en cada almacén, así que hay que verificar en cada uno que de verdad rechaza los cambios.
2. Un registro con un error no se puede corregir ni borrar: queda tal como se escribió.

---

<a id="adr-046"></a>
## ADR-046 · Registro y trazabilidad de las operaciones de negocio

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-20, US-33, RT-03, RN-02  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El historial de préstamos, devoluciones, multas y pagos debe servir como evidencia ante un reclamo de un usuario, de una institución o de una autoridad. Ya está definido que ese historial no se puede modificar ni borrar. Falta decidir dónde vive.

### Alternativas consideradas

**A.** En la misma base de datos del negocio. El historial se guarda junto a los préstamos, las multas y los pagos, en tablas donde solo se puede agregar.

**B.** En una base de datos aparte. El historial vive en su propia base, dentro de la misma plataforma, con sus propios permisos y su propio plazo de conservación.

**C.** En un servicio externo especializado en registros que no se alteran. Un tercero guarda el historial y ofrece herramientas para comprobar que nada cambió.

### Decisión

Se elige A. Las tres son equivalentes en seguridad, porque lo que impide alterar un registro son los permisos, no el lugar donde vive; se diferencian en costo, operación, cumplimiento y resiliencia.

En eficiencia de costos, no se suma una base más que administrar, respaldar y vigilar.

En operabilidad, el historial siempre se consulta junto al estado actual de la operación, así que vivir con ella evita cruzar dos bases para responder una sola pregunta.

En cumplimiento, la información financiera y personal se queda donde ya está, sin sumar a un tercero ni una transferencia de datos adicional.

Se descarta B porque agrega una base más que administrar sin resolver ningún riesgo de que los permisos no cubran, y al estar en la misma plataforma tampoco protege frente a un incidente grave.

Se descarta C porque sacaría información financiera y personal hacia otro tercero, con las implicaciones legales de una transferencia de datos, a cambio de una garantía que los permisos ya dan.

### Consecuencias

**Positivas**

1. No se agrega una base que administrar, respaldar ni vigilar.
2. Consultar una operación junto con su historial es directo.
3. La información se queda bajo el control del proyecto, sin sumar a un tercero.

**Negativas**

1. Un problema en la base afecta al mismo tiempo la operación diaria y la evidencia de los cobros.
2. El historial nunca se borra, así que crece de forma indefinida dentro de la misma base y es necesario depurar.

---

<a id="adr-047"></a>
## ADR-047 · Registro de auditoría y trazabilidad de eventos, elección entre tecnología propia o servicio externo

**Fecha:** 05/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-14, US-20, RF-17, RT-15, RN-03  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

BookHive debe registrar los eventos de seguridad y de administración: intentos de acceso fallidos, bloqueos de cuenta, cambios de configuración, para poder auditar un incidente o demostrar cumplimiento ante una institución. Ya se definió que esos registros no se pueden modificar ni borrar. Falta decidir si esa herramienta se construye desde cero o se usa una ya existente.

### Alternativas consideradas

**A.** Desarrollo propio (construir desde cero). El equipo diseña y programa su propia herramienta para capturar, almacenar y analizar los eventos de seguridad, sin apoyarse en ningún software ya existente para esta función. Da control total, pero exige construir desde cero capacidades como indexación eficiente, búsqueda rápida sobre grandes volúmenes y paneles de análisis.

**B.** Tecnología ya existente (autoalojada o como servicio externo). Se usa una herramienta especializada ya construida, ya sea instalada y administrada por el propio equipo o contratada como servicio administrado por un tercero, en vez de programar esa lógica desde cero.

### Decisión

Se elige la opción B: usar una herramienta ya existente.

Construirla desde cero significaría rehacer capacidades que ya están resueltas y probadas: guardar grandes volúmenes de eventos, buscar rápido entre ellos, mostrar patrones, con meses de trabajo y sin ninguna ventaja frente a adoptarlas. Además, una herramienta hecha en casa arrastraría errores que las existentes ya corrigieron hace años.

### Consecuencias

**Positivas**

1. Se aprovechan funciones ya construidas y probadas: guardar, buscar, detectar patrones, sin invertir tiempo de desarrollo en rehacerlas.
2. El equipo dedica su tiempo a las funcionalidades propias de BookHive y no a construir un sistema de registro de eventos.
3. Ante un reclamo, encontrar el rastro de lo que pasó toma minutos, porque la búsqueda entre miles de eventos ya viene resuelta.

**Negativas**

1. La herramienta solo guarda y busca los eventos; detectar que algo ocurrió y armar el registro con quién lo hizo, cuándo y con qué resultado sigue siendo trabajo del diseño de BookHive.
2. Aunque la licencia sea gratuita, la herramienta consume memoria y disco, y ese costo hay que contemplarlo en la infraestructura.

---

<a id="adr-048"></a>
## ADR-048 · Selección de tecnología de auditoría y logs de eventos

**Fecha:** 12/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-20, US-33, RF-17, RT-15, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Se requiere una tecnología que capture, almacene y analice los eventos de auditoría y seguridad, permitiendo buscar rápidamente en un volumen creciente de eventos, sin incurrir en un costo que el presupuesto fijo del proyecto no contemple.

### Alternativas consideradas

**A.** OpenSearch. Motor de código abierto, sin costo de licencia, que el equipo instala y mantiene en su propia máquina. Guarda el contenido completo de cada evento y permite buscarlo. Incluye paneles de visualización.

**B.** Graylog. Plataforma de código abierto para administrar registros. Para funcionar necesita, además, un OpenSearch para guardar y buscar los eventos y una base de datos MongoDB para su propia configuración.

**C.** Grafana Loki. Sistema de código abierto para reunir registros. Guarda el contenido de cada evento, pero solo permite buscar por las etiquetas que lo acompañan, no por lo que dice el mensaje.

**D.** Cloud Logging. Servicio de la plataforma donde ya corre el sistema, administrado por Google. Recibe los eventos, los guarda sin que nadie pueda alterarlos y permite buscarlos. Tiene una cuota gratuita mensual y cobra por volumen por encima de ella.

### Decisión

Se elige la opción D, Cloud Logging.

Es el mismo servicio que ya se eligió para vigilar el sistema, así que los eventos de auditoría no exigen una herramienta aparte. No hay nada que instalar ni una máquina encendida que pagar, y en el volumen del proyecto no genera costo. Los eventos quedan guardados sin que nadie pueda modificarlos ni borrarlos, y se pueden buscar y conservar por el tiempo que se defina.

Los eventos registran el número de cuenta de la persona, nunca su nombre ni su correo. La única excepción es cuando alguien intenta entrar con una cuenta que no existe: ahí no hay número que guardar y solo quedan el correo escrito y el origen de la solicitud. Esos eventos se conservan por un plazo corto, el mínimo para detectar un ataque, y se descartan al vencerse.

Se descarta A porque exigiría una máquina propia encendida todo el tiempo, con el mayor costo de infraestructura del proyecto, para algo que se consulta ocasionalmente.

Se descartan B y C porque suman piezas o limitan la búsqueda, y ninguna resuelve nada que Cloud Logging no cubra ya.

### Consecuencias

**Positivas**

1. No hay nada que instalar ni una máquina encendida que pagar: es el mismo servicio que ya vigila el sistema.
2. En el volumen del proyecto no genera costo.
3. Los eventos quedan guardados sin que nadie pueda modificarlos ni borrarlos.
4. Vigilancia y auditoría quedan en un solo lugar, así que ante un incidente no hay que cruzar dos herramientas.

**Negativas**

1. Por encima de la cuota gratuita se paga por volumen, y los intentos fallidos, que suelen ser muchos, lo hacen crecer de forma constante.
2. Los registros se conservan según su tipo: 30 días los técnicos y los intentos de entrada con correo suelto, y un año los eventos de seguridad y administración. Conservar más de 30 días tiene un costo por volumen.
3. Las búsquedas y los paneles son los que ofrecen el servicio; un análisis más elaborado quedaría limitado a lo que permita.

---

<a id="adr-049"></a>
## ADR-049 · Herramienta de pruebas unitarias del backend

**Fecha:** 06/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-13, RT-14, RF-10, RF-15, RF-21  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Las reglas del negocio, como el cálculo de multas, el orden de la fila de reservas o el límite de préstamos, quedaron separadas de la base de datos y de los servicios externos, así que se pueden probar solas. Cada vez que alguien sube un cambio, las pruebas se ejecutan de forma automática y ninguna versión se publica si alguna falla. Falta decidir con qué herramienta se escriben esas pruebas, teniendo en cuenta que el backend está hecho en Java con Spring Boot y que el presupuesto es fijo.

### Alternativas consideradas

**A.** JUnit 5 con Mockito. Herramienta estándar para probar código Java; Mockito reemplaza durante la prueba la base de datos y los servicios externos por piezas simuladas. Viene incluida con Spring Boot y el equipo escribe las pruebas en Java. Es gratuita.

**B.** TestNG con Mockito. Herramienta para probar código Java, con opciones para agrupar pruebas y ejecutarlas en paralelo. Se agrega aparte al proyecto y el equipo escribe las pruebas en Java. Es gratuita.

**C.** Spock. Herramienta en la que cada prueba se escribe como un escenario de "dado, cuando, entonces". Se agrega aparte al proyecto y el equipo escribe las pruebas en Groovy, un lenguaje distinto al del backend. Es gratuita.

**D.** Pruebas manuales. Antes de cada publicación, una persona del equipo revisa a mano los casos importantes siguiendo una lista. No usa ninguna herramienta; cuesta horas del equipo en cada publicación.

### Decisión

Se elige la opción A, JUnit 5 con Mockito. Viene incluida con Spring Boot, así que no hay nada que agregar ni configurar, y las pruebas se escriben en el mismo lenguaje del backend. Con las piezas simuladas, cada regla del negocio se prueba sin encender la base de datos ni llamar a la pasarela de pagos, así que las pruebas corren en segundos con cada cambio. No tiene costo.

Se descarta B porque hace lo mismo, pero hay que agregarla aparte sin que ofrezca nada que este proyecto necesite.

Se descarta C porque obliga a escribir las pruebas en un lenguaje distinto al del backend, que el equipo tendría que aprender.

Se descarta D porque no se puede ejecutar de forma automática con cada cambio, y depende de que alguien se acuerde de revisar cada caso.

### Consecuencias


**Positivas**

1. Las pruebas corren en segundos, sin base de datos ni servicios externos.
2. No hay que instalar ni configurar nada: viene con Spring Boot.
3. Las pruebas se escriben en Java, el mismo lenguaje del backend.
4. Un error en una regla del negocio se detecta antes de publicar la versión.

**Negativas**

2. Una pieza simulada que no se comporte como el servicio real puede ocultar un error.
3. Escribir y mantener las pruebas toma tiempo del equipo en cada funcionalidad nueva.

---

<a id="adr-050"></a>
## ADR-050 · Entrega de los datos de una institución que cancela su membresía

**Fecha:** 20/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-34, RF-20, RN-01  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cuando una institución cancela, BookHive debe devolverle sus datos antes de borrarlos. Falta decidir en qué forma se entregan. La institución debe poder cargarlos en otro sistema sin arreglarlos a mano, y no pueden traer datos de otra universidad.

### Alternativas consideradas

**A.** Archivos en formato abierto, uno por cada tipo de dato, con la explicación de cada columna publicada desde antes. Los arma el sistema y la institución los descarga. Sin costo.

**B.** Una copia de la base de datos tal como está guardada. La arma el equipo y solo se abre con el mismo motor de base de datos. Sin costo.

**C.** Una conexión por la que el nuevo sistema de la institución pide los datos. La construye el equipo y la institución la usa desde su nuevo sistema. No se paga ningún servicio; el costo es construirla.

### Decisión

Se elige la opción A, archivos en formato abierto. Cualquier sistema puede leerlos, y como la explicación de las columnas está publicada desde antes, la institución no necesita ayuda de BookHive para cargarlos. Se arman aparte, sin que nadie espere en pantalla, leyendo solo los datos de esa institución, y quedan en un depósito de archivos de Google durante 30 días.

Se descarta B porque solo sirve si el nuevo sistema usa el mismo motor de base de datos.

Se descarta C porque obliga a la institución a construir algo para recibir unos datos que se llevan una sola vez.

### Consecuencias

**Positivas**

1. La forma de los archivos se puede prometer en el contrato.
2. El administrador los descarga desde la misma BookHive, que con la membresía cancelada solo le muestra esa opción, con un enlace que vence en minutos.
3. Ningún dato de otra universidad se cuela en la entrega.

**Negativas**

1. Con una institución grande, armar los archivos toma tiempo.
2. Si cambia la forma de los datos, hay que actualizar la explicación publicada.
3. Los archivos se borran solos a los 30 días; si la institución no los descarga a tiempo, se pierden.

---

<a id="adr-051"></a>
## ADR-051 · Selección del estilo arquitectónico del backend

**Fecha:** 25/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-16, US-24, US-31, US-32, US-26, RT-01, RT-04, RF-02, RT-16, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Ya se definió dónde funciona el backend y cómo se ordenan por dentro sus reglas del negocio. Falta decidir en cuántas piezas se divide y cómo se publica. La opción elegida debe garantizar que un préstamo se registre completo o no se registre, que dos personas puedan publicarla y vigilarla con el presupuesto fijo, y que el sistema siga atendiendo aunque se caiga una máquina o un servicio externo.

### Alternativas consideradas

**A.** Monolito modular. Una sola pieza, dividida por dentro en partes del negocio: circulación, catálogo, pagos, instituciones y notificaciones. Las partes se comunican dentro del mismo programa y usan una sola base de datos. Hay una pieza que publicar y vigilar.

**B.** Arquitectura basada en servicios. Cinco piezas separadas, una por cada parte del negocio. Comparten la misma base de datos y se avisan entre sí por mensajes. Hay cinco piezas que publicar y vigilar.

**C.** Microservicios. Muchas piezas pequeñas, cada una con su propia base de datos, que se comunican por la red. El préstamo y el inventario quedan en piezas y bases distintas. Hay una pieza y una base por cada parte que publicar y vigilar.

### Decisión

Se elige la opción A, monolito modular. Todo el sistema queda en una sola pieza con una sola base de datos. Así, un préstamo y el descuento del inventario se guardan juntos, o no se guarda ninguno. Una sola pieza que el equipo puede mantener con el presupuesto fijo. Y si se cae una máquina o un servicio externo, el resto del sistema sigue atendiendo.

Se descarta B porque son cinco piezas que mantener en lugar de una.

Se descarta C porque el préstamo y el inventario quedan en bases distintas, y uno podría guardarse sin el otro.

### Consecuencias

**Positivas**

1. Las partes se comunican dentro del mismo programa, así que las respuestas son más rápidas.
2. Probar el sistema y encontrar un error se hace en un solo lugar.
3. Cada parte solo cambia sus propios datos, así que una no daña la información de otra.
4. Si algún día una parte necesita funcionar aparte, se puede separar sin rehacer el resto.

**Negativas**

1. Un error grave que tumbe el programa detiene todas las partes al tiempo; un error en una sola operación no afecta a las demás.
2. No se puede aumentar la capacidad de una sola parte: crece todo junto.
3. Un cambio pequeño en una parte obliga a publicar el sistema completo.

---

<a id="adr-052"></a>
## ADR-052 · Estilo de comunicación usado

**Fecha:** 25/09/2026  
**Estado:** Rechazado  
**Drivers de arquitectura:** RT-05, US-01, US-04, US-14, RF-12, RF-23, US-18, RT-09, US-11, RF-05  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Las solicitudes ya tienen un recorrido definido: salen de la aplicación web, pasan por el filtro de protección y la puerta de entrada, y llegan al backend. Falta decidir en qué forma se piden y se responden. La forma elegida debe permitir controlar por separado quién usa cada operación y cuántas veces, cargar las pantallas que reúnen datos de varias partes sin hacer muchas llamadas, y recibir los avisos de la pasarela de pagos.

### Alternativas consideradas

**A.** REST. Una dirección por cada operación. El backend decide qué datos devuelve cada una, así que una pantalla que reúne datos de varias partes hace una llamada por cada parte.

**B.** GraphQL. Una sola dirección para todas las operaciones. La aplicación web pide exactamente los datos que necesita, en una sola consulta.

**C.** REST y GraphQL combinados. REST para las operaciones que cambian datos y para los avisos de pagos. GraphQL solo para consultar las pantallas que reúnen datos de varias partes.

### Decisión

Se elige la opción C, REST y GraphQL combinados. Las operaciones que cambian datos van por REST, cada una con su propia dirección, así que se puede controlar por separado quién las usa y cuántas veces. Por ahí mismo llegan los avisos de la pasarela de pagos. GraphQL se usa solo para consultar, y carga en una sola consulta las pantallas que reúnen datos de varias partes.

Se descarta A porque cada pantalla que reúne datos de varias partes obliga a hacer una llamada por cada parte.

Se descarta B porque todas las operaciones entran por la misma dirección, y no se puede controlar cada una por separado.

### Consecuencias

**Positivas**

1. Cada operación que cambia datos se controla por separado: quién la usa y cuántas veces.
2. Las pantallas que reúnen datos de varias partes se cargan con una sola consulta.
3. La aplicación web recibe solo los datos que muestra, sin datos de más.
4. Los avisos de la pasarela de pagos llegan por REST, en la misma forma en que ella los envía.

**Negativas**

1. El equipo debe manejar dos formas de comunicación en el mismo sistema.
2. Cada consulta de GraphQL debe filtrar por la institución del usuario, para que nadie vea datos de otra.

---

<a id="adr-053"></a>
## ADR-053 · Manejo del crecimiento de las tablas de historial

**Fecha:** 27/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-03, RT-24, US-12, US-18, US-20, US-33, US-26, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Las tablas de historial (préstamos, multas, pagos y avisos enviados) solo reciben registros nuevos y nunca se modifican, así que crecen todos los días. Falta decidir cómo manejarlas para que no se vuelvan lentas. La opción elegida debe mantener rápidas las consultas por fecha, permitir retirar los registros viejos sin afectar los recientes y no sumar costo.

### Alternativas consideradas

**A.** Una sola tabla con índice por fecha. Cada historial es una tabla única, y un índice ayuda a encontrar los registros por fecha. Los registros viejos se retiran borrándolos uno por uno.

**B.** Partición por periodo. Cada tabla se divide por dentro en una parte por mes, en el mismo servidor, y el sistema la sigue viendo como una sola tabla. Un mes viejo se retira quitando su parte completa.

**C.** Archivar lo viejo fuera de la base. Los registros antiguos se trasladan cada cierto tiempo a un almacenamiento aparte y salen de la base. Consultarlos exige buscarlos en ese otro lugar.

### Decisión

Se elige la opción B, partición por periodo. Cada consulta por fecha revisa solo los meses que necesita, y un mes viejo se retira completo sin tocar los recientes. Todo ocurre en el mismo servidor, sin costo adicional.

Se descarta A porque borrar registros viejos uno por uno frena la tabla mientras se borran.

Se descarta C porque una auditoría o un reclamo de meses atrás obliga a buscar los registros fuera de la base.

### Consecuencias

**Positivas**

1. Las consultas por fecha revisan solo los meses que necesitan.
2. Un mes viejo se retira completo, de una sola vez.
3. El backend sigue viendo cada historial como una sola tabla.
4. No suma servidores ni costo.

**Negativas**

1. Una consulta que no filtra por fecha revisa todas las partes y no gana velocidad.

---

<a id="adr-054"></a>
## ADR-054 · Capacidad de la base de datos cuando un servidor no alcance

**Fecha:** 27/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-26, US-15, RT-01, RT-04, RT-02, RF-05, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La base de datos funciona en un solo servidor principal, con uno de respaldo. Falta decidir qué hacer si los préstamos, devoluciones y pagos llegan a superar lo que ese servidor puede atender. La opción elegida debe mantener cada préstamo completo, conservar separados los datos de cada institución y no sumar costo mientras no haga falta.

### Alternativas consideradas

**A.** Un solo servidor que se agranda. Toda la información vive en un servidor, y cuando no alcanza se le aumenta el procesador y la memoria, hasta el tamaño máximo que ofrece la plataforma.

**B.** Repartir por institución desde el inicio. Desde el primer día hay dos servidores, cada uno con su base de respaldo y con los datos completos de un grupo de instituciones.

**C.** Repartir por institución cuando haga falta. Hoy hay un solo servidor. Cuando el monitoreo muestre que ya no alcanza, se trasladan instituciones completas a un segundo servidor.

### Decisión

Se elige la opción C, repartir por institución cuando haga falta. Cada institución vive completa en un solo servidor, así que cada préstamo se guarda completo y sus datos siguen separados de los de las demás. El segundo servidor se paga solo cuando el primero ya no alcance.

Se descarta A porque, al llegar al tamaño máximo de la máquina, no queda hacia dónde crecer.

Se descarta B porque se paga desde el primer día un segundo servidor que todavía no se necesita..

### Consecuencias

**Positivas**

1. Cada préstamo se guarda completo en un solo servidor.
2. Los datos de cada institución siguen juntos y separados de los de las demás.
3. El segundo servidor se paga solo cuando el primero ya no alcance.
4. Trasladar una institución no exige rediseñar nada, porque ya tiene su propio espacio.

**Negativas**

1. Hay que fijar la cifra que activa el traslado y vigilarla todo el tiempo.
2. Con dos servidores, los reportes de toda la plataforma deben consultar ambos.

---

<a id="adr-055"></a>
## ADR-055 · Dónde se guardan las imágenes del sistema

**Fecha:** 27/09/2026  
**Estado:** Aceptado  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Ya está decidido que el backend y el API Gateway se entregan empaquetados en contenedores y que Cloud Run los ejecuta. Cloud Run no guarda imágenes: siempre las descarga del lugar donde estén publicadas. Falta decidir cuál es ese lugar, teniendo en cuenta que el repositorio del código es público y que el presupuesto es fijo.

### Alternativas consideradas

**A.** Artifact Registry. Almacén de imágenes de Google, en la misma nube y el mismo proyecto donde corre el sistema. La imagen queda privada y Cloud Run la toma con los permisos del propio proyecto, sin guardar claves. Los primeros 0,5 GB al mes no tienen costo y después se cobra por gigabyte.

**B.** Docker Hub. El almacén más conocido. Su plan gratuito guarda sin límite las imágenes públicas, limita las privadas y restringe cuántas veces se puede descargar una imagen en un periodo.

**C.** El registro de GitHub. Viene incluido con la cuenta donde ya está el código. Guardar y descargar imágenes públicas no tiene costo ni límite.

### Decisión

Se elige C, el registro de GitHub, con las imágenes publicadas como públicas.

En eficiencia de costos, es la única de las tres que no cuesta nada: ni el espacio ocupado ni las descargas, sin límite de versiones.

En operabilidad, vive en la misma cuenta donde ya está el código y donde se construye la imagen, así que no hay otro almacén que crear ni administrar, y publicar no exige claves nuevas.

En seguridad, la imagen queda pública igual que el repositorio, y no revela nada que no esté ya a la vista: las librerías y sus versiones están escritas en el código. Las claves nunca van dentro de la imagen; se leen al ejecutar.

Cada versión se publica con la etiqueta del cambio que la originó, así que Cloud Run siempre recibe una etiqueta nueva y nunca queda esperando a que se venza una copia guardada.

Se descarta A porque cobra por el espacio ocupado, cerca de 2.400 pesos al año, a cambio de mantener privada una imagen que no necesita serlo. Se descarta B porque limita cuántas veces se puede descargar una imagen en un periodo, y el propio proveedor de la nube recomienda usarlo solo para imágenes oficiales muy conocidas.

### Consecuencias

**Positivas**

1. No cuesta nada, ni por espacio ni por descargas.
2. Está donde ya vive el código: no hay otro almacén que administrar ni claves nuevas que guardar.
3. Las claves no van dentro de la imagen: se leen al ejecutar.

**Negativas**

1. La imagen queda pública. Si algún día el repositorio pasa a privado, hay que revisar esta decisión.
2. Cloud Run baja la imagen por internet, así que encender una copia nueva es algo más lento que si estuviera en la misma región.

---

<a id="adr-056"></a>
## ADR-056 · Seguimiento de una solicitud entre las piezas del sistema

**Fecha:** 27/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-15, US-33, US-24, US-15, US-10, RN-01, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cuando alguien paga una multa, el pago pasa por varios puntos del sistema hasta llegar a la pasarela de pagos, y después sale un aviso de confirmación. Cada punto guarda sus registros por separado, así que si el pago falla, no se sabe en cuál falló ni cuál se demoró. Falta decidir cómo seguir ese recorrido completo, incluido el aviso, sin sumar costo.

### Alternativas consideradas

**A.** Buscar por un número común. Cada punto del sistema anota el mismo número del pago en sus registros, y el equipo busca ese número a mano, punto por punto. Muestra qué pasó en cada punto, sin medir cuánto tardó. No cuesta nada.

**B.** Jaeger. Herramienta gratuita que muestra el recorrido del pago como una línea de tiempo, con lo que tardó cada paso. El equipo la instala y la mantiene en una máquina propia, encendida todo el tiempo. Se paga esa máquina.

**C.** Cloud Trace. Servicio de Google que muestra el recorrido del pago como una línea de tiempo, con lo que tardó cada paso, en el mismo lugar donde ya están los registros del sistema. Google lo mantiene. Es gratis hasta 2,5 millones de pasos al mes, y después cobra USD 0,20 por cada millón.

### Decisión

Se elige la opción C, Cloud Trace. Muestra en una línea de tiempo en qué paso falló una solicitud y cuánto tardó cada uno, en la misma plataforma donde ya están los registros y las mediciones, sin nada que instalar. El seguimiento continúa también por Pub/Sub, porque el backend pasa el número de la solicitud dentro de cada aviso que envía. En el volumen del proyecto no genera costo.

Se descarta A porque seguir una solicitud obliga a buscar a mano los registros de cada servicio, sin ver cuánto tardó cada paso.

Se descarta B porque exige una máquina encendida todo el tiempo, y mantenerla, para algo que la plataforma ya ofrece.

### Consecuencias

**Positivas**

1. Se ve en una línea de tiempo en qué paso falló una solicitud y cuánto tardó cada uno.
2. Registros, mediciones y seguimiento quedan en un solo lugar: desde un error se salta a su recorrido.
3. El volumen del proyecto no genera costo.

**Negativas**

1. Hay que activar a propósito que Pub/Sub lleve el número de la solicitud; si no, el seguimiento se corta ahí.
2. Por encima de lo gratuito se cobra por volumen, así que solo se sigue un porcentaje de las solicitudes normales.
3. Cada paso lleva solo el número de cuenta y la institución, nunca nombre ni correo.

---

<a id="adr-057"></a>
## ADR-057 · Eliminación de los datos personales de un usuario

**Fecha:** 27/09/2026  
**Estado:** Aceptado  
**Drivers de arquitectura:** RF-15, RF-10, RF-21, US-13, US-22, RT-19  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

La ley obliga a eliminar los datos personales de un usuario cuando lo pide o cuando vence el plazo de conservación, pero el historial de préstamos, multas y pagos no se puede modificar ni borrar. Falta decidir cómo cumplir las dos cosas al tiempo. La opción elegida debe dejar sin identificar a la persona, conservar el registro de cada operación y aplicarse solo cuando no tenga préstamos activos ni multas pendientes.

### Alternativas consideradas

**A.** Borrar el usuario con todo su historial. Se eliminan la persona y todos sus préstamos, multas y pagos.

**B.** Reemplazar los datos dentro de cada registro. El nombre y el correo quedan escritos dentro de cada préstamo, multa y pago. Al eliminar, se sobrescriben uno por uno con un texto como "usuario eliminado".

**C.** Guardar la identidad aparte. Los préstamos, multas y pagos solo llevan un número de usuario, y el nombre, el correo y el documento viven en una ficha aparte. Al eliminar, se borra solo la ficha, y el número queda sin nadie detrás.

### Decisión

Se elige la opción C, guardar la identidad aparte. El historial nunca se toca: solo se borra la ficha, así que cada préstamo, multa y pago sigue registrado sin que se sepa quién lo hizo. La ficha solo se borra si el usuario no tiene préstamos activos ni multas pendientes.

Se descarta A porque se pierde el registro de los préstamos y los pagos, que debe conservarse.

Se descarta B porque obliga a modificar cada registro del historial, que no se puede modificar.

### Consecuencias

**Positivas**

1. Se cumple la ley sin tocar el historial.
2. La biblioteca sigue sabiendo que un ejemplar se prestó en tal fecha, aunque ya no sepa a quién.
3. Los reportes siguen contando los préstamos, aunque la persona ya no exista.

**Negativas**

1. Mostrar un préstamo con el nombre de la persona exige unir el historial con la ficha.
2. Los datos borrados siguen en las copias de seguridad hasta que estas vencen.
3. La condición de no tener préstamos ni multas se revisa en el momento exacto de borrar.

---

<a id="adr-058"></a>
## ADR-058 · Organización de las reglas del préstamo, la devolución y la multa

**Fecha:** 27/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RF-27, RT-24, RN-01, RT-03, US-20  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Un préstamo pasa por varios estados (activo, vencido, devuelto), y varias funciones los cambian: prestar, devolver, reservar y vencer. Al devolverse tarde se genera una multa, cuyo cálculo cambia según cada institución. Falta decidir cómo se organizan esas reglas en el código. La opción elegida debe impedir desde un solo lugar los cambios de estado no permitidos, dejar que cada institución tenga su forma de calcular la multa sin tocar el código, y guardar una devolución completa o no guardarla.

### Alternativas consideradas

**A.** Condiciones escritas en cada función. Cada función revisa por su cuenta si el cambio de estado está permitido y calcula la multa con una lista de condiciones según la institución.

**B.** Reglas dentro de la base de datos. Los cambios de estado permitidos y el cálculo de la multa se validan con reglas escritas en la base de datos. Probarlas exige tener la base encendida.

**C.** Patrones de diseño en el código. -Cada estado del préstamo tiene la lista de estados a los que puede pasar, y cualquier otro cambio se rechaza (patrón State). -Cada forma de calcular la multa se programa por separado, y el administrador de cada institución elige cuál usar y con qué valores (patrón Strategy). -Una devolución guarda todos sus cambios juntos o ninguno (patrón Unit of Work).

### Decisión

Se elige la opción C, patrones de diseño en el código. Cada estado tiene escrita una sola vez la lista de estados a los que puede pasar, así que ninguna función puede hacer un cambio no permitido. El administrador de cada institución elige cómo se calcula su multa y con qué valores, sin tocar el código. Y al devolver un libro, el cierre del préstamo, la multa y el ejemplar liberado se guardan juntos, o no se guarda nada.

Se descarta A porque la misma regla queda repetida en cada función, y basta con que una la olvide para permitir un cambio indebido.

Se descarta B porque cada regla solo se puede probar con la base de datos encendida.

### Consecuencias

**Positivas**

1. Un cambio de estado no permitido se rechaza siempre, venga de la función que venga.
2. El administrador cambia la forma de calcular la multa de su institución sin esperar una versión nueva.
3. Una devolución nunca queda a medias.
4. Cada regla se prueba sola, sin encender la base de datos.

**Negativas**

1. Agregar un estado nuevo exige definir a qué estados puede pasar y revisar los demás.
2. El administrador solo puede elegir entre las formas de calcular la multa que ya están programadas.

---

<a id="adr-059"></a>
## ADR-059 · Atención de registros masivos en el proveedor de seguridad

**Fecha:** 29/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-09, RF-01, RF-24, RN-04, RN-11.  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El registro y el inicio de sesión los maneja Identity Platform, con el correo institucional y una contraseña que crea cada estudiante. Identity Platform permite como máximo 100 cuentas nuevas por hora desde una misma dirección de internet, y todos los estudiantes que se registran desde la red de su universidad comparten esa dirección. Falta decidir cómo atender las avalanchas de registros al inicio del semestre y en época de parciales, sin que ningún estudiante se quede por fuera y sin cambiar el registro que define el negocio

### Alternativas consideradas

**A.** Solicitar automáticamente un límite mayor a Google. Al incorporar una universidad, se registran su número de estudiantes y sus fechas de inicio de semestre y de parciales, y estas fechas van a poderse modificar por el administrador institucional. Dos semanas antes de cada fecha, una tarea programada abre sola una solicitud al soporte de Google para subir el límite, y al terminar el periodo pide volverlo a su valor normal. Requiere el plan de soporte Standard de Google B. Crear las cuentas por adelantado. La universidad entrega la lista de sus estudiantes y BookHive carga las cuentas antes del semestre. Cada estudiante recibe un correo para crear su contraseña. Requiere programar la carga y los correos. C. Registrar por turnos. Si ya se alcanzó el límite de la hora, BookHive guarda la solicitud en una fila y crea la cuenta en la hora siguiente. El estudiante recibe un correo cuando está lista. Requiere programar la fila y el aviso.

### Decisión

Se elige la opción A, solicitar automáticamente un límite mayor a Google.

Es la única que conserva el registro como lo define el negocio (el estudiante crea su cuenta y su contraseña). Como la solicitud la hace el sistema con las fechas que cada universidad registra al incorporarse, no depende de que alguien del equipo se acuerde.

Se descarta B porque BookHive tendría los datos de los estudiantes antes de que acepten la política de datos.

Se descarta C porque el estudiante podría esperar horas para usar su cuenta, cuando el registro debe continuar en menos de 2 segundos.

### Consecuencias

**Positivas**

1. Ningún estudiante queda bloqueado al registrarse, aunque todo el campus salga por la misma dirección de internet.
2. El registro no cambia: cada estudiante crea su cuenta y su contraseña.
3. Nadie tiene que acordarse de pedir el aumento: el sistema lo hace solo con las fechas de cada universidad.
4. El límite alto dura solo las semanas de más registros; el resto del año sigue frenando la creación de cuentas falsas.

**Negativas**

1. El plan de soporte Standard de Google cuesta el 3% del cobro al mes y sale del presupuesto de infraestructura.
2. El sistema envía la solicitud, pero la aprueba una persona de Google; si la rechaza o se demora, el límite no sube.
3. Si una universidad no registra sus fechas o su número de estudiantes, la tarea no tiene con qué pedir el aumento.

---

<a id="adr-060"></a>
## ADR-060 · Orden de envío de los avisos cuando se alcanza el límite diario de correos

**Fecha:** 29/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-08, US-10, RF-18, RF-19  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Los correos del sistema los envía Brevo, que permite 300 al día; los que sobran salen al día siguiente. Algunos avisos no pueden esperar: si el de una reserva disponible llega tarde, la reserva se vence sin que la persona se entere. Falta decidir en qué orden salen los avisos cuando se llega a ese tope, para que ninguno con plazo corto llegue tarde

### Alternativas consideradas

**A.** Por orden de llegada. Los avisos salen en el orden en que se generan. Al llegar al tope, los que faltan salen al día siguiente en ese mismo orden. No requiere programar nada adicional.

**B.** Primero los urgentes. Cada tipo de aviso se clasifica como urgente (los que tienen plazo corto, como la reserva disponible) o como recordatorio. Al llegar al tope, los urgentes siempre salen y los recordatorios pasan al día siguiente. Requiere clasificar los avisos y programar el orden de envío.

**C.** Un solo correo diario por persona. Los avisos de cada persona se juntan y salen en un único correo a una hora fija. Requiere programar la unión de los avisos.

### Decisión

Se elige la opción B, primero los urgentes.

Es la única que garantiza que los avisos con plazo corto salgan el mismo día, aunque se llegue al tope.

Se descarta A porque el aviso de una reserva disponible podría quedar detrás de cientos de recordatorios y llegar cuando la reserva ya venció.

Se descarta C porque un aviso urgente esperaría hasta la hora del correo diario, cuando debe llegar en menos de 3 segundos.

### Consecuencias

**Positivas**

1. Al llegar a 250 correos en el día, el sistema avisa al equipo, antes de chocar con el tope.
2. La clasificación de cada aviso y su orden de atención quedan en la tabla anexa; un recordatorio que ya esperó un día pasa a urgente.
3. El aviso en el navegador sale al instante, aunque el correo espere.

**Negativas**

1. Todo tipo de aviso nuevo debe agregarse a la tabla con su nivel antes de empezar a enviarse.
2. Si los avisos urgentes de un solo día superan los 300, algunos igual esperan al día siguiente; la alerta de la consecuencia 2 existe para no llegar a ese punto.
3. En temporadas de mucho envío, los recordatorios pueden llegar hasta un día tarde.

---

<a id="adr-061"></a>
## ADR-061 · Crecimiento de la máquina del motor de busqueda del caché

**Fecha:** 29/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** US-15, US-26, US-27, RN-11  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

El motor de búsqueda y la caché corren en una sola máquina que instala y mantiene el equipo, y el motor guarda todo el catálogo en memoria. A medida que entran instituciones, esa memoria se va llenando, y nada la amplía por sí solo. Falta decidir cómo crece esta parte del sistema sin que las búsquedas se vuelvan lentas y sin dejar a los usuarios sin buscar en hora pico.

### Alternativas consideradas

**A.** Máquina más grande. Cuando la memoria llega al 80 %, se cambia la máquina por una con más memoria. Hay que apagarla unos minutos, y cada cambio cuesta cerca del doble.

**B.** Repartir las instituciones en dos máquinas. Cuando la memoria llega al 80 %, se enciende una segunda máquina y se le pasan los catálogos de un grupo de instituciones. Cada máquina guarda solo los suyos.

**C.** Tres copias del catálogo completo. El motor funciona en tres máquinas iguales; si una se cae, las otras siguen. Cuesta cerca del triple.

### Decisión

Se elige la opción B, repartir las instituciones en dos máquinas. Cada máquina guarda solo los catálogos de su grupo, así que se reparten la memoria y las búsquedas, y se pueden sumar más máquinas si hace falta.

Se descarta A porque, al llegar al tamaño máximo de la máquina, no queda hacia dónde crecer.

Se descarta C porque cada máquina guarda el catálogo completo: triplica el costo sin repartir la memoria.

### Consecuencias

**Positivas**

1. La memoria y las búsquedas se reparten entre las dos máquinas.
2. Si el catálogo sigue creciendo, se suma otra máquina de la misma forma.
3. La segunda máquina se paga solo cuando la alerta indica que hace falta.

**Negativas**

1. El sistema debe saber en qué máquina está el catálogo de cada institución.
2. Son dos máquinas que el equipo debe mantener.
3. Cada cambio del catálogo debe llegar a la máquina correcta.

---

<a id="adr-062"></a>
## ADR-062 · Ejecución de las tareas programadas cuando crece el numero de instituciones

**Fecha:** 29/09/2026  
**Estado:** Propuesto  
**Drivers de arquitectura:** RT-08, RT-16, US-26, RF-06, RF-07, RF-18, RF-19  
**Participantes:** Johan Camilo Bedoya · Fernando Zuluaga Botero

### Contexto

Cloud Scheduler dispara cada tarea programada con una sola llamada al backend, y esa llamada recorre todas las instituciones, por ejemplo, para actualizar las multas. Cloud Run corta cualquier solicitud que pase de su tiempo máximo, y el recorrido se alarga con cada institución nueva. Falta decidir cómo se ejecutan estas tareas para que terminen sin importar cuántas instituciones haya, y para que la falla en una no frene a las demás.

### Alternativas consideradas

**A.** Todas las universidades en una sola tarea. A la hora programada, el backend actualiza las multas de la primera universidad, después las de la segunda, y así hasta la última, todo dentro de la misma tarea. Es como funciona hoy.

**B.** Una tarea por universidad. A la hora programada, el sistema deja en la fila de mensajes (Pub/Sub) una tarea por cada universidad: "actualizar las multas de la UCO", "actualizar las multas de la UdeA"... El backend las atiende al mismo tiempo, cada una por separado.

**C.** Una sola tarea larga en otro servicio de Google. La tarea recorre todas las universidades una tras otra, como en A, pero corre en Cloud Run Jobs, un servicio de Google hecho para trabajos largos que no la corta por tiempo. Hay que configurar ese servicio aparte.

### Decisión

Se elige la opción B, una tarea por universidad. Cada tarea atiende a una sola universidad, así que termina rápido y nunca se pasa del tiempo máximo, sin importar cuántas universidades haya. Si la de una universidad falla, se repite solo esa, sin volver a hacer las demás.

Se descarta A porque cada universidad nueva alarga la tarea, hasta que se pasa del tiempo máximo y las últimas quedan sin procesar.

Se descarta C porque suma un servicio nuevo que configurar y vigilar, cuando la fila de mensajes y el backend ya resuelven lo mismo.

### Consecuencias

Se elige la opción B, una tarea por universidad. Actualizar las multas de una sola universidad toma poco tiempo, así que ninguna tarea llega al límite en el que Cloud Run la corta, haya 5 o 100 universidades. Y si falla la tarea de la UCO, se repite solo la de la UCO; las demás ya quedaron hechas.

Se descarta A porque con cada universidad nueva la tarea dura más, y un día Cloud Run la cortará a mitad de camino: las universidades que faltaban se quedan sin sus multas actualizadas.

Se descarta C porque obliga a montar y vigilar un servicio más, cuando la fila de mensajes y el backend, que ya existen, hacen lo mismo.

---
