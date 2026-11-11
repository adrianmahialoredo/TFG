# Prueba de usabilidad de TOCO: protocolo y cuestionarios

Material para hacer la prueba con usuarios reales y obtener los datos del apartado 7.4 de la memoria. Todo lo que se apunte en las plantillas de `plantillas/` lo procesa `analyze_usability.py`.

## 1. Diseño de la prueba

- **Tipo:** prueba moderada, presencial (o por videollamada compartiendo pantalla), con tareas y cuestionario SUS al final.
- **Participantes:** entre 5 y 10. Con 5 se detectan la mayoría de problemas de uso (Nielsen y Landauer); con 8 o más la media del SUS es más fiable. Conviene mezclar niveles: principiantes (< 1 año tocando), intermedios y alguien avanzado. Apunta su perfil en `plantillas/perfil_participantes.csv`.
- **Dispositivo:** el APK de `preview` (EAS) instalado en un Android, contra el backend de Render. Anota modelo y versión de Android. Antes de cada sesión, despierta el backend (abre la app o `/actuator/health`) para que el arranque en frío no cuente en los tiempos.
- **Cuentas:** cada participante crea la suya en la tarea T1 con su email. Para la tarea de Spotify, su cuenta de Spotify debe estar en la lista *User Management* del dashboard de Spotify (la app está en modo desarrollo); si no, esa tarea se marca como "no aplica".
- **Duración:** 25–35 min por persona.
- **Material:** este guion, el consentimiento, las tarjetas de tareas (sección 4, una por hoja), cronómetro (el del móvil del moderador) y las plantillas CSV (o una hoja de cálculo con las mismas columnas).

## 2. Consentimiento informado (leer y firmar antes de empezar)

> Vas a participar en una prueba de la aplicación TOCO, parte del Trabajo Fin de Grado de [nombre del autor] en la Universidad de Oviedo. Se evalúa la aplicación, no a ti: si algo te cuesta, es un fallo de la aplicación.
>
> Durante la prueba se anotará el tiempo de cada tarea, si la completas y los comentarios que hagas en voz alta. No se graba tu cara ni tu voz [o: se grabará solo la pantalla del móvil]. Tus respuestas son anónimas: en la memoria solo aparecerás como "P1", "P2"… y con tu perfil general (nivel musical, edad aproximada).
>
> Puedes dejar la prueba cuando quieras, sin dar explicaciones. Al terminar, puedes pedir que se borre tu cuenta de la aplicación.
>
> Nombre: ______________________  Fecha: ___________  Firma: ___________

## 3. Guion del moderador

1. Bienvenida, consentimiento y cuestionario de perfil (sección 5).
2. Explicar: "Piensa en voz alta mientras usas la app. No puedo ayudarte salvo que te atasques del todo; si eso pasa, lo apunto como ayuda."
3. Entregar las tareas de una en una. Pon el cronómetro en marcha al terminar de leerla y páralo cuando el participante diga que ha terminado o se rinda (máximo 5 min por tarea).
4. Por cada tarea, apunta en `plantillas/observacion_tareas.csv`:
   - `completada`: 1 = sola y bien; 0.5 = con una ayuda o resultado parcial; 0 = no lo consigue o se rinde;
   - `tiempo_s`: segundos;
   - `errores`: acciones equivocadas (tocar algo que no era, volver atrás perdido…);
   - `ayudas`: veces que tuviste que intervenir;
   - `observaciones`: lo que dice o hace que te llame la atención (citas literales entre comillas).
5. Al terminar las tareas: cuestionario SUS (sección 6) y preguntas abiertas (sección 7), sin el moderador mirando.
6. Agradecer. Si lo pide, borrar su cuenta.

## 4. Tareas (una tarjeta por tarea)

| ID | Tarea que se lee al participante | Se considera completada si… |
|---|---|---|
| T1 | Crea una cuenta en la aplicación con tu email. Elige tus géneros e instrumentos favoritos. | Entra en Inicio con la sesión iniciada. |
| T2 | Busca la canción "[canción del catálogo]", ponla en dificultad "Original" y mira cómo se toca uno de sus acordes. | Abre el modal de un acorde con la dificultad Original. |
| T3 | Crea una lista llamada "Para practicar" y añade 3 canciones y 2 lecciones de las que te recomienda la app. | La lista tiene 3 canciones y 2 lecciones. |
| T4 | Abre el género "[género]" y empieza el itinerario de guitarra. Pasa a la tercera lección. | Ve la tercera lección del itinerario. |
| T5 | Abre el afinador, elige la afinación "Drop D" y afina la 6.ª cuerda (o haz como si la afinaras con la guía). | Selecciona Drop D y la cuerda D2, y la pantalla muestra la lectura de esa cuerda. |
| T6 | Abre el metrónomo, pon el tempo a unos 140 BPM marcando el ritmo con el dedo y cambia el sonido a hi-hat. | Suena a 135–145 BPM con hi-hat usando "TAP TEMPO". |
| T7 | Conecta tu cuenta de Spotify y comprueba qué te recomienda la app a partir de ella. *(Solo si su cuenta está en User Management)* | Vuelve a la app y ve el carrusel "Basado en tu Spotify" (con contenido o con su aviso). |
| T8 | Cierra sesión. Imagina que no recuerdas tu contraseña: cámbiala por una nueva y entra con ella. | Entra con la contraseña nueva usando "Olvidé mi contraseña". |

