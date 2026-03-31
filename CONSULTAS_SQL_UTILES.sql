-- ====================================================================
-- CONSULTAS SQL ÚTILES - SISTEMA DE APRENDIZAJE DE MÚSICA
-- ====================================================================
-- Este archivo contiene consultas SQL comúnmente usadas para consultar
-- y mantener la base de datos del sistema de aprendizaje musical.
-- ====================================================================

-- ====================================================================
-- 1. CONSULTAS DE USUARIOS
-- ====================================================================

-- Obtener usuario por email con sus géneros favoritos
SELECT u.*, g.id as genre_id, g.name as genre_name
FROM users u
LEFT JOIN user_favorite_genres ufg ON u.id = ufg.user_id
LEFT JOIN items g ON ufg.genre_id = g.id
WHERE u.email = 'user@example.com';

-- Obtener usuario con sus instrumentos favoritos
SELECT u.*, i.id as instrument_id, i.name as instrument_name
FROM users u
LEFT JOIN user_favorite_instruments ufi ON u.id = ufi.user_id
LEFT JOIN instruments i ON ufi.instrument_id = i.id
WHERE u.id = ?;

-- Obtener todos los usuarios activos (con listas)
SELECT DISTINCT u.id, u.email, u.username, COUNT(il.id) as lists_count
FROM users u
LEFT JOIN item_lists il ON u.id = il.user_id
WHERE u.role = 'USER'
GROUP BY u.id
HAVING COUNT(il.id) > 0;

-- Usuarios que nunca han creado listas
SELECT u.id, u.email, u.username
FROM users u
WHERE u.id NOT IN (SELECT DISTINCT user_id FROM item_lists);

-- ====================================================================
-- 2. CONSULTAS DE CANCIONES
-- ====================================================================

-- Obtener todas las canciones de un género
SELECT s.id, s.title, s.artist, g.id as genre_id, g.name as genre_name
FROM songs s
JOIN song_genre sg ON s.id = sg.song_id
JOIN items g ON sg.genre_id = g.id
WHERE g.id = ?
ORDER BY s.title;

-- Obtener canciones que requieren un instrumento específico
SELECT DISTINCT s.id, s.title, s.artist, i.id as instrument_id, i.name
FROM songs s
JOIN song_genre sg ON s.id = sg.song_id
JOIN instrument_song ins ON s.id = ins.song_id
JOIN instruments i ON ins.instrument_id = i.id
WHERE i.id = ?;

-- Canciones populares por década
SELECT s.id, s.title, s.artist, s.decade, COUNT(sl.id) as lesson_count
FROM songs s
LEFT JOIN song_lessons sl ON s.id = sl.song_id
WHERE s.popular = true AND s.decade = '1980s'
GROUP BY s.id
ORDER BY lesson_count DESC;

-- Canciones con más lecciones
SELECT s.id, s.title, s.artist, COUNT(sl.id) as lesson_count
FROM songs s
LEFT JOIN song_lessons sl ON s.id = sl.song_id
GROUP BY s.id
ORDER BY lesson_count DESC
LIMIT 10;

-- Canciones que no tienen ninguna lección
SELECT s.id, s.title, s.artist
FROM songs s
WHERE s.id NOT IN (SELECT DISTINCT song_id FROM song_lessons);

-- ====================================================================
-- 3. CONSULTAS DE LECCIONES
-- ====================================================================

-- Obtener todas las lecciones de una canción por instrumento
SELECT sl.id, sl.difficulty, i.name as instrument,
       COUNT(c.id) as chord_count
FROM song_lessons sl
JOIN instruments i ON sl.instrument_id = i.id
LEFT JOIN song_chords sc ON sl.id = sc.song_id
LEFT JOIN chords c ON sc.chord_id = c.id
WHERE sl.song_id = ?
GROUP BY sl.id, sl.difficulty, i.name;

-- Lecciones por nivel de dificultad
SELECT sl.difficulty, COUNT(*) as count
FROM song_lessons sl
GROUP BY sl.difficulty
ORDER BY FIELD(sl.difficulty, 'EASY', 'MEDIUM', 'ORIGINAL');

-- Obtener lecciones con sus secciones de tabs
SELECT sl.id, sl.song_id, ts.id as tab_id, ts.identifier
FROM song_lessons sl
LEFT JOIN tab_sections ts ON sl.id = ts.song_lesson_id
WHERE sl.song_id = ?
ORDER BY sl.difficulty, ts.id;

-- ====================================================================
-- 4. CONSULTAS DE ACORDES
-- ====================================================================

-- Obtener acordes de una lección específica
SELECT c.id, c.name, cn.note
FROM chords c
LEFT JOIN chord_notes cn ON c.id = cn.chord_id
WHERE c.id IN (
    SELECT chord_id FROM song_chords
    WHERE song_id = ?
)
ORDER BY c.name, cn.note;

-- Acordes más usados
SELECT c.id, c.name, COUNT(sc.song_id) as usage_count
FROM chords c
LEFT JOIN song_chords sc ON c.id = sc.chord_id
GROUP BY c.id
ORDER BY usage_count DESC
LIMIT 20;

-- Acordes por tipo
SELECT
    SUM(CASE WHEN c.major THEN 1 ELSE 0 END) as major_count,
    SUM(CASE WHEN c.minor THEN 1 ELSE 0 END) as minor_count,
    SUM(CASE WHEN c.seventh THEN 1 ELSE 0 END) as seventh_count,
    SUM(CASE WHEN c.powerChord THEN 1 ELSE 0 END) as power_chord_count,
    SUM(CASE WHEN c.open THEN 1 ELSE 0 END) as open_count,
    SUM(CASE WHEN c.barre THEN 1 ELSE 0 END) as barre_count
FROM chords c;

-- ====================================================================
-- 5. CONSULTAS DE GÉNEROS
-- ====================================================================

-- Géneros con más canciones
SELECT g.id, g.name, COUNT(sg.song_id) as song_count
FROM items g
LEFT JOIN song_genre sg ON g.id = sg.genre_id
WHERE g.dtype = 'GENRE'
GROUP BY g.id
ORDER BY song_count DESC;

-- Géneros con más usuarios siguiendo
SELECT g.id, g.name, COUNT(ufg.user_id) as user_count
FROM items g
LEFT JOIN user_favorite_genres ufg ON g.id = ufg.genre_id
WHERE g.dtype = 'GENRE'
GROUP BY g.id
ORDER BY user_count DESC;

-- Obtener todas las lecciones de un género
SELECT l.id, l.name, ls.id as section_id, ls.orderIndex
FROM items l
LEFT JOIN lesson_sections ls ON l.id = ls.lesson_id
WHERE l.dtype = 'LESSON'
  AND l.id IN (
    SELECT lesson_id FROM instrument_genres
    WHERE genre_id = ?
  )
ORDER BY l.name, ls.orderIndex;

-- ====================================================================
-- 6. CONSULTAS DE INSTRUMENTOS
-- ====================================================================

-- Instrumentos más usados en canciones
SELECT i.id, i.name, COUNT(DISTINCT s.id) as song_count
FROM instruments i
LEFT JOIN instrument_song ins ON i.id = ins.instrument_id
LEFT JOIN songs s ON ins.song_id = s.id
GROUP BY i.id
ORDER BY song_count DESC;

-- Instrumentos favoritos de usuarios
SELECT i.id, i.name, COUNT(ufi.user_id) as user_count
FROM instruments i
LEFT JOIN user_favorite_instruments ufi ON i.id = ufi.instrument_id
GROUP BY i.id
ORDER BY user_count DESC;

-- Lecciones disponibles para un instrumento
SELECT DISTINCT s.id, s.title, s.artist, i.name
FROM songs s
JOIN song_lessons sl ON s.id = sl.song_id
JOIN instruments i ON sl.instrument_id = i.id
WHERE i.id = ?
ORDER BY s.title;

-- ====================================================================
-- 7. CONSULTAS DE LISTAS DE ITEMS
-- ====================================================================

-- Obtener una lista con todos sus items
SELECT il.id, il.name, COUNT(iti.item_id) as item_count
FROM item_lists il
LEFT JOIN itemlist_items iti ON il.id = iti.itemlist_id
WHERE il.user_id = ?
GROUP BY il.id
ORDER BY il.name;

-- Contenido detallado de una lista
SELECT il.id, il.name,
       i.id as item_id, i.name as item_name, i.dtype as item_type
FROM item_lists il
LEFT JOIN itemlist_items iti ON il.id = iti.itemlist_id
LEFT JOIN items i ON iti.item_id = i.id
WHERE il.id = ?
ORDER BY i.name;

-- Listas más populares (con más items)
SELECT il.id, il.name, il.user_id, COUNT(iti.item_id) as item_count
FROM item_lists il
LEFT JOIN itemlist_items iti ON il.id = iti.itemlist_id
GROUP BY il.id
ORDER BY item_count DESC
LIMIT 10;

-- ====================================================================
-- 8. CONSULTAS ANALÍTICAS
-- ====================================================================