## 5. Cuestionario de perfil (antes de las tareas)

1. Edad: menos de 18 / 18–24 / 25–34 / 35–49 / 50 o más.
2. ¿Qué instrumento(s) tocas?
3. ¿Cuánto tiempo llevas tocando? Menos de 1 año / 1–3 años / más de 3 años.
4. ¿Cómo describirías tu nivel? Principiante / Intermedio / Avanzado.
5. ¿Tienes formación musical reglada (conservatorio, escuela de música)? Sí / No.
6. ¿Qué aplicaciones o webs usas para aprender o practicar? (Ultimate Guitar, Songsterr, Yousician, YouTube, afinadores…)
7. ¿Usas Spotify habitualmente? Sí / No.

## 6. Cuestionario SUS (System Usability Scale)

Instrucciones al participante: *"Marca para cada frase cuánto estás de acuerdo, de 1 (totalmente en desacuerdo) a 5 (totalmente de acuerdo). Responde rápido, sin pensarlo demasiado; si no sabes qué contestar, marca 3."*

| # | Afirmación | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|---|
| 1 | Creo que me gustaría usar esta aplicación con frecuencia. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 2 | Encontré la aplicación innecesariamente compleja. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 3 | Pensé que la aplicación era fácil de usar. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 4 | Creo que necesitaría la ayuda de una persona con conocimientos técnicos para poder usar esta aplicación. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 5 | Encontré que las distintas funciones de la aplicación estaban bien integradas. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 6 | Pensé que había demasiada incoherencia en la aplicación. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 7 | Imagino que la mayoría de la gente aprendería a usar esta aplicación muy rápidamente. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 8 | Encontré la aplicación muy engorrosa de usar. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 9 | Me sentí muy seguro/a usando la aplicación. | ☐ | ☐ | ☐ | ☐ | ☐ |
| 10 | Necesité aprender muchas cosas antes de poder manejarme con la aplicación. | ☐ | ☐ | ☐ | ☐ | ☐ |

Las respuestas se pasan a `plantillas/respuestas_sus.csv` (columnas `p1` a `p10`), o se exporta el CSV del formulario (ver sección 8).

**Cálculo** (lo hace `analyze_usability.py`): en las afirmaciones impares se resta 1 a la respuesta; en las pares, se resta la respuesta a 5. Se suman las diez y se multiplica por 2,5: queda una puntuación de 0 a 100 por participante. La media de referencia de la escala es 68.

## 7. Preguntas abiertas (después del SUS)

1. ¿Qué es lo que más te ha gustado de la aplicación?
2. ¿Qué te ha resultado más difícil o confuso?
3. ¿Echas en falta algo que usarías para aprender o practicar?
4. ¿La usarías en lugar de las aplicaciones que usas ahora? ¿Por qué?

Apunta las respuestas en la columna `comentarios` de `respuestas_sus.csv` (o en el formulario).

## 8. Con un formulario en línea

**Opción recomendada: el formulario SUS de TOCO** → https://claude.ai/artifact/WbtESYnX7vV2F3JSGHcWAQ (código en `sus_form.html`).

- Tiene las 10 afirmaciones de la sección 6 y las 4 preguntas abiertas de la sección 7. Avisa si falta alguna respuesta y propone el siguiente código de participante.
- Solo guarda con la sesión de claude.ai del moderador: el participante lo rellena **en el móvil del moderador** al terminar las tareas.
- La pestaña "Resultados" (solo el moderador) muestra la puntuación de cada participante, la media, la desviación típica, la nota y los comentarios, y exporta el CSV en el formato de `plantillas/respuestas_sus.csv` ("Copiar CSV" o "Descargar CSV"), listo para `analyze_usability.py --sus`.
- Antes de la primera sesión, haz un envío de prueba y bórralo desde "Resultados".

**Alternativa: Google Forms o Microsoft Forms de la Universidad**

1. Crea un formulario con: un campo "Código de participante" (P1, P2…), las 10 afirmaciones del SUS como preguntas de **escala lineal 1–5** (copiando el texto tal cual, empezando por el número: "1. Creo que me gustaría…") y las 4 preguntas abiertas como párrafo.
2. Al final de cada sesión, el participante lo rellena en el móvil del moderador o en el suyo.
3. Exporta las respuestas a CSV (Google Forms: Respuestas → icono de hoja de cálculo → Archivo → Descargar → CSV; Microsoft Forms: Abrir en Excel → Guardar como CSV UTF-8).
4. Analízalo con `python tests/usability/analyze_usability.py --sus <export.csv>`: el script reconoce las columnas que empiezan por "1." … "10.".

## 9. Qué poner en la memoria (7.4)

`analyze_usability.py` genera `results/usabilidad.md` con:

- la tabla del perfil de los participantes (cuántos de cada nivel);
- la tabla por tarea: tasa de éxito, tiempo medio y mediano, errores y ayudas medias;
- la puntuación SUS de cada participante, la media, la desviación típica y el intervalo de confianza al 95 %, la nota según la escala de Sauro y Lewis y el adjetivo más cercano según Bangor, Kortum y Miller.

En el texto de 7.4.4 se resumen los comentarios repetidos y los problemas observados (con su gravedad y la mejora que se propone o se ha hecho). Hay que citar el SUS (Brooke), la escala de notas (Sauro y Lewis) y la de adjetivos (Bangor et al.): ver el apartado 10 de `corrections.md`.