-- Resumen general del sistema
SELECT
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM songs s WHERE s.dtype = 'SONG') as total_songs,
    (SELECT COUNT(*) FROM items i WHERE i.dtype = 'GENRE') as total_genres,
    (SELECT COUNT(*) FROM instruments) as total_instruments,
    (SELECT COUNT(*) FROM chords) as total_chords,
    (SELECT COUNT(*) FROM song_lessons) as total_lessons;

-- Estadísticas de cobertura por género
SELECT
    g.id,
    g.name,
    COUNT(DISTINCT sg.song_id) as songs,
    COUNT(DISTINCT sl.id) as lessons,
    COUNT(DISTINCT ufi.user_id) as users
FROM items g
LEFT JOIN song_genre sg ON g.id = sg.genre_id
LEFT JOIN song_lessons sl ON sg.song_id = sl.song_id
LEFT JOIN user_favorite_genres ufi ON g.id = ufi.genre_id
WHERE g.dtype = 'GENRE'
GROUP BY g.id
ORDER BY songs DESC;

-- Actividad de usuarios
SELECT
    u.id,
    u.email,
    COUNT(DISTINCT il.id) as lists_created,
    COUNT(DISTINCT ufg.genre_id) as favorite_genres,
    COUNT(DISTINCT ufi.instrument_id) as favorite_instruments
FROM users u
LEFT JOIN item_lists il ON u.id = il.user_id
LEFT JOIN user_favorite_genres ufg ON u.id = ufg.user_id
LEFT JOIN user_favorite_instruments ufi ON u.id = ufi.user_id
GROUP BY u.id
ORDER BY u.id;

-- Cobertura de instrumentos por canción
SELECT
    s.id,
    s.title,
    COUNT(DISTINCT sl.instrument_id) as instruments_with_lessons,
    GROUP_CONCAT(DISTINCT i.name SEPARATOR ', ') as instrument_names
FROM songs s
LEFT JOIN song_lessons sl ON s.id = sl.song_id
LEFT JOIN instruments i ON sl.instrument_id = i.id
WHERE s.dtype = 'SONG'
GROUP BY s.id
ORDER BY instruments_with_lessons DESC;

-- ====================================================================
-- 9. CONSULTAS DE MANTENIMIENTO
-- ====================================================================

-- Encontrar registros huérfanos (items sin referencia)
SELECT i.id, i.name, i.dtype
FROM items i
LEFT JOIN itemlist_items iti ON i.id = iti.item_id
WHERE iti.item_id IS NULL AND i.dtype = 'SONG'
  AND i.id NOT IN (SELECT id FROM songs);

-- Chequear integridad referencial
SELECT 'SongLesson without Song' as issue, COUNT(*) as count
FROM song_lessons sl
WHERE sl.song_id IS NULL
UNION ALL
SELECT 'SongLesson without Instrument' as issue, COUNT(*) as count
FROM song_lessons sl
WHERE sl.instrument_id IS NULL
UNION ALL
SELECT 'TabSection without SongLesson' as issue, COUNT(*) as count
FROM tab_sections ts
WHERE ts.song_lesson_id IS NULL;

-- Limpieza: Eliminar canciones sin lecciones (cuidado!)
-- DELETE FROM songs WHERE id NOT IN (
--     SELECT DISTINCT song_id FROM song_lessons
-- ) AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- ====================================================================
-- 10. ÍNDICES RECOMENDADOS (Para optimizar)
-- ====================================================================

-- Crear índices para mejoras de rendimiento
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_song_title ON songs(title);
CREATE INDEX idx_song_artist ON songs(artist);
CREATE INDEX idx_song_popular ON songs(popular);
CREATE INDEX idx_song_decade ON songs(decade);
CREATE INDEX idx_songlesson_song ON song_lessons(song_id);
CREATE INDEX idx_songlesson_instrument ON song_lessons(instrument_id);
CREATE INDEX idx_tabsection_lesson ON tab_sections(song_lesson_id);
CREATE INDEX idx_lesson_genre ON lessons(genre_id);
CREATE INDEX idx_itemlist_user ON item_lists(user_id);
CREATE INDEX idx_chord_name ON chords(name);

-- Índices compuestos para búsquedas comunes
CREATE INDEX idx_song_genre_popular ON songs(popular, decade);
CREATE INDEX idx_songlesson_difficulty ON song_lessons(song_id, difficulty);

-- ====================================================================
-- NOTAS IMPORTANTES
-- ====================================================================
--
-- 1. Reemplazar "?" con valores reales o parámetros preparados
-- 2. Usar prepared statements en la aplicación para evitar SQL injection
-- 3. Las consultas de DELETE deben ser ejecutadas con cuidado
-- 4. Para producción, ajustar LIMIT según necesidades
-- 5. Considerar paginación para resultados grandes
--
-- ====================================================================

